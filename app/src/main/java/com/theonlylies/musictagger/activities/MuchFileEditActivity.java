package com.theonlylies.musictagger.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.signature.MediaStoreSignature;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionButtonBehavior;
import com.github.clans.fab.FloatingActionMenu;
import com.github.clans.fab.FloatingActionMenuBehavior;
import com.theonlylies.musictagger.R;
import com.theonlylies.musictagger.services.ForegroundTagEditService;
import com.theonlylies.musictagger.utils.GlideApp;
import com.theonlylies.musictagger.utils.MediaStoreUtils;
import com.theonlylies.musictagger.utils.ParcelableMusicFile;
import com.theonlylies.musictagger.utils.adapters.MusicFile;

import java.util.ArrayList;

import static com.theonlylies.musictagger.utils.MediaStoreUtils.GENRES;

/**
 * Created by theonlylies on 05.01.18.
 */

public class MuchFileEditActivity extends AppCompatActivity implements View.OnClickListener {
    FloatingActionMenu menu;

    EditText albumEdit,artistEdit,yearEdit;
    AutoCompleteTextView genreEdit;
    ImageView artworkImageView;
    ParcelableMusicFile musicFile;

    NestedScrollView nestedScrollView;
    AppBarLayout appBarLayout;
    ArrayList<ParcelableMusicFile> musicFiles;

