package com.bc.gordiansigner.ui.account

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bc.gordiansigner.R
import com.bc.gordiansigner.helper.ext.gone
import com.bc.gordiansigner.helper.ext.setSafetyOnclickListener
import com.bc.gordiansigner.helper.ext.visible
import kotlinx.android.synthetic.main.item_signer.view.*

class AccountRecyclerViewAdapter(
    private var onDelete: ((String) -> Unit)? = null
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val items = mutableListOf<String>()
    private var onItemSelected: ((String) -> Unit)? = null
    var isEditing = false
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    fun set(items: List<String>) {
        this.items.clear()
        this.items.addAll(items)
        notifyDataSetChanged()
    }

    fun add(item: String) {
        items.add(item)
        notifyItemInserted(items.size - 1)
    }

    fun remove(fingerprint: String) {
        val index = items.indexOf(fingerprint)
        if (index != -1) {
            items.removeAt(index)
            notifyItemRemoved(index)
        }
    }

    fun setItemSelectedListener(callback: (String) -> Unit) {
        this.onItemSelected = callback
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return KeyViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_signer,
                parent,
                false
            ),
            onDelete,
            onItemSelected
        )
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as? KeyViewHolder)?.bind(items[position], isEditing)
    }

    class KeyViewHolder(
        view: View,
        onDelete: ((String) -> Unit)?,
        onItemSelected: ((String) -> Unit)?
    ) : RecyclerView.ViewHolder(view) {

        private lateinit var fingerprint: String

        init {
            itemView.buttonDelete.setSafetyOnclickListener {
                onDelete?.invoke(fingerprint)
            }

            itemView.setSafetyOnclickListener {
                onItemSelected?.invoke(fingerprint)
            }
        }

        fun bind(fingerprint: String, isEditing: Boolean) {
            this.fingerprint = fingerprint
            with(itemView) {
                tvFingerprint.text = fingerprint
                if (isEditing) {
                    buttonDelete.visible(true)
                } else {
                    buttonDelete.gone()
                }
            }
        }
    }
}