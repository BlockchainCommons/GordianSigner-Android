package com.bc.gordiansigner.ui.sign

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.util.Base64
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.biometric.BiometricManager
import androidx.core.content.FileProvider
import androidx.lifecycle.Observer
import com.bc.gordiansigner.R
import com.bc.gordiansigner.helper.Error.HD_KEY_NOT_MATCH_ERROR
import com.bc.gordiansigner.helper.Error.PSBT_UNABLE_TO_SIGN_ERROR
import com.bc.gordiansigner.helper.KeyStoreHelper
import com.bc.gordiansigner.helper.ext.*
import com.bc.gordiansigner.helper.view.ExportBottomSheetDialog
import com.bc.gordiansigner.helper.view.QRCodeBottomSheetDialog
import com.bc.gordiansigner.model.KeyInfo
import com.bc.gordiansigner.ui.BaseAppCompatActivity
import com.bc.gordiansigner.ui.DialogController
import com.bc.gordiansigner.ui.Navigator
import com.bc.gordiansigner.ui.Navigator.Companion.RIGHT_LEFT
import com.bc.gordiansigner.ui.account.AccountsActivity
import com.bc.gordiansigner.ui.account.add_account.AddAccountActivity
import com.bc.gordiansigner.ui.scan.QRScannerActivity
import com.tbruyelle.rxpermissions2.RxPermissions
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_psbt_sign.*
import java.io.File
import javax.inject.Inject

class PsbtSignActivity : BaseAppCompatActivity() {

    companion object {
        private const val TAG = "PsbtSignActivity"
        private const val REQUEST_CODE_QR_PSBT = 0x02
        private const val REQUEST_CODE_BROWSE = 0x03
        private const val REQUEST_CODE_INPUT_KEY = 0x04
    }

    @Inject
    internal lateinit var viewModel: PsbtSignViewModel

    @Inject
    internal lateinit var navigator: Navigator

    @Inject
    internal lateinit var dialogController: DialogController

    private lateinit var currentKeyInfo: KeyInfo
    private var export = false
    private val compositeDisposable = CompositeDisposable()

    override fun layoutRes() = R.layout.activity_psbt_sign

    override fun viewModel() = viewModel

    override fun initComponents() {
        super.initComponents()

        supportActionBar?.title = ""
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_qr_code_24)

