@file:Suppress("UNCHECKED_CAST")

package org.kontrol

import org.kontrol.ClassWithResolvedTypes.Companion.resolveConstructorsAndTypesToInject
import org.kontrol.logging.KLog
import org.kontrol.logging.LinearExecutor
import org.kontrol.logging.Logger
import org.reflections.Reflections
import org.reflections.scanners.Scanners
import org.reflections.util.ConfigurationBuilder
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Modifier
import kotlin.reflect.KClass
import kotlin.reflect.full.declaredMemberProperties
import kotlin.test.currentStackTrace

private var conf: KontrolConfig? = null

fun kontrol(contextSupplier: KontrolConfigurationContext.() -> Unit = {}){
    conf = KontrolConfigurationContext().also(contextSupplier).config
    try {
        Kontrol
    }catch(e: Throwable){
        Kontrol.ErrorHandler.handleTopMostException(e, conf?.logger ?: KLog())
    }
}

class KontrolConfigurationContext{
    lateinit var packages: Array<String>
    var logger: Logger = KLog()

    val config: KontrolConfig
        get() {
        return KontrolConfig(packages.asList(), logger)
    }
}

data class KontrolConfig(val packages: List<String>, val logger: Logger)

object Kontrol{
    private val config = conf ?: ErrorHandler.handleConfigNotPresent()
    val logger = config.logger

    private val reflections = Reflections(
        ConfigurationBuilder()
            .setScanners(Scanners.TypesAnnotated, Scanners.SubTypes)
            .forPackages(*config.packages.toTypedArray())
    )

    private val classesWithResolvedTypes: Set<ClassWithResolvedTypes<*>>

    private val kontrolledClasses: MutableSet<KontrolClass<out Kontrolled>> = mutableSetOf()
    private val kontrolledInstances: MutableMap<Thread, MutableMap<KontrolClass<out Kontrolled>, Kontrolled>> =
        mutableMapOf(Thread.currentThread() to mutableMapOf())

    init {
        logger.info{ "Starting Framework. Starting scan for packages ${config.packages}" }

        classesWithResolvedTypes = reflections.getSubTypesOf(Kontrolled::class.java)
            .filter { !Modifier.isAbstract(it.modifiers) }
            .map { it.resolveConstructorsAndTypesToInject() }
            .toSet()

        logger.info { "Found ${classesWithResolvedTypes.size} ${Kontrolled::class.simpleName} classes: " +
                    "${classesWithResolvedTypes.map { it.clazz.name }}" }

        for(clazzWRT in classesWithResolvedTypes){
            resolveFullDependencyTree(clazzWRT)
        }

        logger.debug { "Resolved full dependency trees for ${kontrolledClasses.size} classes: " +
                    "${kontrolledClasses.map { it.clazz.name }}" }

        executeExecutables()
    }

    private fun <T: Kontrolled> resolveFullDependencyTree(clazzWRT: ClassWithResolvedTypes<T>): KontrolClass<T> {
        kontrolledClasses.firstOrNull { clazzWRT == it.clazzWRT }
            ?.let { return it as KontrolClass<T> }

        logger.debug { "Resolving full dependency tree for class <${clazzWRT.clazz.name}>" }

        val injectableConstructors = resolveConstructorDependencies(clazzWRT)
        val kontrolledTypesToInject = resolveInjectedFieldDependencies(clazzWRT)

        logger.debug { "Successfully resolved full dependency tree for class <${clazzWRT.clazz.name}>" }

        return KontrolClass(clazzWRT, injectableConstructors, kontrolledTypesToInject).also(kontrolledClasses::add)
    }

    private fun <T: Kontrolled> resolveConstructorDependencies(clazzWRT: ClassWithResolvedTypes<T>): List<KontrolClass.InjectableConstructor<T>> {
        logger.debug { "Resolving constructor dependencies for class <${clazzWRT.clazz.name}> with ${clazzWRT.constructorsWithArgTypes.size} constructors" }//+

        return clazzWRT
            .constructorsWithArgTypes
            .map { constructor ->
                logger.debug { "Resolving constructor <$constructor>" }
                constructor.argTypes.map { arg -> classesWithResolvedTypes.findByClass(arg as Class<Kontrolled>) }
                    .map {
                        logger.debug { "Resolving constructor parameter of type <${it.clazz.name}>" }
                        resolveFullDependencyTree(it)
                            .also { _ -> logger.debug { "Successfully resolved constructor parameter of type <${it.clazz.name}>" } }
                    }
                    .let { KontrolClass.InjectableConstructor(it, constructor.constructor) }
                    .also { logger.debug { "Successfully resolved constructor <$constructor>" } }
            }
    }

