package com.example.frontend.processors

import android.content.Context
import android.graphics.*
import com.example.frontend.utils.*
import com.google.mediapipe.framework.image.MPImage
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.objectdetector.ObjectDetector

class CrowdProcessor(private val context: Context) : FrameProcessor {
    private var objectDetector: ObjectDetector? = null

    override fun process(image: MPImage, originalBitmap: Bitmap, options: Map<String, Any>?): Map<String, Any>? {
        if (objectDetector == null) {
            val baseOptions = BaseOptions.builder()
                .setModelAssetPath("models/efficientdet_lite0.tflite")
                .setDelegate(com.google.mediapipe.tasks.core.Delegate.CPU)
                .build()

            val config = ObjectDetector.ObjectDetectorOptions.builder()
                .setBaseOptions(baseOptions)
                .setScoreThreshold(0.3f)
                .setMaxResults(15)
                .setRunningMode(RunningMode.IMAGE)
                .build()
            objectDetector = ObjectDetector.createFromOptions(context, config)
        }

        val res = objectDetector?.detect(image) ?: return null
        val mutable = originalBitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(mutable)
        val accentColor = Color.parseColor("#34D399")
        var count = 0

        res.detections().forEach { det ->
            if (det.categories().firstOrNull()?.categoryName() == "person") {
                count++
                val box = det.boundingBox()
                val rect = RectF(box.left, box.top, box.right, box.bottom)
                canvas.drawStylizedBox(rect, accentColor)
                canvas.drawLabelBadge("person", rect.left, rect.top - 4f, accentColor)
            }
        }

        return mapOf("count" to count, "frame" to BitmapUtils.encodeBitmap(mutable))
    }

    override fun close() {
        objectDetector?.close()
        objectDetector = null
    }
}
