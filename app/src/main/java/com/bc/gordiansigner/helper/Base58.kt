package com.bc.gordiansigner.helper

import com.blockstream.libwally.Wally

object Base58 {

    fun base58FromBytes(byteArray: ByteArray) =
        Wally.base58_from_bytes(byteArray, Wally.BASE58_FLAG_CHECKSUM.toLong())

    fun base58ToBytes(string: String) = Wally.base58_to_bytes(string)
}