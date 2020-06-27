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

//import 'package:path_provider/path_provider.dart';
void main() {
  runApp(MyApp());
}

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  String _platformVersion = 'Unknown';
  VideoPlayerController _controller;
  VoidCallback listener;

//  String videoUrl =
//      'http://5815.liveplay.myqcloud.com/live/5815_89aad37e06ff11e892905cb9018cf0d4.flv';
//  String videoUrlB =
//      'http://5815.liveplay.myqcloud.com/live/5815_89aad37e06ff11e892905cb9018cf0d4_550.flv';
//  String videoUrlG =
//      'http://5815.liveplay.myqcloud.com/live/5815_89aad37e06ff11e892905cb9018cf0d4_900.flv';
  String videoUrlAAA =
      'http://video.mxiaotu.com/sv/45f581bf-170dcfb80a5/45f581bf-170dcfb80a5.mp4';
  String videoUrlBBB =
      'http://video.mxiaotu.com/sv/45f581bf-170dcfb80a5/45f581bf-170dcfb80a5.mp4';

//  String mu =
//      'http://devimages.apple.com.edgekey.net/streaming/examples/bipbop_4x3/gear2/prog_index.m3u8';
//  String spe1 =
//      'http://1252463788.vod2.myqcloud.com/95576ef5vodtransgzp1252463788/e1ab85305285890781763144364/v.f10.mp4';
//  String spe2 =
//      'http://1252463788.vod2.myqcloud.com/95576ef5vodtransgzp1252463788/e1ab85305285890781763144364/v.f20.mp4';
  String videoUrl =
      'http://video.mxiaotu.com/sv/45f581bf-170dcfb80a5/45f581bf-170dcfb80a5.mp4';
  String mu =
      'http://video.mxiaotu.com/sv/45f581bf-170dcfb80a5/45f581bf-170dcfb80a5.mp4';
  String videoUrlB =
      'http://video.mxiaotu.com/sv/45f581bf-170dcfb80a5/45f581bf-170dcfb80a5.mp4';
  String videoUrlG =
      'http://video.mxiaotu.com/sv/45f581bf-170dcfb80a5/45f581bf-170dcfb80a5.mp4';
  String spe1 =
      'http://video.mxiaotu.com/sv/45f581bf-170dcfb80a5/45f581bf-170dcfb80a5.mp4';
  String spe2 =
      'http://video.mxiaotu.com/sv/45f581bf-170dcfb80a5/45f581bf-170dcfb80a5.mp4';
  String spe3 =
      'http://video.mxiaotu.com/sv/45f581bf-170dcfb80a5/45f581bf-170dcfb80a5.mp4';

  String testDownload =
      'http://video.mxiaotu.com/sv/45f581bf-170dcfb80a5/45f581bf-170dcfb80a5.mp4';
  String downloadRes =
      '/storage/emulated/0/tencentdownload/txdownload/2c58873a5b9916f9fef5103c74f0ce5e.m3u8.sqlite';
  String downloadRes2 =
      '/storage/emulated/0/tencentdownload/txdownload/cf3e281653e562303c8c2b14729ba7f5.m3u8.sqlite';

