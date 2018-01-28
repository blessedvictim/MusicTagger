package com.theonlylies.musictagger.utils.adapters;

/**
 * Created by theonlylies on 25.01.18.
 */


import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.theonlylies.musictagger.R;
import com.theonlylies.musictagger.utils.GlideApp;

import java.util.ArrayList;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

public class GridViewAdapter extends ArrayAdapter<String> {
    private Context context;
    private int layoutResourceId;
    private ArrayList<String> albumNames;
    private ArrayList<String> albumUrls;


    public GridViewAdapter(Context context, int layoutResourceIdm) {
        super(context, layoutResourceIdm);
        this.layoutResourceId = layoutResourceIdm;
        this.context = context;
        this.albumNames = new ArrayList<>();
        this.albumUrls = new ArrayList<>();
    }

    public void addData(String album, String artist,String url) {
        super.add(album);
        albumNames.add(album+"\n"+artist);
        albumUrls.add(url);
        notifyDataSetChanged();
    }

    public String getUrl(int pos){
        return albumUrls.get(pos);
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View row = convertView;
        ViewHolder holder = null;

        if (row == null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);
            holder = new ViewHolder();
            holder.imageTitle = (TextView) row.findViewById(R.id.gridTextView);
            holder.image = (ImageView) row.findViewById(R.id.gridImageView);
            row.setTag(holder);
        } else {
            holder = (ViewHolder) row.getTag();
        }

        holder.imageTitle.setText(albumNames.get(position));
        GlideApp.with(context)
                .load(albumUrls.get(position))
                .transition(withCrossFade())
                .into(holder.image);
        return row;
    }


    static class ViewHolder {
        TextView imageTitle;
        ImageView image;
    }

}