//import 'package:flutter/foundation.dart';
//import 'package:flutter/material.dart';
//import 'package:flutterpluginvideoplayer/controller/video_player_controller.dart';
//import 'package:flutterpluginvideoplayer/view/video_player.dart';
//import 'materialcontrols.dart';
//
//class PlayerWithControls extends StatelessWidget {
//  VideoPlayerController controller;
//
//  PlayerWithControls(this.controller, {Key key}) : super(key: key);
//
//  @override
//  Widget build(BuildContext context) {
//    return Center(
//      child: Hero(
//        tag: "player",
//        child: AspectRatio(
//          aspectRatio: controller.value.aspectRatio,
//          child: VideoPlayer(controller),
//        ),
//      ),
//    );
//    return Center(
//      child: Container(
//        width: MediaQuery.of(context).size.width,
//        child: AspectRatio(
//          aspectRatio:
//              controller.value.aspectRatio ?? _calculateAspectRatio(context),
//          child: _buildPlayerWithControls(controller, context),
//        ),
//      ),
//    );
//  }
//
//  Container _buildPlayerWithControls(
//      VideoPlayerController controller, BuildContext context) {
//    return Container(
//      child: Stack(
//        children: <Widget>[
//          controller.placeholder ?? Container(),
//          Center(
//            child: AspectRatio(
//              aspectRatio: controller.value.aspectRatio ??
//                  _calculateAspectRatio(context),
//              child: VideoPlayer(controller),
//            ),
//          ),
//          controller.overlay ?? Container(),
//          _buildControls(context, controller),
//        ],
//      ),
//    );
//  }
//
//  Widget _buildControls(
//    BuildContext context,
//    VideoPlayerController chewieController,
//  ) {
//    return chewieController.showControls
//        ? chewieController.customControls != null
//            ? chewieController.customControls
//            : Theme.of(context).platform == TargetPlatform.android
//                ? MaterialControls(chewieController)
//                : MaterialControls(chewieController)
////    CupertinoControls(
////      backgroundColor: Color.fromRGBO(41, 41, 41, 0.7),
////      iconColor: Color.fromARGB(255, 200, 200, 200),
////    )
//        : Container();
//  }
//
//  double _calculateAspectRatio(BuildContext context) {
//    final size = MediaQuery.of(context).size;
//    final width = size.width;
//    final height = size.height;
//
//    return width > height ? width / height : height / width;
//  }
//}
