import 'dart:io';
import 'dart:typed_data';
import 'package:flutter/material.dart';
import 'package:get/get.dart';
import 'package:image_picker/image_picker.dart';
import 'package:gal/gal.dart';
import 'package:path_provider/path_provider.dart';
import '../services/detector_service.dart';

class BackgroundEraserScreen extends StatelessWidget {
  final ImagePicker _picker = ImagePicker();
  final RxString selectedImagePath = ''.obs;
  final RxBool isProcessing = false.obs;
  final RxBool isSaving = false.obs;
  final RxMap<String, dynamic> results = <String, dynamic>{}.obs;

  Future<void> pickImage() async {
    final XFile? image = await _picker.pickImage(source: ImageSource.gallery);
    if (image != null) {
      selectedImagePath.value = image.path;
      isProcessing.value = true;
      results.clear();

      try {
        final Uint8List bytes = await File(image.path).readAsBytes();
        
        final result = await DetectorService.processImage(
          model: DetectorModel.segment,
          imageBytes: bytes,
          effect: 'transparent',
        );

        if (result != null) {
          results.value = result;
        }
      } catch (e) {
        Get.snackbar('Error', 'Failed to process image locally.');
      } finally {
        isProcessing.value = false;
      }
    }
  }

  Future<void> saveToGallery() async {
    if (!results.containsKey('frame')) return;
    
    isSaving.value = true;
    try {
      final Uint8List bytes = results['frame'] as Uint8List;
      
      final hasPermission = await Gal.hasAccess();
      if (!hasPermission) {
        await Gal.requestAccess();
      }

      final tempDir = await getTemporaryDirectory();
      final file = File('${tempDir.path}/erased_${DateTime.now().millisecondsSinceEpoch}.png');
      await file.writeAsBytes(bytes);

      await Gal.putImage(file.path);
      
      Get.snackbar(
        'Success', 
        'Transparent image saved to gallery!',
        backgroundColor: Colors.green.withOpacity(0.8),
        colorText: Colors.white,
        snackPosition: SnackPosition.BOTTOM,
      );
    } catch (e) {
      Get.snackbar('Error', 'Failed to save image: $e');
    } finally {
      isSaving.value = false;
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: const Color(0xFF060818),
      extendBodyBehindAppBar: true,
      appBar: AppBar(
        title: const Text(
          'Background Eraser',
          style: TextStyle(color: Colors.white, fontWeight: FontWeight.bold),
        ),
        backgroundColor: Colors.transparent,
        elevation: 0,
        iconTheme: const IconThemeData(color: Colors.white),
      ),
      body: Stack(
        children: [
          Positioned.fill(
            child: Container(
              color: const Color(0xFF060818),
              child: CustomPaint(painter: CheckerboardPainter()),
            ),
          ),
          Positioned.fill(
            child: SafeArea(
              child: Obx(() {
                if (isProcessing.value) {
                  return const Center(child: CircularProgressIndicator(color: Colors.white));
                }
                if (results.containsKey('frame')) {
                  return Center(
                    child: Padding(
                      padding: const EdgeInsets.all(20.0),
                      child: Image.memory(
                        results['frame'] as Uint8List,
                        fit: BoxFit.contain,
                      ),
                    ),
                  );
                }
                if (selectedImagePath.value.isNotEmpty) {
                  return Center(
                    child: Padding(
                      padding: const EdgeInsets.all(20.0),
                      child: Image.file(
                        File(selectedImagePath.value),
                        fit: BoxFit.contain,
                      ),
                    ),
                  );
                }
                return const Center(
                  child: Column(
                    mainAxisSize: MainAxisSize.min,
                    children: [
                      Icon(Icons.image_search_rounded, color: Colors.white24, size: 80),
                      SizedBox(height: 16),
                      Text('No Image Selected', style: TextStyle(color: Colors.white54, fontSize: 18)),
                    ],
                  ),
                );
              }),
            ),
          ),
          Positioned(
            bottom: 40,
            left: 20,
            right: 20,
            child: Column(
              mainAxisSize: MainAxisSize.min,
              children: [
                Obx(() => results.containsKey('frame') 
                  ? Padding(
                      padding: const EdgeInsets.only(bottom: 12),
                      child: SizedBox(
                        width: double.infinity,
                        child: ElevatedButton.icon(
                          style: ElevatedButton.styleFrom(
                            backgroundColor: Colors.greenAccent.shade700,
                            padding: const EdgeInsets.symmetric(vertical: 18),
                            shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(24)),
                          ),
                          onPressed: isSaving.value ? null : saveToGallery,
                          icon: isSaving.value 
                            ? const SizedBox(width: 20, height: 20, child: CircularProgressIndicator(strokeWidth: 2, color: Colors.white))
                            : const Icon(Icons.download_rounded, color: Colors.white),
                          label: const Text('Save Transparent PNG', style: TextStyle(color: Colors.white, fontSize: 16, fontWeight: FontWeight.bold)),
                        ),
                      ),
                    )
                  : const SizedBox.shrink()
                ),
                SizedBox(
                  width: double.infinity,
                  child: ElevatedButton.icon(
                    style: ElevatedButton.styleFrom(
                      backgroundColor: const Color(0xFFec4899),
                      padding: const EdgeInsets.symmetric(vertical: 18),
                      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(24)),
                    ),
                    onPressed: pickImage,
                    icon: const Icon(Icons.add_photo_alternate_rounded),
                    label: const Text('Select & Erase', style: TextStyle(fontSize: 16, fontWeight: FontWeight.bold)),
                  ),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }
}

class CheckerboardPainter extends CustomPainter {
  @override
  void paint(Canvas canvas, Size size) {
    const double squareSize = 20.0;
    final Paint paint1 = Paint()..color = const Color(0xFF1a1c2e);
    final Paint paint2 = Paint()..color = const Color(0xFF111322);
    for (double i = 0; i < size.width; i += squareSize) {
      for (double j = 0; j < size.height; j += squareSize) {
        if (((i / squareSize).floor() + (j / squareSize).floor()) % 2 == 0) {
          canvas.drawRect(Rect.fromLTWH(i, j, squareSize, squareSize), paint1);
        } else {
          canvas.drawRect(Rect.fromLTWH(i, j, squareSize, squareSize), paint2);
        }
      }
    }
  }
  @override
  bool shouldRepaint(CustomPainter oldDelegate) => false;
}
