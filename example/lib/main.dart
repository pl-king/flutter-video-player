import 'dart:ui';

import 'package:auto_orientation/auto_orientation.dart';
import 'package:flutter/material.dart';
import 'dart:async';

import 'package:flutter/services.dart';
import 'package:flutterpluginvideoplayer/flutterpluginvideoplayer.dart';

//import 'package:flutterpluginvideoplayer/controller/download_controller.dart';
import 'package:flutterpluginvideoplayer/controller/video_player_controller.dart';

//import 'package:flutterpluginvideoplayer/flutterplugintencentplayer.dart';
import 'package:flutterpluginvideoplayer/model/player_config.dart';
import 'package:flutterpluginvideoplayer/model/video_player_value.dart';
import 'package:flutterpluginvideoplayer/view/video_player.dart';
import 'package:flutterpluginvideoplayer/view/video_player_full.dart';

//import 'package:path_provider/path_provider.dart';
void main() {
  runApp(MyApp());
}

class MyApp extends StatefulWidget {
  @override
  State<StatefulWidget> createState() {
    // TODO: implement createState
    return _MyAppState();
  }
}

class _MyAppState extends State<MyApp> {
  String _platformVersion = 'Unknown';
  VideoPlayerController _controller;
  VoidCallback listener;

//  String mu =
//      'http://video.mxiaotu.com/sv/45f581bf-170dcfb80a5/45f581bf-170dcfb80a5.mp4';
//  String videoUrlB =
//      'http://video.mxiaotu.com/sv/45f581bf-170dcfb80a5/45f581bf-170dcfb80a5.mp4';
//  String videoUrlG =
//      'http://video.mxiaotu.com/sv/45f581bf-170dcfb80a5/45f581bf-170dcfb80a5.mp4';
//  String spe1 =
//      'http://video.mxiaotu.com/sv/45f581bf-170dcfb80a5/45f581bf-170dcfb80a5.mp4';
//  String spe2 =
//      'http://video.mxiaotu.com/sv/45f581bf-170dcfb80a5/45f581bf-170dcfb80a5.mp4';
  String spe3 =
      'http://1252463788.vod2.myqcloud.com/95576ef5vodtransgzp1252463788/e1ab85305285890781763144364/v.f20.mp4';

  VoidCallback downloadListener;

  Future<void> initPlatformState() async {
    _controller = VideoPlayerController.network(spe3,
        playerConfig: PlayerConfig(autoPlay: false))
      ..initialize().then((_) {
        setState(() {});
      });
  }

  @override
  void initState() {
    super.initState();
    initPlatformState();
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Video Demo',
      home: Scaffold(
        appBar: PreferredSize(
            child: _buildTopApBar(), preferredSize: Size.fromHeight(80)),
        backgroundColor: Colors.white,
        body: Stack(
          children: <Widget>[
            Align(
                alignment: Alignment.topCenter,
                child: Container(
                  height: 200,
                  color: Colors.black,
                  child: _controller.value.initialized
                      ? VideoPlayer(_controller)
                      : Container(
                          alignment: Alignment.center,
                          child: CircularProgressIndicator(),
                        ),
                ))
          ],
        ),
      ),
    );
  }

  _buildTopApBar() {
    var top = MediaQueryData.fromWindow(window).padding.top;
    return Container(
      padding: EdgeInsets.only(top: top),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.spaceBetween,
        children: <Widget>[
          Container(width: 44, child: SizedBox()),
        ],
      ),
    );
  }

  @override
  void dispose() {
    super.dispose();
    _controller.dispose();
  }
}
