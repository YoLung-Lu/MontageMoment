package com.my.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.percent.PercentRelativeLayout;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class SquaredMenuItemView extends PercentRelativeLayout {

    protected int mIconRes;
    protected int mCaptionRes;

    protected ImageView mIcon;
    protected TextView mCaption;
    protected View mNotiBadge;

    public SquaredMenuItemView(Context context) {
        super(context);

        initView(context);
    }

    public SquaredMenuItemView(Context context, AttributeSet attrs) {
        super(context, attrs);

        initAttrs(context, attrs);
        initView(context);
    }

    @Override
    protected void onMeasure(int widthSpec, int heightSpec) {
        super.onMeasure(widthSpec, widthSpec);
    }

    public void setHasNotificationBadge(boolean hasNoti) {
        mNotiBadge.setVisibility(hasNoti ? View.VISIBLE : View.INVISIBLE);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Protected / Private Methods ////////////////////////////////////////////

    protected void initAttrs(Context context, AttributeSet attrs) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SquaredMenuItemView);

        try {
            mIconRes = a.getResourceId(R.styleable.SquaredMenuItemView_iconSrcCompat,
                                       R.drawable.icon_settings);
            mCaptionRes = a.getResourceId(R.styleable.SquaredMenuItemView_caption,
                                          R.string.menu_settings);
        } finally {
            a.recycle();
        }
    }

    protected void initView(Context context) {
        // The main layout.
        inflate(context, R.layout.view_squared_menu_item, this);

        mIcon = (ImageView) findViewById(R.id.icon);
        if (mIconRes != 0) {
            try {
                mIcon.setImageResource(mIconRes);
            } catch (Throwable ex) {
                // DO NOTHING.
            }
        }
        mCaption = (TextView) findViewById(R.id.caption);
        if (mCaptionRes != 0) {
            mCaption.setText(mCaptionRes);
        }
        mNotiBadge = findViewById(R.id.icon_noti_badge);
    }
}
