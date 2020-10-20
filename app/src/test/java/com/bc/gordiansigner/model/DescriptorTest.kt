package com.bc.gordiansigner.model

import org.junit.Assert.*
import org.junit.Test

class DescriptorTest {

    @Test
    fun `single sig descriptor test`() {
        val descriptorStr = "pkh([d34db33f/44'/0'/0']xpub6ERApfZwUNrhLCkDtcHTcxd75RbzS1ed54G1LkBUHQVHQKqhMkhgbmJbZRkrgZw4koxb5JaHWkY4ALHY2grBGRjaDMzQLcgJvLJuZZvRcEL/1/*)"

        val descriptor = Descriptor.fromString(descriptorStr)

        assertEquals(descriptor.isMulti, false)
        assertEquals(descriptor.format, "P2PKH")
        assertEquals(descriptor.isP2PKH, true)
        assertEquals(descriptor.fingerprint, "d34db33f")
        assertEquals(descriptor.accountXpub, "xpub6ERApfZwUNrhLCkDtcHTcxd75RbzS1ed54G1LkBUHQVHQKqhMkhgbmJbZRkrgZw4koxb5JaHWkY4ALHY2grBGRjaDMzQLcgJvLJuZZvRcEL")
        assertEquals(descriptor.keysWithPath.size, 1)
        assertEquals(descriptor.keysWithPath[0], "[d34db33f/44'/0'/0']xpub6ERApfZwUNrhLCkDtcHTcxd75RbzS1ed54G1LkBUHQVHQKqhMkhgbmJbZRkrgZw4koxb5JaHWkY4ALHY2grBGRjaDMzQLcgJvLJuZZvRcEL/1/*")
        assertEquals(descriptor.derivation, "m/44'/0'/0'")
        assertEquals(descriptor.chain, "Mainnet")
    }

    @Test
    fun `partial multi sig descriptor test`() {
        val descriptorStr = "wsh(sortedmulti(2,[83hf9h94/48h/0h/0h/2h]xpub6CMZuJmP86KE8gaLyDNCQJWWzfGvWfDPhepzBG3keCLZPZ6XPXzsU82ms8BZwCUVR2UxrsDRa2YQ6nSmYbASTqadhRDp2qqd37UvFksA3wT,[<fingerprint>/48h/0h/0h/2h]<xpub>,[<fingerprint>/48h/0h/0h/2h]<xpub>))"

        val descriptor = Descriptor.fromString(descriptorStr)

        assertEquals(descriptor.isMulti, true)
        assertEquals(descriptor.format, "P2WSH")
        assertEquals(descriptor.isBIP67, true)
        assertEquals(descriptor.fingerprints.size, 3)
        assertEquals(descriptor.keysWithPath.size, 3)
        assertEquals(descriptor.keysWithPath[0], "[83hf9h94/48h/0h/0h/2h]xpub6CMZuJmP86KE8gaLyDNCQJWWzfGvWfDPhepzBG3keCLZPZ6XPXzsU82ms8BZwCUVR2UxrsDRa2YQ6nSmYbASTqadhRDp2qqd37UvFksA3wT")
        assertEquals(descriptor.derivationArray.size, 3)
        assertEquals(descriptor.derivationArray[0], "m/48h/0h/0h/2h")
        assertEquals(descriptor.multiSigKeys.size, 3)
        assertEquals(descriptor.multiSigKeys[0], "xpub6CMZuJmP86KE8gaLyDNCQJWWzfGvWfDPhepzBG3keCLZPZ6XPXzsU82ms8BZwCUVR2UxrsDRa2YQ6nSmYbASTqadhRDp2qqd37UvFksA3wT")
        assertEquals(descriptor.multiSigKeys[1], "<xpub>")
        assertEquals(descriptor.chain, "Mainnet")
    }

