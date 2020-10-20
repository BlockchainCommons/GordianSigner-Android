package com.bc.gordiansigner.model

import com.blockstream.libwally.Wally.*

class Bip39Mnemonic {

    private val mnemonic: String

    constructor(words: String) {
        bip39_mnemonic_validate(bip39_get_wordlist(null), words)
        mnemonic = words
    }

    constructor(entropy: ByteArray) {
        mnemonic = bip39_mnemonic_from_bytes(bip39_get_wordlist(null), entropy)
    }

    val seed
        get() = ByteArray(BIP39_SEED_LEN_512).apply {
            bip39_mnemonic_to_seed(mnemonic, null, this)
        }

    val seedHex: String get() = hex_from_bytes(seed)
}