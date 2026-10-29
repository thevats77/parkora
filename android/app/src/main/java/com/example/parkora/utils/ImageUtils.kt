package com.example.parkora.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Base64
import java.io.ByteArrayOutputStream

/*
 * PHOTOS FOR THE ONLINE DATABASE
 * ------------------------------
 * Firebase Storage (for files) needs a paid plan, so Parkora keeps photos INSIDE the
 * Firestore documents instead. To fit (a document may be at most 1 MB) every photo is
 * made small – max 800 px, JPEG quality 70 (≈ 60–150 KB) – and turned into text:
 *     "data:image/jpeg;base64,/9j/4AAQ..."
 * The website uses exactly the same format, so photos show up on both.
 */
object ImageUtils {

    private const val PREFIX = "data:image/jpeg;base64,"

    /** Reads the picked photo, shrinks it and returns it as a data-URL string (or null on failure). */
    fun uriToDataUrl(context: Context, uri: Uri, maxSize: Int = 800, quality: Int = 70): String? = try {
        // 1. Read only the picture size first, to choose a cheap down-sampling factor
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
        var sample = 1
        while (bounds.outWidth / (sample * 2) >= maxSize && bounds.outHeight / (sample * 2) >= maxSize) sample *= 2

        // 2. Decode the (already smaller) picture and scale it to at most maxSize px
        val decoded = context.contentResolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, BitmapFactory.Options().apply { inSampleSize = sample })
        } ?: error("cannot decode")
        val scale = minOf(1f, maxSize.toFloat() / maxOf(decoded.width, decoded.height))
        val bitmap = Bitmap.createScaledBitmap(decoded, (decoded.width * scale).toInt(), (decoded.height * scale).toInt(), true)

        // 3. Compress to JPEG and encode as Base64 text
        val bytes = ByteArrayOutputStream().also { bitmap.compress(Bitmap.CompressFormat.JPEG, quality, it) }.toByteArray()
        PREFIX + Base64.encodeToString(bytes, Base64.NO_WRAP)
    } catch (e: Exception) {
        null
    }

    fun isDataUrl(path: String?): Boolean = path?.startsWith("data:image") == true

    /** Turns "data:image/jpeg;base64,..." back into bytes so Coil can show it. */
    fun dataUrlToBytes(dataUrl: String): ByteArray =
        Base64.decode(dataUrl.substringAfter("base64,"), Base64.DEFAULT)

    /** Reads the display name of a picked file, e.g. "ownership_proof.pdf". */
    fun getFileName(context: Context, uri: Uri): String {
        var name = "document"
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (nameIndex >= 0 && cursor.moveToFirst()) name = cursor.getString(nameIndex) ?: name
        }
        return name
    }
}
