package com.kyberswap.android.util.ext

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData

fun <T : Any> MutableLiveData<List<T>>.addAll(newList: List<T>) {
    val list = mutableListOf<T>()
    value?.let {
        list.addAll(it)
    }
    list.addAll(newList)
    value = list
}


fun <T, A, B> LiveData<A>.combineAndCompute(
    other: LiveData<B>,
    onChange: (A?, B?) -> T
): MediatorLiveData<T> {

    var source1emitted = false
    var source2emitted = false

    val result = MediatorLiveData<T>()

    val mergeF = {
        val source1Value = this.value
        val source2Value = other.value

        result.value = onChange.invoke(source1Value, source2Value)
    }

    result.addSource(this) { source1emitted = true; mergeF.invoke() }
    result.addSource(other) { source2emitted = true; mergeF.invoke() }

    return result
}