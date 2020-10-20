package com.bc.gordiansigner.helper.livedata

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.MutableLiveData
import java.util.*

class BufferedLiveData<T>(private val lifecycle: Lifecycle) :
    MutableLiveData<T>(), LifecycleObserver {

    private val buffer = LinkedList<T>()

    init {
        lifecycle.addObserver(this)
    }

    override fun onActive() {
        if (buffer.isEmpty()) {
            super.onActive()
        } else {
            while (buffer.isNotEmpty()) {
                setValue(buffer.pop())
            }
        }
    }

    override fun setValue(value: T) {
        if (lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
            super.setValue(value)
        } else {
            buffer.add(value)
        }
    }
}