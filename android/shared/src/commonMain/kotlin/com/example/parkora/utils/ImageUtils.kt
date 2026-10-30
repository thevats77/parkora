package com.example.parkora.utils

import com.example.parkora.platform.Platform
import kotlin.io.encoding.Base64

/*
 * PHOTOS FOR THE ONLINE DATABASE
 * ------------------------------
 * Firebase Storage (for files) needs a paid plan, so Parkora keeps photos INSIDE the
 * Firestore documents instead. To fit (a document may be at most 1 MB) every photo is
 * made small – max 800 px, JPEG quality 70 (≈ 60–150 KB) – and turned into text:
 *     "data:image/jpeg;base64,/9j/4AAQ..."
 * The website uses exactly the same format, so photos show up on both.
 * (Shrinking is done by each platform: see Platform.compressToJpeg.)
 */
object ImageUtils {

    private const val PREFIX = "data:image/jpeg;base64,"

    /** Shrinks the picked photo and returns it as a data-URL string (or null on failure). */
    fun bytesToDataUrl(bytes: ByteArray, maxSize: Int = 800, quality: Int = 70): String? = try {
        Platform.compressToJpeg(bytes, maxSize, quality)?.let { PREFIX + Base64.encode(it) }
    } catch (e: Exception) {
        null
    }

    fun isDataUrl(path: String?): Boolean = path?.startsWith("data:image") == true

    /** Turns "data:image/jpeg;base64,..." back into bytes so Coil can show it. */
    fun dataUrlToBytes(dataUrl: String): ByteArray =
        Base64.decode(dataUrl.substringAfter("base64,").filterNot { it.isWhitespace() })
}
