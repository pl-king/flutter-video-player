import 'dart:async';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutterpluginvideoplayer/model/video_player_value.dart';
import 'package:flutterpluginvideoplayer/model/player_config.dart';
import 'package:flutterpluginvideoplayer/view/video_player.dart';

///Copyright (C) 2019 MIXIAOTU.COM Inc. All rights reserved.
///This is source code from kexuemihe project.
///The distribution of any copyright must be permitted mixiaotu Company.
///说明:
///日期: Created by Dell  on 2020/6/27
///作者: Dell
///更新版本          日期            作者             备注
///v0001                  2020/6/27           Dell               创建
///
class VideoPlayerController extends ValueNotifier<VideoPlayerValue> {
  ///  是否显示控件
  final bool showControls;
  final Widget placeholder;
  final Widget overlay;

  ///全屏显示
  bool _isFullScreen = false;

  bool get isFullScreen => _isFullScreen;

  ///控制休眠保持屏幕常亮
  final bool allowedScreenSleep;
//  final double aspectRatio;

  /// Defines the system overlays visible after exiting fullscreen
  final List<SystemUiOverlay> systemOverlaysAfterFullScreen;

  /// Defines the set of allowed device orientations after exiting fullscreen
  final List<DeviceOrientation> deviceOrientationsAfterFullScreen;
  final ChewieRoutePageBuilder routePageBuilder;
  int _textureId;
  final String dataSource;
  final DataSourceType dataSourceType;
  final PlayerConfig playerConfig;
  MethodChannel channel = VideoPlayer.channel;
  bool _isDisposed = false;

  StreamSubscription<dynamic> _eventSubscription;
  _VideoAppLifeCycleObserver _lifeCycleObserver;

  @visibleForTesting
  int get textureId => _textureId;

  VideoPlayerController.asset(this.dataSource,
      {this.showControls = true,
      this.allowedScreenSleep = true,
      this.systemOverlaysAfterFullScreen = SystemUiOverlay.values,
      this.deviceOrientationsAfterFullScreen = const [
        DeviceOrientation.portraitUp,
        DeviceOrientation.portraitDown,
        DeviceOrientation.landscapeLeft,
        DeviceOrientation.landscapeRight,
      ],
      this.routePageBuilder = null,
//      this.aspectRatio,
      this.placeholder,
      this.overlay,
      this.customControls,
      this.playerConfig = const PlayerConfig()})
      : dataSourceType = DataSourceType.asset,
        super(VideoPlayerValue());

  VideoPlayerController.network(this.dataSource,
      {this.showControls = true,
      this.allowedScreenSleep = true,
      this.systemOverlaysAfterFullScreen = SystemUiOverlay.values,
      this.deviceOrientationsAfterFullScreen = const [
        DeviceOrientation.portraitUp,
        DeviceOrientation.portraitDown,
        DeviceOrientation.landscapeLeft,
        DeviceOrientation.landscapeRight,
      ],
      this.routePageBuilder = null,
//      this.aspectRatio,
      this.placeholder,
      this.overlay,
      this.customControls,
      this.playerConfig = const PlayerConfig()})
      : dataSourceType = DataSourceType.network,
        super(VideoPlayerValue());

  VideoPlayerController.file(String filePath,
      {this.showControls = true,
      this.allowedScreenSleep = true,
      this.systemOverlaysAfterFullScreen = SystemUiOverlay.values,
      this.deviceOrientationsAfterFullScreen = const [
        DeviceOrientation.portraitUp,
        DeviceOrientation.portraitDown,
        DeviceOrientation.landscapeLeft,
        DeviceOrientation.landscapeRight,
      ],
      this.routePageBuilder = null,
//      this.aspectRatio,
      this.placeholder,
      this.overlay,
      this.customControls,
      this.playerConfig = const PlayerConfig()})
      : dataSource = filePath,
        dataSourceType = DataSourceType.file,
        super(VideoPlayerValue());

  final Widget customControls;

  ///初始化播放器的方法
  Future<void> initialize() async {
    _lifeCycleObserver = _VideoAppLifeCycleObserver(this);
    _lifeCycleObserver.initialize();
    Map<dynamic, dynamic> dataSourceDescription;
    switch (dataSourceType) {
      case DataSourceType.asset:
        dataSourceDescription = <String, dynamic>{'asset': dataSource};
        break;
      case DataSourceType.network:
      case DataSourceType.file:
        dataSourceDescription = <String, dynamic>{'uri': dataSource};
        break;
    }

    value = value.copyWith(isPlaying: playerConfig.autoPlay);
    dataSourceDescription.addAll(playerConfig.toJson());

    final Map<String, dynamic> response =
        await channel.invokeMapMethod<String, dynamic>(
      'create',
      dataSourceDescription,
    );

    _textureId = response['textureId'];

    ///设置监听naive 返回的的数据
    _eventSubscription = _eventChannelFor(_textureId)
        .receiveBroadcastStream()
        .listen(eventListener);
  }

