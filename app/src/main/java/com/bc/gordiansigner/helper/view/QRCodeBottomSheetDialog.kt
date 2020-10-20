package com.bc.gordiansigner.helper.view

import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bc.gordiansigner.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.fragment_qr_code.*

class QRCodeBottomSheetDialog(private val qrBitmap: Bitmap) : BottomSheetDialogFragment() {

    companion object {
        const val TAG = "QrCodeBottomSheetDialog"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_qr_code, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ivQRCode.setImageBitmap(qrBitmap)
    }

}