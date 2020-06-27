package com.pl.flutterpluginvideoplayer;

import android.content.res.AssetManager;
import android.net.Uri;
import android.os.Environment;
import android.util.Base64;
import android.util.Log;
import android.view.Surface;

import java.util.HashMap;
import java.util.Map;

import androidx.annotation.NonNull;


import com.pl.flutterpluginvideoplayer.constants.PlayParameter;
import com.alivc.player.AliyunErrorCode;
import com.aliyun.vodplayer.media.AliyunLocalSource;
import com.aliyun.vodplayer.media.AliyunMediaInfo;
import com.aliyun.vodplayer.media.AliyunPlayAuth;
import com.aliyun.vodplayer.media.AliyunVidSts;
import com.aliyun.vodplayer.media.AliyunVodPlayer;
import com.aliyun.vodplayer.media.IAliyunVodPlayer;
//import com.aliyun.vodplayerview.utils.NetWatchdog;
import androidx.collection.LongSparseArray;
import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.plugin.common.EventChannel;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry.Registrar;
import io.flutter.view.TextureRegistry;

/**
 * FlutterpluginvideoplayerPlugin
 */
public class FlutterpluginvideoplayerPlugin implements FlutterPlugin, MethodCallHandler {

    static final String TAG = "Plugin";
    static final String NativeEvent = "flutter_tencentplayer/videoEvents";
    /// The MethodChannel that will the communication between Flutter and native Android
    ///
    /// This local reference serves to register the plugin with the Flutter Engine and unregister it
    /// when the Flutter Engine is detached from the Activity
    private MethodChannel channel;
    private final LongSparseArray<MyVideoPlayer> videoPlayers;
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

                String url = call.argument("uri");
                final TextureRegistry.SurfaceTextureEntry textureEntry = textures.createSurfaceTexture();
                MyVideoPlayer player = createPlayer(textureEntry, result, call);
                player.setPlaySource(url);
                player.initSurface(textureEntry.surfaceTexture());
                break;
            default:
                long textureId = ((Number) call.argument("textureId")).longValue();
                MyVideoPlayer tencentPlayer = videoPlayers.get(textureId);
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

    private MyVideoPlayer createPlayer(TextureRegistry.SurfaceTextureEntry textureEntry, Result result, MethodCall call) {
        EventChannel eventChannel = new EventChannel(mFlutterPluginBinding.getBinaryMessenger(), NativeEvent + textureEntry.id());

        final MyVideoPlayer player = new MyVideoPlayer(mFlutterPluginBinding.getApplicationContext(), eventChannel, textureEntry);
        initPlayerCallback(player);

        videoPlayers.put(textureEntry.id(), player);
        Map<String, Object> reply = new HashMap<>();
        reply.put("textureId", textureEntry.id());
        result.success(reply);
        return player;
    }

    private void initPlayerCallback(final MyVideoPlayer player) {
        String sdDir = Environment.getExternalStorageDirectory().getAbsolutePath() + "/test_save_cache";
        player.setPlayingCache(true, sdDir, 60 * 60 /*时长, s */, 300 /*大小，MB*/);
        player.setOnPreparedListener(new IAliyunVodPlayer.OnPreparedListener() {
            @Override
            public void onPrepared() {
                sendInitialized(player, player.eventSink);
                player.resetSurfaceTextureSize();
            }
        });
    }

    private void sendInitialized(MyVideoPlayer player, QueuingEventSink eventSink) {
        Log.e("Plugin", "设置准备回调111");
        if (eventSink != null) {
            Log.e("Plugin", "设置准备回调2222");
            Map<String, Object> preparedMap = new HashMap<>();
            preparedMap.put("event", "initialized");
            preparedMap.put("duration", (int) player.getDuration());
            preparedMap.put("width", player.getVideoWidth());
            preparedMap.put("height", player.getVideoHeight());
            eventSink.success(preparedMap);
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
    private void onMethodCall(MethodCall call, Result result, long textureId, MyVideoPlayer player) {

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

}