package org.kontrol.logging

import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread

class LinearExecutor(
    private val modeWhenCallingThreadDies: Mode = Mode.RUN_UNTIL_EMPTY_QUEUE,
    private val modeChangeCheckFrequency: Duration = Duration(5, TimeUnit.SECONDS)
){

    private val callingThread = Thread.currentThread()
    private val queue = LinkedBlockingQueue<() -> Unit>()
    private var currentMode = Mode.RUN

    init {
        thread(name = EXECUTOR_THREAD_NAME) { run() }
        thread(name = "MainThreadFinishedListener") { mainThreadFinishedListener() }
    }

    private fun run(){
        while(currentMode == Mode.RUN){
            queue.poll(modeChangeCheckFrequency.i, modeChangeCheckFrequency.unit)?.invoke()
        }
        while(currentMode == Mode.RUN_UNTIL_EMPTY_QUEUE){
            queue.poll(1, TimeUnit.SECONDS)?.invoke() ?: run { currentMode = Mode.STOP }
        }
    }

    private fun mainThreadFinishedListener(){
        callingThread.join()
        currentMode = modeWhenCallingThreadDies
    }

    fun exec(func: () -> Unit){
        queue.put(func)
    }

    @Suppress("unused")
    fun stop(waitForEmptyQueue: Boolean = true){
        currentMode = if(waitForEmptyQueue) Mode.RUN_UNTIL_EMPTY_QUEUE else Mode.STOP
    }

    enum class Mode{RUN, RUN_UNTIL_EMPTY_QUEUE, STOP}
    data class Duration(val i: Long, val unit: TimeUnit)
    companion object {
        const val EXECUTOR_THREAD_NAME = "LinearExecutor"
    }
}