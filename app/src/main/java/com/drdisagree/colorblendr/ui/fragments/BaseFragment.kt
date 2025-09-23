package com.drdisagree.colorblendr.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.drdisagree.colorblendr.ui.viewmodels.ColorsViewModel
import com.drdisagree.colorblendr.ui.viewmodels.StylesViewModel

open class BaseFragment : Fragment() {

    private val colorsViewModel: ColorsViewModel by activityViewModels()
    private val stylesViewModel: StylesViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        colorsViewModel.refreshData()
        stylesViewModel.refreshData()
    }
}