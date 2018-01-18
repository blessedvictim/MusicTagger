package com.theonlylies.musictagger.activities;

import android.Manifest;
import android.app.NotificationManager;
import android.app.SearchManager;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.theonlylies.musictagger.R;
import com.theonlylies.musictagger.utils.adapters.BlockItem;
import com.theonlylies.musictagger.utils.adapters.MusicFile;
import com.theonlylies.musictagger.utils.adapters.ExpandBlockAdapter;
import com.theonlylies.musictagger.utils.adapters.ListAdapter;
import com.theonlylies.musictagger.utils.adapters.SimpleListAdapter;
import com.tingyik90.snackprogressbar.SnackProgressBarManager;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

enum ListState {
    SIMPLE,
    GROUP_ALBUM,
    GROUP_ARTIST
}

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, BaseQuickAdapter.OnItemClickListener, BaseQuickAdapter.OnItemLongClickListener, AdapterView.OnItemSelectedListener {

    RecyclerView recyclerView;
    ListAdapter adapter;
    ExpandBlockAdapter blockAdapter;
    SearchView searchView;
    Context context;
    Spinner spinner;
    ListState state = ListState.SIMPLE;
    boolean firstLaunchForSearchView = true;

    /**
     *
     *
     */
    /*SnackProgressBarManager snackProgressBarManager;
    SnackProgressBar determinateType;*/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;

        Log.d("Available threads", String.valueOf(Runtime.getRuntime().availableProcessors()));

        /**
         * ActionBar initialization
         */
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        /**
         * RecyclerViewInitialization
         */
        recyclerView = findViewById(R.id.recyclerView);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);


        /**
         * Snackbar with progressbar for AsyncTask of loading MusicFile into RecyclerView
         */
        //snackProgressBarManager = new SnackProgressBarManager(findViewById(R.id.layout_main_act));
        //determinateType = new SnackProgressBar(SnackProgressBar.TYPE_DETERMINATE, "Loading files...");

        /**
         * Start draw RecyclerView after Permissions have granted
         */

        adapter = new ListAdapter(R.layout.item_simple, this);
        adapter.openLoadAnimation(BaseQuickAdapter.ALPHAIN);
        adapter.isFirstOnly(false);
        adapter.setOnItemClickListener(this);
        adapter.setOnItemLongClickListener(this);
        adapter.bindToRecyclerView(recyclerView);

        blockAdapter = new ExpandBlockAdapter(R.layout.item_expand, this, this);
        blockAdapter.openLoadAnimation(BaseQuickAdapter.ALPHAIN);
        blockAdapter.isFirstOnly(false);
        blockAdapter.bindToRecyclerView(recyclerView);
        blockAdapter.setOnItemClickListener(this);

        initPermissions();

        /**
         * Spinner initialization
         */
        spinner = findViewById(R.id.spinner);
        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.spinner_array, android.R.layout.simple_spinner_item);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(spinnerAdapter);
        spinner.setOnItemSelectedListener(this);
    }

    void createList() {
        switch (state) {
            case SIMPLE: {

                if (!firstLaunchForSearchView) searchView.setVisibility(View.VISIBLE);//hack for first launch search view have been not initialized! its wrong code i thnk!
                recyclerView.setAdapter(adapter);

                if (adapter.getData().isEmpty()) {
                    new MusicReadTask().execute();
                }else {
                    adapter.getData().clear();                     // very interesting code !!!! fuck me
                    new MusicReadTask().execute();
                }


                break;
            }
            case GROUP_ARTIST: {
                searchView.setVisibility(View.GONE);
                recyclerView.setAdapter(blockAdapter);
                blockAdapter.getData().clear();
                new GroupByArtistMusicTask().execute();
                break;
            }
            case GROUP_ALBUM: {
                searchView.setVisibility(View.GONE);
                recyclerView.setAdapter(blockAdapter);
                blockAdapter.getData().clear();
                new GroupByAlbumMusicTask().execute();

                break;
            }
            default: {
                break;
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    /**
     * Spinner works alala-azaza
     */
    boolean spinnerFirst = true;

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        Log.d("onItemSelected", "onItemSelected");
        if (!spinnerFirst) {
            state = ListState.values()[i];
            firstLaunchForSearchView = false;
            createList();
        } else spinnerFirst = false;

    }


    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            if (!searchView.isIconified()) {
                searchView.setIconified(true);
                return;
            } else super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.d("sequence", "onCreateOptionsMenu");
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);

        /**
         * SearchView Initialization
         */
        // Associate searchable configuration with the SearchView
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView = (SearchView) menu.findItem(R.id.action_search)
                .getActionView();
        searchView.setSearchableInfo(searchManager
                .getSearchableInfo(getComponentName()));
        searchView.setMaxWidth(Integer.MAX_VALUE);


        //Это все тлен
        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                if (adapter.getItemCount() != adapter.getDataModelSize()) {
                    adapter.getFilter().filter("");
                }
                return false;
            }
        });
        // listening to search query text change
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // filter recycler view when query submitted
                adapter.getFilter().filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                // filter recycler view when text is changed
                adapter.getFilter().filter(query);
                return false;
            }
        });

        return true;
    }

    int i = 0;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    /**
     * Permission block
     */

    private void initPermissions() {
        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            Log.d("LOL", "QweQQW");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE}, 5);

        } else {
            createList();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        Log.d("PERMISSONS:", "START");
        if (5 == requestCode) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("PERMISSONS:", "GRANTED!!!");

            } else {
                Log.d("PERMISSONS:", "UNGRANTED!!!");

            }
        }
    }

    /**
     * It for adapter onClick action
     */
    private static final int REQUEST_UPDATE_CODE = 123;
    private static final int REQUEST_UPDATE_ALL_CODE = 124;

    @Override
    public void onItemClick(BaseQuickAdapter adapter, View view, int position) {

        if(adapter instanceof ListAdapter){
            if (this.adapter.isMultiselect()) {
                ((ListAdapter) adapter).toogleSelected(position);
                if (this.adapter.getSelectedCount() == 0 && actionMode != null) actionMode.finish();
            } else {
                Intent intent = new Intent(this, OneFileEditActivity.class);
                String data = ((MusicFile) adapter.getItem(position)).getRealPath();
                intent.putExtra("music_file_path", data);
                intent.putExtra("pos", position);
                startActivityForResult(intent, REQUEST_UPDATE_CODE);
            }
        }else if(adapter instanceof ExpandBlockAdapter){
            Log.d("expandItem","ex");
            ( (ExpandBlockAdapter)adapter).expandItem(position);
        } else if (adapter instanceof SimpleListAdapter) {
            Intent intent = new Intent(this, OneFileEditActivity.class);
            String data = ((MusicFile) adapter.getItem(position)).getRealPath();
            intent.putExtra("music_file_path", data);
            intent.putExtra("pos", position);
            startActivityForResult(intent, REQUEST_UPDATE_CODE);
        }


    }

    /**
     * actionMode link for close if selectedItemsCount==0 !!!
     */
    private ActionMode actionMode;

    @Override
    public boolean onItemLongClick(BaseQuickAdapter adapter, View view, int position) {
        if (this.adapter.isMultiselect()) {
            //this.adapter.toogleSelected(position);
            this.onItemClick(adapter,view,position);
        } else {
            this.adapter.setMultiselect(true);
            actionMode = this.startActionMode(new ActionMode.Callback() {
                @Override
                public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                    MenuInflater inflater = mode.getMenuInflater();
                    inflater.inflate(R.menu.cab_menu, menu);
                    return true;
                }

                @Override
                public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                    return false;
                }

                @Override
                public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                    if (item.getItemId() == R.id.cab_button1) {
                        Intent intent = new Intent(getApplicationContext(), MuchFileEditActivity.class);
                        ArrayList<String> files = new ArrayList<>();
                        for (MusicFile file : ((ListAdapter) adapter).getSelectedFiles()) {
                            files.add(file.getRealPath());
                        }
                        intent.putStringArrayListExtra("files", files);
                        startActivityForResult(intent, REQUEST_UPDATE_ALL_CODE);
                    }
                    return true;
                }

                @Override
                public void onDestroyActionMode(ActionMode mode) {
                    ((ListAdapter) adapter).setMultiselect(false);
                    actionMode = null;
                }
            });
            this.adapter.toogleSelected(position);
        }


            return true;
        }

        //TODO right now nothing to do in this place!
        @Override
        protected void onActivityResult ( int requestCode, int resultCode, Intent data){
        if(actionMode!=null)actionMode.finish();
        /*if (data != null && resultCode==AppCompatActivity.RESULT_OK){
            if(requestCode==REQUEST_UPDATE_CODE){
                int pos = data.getIntExtra("pos",-1);
                if(pos>=0){
                    Log.d("result","affirmative!");
                    MusicFile file = adapter.getItem(pos);
                    file = MediaStoreUtils.getMusicFileByPath(file.getRealPath(),this);
                    adapter.notifyItemChanged(pos);
                }

            }
        }*/
        }


        private class GroupByArtistMusicTask extends AsyncTask<Void, Void, List<BlockItem>> {

            @Override
            protected List<BlockItem> doInBackground(Void... voids) {
                final Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                final String[] cursor_cols = {
                        MediaStore.Audio.Media._ID,
                        MediaStore.Audio.Media.ARTIST,
                        MediaStore.Audio.Media.ALBUM,
                        MediaStore.Audio.Media.TITLE,
                        MediaStore.Audio.Media.DATA,
                        MediaStore.Audio.Media.ALBUM_ID,
                        MediaStore.Audio.Media.DURATION,};
                final String where = MediaStore.Audio.Media.IS_MUSIC + "!=0";
                final Cursor cursor = getContentResolver().query(uri, cursor_cols, where, null, MediaStore.Audio.Media.ARTIST);
                int i = 0;
                ArrayList<BlockItem> list = null;
                if (cursor != null) {
                    try {
                        double count = cursor.getCount();
                        Log.d("cursor count", String.valueOf(count));

                        list = new ArrayList<>();
                        ArrayList<MusicFile> files = new ArrayList<>();
                        Set<String> artists = new HashSet<>();

                        while (cursor.moveToNext()) {
                            MusicFile musicFile = new MusicFile();

                            String artist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));
                            String album = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM));
                            String title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
                            String data = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
                            String albumId = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID));
                            String id = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID));

                            i++;
                            /**
                             small workaround for snackbar stuck
                             */
                            Uri albumArtUri = ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart"), Long.decode(albumId));
                            musicFile.setAlbum(album);
                            musicFile.setArtist(artist);
                            musicFile.setRealPath(data);
                            musicFile.setTitle(title);
                            musicFile.setArtworkUri(albumArtUri);
                            musicFile.progress.set((int) (i / count * 100));

                            Log.d("progress", String.valueOf((double) i / count * 100));

                            //publishProgress(musicFile);

                            files.add(musicFile);
                            artists.add(artist);

                        }
                        String artist;
                        Iterator<String> it = artists.iterator();
                        while (it.hasNext()) {
                            artist = it.next();
                            BlockItem item = new BlockItem();
                            ArrayList<MusicFile> blockList = new ArrayList<>();
                            for (MusicFile f : files) {
                                if (f.getArtist().equals(artist)) {
                                    blockList.add(f);
                                }
                            }

                            item.setBlockName(artist);
                            item.setBlockInfo(blockList.size() + " tracks");
                            item.setMusicFiles(blockList);

                            list.add(item);
                        }

                    } catch (NullPointerException e) {
                        e.printStackTrace();
                    } finally {
                        cursor.close();
                    }

                }

                return list;
            }

            @Override
            protected void onPostExecute(List<BlockItem> list) {
                super.onPostExecute(list);
                if (list != null) blockAdapter.setNewData(list);
                recyclerView.scheduleLayoutAnimation();
                //snackProgressBarManager.dismiss();
                //adapter.addData(aVoid);
            }


        }


        private class GroupByAlbumMusicTask extends AsyncTask<Void, BlockItem,Void> {

            @Override
            protected Void doInBackground(Void... voids) {
                final Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                final String[] cursor_cols = {
                        MediaStore.Audio.Media._ID,
                        MediaStore.Audio.Media.ARTIST,
                        MediaStore.Audio.Media.ALBUM,
                        MediaStore.Audio.Media.TITLE,
                        MediaStore.Audio.Media.DATA,
                        MediaStore.Audio.Media.ALBUM_ID,
                        MediaStore.Audio.Media.DURATION,};
                final String where = MediaStore.Audio.Media.IS_MUSIC + "!=0";
                final Cursor cursor = getContentResolver().query(uri, cursor_cols, where, null, MediaStore.Audio.Media.ARTIST);
                ArrayList<BlockItem> list = null;
                if (cursor != null) {
                    try {
                        double count = cursor.getCount();
                        Log.d("cursor count", String.valueOf(count));

                        list = new ArrayList<>();
                        ArrayList<MusicFile> files = new ArrayList<>();
                        Set<String> albums = new HashSet<>();
                        Set<String> artists = new HashSet<>();

                        while (cursor.moveToNext()) {
                            MusicFile musicFile = new MusicFile();

                            String artist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));
                            String album = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM));
                            String title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
                            String data = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
                            String albumId = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID));
                            String id = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID));

                            /**
                             small workaround for snackbar stuck
                             */
                            Uri albumArtUri = ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart"), Long.decode(albumId));
                            musicFile.setAlbum(album);
                            musicFile.setArtist(artist);
                            musicFile.setRealPath(data);
                            musicFile.setTitle(title);
                            musicFile.setArtworkUri(albumArtUri);
                            musicFile.progress.set((int) (i / count * 100));

                            //Log.d("progress", String.valueOf((double) i / count * 100));

                            //publishProgress(musicFile);

                            files.add(musicFile);
                            albums.add(album);
                            artists.add(artist);

                        }

                        String album;
                        Iterator<String> it = albums.iterator();
                        while (it.hasNext()) {
                            album = it.next();
                            BlockItem item = new BlockItem();
                            ArrayList<MusicFile> blockList = new ArrayList<>();
                            for (MusicFile f : files) {
                                if (f.getAlbum().equals(album) ) {
                                    blockList.add(f);
                                }
                            }

                            item.setBlockName(album);
                            item.setBlockInfo(blockList.size() + " tracks");
                            item.setMusicFiles(blockList);

                            publishProgress(item);
                        }





                    } catch (NullPointerException e) {
                        e.printStackTrace();
                    } finally {
                        cursor.close();
                    }

                }

                return null;
            }

            @Override
            protected void onProgressUpdate(BlockItem... progress) {
                //snackProgressBarManager.setProgress(progress[0].progress.get());
                blockAdapter.addData(progress[0]);
                recyclerView.scheduleLayoutAnimation();
            }

            @Override
            protected void onPostExecute(Void list) {
                super.onPostExecute(list);

                //snackProgressBarManager.dismiss();
                //adapter.addData(aVoid);
            }


        }


        private class MusicReadTask extends AsyncTask<Void, MusicFile, Void> {

            @Override
            protected void onPreExecute() {
                /*snackProgressBarManager.show(determinateType, SnackProgressBarManager.LENGTH_LONG);
                determinateType.setShowProgressPercentage(true);*/
            }

            @Override
            protected Void doInBackground(Void... voids) {
                final Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                final String[] cursor_cols = {
                        MediaStore.Audio.Media._ID,
                        MediaStore.Audio.Media.ARTIST,
                        MediaStore.Audio.Media.ALBUM,
                        MediaStore.Audio.Media.TITLE,
                        MediaStore.Audio.Media.DATA,
                        MediaStore.Audio.Media.ALBUM_ID,
                        MediaStore.Audio.Media.DURATION,};
                final String where = MediaStore.Audio.Media.IS_MUSIC + "!=0";
                final Cursor cursor = getContentResolver().query(uri, cursor_cols, where, null, MediaStore.Audio.Media.TITLE);
                int i = 0;
                if (cursor != null) {
                    try {
                        double count = cursor.getCount();
                        Log.d("cursor count", String.valueOf(count));
                        while (cursor.moveToNext()) {
                            MusicFile musicFile = new MusicFile();

                            String artist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));
                            String album = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM));
                            String title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
                            String data = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
                            if (!data.endsWith("mp3")) continue;
                            String albumId = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID));
                            Log.d("albumId", albumId);
                            String id = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID));


                            i++;
                            /**
                             small workaround for snackbar stuck
                             */
                            Uri albumArtUri = ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart"), Long.decode(albumId));
                            musicFile.setAlbum(album);
                            musicFile.setArtist(artist);
                            musicFile.setRealPath(data);
                            musicFile.setTitle(title);
                            musicFile.setArtworkUri(albumArtUri);
                            musicFile.progress.set((int) (i / count * 100));

                            /*if(count>100){
                                Log.d("progress", String.valueOf((double) i / count * 100));

                                publishProgress(musicFile);
                            }*/
                            publishProgress(musicFile);


                        }
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                    } finally {
                        cursor.close();
                    }

                }

                return null;
            }

            @Override
            protected void onProgressUpdate(MusicFile... progress) {
                //snackProgressBarManager.setProgress(progress[0].progress.get());
                adapter.addData(progress[0]);
                recyclerView.scheduleLayoutAnimation();
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                //snackProgressBarManager.dismiss();
                //adapter.addData(aVoid);
            }
        }

    }
