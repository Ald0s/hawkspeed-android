package com.vljx.hawkspeed.models

import androidx.annotation.LayoutRes

interface ListItemViewModel {
    @get:LayoutRes
    val layoutId: Int
    val viewType: Int
        get() = 0

    fun areViewModelsTheSame(listItemViewModel: ListItemViewModel): Boolean
    fun areViewModelContentsTheSame(listItemViewModel: ListItemViewModel): Boolean
}