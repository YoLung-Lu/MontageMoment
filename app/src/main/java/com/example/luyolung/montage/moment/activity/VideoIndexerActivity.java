package com.example.luyolung.montage.moment.activity;

import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import android.widget.VideoView;
import android.widget.ViewSwitcher;
import com.cardinalblue.montage_moment.bing_indexer.BingVideoIndexerHelper;
import com.cardinalblue.montage_moment.bing_indexer.model.Annotation;
import com.cardinalblue.montage_moment.bing_indexer.model.Appearance;
import com.cardinalblue.montage_moment.bing_indexer.model.Breakdown;
import com.cardinalblue.montage_moment.bing_indexer.model.ProcessState;
import com.example.luyolung.montage.moment.R;

import com.example.luyolung.montage.moment.model.KeyFrameModel;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

import bolts.Continuation;
import bolts.Task;

public class VideoIndexerActivity extends AppCompatActivity {
    public static final String VIDEO_PATH = "path";

    // Data.
    ArrayList<KeyFrameModel> mKeyFrameList;
    Long mVideoTimeLength;
    Breakdown mBreakdown;

    // View.
    private VideoView mVideoView;
    private TextView mBtnPlay;
    private ImageView mImageView;
    private TextView mTextView;

    // Threading.
    private Handler handler;
    private Runnable runnable;
    private float updateMilliSecond = 20;
    private int loopCount = 0;
    private int nextKeywordIndex = 0;

    TextView mTvIndexerProgress;

    private File mValidVideoFile;
    private BingVideoIndexerHelper mVideoIndexerHelper;

    private static final int MSG_UPDATE_PROGRESS = 0;

    // View switcher.
    protected static final int INDEX_WEB_PROCESSING = 0;
    protected static final int INDEX_PLAY_VIDEO = 1;
    protected ViewSwitcher mSwitcher;

    class ProgressStatus {
        private static final String TEMPLATE_PROCESS =
            "Start video indexer for %s\n" +
                "Video id: %s\n\n" +
                "Processing\n" +
                "  State: %s, Progress: %s\n\n" +
                "Processed video name: %s\n" +
                "%s";

        String filename;
        String videoId;
        String state;
        String progress;
        String videoName;
        String annotationText;

        @Override
        public String toString() {
            return String.format(TEMPLATE_PROCESS, filename, videoId, state, progress, videoName, annotationText);
        }
    }