//  DownloadController _downloadController;
  VoidCallback downloadListener;

  @override
  void initState() {
    super.initState();
//    addListener();
    initPlatformState();
  }

  // Platform messages are asynchronous, so we initialize in an async method.
  Future<void> initPlatformState() async {
    //点播
//     _controller = TencentPlayerController.network(null, playerConfig:  PlayerConfig(
//        auth: {"appId": , "fileId": ''}
//    ))
    _controller = VideoPlayerController.network(spe3,
        playerConfig: PlayerConfig(autoPlay: false))

//        _controller = TencentPlayerController.asset('static/tencent1.mp4')
//        _controller = TencentPlayerController.file('/storage/emulated/0/test.mp4')
      ..initialize().then((_) {
        setState(() {});
      });
//    final filepath = await getExternalStorageDirectory();
//    _controller.addListener(listener);
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Video Demo',
      home: ValueListenableBuilder(
        valueListenable: _controller,
        builder: (BuildContext context, VideoPlayerValue value, Widget child) {
          return Scaffold(
            body: Container(
              child: Column(
                children: <Widget>[
                  Container(
                    child: Stack(
                      alignment: AlignmentDirectional.center,
                      children: <Widget>[
//                        _controller.value.initialized ?
                        AspectRatio(
                          aspectRatio: _controller.value.aspectRatio,
//                          aspectRatio: 0.5,
                          child: VideoPlayer(_controller),
                        ),
                        Center(
                          child: _controller.value.isLoading
                              ? CircularProgressIndicator()
                              : SizedBox(),
                        ),
                      ],
                    ),
                  ),
                  Expanded(
                    child: ListView(
                      children: <Widget>[
                        Text("aspectRatio${_controller.value.aspectRatio}"),
                        Text(
                          "播放网速：" + _controller.value.netSpeed.toString(),
                          style: TextStyle(color: Colors.pink),
                        ),
                        Text(
                          "错误：" + _controller.value.errorDescription.toString(),
                          style: TextStyle(color: Colors.pink),
                        ),
                        Text(
                          "播放进度：" + _controller.value.position.toString(),
                          style: TextStyle(color: Colors.pink),
                        ),
                        Text(
                          "缓冲进度：" + _controller.value.playable.toString(),
                          style: TextStyle(color: Colors.pink),
                        ),
                        Text(
                          "总时长：" + _controller.value.duration.toString(),
                          style: TextStyle(color: Colors.pink),
                        ),
                        FlatButton(
                            onPressed: () {
                              _controller.seekTo(Duration(seconds: 5));
                            },
                            child: Text(
                              'seekTo 00:00:05',
                              style: TextStyle(color: Colors.blue),
                            )),
                        Row(
                          children: <Widget>[
                            FlatButton(
                                onPressed: () {
                                  _controller.setRate(1.0);
                                },
                                child: Text(
                                  'setRate 1.0',
                                  style: TextStyle(
                                      color: _controller.value.rate == 1.0
                                          ? Colors.red
                                          : Colors.blue),
                                )),
                            FlatButton(
                                onPressed: () {
                                  _controller.setRate(1.5);
                                },
                                child: Text(
                                  'setRate 1.5',
                                  style: TextStyle(
                                      color: _controller.value.rate == 1.5
                                          ? Colors.red
                                          : Colors.blue),
                                )),
                            FlatButton(
                                onPressed: () {
                                  _controller.setRate(2.0);
                                },
                                child: Text(
                                  'setRate 2.0',
                                  style: TextStyle(
                                      color: _controller.value.rate == 2.0
                                          ? Colors.red
                                          : Colors.blue),
                                )),
                          ],
                        ),
                        Row(
                          children: <Widget>[
                            FlatButton(
                                onPressed: () {
                                  _controller =
                                      VideoPlayerController.network(mu);
                                  _controller.initialize().then((_) {
                                    setState(() {});
                                  });
                                  //  _controller.addListener(listener);
                                },
                                child: Text(
                                  'm3u8点播',
                                  style: TextStyle(
                                      color:
                                          _controller.dataSource == videoUrlAAA
                                              ? Colors.red
                                              : Colors.blue),
                                )),
                            FlatButton(
                              onPressed: () {
                                _controller =
                                    VideoPlayerController.network(spe1);
                                _controller.initialize().then((_) {
                                  setState(() {});
                                });
                                // _controller.addListener(listener);
                              },
                              child: Text(
                                '普通点播',
                                style: TextStyle(
                                    color: _controller.dataSource == videoUrlBBB
                                        ? Colors.red
                                        : Colors.blue),
                              ),
                            ),
                          ],
                        ),
                        Row(
                          children: <Widget>[
                            Container(
                              margin: EdgeInsets.only(left: 15),
                              child: Text(
                                'm3u8点播 : ',
                                style: TextStyle(color: Colors.orange),
                              ),
                            ),
                            FlatButton(
                              child: Text(
                                '标',
                                style: TextStyle(
                                    color: _controller.value.bitrateIndex == 0
                                        ? Colors.yellow
                                        : Colors.green),
                              ),
                              onPressed: () {
                                _controller.setBitrateIndex(0);
                              },
                            ),
                            FlatButton(
                              child: Text(
                                '高',
                                style: TextStyle(
                                    color: _controller.value.bitrateIndex == 1
                                        ? Colors.yellow
                                        : Colors.green),
                              ),
                              onPressed: () {
                                _controller.setBitrateIndex(1);
                              },
                            ),
                            FlatButton(
                              child: Text(
                                '超',
                                style: TextStyle(
                                    color: _controller.value.bitrateIndex == 2
                                        ? Colors.yellow
                                        : Colors.green),
                              ),
                              onPressed: () {
                                _controller.setBitrateIndex(2);
                              },
                            ),
                          ],
                        ),
                        Row(
                          children: <Widget>[
                            Container(
                              margin: EdgeInsets.only(left: 15),
                              child: Text(
                                '普通点播 : ',
                                style: TextStyle(color: Colors.orange),
                              ),
                            ),
                            FlatButton(
                                onPressed: () {
                                  _controller = VideoPlayerController.network(
                                      spe1,
                                      playerConfig: PlayerConfig(
                                          startTime: _controller
                                              .value.position.inSeconds));
                                  _controller.initialize().then((_) {
                                    setState(() {});
                                  });
                                  //   _controller.addListener(listener);
                                },
                                child: Text(
                                  '标',
                                  style: TextStyle(
                                      color: _controller.dataSource == videoUrlB
                                          ? Colors.red
                                          : Colors.blue),
                                )),
                            FlatButton(
                                onPressed: () {
                                  _controller = VideoPlayerController.network(
                                      spe2,
                                      playerConfig: PlayerConfig(
                                          startTime: _controller
                                              .value.position.inSeconds));
                                  _controller.initialize().then((_) {
                                    setState(() {});
                                  });
                                  //    _controller.addListener(listener);
                                },
                                child: Text(
                                  '高',
                                  style: TextStyle(
                                      color: _controller.dataSource == videoUrlG
                                          ? Colors.red
                                          : Colors.blue),
                                )),
                            FlatButton(
                              onPressed: () {
                                _controller = VideoPlayerController.network(
                                    spe3,
                                    playerConfig: PlayerConfig(
                                        startTime: _controller
                                            .value.position.inSeconds));
                                _controller.initialize().then((_) {
                                  setState(() {});
                                });
                                //  _controller.addListener(listener);
                              },
                              child: Text(
                                '超',
                                style: TextStyle(
                                    color: _controller.dataSource == videoUrl
                                        ? Colors.red
                                        : Colors.blue),
                              ),
                            ),
                          ],
                        ),
                      ],
                    ),
                  )
                ],
              ),
            ),
            floatingActionButton: FloatingActionButton(
              onPressed: () {
                setState(() {
                  print("11111111111111111111111111111111111111111");
                  _controller.value.isPlaying
                      ? _controller.pause()
                      : _controller.play();
                });
              },
              child: Icon(
                _controller.value.isPlaying ? Icons.pause : Icons.play_arrow,
              ),
            ),
          );
        },
      ),
    );
  }
}
