package com.theonlylies.musictagger.activities;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.transition.TransitionManager;
import android.view.View;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.theonlylies.musictagger.R;
import com.theonlylies.musictagger.utils.BlockItem;

/**
 * Created by linuxoid on 22.12.17.
 */

public class ExpandBlockAdapter extends BaseQuickAdapter<BlockItem, BlockViewHolder> implements BaseQuickAdapter.OnItemChildClickListener {
    private Context context;
    private OnItemClickListener clickListener;
    public ExpandBlockAdapter(int layoutResId, Context context,OnItemClickListener listener) {
        super(layoutResId);
        this.context=context;
        this.clickListener=listener;
        setOnItemChildClickListener(this);
        //get
    }

    @Override
    protected void convert(BlockViewHolder helper, BlockItem item) {
        helper.blockName.setText(item.getBlockName());
        helper.blockInfo.setText(item.getBlockInfo());
        helper.addOnClickListener(R.id.expandButton);
        ListAdapter adapter = (ListAdapter) helper.recyclerView.getAdapter();
        adapter.setOnItemClickListener(clickListener);
        adapter.setNewData(item.getMusicFiles());
    }

    @Override
    public void onItemChildClick(BaseQuickAdapter adapter, View view, int position) {
        if (view.getId()==R.id.expandButton) {
            RecyclerView rv = (RecyclerView) getViewByPosition(getRecyclerView(), position, R.id.musicItemRecyclerView);
            TransitionManager.beginDelayedTransition(rv);
            BlockItem item = getItem(position);
            item.visible = !item.visible;
            rv.setVisibility(item.visible ? View.VISIBLE : View.GONE);
        }else if (view.getId()==R.id.nonameButton1){

        }else if (view.getId()==R.id.nonameButton2){

        }
    }
}
