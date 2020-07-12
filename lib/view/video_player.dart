import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutterpluginvideoplayer/controller/video_player_controller.dart';
import 'package:flutterpluginvideoplayer/model/video_player_value.dart';
import 'package:flutterpluginvideoplayer/view/player_with_controls.dart';
import 'package:flutterpluginvideoplayer/view/video_player_platform_interface.dart';

import 'materialcontrols.dart';

final VideoPlayerPlatform _videoPlayerPlatform = VideoPlayerPlatform.instance
// This will clear all open videos on the platform when a full restart is
// performed.
  ..init();

typedef Widget ChewieRoutePageBuilder(
    BuildContext context,
    Animation<double> animation,
    Animation<double> secondaryAnimation,
    PlayerControllerProvider controllerProvider);

class VideoPlayer extends StatefulWidget {
  static MethodChannel channel = const MethodChannel('flutterpluginvideoplayer')
    ..invokeMethod<void>('init');

  final VideoPlayerController controller;

  VideoPlayer(this.controller);

  @override
  _VideoPlayerState createState() => new _VideoPlayerState();
}

class _VideoPlayerState extends State<VideoPlayer> {
  bool _isFullScreen = false;

  bool get isFullScreen => _isFullScreen;

  @override
  void initState() {
    super.initState();
    widget.controller.addListener(listener);
  }

  @override
  void didChangeDependencies() {
    super.didChangeDependencies();

    ///全屏后回退到这个页面需要重新设置屏幕方向
//    if (widget.controller != null) if (!widget.controller.isFullScreen)
  }

  @override
  void didUpdateWidget(VideoPlayer oldWidget) {
    if (oldWidget.controller != widget.controller) {
      widget.controller.addListener(listener);
    }
    super.didUpdateWidget(oldWidget);
  }

  @override
  void deactivate() {
    print("TencentPlayer  deactivate");
    super.deactivate();
    widget.controller.removeListener(listener);
  }

  @override
  Widget build(BuildContext context) {
    return PlayerControllerProvider(
      controller: widget.controller,
      child:
      PlayerWithControls(),
//
//          Center(
//        child: Container(
//            width: MediaQuery.of(context).size.width,
//            child: AspectRatio(
//              aspectRatio: widget.controller.value.aspectRatio ??
//                  _calculateAspectRatio(context),
//              child: Stack(
//                children: <Widget>[
//                  ValueListenableBuilder(
//                    valueListenable: widget.controller,
//                    builder: (BuildContext context, VideoPlayerValue value,
//                        Widget child) {
//                      var _textureId = widget.controller.textureId;
//                      return _textureId == null
//                          ? Container()
//                          : _videoPlayerPlatform.buildView(_textureId);
//                    },
//                  ),
//                  _buildControls(context, widget.controller),
//                ],
//              ),
//            )
//
////          AspectRatio(
////            aspectRatio:
////            chewieController.aspectRatio ?? _calculateAspectRatio(context),
////            child: _buildPlayerWithControls(chewieController, context),
////          ),
//            ),
//      ),
    );

//    ChangeNotifierProvider
//
//    return _textureId == null ? Container() : Texture(textureId: _textureId);
  }

  @override
  void dispose() {
    widget.controller.removeListener(listener);
//    widget.controller.dispose();
    super.dispose();
  }

  double _calculateAspectRatio(BuildContext context) {
    final size = MediaQuery.of(context).size;
    final width = size.width;
    final height = size.height;

    return width > height ? width / height : height / width;
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

  void listener() async {
    print("1111111111111111111111111not full");
//    if (widget.controller.isFullScreen && !_isFullScreen) {
//      _isFullScreen = true;
////      await _pushFullScreenWidget(context);
//    } else if (_isFullScreen) {
//      Navigator.of(context, rootNavigator: true).pop();
//      _isFullScreen = false;
//    }
  }

//  Future<dynamic> _pushFullScreenWidget(BuildContext context) async {
//    Navigator.of(context).push(MaterialPageRoute(builder: (context) {
//      return VideoFullPage(widget.controller);
//    }));
//    final isAndroid = Theme.of(context).platform == TargetPlatform.android;
//    final TransitionRoute<Null> route = PageRouteBuilder<Null>(
//      pageBuilder: _fullScreenRoutePageBuilder,
//    );
//
//    SystemChrome.setEnabledSystemUIOverlays([]);
//    if (isAndroid) {
//      SystemChrome.setPreferredOrientations([
//        DeviceOrientation.landscapeLeft,
//        DeviceOrientation.landscapeRight,
//      ]);
//    }
//
//    if (!widget.controller.allowedScreenSleep) {
//      Wakelock.enable();
//    }
//
//    await Navigator.of(context, rootNavigator: true).push(route);
//    _isFullScreen = false;
//    widget.controller.exitFullScreen();
//
//    // The wakelock plugins checks whether it needs to perform an action internally,
//    // so we do not need to check Wakelock.isEnabled.
//    Wakelock.disable();
//
//    SystemChrome.setEnabledSystemUIOverlays(
//        widget.controller.systemOverlaysAfterFullScreen);
//    SystemChrome.setPreferredOrientations(
//        widget.controller.deviceOrientationsAfterFullScreen);
//  }

//  Widget _fullScreenRoutePageBuilder(
//    BuildContext context,
//    Animation<double> animation,
//    Animation<double> secondaryAnimation,
//  ) {
//    var controllerProvider = _ChewieControllerProvider(
//      controller: widget.controller,
//      child: PlayerWithControls(widget.controller),
//    );
//
//    if (widget.controller.routePageBuilder == null) {
//      return _defaultRoutePageBuilder(
//          context, animation, secondaryAnimation, controllerProvider);
//    }
//    return widget.controller.routePageBuilder(
//        context, animation, secondaryAnimation, controllerProvider);
//  }

  AnimatedWidget _defaultRoutePageBuilder(
      BuildContext context,
      Animation<double> animation,
      Animation<double> secondaryAnimation,
      PlayerControllerProvider controllerProvider) {
    return AnimatedBuilder(
      animation: animation,
      builder: (BuildContext context, Widget child) {
        return _buildFullScreenVideo(context, animation, controllerProvider);
      },
    );
  }

  Widget _buildFullScreenVideo(
      BuildContext context,
      Animation<double> animation,
      PlayerControllerProvider controllerProvider) {
    return Scaffold(
      resizeToAvoidBottomPadding: false,
      body: Container(
        alignment: Alignment.center,
        color: Colors.black,
        child: controllerProvider,
      ),
    );
  }
}

class PlayerControllerProvider extends InheritedWidget {
  const PlayerControllerProvider({
    Key key,
    @required this.controller,
    @required Widget child,
  })  : assert(controller != null),
        assert(child != null),
        super(key: key, child: child);

  final VideoPlayerController controller;

  @override
  bool updateShouldNotify(PlayerControllerProvider old) =>
      controller != old.controller;
}
