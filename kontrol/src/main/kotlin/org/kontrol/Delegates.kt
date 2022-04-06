package org.kontrol

import kotlin.reflect.KClass
import kotlin.reflect.KProperty

fun injection(): InjectionDelegate {
    return InjectionDelegate()
}

class InjectionDelegate internal constructor(){
    var instance: Kontrolled? = null
    val thread: Thread = Thread.currentThread()

    inline operator fun <BEAN_TYPE: Kontrolled, reified INJECTED_FIELD_TYPE: Kontrolled>
            getValue(bean: BEAN_TYPE, property: KProperty<*>): INJECTED_FIELD_TYPE {

        try {
            val type = (property.returnType.classifier?.let { it as KClass<*> } ) ?: INJECTED_FIELD_TYPE::class

            if(type == Any::class || type == Object::class){
                throw FieldInjectionException(
                    "Injecting field of type <${type.qualifiedName}> is not supported")
            }

            Kontrol.logger.debug { "Resolving injected property '${bean::class.qualifiedName}#" +
                    "${property.name}' of type <${type.qualifiedName}>" }

            if(instance == null) {
                instance = Kontrol.findInstance(type.java, thread)
            }

            return instance as INJECTED_FIELD_TYPE

        } catch (e: Throwable) {
            val type = (property.returnType.classifier?.let { it as KClass<*> } ) ?: INJECTED_FIELD_TYPE::class

            throw FieldInjectionException("Failed to inject field '${property.name}' of type " +
                    "<${type.qualifiedName}> into class <${bean.javaClass.name}>",
                e
            )
        }
    }
}