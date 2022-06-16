import 'instagram_share_feed_method_channel.dart';

class InstagramShareFeed {
  static Future<bool> shareToInstagramFeed(
      {required String mediaPath, required MediaType mediaType}) {
    return MethodChannelInstagramShareFeed()
        .shareToInstagramFeed(mediaPath: mediaPath, mediaType: mediaType);
  }
}
