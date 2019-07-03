import 'dart:async';
import 'dart:io';

import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';

enum CameraType {
  ///普通拍照
  general,

  /// 身份证正面(头像).
  IDCardFront,

  ///身份证背面(国徽)
  IDCardBack,

  ///手持身份证
  handheld,

  ///银行卡
  bankCard,
}

class MediaPicker {
  static const MethodChannel _channel = const MethodChannel('media_picker');

  static Future<String> get platformVersion async {
    final String version = await _channel.invokeMethod('getPlatformVersion');
    return version;
  }

  static Future<List<File>> pickMedias({int maxSelectCount = 1}) async {
    final List<dynamic> paths = await _channel.invokeMethod<List<dynamic>>(
      "pickMedias",
      <String, dynamic>{
        'maxSelectCount': maxSelectCount,
      },
    );
    return paths.isNotEmpty
        ? paths.map((path) => new File(path)).toList()
        : null;
  }

  static Future<File> pickMedia() async {
    final String path = await _channel.invokeMethod<String>(
      "pickMedia",
    );
    return path.isNotEmpty ? new File(path) : null;
  }

  static Future<File> takePhoto({CameraType type = CameraType.general}) async {
    final String path = await _channel.invokeMethod<String>(
      "takePhoto",
      <String, dynamic>{
        'cameraType': type.index,
      },
    );
    return path.isNotEmpty ? new File(path) : null;
  }
}
