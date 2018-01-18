package com.theonlylies.musictagger.utils.adapters;

import android.content.Context;
import android.support.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;

import java.util.List;

/**
 * Created by theonlylies on 18.01.18.
 */

public class SimpleListAdapter extends BaseQuickAdapter<MusicFile,SimpleViewHolder> {
    private Context context;
    public SimpleListAdapter(int layoutResId,Context context) {
        super(layoutResId);
        this.context = context;
    }

    @Override
    protected void convert(SimpleViewHolder helper, MusicFile item, int position) {
        helper.trackTitle.setText(item.getTitle());
    }
}
