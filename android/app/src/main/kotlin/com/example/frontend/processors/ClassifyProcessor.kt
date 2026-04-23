package com.example.frontend.processors

import android.content.Context
import android.graphics.Bitmap
import com.google.mediapipe.framework.image.MPImage
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.imageclassifier.ImageClassifier

class ClassifyProcessor(private val context: Context) : FrameProcessor {
    private var imageClassifier: ImageClassifier? = null

    override fun process(image: MPImage, originalBitmap: Bitmap, options: Map<String, Any>?): Map<String, Any>? {
        if (imageClassifier == null) {
            val baseOptions = BaseOptions.builder()
                .setModelAssetPath("models/efficientnet_lite0.tflite")
                .setDelegate(com.google.mediapipe.tasks.core.Delegate.CPU)
                .build()

            val config = ImageClassifier.ImageClassifierOptions.builder()
                .setBaseOptions(baseOptions)
                .setMaxResults(3)
                .setRunningMode(RunningMode.IMAGE)
                .build()
            imageClassifier = ImageClassifier.createFromOptions(context, config)
        }
        val res = imageClassifier?.classify(image) ?: return null
        val cats = res.classificationResult().classifications().firstOrNull()?.categories() ?: emptyList()
        return mapOf("classifications" to cats.map { mapOf("label" to it.categoryName(), "score" to it.score()) })
    }

    override fun close() {
        imageClassifier?.close()
        imageClassifier = null
    }
}
