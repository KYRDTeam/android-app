package com.kyberswap.android.util

import org.web3j.utils.Numeric
import java.nio.charset.StandardCharsets
import java.security.InvalidKeyException
import java.security.NoSuchAlgorithmException
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

object HMAC {

    fun hmacDigest(msg: String, keyString: String, algo: String = "HmacSHA512"): String? {
        var digest: String? = null
        try {
            val key = SecretKeySpec(keyString.toByteArray(StandardCharsets.UTF_8), algo)
            val mac = Mac.getInstance(algo)
            mac.init(key)
            digest =
                Numeric.toHexStringNoPrefix(mac.doFinal(msg.toByteArray(StandardCharsets.UTF_8)))
        } catch (e: InvalidKeyException) {
            e.printStackTrace()
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        }

        return digest
    }
}
