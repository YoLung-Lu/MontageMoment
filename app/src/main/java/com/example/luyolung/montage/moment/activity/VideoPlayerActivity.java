package com.example.luyolung.montage.moment.activity;

import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.VideoView;
import com.bumptech.glide.Glide;
import com.example.luyolung.montage.moment.R;
import java.util.ArrayList;

/**
 * Created by luyolung on 28/12/2017.
 */

public class VideoPlayerActivity extends AppCompatActivity {

    public static final String DATA_VIDEO_PATH = "path";
    public static final String DATA_IMAGE_PATH = "image_path";
    public static final String DATA_KEYWORD_LIST = "keyword_list";
    public static final String DATA_KEYWORD_TIME_LIST = "keyword_time_list";

    // Given.
    ArrayList<String> mImageListPath;
    ArrayList<String> mKeywordList;
    ArrayList<Integer> mKeywordTimeList;
    Long mVideoTimeLength;

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


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player);

        mVideoView = (VideoView)findViewById(R.id.VideoView);
        mImageView = (ImageView)findViewById(R.id.image);
        mTextView = (TextView)findViewById(R.id.text);

        //MediaController mediaController = new MediaController(this);
        // mediaController.setAnchorView(videoView);
        //videoView.setMediaController(mediaController);

        // Given.
        String videoPath = getIntent().getStringExtra(DATA_VIDEO_PATH);
        mImageListPath = getIntent().getStringArrayListExtra(DATA_IMAGE_PATH);
        mKeywordList = getIntent().getStringArrayListExtra(DATA_KEYWORD_LIST);
        mKeywordTimeList = getIntent().getIntegerArrayListExtra(DATA_KEYWORD_TIME_LIST);
        mVideoTimeLength = getVideoTimeLength(videoPath);

        // View.
        mVideoView.setVideoPath(videoPath);
        mVideoView.setOnCompletionListener(new OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                // Reset count.
                loopCount = 0;
                nextKeywordIndex = 0;

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
        if (mKeywordList == null || mKeywordTimeList == null) {
            return;
        }

        loopCount++;

        if (mKeywordList.size() > nextKeywordIndex &&
            loopCount * updateMilliSecond >= mKeywordTimeList.get(nextKeywordIndex)) {

            mTextView.setText( mKeywordList.get(nextKeywordIndex) );
            nextKeywordIndex++;
        }
    }

    private void playVideo() {
        mVideoView.start();
        if (mImageListPath != null) {
//            mVideoView;

            Glide.with(this)
                .load(mImageListPath.get(0))
                .into(mImageView);
        }
    }

    public OnCompletionListener getListener() {
        return new OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                // Reset count.
                loopCount = 0;
                handler.removeCallbacks(runnable);
            }
        };
    }

//    @Override
//    public void onPause() {
//        handler.removeCallbacks(runnable);
//        super.onPause();
//    }

}
