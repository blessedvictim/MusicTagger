package com.theonlylies.musictagger.activities;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
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
import android.widget.SimpleAdapter;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.signature.MediaStoreSignature;
import com.theonlylies.musictagger.R;
import com.theonlylies.musictagger.services.ForegroundTagEditService;

import com.theonlylies.musictagger.utils.GlideApp;
import com.theonlylies.musictagger.utils.ParcelableMusicFile;
import com.theonlylies.musictagger.utils.adapters.MusicFile;
import com.theonlylies.musictagger.utils.edit.MediaStoreUtils;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.theonlylies.musictagger.utils.edit.MediaStoreUtils.GENRES;

/**
 * Created by theonlylies on 05.01.18.
 */

public class MuchFileEditActivity extends AppCompatActivity implements View.OnClickListener {


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


        appBarLayout = findViewById(R.id.app_bar_much_act);




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

        musicFiles= new ArrayList<>(files.size());
        new ReadFromMediaStore().execute(files);
    }


    //
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


    @Override
    public void onBackPressed(){
        super.onBackPressed();
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


        if(id==R.id.action_save_file){
            saveChanges(musicFile);
            //new WriteChanges().execute(musicFile);
        }
        /*if (id == R.id.action_crop) {
            UCrop.of(musicFile.getArtworkUri(), Uri.fromFile(new File(getCacheDir(), "lol")))
                    .withAspectRatio(1, 1)
                    .withMaxResultSize(800, 800)
                    .start(this);
            return true;
        }*/
        if(id==android.R.id.home){
            //TODO create return intent with msg for update recyclerView !!!
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private static final int REQUEST_CODE_GALLERY_PICK=1;
    private static final int REQUEST_CODE_INTERNET_PICK = 2;

    @Override
    public void onClick(View v) {

        if (v.getId() == R.id.fabImage) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setItems(R.array.image_actions, (dialog, which) -> {
                switch (which) {
                    case 0: {
                        Intent intent = new Intent(Intent.ACTION_PICK);
                        intent.setType("image/*");
                        intent.setAction(Intent.ACTION_GET_CONTENT);
                        startActivityForResult(Intent.createChooser(intent, "Select Picture"), REQUEST_CODE_GALLERY_PICK);
                        break;
                    }
                    case 1: {
                        android.support.v7.app.AlertDialog.Builder builder1 = new android.support.v7.app.AlertDialog.Builder(this);
                        if (this.isOnline()) {
                            Intent intent = new Intent(this, CoverArtGridActivity.class);
                            intent.putExtra("album", this.albumEdit.getText().toString());
                            intent.putExtra("artist", this.artistEdit.getText().toString());

                            builder1.setItems(new CharSequence[]{"MusicBrainz", "LastFM"}, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if (which == 0) intent.putExtra("source", "musicbrainz");
                                    else if (which == 1) intent.putExtra("source", "lastfm");
                                    else return;
                                    startActivityForResult(intent, REQUEST_CODE_INTERNET_PICK);
                                }
                            });
                            builder1.setTitle("Select a source of coverarts");
                            // Create the AlertDialog
                            android.support.v7.app.AlertDialog dialog1 = builder1.create();
                            dialog1.show();
                        } else {
                            builder1.setTitle("");
                            builder1.setMessage("Please turn on internet connection and repeat");
                            android.support.v7.app.AlertDialog dialog1 = builder1.create();
                            dialog1.show();
                        }
                        break;
                    }
                    case 2: {
                        artworkAction = ArtworkAction.DELETED;
                        GlideApp.with(this).load(R.drawable.vector_artwork_placeholder).error(R.drawable.vector_artwork_placeholder).into(artworkImageView);
                        break;
                    }
                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data != null && resultCode==AppCompatActivity.RESULT_OK){
            switch (requestCode){
                case REQUEST_CODE_GALLERY_PICK:{
                    newArtworkUri = data.getData();
                    Log.e("onActivityResult", "GALLERY_PICK");
                    GlideApp.with(this).load(newArtworkUri).centerCrop().error(R.drawable.vector_artwork_placeholder).into(artworkImageView);
                    artworkAction = ArtworkAction.CHANGED;
                    break;
                }
                case REQUEST_CODE_INTERNET_PICK: {
                    Log.e("onActivityResult", "INTERNET_PICK");
                    newArtworkUri = Uri.parse(data.getStringExtra("image"));
                    GlideApp.with(this).load(newArtworkUri).centerCrop().into(artworkImageView);
                    artworkAction = ArtworkAction.CHANGED;
                    break;
                }
            }
        }

    }

    /**
     * artWorkWasChanged for check state of artwork change or not fck my eng!
     */

    static ArtworkAction artworkAction = ArtworkAction.NONE;

    public static String BROADCAST_ACTION = "FOREGROUND_SERVICE_ADMT_FINISH";
    public void saveChanges(ParcelableMusicFile musicFile){
        musicFile= (ParcelableMusicFile) collectDataFromUI();
        /**
         * Section for album art change if it need !
         */

        //TODO change artwork with right logic !!!!! I think i do this !!!!! see service sources !
        Intent intent = new Intent(this, ForegroundTagEditService.class);
        intent.putParcelableArrayListExtra("files",musicFiles);
        intent.putExtra("dest_file",musicFile);
        intent.putExtra("artwork_action", artworkAction);
        Log.e("artworkAction", artworkAction.name());
        intent.putExtra("bitmap", newArtworkUri.toString());


        ProgressDialog dialog = ProgressDialog.show(MuchFileEditActivity.this, "",
                "Changing tags. Please wait...", true);

        BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                dialog.dismiss();
                MuchFileEditActivity.this.setResult(RESULT_OK);
                unregisterReceiver(this);
                MuchFileEditActivity.this.finish();
            }
        };
        // создаем фильтр для BroadcastReceiver
        IntentFilter intFilt = new IntentFilter(BROADCAST_ACTION);
        // регистрируем (включаем) BroadcastReceiver
        registerReceiver(receiver, intFilt);

        startService(intent);
    }

    public boolean isOnline() {
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

    public MusicFile collectDataFromUI(){
        musicFile.setAlbum(albumEdit.getText().toString());
        musicFile.setArtist(artistEdit.getText().toString());
        musicFile.setYear(yearEdit.getText().toString());
        musicFile.setGenre(genreEdit.getText().toString());
        musicFile.setArtworkUri(newArtworkUri);
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
            return list;
        }

        @Override
        protected void onPostExecute(ArrayList<ParcelableMusicFile> file) {
            super.onPostExecute(file);

            musicFiles.add(new ParcelableMusicFile(MusicFile.createEmpty()));
            musicFiles.addAll(file);

            Log.d("musicFile size", String.valueOf(musicFiles.size()));

            //TODO adapter extends ListAdapter and choosing file for tags source
            AlertDialog.Builder builder = new AlertDialog.Builder(MuchFileEditActivity.this);
            List<Map<String, Object>> adapterList = new ArrayList<>();
            for (MusicFile f : musicFiles) {
                Map<String, Object> map = new HashMap<>();
                map.put("title", f.getTitle());
                map.put("album", f.getAlbum());
                if (f.getArtworkUri() != null) map.put("art", f.getArtworkUri());
                else map.put("art", Uri.parse("android.resource://" + context.getPackageName()
                        + "/" + R.drawable.vector_artwork_placeholder));
                Log.e("art_uri", ((Uri) map.get("art")).toString());
                adapterList.add(map);
            }
            // массив имен атрибутов, из которых будут читаться данные
            String[] from = {"title", "album", "art"};
            // массив ID View-компонентов, в которые будут вставлять данные
            int[] to = {R.id.itemSimpleTrNum, R.id.trackAlbum, R.id.artworkImageView};
            SimpleAdapter adapter = new SimpleAdapter(MuchFileEditActivity.this, adapterList, R.layout.item_simple, from, to);
            builder.setSingleChoiceItems(adapter, 0, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    musicFile = musicFiles.get(which);
                    initTagsInterface(musicFile);
                    musicFiles.remove(0);
                    dialog.dismiss();
                }
            });
            builder.setTitle("Select tag donor file");
            builder.setCancelable(false);
            builder.setNegativeButton("Close", (dialog, which) -> {
                MuchFileEditActivity.this.setResult(RESULT_CANCELED);
                finish();
            });

            // Create the AlertDialog
            AlertDialog dialog = builder.create();

            dialog.show();

        }
    }
}
