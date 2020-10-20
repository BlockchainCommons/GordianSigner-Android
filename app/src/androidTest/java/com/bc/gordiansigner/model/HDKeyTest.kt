package com.bc.gordiansigner.model

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.bc.gordiansigner.helper.Network
import com.blockstream.libwally.Wally
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HDKeyTest {

    private val testData = mapOf<String, Any>(
        "mnemonic" to "modify tip timber tissue mandate april title unable valley spawn athlete harsh",
        "privKey" to "034fde991424d45220a35b4fd593920c32883046e3fbceb1acb46307b9e4f23b",
        "pubKey" to "03c6711983134afc54683ff011186cd3429c56fce6ed53128c516445c3a7f796b7",
        "xprv" to "tprv8ZgxMBicQKsPf9YX7pjPPftAPKQuzXqdRsM6buCE4rEA51r8MNXcG8gNopD2Nz6PxntoX4tLsgFax4Ae3NbF6ycD7B41dC6ANgMvxDt2vCP",
        "xpub" to "tpubD6NzVbkrYhZ4YcaK1UPyo5YGxLvr9s2Y1AwstREXV82YuW6tymMCSdJEyzabr4rNACTZbvJcRwmjHzfU3msWnuMqJsesmX9LivPg8pYhwxB",
        "fingerprint" to "20d25116",
        "derivePath" to "m/48h/1h/0h/2h",
        "derivePathArray" to intArrayOf(
            0x80000030.toInt(),
            0x80000001.toInt(),
            0x80000000.toInt(),
            0x80000002.toInt()
        ),
        "account_xprv" to "tprv8j3W3c5rkmj3bDu7q5zm4btALyJLdzHnJEiNtpsUvM3F7vtZxGF5Ts8Xe4CcBvLYQUmnhkT6pA9EnF8SW5rHSh7E8TCendMdRMSBog77wYF"
    )

    @Test
    fun hdKeyGeneratedCorrectFromSeed() {
        val hdKey = HDKey(Bip39Mnemonic(testData["mnemonic"] as String).seed, Network.TEST)

        assertEquals(hdKey.xpub, testData["xpub"])
        assertEquals(hdKey.xprv, testData["xprv"])
        assertEquals(hdKey.fingerprintHex, testData["fingerprint"])
        var accountKey = hdKey.derive(testData["derivePath"] as String)
        assertEquals(accountKey.xprv, testData["account_xprv"])
        accountKey = hdKey.derive(testData["derivePathArray"] as IntArray)
        assertEquals(accountKey.xprv, testData["account_xprv"])
        assertEquals(hdKey.network, Network.TEST)
        assertEquals(Wally.hex_from_bytes(hdKey.privKey), testData["privKey"])
        assertEquals(Wally.hex_from_bytes(hdKey.pubKey), testData["pubKey"])
    }


    @Test
    fun hdKeyGeneratedCorrectFromXPrv() {
        val hdKey = HDKey(testData["xprv"] as String)

        assertEquals(hdKey.xpub, testData["xpub"])
        assertEquals(hdKey.xprv, testData["xprv"])
        assertEquals(hdKey.fingerprintHex, testData["fingerprint"])
        val accountKey = hdKey.derive(testData["derivePath"] as String)
        assertEquals(accountKey.xprv, testData["account_xprv"])
        assertEquals(hdKey.network, Network.TEST)
        assertEquals(Wally.hex_from_bytes(hdKey.privKey), testData["privKey"])
        assertEquals(Wally.hex_from_bytes(hdKey.pubKey), testData["pubKey"])
    }

    @Test(expected = IllegalArgumentException::class)
    fun hdKeyThrowErrorFromBadSeed() {
        HDKey(ByteArray(0), Network.TEST)
        HDKey(ByteArray(17), Network.TEST)
        HDKey(ByteArray(32), Network.TEST)
        HDKey(ByteArray(1), Network.TEST)
    }

}