import 'dart:typed_data';
import 'package:flutter/services.dart';

enum DetectorModel {
  crowd,
  finger,
  face,
  pose,
  classify,
  segment
}

class DetectorService {
  static const _channel = MethodChannel('com.samimalik.crowd_lens/detector');

  static Future<Map<String, dynamic>?> processImage({
    required DetectorModel model,
    required Uint8List imageBytes,
    bool isFrontCamera = false,
    String? effect,
  }) async {
    try {
      final result = await _channel.invokeMapMethod<String, dynamic>(
        'processImage',
        {
          'modelType': model.name,
          'image': imageBytes,
          'isFrontCamera': isFrontCamera,
          'effect': effect,
        },
      );
      return result;
    } on PlatformException catch (e) {
      print("Detection failed: ${e.message}");
      return null;
    }
  }

  static Future<Map<String, dynamic>?> processImageStream({
    required DetectorModel model,
    required Map<String, dynamic> imageArgs,
    bool isFrontCamera = false,
    String? effect,
  }) async {
    try {
      final result = await _channel.invokeMapMethod<String, dynamic>(
        'processImageStream',
        {
          'modelType': model.name,
          'imageArgs': imageArgs,
          'isFrontCamera': isFrontCamera,
          'effect': effect,
        },
      );
      return result;
    } on PlatformException catch (e) {
      print("Detection failed: ${e.message}");
      return null;
    }
  }
}
