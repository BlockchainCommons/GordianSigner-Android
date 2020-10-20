package com.bc.gordiansigner.helper

import com.blockstream.libwally.Wally

object Hex {

    fun hexFromBytes(byteArray: ByteArray) = Wally.hex_from_bytes(byteArray)

    fun hexToBytes(string: String) = Wally.hex_to_bytes(string)

}