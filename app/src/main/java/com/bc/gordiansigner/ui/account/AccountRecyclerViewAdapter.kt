package com.bc.gordiansigner.ui.account

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bc.gordiansigner.R
import com.bc.gordiansigner.helper.SIMPLE_DATE_TIME_FORMAT
import com.bc.gordiansigner.helper.ext.*
import com.bc.gordiansigner.model.KeyInfo
import kotlinx.android.synthetic.main.item_signer.view.*

class AccountRecyclerViewAdapter(
    private var onDelete: ((KeyInfo) -> Unit)? = null
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val items = mutableListOf<KeyInfo>()
    private var onItemSelected: ((KeyInfo) -> Unit)? = null
    var isEditing = false
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    fun set(items: List<KeyInfo>) {
        this.items.clear()
        this.items.addAll(items)
        notifyDataSetChanged()
    }

    fun add(item: KeyInfo) {
        items.add(item)
        notifyItemInserted(items.size - 1)
    }

    fun update(item: KeyInfo) {
        val index = items.indexOf(item)
        if (index != -1) {
            val keyInfo = items[index]
            keyInfo.alias = item.alias
            keyInfo.isSaved = item.isSaved
            notifyItemChanged(index)
        }
    }

    fun remove(fingerprint: String) {
        val index = items.indexOfFirst { it.fingerprint == fingerprint }
        if (index != -1) {
            items.removeAt(index)
            notifyItemRemoved(index)
        }
    }

    fun setItemSelectedListener(callback: (KeyInfo) -> Unit) {
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
        onDelete: ((KeyInfo) -> Unit)?,
        onItemSelected: ((KeyInfo) -> Unit)?
    ) : RecyclerView.ViewHolder(view) {

        private lateinit var keyInfo: KeyInfo

        init {
            itemView.buttonDelete.setSafetyOnclickListener {
                onDelete?.invoke(keyInfo)
            }

            itemView.setSafetyOnclickListener {
                onItemSelected?.invoke(keyInfo)
            }
        }

        fun bind(keyInfo: KeyInfo, isEditing: Boolean) {
            this.keyInfo = keyInfo
            with(itemView) {
                tvAlias.text =
                    if (keyInfo.alias.isNotEmpty()) keyInfo.alias else context.getString(R.string.unnamed)

                tvFingerprint.text =
                    context.getString(R.string.fingerprint_format, keyInfo.fingerprint)

                tvLastUsed.text = context.getString(R.string.last_used_format, keyInfo.lastUsed?.toString(
                    SIMPLE_DATE_TIME_FORMAT))

                if (keyInfo.isSaved) {
                    buttonKey.visible()
                } else {
                    buttonKey.gone()
                }

                if (isEditing) {
                    buttonDelete.visible(true)
                } else {
                    buttonDelete.gone()
                }
            }
        }
    }
}