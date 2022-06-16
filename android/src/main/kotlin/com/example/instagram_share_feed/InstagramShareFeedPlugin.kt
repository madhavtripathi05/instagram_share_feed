package com.example.instagram_share_feed

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.annotation.NonNull
import androidx.core.content.ContextCompat.startActivity
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result


/** InstagramShareFeedPlugin */
class InstagramShareFeedPlugin : FlutterPlugin, MethodCallHandler {
  /// The MethodChannel that will the communication between Flutter and native Android
  ///
  /// This local reference serves to register the plugin with the Flutter Engine and unregister it
  /// when the Flutter Engine is detached from the Activity
  private lateinit var channel : MethodChannel
  private lateinit var context: Context

  override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
    channel = MethodChannel(flutterPluginBinding.binaryMessenger, "instagram_share_feed")
    context = flutterPluginBinding.applicationContext
    channel.setMethodCallHandler(this)
  }

  override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) =
    if (call.method == "shareToInstagramFeed") {
      val mediaType = call.argument<String>("mediaType")
      val mediaPath = call.argument<String>("mediaPath")
      val success = shareToInstaFeed(mediaType!!,mediaPath!!)
      result.success(success)
    } else {
      result.notImplemented()
    }

  override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
    channel.setMethodCallHandler(null)
  }

  private fun shareToInstaFeed(mediaType: String, mediaPath: String): Boolean {
    val sendIntent = Intent(Intent.ACTION_VIEW)
    sendIntent.type = mediaType
    sendIntent.action = Intent.ACTION_SEND
    sendIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://$mediaPath"))
    sendIntent.putExtra(Intent.EXTRA_TEXT, "Sharing from plugin")
    sendIntent.setPackage("com.instagram.android")
    sendIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    return try {
      startActivity(context, Intent.createChooser(sendIntent, "Share image"), null)
      true
    } catch (ex: ActivityNotFoundException) {
      false
    }
  }
}
