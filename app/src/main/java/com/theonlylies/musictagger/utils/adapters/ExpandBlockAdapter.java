package com.theonlylies.musictagger.utils.adapters;

import android.content.Context;
import android.support.constraint.ConstraintLayout;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.signature.MediaStoreSignature;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.theonlylies.musictagger.R;
import com.theonlylies.musictagger.utils.GlideApp;

/**
 * Created by linuxoid on 22.12.17.
 */

public class ExpandBlockAdapter extends BaseQuickAdapter<BlockItem, BlockViewHolder> implements BaseQuickAdapter.OnItemChildClickListener {
    private Context context;
    private OnItemClickListener clickListener;

    public ExpandBlockAdapter(int layoutResId, Context context, OnItemClickListener listener) {
        super(layoutResId);
        this.context = context;
        this.clickListener = listener;
        setOnItemChildClickListener(this);
    }

    public void expandItem(int position) {
        //RecyclerView rv = (RecyclerView) getViewByPosition(getRecyclerView(), position, R.id.musicItemRecyclerView);
        ConstraintLayout rv =  (ConstraintLayout)getViewByPosition(getRecyclerView(), position, R.id.layout_recyclerExpand);
        ImageButton button = (ImageButton) getViewByPosition(getRecyclerView(), position, R.id.expandButton);

        TransitionManager.beginDelayedTransition(rv);
        BlockItem item = getItem(position);
        item.visible = !item.visible;

        Animation rotateAnim;
        if (item.visible) rotateAnim = AnimationUtils.loadAnimation(context, R.anim.rotate_in);
        else rotateAnim = AnimationUtils.loadAnimation(context, R.anim.rotate_out);

        rotateAnim.setFillAfter(true);
        button.startAnimation(rotateAnim);
        rv.setVisibility(item.visible ? View.VISIBLE : View.GONE);
    }

    @Override
    protected void convert(BlockViewHolder helper, BlockItem item, int position) {
        helper.blockName.setText(item.getBlockName());
        helper.blockInfo.setText(item.getBlockInfo());
        helper.blockScName.setText(item.getBlockScName());
        GlideApp.with(context)
                .load(item.getMusicFiles().get(0).artworkUri)
                .signature(new MediaStoreSignature("lol", System.currentTimeMillis(), 3))
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .error(R.drawable.vector_artwork_placeholder)
                .into(helper.artwork);
        helper.addOnClickListener(R.id.expandButton);
        helper.addOnClickListener(R.id.groupEditButton);
        SimpleListAdapter adapter = (SimpleListAdapter) helper.recyclerView.getAdapter();
        adapter.setOnItemClickListener(clickListener);
        adapter.setNewData(item.getMusicFiles());
        TransitionManager.beginDelayedTransition(helper.expandPart);
        helper.expandPart.setVisibility(item.visible ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onItemChildClick(BaseQuickAdapter adapter, View view, int position) {
        if (view.getId() == R.id.expandButton) {
            /*RecyclerView rv = (RecyclerView) getViewByPosition(getRecyclerView(), position, R.id.musicItemRecyclerView);
            TransitionManager.beginDelayedTransition(rv);
            BlockItem item = getItem(position);
            item.visible = !item.visible;
            rv.setVisibility(item.visible ? View.VISIBLE : View.GONE);*/
            expandItem(position);
            Log.d("ExpandBlockAdapter", "expandButton");
        }
        if (view.getId() == R.id.groupEditButton) {
            Log.d("ExpandBlockAdapter", "groupEditButton");
            if (clickListener != null) {
                clickListener.onItemClick(this, view, position);
            }
        }
    }
}
