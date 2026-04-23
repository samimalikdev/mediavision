package com.example.frontend.processors

import android.content.Context
import android.graphics.*
import com.example.frontend.utils.*
import com.google.mediapipe.framework.image.MPImage
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.facelandmarker.FaceLandmarker

class FaceProcessor(private val context: Context) : FrameProcessor {
    private var faceLandmarker: FaceLandmarker? = null

    override fun process(image: MPImage, originalBitmap: Bitmap, options: Map<String, Any>?): Map<String, Any>? {
        if (faceLandmarker == null) {
            val baseOptions = BaseOptions.builder()
                .setModelAssetPath("models/face_landmarker.task")
                .setDelegate(com.google.mediapipe.tasks.core.Delegate.CPU)
                .build()
                
            val config = FaceLandmarker.FaceLandmarkerOptions.builder()
                .setBaseOptions(baseOptions)
                .setRunningMode(RunningMode.IMAGE)
                .build()
            faceLandmarker = FaceLandmarker.createFromOptions(context, config)
        }

        val res = faceLandmarker?.detect(image) ?: return null
        val mutable = originalBitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(mutable)
        val w = mutable.width.toFloat()
        val h = mutable.height.toFloat()

        val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
        val defaultColor = Color.argb(160, 200, 200, 200)

        res.faceLandmarks().forEach { lms ->
            dotPaint.color = defaultColor
            lms.forEach { lm -> canvas.drawCircle(lm.x() * w, lm.y() * h, 2f, dotPaint) }

            DrawStyle.FACE_REGIONS.forEach { (region, indices) ->
                val color = DrawStyle.FACE_REGION_COLORS[region] ?: return@forEach
                val radius = DrawStyle.FACE_REGION_RADIUS[region] ?: 3.5f

                val glowPaint = fillPaint(Color.argb(40, Color.red(color), Color.green(color), Color.blue(color)))
                val borderPaint = fillPaint(color)

                indices.forEach { i ->
                    if (i < lms.size) {
                        val lm = lms[i]
                        val cx = lm.x() * w
                        val cy = lm.y() * h
                        canvas.drawCircle(cx, cy, radius + 2f, glowPaint)
                        canvas.drawCircle(cx, cy, radius, borderPaint)
                        dotPaint.color = Color.argb(200, 255, 255, 255)
                        canvas.drawCircle(cx, cy, radius * 0.4f, dotPaint)
                    }
                }
            }
        }

        return mapOf("face_count" to res.faceLandmarks().size, "frame" to BitmapUtils.encodeBitmap(mutable))
    }

    override fun close() {
        faceLandmarker?.close()
        faceLandmarker = null
    }
}
