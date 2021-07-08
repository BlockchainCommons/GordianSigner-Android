package com.bc.gordiansigner.ui.share_account

import android.app.Activity
import android.content.Intent
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.biometric.BiometricManager
import androidx.lifecycle.Observer
import com.bc.gordiansigner.R
import com.bc.gordiansigner.helper.Error.ACCOUNT_MAP_ALREADY_FILLED_ERROR
import com.bc.gordiansigner.helper.Error.ACCOUNT_MAP_COMPLETED_ERROR
import com.bc.gordiansigner.helper.Error.BAD_DESCRIPTOR_ERROR
import com.bc.gordiansigner.helper.Error.NO_HD_KEY_FOUND_ERROR
import com.bc.gordiansigner.helper.KeyStoreHelper
import com.bc.gordiansigner.helper.ext.copyToClipboard
import com.bc.gordiansigner.helper.ext.enrollDeviceSecurity
import com.bc.gordiansigner.helper.ext.pasteFromClipBoard
import com.bc.gordiansigner.helper.ext.setSafetyOnclickListener
import com.bc.gordiansigner.helper.view.ExportBottomSheetDialog
import com.bc.gordiansigner.helper.view.QRCodeBottomSheetDialog
import com.bc.gordiansigner.ui.BaseAppCompatActivity
import com.bc.gordiansigner.ui.DialogController
import com.bc.gordiansigner.ui.Navigator
import com.bc.gordiansigner.ui.Navigator.Companion.RIGHT_LEFT
import com.bc.gordiansigner.ui.account.AccountsActivity
import com.bc.gordiansigner.ui.scan.QRScannerActivity
import kotlinx.android.synthetic.main.activity_share_account_map.*
import javax.inject.Inject

class ShareAccountMapActivity : BaseAppCompatActivity() {

    companion object {
        private const val TAG = "ShareAccountMapActivity"
        private const val REQUEST_CODE_QR_ACCOUNT_MAP = 0x01
        private const val REQUEST_CODE_SELECT_KEY = 0x02
    }

    @Inject
    internal lateinit var viewModel: ShareAccountMapViewModel

    @Inject
    internal lateinit var navigator: Navigator

    @Inject
    internal lateinit var dialogController: DialogController

    private var export = false
    private var selectedSeed = ""

    override fun layoutRes() = R.layout.activity_share_account_map

    override fun viewModel() = viewModel

    override fun initComponents() {
        super.initComponents()

        supportActionBar?.title = ""
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        buttonFill.setSafetyOnclickListener {
            val accountMapJson = editText.text.toString()

            if (!export) {
                if (accountMapJson.isNotEmpty()) {
                    val bundle = AccountsActivity.getBundle(true)
                    navigator.anim(RIGHT_LEFT).startActivityForResult(
                        AccountsActivity::class.java,
                        REQUEST_CODE_SELECT_KEY,
                        bundle
                    )
                } else {
                    dialogController.alert(R.string.error, R.string.invalid_account_map)
                }
            } else {
                val dialog = ExportBottomSheetDialog(
                    isFileVisible = false,
                    listener = object : ExportBottomSheetDialog.OnItemSelectedListener {
                        override fun onCopy() {
                            this@ShareAccountMapActivity.copyToClipboard(accountMapJson)
                            Toast.makeText(
                                this@ShareAccountMapActivity,
                                R.string.copied,
                                Toast.LENGTH_LONG
                            ).show()
                        }

                        override fun onShowQR() {
                            val qrDialog = QRCodeBottomSheetDialog(editText.text.toString())
                            qrDialog.show(supportFragmentManager, QRCodeBottomSheetDialog.TAG)
                        }

                        override fun onSaveFile() {
                            //Not supported
                        }
                    })
                dialog.show(supportFragmentManager, ExportBottomSheetDialog.TAG)
            }
        }
    }

    override fun observe() {
        super.observe()

        viewModel.accountMapLiveData.asLiveData().observe(this, Observer { res ->
            when {
                res.isSuccess() -> {
                    editText.setText(res.data())
                    export = true
                    buttonFill.setText(R.string.export)
                    dialogController.alert(
                        R.string.success,
                        R.string.you_may_now_export_it_by_tapping_the_export_button
                    )
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
                                    R.string.auth_for_updating_account_map,
                                    successCallback = {
                                        updateAccountMap()
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
                        val msg = when (res.throwable()) {
                            NO_HD_KEY_FOUND_ERROR -> R.string.no_account_found
                            ACCOUNT_MAP_COMPLETED_ERROR -> R.string.account_map_completed
                            ACCOUNT_MAP_ALREADY_FILLED_ERROR -> R.string.account_map_filled
                            BAD_DESCRIPTOR_ERROR -> R.string.bad_descriptor
                            else -> R.string.unsupported_format
                        }
                        dialogController.alert(R.string.error, msg)
                    }
                }
            }
        })

        viewModel.accountMapStatusLiveData.asLiveData().observe(this, Observer { res ->
            when {
                res.isSuccess() -> {
                    res.data()?.let { (joinedSigners, descriptor) ->
                        dialogController.alert(
                            getString(R.string.valid_account_map),
                            getString(
                                R.string.account_map_info,
                                descriptor.sigsRequired,
                                descriptor.keysWithPath.size,
                                if (joinedSigners.isNotEmpty()) joinedSigners.joinToString {
                                    "\n\t\uD83D\uDD11 ${if (it.alias.isNotEmpty()) {
                                        getString(
                                            R.string.fingerprint_alias_format,
                                            it.fingerprint,
                                            it.alias
                                        )
                                    } else {
                                        it.fingerprint
                                    }}"
                                } else "<none>"
                            )
                        )
                    }
                }

                res.isError() -> {
                    Log.d(TAG, res.throwable()?.message ?: "")
                    editText.setText("")
                    dialogController.alert(R.string.error, R.string.invalid_account_map)
                }
            }
        })
    }

    private fun updateAccountMap() {
        val accountMapJson = editText.text.toString()
        viewModel.updateAccountMap(accountMapJson, selectedSeed)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.account_map_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                navigator.anim(RIGHT_LEFT).finishActivity()
            }
            R.id.action_paste -> {
                this.pasteFromClipBoard()?.let {
                    checkAccountMap(it)
                } ?: dialogController.alert(R.string.error, R.string.clipboard_is_empty)
            }
            R.id.action_scan -> {
                navigator.anim(RIGHT_LEFT).startActivityForResult(
                    QRScannerActivity::class.java,
                    REQUEST_CODE_QR_ACCOUNT_MAP
                )
            }
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_CODE_QR_ACCOUNT_MAP -> {
                    data?.let {
                        val accountMap = QRScannerActivity.extractResultData(it)
                        checkAccountMap(accountMap)
                    }
                }

                REQUEST_CODE_SELECT_KEY -> {
                    data?.let {
                        selectedSeed = AccountsActivity.extractResultData(it) ?: return

                        updateAccountMap()
                    }
                }

                else -> {
                    error("unknown request code: $requestCode")
                }
            }
        } else if (resultCode != Activity.RESULT_CANCELED && requestCode == KeyStoreHelper.ENROLLMENT_REQUEST_CODE) {
            // resultCode is 3 after biometric is enrolled
            updateAccountMap()
        }
    }

    private fun checkAccountMap(string: String) {
        editText.setText(string)
        export = false
        buttonFill.setText(R.string.fill)
        viewModel.checkValidAccountMap(string)
    }

    override fun onBackPressed() {
        navigator.anim(RIGHT_LEFT).finishActivity()
        super.onBackPressed()
    }
}