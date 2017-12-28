package com.example.luyolung.montage.moment.activity;

import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.example.luyolung.montage.moment.R;

/**
 * Created by luyolung on 28/12/2017.
 */

public class VideoAnalyzerActivity extends AppCompatActivity {

    public static final String DATA_VIDEO_PATH = "path";
    TextView mBtnDone;
    private ImageView mImageView1;
    private ImageView mImageView2;
    private ImageView mImageView3;
    private ImageView mImageView4;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_analyzer);

        String videoPath = getIntent().getStringExtra(DATA_VIDEO_PATH);

        mImageView1 = (ImageView)findViewById(R.id.image1);
        mImageView2 = (ImageView)findViewById(R.id.image2);
        mImageView3 = (ImageView)findViewById(R.id.image3);
        mImageView4 = (ImageView)findViewById(R.id.image4);

        extractImageToView(videoPath);


//        mBtnDone = (TextView) findViewById(R.id.button);
//        mBtnDone.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (!mVideoView.isPlaying()) {
//                    mVideoView.start();
//                }
//            }
//        });
    }

    private void extractImageToView(String videoPath) {

        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(videoPath);

        String time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        long timeInMicroSecond = Long.parseLong(time)*1000;

        Bitmap bmFrame = retriever.getFrameAtTime(timeInMicroSecond/4);
        mImageView1.setImageBitmap(bmFrame);

        bmFrame = retriever.getFrameAtTime(timeInMicroSecond/4*2);
        mImageView2.setImageBitmap(bmFrame);

        bmFrame = retriever.getFrameAtTime(timeInMicroSecond/4*3);
        mImageView3.setImageBitmap(bmFrame);

        bmFrame = retriever.getFrameAtTime(timeInMicroSecond);
        mImageView4.setImageBitmap(bmFrame);


//        Glide.with(this)
//            .load(bmFrame)
//            .placeholder(R.drawable.img_android_robot)
//            .into(mImageView1);

    }
}
