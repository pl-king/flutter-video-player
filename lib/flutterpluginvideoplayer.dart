import 'dart:async';

import 'package:flutter/services.dart';

class Flutterpluginvideoplayer {
  static const MethodChannel _channel =
      const MethodChannel('flutterpluginvideoplayer');

  static Future<String> get platformVersion async {
    final String version = await _channel.invokeMethod('getPlatformVersion');
    return version;
  }
}
