package com.theonlylies.musictagger.activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.drawable.VectorDrawable;
import android.media.MediaPlayer;
import android.media.MediaScannerConnection;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.signature.MediaStoreSignature;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.github.clans.fab.FloatingActionMenu;
import com.github.clans.fab.FloatingActionMenuBehavior;
import com.theonlylies.musictagger.R;
import com.theonlylies.musictagger.utils.FileUtil;
import com.theonlylies.musictagger.utils.GlideApp;
import com.theonlylies.musictagger.utils.MusicCache;
import com.theonlylies.musictagger.utils.PreferencesManager;
import com.theonlylies.musictagger.utils.adapters.ListAdapter;
import com.theonlylies.musictagger.utils.adapters.MusicFile;
import com.theonlylies.musictagger.utils.edit.MediaStoreUtils;
import com.theonlylies.musictagger.utils.edit.TagManager;
import com.yalantis.ucrop.UCrop;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import Fox.core.lib.general.DOM.FingerPrint;
import Fox.core.lib.general.DOM.ID3V2;
import Fox.core.lib.general.templates.FingerPrintThread;
import Fox.core.lib.general.templates.ProgressState;
import Fox.core.lib.general.utils.FingerPrintProcessingException;
import Fox.core.lib.general.utils.NoAccessingFilesException;
import Fox.core.lib.general.utils.NoMatchesException;
import Fox.core.lib.general.utils.ProgressStateException;
import Fox.core.main.SearchLib;
import Fox.test.util.CustomProgressState;

import static com.theonlylies.musictagger.activities.SplashActivity.fpCalc;
import static com.theonlylies.musictagger.utils.edit.MediaStoreUtils.GENRES;
import static com.theonlylies.musictagger.utils.edit.MediaStoreUtils.dumpAlbums;

public class OneFileEditActivity extends AppCompatActivity implements View.OnClickListener {

    FloatingActionMenu menu, playMenu;

    EditText titleEdit, albumEdit, artistEdit, yearEdit, trackNumberEdit;
    AutoCompleteTextView genreEdit;
    ImageView artworkImageView, bestMatchArtworkImageView;
    TextView bestMatchAlbumTextView, bestMatchArtistTextView, bestMatchTitleTextView;
    TextView musicFilePathView;
    MusicFile musicFile;

    CardView cardBestSearched;
    NestedScrollView nestedScrollView;
    AppBarLayout appBarLayout;
    Context context;

    Uri newArtworkUri;

    SwitchCompat switchRename;

    boolean smartSearch = false;


    //DoConnect smartSearchTask;

    @Override
    protected void onDestroy() {
        Log.d("onDestroy", "syka");
        //if(smartSearchTask!=null) smartSearchTask.cancel(true);
        if (mp3Play != null && mp3Play.isPlaying()) mp3Play.reset();
        Log.d("onDestroy", "syka2");
        super.onDestroy();
    }

    VectorDrawable drawPlay, drawPause;

    MediaPlayer mp3Play = new MediaPlayer();
    boolean play = false;

    void swapAnimationOfplayer() {

        if (!play) {
            try {
                mp3Play.setDataSource(this, Uri.parse(this.musicFile.getRealPath()));
                mp3Play.prepare();
                mp3Play.setLooping(false);
                mp3Play.start();
                playMenu.getMenuMainButton().setImageDrawable(drawPause);
            } catch (IOException e) {
                e.printStackTrace();
            }

        } else {
            if (mp3Play.isPlaying()) {
                mp3Play.stop();
                mp3Play.reset();
            }

            playMenu.getMenuMainButton().setImageDrawable(drawPlay);
        }
        play = !play;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onefiledit);
        context = this;

