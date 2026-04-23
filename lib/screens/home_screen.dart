import 'package:flutter/material.dart';
import 'package:get/get.dart';
import 'package:flutter/services.dart';

import 'crowd_detector_screen.dart';
import 'person_detector_screen.dart';
import 'face_detector_screen.dart';
import 'pose_detector_screen.dart';
import 'image_classifier_screen.dart';
import 'background_eraser_screen.dart';
import 'live_blur_screen.dart';

class HomeScreen extends StatelessWidget {
  const HomeScreen({super.key});

  @override
  Widget build(BuildContext context) {
    SystemChrome.setSystemUIOverlayStyle(SystemUiOverlayStyle.light);

    return Scaffold(
      backgroundColor: const Color.fromARGB(255, 0, 0, 0),
      body: SafeArea(
        child: Padding(
          padding: const EdgeInsets.symmetric(horizontal: 24.0, vertical: 32.0),
          child: SingleChildScrollView(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.stretch,
              children: [
                const SizedBox(height: 40),
                const Icon(
                  Icons.lens_blur_rounded,
                  size: 80,
                  color: Color(0xFF2563eb),
                ),
                const SizedBox(height: 24),
                const Text(
                  'MediaVision',
                  textAlign: TextAlign.center,
                  style: TextStyle(
                    fontSize: 36,
                    fontWeight: FontWeight.bold,
                    color: Colors.white,
                    letterSpacing: 1.2,
                  ),
                ),
                const SizedBox(height: 12),
                Text(
                  'Choose a detection mode to begin analyzing your live camera stream.',
                  textAlign: TextAlign.center,
                  style: TextStyle(
                    fontSize: 16,
                    color: Colors.white.withOpacity(0.7),
                    height: 1.5,
                  ),
                ),
                const SizedBox(height: 60),

                _buildModeCard(
                  title: 'Finger Counting',
                  subtitle:
                      'Real-time hand landmark tracking and finger counting using MediaPipe.',
                  icon: Icons.back_hand_rounded,
                  color: const Color(0xFF7c3aed),
                  onTap: () => Get.to(() => CrowdDetectorScreen()),
                ),
                const SizedBox(height: 20),
                _buildModeCard(
                  title: 'Crowd Detection',
                  subtitle:
                      'Real-time person detection and bounding boxes using YOLOv8.',
                  icon: Icons.groups_rounded,
                  color: const Color(0xFF2563eb),
                  onTap: () => Get.to(() => PersonDetectorScreen()),
                ),
                const SizedBox(height: 20),
                _buildModeCard(
                  title: 'Face Detection',
                  subtitle:
                      'Real-time face detection with bounding boxes using MediaPipe.',
                  icon: Icons.face_retouching_natural_rounded,
                  color: const Color(0xFFeab308),
                  onTap: () => Get.to(() => FaceDetectorScreen()),
                ),
                const SizedBox(height: 20),
                _buildModeCard(
                  title: 'Pose Tracking',
                  subtitle:
                      'Real-time full-body posture tracking and skeleton using MediaPipe.',
                  icon: Icons.accessibility_new_rounded,
                  color: const Color(0xFF10b981),
                  onTap: () => Get.to(() => PoseDetectorScreen()),
                ),
                const SizedBox(height: 20),
                _buildModeCard(
                  title: 'Image Classification',
                  subtitle:
                      'Real-time object recognition using EfficientNet Lite0.',
                  icon: Icons.auto_awesome_rounded,
                  color: const Color(0xFFa855f7),
                  onTap: () => Get.to(() => ImageClassifierScreen()),
                ),
                const SizedBox(height: 20),
                _buildModeCard(
                  title: 'Background Eraser',
                  subtitle:
                      'Select a photo from your gallery and instantly remove the background.',
                  icon: Icons.image_not_supported_rounded,
                  color: const Color(0xFFec4899),
                  onTap: () => Get.to(() => BackgroundEraserScreen()),
                ),
                const SizedBox(height: 20),
                _buildModeCard(
                  title: 'Live Blur (Bokeh)',
                  subtitle:
                      'Real-time background blurring focused on portrait depth.',
                  icon: Icons.blur_on_rounded,
                  color: const Color(0xFFf97316),
                  onTap: () => Get.to(() => LiveBlurScreen()),
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }

  Widget _buildModeCard({
    required String title,
    required String subtitle,
    required IconData icon,
    required Color color,
    required VoidCallback onTap,
  }) {
    return InkWell(
      onTap: onTap,
      borderRadius: BorderRadius.circular(24),
      child: Container(
        padding: const EdgeInsets.all(24),
        decoration: BoxDecoration(
          color: Colors.white.withOpacity(0.05),
          borderRadius: BorderRadius.circular(24),
          border: Border.all(color: color.withOpacity(0.3), width: 1),
          boxShadow: [
            BoxShadow(
              color: color.withOpacity(0.1),
              blurRadius: 20,
              offset: const Offset(0, 10),
            ),
          ],
        ),
        child: Row(
          children: [
            Container(
              padding: const EdgeInsets.all(16),
              decoration: BoxDecoration(
                color: color.withOpacity(0.2),
                borderRadius: BorderRadius.circular(16),
              ),
              child: Icon(icon, size: 32, color: color),
            ),
            const SizedBox(width: 20),
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    title,
                    style: const TextStyle(
                      fontSize: 20,
                      fontWeight: FontWeight.bold,
                      color: Colors.white,
                    ),
                  ),
                  const SizedBox(height: 6),
                  Text(
                    subtitle,
                    style: TextStyle(
                      fontSize: 13,
                      color: Colors.white.withOpacity(0.6),
                      height: 1.4,
                    ),
                  ),
                ],
              ),
            ),
            const SizedBox(width: 10),
            Icon(
              Icons.chevron_right_rounded,
              color: Colors.white.withOpacity(0.5),
            ),
          ],
        ),
      ),
    );
  }
}
