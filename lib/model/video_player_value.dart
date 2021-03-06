import 'package:flutter/material.dart';
import 'package:flutterpluginvideoplayer/view/video_player_platform_interface.dart';

///
///Copyright (C) 2019 MIXIAOTU.COM Inc. All rights reserved.
///This is source code from kexuemihe project.
///The distribution of any copyright must be permitted mixiaotu Company.
///说明:
///日期: Created by Dell  on 2020/6/27
///作者: Dell
///更新版本          日期            作者             备注
///v0001                  2020/6/27           Dell               创建
///

class VideoPlayerValue {
  final List<DurationRange> buffered;
  final Duration duration;
  final Duration position;
  final Duration playable;
  final bool isPlaying;
  final String errorDescription;
  final Size size;
  final bool isLoading;
  final int netSpeed;
  final double rate;
  final int bitrateIndex;
  final double volume;
  final bool isBuffering;

  bool get initialized => duration.inMilliseconds != 0;

  bool get hasError => errorDescription != null;

  double get aspectRatio => size != null
      ? size.width / size.height > 0.0 ? size.width / size.height : 1.0
      : 1.0;

  VideoPlayerValue({
    this.volume = 1.0,
    this.isBuffering = false,
    this.duration = const Duration(),
    this.position = const Duration(),
    this.playable = const Duration(),
    this.isPlaying = false,
    this.errorDescription,
    this.size,
    this.isLoading = false,
    this.netSpeed,
    this.rate = 1.0,
    this.bitrateIndex = 0,
    this.buffered = const <DurationRange>[],
  });

  VideoPlayerValue copyWith({
    List<DurationRange> buffered,
    Duration duration,
    Duration position,
    Duration playable,
    bool isPlaying,
    String errorDescription,
    Size size,
    bool isLoading,
    int netSpeed,
    double rate,
    int bitrateIndex,
  }) {
    return VideoPlayerValue(
      buffered: buffered ?? this.buffered,
      duration: duration ?? this.duration,
      position: position ?? this.position,
      playable: playable ?? this.playable,
      isPlaying: isPlaying ?? this.isPlaying,
      errorDescription: errorDescription ?? this.errorDescription,
      size: size ?? this.size,
      isLoading: isLoading ?? this.isLoading,
      netSpeed: netSpeed ?? this.netSpeed,
      rate: rate ?? this.rate,
      bitrateIndex: bitrateIndex ?? this.bitrateIndex,
    );
  }

  @override
  String toString() {
    return '$runtimeType('
        'duration: $duration, '
        'position: $position, '
        'playable: $playable, '
        'isPlaying: $isPlaying, '
        'errorDescription: $errorDescription),'
        'isLoading: $isLoading),'
        'netSpeed: $netSpeed),'
        'rate: $rate),'
        'bitrateIndex: $bitrateIndex),'
        'size: $size)';
  }
}

enum DataSourceType { asset, network, file }
