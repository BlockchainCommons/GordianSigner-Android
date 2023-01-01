package com.bc.gordiansigner.model

import android.util.Log
import com.bc.gordiansigner.helper.Error.ACCOUNT_MAP_ALREADY_FILLED_ERROR
import com.bc.gordiansigner.helper.Error.ACCOUNT_MAP_COMPLETED_ERROR
import com.bc.gordiansigner.helper.Error.BAD_DESCRIPTOR_ERROR
import com.bc.gordiansigner.helper.Error.UNSUPPORTED_FORMAT_ERROR
import com.bc.gordiansigner.helper.FINGERPRINT_REGEX
import com.bc.gordiansigner.helper.Network
import com.blockstream.libwally.Wally

// MARK: This parser is designed to work with descriptors, we try and make it extensible and this can be an area to be improved so that it handles any descriptor but for the purposes of the app we can make a few assumptions as we know what type of descriptors the wallet will produce.

// Examples:
/// pk(0279be667ef9dcbbac55a06295ce870b07029bfcdb2dce28d959f2815b16f81798)
///
/// pkh(02c6047f9441ed7d6d3045406e95c07cd85c778e4b8cef3ca7abac09b95c709ee5)
///
/// wpkh(02f9308a019258c31049344f85f89d5229b531c845836f99b08601f113bce036f9)
///
/// sh(wpkh(03fff97bd5755eeea420453a14355235d382f6472f8568a18b2f057a1460297556))
///
/// combo(0279be667ef9dcbbac55a06295ce870b07029bfcdb2dce28d959f2815b16f81798)
///
/// multi(1,022f8bde4d1a07209355b4a7250a5c5128e88b84bddc619ab7cba8d569b240efe4,025cbdf0646e5db4eaa398f365f2ea7a0e3d419b7e0330e39ce92bddedcac4f9bc)
///
/// sh(multi(2,022f01e5e15cca351daff3843fb70f3c2f0a1bdd05e5af888a67784ef3e10a2a01,03acd484e2f0c7f65309ad178a9f559abde09796974c57e714c35f110dfc27ccbe))
///
/// sh(sortedmulti(2,03acd484e2f0c7f65309ad178a9f559abde09796974c57e714c35f110dfc27ccbe,022f01e5e15cca351daff3843fb70f3c2f0a1bdd05e5af888a67784ef3e10a2a01))
/// wsh(multi(2,03a0434d9e47f3c86235477c7b1ae6ae5d3442d49b1943c2b752a68e2a47e247c7,03774ae7f858a9411e5ef4246b70c65aac5649980be5c17891bbec17895da008cb,03d01115d548e7561b15c38f004d734633687cf4419620095bc5b0f47070afe85a))
/// sh(wsh(multi(1,03f28773c2d975288bc7d1d205c3748651b075fbc6610e58cddeeddf8f19405aa8,03499fdf9e895e719cfd64e67f07d38e3226aa7b63678949e6e49b241a60e823e4,02d7924d4f7d43ea965a465ae3095ff41131e5946f3c85f79e44adbcf8e27e080e)))
///
/// pk(xpub661MyMwAqRbcFtXgS5sYJABqqG9YLmC4Q1Rdap9gSE8NqtwybGhePY2gZ29ESFjqJoCu1Rupje8YtGqsefD265TMg7usUDFdp6W1EGMcet8)
///
/// pkh(xpub68Gmy5EdvgibQVfPdqkBBCHxA5htiqg55crXYuXoQRKfDBFA1WEjWgP6LHhwBZeNK1VTsfTFUHCdrfp1bgwQ9xv5ski8PX9rL2dZXvgGDnw/1'/2)
///
/// pkh([d34db33f/44'/0'/0']xpub6ERApfZwUNrhLCkDtcHTcxd75RbzS1ed54G1LkBUHQVHQKqhMkhgbmJbZRkrgZw4koxb5JaHWkY4ALHY2grBGRjaDMzQLcgJvLJuZZvRcEL/1/*)

/// wsh(multi(1,xpub661MyMwAqRbcFW31YEwpkMuc5THy2PSt5bDMsktWQcFF8syAmRUapSCGu8ED9W6oDMSgv6Zz8idoc4a6mr8BDzTJY47LJhkJ8UB7WEGuduB/1/0/*,xpub69H7F5d8KSRgmmdJg2KhpAK8SR3DjMwAdkxj3ZuxV27CprR9LgpeyGmXUbC6wb7ERfvrnKZjXoUmmDznezpbZb7ap6r1D3tgFxHmwMkQTPH/0/0/*))