    Uri newArtworkUri;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_muchfileedit);


        albumEdit = findViewById(R.id.albumEdit);
        artistEdit = findViewById(R.id.artistEdit);
        yearEdit = findViewById(R.id.yearEdit);
        genreEdit = findViewById(R.id.genreEdit);
        artworkImageView = findViewById(R.id.artwortImageView);


        nestedScrollView = findViewById(R.id.nestedScrollView);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        /**
         * This Block for FABs init
         */

        final CardView cardView=findViewById(R.id.cardView);

        menu = findViewById(R.id.menu);

        appBarLayout=findViewById(R.id.app_bar);

        FloatingActionMenuBehavior behavior = new FloatingActionMenuBehavior();
        int topInset=getStatusBarHeight(getResources());
        behavior.setTopInset(topInset);

        CoordinatorLayout.LayoutParams params =
                (CoordinatorLayout.LayoutParams) menu.getLayoutParams();
        params.setBehavior(behavior);

        FloatingActionMenu button=findViewById(R.id.fabGroupSmartSearch);
        button.setAlwaysClosed(true);
        button.setOnMenuButtonClickListener(this);
        CoordinatorLayout.LayoutParams paramsButton = (CoordinatorLayout.LayoutParams) button.getLayoutParams();
        paramsButton.setBehavior(behavior);

        /*FloatingActionButton aqua = menu.getMenuMainButton();
        CoordinatorLayout.LayoutParams p = (CoordinatorLayout.LayoutParams) aqua.getLayoutParams();
        p.setBehavior(behaviorButton);*/


        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                int offset = appBarLayout.getTotalScrollRange()- Math.abs(verticalOffset);
                float percentage = (offset/(float)appBarLayout.getTotalScrollRange());
                cardView.setAlpha(percentage);
            }
        });

        /**
         * FAB init block END!
         */


        ArrayList<String> files = getIntent().getStringArrayListExtra("files");
        //Start
        //musicFile = MediaStoreUtils.getMusicFileByPath(path,this);
        //initTagsInterface(musicFile);
        new ReadFromMediaStore().execute(files);
        musicFiles= new ArrayList<>(files.size());
    }

    void initTagsInterface(MusicFile file){
        albumEdit.setText(file.getAlbum());
        artistEdit.setText(file.getArtist());
        yearEdit.setText(file.getYear());
        genreEdit.setText(file.getGenre());
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, GENRES);
        genreEdit.setAdapter(adapter);
        GlideApp.with(this)
                .load(file.getArtworkUri())
                .signature(new MediaStoreSignature("lol",System.currentTimeMillis(),3))
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .error(R.drawable.vector_artwork_placeholder)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(artworkImageView);
        newArtworkUri=file.getArtworkUri();
        //dumpMedia(this);
        //dumpAlbums(this);
    }

    private static int getStatusBarHeight(android.content.res.Resources res) {
        return (int) (24 * res.getDisplayMetrics().density);
    }

    @Override
    public void onBackPressed(){
        if( menu.isOpened() ) {
            menu.close(true);
        }else super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_onefileedit_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        Log.d("id",String.valueOf(id));

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        if(id==R.id.action_save_file){
            saveChanges(musicFile);
            //new WriteChanges().execute(musicFile);
        }
        if(id==android.R.id.home){
            //TODO create return intent with msg for update recyclerView !!!
            finish();
        }
        return super.onOptionsItemSelected(item);
    }


    private static final int REQUEST_CODE_GALLERY_PICK=1;

    @Override
    public void onClick(View v) {

        if(v.getId()==R.id.fabChooseArtworkFromGallery){
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), REQUEST_CODE_GALLERY_PICK);
        }

        if(v.getId()==R.id.fabDeleteArtwork){
            artworkWasDeleted=true;
            GlideApp.with(this).load(R.drawable.vector_artwork_placeholder).error(R.drawable.vector_artwork_placeholder).into(artworkImageView);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data != null && resultCode==AppCompatActivity.RESULT_OK){
            switch (requestCode){
                case REQUEST_CODE_GALLERY_PICK:{
                    newArtworkUri = data.getData();
                    GlideApp.with(this).load(newArtworkUri).centerCrop().error(R.drawable.vector_artwork_placeholder).into(artworkImageView);
                    artWorkWasChanged=true;
                    break;
                }
            }
        }

    }



    /**
     * artWorkWasChanged for check state of artwork change or not fck my eng!
     */

    static boolean artWorkWasChanged=false;
    static boolean artworkWasDeleted=false;

    public void saveChanges(ParcelableMusicFile musicFile){
        musicFile= (ParcelableMusicFile) collectDataFromUI();
        /**
         * Section for album art change if it need !
         */

        //TODO change artwork with rights logic !!!!!
        Intent intent = new Intent(this, ForegroundTagEditService.class);
        intent.putParcelableArrayListExtra("files",musicFiles);
        intent.putExtra("dest_file",musicFile);
        if(artWorkWasChanged){
            intent.putExtra("bitmap",newArtworkUri.toString());
        }else if(artworkWasDeleted){
            intent.putExtra("bitmap","delete");
        }else intent.putExtra("bitmap","nothing");

        startService(intent);


        //All done
    }


    public MusicFile collectDataFromUI(){
        musicFile.setAlbum(albumEdit.getText().toString());
        musicFile.setArtist(artistEdit.getText().toString());
        musicFile.setYear(yearEdit.getText().toString());
        musicFile.setGenre(genreEdit.getText().toString());
        return musicFile;
    }


    class ReadFromMediaStore extends AsyncTask<ArrayList<String>,Void,ArrayList<ParcelableMusicFile>> {

        Context context;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            context=getApplicationContext();
        }

        @Override
        protected ArrayList doInBackground(ArrayList<String >... strings) {
            ArrayList<ParcelableMusicFile> list = new ArrayList<>();
            for(String s : strings[0]){
                list.add( new ParcelableMusicFile( MediaStoreUtils.getMusicFileByPath(s,context) ) );
            }


            //TagManager manager = new TagManager(strings[0]);
            //file.setGenre(manager.getGenre());
            return list;
        }

        @Override
        protected void onPostExecute(ArrayList<ParcelableMusicFile> file) {
            super.onPostExecute(file);
            musicFiles.addAll(file);
            musicFile = musicFiles.get(0);
            initTagsInterface(musicFile);
        }
    }

}
