import 'dart:async';
import 'dart:io';
import 'dart:typed_data';
import 'package:camera/camera.dart';
import 'package:get/get.dart';
import '../services/detector_service.dart';

class DetectorController extends GetxController {
  final DetectorModel model;
  final RxString activeEffect = 'blur'.obs;
  bool _isProcessing = false;
  int _lastFrameTime = 0;

  DetectorController({required this.model, String? effect}) {
    if (effect != null) activeEffect.value = effect;
  }

  void setEffect(String effect) {
    activeEffect.value = effect;
  }

  RxBool isInitialized = false.obs;
  RxBool isStreaming = false.obs;
  RxBool isFrontCamera = true.obs;
  
  CameraController? cameraController;
  List<CameraDescription>? cameras;

  RxMap<String, dynamic> results = <String, dynamic>{}.obs;
  Rx<Uint8List?> frameBytes = Rx<Uint8List?>(null);
  RxInt count = 0.obs;
  RxString status = ''.obs;

  Timer? _timer;

  @override
  void onInit() {
    super.onInit();
    _initializeCamera();
  }

  Future<void> _initializeCamera() async {
    cameras = await availableCameras();
    if (cameras != null && cameras!.isNotEmpty) {
      final camera = cameras!.firstWhere(
        (c) => c.lensDirection == (isFrontCamera.value ? CameraLensDirection.front : CameraLensDirection.back),
        orElse: () => cameras!.first,
      );
      
      isFrontCamera.value = camera.lensDirection == CameraLensDirection.front;

      cameraController = CameraController(
        camera,
        ResolutionPreset.medium,
        enableAudio: false,
        imageFormatGroup: Platform.isAndroid ? ImageFormatGroup.yuv420 : ImageFormatGroup.bgra8888,
      );

      await cameraController!.initialize();
      isInitialized.value = true;
    }
  }

  int _frameCount = 0;

  void startStream() {
    isStreaming.value = true;
    _isProcessing = false;
    _frameCount = 0;
    cameraController?.startImageStream((CameraImage image) {
      if (_isProcessing) return;
      
      _lastFrameTime = DateTime.now().millisecondsSinceEpoch;
      _isProcessing = true;
      
      try {
        final planes = image.planes.map((p) => p.bytes).toList();
        _processFrame(image, planes);
      } catch (e) {
        _isProcessing = false;
        print("Error copying frame: $e");
      }
    });
  }

  Future<void> _processFrame(CameraImage image, List<Uint8List> copiedPlanes) async {
    try {
      Map<String, dynamic> imageArgs;

      if (Platform.isAndroid) {
        imageArgs = {
          'format': 'yuv420',
          'planes': copiedPlanes,
          'yRowStride': image.planes[0].bytesPerRow,
          'uvRowStride': image.planes[1].bytesPerRow,
          'uvPixelStride': image.planes[1].bytesPerPixel,
          'width': image.width,
          'height': image.height,
          'sensorOrientation': cameraController!.description.sensorOrientation,
        };
      } else {
        imageArgs = {
          'format': 'bgra8888',
          'bytes': image.planes[0].bytes,
          'bytesPerRow': image.planes[0].bytesPerRow,
          'width': image.width,
          'height': image.height,
          'sensorOrientation': cameraController!.description.sensorOrientation,
        };
      }

      final result = await DetectorService.processImageStream(
        model: model,
        imageArgs: imageArgs,
        isFrontCamera: isFrontCamera.value,
        effect: activeEffect.value,
      );

      if (result != null) {
        if (result.containsKey('frame')) {
          print("Frame Received: ${result['status'] ?? 'unknown'}");
        }
        if (result.containsKey('detections') || result.containsKey('faces') || result.containsKey('hands') || result.containsKey('poses')) {
          print("Valid Detections Received: ${result.keys}");
        } else if (!result.containsKey('frame')) {
          print("Empty Result or No Objects Found");
        }
        _updateCounts(result);
      }
    } catch (e) {
      print("Stream error: $e");
    } finally {
      _isProcessing = false;
    }
  }

  void _updateCounts(Map<String, dynamic> res) {
    final raw = res['frame'];
    if (raw != null) {
      frameBytes.value = raw is Uint8List 
          ? raw 
          : Uint8List.fromList(List<int>.from(raw));
    }

    switch (model) {
      case DetectorModel.crowd:
        count.value = res['count'] ?? 0;
        status.value = count.value > 10
            ? 'Crowded'
            : count.value > 0
                ? 'Sparse'
                : 'Empty';
        break;
      case DetectorModel.finger:
        count.value = res['total_fingers'] ?? 0;
        status.value = count.value > 0 ? 'Hand Detected' : 'No Hand';
        break;
      case DetectorModel.face:
        count.value = res['face_count'] ?? 0;
        status.value = count.value > 0 ? 'Face Detected' : 'Scanning...';
        break;
      case DetectorModel.pose:
        count.value = res['pose_count'] ?? 0;
        status.value = count.value > 0 ? 'Body Detected' : 'Scanning...';
        break;
      case DetectorModel.classify:
        final cats = res['classifications'] as List?;
        status.value = cats?.firstOrNull?['label'] ?? 'Unknown';
        break;
      case DetectorModel.segment:
        status.value = res['status'] ?? '';
        break;
    }
  }

  Future<void> stopStream() async {
    if (cameraController == null) return;
    isStreaming.value = false;
    frameBytes.value = null;
    if (cameraController!.value.isStreamingImages) {
      await cameraController?.stopImageStream();
    }
  }

  Future<void> switchCamera() async {
    isFrontCamera.value = !isFrontCamera.value;
    isInitialized.value = false;
    await cameraController?.dispose();
    await _initializeCamera();
    if (isStreaming.value) {
      startStream();
    }
  }

  @override
  void onClose() {
    stopStream();
    cameraController?.dispose();
    super.onClose();
  }
}
