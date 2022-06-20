import 'instagram_share_feed_method_channel.dart';

class InstagramShareFeed {
  static Future<bool?> shareToInstagramFeed(
      {required String mediaPath, required MediaType mediaType}) {
    return MethodChannelInstagramShareFeed()
        .shareToInstagramFeed(mediaPath: mediaPath, mediaType: mediaType);
  }

  static Future<bool?> shareToTwitter({
    required String mediaPath,
    required MediaType mediaType,
    required String contentText,
  }) {
    return MethodChannelInstagramShareFeed().shareToTwitter(
      mediaPath: mediaPath,
      mediaType: mediaType,
      contentText: contentText,
    );
  }
}
