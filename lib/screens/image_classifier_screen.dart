import 'package:flutter/material.dart';
import 'package:get/get.dart';
import 'package:camera/camera.dart';
import '../controllers/detector_controller.dart';
import '../services/detector_service.dart';

class ImageClassifierScreen extends StatelessWidget {
  final c = Get.put(DetectorController(model: DetectorModel.classify), tag: 'classify_mode');

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: const Color(0xFF060818),
      extendBodyBehindAppBar: true,
      appBar: AppBar(
        title: const Text(
          'Image Classifier',
          style: TextStyle(color: Colors.white, fontWeight: FontWeight.bold),
        ),
        backgroundColor: Colors.transparent,
        elevation: 0,
        iconTheme: const IconThemeData(color: Colors.white),
      ),
      body: Stack(
        children: [
          Positioned.fill(
            child: Obx(() {
              if (!c.isInitialized.value) return const Center(child: CircularProgressIndicator());
              return FittedBox(
                fit: BoxFit.cover,
                clipBehavior: Clip.hardEdge,
                child: SizedBox(
                  width: 1,
                  height: c.cameraController!.value.aspectRatio,
                  child: CameraPreview(c.cameraController!),
                ),
              );
            }),
          ),
          Positioned(
            top: kToolbarHeight + 20,
            right: 20,
            child: Obx(() => Container(
              padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 16),
              decoration: BoxDecoration(
                color: Colors.black.withOpacity(0.65),
                borderRadius: BorderRadius.circular(24),
                border: Border.all(color: Colors.purpleAccent.withOpacity(0.6)),
              ),
              child: Column(
                mainAxisSize: MainAxisSize.min,
                children: [
                  Text(
                    c.status.value,
                    style: const TextStyle(fontSize: 20, fontWeight: FontWeight.w900, color: Colors.purpleAccent),
                  ),
                  const Text(
                    'TOP PREDICTION',
                    style: TextStyle(color: Colors.white70, fontSize: 11, fontWeight: FontWeight.bold),
                  ),
                ],
              ),
            )),
          ),
          Positioned(
            bottom: 40,
            left: 20,
            right: 20,
            child: Obx(() => Row(
              children: [
                Expanded(
                  child: ElevatedButton.icon(
                    style: ElevatedButton.styleFrom(
                      backgroundColor: Colors.purpleAccent,
                      padding: const EdgeInsets.symmetric(vertical: 18),
                      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(24)),
                    ),
                    onPressed: c.isStreaming.value ? c.stopStream : c.startStream,
                    icon: Icon(c.isStreaming.value ? Icons.stop_rounded : Icons.videocam_rounded),
                    label: Text(c.isStreaming.value ? 'Stop' : 'Start'),
                  ),
                ),
                const SizedBox(width: 12),
                IconButton(
                  onPressed: c.switchCamera,
                  icon: const Icon(Icons.flip_camera_ios_rounded, color: Colors.white),
                ),
              ],
            )),
          ),
        ],
      ),
    );
  }
}
