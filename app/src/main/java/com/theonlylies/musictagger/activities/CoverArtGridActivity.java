package com.theonlylies.musictagger.activities;

/**
 * Created by theonlylies on 25.01.18.
 */


import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.theonlylies.musictagger.R;
import com.theonlylies.musictagger.utils.GlideApp;
import com.theonlylies.musictagger.utils.adapters.GridViewAdapter;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import Fox.core.lib.general.DOM.AlbumArtCompilation;
import Fox.core.lib.general.DOM.Art;
import Fox.core.lib.general.utils.target;
import Fox.core.main.CoverArtSearch;

public class CoverArtGridActivity extends AppCompatActivity {

    private GridView gridView;
    private GridViewAdapter gridAdapter;
    private ArrayList<String> images_urls;
    private ArrayList<String> images_names;
    private ArrayList<Bitmap> images;
    int pos = 0;

    RelativeLayout progress;

    Context ctx;
    ImageView image;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grid_gallery);
        gridView = (GridView) findViewById(R.id.gridView);


        String searchAlbum = getIntent().getStringExtra("album");
        String searchArtist = getIntent().getStringExtra("artist");
        String searchSource = getIntent().getStringExtra("source");


        ctx = this;

        Log.d("album",searchAlbum);
        Log.d("artist",searchArtist);




        gridAdapter = new GridViewAdapter(this, R.layout.item_grid_gallery);
        gridView.setNumColumns(2);
        gridView.setAdapter(gridAdapter);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                gridAdapter.getUrl(position);
                Intent intent = new Intent();
                intent.putExtra("image", gridAdapter.getUrl(position));
                setResult(RESULT_OK, intent);
                finish();

            }
        });
        if(searchSource.equals("musicbrainz")) new AsyncGridImageLoader(searchAlbum,searchArtist,target.MusicBrainz,12).execute();
        else if(searchSource.equals("lastfm")) new AsyncGridImageLoader(searchAlbum,searchArtist,target.LastFM,12).execute();
    }


    public View getViewByPosition(int pos, GridView listView) {
        final int firstListItemPosition = listView.getFirstVisiblePosition();
        final int lastListItemPosition = firstListItemPosition + listView.getChildCount() - 1;

        if (pos < firstListItemPosition || pos > lastListItemPosition) {
            return listView.getAdapter().getView(pos, null, listView);
        } else {
            final int childIndex = pos - firstListItemPosition;
            return listView.getChildAt(childIndex);
        }
    }


    class AsyncGridImageLoader extends AsyncTask<String, String , Boolean> {
        String album,artist;
        target source;
        int size;
        //CoverArtSearch arts;
        AlbumArtCompilation arts;
        ProgressDialog dialog;
        AsyncGridImageLoader(@NonNull String album, String artist, target source, int size){
            this.album=album;
            this.artist=artist;
            this.source=source;
            this.size = size;
        }

        AsyncGridImageLoader(AlbumArtCompilation arts){
            this.arts=arts;

        }

        @Override
        protected void onPreExecute() {
            Log.d("CoverArtGridActivity","Using target:"+source);
            dialog = ProgressDialog.show(CoverArtGridActivity.this, "",
                    "Loading. Please wait...", true);

        }

        @Override
        protected Boolean doInBackground(String... params) {
            CoverArtSearch art= new CoverArtSearch();

            arts = art.run(album,
                    null,
                    source,
                    size
            );

            if(arts.hasArtList()){
                for(Art q : arts.getArtList()){
                    try {
                        Log.d("CoverArtGridActivity","url="+q.getUrl());
                        publishProgress(q.getUrl());

                    } catch (Exception e) {
                        e.printStackTrace();
                    };
                    //
                }
            }
            else Toast.makeText(ctx,"LOX",Toast.LENGTH_SHORT).show();

            return true;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            gridAdapter.addData("LOL","lel",values[0]);
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(Boolean status) {
            Log.d("end","end");
            dialog.dismiss();
        }



    }

}