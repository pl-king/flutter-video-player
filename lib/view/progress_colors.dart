import 'package:flutter/rendering.dart';

///
///Copyright (C) 2019 MIXIAOTU.COM Inc. All rights reserved.
///This is source code from kexuemihe project.
///The distribution of any copyright must be permitted mixiaotu Company.
///说明: 
///日期: Created by Dell  on 2020/7/4  
///作者: Dell 
///更新版本          日期            作者             备注
///v0001                  2020/7/4           Dell               创建
///
class ChewieProgressColors {
  ChewieProgressColors({
    Color playedColor: const Color.fromRGBO(255, 0, 0, 0.7),
    Color bufferedColor: const Color.fromRGBO(30, 30, 200, 0.2),
    Color handleColor: const Color.fromRGBO(200, 200, 200, 1.0),
    Color backgroundColor: const Color.fromRGBO(200, 200, 200, 0.5),
  })  : playedPaint = Paint()..color = playedColor,
        bufferedPaint = Paint()..color = bufferedColor,
        handlePaint = Paint()..color = handleColor,
        backgroundPaint = Paint()..color = backgroundColor;

  final Paint playedPaint;
  final Paint bufferedPaint;
  final Paint handlePaint;
  final Paint backgroundPaint;
}