        titleEdit = findViewById(R.id.titleEdit);
        albumEdit = findViewById(R.id.albumEdit);
        artistEdit = findViewById(R.id.artistEdit);
        yearEdit = findViewById(R.id.yearEdit);
        genreEdit = findViewById(R.id.genreEdit);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, GENRES);
        genreEdit.setAdapter(adapter);
        trackNumberEdit = findViewById(R.id.trackNumEdit);
        artworkImageView = findViewById(R.id.artwortImageView);
        musicFilePathView = findViewById(R.id.musicFilePath);

        bestMatchArtworkImageView = findViewById(R.id.bestMathcArtworkImageView);
        bestMatchAlbumTextView = findViewById(R.id.bestMatchAlbumText);
        bestMatchArtistTextView = findViewById(R.id.bestMatchArtistText);
        bestMatchTitleTextView = findViewById(R.id.bestMatchTitleText);

        cardBestSearched = findViewById(R.id.cardSearched).findViewById(R.id.cardBestSearched);
        cardBestSearched.setOnClickListener(this);
        nestedScrollView = findViewById(R.id.nestedScrollView);

        switchRename = findViewById(R.id.switchFileRename);
        if (!PreferencesManager.getStringValue(this, "rename-rule", "4").equals("4")) {
            switchRename.setChecked(true);
        } else switchRename.setEnabled(false);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        /*
         * This Block for FABs init
         */

        final CardView cardView = findViewById(R.id.cardView);

        menu = findViewById(R.id.menu);

        appBarLayout = findViewById(R.id.app_bar);

        FloatingActionMenuBehavior behavior = new FloatingActionMenuBehavior();
        int topInset = getStatusBarHeight(getResources());
        behavior.setTopInset(topInset);

        CoordinatorLayout.LayoutParams params =
                (CoordinatorLayout.LayoutParams) menu.getLayoutParams();
        params.setBehavior(behavior);

        FloatingActionMenu button = findViewById(R.id.fabSmartSearch);
        button.setAlwaysClosed(true);
        button.setOnMenuButtonClickListener(this);
        CoordinatorLayout.LayoutParams paramsButton = (CoordinatorLayout.LayoutParams) button.getLayoutParams();
        paramsButton.setBehavior(behavior);

        playMenu = findViewById(R.id.fabPlayer);
        playMenu.setAlwaysClosed(true);
        drawPlay = (VectorDrawable) ContextCompat.getDrawable(this, R.drawable.ic_play_icon);
        drawPause = (VectorDrawable) ContextCompat.getDrawable(this, R.drawable.ic_pause_icon);
        playMenu.getMenuMainButton().setImageDrawable(drawPlay);
        playMenu.setOnMenuButtonClickListener(this);
        CoordinatorLayout.LayoutParams paramsPlayButton = (CoordinatorLayout.LayoutParams) playMenu.getLayoutParams();
        paramsPlayButton.setBehavior(behavior);

        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                int offset = appBarLayout.getTotalScrollRange() - Math.abs(verticalOffset);
                float percentage = (offset / (float) appBarLayout.getTotalScrollRange());
                cardView.setAlpha(percentage);
            }
        });

        /**
         * FAB init block END!
         */


        String path = getIntent().getStringExtra("music_file_path");
        //Start
        //musicFile = MediaStoreUtils.getMusicFileByPath(path,this);
        //initTagsInterface(musicFile);
        new ReadFromMediaStore().execute(path);
    }

    void initTagsInterface(MusicFile file) {
        titleEdit.setText(file.getTitle());
        albumEdit.setText(file.getAlbum());
        artistEdit.setText(file.getArtist());
        yearEdit.setText(file.getYear());
        genreEdit.setText(file.getGenre());
        // addititional info
        musicFilePathView.setText(file.getRealPath());
        trackNumberEdit.setText(file.getTrackNumber());
        reloadImage(file);
        musicFile = file;
        //dumpMedia(this);
        //dumpAlbums(this);
    }

    private void reloadImage(MusicFile file) {
        GlideApp.with(this)
                .load(file.getArtworkUri())
                .signature(new MediaStoreSignature("lol", System.currentTimeMillis(), 3))
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .error(R.drawable.vector_artwork_placeholder)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(artworkImageView);
    }

    private static int getStatusBarHeight(android.content.res.Resources res) {
        return (int) (24 * res.getDisplayMetrics().density);
    }

    @Override
    public void onBackPressed() {
        if (menu.isOpened()) {
            menu.close(true);
        } else super.onBackPressed();
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

        Log.d("id", String.valueOf(id));

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_crop) {
            UCrop.of(musicFile.getArtworkUri(), Uri.fromFile(new File(getCacheDir(), "lol")))
                    .withAspectRatio(1, 1)
                    .withMaxResultSize(800, 800)
                    .start(this);
            return true;
        }
        if (id == R.id.action_save_file) {
            //saveChanges(musicFile);
            new WriteChanges().execute(musicFile);
        }
        if (id == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private static final int REQUEST_CODE_GALLERY_PICK = 1;
    private static final int REQUEST_CODE_INTERNET_PICK = 2;

    @Override
    public void onClick(View v) {

        if (v.getId() == R.id.fabSmartSearch) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            if (this.isOnline()) {
                Log.d("sd", "azazaazzaz");
                //smartSearchTask = new DoConnect();
                //smartSearchTask.execute(musicFile.getRealPath());
                new SmartSearchTask().execute(musicFile.getRealPath());
                final Rect rect = new Rect(0, 0, cardBestSearched.getWidth(), cardBestSearched.getHeight());
                cardBestSearched.requestRectangleOnScreen(rect, false);
                /*builder.setTitle("");
                builder.setMessage("867-5309");
                AlertDialog dialog = builder.create();
                dialog.show();*/
            } else {
                builder.setTitle("");
                builder.setMessage("Please turn on internet connection and repeat");
                AlertDialog dialog = builder.create();
                dialog.show();
            }

        }
        if (v.getId() == R.id.cardBestSearched) {
            if (!smartSearch) {
                if (this.isOnline()) {
                    Log.d("sd", "azazaazzaz");
                    new SmartSearchTask().execute(musicFile.getRealPath());
                    smartSearch = true;
                } else {
                    Toast.makeText(this, "Pls turn on internet!", Toast.LENGTH_SHORT).show();
                }
            } else {
                MusicFile file = (MusicFile) v.getTag();
                if (file != null) {
                    file.setAlbum_id(musicFile.getAlbum_id());
                    file.setRealPath(musicFile.getRealPath());
                    if (file.getArtworkUri() != null) artWorkWasChanged = true;
                    //else artworkWasDeleted=true;
                    initTagsInterface(file);

                }
            }

            Toast.makeText(this, "card best match clicked!", Toast.LENGTH_SHORT).show();
        }


        if (v.getId() == R.id.fabPlayer) {

            swapAnimationOfplayer();
        }

        if (v.getId() == R.id.fabChooseArtworkFromGallery || v.getId() == R.id.artwortImageView) {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), REQUEST_CODE_GALLERY_PICK);
        }

        if (v.getId() == R.id.fabDeleteArtwork) {
            artworkWasDeleted = true;
            GlideApp.with(this).load(R.drawable.vector_artwork_placeholder).error(R.drawable.vector_artwork_placeholder).into(artworkImageView);
        }

        if (v.getId() == R.id.fabChooseArtworkFromInternet) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            if (this.isOnline()) {
                Intent intent = new Intent(this, CoverArtGridActivity.class);
                intent.putExtra("album", this.albumEdit.getText().toString());
                intent.putExtra("artist", this.artistEdit.getText().toString());

                builder.setItems(new CharSequence[]{"MusicBrainz", "LastFM"}, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) intent.putExtra("source", "musicbrainz");
                        else if (which == 1) intent.putExtra("source", "lastfm");
                        else return;
                        startActivityForResult(intent, REQUEST_CODE_INTERNET_PICK);
                    }
                });
                builder.setTitle("Select a source of coverarts");
                // Create the AlertDialog
                AlertDialog dialog = builder.create();
                dialog.show();
            } else {
                builder.setTitle("");
                builder.setMessage("Please turn on internet connection and repeat");
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (menu.isOpened()) menu.close(false);
        if (data != null && resultCode == AppCompatActivity.RESULT_OK) {
            switch (requestCode) {
                case REQUEST_CODE_GALLERY_PICK: {
                    newArtworkUri = data.getData();
                    GlideApp.with(this).load(newArtworkUri).centerCrop().error(R.drawable.vector_artwork_placeholder).into(artworkImageView);
                    artWorkWasChanged = true;
                    break;
                }
                case REQUEST_CODE_INTERNET_PICK: {
                    newArtworkUri = Uri.parse(data.getStringExtra("image"));
                    GlideApp.with(this).load(newArtworkUri).centerCrop().error(R.drawable.vector_artwork_placeholder).into(artworkImageView);
                    artWorkWasChanged = true;
                    break;
                }
                case UCrop.REQUEST_CROP: {
                    final Uri resultUri = UCrop.getOutput(data);
                    MusicFile file = new MusicFile();
                    file.setArtworkUri(resultUri);
                    reloadImage(file);
                }
            }
        }

    }

    /**
     * artWorkWasChanged for check state of artwork change or not fck my eng!
     */

    static boolean artWorkWasChanged = false;
    static boolean artworkWasDeleted = false;

    void updateMusicFile(MusicFile file) {
        musicFile = file;
    }

    public MusicFile collectDataFromUI() {
        musicFile.setTitle(titleEdit.getText().toString());
        musicFile.setAlbum(albumEdit.getText().toString());
        musicFile.setArtist(artistEdit.getText().toString());
        musicFile.setYear(yearEdit.getText().toString());
        musicFile.setTrackNumber(trackNumberEdit.getText().toString());
        musicFile.setGenre(genreEdit.getText().toString());
        return musicFile;
    }

    public boolean saveChanges(final MusicFile musicFile) {
        Bitmap bitmap = null;
        if (!FileUtil.fileOnSdCard(new File(musicFile.getRealPath()))) {
            Log.d("OneFileEdit", "file in internal storage!...");
            TagManager tagManager = new TagManager(musicFile.getRealPath());
            tagManager.setTagsFromMusicFile(
                    collectDataFromUI());
            if (artWorkWasChanged) {
                //((BitmapDrawable)artworkImageView.getDrawable()).getBitmap(); // OLD METHOD

                try {
                    bitmap = GlideApp.with(getApplicationContext())
                            .asBitmap()
                            .load(musicFile.getArtworkUri())
                            .submit()
                            .get();
                    tagManager.setArtwork(bitmap);
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }


            } else if (artworkWasDeleted) {
                tagManager.deleteArtwork();
            }
            tagManager.save();
        } else if (FileUtil.fileOnSdCard(new File(musicFile.getRealPath())) &&
                FileUtil.canWriteThisFileSAF(this, musicFile.getRealPath())) {
            Log.d("OneFileEdit", "file on sdcard ! EDITITNG!");
            MusicCache cache = new MusicCache(this);
            try {
                File file = cache.cacheMusicFile(new File(musicFile.getRealPath()));
                TagManager tagManager = new TagManager(file.getPath());
                tagManager.setTagsFromMusicFile(
                        collectDataFromUI());
                if (artWorkWasChanged) {

                    try {
                        bitmap = GlideApp.with(getApplicationContext())
                                .asBitmap()
                                .load(musicFile.getArtworkUri())
                                .submit()
                                .get();
                        tagManager.setArtwork(bitmap);
                    } catch (InterruptedException | ExecutionException e) {
                        e.printStackTrace();
                    }

                    tagManager.setArtwork(bitmap);
                } else if (artworkWasDeleted) {
                    tagManager.deleteArtwork();
                }
                tagManager.save();
                cache.replaceCache();
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else {
            Log.d("OneFileEdit", "fuck you sd card access");
            //Toast.makeText(this,"fuck you sd card access",Toast.LENGTH_SHORT).show();
            return false;
        }

        // Работа с Media Store
        final long album_id = musicFile.getAlbum_id();

        //dumpMedia(getApplicationContext());
        dumpAlbums(getApplicationContext());

        // TODO renaming for sdcard files !
        PreferencesManager.RenameRules rule = PreferencesManager.
                RenameRules.values()[Integer.parseInt(PreferencesManager.getStringValue(this, "rename-rule", "4"))];

        if (!switchRename.isChecked()) rule = PreferencesManager.RenameRules.none;

        Bitmap finalBitmap = bitmap;
        MediaStoreUtils.updateFileMediaStoreMedia(musicFile, this, rule, new MediaScannerConnection.OnScanCompletedListener() {
            @Override
            public void onScanCompleted(String path, Uri uri) {
                Log.i("ExternalStorage", "Scanned " + path + ":");
                Log.i("ExternalStorage", "-> uri=" + uri);
                //dumpMedia(getApplicationContext());
                dumpAlbums(getApplicationContext());
                updateMusicFile(MediaStoreUtils.getMusicFileByPath(musicFile.getRealPath(), getApplicationContext())); // interface update
                if (musicFile.getAlbum_id() == album_id) {
                    if (artWorkWasChanged) {
                        //Bitmap bitmap = ((BitmapDrawable) artworkImageView.getDrawable()).getBitmap();
                        Log.d("setArtwork()", String.valueOf(MediaStoreUtils.setAlbumArt(finalBitmap, getApplicationContext(), musicFile.getAlbum_id())));
                    } else if (artworkWasDeleted) {
                        MediaStoreUtils.deleteAlbumArt(getApplicationContext(), musicFile.getAlbum_id());
                    }
                }
            }
        });

        //All done
        return true;
    }

    /**
     * @return internet connection status
     */
    public boolean isOnline() {
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

    class WriteChanges extends AsyncTask<MusicFile, Void, Boolean> {
        AlertDialog dialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            AlertDialog.Builder builder = new AlertDialog.Builder(OneFileEditActivity.this);
            builder.setTitle("Changes writing...");
            builder.setCancelable(false);
            dialog = builder.create();
            dialog.show();

        }

        @Override
        protected Boolean doInBackground(MusicFile... files) {
            return saveChanges(files[0]);
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            dialog.dismiss();
            if (!aBoolean) {
                Toast.makeText(OneFileEditActivity.this,
                        "You provide bad SD-Card path permission,please setup right path in settings and try again", Toast.LENGTH_LONG).show();
            }
        }
    }

    class ReadFromMediaStore extends AsyncTask<String, Void, MusicFile> {

        Context context;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            context = getApplicationContext();
        }

        @Override
        protected MusicFile doInBackground(String... strings) {
            MusicFile file = MediaStoreUtils.getMusicFileByPath(strings[0], context);
            TagManager manager = new TagManager(strings[0]);
            file.setGenre(manager.getGenre());
            return file;
        }

        @Override
        protected void onPostExecute(MusicFile file) {
            super.onPostExecute(file);
            initTagsInterface(file);
            musicFile = file;
        }
    }

    class SmartSearchTask extends AsyncTask<String, Void, List<MusicFile>> {
        class FpcaltThread implements FingerPrintThread {

            static final String CONST_FINGERPRINT = "FINGERPRINT=";
            static final String CONST_DURATION = "DURATION=";

            public Map<String, String> fpcalc(String in) {

                Log.d("fpCacl", "my url: " + in);

                String[] args = {in};// {"-version"};// { in};
                String result = fpCalc(args);
                Map<String, String> map = new HashMap<>();
                String[] parts = result.split("\n");
                String f = null;
                Integer d = null;
                for (int i = 0; i < parts.length; i++) {
                    if (parts[i].startsWith(CONST_FINGERPRINT))
                        f = parts[i].substring(CONST_FINGERPRINT.length());
                    if (parts[i].startsWith(CONST_DURATION))
                        d = Integer.parseInt(parts[i].substring(CONST_DURATION.length()));
                }
                Log.d("f", f);
                Log.d("d", String.valueOf(d));
                map.put("fingerprint", f);
                map.put("duration", d.toString());
                return map;
            }

            @Override
            public FingerPrint getFingerPrint(@NotNull String s) throws FingerPrintProcessingException {
                Map<String, String> map = new HashMap<>();
                map = fpcalc(s);
                return new FingerPrint(map.get("fingerprint"), map.get("duration"), "LOL");
            }
        }

        Context context;
        String bmp = null;
        RecyclerView recyclerView;
        ListAdapter adapter;
        ConstraintLayout searchedView;
        CardView cardOthersSearched, cardBestSearch;
        ConstraintLayout progressHolderLayout, bestSearchHolderLayout;
        ProgressBar bar;
        TextView textProgress;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            System.setProperty("setprop.log.tag.SearchLib.class", "DEBUG");
            context = getApplicationContext();
            progressHolderLayout = findViewById(R.id.progressHolderLayout);
            bestSearchHolderLayout = findViewById(R.id.bestSearchholderLayout);
            recyclerView = findViewById(R.id.otherMatchesRecyclerView);
            cardOthersSearched = findViewById(R.id.cardOthersSearched);
            searchedView = findViewById(R.id.cardSearched);
            searchedView.setVisibility(View.VISIBLE);

            textProgress = findViewById(R.id.progressSearchTextView);
            textProgress.setText("Data loading ...");
            bar = findViewById(R.id.smartSearchProgressBar);
            bar.setVisibility(View.VISIBLE);
            //qwerty
            LinearLayoutManager layoutManager = new LinearLayoutManager(context);
            recyclerView.setLayoutManager(layoutManager);
            adapter = new ListAdapter(R.layout.item_simple_matches, context);
            adapter.openLoadAnimation(BaseQuickAdapter.ALPHAIN);
            adapter.isFirstOnly(false);
            adapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                    //updateMusicFile((MusicFile) adapter.getData().get(position));
                    MusicFile file = (MusicFile) adapter.getData().get(position);
                    file.setAlbum_id(musicFile.getAlbum_id());
                    file.setRealPath(musicFile.getRealPath());
                    OneFileEditActivity.this.initTagsInterface(file);
                }
            });
            adapter.bindToRecyclerView(recyclerView);
        }

        @Override
        protected List<MusicFile> doInBackground(String... params) {
            Map<String, List<ID3V2>> result = null;
            ArrayList<String> list = new ArrayList<>();
            list.add(params[0]);
            try {
                ProgressState Line4 = new CustomProgressState(0, "Common", "All progress");
                result = SearchLib.SearchTags(params[0], new FpcaltThread(), Line4, false, 4);
                ArrayList<MusicFile> data = new ArrayList(result.get("LOL").size());
                for (ID3V2 id3V2 : result.get("LOL")) {
                    MusicFile file = new MusicFile();
                    file.setAlbum(id3V2.getAlbum());
                    file.setArtist(id3V2.getArtist());
                    file.setTitle(id3V2.getTitle());
                    file.setYear(id3V2.getYear());
                    file.setTrackNumber(String.valueOf(id3V2.getNumber()));
                    if (!id3V2.getArtLinks().isEmpty()) {
                        file.setArtworkUri(Uri.parse(id3V2.getArtLinks().get(0)));
                    }
                    data.add(file);
                }

                return data;
            } catch (ProgressStateException | NoAccessingFilesException | InterruptedException | NoMatchesException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(List<MusicFile> tracks) {
            super.onPostExecute(tracks);
            if (tracks != null && !tracks.isEmpty()) {
                progressHolderLayout.setVisibility(View.GONE);
                bestSearchHolderLayout.setVisibility(View.VISIBLE);
                MusicFile track = tracks.get(0);
                tracks.remove(0);
                Log.d("Title", track.getTitle());
                Log.d("Artist", track.getArtist());
                Log.d("Album", track.getAlbum());
                Log.d("artlink", track.getArtworkUri().toString());

                bestMatchAlbumTextView.setText(track.getAlbum(), TextView.BufferType.EDITABLE);
                bestMatchArtistTextView.setText(track.getArtist(), TextView.BufferType.EDITABLE);
                bestMatchTitleTextView.setText(track.getTitle(), TextView.BufferType.EDITABLE);
                findViewById(R.id.cardBestSearched).setTag(track);

                GlideApp.with(context)
                        .load(track.getArtworkUri())
                        .placeholder(R.drawable.vector_artwork_placeholder)
                        .error(R.drawable.vector_artwork_placeholder)
                        .transition(DrawableTransitionOptions.withCrossFade(700))
                        .into(bestMatchArtworkImageView);
                if (!tracks.isEmpty()) {

                    cardOthersSearched.setVisibility(View.VISIBLE);
                    for (MusicFile file : tracks)
                        adapter.addData(file);


                    //((NestedScrollView)findViewById(R.id.nestedScrollView)).fullScroll(ScrollView.FOCUS_DOWN);

                    /*final Rect rect = new Rect(0, 0, card.getWidth(), cardOthersSearched.getHeight());
                    cardOthersSearched.requestRectangleOnScreen(rect, false);*/
                }

            } else {
                textProgress.setText("No mathces :(");
                bar.setVisibility(View.GONE);
            }

        }

    }


}