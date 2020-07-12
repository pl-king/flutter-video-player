import 'package:auto_orientation/auto_orientation.dart';
import 'package:flutter/material.dart';
import 'package:flutterpluginvideoplayer/controller/video_player_controller.dart';
import 'package:flutterpluginvideoplayer/view/video_player.dart';

import 'materialcontrols.dart';

class VideoFullPage extends StatefulWidget {
  final VideoPlayerController controller;

  VideoFullPage(this.controller);

  @override
  _VideoFullState createState() => _VideoFullState();
}

class _VideoFullState extends State<VideoFullPage> {
  bool isLand = false;

  @override
  void initState() {
    super.initState();
    AutoOrientation.landscapeAutoMode();
    widget.controller.addListener(listener);
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        body: Container(
          color: Colors.black,
          child: Stack(
            children: <Widget>[
              Center(
                child: Hero(
                  tag: "player",
                  child: AspectRatio(
                    aspectRatio: widget.controller.value.aspectRatio,
                    child: VideoPlayer(widget.controller),
                  ),
                ),
              ),
              Padding(
                padding: EdgeInsets.only(top: 25, right: 20),
                child: IconButton(
                  icon: const BackButtonIcon(),
                  color: Colors.white,
                  onPressed: () {
                    Navigator.pop(context);
                    AutoOrientation.portraitUpMode();
                    widget.controller.toggleScreen();
                  },
                ),
              ),
//              _buildControls(context, widget.controller),
            ],
          ),
        ),
      ),
    );
  }

  @override
  void dispose() {
    super.dispose();
    widget.controller.removeListener(listener);
    AutoOrientation.portraitUpMode();
//    }
  }

  void listener() async {
    print("1111111111111111111111111full");
//    if (widget.controller.isFullScreen && !_isFullScreen) {
//      _isFullScreen = true;
////      await _pushFullScreenWidget(context);
//    } else if (_isFullScreen) {
//      Navigator.of(context, rootNavigator: true).pop();
//      _isFullScreen = false;
//    }
  }

  Widget _buildControls(
    BuildContext context,
    VideoPlayerController controller,
  ) {
    return controller.showControls ? MaterialControls() : Container();
//      controller.showControls
//        ? controller.customControls != null
//            ? controller.customControls
//            : Theme.of(context).platform == TargetPlatform.android
//                ? MaterialControls()
//                :
//    CupertinoControls(
//                    backgroundColor: Color.fromRGBO(41, 41, 41, 0.7),
//                    iconColor: Color.fromARGB(255, 200, 200, 200),
//                  );
    Container();
  }
}
