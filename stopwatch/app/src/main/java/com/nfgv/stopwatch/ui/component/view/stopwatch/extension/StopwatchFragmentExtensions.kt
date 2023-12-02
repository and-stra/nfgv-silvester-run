package com.nfgv.stopwatch.ui.component.view.stopwatch.extension

import android.content.res.Configuration
import android.graphics.Color
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.nfgv.stopwatch.ui.component.view.stopwatch.StopwatchFragment
import com.nfgv.stopwatch.util.Constants

fun StopwatchFragment.disableScreenTimeout() {
    activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
}

fun StopwatchFragment.enableScreenTimeout() {
    activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
}

fun StopwatchFragment.setStopTimeButtonBackgroundColor() {
    // TODO maybe possible via xml???
    (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK).also { currentTheme ->
        if (currentTheme == Configuration.UI_MODE_NIGHT_YES) {
            binding.buttonStopTime.setBackgroundColor(Color.WHITE)
        } else {
            binding.buttonStopTime.setBackgroundColor(Color.BLACK)
        }
    }
}

fun StopwatchFragment.setActionBarTitle() {
    (activity as AppCompatActivity).supportActionBar?.title =
        arguments?.getString(Constants.RUN_NAME_KEY).orEmpty()
}