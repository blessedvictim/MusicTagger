package com.theonlylies.musictagger.utils.adapters;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.support.v7.widget.CardView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseViewHolder;
import com.theonlylies.musictagger.R;

/**
 * Created by linuxoid on 17.12.17.
 */

public class ViewHolder extends BaseViewHolder {
    public TextView trackTitle;
    public TextView trackAlbum;
    public TextView trackArtist;
    public ImageView atrworkImageView;
    public CardView cardView;
    ColorStateList color;

    public ViewHolder(final View itemView) {
        super(itemView);
        atrworkImageView=itemView.findViewById(R.id.artworkImageView);
        trackTitle=itemView.findViewById(R.id.itemSimpleTrNum);
        trackAlbum = itemView.findViewById(R.id.trackAlbum);
        trackArtist = itemView.findViewById(R.id.trackArtist);
        cardView=itemView.findViewById(R.id.card_item_simple);
        color = cardView.getCardBackgroundColor();
    }


    public void setSelected(boolean selected) {
        if (selected){
            cardView.setCardBackgroundColor(Color.GRAY);
        }else{
            cardView.setCardBackgroundColor(color);
        }
    }
}