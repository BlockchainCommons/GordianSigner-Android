package com.bc.gordiansigner.model

import com.bc.gordiansigner.helper.Network
import com.blockstream.libwally.Wally.*

class HDKey {

    companion object {
        fun fingerprintFromSeed(seed: ByteArray) = HDKey(seed, Network.TEST).fingerprintHex
    }

    private val rootKey: Any

    private constructor(key: Any, network: Network) {
        rootKey = key
        this.network = network
    }

    constructor(seed: ByteArray, network: Network) {
        this.network = network
        rootKey = bip32_key_from_seed(
            seed, (if (network == Network.TEST) {
                BIP32_VER_TEST_PRIVATE
            } else {
                BIP32_VER_MAIN_PRIVATE
            }).toLong(), 0
        )
    }

    constructor(rootXpriv: String) {
        rootKey = bip32_key_from_base58(rootXpriv)
        network = if (rootXpriv.startsWith("xprv")) Network.MAIN else Network.TEST
    }

    val network: Network

    val xprv: String get() = bip32_key_to_base58(rootKey, BIP32_FLAG_KEY_PRIVATE.toLong())

    val xpub: String get() = bip32_key_to_base58(rootKey, BIP32_FLAG_KEY_PUBLIC.toLong())

    val fingerprint: ByteArray get() = bip32_key_get_fingerprint(rootKey)

    val fingerprintHex: String get() = hex_from_bytes(fingerprint)

    val privKey: ByteArray get() = bip32_key_get_priv_key(rootKey)

    val pubKey: ByteArray get() = bip32_key_get_pub_key(rootKey)

    fun derive(derivationPath: String): HDKey {
        val path = Regex("\\d+").findAll(derivationPath)
            .map(MatchResult::value)
            .map { BIP32_INITIAL_HARDENED_CHILD + it.toInt() }
            .toList()
            .toIntArray()

        return derive(path)
    }

    fun derive(path: IntArray) = if (path.isNotEmpty()) {
        HDKey(
            bip32_key_from_parent_path(
                rootKey,
                path,
                BIP32_FLAG_KEY_PRIVATE.toLong()
            ),
            network
        )
    } else this

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as HDKey

        if (xprv != other.xprv) return false
        return true
    }

    override fun hashCode(): Int {
        return xprv.hashCode()
    }


}