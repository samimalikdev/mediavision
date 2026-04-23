package com.example.frontend

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.annotation.NonNull
import android.util.Log
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel
import com.google.mediapipe.framework.image.BitmapImageBuilder
import java.util.concurrent.Executors
import android.os.Handler
import android.os.Looper

import com.example.frontend.utils.BitmapUtils
import com.example.frontend.processors.*

class MainActivity : FlutterActivity() {
    private val CHANNEL = "com.samimalik.crowd_lens/detector"
    private val executor = Executors.newSingleThreadExecutor()
    private val mainHandler = Handler(Looper.getMainLooper())

    private val processors = mutableMapOf<String, FrameProcessor>()

    override fun configureFlutterEngine(@NonNull flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)
        
        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, CHANNEL).setMethodCallHandler { call, result ->
            val modelType = call.argument<String>("modelType") ?: ""
            
            when (call.method) {
                "processImage" -> {
                    val imageBytes = call.argument<ByteArray>("image")
                    if (imageBytes == null) {
                        result.error("INVALID_ARGUMENT", "Image bytes are null", null)
                        return@setMethodCallHandler
                    }

                    executor.execute {
                        var bitmap: Bitmap? = null
                        try {
                            bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size) ?: return@execute
                            val resizedBitmap = BitmapUtils.rotateFlipResize(bitmap, 0f, false, 640)
                            bitmap = resizedBitmap
                            
                            val processor = getProcessor(modelType)
                            val mpImage = BitmapImageBuilder(bitmap!!).build()
                            
                            val options = mutableMapOf<String, Any>()
                            call.argument<String>("effect")?.let { options["effect"] = it }
                            call.argument<Boolean>("isFrontCamera")?.let { options["isFront"] = it }

                            val output = processor?.process(mpImage, bitmap!!, options)
                            mainHandler.post {
                                if (output != null) result.success(output)
                                else result.error("UNKNOWN_MODEL", "Model $modelType not recognized", null)
                            }
                        } catch (e: Exception) {
                            mainHandler.post { result.error("DETECTION_ERROR", e.message, null) }
                        } finally {
                            bitmap?.recycle()
                        }
                    }
                }

                "processImageStream" -> {
                    val imageArgs = call.argument<Map<String, Any>>("imageArgs")
                    if (imageArgs == null) {
                        result.error("INVALID_ARGUMENT", "Image args are null", null)
                        return@setMethodCallHandler
                    }

                    executor.execute {
                        try {
                            val bitmap = BitmapUtils.decodeYUV420(imageArgs) ?: return@execute
                            val sensorOrientation = imageArgs["sensorOrientation"] as Int
                            val isFront = call.argument<Boolean>("isFrontCamera") ?: false
                            
                            val processedBitmap = BitmapUtils.rotateFlipResize(bitmap, sensorOrientation.toFloat(), isFront, 640)
                            
                            val processor = getProcessor(modelType)
                            val mpImage = BitmapImageBuilder(processedBitmap).build()

                            val options = mutableMapOf<String, Any>()
                            options["isFront"] = isFront
                            call.argument<String>("effect")?.let { options["effect"] = it }

                            val output = processor?.process(mpImage, processedBitmap, options)
                            
                            processedBitmap.recycle()

                            mainHandler.post {
                                if (output != null) result.success(output)
                                else result.error("UNKNOWN_MODEL", "Model $modelType not recognized", null)
                            }
                        } catch (e: Exception) {
                            mainHandler.post { result.error("DETECTION_ERROR", e.message, null) }
                        }
                    }
                }
                else -> result.notImplemented()
            }
        }
    }

    private fun getProcessor(type: String): FrameProcessor? {
        return processors.getOrPut(type) {
            when (type) {
                "face"     -> FaceProcessor(context)
                "finger"   -> HandProcessor(context)
                "pose"     -> PoseProcessor(context)
                "segment"  -> SegmentProcessor(context)
                "crowd"    -> CrowdProcessor(context)
                "classify" -> ClassifyProcessor(context)
                else -> return null
            }
        }
    }

    override fun onDestroy() {
        processors.values.forEach { it.close() }
        processors.clear()
        super.onDestroy()
    }
}