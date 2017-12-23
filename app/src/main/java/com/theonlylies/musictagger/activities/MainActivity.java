package com.theonlylies.musictagger.activities;

import android.Manifest;
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
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.theonlylies.musictagger.R;
import com.theonlylies.musictagger.utils.BlockItem;
import com.theonlylies.musictagger.utils.MusicFile;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

enum ListState{
    SIMPLE,
    GROUP_ARTIST,
    GROUP_ALBUM

}

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener,BaseQuickAdapter.OnItemClickListener,AdapterView.OnItemSelectedListener {

    RecyclerView recyclerView;
    ListAdapter adapter;
    ExpandBlockAdapter blockAdapter;
    SearchView searchView;
    Context context;
    Spinner spinner;
    ListState state=ListState.SIMPLE;
    boolean firstLaunchForSearchView=true;

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
        boolean test=false;
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

        adapter = new ListAdapter(R.layout.item_simple,this);
        //recyclerView.setAdapter(adapter);
        adapter.openLoadAnimation(BaseQuickAdapter.ALPHAIN);
        adapter.setOnItemClickListener(this);

        blockAdapter = new ExpandBlockAdapter(R.layout.item_expand,this,this);
        blockAdapter.openLoadAnimation(BaseQuickAdapter.ALPHAIN);
        blockAdapter.bindToRecyclerView(recyclerView);

        initPermissions();

        /**
         * Spinner initialization
         */
        spinner=findViewById(R.id.spinner);
        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.spinner_array, android.R.layout.simple_spinner_item);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(spinnerAdapter);
        spinner.setOnItemSelectedListener(this);
    }

    void createList(){
        switch (state){
            case SIMPLE:{
                if(!firstLaunchForSearchView)searchView.setVisibility(View.VISIBLE);
                recyclerView.setAdapter(adapter);
                new MusicReadTask().execute();
                break;
            }
            case GROUP_ARTIST:{
                searchView.setVisibility(View.INVISIBLE);
                recyclerView.setAdapter(blockAdapter);
                new GroupByArtistMusicTask().execute();
                break;
            }
            case GROUP_ALBUM:{
                break;
            }
            default:{
                break;
            }
        }
    }

    /**
     * Spinner works alala-azaza
     */
    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        state= ListState.values()[i];
        firstLaunchForSearchView=false;
        createList();
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
        Log.d("sequence","onCreateOptionsMenu");
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
                if(adapter.getItemCount()!=adapter.getDataModelSize()){
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

        if (id == R.id.addItem) {
            MusicFile data = new MusicFile();
            adapter.addData(data);
            return true;
        }

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
        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            Log.d("LOL", "QweQQW");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 5);

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
                new MusicReadTask().execute();
            } else {
                Log.d("PERMISSONS:", "UNGRANTED!!!");

            }
        }
    }

    /**
     * It for adapter onClick action
     * @param adapter
     * @param view
     * @param position
     */
    @Override
    public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
        Toast.makeText(this, "OnItemCLick " + position, Toast.LENGTH_SHORT).show();
        //write some code here to edit tag

    }



    class GroupByArtistMusicTask extends AsyncTask<Void, Void, LinkedList<BlockItem>>{

        @Override
        protected LinkedList doInBackground(Void... voids) {
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
            LinkedList<BlockItem> list=null;
            if (cursor != null) {
                try {
                    double count = cursor.getCount();
                    Log.d("cursor count", String.valueOf(count));

                    list=new LinkedList<>();
                    ArrayList<MusicFile> files= new ArrayList<>();

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
                        musicFile.progress.set((int)(i / count * 100));

                        Log.d("progress",String.valueOf((double)i / count * 100));

                        //publishProgress(musicFile);
                        files.add(musicFile);

                    }
                    MusicFile file;
                    ListIterator it = files.listIterator();
                    while(it.hasNext()){
                        file=(MusicFile) it.next();
                        String compareStr=file.getArtist();
                        BlockItem item=new BlockItem();
                        ArrayList<MusicFile> blockList=new ArrayList<>();
                        for(MusicFile f : files){
                            if(f.getArtist().equals(compareStr))blockList.add(f);
                        }
                        item.setBlockName(compareStr);
                        item.setBlockInfo(blockList.size()+" tracks");
                        item.setMusicFiles(blockList);
                        list.add(item);
                    }



                } catch (NullPointerException e) {
                    e.printStackTrace();
                }
                finally {
                    cursor.close();
                }

            }

            return list;
        }

        @Override
        protected void onPostExecute(LinkedList<BlockItem> list) {
            super.onPostExecute(list);
            if(list!=null)blockAdapter.setNewData(list);
            //snackProgressBarManager.dismiss();
            //adapter.addData(aVoid);
        }


    }


    class MusicReadTask extends AsyncTask<Void, MusicFile, Void> {

        @Override
        protected void onPreExecute() {
            //snackProgressBarManager.show(determinateType, SnackProgressBarManager.LENGTH_LONG);
            //determinateType.setShowProgressPercentage(true);
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
            final Cursor cursor = getContentResolver().query(uri, cursor_cols, where, null, MediaStore.Audio.Media.ARTIST);
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
                        musicFile.progress.set((int)(i / count * 100));

                        Log.d("progress",String.valueOf((double)i / count * 100));

                        publishProgress(musicFile);

                    }
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }
                finally {
                    cursor.close();
                }

            }

            return null;
        }

        @Override
        protected void onProgressUpdate(MusicFile... progress) {
            //snackProgressBarManager.setProgress(progress[0].progress.get());
            adapter.addData(progress[0]);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            //snackProgressBarManager.dismiss();
            //adapter.addData(aVoid);
        }
    }

}
