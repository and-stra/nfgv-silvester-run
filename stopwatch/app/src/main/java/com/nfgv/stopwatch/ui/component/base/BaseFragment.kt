package com.nfgv.stopwatch.ui.component.base

import android.os.Bundle
import android.view.View
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import com.nfgv.stopwatch.util.Constants

abstract class BaseFragment : Fragment() {
    abstract fun observeViewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        observeViewModel()
    }
}