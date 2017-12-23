package com.theonlylies.musictagger.activities;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Filter;
import android.widget.Filterable;

import com.bumptech.glide.Glide;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.theonlylies.musictagger.utils.MusicFile;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by linuxoid on 17.12.17.
 */

public class ListAdapter extends BaseQuickAdapter<MusicFile, ViewHolder>
        implements Filterable {

    public int getDataModelSize(){
        return dataModelsFULL.size();
    }

    private List<MusicFile> dataModelsFULL;
    private Context context;

    public ListAdapter(int layoutResId, Context context) {
        super(layoutResId);
        dataModelsFULL = getData();
        this.context = context;
    }

    /**
     * Так как я дебил то это перестнает работать если вы вызовете addData for collection , insert
     * перестанет работать поиск и вообще пиздец
     */
    @Override
    public void addData(MusicFile file) {
        super.addData(file);
        dataModelsFULL.add(file);
    }

    @Override
    public void addData(@NonNull Collection file) {
        super.addData(file);
        dataModelsFULL.addAll(file);
    }

    @Override
    public void remove(int position) {
        super.remove(position);
        dataModelsFULL.remove(position);
    }


    public void returnFromSearch() {
        setNewData(dataModelsFULL);
    }

    @Override
    protected void convert(ViewHolder helper, MusicFile item) {
        helper.trackTitle.setText(item.getTitle());
        String album = item.getAlbum();
        String artist = item.getArtist();
        StringBuilder res = new StringBuilder();
        if (album != null) res.append(album);
        if (artist != null) res.append(" | ".concat(artist));
        helper.trackArtistAlbum.setText(res.toString());
        Glide.with(context)
                .load(item.getArtworkUri())
                .into(helper.atrworkImageView);
        //helper.addOnClickListener(R.id.imageButton);
    }

    @Override
    public Filter getFilter() {

        return new Filter() {
            List<MusicFile> data = new LinkedList<>();
            List<MusicFile> dataFiltered;

            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                Log.d("COUNT", String.valueOf(dataModelsFULL.size()));
                Log.d("COUNT ADAPTER", String.valueOf(getItemCount()));
                data.addAll(dataModelsFULL);
                String charString = charSequence.toString();
                if (charString.isEmpty() || charString == null) {
                    dataFiltered = data;
                } else {
                    dataFiltered = new LinkedList<>();
                    for (MusicFile row : data) {
                        if (row.getTitle().toLowerCase().contains(charString.toLowerCase())
                                || row.getAlbum().toLowerCase().contains(charString.toLowerCase())
                                || row.getArtist().toLowerCase().contains(charString.toLowerCase())) {
                            dataFiltered.add(row);
                        }
                    }
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = dataFiltered;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                setNewData((List<MusicFile>) filterResults.values);
                ///notifyDataSetChanged();
            }
        };
    }
}
