package com.theonlylies.musictagger.activities;

/**
 * Created by theonlylies on 25.01.18.
 */


import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.theonlylies.musictagger.R;
import com.theonlylies.musictagger.utils.PreferencesManager;
import com.theonlylies.musictagger.utils.adapters.GridViewAdapter;

import java.util.ArrayList;

import Fox.core.lib.general.data.AlbumArtCompilation;
import Fox.core.lib.general.data.Art;
import Fox.core.lib.general.utils.NoMatchesException;
import Fox.core.lib.general.utils.target;
import Fox.core.main.SearchLib;


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
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

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
        if (searchSource.equals("musicbrainz"))
            new AsyncGridImageLoader(searchAlbum, searchArtist, target.MusicBrainz).execute();
        else if (searchSource.equals("lastfm"))
            new AsyncGridImageLoader(searchAlbum, searchArtist, target.LastFM).execute();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    class AsyncGridImageLoader extends AsyncTask<String, AsyncGridImageLoader.Data, Boolean> {
        String album,artist;
        target source;
        //CoverArtSearch arts;
        AlbumArtCompilation arts;
        TextView label;
        ProgressBar progressBar;

        class Data{
            String album,artist,url;
            Data(String album,String artist,String url){
                this.url=url;
                this.album=album;
                this.artist=artist;
            }
        }

        AsyncGridImageLoader(@NonNull String album, String artist, target source) {
            this.album=album;
            this.artist=artist;
            this.source=source;
        }

        AsyncGridImageLoader(AlbumArtCompilation arts){
            this.arts=arts;

        }

        boolean albumartist = true;
        int count;

        RelativeLayout progressLayout;

        @Override
        protected void onPreExecute() {
            Log.d("CoverArtGridActivity","Using target:"+source);

            albumartist = PreferencesManager.getStringValue(CoverArtGridActivity.this, "artwork-rule-term", "albumartist")
                    .equals("albumartist");
            count = Integer.parseInt(PreferencesManager.getStringValue(CoverArtGridActivity.this, "artwork-rule-count", "8"));
            progressLayout = findViewById(R.id.coverProgressLayout);
            label = findViewById(R.id.textLabel);
            progressBar = findViewById(R.id.progressBar);
        }

        @Override
        protected Boolean doInBackground(String... params) {
            try {
                if (albumartist)
                    arts = SearchLib.SearchCovers(album,
                            artist,
                            source,
                            count
                    );
                else
                    arts = SearchLib.SearchCovers(album,
                            null,
                            source,
                            count
                    );

                    for(Art q : arts.getArtList()){
                        try {
                            Log.d("CoverArtGridActivity","url="+q.getUrl());
                            publishProgress(new Data(q.getAlbum(),q.getArtist(),q.getUrl()));
                        } catch (Exception e) {
                            e.printStackTrace();
                        };

                }
            } catch (NoMatchesException | IllegalArgumentException e) {
                e.printStackTrace();
                return false;
            }


            return true;
        }

        @Override
        protected void onProgressUpdate(Data... values) {
            progressLayout.setVisibility(View.GONE);
            gridAdapter.addData(values[0].album,values[0].artist,values[0].url);
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(Boolean status) {
            Log.d("end","end");
            if (!status) {
                progressBar.setVisibility(View.INVISIBLE);
                label.setText("Sorry,nothing found");
            }
        }



    }

}