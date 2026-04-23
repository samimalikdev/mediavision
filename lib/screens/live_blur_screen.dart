import 'package:flutter/material.dart';
import 'package:get/get.dart';
import 'package:camera/camera.dart';
import '../controllers/detector_controller.dart';
import '../services/detector_service.dart';

class LiveBlurScreen extends StatelessWidget {
  final c = Get.put(DetectorController(model: DetectorModel.segment, effect: 'blur'), tag: 'blur_mode');

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: const Color(0xFF060818),
      extendBodyBehindAppBar: true,
      appBar: AppBar(
        title: const Text(
          'Live Blur (Bokeh)',
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
                  width: 1080,
                  height: 1080 * c.cameraController!.value.aspectRatio,
                  child: Obx(() {
                    final bytes = c.frameBytes.value;
                    if (bytes == null || bytes.isEmpty) {
                      return CameraPreview(c.cameraController!);
                    }
                    return Image.memory(
                      bytes,
                      fit: BoxFit.cover,
                      gaplessPlayback: true,
                    );
                  }),
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
                border: Border.all(color: Colors.orangeAccent.withOpacity(0.6)),
              ),
              child: Column(
                mainAxisSize: MainAxisSize.min,
                children: [
                  Text(
                    c.isStreaming.value ? 'ACTIVE' : 'INACTIVE',
                    style: const TextStyle(fontSize: 24, fontWeight: FontWeight.w900, color: Colors.orangeAccent),
                  ),
                  const Text(
                    'BOKEH EFFECT',
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
            child: Column(
              mainAxisSize: MainAxisSize.min,
              children: [
                Container(
                  padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 8),
                  decoration: BoxDecoration(
                    color: Colors.black.withOpacity(0.5),
                    borderRadius: BorderRadius.circular(30),
                  ),
                  child: Row(
                    mainAxisAlignment: MainAxisAlignment.spaceEvenly,
                    children: [
                      _buildEffectButton('blur', 'Blur', Icons.blur_on),
                      _buildEffectButton('virtual', 'Virtual', Icons.wallpaper),
                      _buildEffectButton('neon', 'Neon', Icons.wb_twilight),
                    ],
                  ),
                ),
                const SizedBox(height: 16),
                Obx(() => Row(
                  children: [
                    Expanded(
                      child: ElevatedButton.icon(
                        style: ElevatedButton.styleFrom(
                          backgroundColor: Colors.orangeAccent,
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
              ],
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildEffectButton(String effect, String label, IconData icon) {
    return Obx(() {
      final isSelected = c.activeEffect.value == effect;
      return GestureDetector(
        onTap: () => c.setEffect(effect),
        child: Container(
          padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
          decoration: BoxDecoration(
            color: isSelected ? Colors.orangeAccent : Colors.transparent,
            borderRadius: BorderRadius.circular(20),
          ),
          child: Row(
            children: [
              Icon(icon, size: 18, color: isSelected ? Colors.black : Colors.white70),
              const SizedBox(width: 6),
              Text(
                label,
                style: TextStyle(
                  color: isSelected ? Colors.black : Colors.white70,
                  fontWeight: isSelected ? FontWeight.bold : FontWeight.normal,
                  fontSize: 12,
                ),
              ),
            ],
          ),
        ),
      );
    });
  }
}
