package com.example.luyolung.montage.moment.activity;

import android.os.Bundle;
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

public class VideoPlayerActivity2 extends AppCompatActivity {

    public static final String DATA_VIDEO_PATH = "path";
    public static final String DATA_IMAGE_PATH = "image_path";

    private ImageView mImageView;
    TextView mBtnDone;
    VideoView mVideoView;

    ArrayList<String> mImageListPath;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player);

        mVideoView = (VideoView)findViewById(R.id.VideoView);
        mImageView = (ImageView)findViewById(R.id.image);
        //MediaController mediaController = new MediaController(this);
        // mediaController.setAnchorView(videoView);
        //videoView.setMediaController(mediaController);

        String videoPath = getIntent().getStringExtra(DATA_VIDEO_PATH);
        mImageListPath = getIntent().getStringArrayListExtra(DATA_IMAGE_PATH);

        mVideoView.setVideoPath(videoPath);
//        videoView.setVideoPath("/sdcard/blonde_secretary.3gp");

        mBtnDone = (TextView) findViewById(R.id.button);
        mBtnDone.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mVideoView.isPlaying()) {
                    playVideo();
                }
            }
        });
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
}
