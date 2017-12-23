package com.theonlylies.musictagger.activities;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseViewHolder;
import com.theonlylies.musictagger.R;

/**
 * Created by linuxoid on 22.12.17.
 */

public class BlockViewHolder extends BaseViewHolder {
    TextView blockName,blockInfo;
    ImageButton expandButton,nonameButton1,nonameButton2;
    RecyclerView recyclerView;

    public BlockViewHolder(View view) {
        super(view);
        blockName=view.findViewById(R.id.blockName);
        blockInfo=view.findViewById(R.id.blockInfo);
        expandButton=view.findViewById(R.id.expandButton);
        nonameButton1=view.findViewById(R.id.nonameButton1);
        nonameButton2=view.findViewById(R.id.nonameButton2);
        recyclerView=view.findViewById(R.id.musicItemRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
        ListAdapter adapter = new ListAdapter(R.layout.item_simple,view.getContext());
        recyclerView.setAdapter(adapter);
    }
}
