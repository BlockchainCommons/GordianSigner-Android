package com.bc.gordiansigner.helper.view

import android.content.Context
import androidx.annotation.StyleRes
import androidx.appcompat.app.AlertDialog
import com.bc.gordiansigner.R

open class TaggedAlertDialog(
    context: Context,
    val tag: String?,
    @StyleRes theme: Int = R.style.AlertDialogCustom
) : AlertDialog(context, theme)