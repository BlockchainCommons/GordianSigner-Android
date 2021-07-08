package com.bc.gordiansigner.helper

object Error {
    val NO_HD_KEY_FOUND_ERROR = GeneralError(0x01, "No HD key was found")
    val HD_KEY_NOT_MATCH_ERROR = GeneralError(0x01, "HD key not match with current psbt")
    val PSBT_UNABLE_TO_SIGN_ERROR = GeneralError(0x02, "PSBT is unable to sign")
    val NO_APPROPRIATE_HD_KEY_ERROR = GeneralError(0x03, "No appropriate HD key for signing")
    val ACCOUNT_MAP_COMPLETED_ERROR = GeneralError(0x04, "Account map is already completed")
    val ACCOUNT_MAP_ALREADY_FILLED_ERROR = GeneralError(0x04, "Account map is already filled")
    val BAD_DESCRIPTOR_ERROR = GeneralError(0x05, "Bad descriptor")
    val UNSUPPORTED_FORMAT_ERROR = GeneralError(0x06, "Unsupported format")
    val FINGERPRINT_NOT_MATCH_ERROR = GeneralError(0x07, "imported key not match with current fingerprint")
}

data class GeneralError(val code: Int, val msg: String) :
    Throwable("{\"code\" : $code, \"message\" : \"$msg\"}")