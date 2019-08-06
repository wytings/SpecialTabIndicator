package com.wytings.scroll.behavior;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import com.wytings.special.R;

/**
 * Created by Rex.Wei on 2019-08-06
 *
 * @author weiyuting
 */
public class SearchBehavior extends AbsBehavior<View> {

    public SearchBehavior(final Context context, final AttributeSet attrs) {
        super(context, attrs, R.id.search_layout);
    }
}
