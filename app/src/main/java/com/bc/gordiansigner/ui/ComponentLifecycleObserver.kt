package com.bc.gordiansigner.ui

import android.content.Intent

interface ComponentLifecycleObserver {

    fun onCreate() {}

    fun onStart() {}

    fun onResume() {}

    fun onPause() {}

    fun onStop() {}

    fun onDestroy() {}

    fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
    }

}