///wsh(sortedmulti(1,xpub661MyMwAqRbcFW31YEwpkMuc5THy2PSt5bDMsktWQcFF8syAmRUapSCGu8ED9W6oDMSgv6Zz8idoc4a6mr8BDzTJY47LJhkJ8UB7WEGuduB/1/0/*,xpub69H7F5d8KSRgmmdJg2KhpAK8SR3DjMwAdkxj3ZuxV27CprR9LgpeyGmXUbC6wb7ERfvrnKZjXoUmmDznezpbZb7ap6r1D3tgFxHmwMkQTPH/0/0/*))

data class Descriptor(
    val isSpecter: Boolean,
    val isMulti: Boolean,
    val isBIP67: Boolean,
    val format: String,
    val mOfNType: String,
    val sigsRequired: Int,
    val keysWithPath: MutableList<String>,
    val derivationArray: List<String>,
    val multiSigKeys: List<String>,
    val multiSigPaths: List<String>,
    val fingerprint: String?,
    val fingerprints: List<String>,

    val isBIP44: Boolean = false,
    val isP2PKH: Boolean = false,
    val isBIP84: Boolean = false,
    val isP2WPKH: Boolean = false,
    val isBIP49: Boolean = false,
    val isP2SHP2WPKH: Boolean = false,
    val isWIP48: Boolean = false,
    val isAccount: Boolean = false,

    val prefix: String?,
    val accountXpub: String?,
    val accountXprv: String?,
    val derivation: String?,
    val chain: String,
    val isHD: Boolean,
    val isHot: Boolean,
    val network: Network
) {

    fun isPartial() = isMulti && firstEmptyKey() > 0

    fun isPolicy() = isMulti && firstEmptyKey() == 0

    fun isCompleted() = isMulti && !multiSigKeys.contains("<xpub>")

    fun firstEmptyKey() = multiSigKeys.indexOfFirst { it == "<xpub>" }

    fun firstEmptyDerivationPath() = derivationArray[firstEmptyKey()]

    fun updatePartialAccountMap(fingerprint: String, xpub: String) {
        val index = firstEmptyKey()
        keysWithPath[index] =
            keysWithPath[index].replace("<fingerprint>", fingerprint).replace("<xpub>", xpub)
    }

    fun updatePartialAccountMapFromKey(hdKey: HDKey) {
        if (isCompleted()) throw ACCOUNT_MAP_COMPLETED_ERROR
        if (validFingerprints().contains(hdKey.fingerprintHex)) throw ACCOUNT_MAP_ALREADY_FILLED_ERROR
        val keyAccount = hdKey.derive(firstEmptyDerivationPath())
        updatePartialAccountMap(hdKey.fingerprintHex, keyAccount.xpub)
    }

    fun validFingerprints() = fingerprints
        .map { it.replace("[\\[\\]]".toRegex(), "") }
        .filter { Regex(FINGERPRINT_REGEX).matches(it) }

    override fun toString() = when (isMulti) {
        true -> {
            val body =
                "${if (isBIP67) "sortedmulti" else "multi"}(${sigsRequired},${keysWithPath.joinToString()})"
            when (format) {
                "Bare-multi" -> body
                "P2WSH" -> "wsh($body)"
                "P2SH" -> "sh($body)"
                "P2SH-P2WSH" -> "sh(wsh($body))"
                else -> "not supported yet"
            }
        }

        false -> {
            val body = keysWithPath.first()
            when (format) {
                "P2WPKH" -> "wpkh($body)"
                "P2SH" -> "sh($body)"
                "P2SH-P2WPKH" -> "sh(wpkh($body))"
                "P2PK" -> "pk($body)"
                "P2PKH" -> "pkh($body)"
                else -> "not supported yet"
            }
        }
    }

    companion object {
        fun fromString(descriptor: String): Descriptor {
            val isSpecter = descriptor.contains("&")
            val isMulti: Boolean
            var isBIP67 = false
            var format = ""
            var mOfNType = ""
            var sigsRequired = 0
            var keysWithPath: MutableList<String> = mutableListOf()
            var derivationArray: List<String> = emptyList()
            var multiSigKeys: List<String> = emptyList()
            var multiSigPaths: List<String> = emptyList()
            var fingerprint = ""
            var fingerprints: List<String> = emptyList()

            var isBIP44 = false
            var isP2PKH = false
            var isBIP84 = false
            var isP2WPKH = false
            var isBIP49 = false
            var isP2SHP2WPKH = false
            var isWIP48 = false
            var isAccount = false

            var prefix: String? = null
            var accountXpub: String? = null
            var accountXprv: String? = null
            var derivation: String? = null
            var chain = ""
            var network = Network.TEST
            val isHD: Boolean

            if (descriptor.contains("multi")) {
                isMulti = true

                if (descriptor.contains("sortedmulti")) {
                    isBIP67 = true
                }

                val arr = descriptor.split("(")

                if (arr.isEmpty()) throw BAD_DESCRIPTOR_ERROR
                arr.forEachIndexed { i, item ->
                    if (i == 0) {
                        format = when (item) {
                            "multi" -> "Bare-multi"
                            "wsh" -> "P2WSH"
                            "sh" -> {
                                if (arr[1] == "wsh") {
                                    "P2SH-P2WSH"
                                } else {
                                    "P2SH"
                                }
                            }
                            else -> throw UNSUPPORTED_FORMAT_ERROR
                        }
                    }

                    if (item == "multi" || item == "sortedmulti") {
                        val mOfArray = arr[i + 1].replace(")", "").split(",")
                        val numOfKeys = mOfArray.size - 1
                        mOfNType = "${mOfArray[0]} of $numOfKeys"
                        sigsRequired = mOfArray[0].toInt()
                        keysWithPath = mOfArray.subList(1, mOfArray.size).toMutableList()

                        val fingerprintsList = mutableListOf<String>()
                        val keyArray = mutableListOf<String>()
                        val paths = mutableListOf<String>()
                        val derivationArrayList = mutableListOf<String>()
                        for (key in keysWithPath) {
                            var path = ""
                            if (key.contains("/")) {
                                if (key.contains("[") && key.contains("]")) {
                                    val arr2 = key.split("]")
                                    val rootPath = arr2[0].replace("[", "")

                                    val rootPathArr = rootPath.split("/")
                                    fingerprintsList.add("[${rootPathArr[0]}]")

                                    val deriv = rootPath.replace(rootPathArr[0], "m")
                                    derivationArrayList.add(deriv)

                                    val processedKey = arr2[1]
                                    val pathArray = processedKey.split("/")

                                    for (pathItem in pathArray) {
                                        if (pathItem.contains("xpub")
                                            || pathItem.contains("tpub")
                                            || pathItem.contains("xprv")
                                            || pathItem.contains("tprv")
                                        ) {
                                            keyArray.add(pathItem.replace(")", ""))
                                        } else {
                                            if (!pathItem.contains("*")) {
                                                path = if (path.isEmpty()) {
                                                    pathItem
                                                } else {
                                                    "$path/$pathItem"
                                                }
                                            } else {
                                                paths.add(path)
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        derivationArray = derivationArrayList
                        multiSigKeys = keyArray
                        multiSigPaths = paths

                        fingerprint = fingerprintsList.joinToString()
                        fingerprints = fingerprintsList

                        for (deriv in derivationArrayList) {
                            when (deriv) {
                                "m/48'/0'/0'/1'", "m/48'/1'/0'/1'", "m/48h/0h/0h/1h", "m/48h/1h/0h/1h" -> {
                                    isBIP44 = false
                                    isP2PKH = true
                                    isBIP84 = false
                                    isP2WPKH = false
                                    isBIP49 = false
                                    isP2SHP2WPKH = false
                                    isWIP48 = true
                                    isAccount = true
                                }

                                "m/48'/0'/0'/2'", "m/48'/1'/0'/2'", "m/48h/0h/0h/2h", "m/48h/h/0h/2h" -> {
                                    isBIP44 = false
                                    isP2PKH = false
                                    isBIP84 = false
                                    isP2WPKH = true
                                    isBIP49 = false
                                    isP2SHP2WPKH = false
                                    isWIP48 = true
                                    isAccount = true
                                }

                                "m/48'/0'/0'/3'", "m/48'/1'/0'/3'", "m/48h/0h/0h/3h", "m/48h/1h/0h/3h" -> {
                                    isBIP44 = false
                                    isP2PKH = false
                                    isBIP84 = false
                                    isP2WPKH = false
                                    isBIP49 = false
                                    isP2SHP2WPKH = true
                                    isWIP48 = true
                                    isAccount = true
                                }

                                "m/44'/0'/0'", "m/44'/1'/0'", "m/44h/0h/0h", "m/44h/1h/0h" -> {
                                    isBIP44 = true
                                    isP2PKH = true
                                    isBIP84 = false
                                    isP2WPKH = false
                                    isBIP49 = false
                                    isP2SHP2WPKH = false
                                    isAccount = true
                                }

                                "m/84'/0'/0'", "m/84'/1'/0'", "m/84h/0h/0h", "m/84h/1h/0h" -> {
                                    isBIP44 = false
                                    isP2PKH = false
                                    isBIP84 = true
                                    isP2WPKH = true
                                    isBIP49 = false
                                    isP2SHP2WPKH = false
                                    isAccount = true
                                }

                                "m/49'/0'/0'", "m/49'/1'/0'", "m/49h/0h/0h", "m/49h/1h/0h" -> {
                                    isBIP44 = false
                                    isP2PKH = false
                                    isBIP84 = false
                                    isP2WPKH = false
                                    isBIP49 = true
                                    isP2SHP2WPKH = true
                                    isAccount = true
                                }
                            }
                        }
                    }
                }
            } else {
                isMulti = false

                if (descriptor.contains("[") && descriptor.contains("]")) {
                    val arr1 = descriptor.split("[")
                    keysWithPath = mutableListOf("[${arr1[1].replace(")", "")}")
                    val arr2 = arr1[1].split("]")
                    val deriv = arr2[0]
                    prefix = "[$deriv]"
                    fingerprint = deriv.split("/").first()
                    val extendedKeyWithPath = arr2[1]
                    val arr4 = extendedKeyWithPath.split("/")
                    val extendedKey = arr4[0]
                    if (extendedKey.contains("tpub") || extendedKey.contains("xpub")) {
                        accountXpub = extendedKey
                    } else if (extendedKey.contains("tprv") || extendedKey.contains("xprv")) {
                        accountXprv = extendedKey
                    }

                    val arr3 = deriv.split(("/"))
                    derivation = deriv.replace(arr3[0], "m")
                    when (derivation) {
                        "m/44'/0'/0'", "m/44'/1'/0'", "m/44h/0h/0h", "m/44h/1h/0h" -> {
                            isBIP44 = true
                            isP2PKH = true
                            isAccount = true
                        }

                        "m/84'/0'/0'", "m/84'/1'/0'", "m/84h/0h/0h", "m/84h/1h/0h" -> {
                            isBIP84 = true
                            isP2WPKH = true
                            isAccount = true
                        }

                        "m/49'/0'/0'", "m/49'/1'/0'", "m/49h/0h/0h", "m/49h/1h/0h" -> {
                            isBIP49 = true
                            isP2SHP2WPKH = true
                            isAccount = true
                        }
                    }
                } else {
                    val arr1 = descriptor.split("(")
                    keysWithPath = mutableListOf("[${arr1[1].replace(")", "")}")
                }

                if (descriptor.contains("combo")) {
                    format = "Combo"
                } else {
                    val arr = descriptor.split("(")

                    when (arr[0]) {
                        "wpkh" -> {
                            format = "P2WPKH"
                            isP2WPKH = true
                        }
                        "sh" -> {
                            if (arr[1] == "wpkh") {
                                format = "P2SH-P2WPKH"
                                isP2SHP2WPKH = true
                            } else {
                                format = "P2SH"
                            }
                        }
                        "pk" -> {
                            format = "P2PK"
                        }
                        "pkh" -> {
                            format = "P2PKH"
                            isP2PKH = true
                        }
                    }
                }
            }

            if (descriptor.contains("xpub") || descriptor.contains("xprv")) {
                chain = "Mainnet"
                network = Network.MAIN
                isHD = true

            } else if (descriptor.contains("tpub") || descriptor.contains("tprv")) {
                chain = "Testnet"
                network = Network.TEST
                isHD = true
            } else {
                isHD = false
            }

            val isHot: Boolean = descriptor.contains("xprv") || descriptor.contains("tprv")

            return Descriptor(
                isSpecter,
                isMulti,
                isBIP67,
                format,
                mOfNType,
                sigsRequired,
                keysWithPath,
                derivationArray,
                multiSigKeys,
                multiSigPaths,
                fingerprint,
                fingerprints,
                isBIP44,
                isP2PKH,
                isBIP84,
                isP2WPKH,
                isBIP49,
                isP2SHP2WPKH,
                isWIP48,
                isAccount,
                prefix,
                accountXpub,
                accountXprv,
                derivation,
                chain,
                isHD,
                isHot,
                network
            )
        }
    }
}