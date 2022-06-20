import 'package:plugin_platform_interface/plugin_platform_interface.dart';

import 'instagram_share_feed_method_channel.dart';

abstract class InstagramShareFeedPlatform extends PlatformInterface {
  /// Constructs a InstagramShareFeedPlatform.
  InstagramShareFeedPlatform() : super(token: _token);

  static final Object _token = Object();

  static InstagramShareFeedPlatform _instance =
      MethodChannelInstagramShareFeed();

  /// The default instance of [InstagramShareFeedPlatform] to use.
  ///
  /// Defaults to [MethodChannelInstagramShareFeed].
  static InstagramShareFeedPlatform get instance => _instance;

  /// Platform-specific implementations should set this with their own
  /// platform-specific class that extends [InstagramShareFeedPlatform] when
  /// they register themselves.
  static set instance(InstagramShareFeedPlatform instance) {
    PlatformInterface.verifyToken(instance, _token);
    _instance = instance;
  }

  Future<bool?> shareToInstagramFeed(
      {required String mediaPath, required MediaType mediaType}) {
    throw UnimplementedError(
        'shareToInstagramFeed() has not been implemented.');
  }

  Future<bool?> shareToTwitter({
    required String mediaPath,
    required MediaType mediaType,
    required String contentText,
  }) {
    throw UnimplementedError('shareToTwitter() has not been implemented.');
  }
}
