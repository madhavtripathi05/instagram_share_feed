package com.example.instagram_share_feed

import android.app.Activity
import android.content.*
import androidx.annotation.NonNull
import androidx.core.content.FileProvider
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import java.io.File


/** InstagramShareFeedPlugin */
class InstagramShareFeedPlugin : FlutterPlugin, MethodCallHandler, ActivityAware {
    /// The MethodChannel that will the communication between Flutter and native Android
    ///
    /// This local reference serves to register the plugin with the Flutter Engine and unregister it
    /// when the Flutter Engine is detached from the Activity
    private lateinit var channel: MethodChannel
    private var activity: Activity? = null
    private val instagramPackageIdentifier = "com.instagram.android"

    override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        channel = MethodChannel(flutterPluginBinding.binaryMessenger, "instagram_share_feed")
        channel.setMethodCallHandler(this)
    }

    override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {
        if (call.method == "shareToInstagramFeed") {
            val mediaType = call.argument<String>("mediaType")
            val mediaPath = call.argument<String>("mediaPath")
            shareToInstagram(mediaPath!!, mediaType!!, result)
        } else {
            result.notImplemented()
        }
    }

    override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
        channel.setMethodCallHandler(null)
    }

    private fun shareToInstagram(mediaPath: String, mediaType: String, result: Result) {
        val file = File(mediaPath)
        val fileUri = FileProvider.getUriForFile(
            activity!!,
            activity!!.applicationContext.packageName + ".com.example.instagram_share_feed",
            file
        )
        val instagramIntent = Intent(Intent.ACTION_SEND)
        instagramIntent.type = "$mediaType/*"
        instagramIntent.putExtra(Intent.EXTRA_STREAM, fileUri)
        instagramIntent.setPackage(instagramPackageIdentifier)
        try {
            activity!!.startActivity(instagramIntent)
            result.success(true)
        } catch (e: Exception) {
            e.printStackTrace()
            result.error("-1", e.message, e.toString())
        }
    }

    override fun onAttachedToActivity(binding: ActivityPluginBinding) {
        activity = binding.activity
    }

    override fun onDetachedFromActivityForConfigChanges() {
        activity = null
    }

    override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
        activity = binding.activity
    }

    override fun onDetachedFromActivity() {
        activity = null
    }
}
