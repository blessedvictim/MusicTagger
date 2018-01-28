package com.theonlylies.musictagger.activities;

/**
 * Created by theonlylies on 25.01.18.
 */


import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
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
import Fox.core.lib.general.utils.NoMatchesException;
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


    class AsyncGridImageLoader extends AsyncTask<String, AsyncGridImageLoader.Data, Boolean> {
        String album,artist;
        target source;
        int size;
        //CoverArtSearch arts;
        AlbumArtCompilation arts;
        ProgressDialog dialog;

        class Data{
            String album,artist,url;
            Data(String album,String artist,String url){
                this.url=url;
                this.album=album;
                this.artist=artist;
            }
        }

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

            try {
                arts = art.run(album,
                        null,
                        source,
                        size
                );

                    for(Art q : arts.getArtList()){
                        try {
                            Log.d("CoverArtGridActivity","url="+q.getUrl());
                            publishProgress(new Data(q.getAlbum(),q.getArtist(),q.getUrl()));

                        } catch (Exception e) {
                            e.printStackTrace();
                        };

                }
            } catch (NoMatchesException e) {
                AlertDialog.Builder builder = new AlertDialog.Builder(CoverArtGridActivity.this);
                builder.setPositiveButton("understand", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        CoverArtGridActivity.this.finish();
                    }
                });
                builder.setTitle("Error");
                builder.setMessage("No matched finded");
                // Create the AlertDialog
                AlertDialog dialog = builder.create();
                dialog.show();
                e.printStackTrace();
            }



            return true;
        }

        @Override
        protected void onProgressUpdate(Data... values) {
            gridAdapter.addData(values[0].album,values[0].artist,values[0].url);
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(Boolean status) {
            Log.d("end","end");
            dialog.dismiss();
        }



    }

}