    private fun resolveInjectedFieldDependencies(clazzWRT: ClassWithResolvedTypes<out Kontrolled>): List<KontrolClass<*>> {
        logger.debug { "Resolving ${clazzWRT.typesToInject.size} injected field dependencies " +
                    "for class <${clazzWRT.clazz.name}>: ${clazzWRT.typesToInject.map { it.name }}" }

        return clazzWRT
            .typesToInject
            .asSequence()
            .map { it.name.split("$")[0] }
            .map { name ->
                logger.debug { "Resolving KProperty '$name'" }
                val kClass = clazzWRT.clazz.kotlin.declaredMemberProperties.first { it.name == name }
                    .returnType.classifier as KClass<*>
                val jClass = kClass.java as Class<Kontrolled>
                logger.debug { "Resolved KProperty '$name' to be of type <${jClass.name}>" }
                return@map jClass
            }
            .map { classesWithResolvedTypes.findByClass(it) }
            .map {
                logger.debug { "Resolving injected field of type <${it.clazz.name}>" }
                resolveFullDependencyTree(it)
                    .also { _ -> logger.debug { "Successfully resolved injected field of type <${it.clazz.name}>" } }
            }
            .toList()
    }

    private fun <T: Kontrolled> Set<ClassWithResolvedTypes<*>>.findByClass(clazz: Class<T>): ClassWithResolvedTypes<T> {
        return first { clazz.isAssignableFrom(it.clazz) } as ClassWithResolvedTypes<T>
    }

    private fun executeExecutables() {
        logger.debug { "" }
        logger.debug { "Executing all classes that implement ${Executable::class.simpleName}" }
        classesWithResolvedTypes
            .filter { Executable::class.java.isAssignableFrom(it.clazz) }
            .also { executables -> logger.debug { "Executing ${executables.size} classes: ${executables.map { it.clazz.simpleName }}" } }
            .forEach {
                val executable = findInstance(it.clazz, Thread.currentThread()) as Executable
                try {
                    executable.execute()
                }catch(e: Throwable){
                    ErrorHandler.handleExecutableExecutingException(it, e)
                }
            }
    }

    inline fun <reified T: Kontrolled> findInstance(thread: Thread): T{
        return findInstance(T::class.java, thread)
    }

    fun <T> findInstance(clazz: Class<*>, thread: Thread): T{
        return findInstance(kontrolledClasses.first { clazz.isAssignableFrom(it.clazz) }, thread) as T
    }

    @Suppress("MemberVisibilityCanBePrivate")
    fun <T: Kontrolled> findInstance(type: KontrolClass<T>, thread: Thread): T{
        if(!kontrolledClasses.contains(type)){
            ErrorHandler.handleNonKontrolClass(type, thread)
        }
        kontrolledInstances[thread]?.get(type)?.let { return it as T }

        return instantiate(type, thread)
    }

    private fun <T: Kontrolled> instantiate(type: KontrolClass<T>, thread: Thread): T {
        logger.debug { "Instantiating class of type <${type.clazz}> for thread <${thread.name}>" }

        logger.debug { "Retrieving injected fields. Injected fields are of types ${type.kontrolledTypesToInject.map { it.clazz.name }}" }
        type.kontrolledTypesToInject.forEach { findInstance(it, thread) }

        val constructorToUse =
            type.injectableConstructors.minByOrNull { it.argTypes.size } ?: ErrorHandler.handleNoConstructorsFound(type)

        logger.debug { "Found constructor to use: $constructorToUse" }

        if(constructorToUse.argTypes.isEmpty()){
            return createAndRegisterInstance(type, constructorToUse, thread) { constructorToUse.constructor.newInstance() }
        }

        val args =
        constructorToUse.argTypes
            .map { findInstance(it, thread) }

        return createAndRegisterInstance(type, constructorToUse, thread) { constructorToUse.constructor.newInstance(*args.toTypedArray()) }
    }

    private fun <T: Kontrolled> createAndRegisterInstance(
        type: KontrolClass<T>,
        constructor: KontrolClass.InjectableConstructor<T>,
        thread: Thread,
        instanceSupplier: () -> T
    ): T{
        try {
            return registerInstance(type, instanceSupplier.invoke(), thread)
                .also { logger.debug { "Successfully instantiated and registered class of type <${type.clazz.name}> for thread <${thread.name}>" } }
        }catch(e: Throwable){
            ErrorHandler.handleInstanceCreationException(e, type, constructor)
        }
    }

    private fun <T: Kontrolled> registerInstance(type: KontrolClass<T>, instance: T, thread: Thread): T{
        kontrolledInstances[thread]?.apply { put(type, instance) } ?: ErrorHandler.handleNonKontrolThread(thread)
        return instance
    }

    object ErrorHandler{

        fun handleTopMostException(thrownException: Throwable, logger: Logger){
            val actualException: Throwable =
                if(thrownException is ExceptionInInitializerError) {
                    thrownException.cause ?: thrownException
                } else {
                    thrownException
                }

            var isFatal = actualException !is KontrolException

            var nextCause = actualException.cause
            while(nextCause?.cause != null){
                if(nextCause is FatalKontrolError){
                    isFatal = true
                }
                nextCause = nextCause.cause
            }

            if(isFatal){
                logger.fatal(actualException) { "Fatal Unexpected Error in ${Kontrol::class.simpleName}" }
            }
            else {
                logger.error(actualException) { "An Exception occurred" }
            }
        }

