import 'dart:async';

import 'package:flutter/services.dart';

class PluginVideoplayer {
  static const MethodChannel _channel =
      const MethodChannel('plugin_videoplayer');

  static Future<String> get platformVersion async {
    final String version = await _channel.invokeMethod('getPlatformVersion');
    return version;
  }
}
