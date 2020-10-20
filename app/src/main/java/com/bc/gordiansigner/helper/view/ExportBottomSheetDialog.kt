package com.bc.gordiansigner.helper.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bc.gordiansigner.R
import com.bc.gordiansigner.helper.ext.gone
import com.bc.gordiansigner.helper.ext.setSafetyOnclickListener
import com.bc.gordiansigner.helper.ext.visible
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.fragment_export.*

class ExportBottomSheetDialog(
    private val isFileVisible: Boolean = true,
    private val listener: OnItemSelectedListener
) : BottomSheetDialogFragment() {

    companion object {
        const val TAG = "ExportBottomSheetDialog"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_export, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tvCopy.setSafetyOnclickListener {
            listener.onCopy()
            dismiss()
        }

        tvQRCode.setSafetyOnclickListener {
            listener.onShowQR()
            dismiss()
        }

        if (isFileVisible) {
            tvFile.visible()
            tvFile.setSafetyOnclickListener {
                listener.onSaveFile()
                dismiss()
            }
        } else {
            tvFile.gone()
        }
    }

    interface OnItemSelectedListener {
        fun onCopy()
        fun onShowQR()
        fun onSaveFile()
    }
}