        buttonNext.setSafetyOnclickListener {
            if (export) {
                val dialog = ExportBottomSheetDialog(listener = object :
                    ExportBottomSheetDialog.OnItemSelectedListener {
                    override fun onCopy() {
                        this@PsbtSignActivity.copyToClipboard(editText.text.toString())
                        Toast.makeText(
                            this@PsbtSignActivity,
                            getString(R.string.copied),
                            Toast.LENGTH_LONG
                        ).show()
                    }

                    override fun onShowQR() {
                        val qrDialog =
                            QRCodeBottomSheetDialog(editText.text.toString(), animateEnabled = true)
                        qrDialog.show(supportFragmentManager, QRCodeBottomSheetDialog.TAG)
                    }

                    override fun onSaveFile() {
                        savePsbtFileAndShare(editText.text.toString())
                    }
                })
                dialog.show(supportFragmentManager, ExportBottomSheetDialog.TAG)
            } else {
                signPsbt()
            }
        }
    }

    private fun signPsbt(keyInfo: KeyInfo? = null, seed: String? = null) {
        val psbt = editText.text.toString()
        if (psbt.isBlank()) return

        if (keyInfo != null) {
            currentKeyInfo = keyInfo
            dialogController.confirm(
                getString(R.string.confirm_to_sign_psbt),
                getString(
                    R.string.please_review_your_info_before_signing,
                    keyInfo.fingerprint,
                    keyInfo.alias
                ),
                positive = getString(R.string.sign),
                positiveEvent = {
                    viewModel.signPsbt(psbt, keyInfo, seed)
                }
            )
        } else {
            viewModel.getKeyToSign(psbt)
        }
    }

    override fun observe() {
        super.observe()

        viewModel.psbtSigningLiveData.asLiveData().observe(this, Observer { res ->
            when {
                res.isSuccess() -> {
                    res.data()?.let { base64 ->
                        editText.setText(base64)
                        buttonNext.setText(R.string.export)
                        export = true
                        dialogController.alert(
                            R.string.psbt_signed,
                            R.string.you_may_now_export_it_by_tapping_the_export_button
                        )
                    }
                }

                res.isError() -> {
                    if (!KeyStoreHelper.handleKeyStoreError(
                            applicationContext,
                            res.throwable()!!,
                            dialogController,
                            navigator,
                            authRequiredCallback = {
                                KeyStoreHelper.biometricAuth(
                                    this,
                                    R.string.auth_required,
                                    R.string.auth_for_signing,
                                    successCallback = {
                                        viewModel.signPsbt(
                                            editText.text.toString(),
                                            currentKeyInfo,
                                            null
                                        )
                                    },
                                    failedCallback = { code ->
                                        if (code == BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED) {
                                            navigator.anim(RIGHT_LEFT).enrollDeviceSecurity()
                                        } else {
                                            Log.e(TAG, "Biometric auth failed with code: $code")
                                        }
                                    })
                            })
                    ) {
                        val message = when (res.throwable()!!) {
                            PSBT_UNABLE_TO_SIGN_ERROR -> R.string.psbt_is_unable_to_sign
                            HD_KEY_NOT_MATCH_ERROR -> R.string.your_account_does_not_match_with_current_psbt
                            else -> R.string.unable_to_sign_psbt_unknown_error
                        }
                        dialogController.alert(
                            R.string.error,
                            message
                        )
                    }
                }
            }
        })

        viewModel.keyToSignCheckingLiveData.asLiveData().observe(this, Observer { res ->
            when {
                res.isSuccess() -> {
                    res.data()?.let { keyInfo ->
                        if (!keyInfo.isEmpty() && keyInfo.isSaved) {
                            signPsbt(keyInfo, null)
                        } else {
                            val bundle = AddAccountActivity.getBundle(keyInfo)
                            navigator.anim(RIGHT_LEFT).startActivityForResult(
                                AddAccountActivity::class.java,
                                REQUEST_CODE_INPUT_KEY,
                                bundle
                            )
                        }
                    }
                }

                res.isError() -> {
                    val message = when (res.throwable()!!) {
                        PSBT_UNABLE_TO_SIGN_ERROR -> R.string.psbt_is_unable_to_sign
                        HD_KEY_NOT_MATCH_ERROR -> R.string.your_account_does_not_match_with_current_psbt
                        else -> R.string.unable_to_sign_psbt_unknown_error
                    }
                    dialogController.alert(
                        R.string.error,
                        message
                    )
                }
            }
        })

        viewModel.psbtCheckingLiveData.asLiveData().observe(this, Observer { res ->
            when {
                res.isSuccess() -> {
                    res.data()?.let { (joinedSigners, psbt) ->
                        if (psbt.signable) {
                            dialogController.alert(
                                getString(R.string.valid_psbt),
                                getString(
                                    R.string.psbt_info,
                                    psbt.signatures.size,
                                    joinedSigners.joinToString {
                                        "\n\t\uD83D\uDD11 ${if (it.alias.isNotEmpty()) {
                                            getString(
                                                R.string.fingerprint_alias_format,
                                                it.fingerprint,
                                                it.alias
                                            )
                                        } else {
                                            it.fingerprint
                                        }}"
                                    }
                                )
                            )
                        } else {
                            editText.setText("")
                            dialogController.alert(
                                R.string.warning,
                                R.string.the_psbt_has_been_fully_signed
                            )
                        }
                    }
                }

                res.isError() -> {
                    editText.setText("")
                    dialogController.alert(R.string.error, R.string.invalid_psbt)
                }
            }
        })

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.common_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                val bundle = QRScannerActivity.getBundle(isUR = true)
                navigator.anim(RIGHT_LEFT)
                    .startActivityForResult(
                        QRScannerActivity::class.java,
                        REQUEST_CODE_QR_PSBT,
                        bundle
                    )
            }
            R.id.action_import -> {
                navigator.browseDocument(requestCode = REQUEST_CODE_BROWSE)
            }
            R.id.action_paste -> {
                this.pasteFromClipBoard()?.let {
                    checkPsbt(it)
                } ?: dialogController.alert(R.string.error, R.string.clipboard_is_empty)
            }
            R.id.action_signer -> {
                navigator.anim(RIGHT_LEFT).startActivity(AccountsActivity::class.java)
            }
        }

        return super.onOptionsItemSelected(item)
    }

    override fun deinitComponents() {
        compositeDisposable.dispose()
        super.deinitComponents()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_CODE_QR_PSBT -> {
                    data?.let {
                        val base64 = QRScannerActivity.extractResultData(it)
                        checkPsbt(base64)
                    }
                }
                REQUEST_CODE_BROWSE -> {
                    data?.data?.let { uri ->
                        progressBar.visible()
                        Single.fromCallable {
                            val bytes = contentResolver.openInputStream(uri)?.readBytes()
                            Base64.encodeToString(bytes, Base64.NO_WRAP)
                        }
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .doFinally { progressBar.gone() }
                            .subscribe({ base64 ->
                                checkPsbt(base64)
                            }, {
                                //Ignored
                            }).let { compositeDisposable.add(it) }
                    }
                }
                REQUEST_CODE_INPUT_KEY -> {
                    data?.let {
                        val (keyInfo, seed) = AddAccountActivity.extractResultData(it)
                        signPsbt(keyInfo, seed)
                    }
                }
                else -> {
                    error("unknown request code: $requestCode")
                }
            }
        } else if (resultCode != Activity.RESULT_CANCELED && requestCode == KeyStoreHelper.ENROLLMENT_REQUEST_CODE) {
            // resultCode is 3 after biometric is enrolled
            signPsbt()
        }
    }

    private fun checkPsbt(base64: String) {
        editText.setText(base64)
        export = false
        buttonNext.setText(R.string.sign_psbt)
        viewModel.checkPsbt(base64)
    }

    fun savePsbtFileAndShare(base64: String) {
        RxPermissions(this).requestEach(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ).subscribe { permission ->
            when {
                permission.granted -> {
                    val dir = this.getExternalFilesDir(null)
                    val psbtFile = File(dir, "GordianSigner.psbt")
                    psbtFile.writeBytes(Base64.decode(base64, Base64.NO_WRAP))

                    val psbtUri = FileProvider.getUriForFile(
                        this,
                        getString(R.string.app_authority),
                        psbtFile
                    )

                    navigator.shareFile(psbtUri)
                }
                permission.shouldShowRequestPermissionRationale -> {
                    // do nothing
                }
                else -> {
                    navigator.openAppSetting(this)
                }
            }
        }.let {
            compositeDisposable.add(it)
        }
    }
}