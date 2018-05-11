package com.theonlylies.musictagger.activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.drawable.VectorDrawable;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.google.android.gms.analytics.ExceptionReporter;
import com.theonlylies.musictagger.Aapplication;
import com.theonlylies.musictagger.R;
import com.theonlylies.musictagger.utils.FileUtil;
import com.theonlylies.musictagger.utils.GlideApp;
import com.theonlylies.musictagger.utils.MusicCache;
import com.theonlylies.musictagger.utils.PreferencesManager;
import com.theonlylies.musictagger.utils.adapters.ListAdapter;
import com.theonlylies.musictagger.utils.adapters.MusicFile;
import com.theonlylies.musictagger.utils.edit.BitmapUtils;
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

import Fox.core.lib.general.data.FingerPrint;
import Fox.core.lib.general.data.ID3V2;
import Fox.core.lib.general.templates.FingerPrintThread;
import Fox.core.lib.general.templates.ProgressState;
import Fox.core.lib.general.utils.FingerPrintProcessingException;
import Fox.core.lib.general.utils.NoAccessingFilesException;
import Fox.core.lib.general.utils.NoMatchesException;
import Fox.core.lib.general.utils.ProgressStateException;
import Fox.core.main.SearchLib;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

import static com.theonlylies.musictagger.utils.edit.MediaStoreUtils.GENRES;

public class OneFileEditActivity extends AppCompatActivity implements View.OnClickListener {

    FloatingActionButton fabPlayer;

    EditText titleEdit, albumEdit, artistEdit, yearEdit, trackNumberEdit, albumArtistEdit, composerEdit, discNumEdit, commentEdit;
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

    SmartSearchTask smartSearchTask = null;


    //DoConnect smartSearchTask;

