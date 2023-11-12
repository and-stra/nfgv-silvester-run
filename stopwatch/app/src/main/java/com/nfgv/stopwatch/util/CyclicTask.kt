package com.nfgv.stopwatch.util

import java.util.Timer
import java.util.TimerTask

class CyclicTask(private val interval: Long) {
    private val timer: Timer = Timer()

    fun start(task: Runnable) {
        val timerTask = object : TimerTask() {
            override fun run() {
                task.run()
            }
        }

        timer.scheduleAtFixedRate(timerTask, 0, interval)
    }

    fun cancel() {
        timer.cancel()
    }

    fun stop() {
        timer.cancel()
        timer.purge()
    }
}