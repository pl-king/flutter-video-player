package com.pl.flutterpluginvideoplayer;

import android.net.Uri;
import android.util.Log;
import android.view.Surface;

import com.aliyun.vodplayer.media.AliyunLocalSource;
import com.aliyun.vodplayer.media.AliyunVodPlayer;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.plugin.common.EventChannel;
import io.flutter.view.TextureRegistry;

import com.aliyun.vodplayer.media.IAliyunVodPlayer;
import com.pl.flutterpluginvideoplayer.constants.PlayParameter;
//import com.aliyun.vodplayerview.utils.NetWatchdog;
import java.util.HashMap;
import java.util.Map;

import androidx.collection.LongSparseArray;
import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.plugin.common.EventChannel;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry.Registrar;
import io.flutter.view.TextureRegistry;

public class PlayerManager implements IAliyunVodPlayer.OnPreparedListener, IAliyunVodPlayer.OnErrorListener, IAliyunVodPlayer.OnInfoListener, IAliyunVodPlayer.OnSeekCompleteListener {
    static final String TAG = "Plugin";
    static final String NativeEvent = "flutter_tencentplayer/videoEvents";
    private AliyunVodPlayer mAliyunVodPlayer;
    private Surface surface;
    private final TextureRegistry.SurfaceTextureEntry textureEntry;
    private final FlutterPlugin.FlutterPluginBinding mFlutterPluginBinding;
    private QueuingEventSink eventSink = new QueuingEventSink();

    private final EventChannel eventChannel;
    //是不是在seek中
    private boolean inSeek = false;
    /**
     * 精准seek开启判断逻辑：当视频时长小于5分钟的时候。
     */
    private static final int ACCURATE = 5 * 60 * 1000;


    PlayerManager(FlutterPlugin.FlutterPluginBinding binding, EventChannel eventChannel, TextureRegistry.SurfaceTextureEntry textureEntry, MethodCall call,
                  Result result) {

        this.eventChannel = eventChannel;
        this.textureEntry = textureEntry;
        mFlutterPluginBinding = binding;
        //初始化播放器
        initAliVcPlayer();

        // 注册android向flutter发事件
        setFlutterBridge(eventChannel, textureEntry, result);
//        setPlaySource(call.argument("uri").toString());
    }

    /**
     * 初始化播放器
     */
    private void initAliVcPlayer() {
        mAliyunVodPlayer = new AliyunVodPlayer(mFlutterPluginBinding.getApplicationContext());

        //设置准备回调
        mAliyunVodPlayer.setOnPreparedListener(this);
//            //播放器出错监听
        mAliyunVodPlayer.setOnErrorListener(this);
//            //播放器加载回调
//            mAliyunVodPlayer.setOnLoadingStatusListener(new VideoPlayerLoadingStatusListener(this));

//            //播放结束
//            mAliyunVodPlayer.setOnCompletionListener(new VideoPlayerCompletionListener(this));
//            //播放信息监听
        mAliyunVodPlayer.setOnInfoListener(this);
//            //seek结束事件
        mAliyunVodPlayer.setOnSeekCompleteListener(this);

    }

    private void setFlutterBridge(EventChannel eventChannel, TextureRegistry.SurfaceTextureEntry textureEntry, Result result) {
        // 注册android向flutter发事件
        eventChannel.setStreamHandler(
                new EventChannel.StreamHandler() {
                    @Override
                    public void onListen(Object o, EventChannel.EventSink sink) {
                        eventSink.setDelegate(sink);
                    }

                    @Override
                    public void onCancel(Object o) {
                        eventSink.setDelegate(null);
                    }
                }
        );
        surface = new Surface(textureEntry.surfaceTexture());
        mAliyunVodPlayer.setSurface(surface);
        Map<String, Object> reply = new HashMap<>();
        reply.put("textureId", textureEntry.id());
        result.success(reply);
    }