    @Override
    protected void onDestroy() {
        Log.d("onDestroy", "syka");
        if (smartSearchTask != null && smartSearchTask.getStatus() == AsyncTask.Status.RUNNING)
            smartSearchTask.cancel(true);
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
                fabPlayer.setImageDrawable(drawPause);
            } catch (IOException e) {
                e.printStackTrace();
            }

        } else {
            if (mp3Play.isPlaying()) {
                mp3Play.stop();
                mp3Play.reset();
            }

            fabPlayer.setImageDrawable(drawPlay);
        }
        play = !play;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onefiledit);
        Log.w("artworkAction", artworkAction.name());

        Thread.UncaughtExceptionHandler myHandler = new ExceptionReporter(
                ((Aapplication) getApplication()).getDefaultTracker(),
                Thread.getDefaultUncaughtExceptionHandler(),
                this);

        // Make myHandler the new default uncaught exception handler.
        Thread.setDefaultUncaughtExceptionHandler(myHandler);
        ///Exceptions handler LOL

        context = this;

        titleEdit = findViewById(R.id.titleEdit);
        albumEdit = findViewById(R.id.albumEdit);
        artistEdit = findViewById(R.id.artistEdit);
        yearEdit = findViewById(R.id.yearEdit);
        genreEdit = findViewById(R.id.genreEdit);
        commentEdit = findViewById(R.id.commentEdit);
        composerEdit = findViewById(R.id.composerEdit);
        albumArtistEdit = findViewById(R.id.albumArtistEdit);
        discNumEdit = findViewById(R.id.discNumEdit);
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

        fabPlayer = findViewById(R.id.fabPlayer);
        drawPlay = (VectorDrawable) ContextCompat.getDrawable(this, R.drawable.ic_play_icon);
        drawPause = (VectorDrawable) ContextCompat.getDrawable(this, R.drawable.ic_pause_icon);

        switchRename = findViewById(R.id.switchFileRename);
        if (!PreferencesManager.getStringValue(this, "rename-rule", "4").equals("4")) {
            switchRename.setChecked(true);
        } else switchRename.setEnabled(false);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        appBarLayout = findViewById(R.id.app_bar_much_act);
        CardView cardView = findViewById(R.id.cardView);


        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                int offset = appBarLayout.getTotalScrollRange() - Math.abs(verticalOffset);
                float percentage = (offset / (float) appBarLayout.getTotalScrollRange());
                cardView.setAlpha(percentage);
            }
        });


        String path = getIntent().getStringExtra("music_file_path");
        new ReadFromMediaStore().execute(path);
    }

    void initTagsInterface(MusicFile file) {
        titleEdit.setText(file.getTitle());
        albumEdit.setText(file.getAlbum());
        artistEdit.setText(file.getArtist());
        yearEdit.setText(file.getYear());
        genreEdit.setText(file.getGenre());
        musicFilePathView.setText(file.getRealPath());
        trackNumberEdit.setText(file.getTrackNumber());
        discNumEdit.setText(file.getDiscNumber());
        composerEdit.setText(file.getComposer());
        commentEdit.setText(file.getComment());
        reloadImage(file);
        musicFile = file;
    }

    private void reloadImage(MusicFile file) {
        GlideApp.with(this)
                .load(file.getArtworkUri())
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .error(R.drawable.vector_artwork_placeholder)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(artworkImageView);
    }

    @Override
    public void onBackPressed() {
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

        Log.d("id", String.valueOf(id));

        //noinspection SimplifiableIfStatement
        /*if (id == R.id.action_crop) {
            File cache = new File(getCacheDir(), "lol");
            if(cache.exists()) cache.delete();
            Uri uri;
            if(artworkAction==ArtworkAction.CHANGED)uri=newArtworkUri;
            else  if(artworkAction==ArtworkAction.NONE)uri=this.musicFile.getArtworkUri();
            else uri=null;
            if(uri!=null)
            UCrop.of(this.musicFile.getArtworkUri(), Uri.fromFile(cache))
                    .withAspectRatio(1, 1)
                    .withMaxResultSize(800, 800)
                    .start(this);
        }*/
        if (id == R.id.action_save_file) {
            //saveChanges(musicFile);
            new WriteChanges().execute(musicFile);
        }
        if (id == android.R.id.home) {
            setResult(RESULT_CANCELED);
            finish();
        }

        /*if(id==R.id.test){  //FIXME for test !!!
            Log.e("test", String.valueOf(FileUtil.canWriteThisFileSAF(this,this.musicFile.getRealPath())));
        }*/
        return super.onOptionsItemSelected(item);
    }

    private static final int REQUEST_CODE_GALLERY_PICK = 1;
    private static final int REQUEST_CODE_INTERNET_PICK = 2;

    void goToView(View view) {
        if (view != null) {
            final Rect rect = new Rect(0, 0, view.getWidth(), view.getHeight());
            view.requestRectangleOnScreen(rect, false);
        }

    }

    @Override
    public void onClick(View v) {

        if (v.getId() == R.id.fabSmartSearch) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            if (!smartSearch) {
                if (this.isOnline()) {
                    Log.d("sd", "azazaazzaz");
                    smartSearchTask = new SmartSearchTask();
                    smartSearchTask.execute(musicFile.getRealPath());
                    goToView(cardBestSearched);
                } else {
                    builder.setTitle("");
                    builder.setMessage("Please turn on internet connection and repeat");
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
            } else {
                goToView(cardBestSearched);
            }
        }

        if (v.getId() == R.id.cardBestSearched) {

            AlertDialog.Builder builder = new AlertDialog.Builder(this);

            if (!smartSearch) {// если еще не прикрепили тег(MusicFile объект)
                if (this.isOnline()) {
                    new SmartSearchTask().execute(musicFile.getRealPath());
                } else {
                    builder.setTitle("");
                    builder.setMessage("Please turn on internet connection and repeat");
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
            } else if (v.getTag() != null) {
                MusicFile file = (MusicFile) v.getTag();
                if (file != null) {
                    file.setAlbum_id(musicFile.getAlbum_id());
                    file.setRealPath(musicFile.getRealPath());
                    artworkAction = ArtworkAction.CHANGED;
                    newArtworkUri = file.getArtworkUri();
                    initTagsInterface(file);
                    //NEW
                    Toast.makeText(this, "Tags was applied", Toast.LENGTH_SHORT).show();

                }
            }


        }

        if (v.getId() == R.id.fabPlayer) {

            swapAnimationOfplayer();
        }

        if (v.getId() == R.id.fabImage) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setItems(R.array.image_actions, (dialog, which) -> {
                switch (which) {
                    case 0: {
                        Intent intent = new Intent(Intent.ACTION_PICK);
                        intent.setType("image/*");
                        intent.setAction(Intent.ACTION_GET_CONTENT);
                        startActivityForResult(Intent.createChooser(intent, getString(R.string.selectpic_string)), REQUEST_CODE_GALLERY_PICK);
                        break;
                    }
                    case 1: {
                        AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
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
                            builder1.setTitle(R.string.selectsource_string);
                            // Create the AlertDialog
                            AlertDialog dialog1 = builder1.create();
                            dialog1.show();
                        } else {
                            builder1.setTitle("");
                            builder1.setMessage(R.string.turnoninternet_string);
                            AlertDialog dialog1 = builder1.create();
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

        if (v.getId() == R.id.cardOtherTags) {
            ViewGroup group = findViewById(R.id.layoutOtherTags);
            TransitionManager.beginDelayedTransition(group);
            if (group.getVisibility() != View.VISIBLE) group.setVisibility(View.VISIBLE);
            else group.setVisibility(View.GONE);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data != null && resultCode == AppCompatActivity.RESULT_OK) {
            switch (requestCode) {
                case REQUEST_CODE_GALLERY_PICK: {
                    newArtworkUri = data.getData();
                    GlideApp.with(this).load(newArtworkUri).centerCrop().error(R.drawable.vector_artwork_placeholder).into(artworkImageView);
                    artworkAction = ArtworkAction.CHANGED;
                    break;
                }
                case REQUEST_CODE_INTERNET_PICK: {
                    newArtworkUri = Uri.parse(data.getStringExtra("image"));
                    GlideApp.with(this).load(newArtworkUri).centerCrop().error(R.drawable.vector_artwork_placeholder).into(artworkImageView);
                    artworkAction = ArtworkAction.CHANGED;
                    break;
                }
                case UCrop.REQUEST_CROP: {
                    final Uri resultUri = UCrop.getOutput(data);
                    newArtworkUri = resultUri;
                    artworkAction = ArtworkAction.CHANGED;
                    GlideApp.with(this)
                            .load(newArtworkUri)
                            .error(R.drawable.vector_artwork_placeholder)
                            .transition(DrawableTransitionOptions.withCrossFade())
                            .into(artworkImageView);
                }
            }
        }

    }

    /**
     * artWorkWasChanged for check state of artwork change or not fck my eng!
     */

    ArtworkAction artworkAction = ArtworkAction.NONE;

    void updateMusicFile(MusicFile file) {
        musicFile.setFieldsByMusocFile(file);
        musicFile.setTitle(file.getTitle());
    }

    public MusicFile collectDataFromUI() {
        musicFile.setTitle(titleEdit.getText().toString());
        musicFile.setAlbum(albumEdit.getText().toString());
        musicFile.setArtist(artistEdit.getText().toString());
        musicFile.setYear(yearEdit.getText().toString());
        musicFile.setTrackNumber(trackNumberEdit.getText().toString());
        musicFile.setGenre(genreEdit.getText().toString());
        musicFile.setDiscNumber(discNumEdit.getText().toString());
        musicFile.setComment(commentEdit.getText().toString());
        musicFile.setAlbumArtist(albumArtistEdit.getText().toString());
        musicFile.setComposer(composerEdit.getText().toString());
        return musicFile;
    }

    public boolean saveChanges(final MusicFile musicFile, AlertDialog dialog) {

        Log.w("artworkAction", artworkAction.name());

        Bitmap bitmap = null;

        PreferencesManager.RenameRules rule = PreferencesManager.
                RenameRules.values()[Integer.parseInt(PreferencesManager.getStringValue(this, "rename-rule", "4"))];
        if (!switchRename.isChecked()) rule = PreferencesManager.RenameRules.none;


        //update musicFile instance for editing
        collectDataFromUI();

        if (!FileUtil.fileOnSdCard(new File(musicFile.getRealPath()))) {
            Log.d("OneFileEdit", "file in internal storage!...");
            TagManager.rewriteTag(this.musicFile.getRealPath());
            TagManager tagManager = new TagManager(musicFile.getRealPath());
            tagManager.setTagsFromMusicFile(this.musicFile);
            switch (artworkAction) {
                case CHANGED: {
                    try {
                        bitmap = GlideApp.with(getApplicationContext())
                                .asBitmap()
                                .centerCrop()
                                .load(newArtworkUri)
                                .submit()
                                .get();
                        bitmap = BitmapUtils.getCenterCropedBitmap(bitmap);
                        tagManager.setArtwork(bitmap);
                    } catch (InterruptedException | ExecutionException e) {
                        e.printStackTrace();
                    }
                    break;
                }
                case DELETED: {
                    tagManager.deleteArtwork();
                    break;
                }
            }
            tagManager.save();
        } else if (FileUtil.fileOnSdCard(new File(musicFile.getRealPath())) && FileUtil.canWriteThisFileSAF(this, musicFile.getRealPath())) {
            Log.d("OneFileEdit", "file on sdcard ! EDITITNG!");
            MusicCache cache = new MusicCache(this);
            try {
                File file = cache.cacheMusicFile(new File(musicFile.getRealPath()));
                TagManager.rewriteTag(file.getAbsolutePath());
                TagManager tagManager = new TagManager(file.getPath());
                tagManager.setTagsFromMusicFile(this.musicFile);
                switch (artworkAction) {
                    case CHANGED: {
                        try {
                            bitmap = GlideApp.with(getApplicationContext())
                                    .asBitmap()
                                    .centerCrop()
                                    .load(newArtworkUri)
                                    .submit()
                                    .get();
                            bitmap = BitmapUtils.getCenterCropedBitmap(bitmap);
                            tagManager.setArtwork(bitmap);
                        } catch (InterruptedException | ExecutionException e) {
                            e.printStackTrace();
                        }
                        break;
                    }
                    case DELETED: {
                        tagManager.deleteArtwork();
                        break;
                    }
                }

                tagManager.save();
                cache.replaceCache();
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else {
            Log.d("OneFileEdit", "fuck you sd card access :/");
            Toast.makeText(this, "Seems there is problem with removable sd card", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Работа с Media Store
        final long album_id = musicFile.getAlbum_id();
        Log.e("album_id before", String.valueOf(album_id));

        //dumpMedia(getApplicationContext());
        //dumpAlbums(getApplicationContext());

        //renaming
        String newName = MediaStoreUtils.generateNewName(this.musicFile, rule);
        if (rule != PreferencesManager.RenameRules.none) {
            if (!new File(musicFile.getRealPath()).getName().equals(newName)) {
                String oldPath = "\"" + musicFile.getRealPath() + "\"";
                if (FileUtil.renameFile(this, musicFile, newName)) {
                    int deleted = context.getContentResolver().delete(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, MediaStore.Audio.Media.DATA + "==" + oldPath, null);
                    Log.d("deleted rows count", String.valueOf(deleted));
                    Log.v("renaming", "success");
                } else Log.v("renaming", "failed");
            }
        }


        Bitmap finalBitmap = bitmap;
        MediaStoreUtils.updateFileMediaStoreMedia(musicFile, this, (path, uri) -> {
            Log.i("ExternalStorage", "Scanned " + path + ":");
            Log.i("ExternalStorage", "-> uri=" + uri);
            //                 interface  music file update
            updateMusicFile(MediaStoreUtils.getMusicFileByPath(musicFile.getRealPath(), getApplicationContext())); // interface update
            //                 interface  music file update END !!!

            //FIXME need change album art
            Log.e("album_id after", String.valueOf(musicFile.getAlbum_id()));
            if (musicFile.getAlbum_id() == album_id) {
                switch (artworkAction) {
                    case CHANGED: {
                        //Bitmap bitmap = ((BitmapDrawable) artworkImageView.getDrawable()).getBitmap();
                        Log.d("setArtwork()", String.valueOf(MediaStoreUtils.setAlbumArt(finalBitmap, getApplicationContext(), musicFile.getAlbum_id())));
                        break;
                    }
                    case DELETED: {
                        MediaStoreUtils.deleteAlbumArt(getApplicationContext(), musicFile.getAlbum_id());
                    }
                }
            }

            if (dialog != null) dialog.dismiss();
            OneFileEditActivity.this.setResult(RESULT_OK);
            finishAfterTransition();

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

        static final int TIME_OUT = 5000;

        static final int MSG_DISMISS_DIALOG = 0;


        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            AlertDialog.Builder builder = new AlertDialog.Builder(OneFileEditActivity.this);
            builder.setCancelable(false);
            builder.setMessage(R.string.changeswriting_string);
            dialog = builder.create();
            dialog.show();
            io.reactivex.Observable.fromCallable(() -> {
                Thread.sleep(5000);
                return true;
            })
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe((result) -> {
                        if (dialog != null && dialog.isShowing()) {
                            dialog.dismiss();
                            Toast.makeText(OneFileEditActivity.this, getString(R.string.something_wrong_string), Toast.LENGTH_LONG).show();
                            OneFileEditActivity.this.setResult(RESULT_OK);
                            finishAfterTransition();
                        }
                    });
        }

        @Override
        protected Boolean doInBackground(MusicFile... files) {
            return saveChanges(files[0], dialog);
            //return saveChangesTEST(files[0], dialog);

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
            MusicFile file1 = TagManager.getMusicFileByPath(strings[0]);
            file1.setAlbum_id(file.getAlbum_id());
            file1.setArtworkUri(file.getArtworkUri());
            TagManager manager = new TagManager(strings[0]);
            file.setGenre(manager.getGenre());
            return file1;
        }

        @Override
        protected void onPostExecute(MusicFile file) {
            super.onPostExecute(file);
            initTagsInterface(file);
            musicFile = file;
        }
    }

    static {
        try {
            System.loadLibrary("fpcalc");
            Log.i("fpCalc", "JNI fpCalc loaded");
        } catch (UnsatisfiedLinkError e) {
            Log.e("fpCalc", "Could not load library libfpcalc.so : " + e);
        }
    }

    public native String fpCalc(String[] args);

    class SmartSearchTask extends AsyncTask<String, Double, List<MusicFile>> {


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
            textProgress.setText(R.string.dataloading_string);
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
                    artworkAction = ArtworkAction.CHANGED;
                    newArtworkUri = file.getArtworkUri();
                    OneFileEditActivity.this.initTagsInterface(file);
                    Toast.makeText(OneFileEditActivity.this, getString(R.string.tags_apllied_string), Toast.LENGTH_SHORT).show();
                }
            });
            adapter.bindToRecyclerView(recyclerView);
        }

        @Override
        protected void onProgressUpdate(Double... values) {
            super.onProgressUpdate(values);
            textProgress.setText(getString(R.string.wait_loading_string) + " (" + values[0].shortValue() + "%)");
        }

        @Override
        protected List<MusicFile> doInBackground(String... params) {
            List<ID3V2> result;
            ArrayList<String> list = new ArrayList<>();
            list.add(params[0]);
            try {
                //ProgressState Line4 = new CustomProgressState(0, "Common", "All progress");
                result = SearchLib.SearchTags(params[0], new FpcaltThread(), new ProgressState(0, "Common", "All progress") {
                    @Override
                    protected void onDone() {

                    }

                    @Override
                    protected void onResize() {

                    }

                    @Override
                    protected void onChange() {
                        SmartSearchTask.this.publishProgress(((double) this.state) / ((double) this.getSize()) * 100);
                    }
                }, 4);
                List<MusicFile> data = new ArrayList<>(result.size());
                for (ID3V2 id3V2 : result) {
                    MusicFile file = new MusicFile();
                    file.setAlbum(id3V2.getAlbum());
                    file.setArtist(id3V2.getArtist());
                    file.setTitle(id3V2.getTitle());
                    file.setYear(id3V2.getYear());
                    file.setTrackNumber(String.valueOf(id3V2.getNumber()));
                    if (id3V2.getArtLinks() != null && !id3V2.getArtLinks().isEmpty()) {
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

                    for (MusicFile file : tracks)
                        adapter.addData(file);
                    cardOthersSearched.setVisibility(View.VISIBLE);
                    ConstraintLayout lay = findViewById(R.id.layoutParentSearched);

                    //((NestedScrollView)findViewById(R.id.nestedScrollView)).fullScroll(ScrollView.FOCUS_DOWN);

                    goToView(lay);
                }

            } else {
                textProgress.setText(R.string.no_matches_string);
                bar.setVisibility(View.GONE);
            }
            smartSearch = true;

        }

    }


}