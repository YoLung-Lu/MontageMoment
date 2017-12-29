package com.example.luyolung.montage.moment.activity;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.cardinalblue.montage_moment.bing_indexer.BingVideoIndexerHelper;
import com.cardinalblue.montage_moment.bing_indexer.model.Annotation;
import com.cardinalblue.montage_moment.bing_indexer.model.Appearance;
import com.cardinalblue.montage_moment.bing_indexer.model.Breakdown;
import com.cardinalblue.montage_moment.bing_indexer.model.ProcessState;
import com.example.luyolung.montage.moment.R;

import java.io.File;
import java.util.List;
import java.util.concurrent.Callable;

import bolts.Continuation;
import bolts.Task;

public class VideoIndexerActivity extends AppCompatActivity {
    public static final String VIDEO_PATH = "path";

    TextView mTvIndexerProgress;

    private File mValidVideoFile;
    private BingVideoIndexerHelper mVideoIndexerHelper;

    private static final int MSG_UPDATE_PROGRESS = 0;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case MSG_UPDATE_PROGRESS:
                String text = (String)msg.obj;
                mTvIndexerProgress.append(text);
            default:
                super.handleMessage(msg);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_indexer);
        mTvIndexerProgress = (TextView) findViewById(R.id.indexer_progress);

        String videoPath;
        if (savedInstanceState != null) {
            videoPath = savedInstanceState.getString(VIDEO_PATH);
        } else {
            videoPath = getIntent().getStringExtra(VIDEO_PATH);
        }

        if (videoPath == null || videoPath.isEmpty()) {
            mTvIndexerProgress.setText("Empty video path");
        } else {
            File file = new File(videoPath);
            if (!file.exists()) {
                mTvIndexerProgress.setText("File " + file.getName() + " does not exist.");
            } else {
                mValidVideoFile = file;
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mValidVideoFile != null) {
            mTvIndexerProgress.setText("Start video indexer for " + mValidVideoFile.getName());

            mVideoIndexerHelper = new BingVideoIndexerHelper();

            Task.callInBackground(new Callable<String>() {
                @Override
                public String call() throws Exception {
                    String videoId = mVideoIndexerHelper.uploadVideo(mValidVideoFile.getName(), false, mValidVideoFile);
                    Message msg = Message.obtain(mHandler, MSG_UPDATE_PROGRESS);
                    msg.obj = "\nVideo Id: " + videoId;
                    mHandler.sendMessage(msg);
                    return videoId;
                }
            }).continueWith(new Continuation<String, String>() {
                @Override
                public String then(Task<String> task) throws Exception {
                    String videoId = task.getResult();
                    while(true) {
                        Thread.sleep(300);

                        ProcessState state = mVideoIndexerHelper.getProcessState(videoId);
                        if (state != null) {
                            Message msg = Message.obtain(mHandler, MSG_UPDATE_PROGRESS);
                            msg.obj = "\n  State: " + state.getState() + ", Progress: " + state.getProgress();
                            mHandler.sendMessage(msg);
                        }

                        if ("Processed".equalsIgnoreCase(state.getState())) break;
                    }
                    return videoId;
                }
            }).continueWith(new Continuation<String, Breakdown>() {
                @Override
                public Breakdown then(Task<String> task) throws Exception {
                    String videoId = task.getResult();
                    Breakdown breakdown = mVideoIndexerHelper.getBreakdown(videoId);
                    if (breakdown != null) {
                        StringBuilder stringBuilder = new StringBuilder("\n");
                        stringBuilder.append("Processed video: " + breakdown.getName());
                        List<Annotation> annotations = breakdown.getSummarizedInsights().getAnnotations();
                        for (Annotation annotation : annotations) {
                            stringBuilder.append("  Annotation: " + annotation.getName());
                            List<Appearance> appearances = annotation.getAppearances();
                            for (Appearance appearance : appearances) {
                                stringBuilder.append("    Appearance: " + appearance.getStartSeconds() + " - " + appearance.getEndSeconds());
                            }
                        }
                        Message msg = Message.obtain(mHandler, MSG_UPDATE_PROGRESS);
                        msg.obj = stringBuilder.toString();
                        mHandler.sendMessage(msg);
                    }
                    return breakdown;
                }
            });
        }
    }
}
