import 'package:flutter/material.dart';
import 'dart:async';

import 'package:flutter/services.dart';
import 'package:image_picker/image_picker.dart';
import 'package:instagram_share_feed/instagram_share_feed.dart';
import 'package:instagram_share_feed/instagram_share_feed_method_channel.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatefulWidget {
  const MyApp({Key? key}) : super(key: key);

  @override
  State<MyApp> createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  void shareToInstagramFeed() async {
    final image = await ImagePicker().pickImage(source: ImageSource.gallery);
    if (image == null) {
      throw Exception('Empty image returned');
    }
    InstagramShareFeed.shareToInstagramFeed(
        mediaPath: image.path, mediaType: MediaType.image);
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Plugin example app'),
        ),
        body: Center(
          child: MaterialButton(
              onPressed: shareToInstagramFeed, child: Text('Pick & share')),
        ),
      ),
    );
  }
}