  ///注册监听native的方法
  EventChannel _eventChannelFor(int textureId) {
    return EventChannel('flutter_tencentplayer/videoEvents$textureId');
  }

  ///native 传递到flutter 进行数据处理
  void eventListener(dynamic event) {
    if (_isDisposed) {
      return;
    }
    final Map<dynamic, dynamic> map = event;
    switch (map['event']) {
      case 'initialized':
        value = value.copyWith(
          duration: Duration(milliseconds: map['duration']),
          size: Size(map['width']?.toDouble() ?? 0.0,
              map['height']?.toDouble() ?? 0.0),
        );
        break;
      case 'progress':
        value = value.copyWith(
          position: Duration(milliseconds: map['progress']),
          duration: Duration(milliseconds: map['duration']),
          playable: Duration(milliseconds: map['playable']),
        );
        break;
      case 'loading':
        value = value.copyWith(isLoading: true);
        break;
      case 'loadingend':
        value = value.copyWith(isLoading: false);
        break;
      case 'playend':
        value = value.copyWith(isPlaying: false, position: value.duration);
        break;
      case 'netStatus':
        value = value.copyWith(netSpeed: map['netSpeed']);
        break;
      case 'error':
        value = value.copyWith(errorDescription: map['errorInfo']);
        break;
    }
  }

  @override
  Future dispose() async {
    if (!_isDisposed) {
      _isDisposed = true;
      await _eventSubscription?.cancel();
      await channel.invokeListMethod(
          'dispose', <String, dynamic>{'textureId': _textureId});
      _lifeCycleObserver.dispose();
    }
    super.dispose();
  }

  Future<void> play() async {
    value = value.copyWith(isPlaying: true);
    await _applyPlayPause();
  }

  Future<void> pause() async {
    value = value.copyWith(isPlaying: false);
    await _applyPlayPause();
  }

  Future<void> _applyPlayPause() async {
    if (!value.initialized || _isDisposed) {
      print('init fail${!value.initialized}');
      print('init fail${_isDisposed}');

      return;
    }
    if (value.isPlaying) {
      await channel
          .invokeMethod('play', <String, dynamic>{'textureId': _textureId});
    } else {
      await channel
          .invokeMethod('pause', <String, dynamic>{'textureId': _textureId});
    }
  }

  Future<void> seekTo(Duration moment) async {
    if (_isDisposed) {
      return;
    }
    if (moment == null) {
      return;
    }
    if (moment > value.duration) {
      moment = value.duration;
    } else if (moment < const Duration()) {
      moment = const Duration();
    }
    await channel.invokeMethod('seekTo', <String, dynamic>{
      'textureId': _textureId,
      'location': moment.inSeconds,
    });
    value = value.copyWith(position: moment);
  }

  ///点播为m3u8子流，会自动无缝seek
  Future<void> setBitrateIndex(int index) async {
    if (_isDisposed) {
      return;
    }
    await channel.invokeMethod('setBitrateIndex', <String, dynamic>{
      'textureId': _textureId,
      'index': index,
    });
    value = value.copyWith(bitrateIndex: index);
  }

  setLooping(bool looping) {}

  setVolume(double volume) {}

  ///改变屏幕方向
  void toggleScreen() {
    _isFullScreen = !_isFullScreen;
//    notifyListeners();
//    print("1111111111111111111111111111111toggleScreen");
  }

  ///进入全屏
  void enterFullScreen() {
    _isFullScreen = true;
    notifyListeners();
    print("1111111111111111111111111111111enterFullScreen");
  }

  ///退出全屏
  void exitFullScreen() {
    _isFullScreen = false;
    notifyListeners();
    print("1111111111111111111111111111111exitFullScreen");
  }

  Future<void> setRate(double rate) async {
    if (_isDisposed) {
      return;
    }
    if (rate > 2.0) {
      rate = 2.0;
    } else if (rate < 1.0) {
      rate = 1.0;
    }
    await channel.invokeMethod('setRate', <String, dynamic>{
      'textureId': _textureId,
      'rate': rate,
    });
    value = value.copyWith(rate: rate);
  }
}

///视频组件生命周期监听
class _VideoAppLifeCycleObserver with WidgetsBindingObserver {
  bool _wasPlayingBeforePause = false;
  final VideoPlayerController _controller;

  _VideoAppLifeCycleObserver(this._controller);

  void initialize() {
    WidgetsBinding.instance.addObserver(this);
  }

  @override
  void didChangeAppLifecycleState(AppLifecycleState state) {
    switch (state) {

      ///组件进入暂停状态
      case AppLifecycleState.paused:
        _wasPlayingBeforePause = _controller.value.isPlaying;
        _controller.pause();
        break;

      ///组件进入活跃状态
      case AppLifecycleState.resumed:
        if (_wasPlayingBeforePause) {
          _controller.play();
        }
        break;
      default:
    }
  }

  void dispose() {
    WidgetsBinding.instance.removeObserver(this);
  }
}
