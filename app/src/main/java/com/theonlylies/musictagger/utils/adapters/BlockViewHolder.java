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
 * Created by linuxoid on 22.12.17.
 */

public class BlockViewHolder extends BaseViewHolder {
    TextView blockName,blockInfo;
    ImageButton expandButton,nonameButton1;
    ImageView artwork;
    RecyclerView recyclerView;

    public BlockViewHolder(View view) {
        super(view);
        blockName=view.findViewById(R.id.blockName);
        blockInfo=view.findViewById(R.id.blockInfo);
        expandButton=view.findViewById(R.id.expandButton);
        nonameButton1=view.findViewById(R.id.nonameButton1);
        artwork=view.findViewById(R.id.blockArtworkView);
        recyclerView=view.findViewById(R.id.musicItemRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
        SimpleListAdapter adapter = new SimpleListAdapter(R.layout.item_simple_without_img,view.getContext());
        recyclerView.setAdapter(adapter);
    }
}
