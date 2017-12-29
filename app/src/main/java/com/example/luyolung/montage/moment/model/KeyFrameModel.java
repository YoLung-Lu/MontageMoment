package com.example.luyolung.montage.moment.model;

import android.support.annotation.NonNull;

/**
 * Created by luyolung on 29/12/2017.
 */

public class KeyFrameModel implements Comparable<KeyFrameModel> {
    public String Keyword;
    public int startSecond;
    public int endSecond;

    public KeyFrameModel(String keyword, int startSecond, int endSecond) {
        Keyword = keyword;
        this.startSecond = startSecond;
        this.endSecond = endSecond;
    }

    @Override
    public int compareTo(@NonNull KeyFrameModel o) {
        return this.endSecond > o.endSecond ? 1 : -1;
    }
}
