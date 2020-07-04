package com.pl.flutterpluginvideoplayer;


import android.util.Base64;
import android.util.Log;
import android.view.Surface;

import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Map;

import androidx.annotation.NonNull;

import com.aliyun.player.AliPlayer;
import com.aliyun.player.AliPlayerFactory;
import com.aliyun.player.IPlayer;
import com.aliyun.player.bean.ErrorInfo;
import com.aliyun.player.bean.InfoBean;
import com.aliyun.player.nativeclass.CacheConfig;
import com.aliyun.player.nativeclass.MediaInfo;
import com.aliyun.player.nativeclass.PlayerConfig;
import com.aliyun.player.nativeclass.TrackInfo;
import com.aliyun.player.source.UrlSource;
import com.pl.flutterpluginvideoplayer.constants.PlayParameter;

import androidx.collection.LongSparseArray;
import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.plugin.common.EventChannel;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry.Registrar;
import io.flutter.view.TextureRegistry;

import static com.aliyun.player.bean.InfoCode.BufferedPosition;
import static com.aliyun.player.bean.InfoCode.CurrentPosition;


import static com.pl.flutterpluginvideoplayer.FlutterpluginvideoplayerPlugin.TAG;

public class PlayerManager implements IPlayer.OnPreparedListener, IPlayer.OnErrorListener, IPlayer.OnStateChangedListener, IPlayer.OnInfoListener, IPlayer.OnRenderingStartListener, IPlayer.OnTrackChangedListener, IPlayer.OnSeekCompleteListener, IPlayer.OnSeiDataListener, IPlayer.OnCompletionListener {
    //播放器
    private AliPlayer mAliyunVodPlayer;
    private Surface surface;
    private final TextureRegistry.SurfaceTextureEntry textureEntry;
    private final FlutterPlugin.FlutterPluginBinding mFlutterPluginBinding;
    private QueuingEventSink eventSink = new QueuingEventSink();
    //当前播放器的状态 默认为idle状态
    private int mPlayerState = IPlayer.idle;
    private final EventChannel eventChannel;
    //是不是在seek中
    private boolean inSeek = false;
    /**
     * 精准seek开启判断逻辑：当视频时长小于5分钟的时候。
     */
    private static final int ACCURATE = 5 * 60 * 1000;

    /**
     * 是否处于播放状态：start或者pause了
     *
     * @return 是否处于播放状态
     */
    public boolean isPlaying() {
        return mPlayerState == IPlayer.started;
    }

    PlayerManager(FlutterPlugin.FlutterPluginBinding binding, EventChannel eventChannel, TextureRegistry.SurfaceTextureEntry textureEntry, MethodCall call,
                  Result result) {

        this.eventChannel = eventChannel;
        this.textureEntry = textureEntry;
        mFlutterPluginBinding = binding;
        //初始化播放器
        initAliVcPlayer();

        setCheconfig();
        // 注册android向flutter发事件
        setFlutterBridge(eventChannel, textureEntry, result);
        setPlaySource(call);
    }

    private void setCheconfig() {
        CacheConfig cacheConfig = new CacheConfig();
        //开启缓存功能
        cacheConfig.mEnable = true;
        //能够缓存的单个文件最大时长。超过此长度则不缓存
        cacheConfig.mMaxDurationS = 1000;
        //缓存目录的位置
        String cacheDir = mFlutterPluginBinding.getApplicationContext().getCacheDir().getAbsoluteFile().getPath();
        //        String fileName = Base64.encodeToString(assetLookupKey.getBytes(), Base64.DEFAULT);

        Log.e(TAG, "cacheDir:" + cacheDir);
        cacheConfig.mDir = cacheDir;
        //缓存目录的最大大小。超过此大小，将会删除最旧的缓存文件
        cacheConfig.mMaxSizeMB = 200;
        //设置缓存配置给到播放器
        mAliyunVodPlayer.setCacheConfig(cacheConfig);
    }


