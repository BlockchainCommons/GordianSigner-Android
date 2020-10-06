package com.bc.gordiansigner.ui.share_account

import android.util.Log
import androidx.lifecycle.Observer
import com.bc.gordiansigner.R
import com.bc.gordiansigner.ui.BaseAppCompatActivity
import kotlinx.android.synthetic.main.activity_share_account_map.*
import javax.inject.Inject

class ShareAccountMapActivity : BaseAppCompatActivity() {

    @Inject
    internal lateinit var viewModel: ShareAccountMapViewModel

    override fun layoutRes() = R.layout.activity_share_account_map

    override fun viewModel() = viewModel

    override fun initComponents() {
        super.initComponents()

        title = "Fill Account Map"

        buttonFill.setOnClickListener {
            val accountMapJson = editText.text.toString()
            viewModel.updateAccountMap(accountMapJson)
        }
    }

    override fun observe() {
        super.observe()

        viewModel.accountMapLiveData.asLiveData().observe(this, Observer { res ->
            when {
                res.isSuccess() -> {
                    tvResult.text = res.data()
                }

                res.isError() -> {
                    Log.d("ShareAccountMapActivity", res.throwable()?.message ?: "")
                    tvResult.text = res.throwable()?.message
                }
            }
        })
    }
}