package com.theonlylies.musictagger.activities;

import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseViewHolder;
import com.theonlylies.musictagger.R;

/**
 * Created by linuxoid on 17.12.17.
 */

public class ViewHolder extends BaseViewHolder {
    public TextView trackTitle;
    public TextView trackArtistAlbum;
    public ImageView atrworkImageView;

    public ViewHolder(final View itemView) {
        super(itemView);
        atrworkImageView=itemView.findViewById(R.id.artworkImageView);
        trackTitle=itemView.findViewById(R.id.trackTitle);
        trackArtistAlbum=itemView.findViewById(R.id.trackArtistAlbum);
    }
}