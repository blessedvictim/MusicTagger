package com.theonlylies.musictagger.utils.adapters;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseViewHolder;
import com.theonlylies.musictagger.R;

/**
 * Created by theonlylies on 17.01.18.
 */

public class SimpleViewHolder extends BaseViewHolder {
    TextView trackTitle;
    public SimpleViewHolder(View view) {
        super(view);
        trackTitle=view.findViewById(R.id.itemSimpleTrTitle);
    }
}
