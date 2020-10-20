package com.bc.gordiansigner.helper.livedata

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData

class CompositeLiveData<T> {

    private val mediatorLiveData = MediatorLiveData<Resource<T>>()

    fun asLiveData(): LiveData<Resource<T>> = mediatorLiveData

    fun add(source: LiveData<Resource<T>>) {
        mediatorLiveData.addSource(source) { r -> mediatorLiveData.value = r }
    }

    fun remove(source: LiveData<Resource<T>>) {
        mediatorLiveData.removeSource(source)
    }
}