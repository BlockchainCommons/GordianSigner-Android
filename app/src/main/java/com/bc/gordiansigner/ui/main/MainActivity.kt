package com.bc.gordiansigner.ui.main

import android.content.Intent
import com.bc.gordiansigner.R
import com.bc.gordiansigner.service.WalletService
import com.bc.gordiansigner.ui.BaseAppCompatActivity
import com.bc.gordiansigner.ui.BaseViewModel
import com.bc.gordiansigner.ui.add_account.AddAccountActivity
import com.bc.gordiansigner.ui.share_account.ShareAccountMapActivity
import com.bc.gordiansigner.ui.sign.PsbtSignActivity
import kotlinx.android.synthetic.main.activity_main.*
import javax.inject.Inject

class MainActivity : BaseAppCompatActivity() {

    @Inject
    internal lateinit var viewModel: MainViewModel

    @Inject
    internal lateinit var walletService: WalletService

    override fun layoutRes(): Int = R.layout.activity_main

    override fun viewModel(): BaseViewModel? = viewModel

    override fun initComponents() {
        super.initComponents()

        buttonImportAccount.setOnClickListener {
            val intent = Intent(this, AddAccountActivity::class.java)
            startActivity(intent)
        }

        buttonConfirmAccount.setOnClickListener {
            val intent = Intent(this, ShareAccountMapActivity::class.java)
            startActivity(intent)
        }

        buttonSignPsbt.setOnClickListener {
            val intent = Intent(this, PsbtSignActivity::class.java)
            startActivity(intent)
        }
    }
}