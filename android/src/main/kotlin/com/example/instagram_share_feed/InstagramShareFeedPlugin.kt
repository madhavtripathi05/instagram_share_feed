package com.example.instagram_share_feed

import android.annotation.SuppressLint
import android.app.Activity
import android.content.*
import android.content.ContentValues.TAG
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
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
    private var activeContext: Context? = null
    private var context: Context? = null
    private val instagramPackageIdentifier = "com.instagram.android"

    override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        channel = MethodChannel(flutterPluginBinding.binaryMessenger, "instagram_share_feed")
        context = flutterPluginBinding.applicationContext
        channel.setMethodCallHandler(this)
    }

    override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {
        activeContext = if (activity != null) activity!!.applicationContext else context!!
        if (call.method == "shareToInstagramFeed") {
            val mediaType = call.argument<String>("mediaType")
            val mediaPath = call.argument<String>("mediaPath")
//            launchIntent(mediaPath!!)
//            result.success(true)
            shareToInstaFeed(mediaType!!, mediaPath!!, result)
        } else {
            result.notImplemented()
        }
    }

    override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
        channel.setMethodCallHandler(null)
    }


    private fun launchIntent(filePath: String) {

        val context: Context? = activeContext
        val intent = Intent(Intent.ACTION_SEND)

        insertToMediaStore(File(activeContext!!.cacheDir, filePath))?.let { shareableContent ->

            val clipLabel = when (shareableContent.mimeType.startsWith("image")) {
                true -> "Image"
                false -> "Video"
            }

            val clipData = ClipData.newRawUri(clipLabel, shareableContent.contentUri)

            intent.type = shareableContent.mimeType
            intent.clipData = clipData

            intent.putExtra(Intent.EXTRA_STREAM, shareableContent.contentUri)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.setPackage(instagramPackageIdentifier)

            val target = Intent.createChooser(intent, "Sharing Media")
            target?.let { context?.startActivity(it) }

        } ?: run {
            Log.e(TAG, "Unsupported media file")
            return
        }
    }

    data class ShareableContent(val contentUri: Uri, val mimeType: String)

    private fun insertToMediaStore(file: File): ShareableContent? {

        val mimeTypeString = when (file.extension) {
            "jpg", "jpeg" -> "image/jpeg"
            "png" -> "image/png"
            "mp4" -> "video/mp4" // add whichever other video extensions that you want to support
            else -> return null
        }

        val uri = when (mimeTypeString.startsWith("image")) {
            true -> insertImageToMediaStore(
                file,
                "DCIM/images",
                mimeTypeString
            )
            else -> insertVideoToMediaStore(
                file,
                " DCIM/movies",
                mimeTypeString
            )
        }

        uri?.let {
            return ShareableContent(contentUri = uri, mimeType = mimeTypeString)
        } ?: throw RuntimeException("Unable to insert to media store")
    }

    @SuppressLint("InlinedApi")
    fun insertImageToMediaStore(file: File, relativePath: String, mimeType: String): Uri? {

        val values = ContentValues().apply {

            put(MediaStore.Images.Media.DISPLAY_NAME, file.name)
            put(MediaStore.Images.Media.MIME_TYPE, mimeType)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.MediaColumns.RELATIVE_PATH, relativePath)
                put(MediaStore.MediaColumns.IS_PENDING, 1)
            }
        }

        val collection = when (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            true -> MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
            false -> MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        }

        insertFileToMediaStore(activeContext!!.contentResolver, collection, values, file)?.let {
            return it
        } ?: run {
            return null
        }
    }

    @SuppressLint("InlinedApi")
    fun insertVideoToMediaStore(file: File, relativePath: String, mimeType: String): Uri? {

        val values = ContentValues().apply {

            put(MediaStore.Video.Media.DISPLAY_NAME, file.name)
            put(MediaStore.Video.Media.MIME_TYPE, mimeType)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.MediaColumns.RELATIVE_PATH, relativePath)
                put(MediaStore.MediaColumns.IS_PENDING, 1)
            }
        }

        val collection = when (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            true -> MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
            false -> MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        }

        insertFileToMediaStore(activeContext!!.contentResolver, collection, values, file)?.let {
            return it
        } ?: run {
            return null
        }
    }

    private fun insertFileToMediaStore(
        contentResolver: ContentResolver,
        collection: Uri,
        values: ContentValues,
        file: File
    ): Uri? {

        val uri = contentResolver.insert(collection, values)

        uri?.let {
            contentResolver.openOutputStream(uri)?.use { outputStream ->
                try {
                    outputStream.write(file.readBytes())
                    outputStream.close()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            values.clear()

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                values.put(MediaStore.Images.Media.IS_PENDING, 0)
                contentResolver.update(uri, values, null, null)
            }

        }
//            ?: throw RuntimeException("Unable to write file to MediaStore")

        return uri
    }

    private fun shareToInstaFeed(mediaType: String, mediaPath: String, result: Result) {
        /// Creating a media file (Can be an image or a video)
        val mediaFile = File(activeContext!!.cacheDir, mediaPath)
        if (mediaType == "image") {
            val imageFile = FileProvider.getUriForFile(
                activeContext!!,
                activeContext!!.applicationContext.packageName + ".com.example.instagram_share_feed",
                mediaFile
            )
            /// Creating and adding items to intent
//            val intent = Intent("com.instagram.android")
//            intent.type = "image/*"
//            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
//            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//            intent.putExtra("interactive_asset_uri", imageFile)
            // TODO: Take this from params
//            intent.putExtra("top_background_color", "#111111")
//            intent.putExtra("bottom_background_color", "#111111")
//            if (backgroundImage!=null) {
//                //check if background image is also provided
//                val backgroundFile =  File(activeContext!!.cacheDir,backgroundImage)
//                val backgroundImageFile = FileProvider.getUriForFile(activeContext!!, activeContext!!.applicationContext.packageName + ".com.example.instagram_share_feed", backgroundFile)
//                intent.setDataAndType(backgroundImageFile,"image/*")
//            }

            // Instantiate activity and verify it will resolve implicit intent
//            activity!!.grantUriPermission("com.instagram.android", imageFile, Intent.FLAG_GRANT_READ_URI_PERMISSION)
//            if (activity!!.packageManager.resolveActivity(intent, 0) != null) {
//                try {
//
//                activeContext!!.startActivity(intent)
//                    result.success(true)
//                } catch (ex: Exception) {
//                    result.error("-1", ex.toString(), ex)
//                }
//                result.success(true)
//            } else {
//                result.success(false)
//            }

            val sendIntent = Intent("com.instagram.share.ADD_TO_STORY")
            sendIntent.type = "image/*"
            Log.i("imagePath",imageFile.path!!)
            sendIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            sendIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            sendIntent.putExtra("interactive_asset_uri", imageFile)
            activity!!.grantUriPermission("com.instagram.android", imageFile, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            val resolvableActivity  = activity!!.packageManager.resolveActivity(sendIntent,0)
            Log.i("resolvableActivity",resolvableActivity.toString())

            try {
                activeContext!!.startActivity(sendIntent)
                result.success(true)
            } catch (ex: Exception) {
                result.error("-1", ex.toString(), ex)
            }

        }
//        val sendIntent = Intent(Intent.ACTION_VIEW)
//        sendIntent.type = mediaType
//        sendIntent.action = Intent.ACTION_SEND
//        sendIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://$mediaPath"))
//        sendIntent.putExtra(Intent.EXTRA_TEXT, "Sharing from plugin")
//        sendIntent.setPackage("com.instagram.android")
//        sendIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
//        try {
//            startActivity(context!!, Intent.createChooser(sendIntent, "Share image"), null)
//            result.success(true)
//        } catch (ex: Exception) {
//            result.error("-1", ex.toString(), ex)
//        }
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
