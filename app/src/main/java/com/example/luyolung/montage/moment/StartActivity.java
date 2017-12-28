package com.example.luyolung.montage.moment;

/**
 * Created by luyolung on 14/08/2017.
 */


import android.content.Intent;
import android.os.Bundle;
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
import com.example.luyolung.montage.moment.activity.ConstraintLayoutActivity;
import com.example.luyolung.montage.moment.activity.MyTakePhotoDelegateActivity;
import com.example.luyolung.montage.moment.activity.VideoPlayerActivity;
import com.my.core.protocol.IDrawerViewLayout;
import com.my.core.protocol.IProgressBarView;
import com.my.core.util.ViewUtil;
import com.my.widget.adapter.SampleMenuAdapter;
import com.my.widget.adapter.SampleMenuAdapter.SampleMenuItem;

public class StartActivity
    extends AppCompatActivity
    implements IProgressBarView {

    static final int NAVIGATE_TAKE_VIDEO = 0;
    static final int NAVIGATE_WATCH_VIDEO = 1;

    Toolbar mToolbar;
    ListView mStartMenu;
    private String mVideoPath = "";


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
                    "Video Player",
                    "GOGO",
                    new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(StartActivity.this, VideoPlayerActivity.class);
                            intent.putExtra(VideoPlayerActivity.DATA_VIDEO_PATH, mVideoPath);
                            startActivityForResult(intent, NAVIGATE_WATCH_VIDEO);
                        }
                    }),
                new SampleMenuItem(
                    "Constraint Layout",
                    "Practice on every kind of constraint relationship.\n",
                    new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            startActivity(new Intent(StartActivity.this,
                                                     ConstraintLayoutActivity.class)
                                              .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
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

}
