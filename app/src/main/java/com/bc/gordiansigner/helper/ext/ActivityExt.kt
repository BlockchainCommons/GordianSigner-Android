package com.bc.gordiansigner.helper.ext

import android.app.Activity
import android.content.Context
import android.view.inputmethod.InputMethodManager

fun Activity.hideKeyBoard() {
    val view = this.currentFocus
    if (null != view) {
        val inputManager =
            getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        inputManager?.hideSoftInputFromWindow(view.windowToken, 0)
    }

}