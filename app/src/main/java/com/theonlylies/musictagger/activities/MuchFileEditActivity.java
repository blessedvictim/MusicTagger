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
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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
import android.widget.Toast;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.signature.MediaStoreSignature;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.theonlylies.musictagger.R;
import com.theonlylies.musictagger.services.ForegroundTagEditService;
import com.theonlylies.musictagger.utils.GlideApp;
import com.theonlylies.musictagger.utils.PreferencesManager;
import com.theonlylies.musictagger.utils.adapters.ListAdapter;
import com.theonlylies.musictagger.utils.adapters.MusicFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java8.util.stream.Collectors;
import java8.util.stream.StreamSupport;

import static com.theonlylies.musictagger.utils.edit.MediaStoreUtils.GENRES;

/**
 * Created by theonlylies on 05.01.18.
 */

public class MuchFileEditActivity extends AppCompatActivity implements View.OnClickListener,BaseQuickAdapter.OnItemClickListener {


    EditText albumEdit, artistEdit, yearEdit, composerEdit, discNumEdit, commentEdit;
    AutoCompleteTextView genreEdit;
    ImageView artworkImageView;
    MusicFile musicFile;
    List<MusicFile> allFiles;

    NestedScrollView nestedScrollView;
    AppBarLayout appBarLayout;
    ArrayList<MusicFile> musicFiles;

    RecyclerView selectedRecyclerView;
    ListAdapter selectedAdapter;

    Uri newArtworkUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_muchfileedit);


        albumEdit = findViewById(R.id.albumEdit);
        artistEdit = findViewById(R.id.artistEdit);
        yearEdit = findViewById(R.id.yearEdit);
        genreEdit = findViewById(R.id.genreEdit);
        commentEdit = findViewById(R.id.commentEdit);
        composerEdit = findViewById(R.id.composerEdit);
        discNumEdit = findViewById(R.id.discNumberEdit);

        artworkImageView = findViewById(R.id.artwortImageView);
        selectedRecyclerView = findViewById(R.id.selectedRecyclerView);

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


        //ArrayList<String> files = getIntent().getStringArrayListExtra("files");
        allFiles = getIntent().getParcelableArrayListExtra("files");
        musicFiles = new ArrayList<>(allFiles.size());
        selectedAdapter=new ListAdapter(R.layout.item_simple,this);
        selectedRecyclerView.setAdapter(selectedAdapter);
        selectedRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        selectedAdapter.setOnItemClickListener(this);
        selectedAdapter.bindToRecyclerView(selectedRecyclerView);

        showDialog(allFiles);
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


        // if artworks different change to setup donor
        if (StreamSupport.stream(allFiles).map((f) -> f.getArtworkUri()).distinct().collect(Collectors.toList()).size() > 1 && file.getArtworkUri() != null) {
            artworkAction = ArtworkAction.CHANGED;
        }
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
            String album=albumEdit.getText().toString();
            String artist=artistEdit.getText().toString();
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
                        boolean albumartist = PreferencesManager.getStringValue(MuchFileEditActivity.this, "artwork-rule-term", "albumartist")
                                .equals("albumartist");
                        Log.e("albumartist", String.valueOf(albumartist));
                        if(albumartist && (album==null || artist == null || (album.isEmpty() || artist.isEmpty()))){
                            Toast.makeText(this,"Album or Artist is empty, fill in first",Toast.LENGTH_SHORT).show();
                            return;
                        }
                        if(albumartist==false && (album==null  || album.isEmpty()) ){
                            Toast.makeText(this,"Album is empty, fill in first",Toast.LENGTH_SHORT).show();
                            return;
                        }
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

    public void saveChanges(MusicFile musicFile) {
        musicFile = (MusicFile) collectDataFromUI();
        /**
         * Section for album art change if it need !
         */

        //TODO change artwork with right logic !!!!! I think i do this !!!!! see service sources !
        Intent intent = new Intent(this, ForegroundTagEditService.class);
        intent.putParcelableArrayListExtra("files", musicFiles);
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
        ///musicFile.setArtworkUri(newArtworkUri); //FIXME странный фрагмент

        musicFile.setAlbum(albumEdit.getText().toString());
        musicFile.setArtist(artistEdit.getText().toString());
        musicFile.setYear(yearEdit.getText().toString());
        musicFile.setGenre(genreEdit.getText().toString());
        musicFile.setDiscNumber(discNumEdit.getText().toString());
        musicFile.setComment(commentEdit.getText().toString());
        musicFile.setComposer(composerEdit.getText().toString());
        return musicFile;
    }

    @Override
    public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
        if (adapter instanceof ListAdapter){
            initTagsInterface(musicFiles.get(position));
            selectedAdapter.changeSelected(position);
        }
    }

    void showDialog(List<MusicFile> file) {
        musicFiles.add(MusicFile.createEmpty(MuchFileEditActivity.this));
        musicFiles.addAll(file);
        List<MusicFile> files = new ArrayList<>();
        files.addAll(musicFiles);
        files.remove(0);
        selectedAdapter.setNewData(files);

        Log.d("musicFile size", String.valueOf(musicFiles.size()));

        AlertDialog.Builder builder = new AlertDialog.Builder(MuchFileEditActivity.this);
        List<Map<String, Object>> adapterList = new ArrayList<>();
        for (MusicFile f : musicFiles) {
            Map<String, Object> map = new HashMap<>();
            map.put("title", f.getTitle());
            map.put("album", f.getAlbum());
            Log.e("art_uri", f.getArtworkUri().toString());
            if (f.getArtworkUri() != null) map.put("art", f.getArtworkUri());
            else
                map.put("art", Uri.parse("android.resource://" + this.getPackageName() + "/" + R.drawable.vector_artwork_placeholder));

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
                if (which > 0) selectedAdapter.changeSelected(which - 1);
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
