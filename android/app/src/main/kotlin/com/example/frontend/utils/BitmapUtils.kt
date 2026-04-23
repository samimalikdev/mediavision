package com.example.frontend.utils

import android.graphics.*
import java.io.ByteArrayOutputStream

object BitmapUtils {
    fun encodeBitmap(bitmap: Bitmap, recycle: Boolean = true, format: String = "jpeg"): ByteArray {
        val width = 480
        val height = (480f / bitmap.width * bitmap.height).toInt()
        val scaled = Bitmap.createScaledBitmap(bitmap, width, height, false)
        
        val out = ByteArrayOutputStream()
        if (format.lowercase() == "png") {
            scaled.compress(Bitmap.CompressFormat.PNG, 100, out)
        } else {
            scaled.compress(Bitmap.CompressFormat.JPEG, 40, out)
        }
        scaled.recycle()
        if (recycle) bitmap.recycle()
        return out.toByteArray()
    }

    fun decodeYUV420(imageArgs: Map<String, Any>): Bitmap? {
        return try {
            val planes = imageArgs["planes"] as List<*>
            val yBuffer = planes[0] as ByteArray
            val uBuffer = planes[1] as ByteArray
            val vBuffer = planes[2] as ByteArray

            val width = imageArgs["width"] as Int
            val height = imageArgs["height"] as Int
            val yRowStride = imageArgs["yRowStride"] as Int
            val uvRowStride = imageArgs["uvRowStride"] as Int
            val uvPixelStride = imageArgs["uvPixelStride"] as Int

            val nv21 = ByteArray(width * height * 3 / 2)

            for (row in 0 until height) {
                System.arraycopy(yBuffer, row * yRowStride, nv21, row * width, width)
            }

            var uvIndex = width * height
            for (row in 0 until height / 2) {
                val uvRow = row * uvRowStride
                for (col in 0 until width / 2) {
                    val uvOffset = uvRow + col * uvPixelStride
                    nv21[uvIndex++] = vBuffer[uvOffset]
                    nv21[uvIndex++] = uBuffer[uvOffset]
                }
            }

            val yuvImage = YuvImage(nv21, ImageFormat.NV21, width, height, null)
            val out = ByteArrayOutputStream()
            yuvImage.compressToJpeg(Rect(0, 0, width, height), 80, out)
            BitmapFactory.decodeByteArray(out.toByteArray(), 0, out.size())
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun rotateFlipResize(bitmap: Bitmap, degrees: Float, isFront: Boolean, maxSize: Int): Bitmap {
        val w = bitmap.width
        val h = bitmap.height
        
        val scale = if (w > h) maxSize.toFloat() / w else maxSize.toFloat() / h
        
        val scaled = if (scale < 1f) {
            Bitmap.createScaledBitmap(bitmap, (w * scale).toInt(), (h * scale).toInt(), false)
        } else {
            bitmap
        }

        val matrix = Matrix().apply {
            if (degrees != 0f) postRotate(degrees)
            if (isFront) postScale(-1f, 1f)
        }
        
        val result = if (degrees != 0f || isFront) {
            Bitmap.createBitmap(scaled, 0, 0, scaled.width, scaled.height, matrix, false)
        } else {
            scaled
        }

        if (scaled != bitmap && scaled != result) scaled.recycle()
        if (result != bitmap) bitmap.recycle()
        
        return result
    }
}
