package com.kyberswap.android.util.ext

import android.arch.lifecycle.MutableLiveData

fun <T : Any> MutableLiveData<List<T>>.addAll(newList: List<T>) {
    val list = mutableListOf<T>()
    value?.let {
        list.addAll(it)
    }
    list.addAll(newList)
    value = list
}