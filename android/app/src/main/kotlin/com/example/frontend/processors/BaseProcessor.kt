package com.example.frontend.processors

import android.graphics.Bitmap
import com.google.mediapipe.framework.image.MPImage

interface FrameProcessor {
    fun process(image: MPImage, originalBitmap: Bitmap, options: Map<String, Any>? = null): Map<String, Any>?
    fun close()
}
