package com.bc.gordiansigner.ui.scan

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.util.Base64
import android.view.MenuItem
import com.bc.gordiansigner.R
import com.bc.gordiansigner.helper.ext.openAppSetting
import com.bc.gordiansigner.helper.ext.visible
import com.bc.gordiansigner.ui.BaseAppCompatActivity
import com.bc.gordiansigner.ui.BaseViewModel
import com.bc.gordiansigner.ui.DialogController
import com.bc.gordiansigner.ui.Navigator
import com.bc.gordiansigner.ui.Navigator.Companion.RIGHT_LEFT
import com.bc.ur.URDecoder
import com.google.zxing.BarcodeFormat
import com.google.zxing.ResultPoint
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult
import com.journeyapps.barcodescanner.DefaultDecoderFactory
import com.tbruyelle.rxpermissions2.RxPermissions
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.activity_qrscanner.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class QRScannerActivity : BaseAppCompatActivity() {

    companion object {
        private const val SOFT_DELAY_TIME = 500L
        private const val IS_UR = "is_ur"
        private const val QR_CODE_STRING = "qr_code_string"

        fun getBundle(isUR: Boolean) = Bundle().apply { putBoolean(IS_UR, isUR) }

        fun extractResultData(intent: Intent): String = intent.getStringExtra(QR_CODE_STRING) ?: ""
    }

    @Inject
    internal lateinit var navigator: Navigator

    @Inject
    internal lateinit var dialogController: DialogController

    private var decoder: URDecoder? = null

    private var isUR = false

    override fun layoutRes() = R.layout.activity_qrscanner

    override fun viewModel(): BaseViewModel? = null

    private val compositeDisposable = CompositeDisposable()

    override fun initComponents() {
        super.initComponents()

        isUR = intent.getBooleanExtra(IS_UR, false)

        if (isUR) {
            progressBar.visible()
            tvProgress.visible()
        }

        title = "Scan QR Code"

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        decoder = URDecoder()

        val rxPermission = RxPermissions(this)
        Observable.timer(SOFT_DELAY_TIME, TimeUnit.MILLISECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .flatMap { rxPermission.requestEach(Manifest.permission.CAMERA) }
            .subscribe { permission ->
                if (!permission.granted) {
                    if (permission.shouldShowRequestPermissionRationale) {
                        finish()
                    } else {
                        dialogController.alert(
                            R.string.enable_camera_access,
                            R.string.to_get_started_allow_access_camera,
                            R.string.enable_access
                        ) {
                            navigator.openAppSetting(this)
                        }
                    }
                }
            }.let { compositeDisposable.add(it) }

        val formats = listOf(BarcodeFormat.QR_CODE)
        scannerView.decoderFactory = DefaultDecoderFactory(formats)
        scannerView.decodeContinuous(object : BarcodeCallback {
            override fun barcodeResult(result: BarcodeResult?) {
                result?.text?.let { str ->
                    if (isUR) {
                        val decoder = decoder ?: return@let

                        decoder.receivePart(str)

                        val percentageCompletion = (decoder.estimatedPercentComplete() * 100).toInt()
                        progressBar.progress = percentageCompletion
                        tvProgress.text = getString(R.string.percent_completed, percentageCompletion)

                        if (decoder.isComplete) {
                            if (decoder.isSuccess) {
                                val ur = decoder.resultUR()
                                val base64 = Base64.encodeToString(ur.message, Base64.NO_WRAP)
                                val intent = Intent().apply { putExtra(QR_CODE_STRING, base64) }
                                navigator.finishActivityForResult(intent)
                            } else {
                                this@QRScannerActivity.decoder = null
                                dialogController.alert(
                                    R.string.error,
                                    R.string.content_is_invalid_please_check_and_try_again
                                ) {
                                    this@QRScannerActivity.decoder = URDecoder()
                                    progressBar.progress = 0
                                    tvProgress.text = getString(R.string.percent_completed, 0)
                                }
                            }
                        }
                    } else {
                        val intent = Intent().apply { putExtra(QR_CODE_STRING, str) }
                        navigator.finishActivityForResult(intent)
                    }
                }
            }

            override fun possibleResultPoints(resultPoints: MutableList<ResultPoint>?) {

            }

        })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                navigator.anim(RIGHT_LEFT).finishActivity()
            }
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()
        Completable.timer(SOFT_DELAY_TIME, TimeUnit.MILLISECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { scannerView.resume() }
            .let { compositeDisposable.add(it) }
    }

    override fun onPause() {
        scannerView.pause()
        super.onPause()
    }

    override fun deinitComponents() {
        compositeDisposable.dispose()
        super.deinitComponents()
    }
}