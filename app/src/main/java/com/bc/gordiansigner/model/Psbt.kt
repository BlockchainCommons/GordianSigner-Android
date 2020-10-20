package com.bc.gordiansigner.model

import com.blockstream.libwally.Wally.*

class Psbt(base64: String) {

    private val psbt: Any = psbt_from_base64(base64)

    val signable = psbt_get_num_inputs(psbt) > 0 && psbt_is_finalized(psbt) == 0

    val inputBip32Derivs = mutableListOf<Bip32Deriv>().apply {
        val inputCount = psbt_get_num_inputs(psbt)
        for (idx in 0 until inputCount) {
            var end = false
            var subIdx = 0
            while (!end) {
                try {
                    val fingerprint = hex_from_bytes(
                        psbt_get_input_keypath_fingerprint(
                            psbt,
                            idx.toLong(),
                            subIdx.toLong()
                        )
                    )
                    val path = psbt_get_input_keypath_path(psbt, idx.toLong(), subIdx.toLong())
                    add(Bip32Deriv(fingerprint, path))
                    subIdx++
                } catch (e: IllegalArgumentException) {
                    // out of range of `subIdx`
                    end = true
                }
            }
        }
    }.toList()

    fun sign(privKey: ByteArray) {
        val base64 = toBase64()
        psbt_sign(psbt, privKey, 0)
        if (base64 == toBase64()) throw IllegalArgumentException("invalid key")
    }

    fun sign(hdKey: HDKey) {
        sign(hdKey.privKey)
    }

    fun toBase64(): String = psbt_to_base64(psbt, 0)
}

data class Bip32Deriv(val fingerprintHex: String, val path: IntArray) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Bip32Deriv

        if (fingerprintHex != other.fingerprintHex) return false
        if (!path.contentEquals(other.path)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = fingerprintHex.hashCode()
        result = 31 * result + path.contentHashCode()
        return result
    }
}