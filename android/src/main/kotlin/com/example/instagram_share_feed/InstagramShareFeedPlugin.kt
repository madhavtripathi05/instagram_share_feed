package com.example.instagram_share_feed

import android.app.Activity
import android.content.*
import android.util.Log
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
    private val twitterPackageIdentifier = "com.twitter.android"


    override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        channel = MethodChannel(flutterPluginBinding.binaryMessenger, "instagram_share_feed")
        channel.setMethodCallHandler(this)
    }

    override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {
        when (call.method) {
            "shareToInstagramFeed" -> {
                val mediaType = call.argument<String>("mediaType")
                val mediaPath = call.argument<String>("mediaPath")
                shareToInstagram(mediaPath!!, mediaType!!, result)
            }
            "shareToTwitter" -> {
                val mediaType = call.argument<String>("mediaType")
                val mediaPath = call.argument<String>("mediaPath")
                val contentText = call.argument<String>("contentText")
                Log.i("share to twitter before call", "$mediaPath $mediaType $contentText")

                shareToTwitter(mediaPath!!, mediaType!!, contentText!!, result);
            }
            else -> {
                result.notImplemented()
            }
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

    private fun shareToTwitter(mediaPath: String, mediaType: String,contentText: String, result: Result ) {
        Log.i("share to twitter", "$mediaPath $mediaType $contentText")
        val file = File(mediaPath)
        val fileUri = FileProvider.getUriForFile(
            activity!!,
            activity!!.applicationContext.packageName + ".com.example.instagram_share_feed",
            file
        )
         val intent = Intent(Intent.ACTION_SEND)
         intent.putExtra(Intent.EXTRA_TEXT, contentText)
         intent.type = "text/plain"
         intent.putExtra(Intent.EXTRA_STREAM, fileUri)
         intent.type = "image/*"
         intent.setPackage(twitterPackageIdentifier)
         try {
             activity!!.startActivity(intent)
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
