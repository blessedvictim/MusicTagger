package com.theonlylies.musictagger.utils.adapters;

import android.support.constraint.ConstraintLayout;
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
    TextView blockName,blockInfo,blockScName;
    ImageButton expandButton,nonameButton1;
    ImageView artwork;
    RecyclerView recyclerView;
    ConstraintLayout expandPart;

    public BlockViewHolder(View view) {
        super(view);
        blockName=view.findViewById(R.id.blockName);
        blockInfo=view.findViewById(R.id.blockInfo);
        blockScName=view.findViewById(R.id.blockScName);
        expandButton=view.findViewById(R.id.expandButton);
        expandPart=view.findViewById(R.id.layout_recyclerExpand);
        nonameButton1=view.findViewById(R.id.groupEditButton);
        artwork=view.findViewById(R.id.blockArtworkView);
        recyclerView=view.findViewById(R.id.musicItemRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
        SimpleListAdapter adapter = new SimpleListAdapter(R.layout.item_simple_without_img,view.getContext());
        recyclerView.setAdapter(adapter);
    }
}
