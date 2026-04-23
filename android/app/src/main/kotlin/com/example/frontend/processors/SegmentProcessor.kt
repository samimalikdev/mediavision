package com.example.frontend.processors

import android.content.Context
import android.graphics.*
import android.util.Log
import com.example.frontend.utils.BitmapUtils
import com.google.mediapipe.framework.image.MPImage
import com.google.mediapipe.framework.image.ByteBufferExtractor
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.imagesegmenter.ImageSegmenter

class SegmentProcessor(private val context: Context) : FrameProcessor {
    private var imageSegmenter: ImageSegmenter? = null

    override fun process(image: MPImage, originalBitmap: Bitmap, options: Map<String, Any>?): Map<String, Any>? {
        if (imageSegmenter == null) {
            val baseOptions = BaseOptions.builder()
                .setModelAssetPath("models/selfie_segmenter.tflite")
                .setDelegate(com.google.mediapipe.tasks.core.Delegate.CPU)
                .build()

            val config = ImageSegmenter.ImageSegmenterOptions.builder()
                .setBaseOptions(baseOptions)
                .setRunningMode(RunningMode.IMAGE)
                .setOutputConfidenceMasks(true)
                .build()
            imageSegmenter = ImageSegmenter.createFromOptions(context, config)
        }

        val res = imageSegmenter?.segment(image) ?: return null
        val masks = res.confidenceMasks()
        if (!masks.isPresent || masks.get().isEmpty()) return null

        val maskMPImage = masks.get()[0]
        val maskWidth = maskMPImage.width
        val maskHeight = maskMPImage.height
        val buffer = ByteBufferExtractor.extract(maskMPImage)
        buffer.rewind()

        val maskBitmap = Bitmap.createBitmap(maskWidth, maskHeight, Bitmap.Config.ARGB_8888)
        val pixels = IntArray(maskWidth * maskHeight)
        for (i in 0 until maskWidth * maskHeight) {
            val confidence = buffer.float
            val alpha = (confidence * 255).toInt().coerceIn(0, 255)
            pixels[i] = Color.argb(alpha, 255, 255, 255)
        }
        maskBitmap.setPixels(pixels, 0, maskWidth, 0, 0, maskWidth, maskHeight)

        val width = originalBitmap.width
        val height = originalBitmap.height
        val result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)
        val scaledMask = Bitmap.createScaledBitmap(maskBitmap, width, height, true)

        val effect = options?.get("effect") as? String ?: "blur"
        when (effect) {
            "blur" -> {
                val scale = 0.1f
                val small = Bitmap.createScaledBitmap(originalBitmap, (width * scale).toInt().coerceAtLeast(1), (height * scale).toInt().coerceAtLeast(1), true)
                val blurred = Bitmap.createScaledBitmap(small, width, height, true)
                canvas.drawBitmap(blurred, 0f, 0f, null)
                small.recycle(); blurred.recycle()
            }
            "virtual" -> {
                val gradient = LinearGradient(0f, 0f, 0f, height.toFloat(), Color.parseColor("#0F172A"), Color.parseColor("#1E293B"), Shader.TileMode.CLAMP)
                canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), Paint().apply { shader = gradient })
            }
            "neon" -> {
                canvas.drawColor(Color.parseColor("#020617"))
                val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    colorFilter = PorterDuffColorFilter(Color.parseColor("#22D3EE"), PorterDuff.Mode.SRC_IN)
                    maskFilter = BlurMaskFilter(20f, BlurMaskFilter.Blur.NORMAL)
                }
                canvas.drawBitmap(scaledMask, 0f, 0f, glowPaint)
            }
            "transparent" -> {
            }
            else -> canvas.drawColor(Color.BLACK)
        }

        val saveCount = canvas.saveLayer(0f, 0f, width.toFloat(), height.toFloat(), null)
        canvas.drawBitmap(originalBitmap, 0f, 0f, null)
        canvas.drawBitmap(scaledMask, 0f, 0f, Paint(Paint.ANTI_ALIAS_FLAG).apply { xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_IN) })
        canvas.restoreToCount(saveCount)

        scaledMask.recycle()
        maskBitmap.recycle()

        val format = if (effect == "transparent") "png" else "jpeg"
        return mapOf("status" to "segmented", "frame" to BitmapUtils.encodeBitmap(result, format = format))
    }

    override fun close() {
        imageSegmenter?.close()
        imageSegmenter = null
    }
}