    @Test
    fun `policy multi sig descriptor test`() {
        val descriptorStr = "sh(multi(2,[<fingerprint>/48h/0h/0h/2h]<xpub>,[<fingerprint>/48h/0h/0h/2h]<xpub>,[<fingerprint>/48h/0h/0h/2h]<xpub>))"

        val descriptor = Descriptor.fromString(descriptorStr)

        assertEquals(descriptor.isMulti, true)
        assertEquals(descriptor.format, "P2SH")
        assertEquals(descriptor.isBIP67, false)
        assertEquals(descriptor.fingerprints.size, 3)
        assertEquals(descriptor.keysWithPath.size, 3)
        assertEquals(descriptor.derivationArray.size, 3)
        assertEquals(descriptor.derivationArray[0], "m/48h/0h/0h/2h")
        assertEquals(descriptor.multiSigKeys.size, 3)
        assertEquals(descriptor.multiSigKeys[0], "<xpub>")
        assertEquals(descriptor.multiSigKeys[1], "<xpub>")
        assertEquals(descriptor.multiSigKeys[2], "<xpub>")
    }

    @Test
    fun `multi sig descriptor test`() {
        val descriptorStr = "wsh(sortedmulti(2,[119dbcab/48h/1h/0h/2h]tpubDFYr9xD4WtT3yDBdX2qT2j2v6ZruqccwPKFwLguuJL99bWBrk6D2Lv1aPpRbFnw1sQUU9DM7ScMAkPRJqR1iXKhWMBNMAJ45QCTuvSZbzzv/0/*,[e650dc93/48h/1h/0h/2h]tpubDEijNAeHVNmm6wHwspPv4fV8mRkoMimeVCk47dExpN9e17jFti12BdjzL8MX17GvKEekRzknNuDoLy1Q8fujYfsWfCvjwYmjjENUpzwDy6B/0/*,[bcc3df08/48h/1h/0h/2h]tpubDFLAjoM9CeEsvZp3UEakCW9jGpx1MgVJP9eteh8Qyr8XN9ASDJoMz58D5YNqm4oRbuBr5LFjfzv6SzsQYUPNWHHYUxvsPimak1tU3cMUhqv/0/*))"

        val descriptor = Descriptor.fromString(descriptorStr)

        assertEquals(descriptor.isMulti, true)
        assertEquals(descriptor.format, "P2WSH")
        assertEquals(descriptor.isBIP67, true)
        assertEquals(descriptor.fingerprints.size, 3)
        assertEquals(descriptor.keysWithPath.size, 3)
        assertEquals(descriptor.keysWithPath[0], "[119dbcab/48h/1h/0h/2h]tpubDFYr9xD4WtT3yDBdX2qT2j2v6ZruqccwPKFwLguuJL99bWBrk6D2Lv1aPpRbFnw1sQUU9DM7ScMAkPRJqR1iXKhWMBNMAJ45QCTuvSZbzzv/0/*")
        assertEquals(descriptor.derivationArray.size, 3)
        assertEquals(descriptor.derivationArray[0], "m/48h/1h/0h/2h")
        assertEquals(descriptor.multiSigKeys.size, 3)
        assertEquals(descriptor.multiSigKeys[0], "tpubDFYr9xD4WtT3yDBdX2qT2j2v6ZruqccwPKFwLguuJL99bWBrk6D2Lv1aPpRbFnw1sQUU9DM7ScMAkPRJqR1iXKhWMBNMAJ45QCTuvSZbzzv")
        assertEquals(descriptor.multiSigKeys[1], "tpubDEijNAeHVNmm6wHwspPv4fV8mRkoMimeVCk47dExpN9e17jFti12BdjzL8MX17GvKEekRzknNuDoLy1Q8fujYfsWfCvjwYmjjENUpzwDy6B")
        assertEquals(descriptor.multiSigKeys[2], "tpubDFLAjoM9CeEsvZp3UEakCW9jGpx1MgVJP9eteh8Qyr8XN9ASDJoMz58D5YNqm4oRbuBr5LFjfzv6SzsQYUPNWHHYUxvsPimak1tU3cMUhqv")
        assertEquals(descriptor.chain, "Testnet")
    }
}