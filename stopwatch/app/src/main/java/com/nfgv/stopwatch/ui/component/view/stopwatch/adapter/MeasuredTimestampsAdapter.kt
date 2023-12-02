package com.nfgv.stopwatch.ui.component.view.stopwatch.adapter

import android.content.Context
import android.widget.ArrayAdapter
import com.nfgv.stopwatch.R

class MeasuredTimestampsAdapter(context: Context, elements: List<String>) : ArrayAdapter<String>(
    context,
    R.layout.listview_item,
    elements
)