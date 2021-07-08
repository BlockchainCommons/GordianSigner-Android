package com.bc.gordiansigner.ui.account.contact

import android.view.Menu
import android.view.MenuItem
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bc.gordiansigner.R
import com.bc.gordiansigner.helper.view.ContactDialog
import com.bc.gordiansigner.ui.BaseAppCompatActivity
import com.bc.gordiansigner.ui.DialogController
import com.bc.gordiansigner.ui.Navigator
import com.bc.gordiansigner.ui.account.AccountRecyclerViewAdapter
import kotlinx.android.synthetic.main.activity_accounts.*
import javax.inject.Inject

class ContactsActivity : BaseAppCompatActivity() {

    @Inject
    internal lateinit var viewModel: ContactsViewModel

    @Inject
    internal lateinit var navigator: Navigator

    @Inject
    internal lateinit var dialogController: DialogController

    private lateinit var adapter: AccountRecyclerViewAdapter

    override fun layoutRes() = R.layout.activity_contacts

    override fun viewModel() = viewModel

    override fun initComponents() {
        super.initComponents()

        title = ""

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        adapter = AccountRecyclerViewAdapter { deleteKeyInfo ->
            dialogController.confirm(
                R.string.delete_contact,
                R.string.this_action_is_undoable_the_contact_will_be_gone_forever,
                cancelable = true,
                positive = R.string.delete,
                positiveEvent = {
                    viewModel.deleteContact(deleteKeyInfo)
                }
            )
        }

        adapter.setItemSelectedListener { keyInfo ->
            val dialog = ContactDialog(keyInfo) { newKeyInfo ->
                viewModel.saveContact(newKeyInfo)
            }
            dialog.show(supportFragmentManager, ContactDialog.TAG)
        }

        with(recyclerView) {
            this.adapter = this@ContactsActivity.adapter
            this.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        }

        viewModel.fetchContacts()
    }

    override fun observe() {
        super.observe()

        viewModel.contactsLiveData.asLiveData().observe(this, Observer { res ->
            when {
                res.isSuccess() -> {
                    res.data()?.let { contacts ->
                        adapter.set(contacts)
                    }
                }

                res.isError() -> {
                    dialogController.alert(res.throwable())
                }
            }
        })

        viewModel.saveContactLiveData.asLiveData().observe(this, Observer { res ->
            when {
                res.isSuccess() -> {
                    res.data()?.let { (keyInfo, isCreating) ->
                        if (isCreating) {
                            adapter.add(keyInfo)
                        } else {
                            adapter.update(keyInfo)
                        }
                    }
                }

                res.isError() -> {
                    dialogController.alert(res.throwable())
                }
            }
        })

        viewModel.deleteContactLiveData.asLiveData().observe(this, Observer { res ->
            when {
                res.isSuccess() -> {
                    viewModel.fetchContacts()
                }

                res.isError() -> {
                    dialogController.alert(res.throwable())
                }
            }
        })
    }

    override fun onPause() {
        if (adapter.isEditing) {
            adapter.isEditing = false
            invalidateOptionsMenu()
        }
        super.onPause()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.contacts_menu, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        menu?.findItem(R.id.action_edit)
            ?.setTitle(if (adapter.isEditing) R.string.done else R.string.edit)
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                navigator.anim(Navigator.RIGHT_LEFT).finishActivity()
            }
            R.id.action_edit -> {
                adapter.isEditing = !adapter.isEditing
                invalidateOptionsMenu()
            }
            R.id.action_add -> {
                val dialog = ContactDialog(null) { newKeyInfo ->
                    viewModel.saveContact(newKeyInfo)
                }
                dialog.show(supportFragmentManager, ContactDialog.TAG)
            }
        }

        return super.onOptionsItemSelected(item)
    }
}