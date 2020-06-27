import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutterpluginvideoplayer/controller/video_player_controller.dart';
import 'package:flutterpluginvideoplayer/model/video_player_value.dart';

class VideoPlayer extends StatefulWidget {
  static MethodChannel channel = const MethodChannel('flutterpluginvideoplayer')
    ..invokeMethod<void>('init');

  final VideoPlayerController controller;

  VideoPlayer(this.controller);

  @override
  _VideoPlayerState createState() => new _VideoPlayerState();
}

class _VideoPlayerState extends State<VideoPlayer> {
//  VoidCallback _listener;
//  int _textureId;
//
//  _TencentPlayerState() {
//    _listener = () {
//      final int newTextureId = widget.controller.textureId;
//      if (newTextureId != _textureId) {
//        setState(() {
//          _textureId = newTextureId;
//        });
//      }
//    };
//  }

  @override
  void initState() {
    super.initState();
//    _textureId = widget.controller.textureId;
//    widget.controller.addListener(_listener);

    print("TencentPlayer  initState");
  }

  @override
  void didUpdateWidget(VideoPlayer oldWidget) {
    //print("TencentPlayer  didUpdateWidget");
    super.didUpdateWidget(oldWidget);
//    oldWidget.controller.removeListener(_listener);
//    _textureId = widget.controller.textureId;
//    widget.controller.addListener(_listener);
  }

  @override
  void deactivate() {
    print("TencentPlayer  deactivate");
    super.deactivate();
//    widget.controller.removeListener(_listener);
  }

  @override
  Widget build(BuildContext context) {
    return ValueListenableBuilder(
      valueListenable: widget.controller,
      builder: (BuildContext context, VideoPlayerValue value, Widget child) {
//        return  Texture(textureId: widget.controller.textureId);
        var _textureId = widget.controller.textureId;
        return _textureId == null
            ? Container()
            : Texture(textureId: _textureId);
      },
    );
//    ChangeNotifierProvider
//
//    return _textureId == null ? Container() : Texture(textureId: _textureId);
  }

  @override
  void dispose() {
    widget.controller.dispose();
    super.dispose();
  }
}