    public void setPlaySource(String url) {
        Log.e("Plugin", "setPlaySource" + url);
        AliyunLocalSource.AliyunLocalSourceBuilder alsb = new AliyunLocalSource.AliyunLocalSourceBuilder();
        alsb.setSource(url);
        Uri uri = Uri.parse(url);
        if ("rtmp".equals(uri.getScheme())) {
            alsb.setTitle("");
        }
        AliyunLocalSource localSource = alsb.build();
        setLocalSource(localSource);
    }
//    private void setPlaySource(MethodCall call) {
//        if (call.argument("auth") != null) {
//            Map authMap = (Map<String, Object>) call.argument("auth");
//            Log.e(TAG, "appId" + authMap.get("appId").toString());
//            Log.e(TAG, "fileId" + authMap.get("fileId").toString());
//        } else {
//            if (call.argument("asset") != null) {
//                Log.e(TAG, "asset" + call.argument("asset").toString());
//            } else {
//                Log.e(TAG, "url" + call.argument("uri").toString());
//
//                //默认是5000
//                int maxDelayTime = 5000;
//                if (PlayParameter.PLAY_PARAM_URL.startsWith("artp")) {
//                    //如果url的开头是artp，将直播延迟设置成100，
//                    maxDelayTime = 100;
//                }
//                AliyunLocalSource.AliyunLocalSourceBuilder alsb = new AliyunLocalSource.AliyunLocalSourceBuilder();
//                alsb.setSource(call.argument("uri").toString());
//                Uri uri = Uri.parse(call.argument("uri").toString());
//                if ("rtmp".equals(uri.getScheme())) {
//                    alsb.setTitle("");
//                }
//                AliyunLocalSource localSource = alsb.build();
//                setLocalSource(localSource);
//            }
//        }
//    }

    private void setLocalSource(AliyunLocalSource aliyunLocalSource) {
        if (mAliyunVodPlayer == null) {
            return;
        }
//            clearAllSource();
//            stop();
//
//            mAliyunLocalSource = aliyunLocalSource;
//            if (NetWatchdog.is4GConnected(this.context)) {
//
//            }
        mAliyunVodPlayer.prepareAsync(aliyunLocalSource);
    }

    void play() {

        Log.e(TAG, "play()");
        mAliyunVodPlayer.start();
    }

    void pause() {
        mAliyunVodPlayer.pause();

    }

    void seekTo(int position) {
        if (mAliyunVodPlayer == null) {
            return;
        }

        inSeek = true;
        realySeekToFunction(position);
    }

//        void setRate(float rate) {
//            mVodPlayer.setRate(rate);
//        }
//
//        void setBitrateIndex(int index) {
//            mVodPlayer.setBitrateIndex(index);
//        }

    void dispose() {


        if (mAliyunVodPlayer != null) {
            mAliyunVodPlayer.stop();
        }

        textureEntry.release();
        eventChannel.setStreamHandler(null);
        if (surface != null) {
            surface.release();
        }
    }

    private void realySeekToFunction(int position) {
        mAliyunVodPlayer.seekTo(position);
        mAliyunVodPlayer.start();
    }

    /**
     * 获取视频时长
     *
     * @return 视频时长
     */
    public int getDuration() {
        if (mAliyunVodPlayer != null) {
            return (int) mAliyunVodPlayer.getDuration();
        }

        return 0;
    }

    /**
     * 广告视频播放器准备对外接口监听
     */
    @Override
    public void onPrepared() {
        Log.e(TAG, "onPrepared 1" + mAliyunVodPlayer.getVideoHeight());
        Log.e(TAG, "onPrepared 2" + mAliyunVodPlayer.getVideoWidth());
        Log.e(TAG, "onPrepared 3" + mAliyunVodPlayer.getDuration());
        Map<String, Object> preparedMap = new HashMap<>();
        preparedMap.put("event", "initialized");
        preparedMap.put("duration", (int) mAliyunVodPlayer.getDuration());
        preparedMap.put("width", mAliyunVodPlayer.getVideoWidth());
        preparedMap.put("height", mAliyunVodPlayer.getVideoHeight());
        eventSink.success(preparedMap);


    }


    @Override
    public void onSeekComplete() {

    }


    @Override
    public void onError(int i, int i1, String s) {

    }

    @Override
    public void onInfo(int i, int i1) {

    }
}
