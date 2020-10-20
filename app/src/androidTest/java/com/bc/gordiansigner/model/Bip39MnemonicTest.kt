package com.bc.gordiansigner.model

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class Bip39MnemonicTest {

    @Test
    fun testSeedGeneratedCorrect() {
        val testData = mapOf<String, Any>(
            "mnemonic" to "modify tip timber tissue mandate april title unable valley spawn athlete harsh",
            "seedHex" to "924fa65681a0d22b434f8068ed8ebdd98fb125a8f787758b93de7388e5f3fc64914bdcf87f706de7e5b32c31d8f14ea3e8e3ffeeb1fd2251f2ab83bf15674895"
        )

        val mnemonic = Bip39Mnemonic(testData["mnemonic"] as String)
        assertEquals(mnemonic.seedHex, testData["seedHex"])
    }

    @Test(expected = IllegalArgumentException::class)
    fun testIncorrectSeedThrowError() {
        Bip39Mnemonic("modifyyy tip timber tissue mandate april title unable valley spawn athlete harsh")
    }
}