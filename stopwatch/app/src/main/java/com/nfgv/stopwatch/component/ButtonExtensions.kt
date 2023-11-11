package com.nfgv.stopwatch.component

import android.content.Context.VIBRATOR_MANAGER_SERVICE
import android.content.Context.VIBRATOR_SERVICE
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.Button

fun Button.flash() {
    this.animate().apply {
        interpolator = AccelerateDecelerateInterpolator()
        duration = 75
        alpha(0.075f)
        startDelay = 0
        withEndAction {
            animate().apply {
                interpolator = AccelerateDecelerateInterpolator()
                duration = 75
                alpha(0.0f)
                startDelay = 0
            }
        }
        start()
    }
}

@Suppress("DEPRECATION")
fun Button.triggerVibrate() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        (context.getSystemService(VIBRATOR_MANAGER_SERVICE) as VibratorManager).defaultVibrator.vibrate(
            VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE)
        )
    } else {
        // backwards compatibility for devices with API < 31
        (context.getSystemService(VIBRATOR_SERVICE) as Vibrator).vibrate(50)
    }
}