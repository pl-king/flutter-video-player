import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:flutterpluginvideoplayer/controller/video_player_controller.dart';
import 'package:flutterpluginvideoplayer/model/video_player_value.dart';
import 'package:flutterpluginvideoplayer/view/video_player.dart';
import 'package:flutterpluginvideoplayer/view/video_player_platform_interface.dart';
import 'cupertino_controls.dart';
import 'materialcontrols.dart';

final VideoPlayerPlatform _videoPlayerPlatform = VideoPlayerPlatform.instance
// This will clear all open videos on the platform when a full restart is
// performed.
  ..init();

class PlayerWithControls extends StatelessWidget {
  PlayerWithControls({Key key}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    final VideoPlayerController controller = VideoPlayerController.of(context);

    return Center(
      child: Container(
          width: MediaQuery.of(context).size.width,
          child: AspectRatio(
            aspectRatio:
                controller.value.aspectRatio ?? _calculateAspectRatio(context),
            child: Stack(
              children: <Widget>[
                ValueListenableBuilder(
                  valueListenable: controller,
                  builder: (BuildContext context, VideoPlayerValue value,
                      Widget child) {
                    var _textureId = controller.textureId;
                    return _textureId == null
                        ? Container()
                        : _videoPlayerPlatform.buildView(_textureId);
                  },
                ),
                _buildControls(context, controller),
              ],
            ),
          )

//          AspectRatio(
//            aspectRatio:
//            chewieController.aspectRatio ?? _calculateAspectRatio(context),
//            child: _buildPlayerWithControls(chewieController, context),
//          ),
          ),
    );

//      Center(
//      child: Hero(
//        tag: "player",
//        child: AspectRatio(
//          aspectRatio: controller.value.aspectRatio,
////          child: VideoPlayer(controller),
//        ),
//      ),
//    );
    return Center(
      child: Container(
        width: MediaQuery.of(context).size.width,
        child: AspectRatio(
          aspectRatio:
              controller.value.aspectRatio ?? _calculateAspectRatio(context),
          child: _buildPlayerWithControls(controller, context),
        ),
      ),
    );
  }

  Container _buildPlayerWithControls(
      VideoPlayerController controller, BuildContext context) {
    return Container(
      child: Stack(
        children: <Widget>[
          controller.placeholder ?? Container(),
          Center(
            child: AspectRatio(
              aspectRatio: controller.value.aspectRatio ??
                  _calculateAspectRatio(context),
              child: VideoPlayer(controller),
            ),
          ),
          controller.overlay ?? Container(),
          _buildControls(context, controller),
        ],
      ),
    );
  }

  Widget _buildControls(
    BuildContext context,
    VideoPlayerController controller,
  ) {
    return controller.showControls
        ? controller.customControls != null
            ? controller.customControls
            : Theme.of(context).platform == TargetPlatform.android
//                ? MaterialControls()
                ? CupertinoControls(
                    backgroundColor: Color.fromRGBO(41, 41, 41, 0.7),
                    iconColor: Color.fromARGB(255, 200, 200, 200),
                  )
                : CupertinoControls(
                    backgroundColor: Color.fromRGBO(41, 41, 41, 0.7),
                    iconColor: Color.fromARGB(255, 200, 200, 200),
                  )
        : Container();
  }

  double _calculateAspectRatio(BuildContext context) {
    final size = MediaQuery.of(context).size;
    final width = size.width;
    final height = size.height;

    return width > height ? width / height : height / width;
  }
}
