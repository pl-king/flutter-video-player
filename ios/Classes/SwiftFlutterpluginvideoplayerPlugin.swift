import Flutter
import UIKit

public class SwiftFlutterpluginvideoplayerPlugin: NSObject, FlutterPlugin {
  public static func register(with registrar: FlutterPluginRegistrar) {
    let channel = FlutterMethodChannel(name: "flutterpluginvideoplayer", binaryMessenger: registrar.messenger())
    let instance = SwiftFlutterpluginvideoplayerPlugin()
    registrar.addMethodCallDelegate(instance, channel: channel)
  }

  public func handle(_ call: FlutterMethodCall, result: @escaping FlutterResult) {
    result("iOS " + UIDevice.current.systemVersion)
  }
}
