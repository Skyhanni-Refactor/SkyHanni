package at.hannibal2.skyhanni.utils

import java.util.Base64


object EncodingUtils {

    fun ByteArray.toBase64(): String {
        return Base64.getEncoder().encodeToString(this)
    }

    fun String.fromBase64(): ByteArray {
        return Base64.getDecoder().decode(this)
    }

}
