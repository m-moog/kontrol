package org.kontrol.logging

import org.slf4j.LoggerAdapter
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.*
import kotlin.test.currentStackTrace

class KLog(
    private val defaultLevel: Logger.Level = Logger.Level.INFO,
    private val classSpecificLevels: Map<Class<*>, Logger.Level> = mapOf(),

    private val logInSeparateThread: Boolean = true,

    private val timestampFormat: DateFormat =
        SimpleDateFormat("'${Colors.WHITE}'yyyy-MM-dd'${Colors.RESET}' HH:mm:ss.SSS"),

    private val levelColors: Map<Logger.Level, String> = mapOf(
        Logger.Level.DEBUG to Colors.WHITE,
        Logger.Level.INFO to Colors.RESET,
        Logger.Level.WARN to Colors.YELLOW,
        Logger.Level.ERROR to Colors.RED,
        Logger.Level.FATAL to Colors.BR_RED
    ),

    private val codePointColor: String = Colors.CYAN,
    private val threadNameColor: String = Colors.MAGENTA,
    private val separatorColor: String = Colors.WHITE,
    private val fatalMainColor: String = Colors.BR_RED,

    private val levelTextWidth       : Int = 5,
    private val codeLocationTextWidth: Int = 50,
    private val codeLocationLineWidth: Int = 5,
    private val threadNameTextWidth  : Int = 10
) : Logger {

    private val executor by lazy { LinearExecutor() }

    override fun log(level: Logger.Level, exception: Throwable?, message: () -> String) {
        val stackTrace = currentStackTrace()
        val thread = Thread.currentThread()
        if(logInSeparateThread){
            executor.exec { log(stackTrace, level, exception, thread, message) }
        }else{
            log(stackTrace, level, exception, thread, message)
        }
    }

    private fun log(stackTrace: Array<out StackTraceElement>, level: Logger.Level, exception: Throwable?, thread: Thread, message: () -> String) {
        val originCodePoint = OriginCodePoint(
            stackTrace = stackTrace,
            codePointColor = codePointColor,
            codeLocationTextWidth = codeLocationTextWidth,
            codeLocationLineWidth = codeLocationLineWidth
        )
        if(originCodePoint.qualifiedName.contains("Ref")){
            kotlin.run {  }
        }
        val actualLevel = classSpecificLevels[originCodePoint.qualifiedName] ?: defaultLevel
        if(actualLevel > level) return

        val log = "${getTimestamp()} ${Colors.GRAY}at${Colors.RESET} ${originCodePoint.fullLocationStr} " +
                "[${formatThreadName(thread)}] ${level.printedName()} ${separatorColor}---${Colors.RESET} " +
                "${fatalFormatting(level)}${message.invoke()}${Colors.RESET}" +
                (exception?.let {"\nCaused by:\n${it.stackTraceToString()}"} ?: "")

        println(log)
    }

    private fun getTimestamp(): String{
        return timestampFormat.format(Date.from(Instant.now()))
    }

    private fun Logger.Level.printedName(): String{
        return (levelColors[this] ?: Colors.RESET) + (this.name.adjustToLength(levelTextWidth, "..")) + Colors.RESET
    }

    private fun formatThreadName(thread: Thread): String {
        return threadNameColor +
                thread.name.adjustToLength(
                    desiredLength = threadNameTextWidth, tooLongPrefix = "",
                    tooLongSuffix = "...", appendOnLeft = false, cutOffOnLeft = false
                ) +
                Colors.RESET
    }

    private fun fatalFormatting(level: Logger.Level): String {
        return if(level == Logger.Level.FATAL) fatalMainColor else ""
    }
}

@Suppress("MemberVisibilityCanBePrivate")
class OriginCodePoint(
    stackTrace: Array<out StackTraceElement>,
    codePointColor: String = "",
    codeLocationTextWidth: Int,
    codeLocationLineWidth: Int
){
    val simpleName: String
    val qualifiedName: String
    val lineNumberStr: String
    val fullLocationStr: String

    init {
        val element = stackTrace.firstOrNull { it.shouldBeConsideredForOrigin() }
        lineNumberStr = (element?.lineNumber ?: -1).toString()

        qualifiedName = element?.className?.split("$")?.get(0) ?: "???"
        simpleName = qualifiedName.split(".").last()
        val fileName = element?.fileName ?: "???"

        fullLocationStr =
            codePointColor +
                    ("${qualifiedName}.${element?.methodName}(${fileName}").adjustToLength(codeLocationTextWidth, "[...]", "") +
                    ":" +
                    ("$lineNumberStr)").adjustToLength(codeLocationLineWidth, tooLongSuffix = "...", appendOnLeft = false) +
                    Colors.RESET
    }

    private fun StackTraceElement.shouldBeConsideredForOrigin(): Boolean {
        if (isNativeMethod) return false
        val className = className.split("$")[0]

        return className != KLog::class.qualifiedName
                && className != Logger::class.qualifiedName
                && className != LinearExecutor::class.qualifiedName
                && className != LoggerAdapter::class.qualifiedName
                && className != "kotlin.concurrent.ThreadsKt"
    }
}

@Suppress("unused")
object Colors{
    val BLACK  : String get() = get("30m")
    val RED    : String get() = get("31m")
    val GREEN  : String get() = get("32m")
    val YELLOW : String get() = get("33m")
    val BLUE   : String get() = get("34m")
    val MAGENTA: String get() = get("35m")
    val CYAN   : String get() = get("36m")
    val WHITE  : String get() = get("37m")

    val GRAY      : String get() = get("90m")
    val BR_RED    : String get() = get("91m")
    val BR_GREEN  : String get() = get("92m")
    val BR_YELLOW : String get() = get("93m")
    val BR_BLUE   : String get() = get("94m")
    val BR_MAGENTA: String get() = get("95m")
    val BR_CYAN   : String get() = get("96m")
    val BR_WHITE  : String get() = get("97m")

    val RESET : String get() = get("0m")

    private fun get(code: String): String{
        return if(consoleSupportsColor()) "\u001B[$code" else ""
    }

    private fun consoleSupportsColor() = true
}

private fun String.adjustToLength(
    desiredLength: Int,
    tooLongPrefix: String = "",
    tooLongSuffix: String = "[...]",
    appendOnLeft: Boolean = true,
    cutOffOnLeft: Boolean = true
): String {
    return if (this.length > desiredLength) {

        tooLongPrefix +

                this.substring(
                    if(cutOffOnLeft) length - desiredLength + tooLongPrefix.length else tooLongPrefix.length,
                    if(cutOffOnLeft) desiredLength - tooLongSuffix.length + (length - desiredLength) else length - (length - desiredLength) - tooLongSuffix.length
                ) +

                tooLongSuffix

    } else {
        if(appendOnLeft){
                   " ".repeat(desiredLength - this.length) + this
        }else{
            this + " ".repeat(desiredLength - this.length)
        }
    }
}

@Suppress("unused")
private operator fun <T> Map<Class<*>, T>.get(qualifiedName: String): T?{
    return this.entries.firstOrNull { it.key.name == qualifiedName }?.value
}