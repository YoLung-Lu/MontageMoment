package com.example.luyolung.montage.moment;

/**
 * Created by luyolung on 14/08/2017.
 */


import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;
import bolts.Continuation;
import bolts.Task;
import com.example.luyolung.montage.moment.Utils.MovieCreator;
import com.example.luyolung.montage.moment.activity.MyTakePhotoDelegateActivity;
import com.example.luyolung.montage.moment.activity.VideoAnalyzerActivity;
import com.example.luyolung.montage.moment.activity.VideoIndexerActivity;
import com.example.luyolung.montage.moment.activity.VideoPlayerActivity;
import com.my.core.protocol.IDrawerViewLayout;
import com.my.core.protocol.IProgressBarView;
import com.my.core.util.ViewUtil;
import com.my.widget.adapter.SampleMenuAdapter;
import com.my.widget.adapter.SampleMenuAdapter.SampleMenuItem;
import java.io.File;
import java.util.ArrayList;

public class StartActivity
    extends AppCompatActivity
    implements IProgressBarView {

    static final int NAVIGATE_TAKE_VIDEO = 0;
    static final int NAVIGATE_ANALYSIS_VIDEO = 1;
    static final int NAVIGATE_WATCH_VIDEO = 2;
    static final int NAVIGATE_VIDEO_INDEXER = 4;

    Toolbar mToolbar;
    ListView mStartMenu;
    private String mVideoPath = "/storage/emulated/0/Movies/Montage Moment/20171228_174327-1042092136.mp4";
    private String mVideoMontagePath;

    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_start);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            getSupportActionBar().setDisplayShowHomeEnabled(false);
        }

        // List menu.
        mStartMenu = (ListView) findViewById(R.id.menu);
        mStartMenu.setAdapter(onCreateSampleMenu());
        mStartMenu.setOnItemClickListener(onClickSampleMenuItem());


        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setTitle(getResources().getString(R.string.loading));
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setCancelable(false);
        mProgressDialog.setCanceledOnTouchOutside(false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();

        inflater.inflate(R.menu.menu_start, menu);

        return true;
    }

    @Override
    protected void onActivityResult(int requestCode,
                                    int resultCode,
                                    Intent data) {
        switch (requestCode) {
            case NAVIGATE_TAKE_VIDEO:
                System.out.println("Result:" + resultCode);
                mVideoPath = data.getData().getPath();
            default:
                super.onActivityResult(requestCode, resultCode, data);
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                toggleDrawerMenu();
                return true;
            case R.id.item_take_photo:
                Toast.makeText(this, "Test", Toast.LENGTH_SHORT)
                    .show();
//                startActivity(new Intent(this, MyTakePhotoDelegateActivity.class));
                startActivityForResult(new Intent(this, MyTakePhotoDelegateActivity.class), NAVIGATE_TAKE_VIDEO);
                return true;
            case R.id.item_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void showProgressBar() {
        ViewUtil
            .with(this)
            .setProgressBarCancelable(false)
            .showProgressBar(getString(R.string.loading));
    }

    @Override
    public void showProgressBar(String msg) {
        showProgressBar();
    }

    @Override
    public void hideProgressBar() {
        ViewUtil
            .with(this)
            .hideProgressBar();
    }

    @Override
    public void updateProgress(int progress) {
        showProgressBar();
    }

    ///////////////////////////////////////////////////////////////////////////
    // Protected / Private Methods ////////////////////////////////////////////

    private void toggleDrawerMenu() {
        // DO NOTHING
    }

    private IDrawerViewLayout.OnDrawerStateChange onMenuStateChange() {
        return new IDrawerViewLayout.OnDrawerStateChange() {
            @Override
            public void onOpenDrawer() {
                mToolbar.setNavigationIcon(R.drawable.icon_toolbar_close);
            }

            @Override
            public void onCloseDrawer() {
                mToolbar.setNavigationIcon(R.drawable.icon_list_black_24px);
            }
        };
    }

    @SuppressWarnings({"unchecked"})
    private SampleMenuAdapter onCreateSampleMenu() {
        return new SampleMenuAdapter(
            this,
            new SampleMenuItem[]{
                new SampleMenuItem(
                    "Video Recorder",
                    "Record !",
                    new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            startActivityForResult(
                                new Intent(StartActivity.this, MyTakePhotoDelegateActivity.class),
                                NAVIGATE_TAKE_VIDEO);
                        }
                    }),
                new SampleMenuItem(
                    "Video Analyzer",
                    "GOGO~",
                    new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(StartActivity.this, VideoAnalyzerActivity.class);
                            intent.putExtra(VideoAnalyzerActivity.DATA_VIDEO_PATH,
                                            mVideoPath);
                            startActivityForResult(intent, NAVIGATE_ANALYSIS_VIDEO);
                        }
                    }),
                new SampleMenuItem(
                    "Video Player",
                    "Play input video",
                    new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(StartActivity.this,
                                                       VideoPlayerActivity.class);
                            intent.putExtra(VideoPlayerActivity.DATA_VIDEO_PATH, mVideoPath);

                            intent.putStringArrayListExtra(
                                VideoPlayerActivity.DATA_KEYWORD_LIST,
                                getKeywordList());
                            intent.putIntegerArrayListExtra(
                                VideoPlayerActivity.DATA_KEYWORD_TIME_LIST,
                                getKeywordTimeList());

                            startActivityForResult(intent, NAVIGATE_WATCH_VIDEO);
                        }
                    }),
                new SampleMenuItem(
                    "Video Indexer",
                    "Get annotation with Bing",
                    new OnClickListener() {
                        @Override
                        public void onClick(View v){
                            Intent intent = new Intent(StartActivity.this, VideoIndexerActivity.class);
                            intent.putExtra(VideoIndexerActivity.VIDEO_PATH, mVideoPath);
                            startActivityForResult(intent, NAVIGATE_VIDEO_INDEXER);
                        }
                    }),
                new SampleMenuItem(
                    "Obama testing",
                    "O~~~~~~~~~~BAMABAMA",
                    new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            File folder = Environment
                                .getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
                            mVideoMontagePath = folder.getAbsolutePath() + "/Videos/film.mp4";

//                            int number_images = 3;
//                            for (int j = 0; j < number_images; j++) {
//                                imageList.add(folder.getAbsolutePath() + "/Obama/obama" + j + ".jpg");
//                            }
//
//                            Intent intent = new Intent(StartActivity.this, VideoPlayerActivity.class);
//                            intent.putExtra(VideoPlayerActivity.DATA_VIDEO_PATH, mVideoPath);
//                            intent.putStringArrayListExtra(VideoPlayerActivity.DATA_IMAGE_PATH, imageList);
//                            startActivityForResult(intent, NAVIGATE_WATCH_VIDEO);

                            mProgressDialog.show();

                            final MovieCreator creator = new MovieCreator(mVideoPath, mVideoMontagePath);
                            creator.setImageList(getMontageImageList());

                            creator.generateMontageVideo().continueWith(
                                new Continuation<String, Void>() {
                                    @Override
                                    public Void then(Task<String> task) throws Exception {
                                        if (task.isCancelled() || task.isFaulted()) {
                                            return null;
                                        }

                                        mProgressDialog.hide();

                                        String filePath = task.getResult();

                                        Intent intent = new Intent(StartActivity.this, VideoPlayerActivity.class);
                                        intent.putExtra(VideoPlayerActivity.DATA_VIDEO_PATH, filePath);
                                        startActivityForResult(intent, NAVIGATE_WATCH_VIDEO);
                                        return null;
                                    }
                                }, Task.UI_THREAD_EXECUTOR);
                        }
                    }),
                new SampleMenuItem(
                    "Play newest video",
                    "GO",
                    new OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            File folder = Environment
                                .getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
                            mVideoMontagePath = folder.getAbsolutePath() + "/Videos/film.mp4";

                            Intent intent = new Intent(StartActivity.this, VideoPlayerActivity.class);
                            intent.putExtra(VideoPlayerActivity.DATA_VIDEO_PATH, mVideoMontagePath);
                            startActivityForResult(intent, NAVIGATE_WATCH_VIDEO);
                        }
                    }),
            });
    }

    private AdapterView.OnItemClickListener onClickSampleMenuItem() {
        return new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent,
                View view,
                int position,
                long id) {
                final SampleMenuItem item = (SampleMenuItem) parent.getAdapter()
                    .getItem(position);
                item.onClickListener.onClick(view);
            }
        };
    }

    public ArrayList<String> getMontageImageList() {
        // TODO: override the hard-code path.
        ArrayList<String> imageList = new ArrayList<>();
        File folder = Environment
            .getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
        String path = folder.getAbsolutePath();
        int number_images = 3;

        for (int j = 0; j < number_images; j++) {
            imageList.add(path + "/Obama/obama" + j + ".jpg");
        }
        return imageList;
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
}
