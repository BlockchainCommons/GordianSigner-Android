package com.bc.gordiansigner.model

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.bc.gordiansigner.helper.Network
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PsbtTest {

    private val validPsbtData = mapOf(
        "base64_psbt" to "cHNidP8BAHQCAAAAAX0yw5njuX+bw9eWhTKamXEwEu7XBrAGHbJzo5rj3WAEAQAAAAD/////ApdXAQAAAAAAFgAUyjGTXa5ifHdGKcWaDMkgw752Y5yKAgAAAAAAABl2qRTFFybFoi59dsywV9Y0Pbh8pefv/4isAAAAAAABAH0CAAAAAUnl/XdIGG3zroFvMA+SA3CI2KlT/i6g0LiCdeXA58USAQAAAAD+////AgWyXwsAAAAAFgAUQdJWpc+3B6ZSlfPyGuk9OI8wwLdgZwEAAAAAACIAIOMp/mJo3CAgB2iF+papUxIA7IjRtRNusB+uMDUMQWZVsAwcAAEBK2BnAQAAAAAAIgAg4yn+YmjcICAHaIX6lqlTEgDsiNG1E26wH64wNQxBZlUBBUdSIQPAppUHEMAHcZMgLUbzBHxSwY049dvJEMMs52jfhliPzyEDxnEZgxNK/FRoP/ARGGzTQpxW/ObtUxKMUWRFw6f3lrdSriIGA8CmlQcQwAdxkyAtRvMEfFLBjTj128kQwyznaN+GWI/PECGCmbgAAACAAAAAgAEAAIAiBgPGcRmDE0r8VGg/8BEYbNNCnFb85u1TEoxRZEXDp/eWtwQg0lEWACICA0lSsbJjq06trZMTvw+RkM6tnsmaqHpfFkujXYvJogN0ECGCmbgAAACAAQAAgAIAAIAAAA==",
        "signed_base64_psbt" to "cHNidP8BAHQCAAAAAX0yw5njuX+bw9eWhTKamXEwEu7XBrAGHbJzo5rj3WAEAQAAAAD/////ApdXAQAAAAAAFgAUyjGTXa5ifHdGKcWaDMkgw752Y5yKAgAAAAAAABl2qRTFFybFoi59dsywV9Y0Pbh8pefv/4isAAAAAAABAH0CAAAAAUnl/XdIGG3zroFvMA+SA3CI2KlT/i6g0LiCdeXA58USAQAAAAD+////AgWyXwsAAAAAFgAUQdJWpc+3B6ZSlfPyGuk9OI8wwLdgZwEAAAAAACIAIOMp/mJo3CAgB2iF+papUxIA7IjRtRNusB+uMDUMQWZVsAwcAAEBK2BnAQAAAAAAIgAg4yn+YmjcICAHaIX6lqlTEgDsiNG1E26wH64wNQxBZlUiAgPGcRmDE0r8VGg/8BEYbNNCnFb85u1TEoxRZEXDp/eWt0gwRQIhAKqGMDdqEJ5fzJeZ/cslYWIU9Q1X+yUR2qZH5f4OvVmqAiAV1igMVXzpRRee7lW6bA+IpXhuSemqrGhHhMVGr1BqfwEBBUdSIQPAppUHEMAHcZMgLUbzBHxSwY049dvJEMMs52jfhliPzyEDxnEZgxNK/FRoP/ARGGzTQpxW/ObtUxKMUWRFw6f3lrdSriIGA8CmlQcQwAdxkyAtRvMEfFLBjTj128kQwyznaN+GWI/PECGCmbgAAACAAAAAgAEAAIAiBgPGcRmDE0r8VGg/8BEYbNNCnFb85u1TEoxRZEXDp/eWtwQg0lEWACICA0lSsbJjq06trZMTvw+RkM6tnsmaqHpfFkujXYvJogN0ECGCmbgAAACAAQAAgAIAAIAAAA==",
        "input_bip32_derivs" to mutableListOf(
            Bip32Deriv(
                "218299b8",
                intArrayOf(0x80000000.toInt(), 0x80000000.toInt(), 0x80000001.toInt())
            ),
            Bip32Deriv("20d25116", intArrayOf())
        ),
        "recovery_phrase" to "modify tip timber tissue mandate april title unable valley spawn athlete harsh"
    )

    private val invalidBase64Psbt = arrayOf(
        "",
        "invalid psbt",
        "cHNidP8BAHQCAAAAAX0yw5njuX+bw9eWhTKamXEwEu7XBrAGHbJzo5rj3WAEAQAAAAD/////ApdXAQAAAAAAFgAUyjGTXa5ifHdGKcWaDMkgw752Y5yKAgAAAAAAABl2qRTFFybFoi59dsywV9Y0Pbh8pefv/4isAAAAAAABAH0CAAAAAUnl/XdIGG3zroFvMA+SA3CI2KlT/i6g0LiCdeXA58USAQAAAAD+////AgWyXwsAAAAAFgAUQdJWpc+3B6ZSlfPyGuk9OI8wwLdgZwEAAAAAACIAIOMp/mJo3CAgB2iF+papUxIA7IjRtRNusB+uMDUMQWZVsAwcAAEBK2BnAQAAAAAAIgAg4yn+YmjcICAHaIX6lqlTEgDsiNG1E26wH64wNQxBZlUBBUdSIQPAppUHEMAHcZMgLUbzBHxSwY049dvJEMMs52jfhliPzyEDxnEZgxNK/FRoP/ARGGzTQpxW/ObtUxKMUWRFw6f3lrdSriIGA8CmlQcQwAdxkyAtRvMEfFLBjTj128kQwyznaN+GWI/PECGCmbgAAACAAAAAgAEAAIAiBgPGcRmDE0r8VGg/8BEYbNNCnFb85u1TEoxRZEXDp/eWtwQg0lEWACICA0lSsbJjq06trZMTvw+RkM6tnsmaqHpfFkujXYvJogN0ECGCmbgAAACAAQAAgAIAAIAAAA==1"
    )

    private val invalidRecoveryPhrase = arrayOf(
        "amateur sadness dwarf gaze runway reject junk find owner advance diesel blouse",
        "slide inherit arrow split slogan flip drip admit hover judge test convince"
    )

    @Test
    fun testConstructValidPsbt() {
        val psbt = Psbt(validPsbtData["base64_psbt"] as String)
        assertEquals(2, psbt.inputBip32Derivs.size)
        val bip32Deriv0 = ((validPsbtData["input_bip32_derivs"] as List<*>)[0] as Bip32Deriv)
        val bip32Deriv1 = ((validPsbtData["input_bip32_derivs"] as List<*>)[1] as Bip32Deriv)
        assertEquals(bip32Deriv0, psbt.inputBip32Derivs[0])
        assertEquals(bip32Deriv1, psbt.inputBip32Derivs[1])
        assertTrue(psbt.signable)
        assertEquals(validPsbtData["base64_psbt"] as String, psbt.toBase64())
    }

    @Test(expected = IllegalArgumentException::class)
    fun testConstructInvalidPsbt() {
        invalidBase64Psbt.forEach {
            Psbt(it)
        }
    }

    @Test
    fun testSignPsbtValidKey() {
        val psbt = Psbt(validPsbtData["base64_psbt"] as String)
        val hdKey =
            HDKey(
                Bip39Mnemonic(validPsbtData["recovery_phrase"] as String).seed,
                Network.TEST
            )
        psbt.sign(hdKey)
        assertEquals(validPsbtData["signed_base64_psbt"] as String, psbt.toBase64())
    }

    @Test(expected = IllegalArgumentException::class)
    fun testSignPsbtInvalidKey() {
        val psbt = Psbt(validPsbtData["base64_psbt"] as String)
        invalidRecoveryPhrase.forEach {
            val hdKey = HDKey(Bip39Mnemonic(it).seed, Network.TEST)
            psbt.sign(hdKey)
        }
    }

}