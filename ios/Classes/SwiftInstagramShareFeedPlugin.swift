import Flutter
import UIKit
import PhotosUI

public class SwiftInstagramShareFeedPlugin: NSObject, FlutterPlugin {
    
    var result: FlutterResult?
    
    public static func register(with registrar: FlutterPluginRegistrar) {
        let channel = FlutterMethodChannel(name: "instagram_share_feed", binaryMessenger: registrar.messenger())
        let instance = SwiftInstagramShareFeedPlugin()
        registrar.addMethodCallDelegate(instance, channel: channel)
    }
    
    public func handle(_ call: FlutterMethodCall, result: @escaping FlutterResult) {
        self.result = result
        if(call.method == "shareToInstagramFeed"){
            let args = call.arguments as? [String:Any]
            postImageToInstagram(args: args!)
        }
    }
    
    func postImageToInstagram(args:Dictionary<String,Any>)  {
        let imageUrl = args["mediaPath"] as! String
        let image = UIImage(named: imageUrl)
        
        if(image==nil){
            self.result!(FlutterError( code: "-1",
                                       message: "Invalid file format",
                                       details: "File format not supported."))
            return;
        }
        
        UIImageWriteToSavedPhotosAlbum(image!, self, #selector(image(_:didFinishSavingWithError:contextInfo:)), nil)
    }
    
    @objc func image(_ image: UIImage, didFinishSavingWithError error: Error?, contextInfo: UnsafeRawPointer) {
        if error != nil {
            print(error!)
            self.result?(FlutterError( code: "-1",
                                       message: "something went wrong",
                                       details: error! ))
        }
        
        let fetchOptions = PHFetchOptions()
        fetchOptions.sortDescriptors = [NSSortDescriptor(key: "creationDate", ascending: false)]
        
        let fetchResult = PHAsset.fetchAssets(with: .image, options: fetchOptions)
        
        if let lastAsset = fetchResult.firstObject {
            
            let url = URL(string: "instagram://library?LocalIdentifier=\(lastAsset.localIdentifier)")!
            
            if UIApplication.shared.canOpenURL(url) {
                if #available(iOS 10.0, *) {
                    UIApplication.shared.open(url)
                } else {
                    UIApplication.shared.openURL(url)
                }
                
                self.result?(true)
                
            } else {
                self.result?(FlutterError( code: "-1",
                                           message: "Instagram app is not installed on your device",
                                           details: "Instagram app is not installed on your device" ))
            }
        }
    }
}
