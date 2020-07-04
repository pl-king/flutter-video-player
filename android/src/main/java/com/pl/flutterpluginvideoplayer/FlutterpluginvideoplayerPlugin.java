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

import androidx.collection.ArrayMap;
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
    private final ArrayMap<String, DownloadManager> downloadManagerMap;
    FlutterPluginBinding mFlutterPluginBinding;

    public FlutterpluginvideoplayerPlugin() {
        this.videoPlayers = new LongSparseArray<>();
        this.downloadManagerMap = new ArrayMap<String, DownloadManager>();
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
            case "download":
                String urlOrFileId = call.argument("urlOrFileId").toString();

                Log.e("pl123", "" + urlOrFileId);
                EventChannel downloadEventChannel = new EventChannel(mFlutterPluginBinding.getBinaryMessenger(), "flutter_tencentplayer/downloadEvents" + urlOrFileId);
                DownloadManager tencentDownload = new DownloadManager(mFlutterPluginBinding.getApplicationContext(), downloadEventChannel, call, result);
                downloadManagerMap.put(urlOrFileId, tencentDownload);
                break;
            case "stopDownload":
                downloadManagerMap.get(call.argument("urlOrFileId").toString()).stopDownload();
                result.success(null);
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
                player.setSpeed(rate);
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
