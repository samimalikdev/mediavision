package com.example.frontend.processors

import android.content.Context
import android.graphics.*
import com.example.frontend.utils.*
import com.google.mediapipe.framework.image.MPImage
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarker

class PoseProcessor(private val context: Context) : FrameProcessor {
    private var poseLandmarker: PoseLandmarker? = null

    override fun process(image: MPImage, originalBitmap: Bitmap, options: Map<String, Any>?): Map<String, Any>? {
        if (poseLandmarker == null) {
            val baseOptions = BaseOptions.builder()
                .setModelAssetPath("models/pose_landmarker_lite.task")
                .setDelegate(com.google.mediapipe.tasks.core.Delegate.CPU)
                .build()

            val config = PoseLandmarker.PoseLandmarkerOptions.builder()
                .setBaseOptions(baseOptions)
                .setRunningMode(RunningMode.IMAGE)
                .build()
            poseLandmarker = PoseLandmarker.createFromOptions(context, config)
        }

        val res = poseLandmarker?.detect(image) ?: return null
        val mutable = originalBitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(mutable)
        val w = mutable.width.toFloat()
        val h = mutable.height.toFloat()

        val zoneColors = mapOf(
            "torso" to Color.parseColor("#A78BFA"),
            "arm"   to Color.parseColor("#34D399"),
            "leg"   to Color.parseColor("#60A5FA"),
            "head"  to Color.parseColor("#F59E0B")
        )

        val connections = listOf(
            11 to 12 to "torso", 11 to 23 to "torso", 12 to 24 to "torso", 23 to 24 to "torso",
            11 to 13 to "arm",   13 to 15 to "arm",   15 to 17 to "arm",   15 to 19 to "arm",
            12 to 14 to "arm",   14 to 16 to "arm",   16 to 18 to "arm",   16 to 20 to "arm",
            23 to 25 to "leg",   25 to 27 to "leg",   27 to 29 to "leg",   27 to 31 to "leg",
            24 to 26 to "leg",   26 to 28 to "leg",   28 to 30 to "leg",   28 to 32 to "leg",
            0 to 11 to "head",  0 to 12 to "head"
        )

        res.landmarks().forEach { pose ->
            connections.forEach { (pair, zone) ->
                val (a, b) = pair
                if (a < pose.size && b < pose.size) {
                    val color = zoneColors[zone] ?: Color.WHITE
                    canvas.drawLine(pose[a].x() * w, pose[a].y() * h, pose[b].x() * w, pose[b].y() * h, linePaint(color, if (zone == "head") 2.5f else 4f))
                }
            }

            pose.forEachIndexed { i, lm ->
                val zone = when (i) {
                    in 0..10 -> "head"
                    11, 12, 23, 24 -> "torso"
                    in 13..22 -> "arm"
                    else -> "leg"
                }
                val color = zoneColors[zone] ?: Color.WHITE
                val radius = if (i in listOf(0, 11, 12, 23, 24)) 8f else 6f
                val (fill, stroke) = jointPaint(color)
                canvas.drawCircle(lm.x() * w, lm.y() * h, radius, fill)
                canvas.drawCircle(lm.x() * w, lm.y() * h, radius, stroke)
            }
        }

        return mapOf("pose_count" to res.landmarks().size, "frame" to BitmapUtils.encodeBitmap(mutable))
    }

    override fun close() {
        poseLandmarker?.close()
        poseLandmarker = null
    }
}
