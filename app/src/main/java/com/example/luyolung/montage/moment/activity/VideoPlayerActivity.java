package com.example.luyolung.montage.moment.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.VideoView;
import com.example.luyolung.montage.moment.R;

/**
 * Created by luyolung on 28/12/2017.
 */

public class VideoPlayerActivity extends AppCompatActivity {

    public static final String DATA_VIDEO_PATH = "path";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player);

        VideoView videoView = (VideoView)findViewById(R.id.VideoView);
        //MediaController mediaController = new MediaController(this);
        // mediaController.setAnchorView(videoView);
        //videoView.setMediaController(mediaController);

        String videoPath = getIntent().getStringExtra(DATA_VIDEO_PATH);

        videoView.setVideoPath(videoPath);
//        videoView.setVideoPath("/sdcard/blonde_secretary.3gp");

        videoView.start();
    }
}
