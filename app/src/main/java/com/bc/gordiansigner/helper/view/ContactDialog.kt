package com.bc.gordiansigner.helper.view

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.DialogFragment
import com.bc.gordiansigner.R
import com.bc.gordiansigner.helper.FINGERPRINT_REGEX
import com.bc.gordiansigner.helper.ext.*
import com.bc.gordiansigner.model.KeyInfo
import kotlinx.android.synthetic.main.fragment_contact.*
import java.util.*

class ContactDialog(
    private val keyInfo: KeyInfo?,
    private val onSave: (KeyInfo) -> Unit
) : DialogFragment() {

    companion object {
        const val TAG = "ContactDialog"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_contact, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (keyInfo != null) {
            tvHeader.setText(R.string.update_contact)

            etFingerprint.setText(keyInfo.fingerprint)
            etFingerprint.isEnabled = false
            etFingerprint.setOpacity(0.5f)
            etFingerprint.setTextColor(Color.LTGRAY)

            etAlias.setText(keyInfo.alias)
        } else {
            tvHeader.setText(R.string.create_new_contact)
        }

        etFingerprint.doOnTextChanged { _, _, _, _ -> tvError.gone() }

        btnCancel.setSafetyOnclickListener {
            dismiss()
        }

        btnSave.setSafetyOnclickListener {
            keyInfo?.let {
                it.alias = etAlias.text.toString()
                onSave.invoke(it)
                dismiss()
            } ?: run {
                val regex = Regex(FINGERPRINT_REGEX)
                val fingerprint = etFingerprint.text.toString().toLowerCase(Locale.ENGLISH)
                if (regex.matches(fingerprint)) {
                    val keyInfo = KeyInfo.newDefaultInstance(
                        fingerprint,
                        etAlias.text.toString(),
                        false
                    )
                    onSave.invoke(keyInfo)
                    dismiss()
                } else {
                    tvError.visible()
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.setCanceledOnTouchOutside(false)
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }
}