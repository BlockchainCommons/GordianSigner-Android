package com.bc.gordiansigner.ui.account

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.biometric.BiometricManager
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bc.gordiansigner.R
import com.bc.gordiansigner.helper.KeyStoreHelper
import com.bc.gordiansigner.helper.ext.enrollDeviceSecurity
import com.bc.gordiansigner.ui.BaseAppCompatActivity
import com.bc.gordiansigner.ui.DialogController
import com.bc.gordiansigner.ui.Navigator
import com.bc.gordiansigner.ui.Navigator.Companion.RIGHT_LEFT
import com.bc.gordiansigner.ui.account.add_account.AddAccountActivity
import com.bc.gordiansigner.ui.share_account.ShareAccountMapActivity
import kotlinx.android.synthetic.main.activity_accounts.*
import javax.inject.Inject

class AccountsActivity : BaseAppCompatActivity() {

    companion object {
        private const val TAG = "AccountsActivity"

        private const val IS_SELECTING_KEY = "is_selecting_key"
        private const val SELECTED_FINGERPRINT = "selected_key"

        fun getBundle(isSelecting: Boolean) = Bundle().apply {
            putBoolean(IS_SELECTING_KEY, isSelecting)
        }

        fun extractResultData(intent: Intent) = intent.getStringExtra(SELECTED_FINGERPRINT)
    }

    @Inject
    internal lateinit var viewModel: AccountsViewModel

    @Inject
    internal lateinit var navigator: Navigator

    @Inject
    internal lateinit var dialogController: DialogController

    private lateinit var adapter: AccountRecyclerViewAdapter

    private var isSelecting: Boolean = false

    private lateinit var deletedAccountFingerprint: String

    override fun layoutRes() = R.layout.activity_accounts

    override fun viewModel() = viewModel

    override fun initComponents() {
        super.initComponents()

        isSelecting = intent.getBooleanExtra(IS_SELECTING_KEY, false)

        title = "Accounts"

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        adapter = AccountRecyclerViewAdapter { fingerprint ->
            deletedAccountFingerprint = fingerprint
            viewModel.deleteAccount(deletedAccountFingerprint)
        }

        if (isSelecting) {
            tvHeader.setText(R.string.select_a_key)
            adapter.setItemSelectedListener { fingerprint ->
                val intent = Intent().apply { putExtra(SELECTED_FINGERPRINT, fingerprint) }
                navigator.finishActivityForResult(intent)
            }
        }

        with(recyclerView) {
            this.adapter = this@AccountsActivity.adapter
            this.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.fetchHDKeyFingerprints()
    }

    override fun observe() {
        super.observe()

        viewModel.hdKeyFingerprintsLiveData.asLiveData().observe(this, Observer { res ->
            when {
                res.isSuccess() -> {
                    res.data()?.let { fingerprints ->
                        adapter.set(fingerprints)
                    }
                }

                res.isError() -> {
                    dialogController.alert(
                        R.string.error,
                        R.string.fetching_accounts_failed
                    )
                }
            }
        })

        viewModel.deleteKeysLiveData.asLiveData().observe(this, Observer { res ->
            when {
                res.isSuccess() -> {
                    res.data()?.let { fingerprint ->
                        adapter.remove(fingerprint)
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
                                    R.string.auth_for_deleting_account,
                                    successCallback = {
                                        viewModel.deleteAccount(deletedAccountFingerprint)
                                    },
                                    failedCallback = { code ->
                                        if (code == BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED) {
                                            navigator.anim(RIGHT_LEFT).enrollDeviceSecurity()
                                        } else {
                                            Log.e(
                                                TAG,
                                                "Biometric auth failed with code: $code"
                                            )
                                        }
                                    })
                            })
                    ) {
                        dialogController.alert(
                            R.string.error,
                            R.string.could_not_delete_account
                        )
                    }
                }
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        if (!isSelecting) menuInflater.inflate(R.menu.accounts_menu, menu)
        return !isSelecting
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        menu?.findItem(R.id.action_edit)?.let { item ->
            item.setTitle(if (adapter.isEditing) R.string.done else R.string.edit)
        }
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                navigator.anim(RIGHT_LEFT).finishActivity()
            }
            R.id.action_fill_account_map -> {
                navigator.anim(RIGHT_LEFT).startActivity(ShareAccountMapActivity::class.java)
            }
            R.id.action_add -> {
                navigator.anim(RIGHT_LEFT).startActivity(AddAccountActivity::class.java)
            }
            R.id.action_edit -> {
                adapter.isEditing = !adapter.isEditing
                invalidateOptionsMenu()
            }
        }

        return super.onOptionsItemSelected(item)
    }
}