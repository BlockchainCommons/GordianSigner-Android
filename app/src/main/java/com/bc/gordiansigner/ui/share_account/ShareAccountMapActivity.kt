package com.bc.gordiansigner.ui.share_account

import android.app.Activity
import android.content.Intent
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.biometric.BiometricManager
import androidx.lifecycle.Observer
import com.bc.gordiansigner.R
import com.bc.gordiansigner.helper.Error.ACCOUNT_MAP_COMPLETED_ERROR
import com.bc.gordiansigner.helper.Error.BAD_DESCRIPTOR_ERROR
import com.bc.gordiansigner.helper.Error.NO_HD_KEY_FOUND_ERROR
import com.bc.gordiansigner.helper.KeyStoreHelper
import com.bc.gordiansigner.helper.ext.*
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
    }

    @Inject
    internal lateinit var viewModel: ShareAccountMapViewModel

    @Inject
    internal lateinit var navigator: Navigator

    @Inject
    internal lateinit var dialogController: DialogController

    override fun layoutRes() = R.layout.activity_share_account_map

    override fun viewModel() = viewModel

    override fun initComponents() {
        super.initComponents()

        supportActionBar?.title = "Account Map"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_qr_code_24)

        buttonFill.setSafetyOnclickListener {
            updateAccountMap()
        }

        buttonCopy.setSafetyOnclickListener {
            this.copyToClipboard(tvResult.text.toString())
            buttonCopy.animateWithString("Copied")
        }
    }

    override fun observe() {
        super.observe()

        viewModel.accountMapLiveData.asLiveData().observe(this, Observer { res ->
            when {
                res.isSuccess() -> {
                    buttonCopy.visible()
                    tvResult.text = res.data()
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
                            NO_HD_KEY_FOUND_ERROR -> R.string.no_hd_key_found
                            ACCOUNT_MAP_COMPLETED_ERROR -> R.string.account_map_completed
                            BAD_DESCRIPTOR_ERROR -> R.string.bad_descriptor
                            else -> R.string.unsupported_format
                        }
                        buttonCopy.gone()
                        dialogController.alert(R.string.error, msg)
                    }
                }
            }
        })

        viewModel.accountMapStatusLiveData.asLiveData().observe(this, Observer { res ->
            when {
                res.isSuccess() -> {
                    dialogController.alert(
                        R.string.valid_account_map,
                        R.string.you_can_tap_fill_now_to_fill_your_account_map
                    )
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
        viewModel.updateAccountMap(accountMapJson)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.common_menu, menu)
        menu?.findItem(R.id.action_import)?.isVisible = false
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                navigator.anim(RIGHT_LEFT).startActivityForResult(
                    QRScannerActivity::class.java,
                    REQUEST_CODE_QR_ACCOUNT_MAP
                )
            }
            R.id.action_paste -> {
                this.pasteFromClipBoard()?.let {
                    checkAccountMap(it)
                } ?: dialogController.alert(R.string.error, R.string.clipboard_is_empty)
            }
            R.id.action_signer -> {
                navigator.anim(RIGHT_LEFT).startActivity(AccountsActivity::class.java)
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
        viewModel.checkValidAccountMap(string)
    }
}