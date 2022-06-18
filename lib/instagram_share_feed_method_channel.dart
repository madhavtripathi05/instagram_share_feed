import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';

import 'instagram_share_feed_platform_interface.dart';

/// An implementation of [InstagramShareFeedPlatform] that uses method channels.
class MethodChannelInstagramShareFeed extends InstagramShareFeedPlatform {
  /// The method channel used to interact with the native platform.
  @visibleForTesting
  final methodChannel = const MethodChannel('instagram_share_feed');

  @override
  Future<bool?> shareToInstagramFeed(
      {required String mediaPath, required MediaType mediaType}) async {
    final success =
        await methodChannel.invokeMethod<bool>('shareToInstagramFeed', {
      'mediaType': mediaType.name,
      'mediaPath': mediaPath,
    });
    return success ?? false;
  }
}

enum MediaType {
  image,
  video,
}
