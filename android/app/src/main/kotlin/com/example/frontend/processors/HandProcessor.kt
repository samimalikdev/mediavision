package com.example.frontend.processors

import android.content.Context
import android.graphics.*
import com.example.frontend.utils.*
import com.google.mediapipe.framework.image.MPImage
import com.google.mediapipe.tasks.components.containers.NormalizedLandmark
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarker
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarkerResult

class HandProcessor(private val context: Context) : FrameProcessor {
    private var handLandmarker: HandLandmarker? = null

    override fun process(image: MPImage, originalBitmap: Bitmap, options: Map<String, Any>?): Map<String, Any>? {
        if (handLandmarker == null) {
            val config = HandLandmarker.HandLandmarkerOptions.builder()
                .setBaseOptions(BaseOptions.builder()
                    .setModelAssetPath("models/hand_landmarker.task")
                    .setDelegate(com.google.mediapipe.tasks.core.Delegate.CPU)
                    .build())
                .setMinHandDetectionConfidence(0.5f)
                .setRunningMode(RunningMode.IMAGE)
                .build()
            handLandmarker = HandLandmarker.createFromOptions(context, config)
        }

        val res: HandLandmarkerResult = handLandmarker?.detect(image) ?: return null
        val mutable = originalBitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(mutable)
        val w = mutable.width.toFloat()
        val h = mutable.height.toFloat()

        val isFront = options?.get("isFront") as? Boolean ?: false
        val palmColor  = Color.parseColor("#34D399")
        val thumbColor = Color.parseColor("#60A5FA")
        
        val bonePaint = linePaint(palmColor, 3.5f)
        val thumbBonePaint = linePaint(thumbColor, 3f)

        var totalFingers = 0
        res.landmarks().forEachIndexed { index: Int, lms: List<NormalizedLandmark> ->
            val handedness = try {
                val hList = res.handedness()
                if (hList.isNotEmpty() && index < hList.size) hList[index].firstOrNull()?.categoryName() ?: "Right"
                else "Right"
            } catch (e: Exception) { "Right" }

            totalFingers += countFingers(lms, handedness, isFront)

            DrawStyle.HAND_CONNECTIONS.forEach { (a, b) ->
                if (a < lms.size && b < lms.size) {
                    val p = if (a <= 4 || b <= 4) thumbBonePaint else bonePaint
                    canvas.drawLine(lms[a].x() * w, lms[a].y() * h, lms[b].x() * w, lms[b].y() * h, p)
                }
            }

            lms.forEachIndexed { i, lm ->
                val isTip = i in listOf(4, 8, 12, 16, 20)
                val borderColor = if (isTip || i in 0..4) thumbColor else palmColor
                val radius = if (isTip) 9f else 6f
                val (fill, stroke) = jointPaint(borderColor)
                canvas.drawCircle(lm.x() * w, lm.y() * h, radius, fill)
                canvas.drawCircle(lm.x() * w, lm.y() * h, radius, stroke)
            }
        }

        return mapOf("total_fingers" to totalFingers, "frame" to BitmapUtils.encodeBitmap(mutable))
    }

    private fun countFingers(landmarks: List<NormalizedLandmark>, handedness: String, isFront: Boolean): Int {
        val tipIds = listOf(4, 8, 12, 16, 20)
        var count = 0
        val isRight = if (isFront) handedness == "Left" else handedness == "Right"
        if (isRight) { if (landmarks[4].x() > landmarks[3].x()) count++ }
        else         { if (landmarks[4].x() < landmarks[3].x()) count++ }
        for (i in 1..4) {
            if (landmarks[tipIds[i]].y() < landmarks[tipIds[i] - 2].y()) count++
        }
        return count
    }

    override fun close() {
        handLandmarker?.close()
        handLandmarker = null
    }
}
