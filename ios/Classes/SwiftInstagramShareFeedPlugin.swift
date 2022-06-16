import Flutter
import UIKit

public class SwiftInstagramShareFeedPlugin: NSObject, FlutterPlugin {
  public static func register(with registrar: FlutterPluginRegistrar) {
    let channel = FlutterMethodChannel(name: "instagram_share_feed", binaryMessenger: registrar.messenger())
    let instance = SwiftInstagramShareFeedPlugin()
    registrar.addMethodCallDelegate(instance, channel: channel)
  }

  public func handle(_ call: FlutterMethodCall, result: @escaping FlutterResult) {
      if(call.method=="shareToInstagramFeed"){
          result(true)
      }
  }
}
