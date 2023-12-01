package com.nfgv.stopwatch.ui.component.view.home.adapter

import android.content.Context
import android.widget.ArrayAdapter
import android.widget.Filter
import com.nfgv.stopwatch.R

class StopperIdAdapter(context: Context) : ArrayAdapter<String>(
    context,
    R.layout.dropdown_item,
    context.resources.getStringArray(R.array.home_stopper_id_items)
) {
    override fun getFilter(): Filter {
        return EmptyFilter()
    }

    private class EmptyFilter : Filter() {
        override fun performFiltering(constraint: CharSequence?): FilterResults {
            return FilterResults().apply {
                values = constraint
                count = constraint?.length ?: 0
            }
        }

        override fun publishResults(constraint: CharSequence?, results: FilterResults?) {}
    }
}