    /**
     * 初始化播放器
     */
    private void initAliVcPlayer() {
        mAliyunVodPlayer = AliPlayerFactory.createAliPlayer(mFlutterPluginBinding.getApplicationContext());
        mAliyunVodPlayer.enableLog(true);
        //设置准备回调
        mAliyunVodPlayer.setOnPreparedListener(this);
//            //播放器出错监听
        mAliyunVodPlayer.setOnErrorListener(this);
//            //播放器加载回调
//            mAliyunVodPlayer.setOnLoadingStatusListener(new VideoPlayerLoadingStatusListener(this));
//            //播放器状态
        mAliyunVodPlayer.setOnStateChangedListener(this);
//            //播放结束
        mAliyunVodPlayer.setOnCompletionListener(this);
//            //播放信息监听
        mAliyunVodPlayer.setOnInfoListener(this);
//            //第一帧显示
        mAliyunVodPlayer.setOnRenderingStartListener(this);
//            //trackChange监听
        mAliyunVodPlayer.setOnTrackChangedListener(this);
//            //seek结束事件
        mAliyunVodPlayer.setOnSeekCompleteListener(this);
        mAliyunVodPlayer.setOnSeiDataListener(this);
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

    private void setPlaySource(MethodCall call) {
        if (call.argument("auth") != null) {
            Map authMap = (Map<String, Object>) call.argument("auth");
            Log.e(TAG, "appId" + authMap.get("appId").toString());
            Log.e(TAG, "fileId" + authMap.get("fileId").toString());
        } else {
            if (call.argument("asset") != null) {
                Log.e(TAG, "asset" + call.argument("asset").toString());
            } else {
                Log.e(TAG, "url" + call.argument("uri").toString());
                UrlSource urlSource = new UrlSource();
                urlSource.setUri(call.argument("uri").toString());
                //默认是5000
                int maxDelayTime = 5000;
                if (PlayParameter.PLAY_PARAM_URL.startsWith("artp")) {
                    //如果url的开头是artp，将直播延迟设置成100，
                    maxDelayTime = 100;
                }

                mAliyunVodPlayer.enableHardwareDecoder(true);
                PlayerConfig playerConfig = mAliyunVodPlayer.getConfig();
                playerConfig.mMaxDelayTime = maxDelayTime;
//                    playerConfig.
                playerConfig.mClearFrameWhenStop = true;
                //开启SEI事件通知
                playerConfig.mEnableSEI = true;
                mAliyunVodPlayer.setConfig(playerConfig);
//                    mAliyunVodPlayer.setAutoPlay(true);
                mAliyunVodPlayer.setDataSource(urlSource);
                mAliyunVodPlayer.prepare();

            }
        }
    }

    void play() {
        if (mPlayerState == IPlayer.paused || mPlayerState == IPlayer.prepared) {


        }
        Log.e(TAG, "play()");
        mAliyunVodPlayer.start();
    }

    void pause() {
        mAliyunVodPlayer.pause();
        if (mPlayerState == IPlayer.started || mPlayerState == IPlayer.prepared) {

        }
    }

    public void setSpeed(float v) {
        mAliyunVodPlayer.setSpeed(v);
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
        Boolean hasLoadedEnd = null;
        MediaInfo mediaInfo = null;
//            if (mAliyunVodPlayer != null && hasLoadEnd != null) {
//                mediaInfo = mAliyunVodPlayer.getMediaInfo();
//                hasLoadedEnd = hasLoadEnd.get(mediaInfo);
//            }

        if (mAliyunVodPlayer != null) {
            mAliyunVodPlayer.stop();
        }
//            if (hasLoadEnd != null) {
//                hasLoadEnd.remove(mediaInfo);
//            }
        textureEntry.release();
        eventChannel.setStreamHandler(null);
        if (surface != null) {
            surface.release();
        }
    }

    private void realySeekToFunction(int position) {
        isAutoAccurate(position);
        mAliyunVodPlayer.start();
    }

    /**
     * 判断是否开启精准seek
     */
    private void isAutoAccurate(int position) {
        if (getDuration() <= ACCURATE) {
            mAliyunVodPlayer.seekTo(position, IPlayer.SeekMode.Accurate);
        } else {
            mAliyunVodPlayer.seekTo(position, IPlayer.SeekMode.Inaccurate);
        }
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

        resetSurfaceTextureSize();
    }

    void resetSurfaceTextureSize() {
        int width = mAliyunVodPlayer.getVideoWidth();
        int height = mAliyunVodPlayer.getVideoHeight();
        textureEntry.surfaceTexture().setDefaultBufferSize(width, height);
    }

    @Override
    public void onError(ErrorInfo errorInfo) {
        Log.e(TAG, "onError" + errorInfo.getMsg());
    }

    @Override
    public void onStateChanged(int i) {
        Log.e(TAG, "onStateChanged" + i);
    }

    long bufferedPosition = 0;
    long currentPosition = 0;

    @Override
    public void onInfo(InfoBean infoBean) {
        Log.e(TAG, "onInfo" + infoBean.getExtraValue() + "---------" + infoBean.getCode());
        Log.e(TAG, "onInfo" + infoBean.getExtraMsg());

        if (infoBean.getCode() == BufferedPosition) {
            bufferedPosition = infoBean.getExtraValue();
        }
        if (infoBean.getCode() == CurrentPosition) {
            currentPosition = infoBean.getExtraValue();
        }
        Map<String, Object> progressMap = new HashMap<>();
        progressMap.put("event", "progress");
        progressMap.put("progress", currentPosition);
        progressMap.put("duration", mAliyunVodPlayer.getDuration());
        progressMap.put("playable", bufferedPosition);
        eventSink.success(progressMap);
    }

    @Override
    public void onRenderingStart() {

    }

    @Override
    public void onChangedSuccess(TrackInfo trackInfo) {

    }

    @Override
    public void onChangedFail(TrackInfo trackInfo, ErrorInfo errorInfo) {

    }

    @Override
    public void onSeekComplete() {

    }

    @Override
    public void onSeiData(int i, byte[] bytes) {

    }

    @Override
    public void onCompletion() {
        Map<String, Object> playendMap = new HashMap<>();
        playendMap.put("event", "playend");
        eventSink.success(playendMap);
    }


}