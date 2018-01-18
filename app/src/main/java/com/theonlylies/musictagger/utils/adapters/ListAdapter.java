package com.theonlylies.musictagger.utils.adapters;

import android.content.Context;
import android.util.Log;
import android.widget.Filter;
import android.widget.Filterable;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.signature.MediaStoreSignature;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.theonlylies.musictagger.R;
import com.theonlylies.musictagger.utils.GlideApp;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by linuxoid on 17.12.17.
 */

public class ListAdapter extends BaseQuickAdapter<MusicFile, ViewHolder>
        implements Filterable {

    private ArrayList<Integer> selectedPositions = new ArrayList<>();

    private boolean isMultiselect=false;

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

    public void toogleSelected(int position){
        ViewHolder viewHolder=(ViewHolder)getRecyclerView().findViewHolderForAdapterPosition(position);
        if(selectedPositions.contains(position)){
            selectedPositions.remove(Integer.valueOf(position));
            if(viewHolder!=null)viewHolder.setSelected(false);
        }else{
            selectedPositions.add(position);
            if(viewHolder!=null)viewHolder.setSelected(true);
        }
    }

    public int getSelectedCount(){
        return selectedPositions.size();
    }

    public List<MusicFile> getSelectedFiles(){
        if(!isMultiselect)return null;
        List<MusicFile> files = new ArrayList<>();
        for (Integer i : selectedPositions){
            files.add( getData().get(i) );
        }
        return files;
    }

    @Override
    protected void convert(ViewHolder helper, MusicFile item,int position) {
        helper.trackTitle.setText(item.getTitle());
        String album = item.getAlbum();
        String artist = item.getArtist();
        StringBuilder res = new StringBuilder();
        if (album != null) res.append(album);
        if (artist != null) res.append(" | ".concat(artist));
        helper.trackArtistAlbum.setText(res.toString());
        GlideApp.with(context)
                .load(item.getArtworkUri())
                .signature(new MediaStoreSignature("lol",System.currentTimeMillis(),3))
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .error(R.drawable.vector_artwork_placeholder)
                .into(helper.atrworkImageView);

        if(selectedPositions.contains(position)){
            helper.setSelected(true);
        }else {
            helper.setSelected(false);
        }

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

    public boolean isMultiselect() {
        return isMultiselect;
    }

    public void setMultiselect(boolean multiselect) {
        isMultiselect = multiselect;
        if(!isMultiselect){
            ArrayList<Integer> copy = new ArrayList<>(selectedPositions);
            for (Integer i : copy){
                toogleSelected(i);
            }
            selectedPositions.clear();
        }
    }
}
