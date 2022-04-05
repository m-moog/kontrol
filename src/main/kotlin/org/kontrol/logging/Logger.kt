package org.kontrol.logging

interface Logger {

    fun log(level: Level, exception: Throwable? = null, message: () -> String)

    fun debug(exception: Throwable? = null, message: () -> String) = log(Level.DEBUG, exception, message)
    fun info (exception: Throwable? = null, message: () -> String) = log(Level.INFO, exception, message)
    fun warn (exception: Throwable? = null, message: () -> String) = log(Level.WARN, exception, message)
    fun error(exception: Throwable? = null, message: () -> String) = log(Level.ERROR, exception, message)
    fun fatal(exception: Throwable? = null, message: () -> String) = log(Level.FATAL, exception, message)

    enum class Level{
        DEBUG, INFO, WARN, ERROR, FATAL
    }
}