        fun handleConfigNotPresent(): Nothing {
            throw NullPointerException(
                "${KontrolConfig::class.simpleName} '${::conf.name}' has not yet been initialized. " +
                        "Make sure the Framework has been started properly " +
                        "before trying to access the ${Kontrol::class.simpleName} instance."
            )
        }

        data class InstanceCreationExceptionMetadata(
            val e: Throwable, val type: KontrolClass<*>, val constructor: KontrolClass.InjectableConstructor<*>
        )
        fun handleInstanceCreationException(
            e: Throwable, type: KontrolClass<*>, constructor: KontrolClass.InjectableConstructor<*>
        ): Nothing{
            val metadata = InstanceCreationExceptionMetadata(e, type, constructor)
            throw ConstructorInjectionException(
                "Failed to instantiate class <${type.clazz.name}> using constructor <$constructor>",

                when (e) {

                    is IllegalAccessException      -> handleIllegalAccessException     (metadata)
                    is IllegalArgumentException    -> handleIllegalArgumentException   (metadata)
                    is InstantiationException      -> handleInstantiationException     (metadata)
                    is InvocationTargetException   -> handleInvocationTargetException  (metadata)
                    is ExceptionInInitializerError -> handleExceptionInInitializerError(metadata)

                    else                           -> handleUnexpectedException        (metadata)
                }
            )
        }

        private fun handleIllegalAccessException(metadata: InstanceCreationExceptionMetadata): Throwable {
            return FatalKontrolError(
                "No permission to invoke constructor. Set the constructor to public to allow " +
                        "${Kontrol::class.simpleName} to access it. This should not occur, since " +
                        "${Class::class.simpleName}.${Class<*>::getConstructors.name}() does not return private constructors.",
                metadata.e
            )
        }

        private fun handleIllegalArgumentException(metadata: InstanceCreationExceptionMetadata): Throwable {
            return FatalKontrolError(
                "The constructor was passed the wrong arguments",
                metadata.e
            )
        }

        private fun handleInstantiationException(metadata: InstanceCreationExceptionMetadata): Throwable {
            return FatalKontrolError(
                "The given class can not be instantiated. Non-instantiable classes should have been filtered out.",
                metadata.e
            )
        }

        private fun handleInvocationTargetException(metadata: InstanceCreationExceptionMetadata): Throwable {
            return ConstructorInjectionException(
                "Constructor threw exception",
                metadata.e
            )
        }

        private fun handleExceptionInInitializerError(metadata: InstanceCreationExceptionMetadata): Throwable {
            return ConstructorInjectionException(
                "Initializer threw exception",
                metadata.e
            )
        }

        private fun handleUnexpectedException(metadata: InstanceCreationExceptionMetadata): Throwable {
            return FatalKontrolError(
                "An exception of type <${metadata.e::class.qualifiedName}> was thrown, which has no specific handling.",
                metadata.e
            )
        }

        fun handleExecutableExecutingException(it: ClassWithResolvedTypes<*>, e: Throwable) {
            throw ExecutableException(
                "Failed to execute ${Executable::class.simpleName} of type <${it.clazz.name}>", e
            )
        }

        fun <T: Kontrolled> handleNoConstructorsFound(type: KontrolClass<T>): Nothing {
            throw ConstructorInjectionException(
                "Failed to find suitable constructor to instantiate <${type.clazz.name}> using constructor injection. " +
                        "Available constructors are: ${
                            type.clazz.constructors
                                .map {
                                    ClassWithResolvedTypes.Constructor(it.parameterTypes.toList(), it) 
                                }
                        }"
            )
        }

        fun handleNonKontrolThread(thread: Thread) {
            val origin = currentStackTrace()
                .filter { it.className.split("$")[0] != Kontrol::class.qualifiedName }
                .first  { it.className.split("$")[0] != ErrorHandler ::class.qualifiedName }
            logger.error {
                "Attempted registering an instance for non-framework thread <$thread>. \n" +
                        "The created instance can therefore not be saved, and repeated dependency injection will result " +
                        "in repeated creation of additional instances. It is highly recommended to fix this issue.\nIf you " +
                        "want to utilize threading, use threading systems provided by the framework.\n" +
                        (if(thread.name == LinearExecutor.EXECUTOR_THREAD_NAME)
                            "Note that the ${KLog::class.simpleName} may be configured to run logging in a " +
                                    "separate thread using the ${LinearExecutor::class.simpleName}."
                        else "") +
                        "Detected origin at ${origin.className.split("$")[0]}.${origin.methodName}(${origin.fileName}:${origin.lineNumber})"
            }
        }

        fun handleNonKontrolClass(type: KontrolClass<*>, thread: Thread): Nothing {
            throw KontrolException("Attempted to find instance for non Framework Class <${type.clazz.name}> for thread <$thread>")
        }
    }
}

interface Kontrolled
interface Executable: Kontrolled { fun execute() }