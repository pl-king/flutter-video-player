package com.pl.flutterpluginvideoplayer;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import java.util.HashMap;

import android.util.Log;
import android.view.Surface;

import java.util.HashMap;
import java.util.Map;

import androidx.annotation.NonNull;

import com.aliyun.downloader.AliDownloaderFactory;
import com.aliyun.downloader.AliMediaDownloader;
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

public class DownloadManager implements AliMediaDownloader.OnPreparedListener, AliMediaDownloader.OnProgressListener, AliMediaDownloader.OnErrorListener, AliMediaDownloader.OnCompletionListener {
    private QueuingEventSink eventSink = new QueuingEventSink();

    private final EventChannel eventChannel;

    private final Context mRegistrar;

    private String fileId;

    private AliMediaDownloader mAliDownloader;

//    private TXVodDownloadMediaInfo txVodDownloadMediaInfo;


    void stopDownload() {
        if (mAliDownloader != null) {
            mAliDownloader.stop();
        }
    }


    DownloadManager(
            Context activity,
            EventChannel eventChannel,
            MethodCall call,
            Result result) {
        this.eventChannel = eventChannel;
        this.mRegistrar = activity;


        mAliDownloader = AliDownloaderFactory.create(activity);
//        mAliDownloader.setListener(this);
        //配置下载保存的路径
        mAliDownloader.setSaveDir(call.argument("savePath").toString());

        mAliDownloader.setOnPreparedListener(this);
        mAliDownloader.setOnProgressListener(this);
        mAliDownloader.setOnErrorListener(this);
        mAliDownloader.setOnCompletionListener(this);
        String urlOrFileId = call.argument("urlOrFileId").toString();

//        if (urlOrFileId.startsWith("http")) {
//            txVodDownloadMediaInfo = downloader.startDownloadUrl(urlOrFileId);
//        } else {
//            TXPlayerAuthBuilder auth = new TXPlayerAuthBuilder();
//            auth.setAppId(((Number) call.argument("appId")).intValue());
//            auth.setFileId(urlOrFileId);
//            int quanlity = ((Number) call.argument("quanlity")).intValue();
//            String templateName = "HLS-标清-SD";
//            if (quanlity == 2) {
//                templateName = "HLS-标清-SD";
//            } else if (quanlity == 3) {
//                templateName = "HLS-高清-HD";
//            } else if (quanlity == 4) {
//                templateName = "HLS-全高清-FHD";
//            }
//            TXVodDownloadDataSource source = new TXVodDownloadDataSource(auth, templateName);
//            txVodDownloadMediaInfo = mAliDownloader.startDownload(source);
//            mAliDownloader.start();
//        }

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
        result.success(null);
    }


    @Override
    public void onPrepared(MediaInfo mediaInfo) {
        dealCallToFlutterData("start", mediaInfo);
    }

    @Override
    public void onDownloadingProgress(int i) {
        dealCallToFlutterData("progress", i);
    }

    @Override
    public void onProcessingProgress(int i) {

    }

    @Override
    public void onError(ErrorInfo errorInfo) {
//        HashMap<String, Object> targetMap = Util.convertToMap(errorInfo);
//        targetMap.put("downloadStatus", "error");
//        targetMap.put("error", "code:" + i + "  msg:" + errorInfo.getMsg());
//        if (txVodDownloadMediaInfo.getDataSource() != null) {
//            targetMap.put("quanlity", txVodDownloadMediaInfo.getDataSource().getQuality());
//            targetMap.putAll(Util.convertToMap(txVodDownloadMediaInfo.getDataSource().getAuthBuilder()));
//        }
//        eventSink.success(targetMap);
    }

    @Override
    public void onCompletion() {
        HashMap<String, Object> targetMap = new HashMap<>();
        targetMap.put("downloadStatus", "complete");
        eventSink.success(targetMap);
    }

    private void dealCallToFlutterData(String type, Object o) {
        HashMap<String, Object> targetMap = Util.convertToMap(o);
        targetMap.put("downloadStatus", type);
        eventSink.success(targetMap);
    }
}
