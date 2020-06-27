package com.pl.flutterpluginvideoplayer;

import android.util.Log;
import android.view.Surface;

import java.util.HashMap;
import java.util.Map;

import androidx.annotation.NonNull;

import com.aliyun.player.AliPlayer;
import com.aliyun.player.AliPlayerFactory;
import com.aliyun.player.IPlayer;
import com.aliyun.player.bean.ErrorInfo;
import com.aliyun.player.bean.InfoBean;
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

/**
 * FlutterpluginvideoplayerPlugin
 */
public class FlutterpluginvideoplayerPlugin implements FlutterPlugin, MethodCallHandler {

    static final String TAG = "Plugin";
    /// The MethodChannel that will the communication between Flutter and native Android
    ///
    /// This local reference serves to register the plugin with the Flutter Engine and unregister it
    /// when the Flutter Engine is detached from the Activity
    private MethodChannel channel;
    private final LongSparseArray<PlayerManager> videoPlayers;
    private final HashMap<String, ?> downloadManagerMap;
    FlutterPluginBinding mFlutterPluginBinding;

    public FlutterpluginvideoplayerPlugin() {
        this.videoPlayers = new LongSparseArray<>();
        this.downloadManagerMap = new HashMap<>();
    }

    @Override
    public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
        channel = new MethodChannel(flutterPluginBinding.getFlutterEngine().getDartExecutor(), "flutterpluginvideoplayer");
        channel.setMethodCallHandler(this);
        mFlutterPluginBinding = flutterPluginBinding;
    }

    // This static function is optional and equivalent to onAttachedToEngine. It supports the old
    // pre-Flutter-1.12 Android projects. You are encouraged to continue supporting
    // plugin registration via this function while apps migrate to use the new Android APIs
    // post-flutter-1.12 via https://flutter.dev/go/android-project-migration.
    //
    // It is encouraged to share logic between onAttachedToEngine and registerWith to keep
    // them functionally equivalent. Only one of onAttachedToEngine or registerWith will be called
    // depending on the user's project. onAttachedToEngine or registerWith must both be defined
    // in the same class.
    public static void registerWith(Registrar registrar) {
        final MethodChannel channel = new MethodChannel(registrar.messenger(), "flutterpluginvideoplayer");
        channel.setMethodCallHandler(new FlutterpluginvideoplayerPlugin());
    }

    @Override
    public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
        TextureRegistry textures = mFlutterPluginBinding.getTextureRegistry();
        if (call.method.equals("getPlatformVersion")) {
            result.success("Android " + android.os.Build.VERSION.RELEASE);
        }
        Log.e(TAG, "call method" + call.method);
        switch (call.method) {
            case "init":
                disposeAllPlayers();
                break;
            case "create":
                TextureRegistry.SurfaceTextureEntry surfaceTexture = textures.createSurfaceTexture();
                EventChannel videoEventsChannel = new EventChannel(mFlutterPluginBinding.getBinaryMessenger(), "flutter_tencentplayer/videoEvents" + surfaceTexture.id());
                PlayerManager player = new PlayerManager(mFlutterPluginBinding, videoEventsChannel, surfaceTexture, call, result);
                videoPlayers.put(surfaceTexture.id(), player);
                break;
            default:
                long textureId = ((Number) call.argument("textureId")).longValue();
                PlayerManager tencentPlayer = videoPlayers.get(textureId);
                if (tencentPlayer == null) {
                    result.error(
                            "Unknown textureId",
                            "No video player associated with texture id " + textureId,
                            null);
                    return;
                }
                onMethodCall(call, result, textureId, tencentPlayer);
                break;
        }
    }

    @Override
    public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
        channel.setMethodCallHandler(null);
    }

    private void disposeAllPlayers() {
        for (int i = 0; i < videoPlayers.size(); i++) {
            videoPlayers.valueAt(i).dispose();
        }
        videoPlayers.clear();
    }

    // flutter 发往android的命令
    private void onMethodCall(MethodCall call, Result result, long textureId, PlayerManager player) {

        switch (call.method) {
            case "play":
                player.play();
                result.success(null);
                break;
            case "pause":
                player.pause();
                result.success(null);
                break;
            case "seekTo":
                int location = ((Number) call.argument("location")).intValue();
                player.seekTo(location);
                result.success(null);
                break;
            case "setRate":
                float rate = ((Number) call.argument("rate")).floatValue();
//                player.setRate(rate);
                result.success(null);
                break;
            case "setBitrateIndex":
                int bitrateIndex = ((Number) call.argument("index")).intValue();
//                player.setBitrateIndex(bitrateIndex);
                result.success(null);
                break;
            case "dispose":
                player.dispose();
                videoPlayers.remove(textureId);
                result.success(null);
                break;
            default:
                result.notImplemented();
                break;
        }

    }

    private static class PlayerManager implements IPlayer.OnPreparedListener, IPlayer.OnErrorListener, IPlayer.OnStateChangedListener, IPlayer.OnInfoListener, IPlayer.OnRenderingStartListener, IPlayer.OnTrackChangedListener, IPlayer.OnSeekCompleteListener, IPlayer.OnSeiDataListener, IPlayer.OnCompletionListener {
        //播放器
        private AliPlayer mAliyunVodPlayer;
        private Surface surface;
        private final TextureRegistry.SurfaceTextureEntry textureEntry;
        private final FlutterPluginBinding mFlutterPluginBinding;
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

        PlayerManager(FlutterPluginBinding binding, EventChannel eventChannel, TextureRegistry.SurfaceTextureEntry textureEntry, MethodCall call,
                      Result result) {

            this.eventChannel = eventChannel;
            this.textureEntry = textureEntry;
            mFlutterPluginBinding = binding;
            //初始化播放器
            initAliVcPlayer();
            // 注册android向flutter发事件
            setFlutterBridge(eventChannel, textureEntry, result);
            setPlaySource(call);
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
//            // network FileId播放
//            if (call.argument("auth") != null) {
//                Log.e("pl111", "87654321");
//                authBuilder = new TXPlayerAuthBuilder();
//                Map authMap = (Map<String, Object>) call.argument("auth");
//                authBuilder.setAppId(((Number) authMap.get("appId")).intValue());
//                authBuilder.setFileId(authMap.get("fileId").toString());
//                mAliyunVodPlayer.startPlay(authBuilder);
//            } else {
//                Log.e("pl111", "12345678");
//                // asset播放
//                if (call.argument("asset") != null) {
//                    String assetLookupKey =
//                            mFlutterPluginBinding.getFlutterAssets().getAssetFilePathByName(call.argument("asset").toString());
////                    String assetLookupKey = mRegistrar.lookupKeyForAsset(call.argument("asset").toString());
//                    AssetManager assetManager = mFlutterPluginBinding.getApplicationContext().getAssets();
//                    try {
//                        InputStream inputStream = assetManager.open(assetLookupKey);
//                        String cacheDir = mFlutterPluginBinding.getApplicationContext().getCacheDir().getAbsoluteFile().getPath();
//
//                        String fileName = Base64.encodeToString(assetLookupKey.getBytes(), Base64.DEFAULT);
//
//                        Log.e("pl111", cacheDir);
//                        Log.e("pl111", fileName);
//                        File file = new File(cacheDir, fileName + ".mp4");
//                        FileOutputStream fileOutputStream = new FileOutputStream(file);
//                        if (!file.exists()) {
//                            file.createNewFile();
//                        }
//                        int ch = 0;
//                        while ((ch = inputStream.read()) != -1) {
//                            fileOutputStream.write(ch);
//                        }
//                        inputStream.close();
//                        fileOutputStream.close();
//
//                        mAliyunVodPlayer.startPlay(file.getPath());
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                } else {
//                    // file、 network播放
//                    mAliyunVodPlayer.startPlay(call.argument("uri").toString());
//                }
//            }
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
}
