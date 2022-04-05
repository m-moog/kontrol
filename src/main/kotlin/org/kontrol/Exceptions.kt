package org.kontrol

@Suppress("unused")
class FatalKontrolError: Error{
    constructor() : super()
    constructor(message: String) : super(appendSuffix(message))
    constructor(message: String, cause: Throwable) : super(appendSuffix(message), cause)
    constructor(cause: Throwable?) : super(cause)

    companion object{
        fun appendSuffix(input: String): String{
            return input.trimEnd().removeSuffix(".") + SUFFIX
        }

        private val SUFFIX = ". This indicates a possible ${Kontrol::class.simpleName} internal bug."
    }
}

open class KontrolException(
    message: String? = null,
    cause: Throwable? = null
): Throwable(message, cause)

class FieldInjectionException(
    message: String? = null,
    cause: Throwable? = null
): KontrolException(message, cause)

class ConstructorInjectionException(
    message: String? = null,
    cause: Throwable? = null
): KontrolException(message, cause)

class ExecutableException(
    message: String? = null,
    cause: Throwable? = null
): KontrolException(message, cause)