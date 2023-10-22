package com.nfgv.stopwatch.component

import android.content.Context
import android.widget.Toast

class OnscreenNotification(context: Context, resId: Int) : Toast(context) {
    init {
        makeText(context, resId, LENGTH_SHORT).show()
    }
}