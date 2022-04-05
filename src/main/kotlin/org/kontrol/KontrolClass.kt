package org.kontrol

import java.lang.reflect.Constructor

data class KontrolClass<CLASS_TYPE: Kontrolled>(
    val clazzWRT: ClassWithResolvedTypes<CLASS_TYPE>,
    val injectableConstructors: List<InjectableConstructor<CLASS_TYPE>>,
    val kontrolledTypesToInject: List<KontrolClass<*>>
){
    val clazz: Class<CLASS_TYPE> get() = clazzWRT.clazz
    @Suppress("unused")
    val constructorsWithArgTypes get() = clazzWRT.constructorsWithArgTypes
    @Suppress("unused")
    val typesToInject            get() = clazzWRT.typesToInject


    data class InjectableConstructor<T>(val argTypes: List<KontrolClass<*>>, val constructor: Constructor<T>){
        override fun toString(): String {
            return "Injectable Constructor for <${constructor.declaringClass}> with ${argTypes.size} arguments: ${argTypes.map { it.clazz.simpleName }}"
        }
    }
}