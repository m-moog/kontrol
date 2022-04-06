package org.kontrol

@Suppress("UNCHECKED_CAST")
data class ClassWithResolvedTypes<CLASS_TYPE: Kontrolled> (
    val clazz: Class<CLASS_TYPE>
){
    val constructorsWithArgTypes = getConstructors()
    val typesToInject = resolveTypesToInject()

    private fun getConstructors(): List<Constructor<CLASS_TYPE>> {
        return clazz
            .constructors
            .filter { constructor ->
                constructor.parameterTypes.all { Kontrolled::class.java.isAssignableFrom(it) }
            }
            .map { Constructor(it.parameterTypes.toList(), it) as Constructor<CLASS_TYPE> }
    }

    private fun resolveTypesToInject(): List<Field> {
        return clazz
            .declaredFields
            .filter { it.type == InjectionDelegate::class.java }
            .map { Field(it.type, it.name) }
    }

    data class Constructor<T>(val argTypes: List<Class<*>>, val constructor: java.lang.reflect.Constructor<T>){
        override fun toString(): String {
            return "Constructor for <${constructor.declaringClass}> with ${argTypes.size} arguments: ${argTypes.map { it.simpleName }}"
        }
    }
    data class Field(val type: Class<*>, val name: String)

    companion object{
        fun <CLASS_TYPE: Kontrolled> Class<CLASS_TYPE>.resolveConstructorsAndTypesToInject(): ClassWithResolvedTypes<CLASS_TYPE> {
            return ClassWithResolvedTypes(this)
        }
    }
}