    private ProgressStatus mStatus = new ProgressStatus();

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case MSG_UPDATE_PROGRESS:
                ProgressStatus status = (ProgressStatus)msg.obj;
                mTvIndexerProgress.setText(status.toString());
            default:
                super.handleMessage(msg);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_indexer);
        mSwitcher = (ViewSwitcher) findViewById(R.id.switcher);
        mSwitcher.setDisplayedChild(INDEX_WEB_PROCESSING);

        // Player view.
        mVideoView = (VideoView)findViewById(R.id.VideoView);
        mImageView = (ImageView)findViewById(R.id.image);
        mTextView = (TextView)findViewById(R.id.text);

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

        // Player init.
        mVideoTimeLength = getVideoTimeLength(videoPath);


        // View.
        mVideoView.setVideoPath(videoPath);
        mVideoView.setOnCompletionListener(new OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                // Reset count.
                loopCount = 0;
                nextKeywordIndex = 0;
                mTextView.setText("");

                handler.removeCallbacks(runnable);
            }
        });
        mBtnPlay = (TextView) findViewById(R.id.button);
        mBtnPlay.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mVideoView.isPlaying()) {
                    playVideo();

                    // Start looper.
                    handler.post(runnable);
                }
            }
        });

        // Looper.
        handler = new Handler();
        runnable = new Runnable()
        {
            @Override
            public void run()
            {
                updateKeyword();
                handler.postDelayed(this, (long) updateMilliSecond);
            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mValidVideoFile != null) {
            mStatus.filename = mValidVideoFile.getName();

            mVideoIndexerHelper = new BingVideoIndexerHelper();

            Task.callInBackground(new Callable<String>() {
                @Override
                public String call() throws Exception {
                    Message msg = Message.obtain(mHandler, MSG_UPDATE_PROGRESS, mStatus);
                    mHandler.sendMessage(msg);

                    String videoId = mVideoIndexerHelper.uploadVideo(mValidVideoFile.getName(), false, mValidVideoFile);
//                    String videoId = "65309cdf01";

                    msg = Message.obtain(mHandler, MSG_UPDATE_PROGRESS, mStatus);
                    mStatus.videoId = videoId;
                    mHandler.sendMessage(msg);
                    return videoId;
                }
            }).continueWith(new Continuation<String, String>() {
                @Override
                public String then(Task<String> task) throws Exception {
                    String videoId = task.getResult();
                    while(true) {
                        Thread.sleep(1000);

                        ProcessState state = mVideoIndexerHelper.getProcessState(videoId);
                        if (state != null) {
                            Message msg = Message.obtain(mHandler, MSG_UPDATE_PROGRESS, mStatus);
                            mStatus.state = state.getState();
                            mStatus.progress = state.getProgress();
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
                            stringBuilder.append("\n  Annotation: " + annotation.getName());
                            List<Appearance> appearances = annotation.getAppearances();
                            for (Appearance appearance : appearances) {
                                stringBuilder.append("\n    Appearance: " + appearance.getStartSeconds() + " - " + appearance.getEndSeconds());
                            }
                        }
                        Message msg = Message.obtain(mHandler, MSG_UPDATE_PROGRESS, mStatus);
                        mStatus.annotationText = stringBuilder.toString();
                        mHandler.sendMessage(msg);
                        Thread.sleep(1000);
                    }
                    return breakdown;
                }
            }).continueWith(new Continuation<Breakdown, Void>() {
                @Override
                public Void then(Task<Breakdown> task) throws Exception {
                    if (task.getResult() != null) {
                        // Switch view.
                        mSwitcher.setDisplayedChild(INDEX_PLAY_VIDEO);
                        mKeyFrameList = new ArrayList<>();

                        mBreakdown = task.getResult();
                        List<Annotation> annotations = mBreakdown.getSummarizedInsights().getAnnotations();
                        for (Annotation annotation : annotations) {
                            List<Appearance> appearances = annotation.getAppearances();
                            for (Appearance appearance : appearances) {
                                mKeyFrameList.add(new KeyFrameModel(annotation.getName(),
                                                                    (int) appearance.getStartSeconds()*1000,
                                                                    (int) appearance.getEndSeconds()*1000));
//                                mKeyFrameList.add(annotation.getName());
//                                mKeywordTimeList.add((int) appearance.getEndSeconds()*1000);
                            }
                        }
                        Collections.sort(mKeyFrameList);
                    }



                    return null;
                }
            }, Task.UI_THREAD_EXECUTOR);
        }
    }

    public ArrayList<String> getKeywordList() {
        ArrayList<String> keywordList = new ArrayList<>();
        for (int j = 0; j < 3; j++) {
            keywordList.add("Obama ~~~ "+j);
        }
        return keywordList;
    }

    public ArrayList<Integer> getKeywordTimeList() {
        ArrayList<Integer> keywordTimeList = new ArrayList<>();
        for (int j = 0; j < 3; j++) {
            keywordTimeList.add(1100*(j+1));
        }
        return keywordTimeList;
    }

    private Long getVideoTimeLength(String videoPath) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(videoPath);
        String inputVideoTime = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);

        long durationMs = Long.parseLong(inputVideoTime);
//        long duration = durationMs / 1000;
//        long h = duration / 3600;
//        long m = (duration - h * 3600) / 60;
//        long s = duration - (h * 3600 + m * 60);

        return durationMs;
    }

    private void updateKeyword() {
        if (mKeyFrameList == null) {
            mTextView.setText("EMPTY DATA");
            return;
        }

        loopCount++;

        if (mKeyFrameList.size() > nextKeywordIndex &&
            loopCount * updateMilliSecond >= mKeyFrameList.get(nextKeywordIndex).endSecond) {

            mTextView.setText(mKeyFrameList.get(nextKeywordIndex).Keyword);
            nextKeywordIndex++;
        }
    }

    private void playVideo() {
        mVideoView.start();
    }
}
