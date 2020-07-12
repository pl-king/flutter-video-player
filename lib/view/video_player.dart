import 'package:auto_orientation/auto_orientation.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutterpluginvideoplayer/controller/video_player_controller.dart';
import 'package:flutterpluginvideoplayer/view/player_with_controls.dart';
import 'package:wakelock/wakelock.dart';

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
  void didUpdateWidget(VideoPlayer oldWidget) {
    if (oldWidget.controller != widget.controller) {
      widget.controller.addListener(listener);
    }
    super.didUpdateWidget(oldWidget);
  }

  @override
  void deactivate() {
    super.deactivate();
    widget.controller.removeListener(listener);
  }

  @override
  Widget build(BuildContext context) {
    return PlayerControllerProvider(
      controller: widget.controller,
      child: PlayerWithControls(),
    );
  }

  @override
  void dispose() {
    widget.controller.removeListener(listener);
    super.dispose();
  }

  double _calculateAspectRatio(BuildContext context) {
    final size = MediaQuery.of(context).size;
    final width = size.width;
    final height = size.height;

    return width > height ? width / height : height / width;
  }

  void listener() async {
    print(
        "1111111111111111111111111￥controller${widget.controller.isFullScreen}");
    print("1111111111111111111111111￥__isFullScreen${_isFullScreen}");
    if (widget.controller.isFullScreen && !_isFullScreen) {
      print("1111111111111111111111111￥12345");
      _isFullScreen = true;

      print("1111111111111111111111111￥__isFullScreen111${_isFullScreen}");
//
      await _pushFullScreenWidget(context);
    } else if (_isFullScreen && !widget.controller.isFullScreen) {
      print("1111111111111111111111111￥54321");
//      AutoOrientation.landscapeAutoMode();
      Navigator.of(context, rootNavigator: true).pop();
      _isFullScreen = false;
      print("1111111111111111111111111￥__isFullScree222${_isFullScreen}");
    } else {
      print("1111111111111111111111111￥88888");
    }
  }

  Future<dynamic> _pushFullScreenWidget(BuildContext context) async {
    final isAndroid = Theme.of(context).platform == TargetPlatform.android;
    final TransitionRoute<Null> route = PageRouteBuilder<Null>(
      pageBuilder: _fullScreenRoutePageBuilder,
    );

    SystemChrome.setEnabledSystemUIOverlays([]);
    if (isAndroid) {
      SystemChrome.setPreferredOrientations([
        DeviceOrientation.landscapeLeft,
        DeviceOrientation.landscapeRight,
      ]);
    }

    if (!widget.controller.allowedScreenSleep) {
      Wakelock.enable();
    }

    await Navigator.of(context, rootNavigator: true).push(route);
    _isFullScreen = false;
    print("1111111111111111111111111￥pull fll__isFullScreen${_isFullScreen}");
    widget.controller.exitFullScreen();

    // The wakelock plugins checks whether it needs to perform an action internally,
    // so we do not need to check Wakelock.isEnabled.
    Wakelock.disable();

    SystemChrome.setEnabledSystemUIOverlays(
        widget.controller.systemOverlaysAfterFullScreen);
    SystemChrome.setPreferredOrientations(
        widget.controller.deviceOrientationsAfterFullScreen);
    AutoOrientation.portraitUpMode();
  }

  Widget _fullScreenRoutePageBuilder(
    BuildContext context,
    Animation<double> animation,
    Animation<double> secondaryAnimation,
  ) {
    var controllerProvider = PlayerControllerProvider(
      controller: widget.controller,
      child: PlayerWithControls(),
    );

    if (widget.controller.routePageBuilder == null) {
      return _defaultRoutePageBuilder(
          context, animation, secondaryAnimation, controllerProvider);
    }
    return widget.controller.routePageBuilder(
        context, animation, secondaryAnimation, controllerProvider);
  }

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
