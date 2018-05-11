package com.theonlylies.musictagger.activities;

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Process;
import android.provider.MediaStore;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.google.android.gms.analytics.ExceptionReporter;
import com.theonlylies.musictagger.Aapplication;
import com.theonlylies.musictagger.R;
import com.theonlylies.musictagger.utils.adapters.BlockItem;
import com.theonlylies.musictagger.utils.adapters.ExpandBlockAdapter;
import com.theonlylies.musictagger.utils.adapters.ListAdapter;
import com.theonlylies.musictagger.utils.adapters.MusicFile;
import com.theonlylies.musictagger.utils.adapters.SimpleListAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

enum ListState {
    SIMPLE,
    GROUP_ALBUM,
    GROUP_ARTIST
}

enum ListStateSort {
    AZtitle,
    ZAtitle
}

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, BaseQuickAdapter.OnItemClickListener, BaseQuickAdapter.OnItemLongClickListener, AdapterView.OnItemSelectedListener {

    RecyclerView recyclerView;
    ListAdapter adapter;
    ExpandBlockAdapter blockAdapter;
    SearchView searchView;
    Context context;
    Spinner spinner, spinnerSort;
    ListState state = ListState.SIMPLE;
    ListStateSort stateSort = ListStateSort.AZtitle;
    boolean firstLaunchForSearchView = true;

    /**
     *
     *
     */
    /*SnackProgressBarManager snackProgressBarManager;
    SnackProgressBar determinateType;*/


    static public int index = -1;
    static public int top = -1;
    LinearLayoutManager layoutManager;


    List<MusicFile> adapterData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        Thread.UncaughtExceptionHandler myHandler = new ExceptionReporter(
                ((Aapplication) getApplication()).getDefaultTracker(),
                Thread.getDefaultUncaughtExceptionHandler(),
                this);

        // Make myHandler the new default uncaught exception handler.
        Thread.setDefaultUncaughtExceptionHandler(myHandler);


        context = this;

        Log.d("Available threads", String.valueOf(Runtime.getRuntime().availableProcessors()));

        /**
         * ActionBar initialization
         */
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setElevation(0);
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

        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        //recyclerView.addItemDecoration(new DividerItemDecoration(getResources().getDrawable( R.drawable.divider )));


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

        /**
         * Spinner initialization
         */
        spinner = findViewById(R.id.spinner);
        spinnerSort = findViewById(R.id.spinnerSort);
        ArrayAdapter<CharSequence> spinnerSortAdapter = ArrayAdapter.createFromResource(this,
                R.array.spinner_sort_array, R.layout.item_spinner);
        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.spinner_array, R.layout.item_spinner);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSortAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSort.setAdapter(spinnerSortAdapter);
        spinnerSort.setOnItemSelectedListener(this);
        spinner.setAdapter(spinnerAdapter);
        spinner.setOnItemSelectedListener(this);

        //
        adapterData = new ArrayList<>();

        createList(state);
    }


    void createList(ListState newState) {
        if (this.state != newState && actionMode != null) actionMode.finish();
        this.state = newState;
        adapterData.clear();
        new ReadMediaStoreTask().execute();
    }


    @Override
    protected void onPause() {
        super.onPause();
        index = layoutManager.findFirstVisibleItemPosition();
        View v = recyclerView.getChildAt(0);
        top = (v == null) ? 0 : (v.getTop() - recyclerView.getPaddingTop());
    }

    @Override
    protected void onResume() {
        Log.d("onResume", "syka");
        //if(recyclerView.getAdapter().getItemCount()<1)createList(state);
        super.onResume();
    }

    /**
     * Spinner works alala-azaza
     */
    boolean spinnerFirst = true;

    public void sortAdapter(int sortState) {
        stateSort = ListStateSort.values()[sortState];
        if (recyclerView.getAdapter() instanceof ExpandBlockAdapter) {
            if (stateSort == ListStateSort.AZtitle) {
                Collections.sort(((ExpandBlockAdapter) recyclerView.getAdapter()).getData(), (f1, f2) -> f1.getBlockName().compareToIgnoreCase(f2.getBlockName()));
                recyclerView.getAdapter().notifyDataSetChanged();
            } else {
                Collections.sort(((ExpandBlockAdapter) recyclerView.getAdapter()).getData(), (f1, f2) -> f2.getBlockName().compareToIgnoreCase(f1.getBlockName()));
                recyclerView.getAdapter().notifyDataSetChanged();
            }
        } else {
            if (stateSort == ListStateSort.AZtitle) {
                Collections.sort(((ListAdapter) recyclerView.getAdapter()).getData(), (f1, f2) -> f1.getTitle().compareToIgnoreCase(f2.getTitle()));
                recyclerView.getAdapter().notifyDataSetChanged();
            } else {
                Collections.sort(((ListAdapter) recyclerView.getAdapter()).getData(), (f1, f2) -> f2.getTitle().compareToIgnoreCase(f1.getTitle()));
                recyclerView.getAdapter().notifyDataSetChanged();
            }
        }

    }

    public void updateAdapter(ListState newState) {
        this.state = newState;
        switch (this.state) {
            case SIMPLE: {
                recyclerView.setAdapter(adapter);
                sortAdapter(this.stateSort.ordinal());
                recyclerView.scheduleLayoutAnimation();
                break;
            }
            case GROUP_ALBUM: {
                recyclerView.setAdapter(blockAdapter);
                blockAdapter.setNewData(createBlockData(adapterData, ListState.GROUP_ALBUM));
                sortAdapter(this.stateSort.ordinal());
                recyclerView.scheduleLayoutAnimation();
                break;
            }
            case GROUP_ARTIST: {
                recyclerView.setAdapter(blockAdapter);
                blockAdapter.setNewData(createBlockData(adapterData, ListState.GROUP_ARTIST));
                sortAdapter(this.stateSort.ordinal());
                recyclerView.scheduleLayoutAnimation();
                break;
            }
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        Log.d("onItemSelected", String.valueOf(adapterView.getId()));
        if (adapterView.getId() == R.id.spinner) {
            if (!spinnerFirst) {
                firstLaunchForSearchView = false;
                //createList(ListState.values()[i]);
                updateAdapter(ListState.values()[i]);
            } else spinnerFirst = false;
        }

        if (adapterView.getId() == R.id.spinnerSort) {
            sortAdapter(i);
        }
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
        //SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView = (SearchView) menu.findItem(R.id.action_search)
                .getActionView();
        /*searchView.setSearchableInfo(searchManager
                .getSearchableInfo(getComponentName()));*/
        searchView.setMaxWidth(Integer.MAX_VALUE);


        //Это все тлен
        searchView.setOnCloseListener(() -> {
            if (recyclerView.getAdapter() instanceof ListAdapter) {
                if (adapter.getItemCount() != adapter.getDataModelSize()) {
                    adapter.getFilter().filter("");
                }
            }
            if (recyclerView.getAdapter() instanceof ExpandBlockAdapter) {
                if (blockAdapter.getItemCount() != blockAdapter.getData().size()) {
                    blockAdapter.getFilter().filter("");
                }
            }

            return false;
        });
        // listening to search query text change
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // filter recycler view when query submitted
                if (recyclerView.getAdapter() instanceof ListAdapter) {
                    adapter.getFilter().filter(query);
                }
                if (recyclerView.getAdapter() instanceof ExpandBlockAdapter) {
                    blockAdapter.getFilter().filter(query);
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                // filter recycler view when text is changed
                if (recyclerView.getAdapter() instanceof ListAdapter) {
                    Log.e("onTextChanged", String.valueOf(((ListAdapter) recyclerView.getAdapter()).getData().size()));
                    adapter.getFilter().filter(query);
                }
                if (recyclerView.getAdapter() instanceof ExpandBlockAdapter) {
                    blockAdapter.getFilter().filter(query);
                }
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
        if (id == R.id.action_reload) {
            createList(this.state);
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_help) {
            new Handler().postDelayed(() -> {
                Intent intent = new Intent(this, HelpActivity.class);
                startActivity(intent);
            }, 200);
        } else if (id == R.id.nav_settings) {
            new Handler().postDelayed(() -> {
                Intent intent = new Intent(this, PreferencesActivity.class);
                startActivity(intent);
            }, 200);

        } else if (id == R.id.nav_faq) {
            new Handler().postDelayed(() -> {
                Intent intent = new Intent(this, FAQActivity.class);
                startActivity(intent);
            }, 200);
        } else if (id == R.id.nav_about) {
            new Handler().postDelayed(() -> {
                Intent intent = new Intent(this, AboutActivity.class);
                startActivity(intent);
            }, 200);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    /**
     * Permission block
     */

    /**
     * It for adapter onClick action
     */
    private static final int REQUEST_UPDATE_CODE = 123;
    private static final int REQUEST_UPDATE_ALL_CODE = 124;

    @Override
    public void onItemClick(BaseQuickAdapter adapter, View view, int position) {

        if (adapter instanceof ListAdapter) {
            if (this.adapter.isMultiselect()) {
                if (((ListAdapter) adapter).getSelectedCount() > 19) {
                    Toast.makeText(this, "You cannot select mre than 20 files", Toast.LENGTH_LONG).show();
                } else {
                    ((ListAdapter) adapter).toogleSelected(position);
                    if (this.adapter.getSelectedCount() == 0 && actionMode != null)
                        actionMode.finish();
                    else actionMode.setTitle(String.valueOf(this.adapter.getSelectedCount()));
                }

            } else {
                Intent intent = new Intent(this, OneFileEditActivity.class);
                String data = ((MusicFile) adapter.getItem(position)).getRealPath();
                intent.putExtra("music_file_path", data);
                intent.putExtra("pos", position);
                startActivityForResult(intent, REQUEST_UPDATE_CODE);
            }
        } else if (adapter instanceof ExpandBlockAdapter) {
            if (view.getId() == R.id.groupEditButton) {
                Log.d("MainActivity", "groupEditButton click");
                Intent intent = new Intent(getApplicationContext(), MuchFileEditActivity.class);
                ArrayList<String> files = new ArrayList<>();
                for (MusicFile file : ((ExpandBlockAdapter) adapter).getItem(position).getMusicFiles()) {
                    files.add(file.getRealPath());
                }
                intent.putStringArrayListExtra("files", files);
                startActivityForResult(intent, REQUEST_UPDATE_ALL_CODE);
            } else {
                Log.d("expandItem", "ex");
                ((ExpandBlockAdapter) adapter).expandItem(position);
            }

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
    public boolean onItemLongClick(final BaseQuickAdapter adapter, View view, int position) {
        if (this.adapter.isMultiselect()) {
            //this.adapter.toogleSelected(position);
            this.onItemClick(adapter, view, position);
        } else {
            this.adapter.setMultiselect(true);
            actionMode = this.startActionMode(new ActionMode.Callback() {
                @Override
                public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                    MenuInflater inflater = mode.getMenuInflater();
                    inflater.inflate(R.menu.cab_menu, menu);
                    spinner.setEnabled(false);
                    spinnerSort.setEnabled(false);
                    return true;
                }

                @Override
                public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                    return false;
                }

                @Override
                public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                    if (item.getItemId() == R.id.cab_button1) {
                        ArrayList<MusicFile> files = (ArrayList<MusicFile>) ((ListAdapter) adapter).getSelectedFiles();
                        if (files.size() == 1) {
                            Intent intent = new Intent(getApplicationContext(), OneFileEditActivity.class);
                            intent.putExtra("music_file_path", files.get(0).getRealPath());
                            intent.putExtra("pos", position);
                            startActivityForResult(intent, REQUEST_UPDATE_CODE);
                        } else {
                            Intent intent = new Intent(getApplicationContext(), MuchFileEditActivity.class);
                            intent.putParcelableArrayListExtra("files", files);
                            startActivityForResult(intent, REQUEST_UPDATE_ALL_CODE);
                        }

                    }
                    return true;
                }

                @Override
                public void onDestroyActionMode(ActionMode mode) {
                    ((ListAdapter) adapter).setMultiselect(false);
                    actionMode = null;
                    spinner.setEnabled(true);
                    spinnerSort.setEnabled(true);
                }
            });
            this.adapter.toogleSelected(position);
            actionMode.setTitle(String.valueOf(this.adapter.getSelectedCount()));
        }


        return true;
    }


    //TODO right now nothing to do in this place!
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (actionMode != null) actionMode.finish();

        if (requestCode == REQUEST_UPDATE_CODE || requestCode == REQUEST_UPDATE_ALL_CODE) {
            Log.w("onActivityResult", "REQUEST_UPDATE_CODE");
            if (resultCode == RESULT_OK) {
                Log.w("onActivityResult", "RESULT_OK");
                createList(this.state);
            } else {
                Log.w("onActivityResult", "RESULT_CANCELLED");
                if (index != -1) layoutManager.scrollToPositionWithOffset(index, top);
            }
        }
    }

    public List<BlockItem> createBlockData(List<MusicFile> data, ListState state) {
        List<BlockItem> blockItems = new ArrayList<>();
        switch (state) {
            case GROUP_ARTIST:
            case GROUP_ALBUM: {
                Set<Long> albumIds = new HashSet<>();
                for (MusicFile file : data) {
                    albumIds.add(file.getAlbum_id());
                }

                for (Long albumId : albumIds) {

                    BlockItem item = new BlockItem();
                    ArrayList<MusicFile> blockList = new ArrayList<>();
                    for (MusicFile file : data)
                        if (file.getAlbum_id() == albumId) blockList.add(file);

                    item.setMusicFiles(blockList);
                    if(state==ListState.GROUP_ALBUM){
                        item.setBlockName(blockList.get(0).getAlbum());
                        item.setBlockScName(blockList.get(0).getArtist());
                    }else{
                        item.setBlockName(blockList.get(0).getArtist());
                        item.setBlockScName(blockList.get(0).getAlbum());
                    }

                    if (blockList.size() == 1) {
                        item.setBlockInfo(blockList.size() + " song");
                    } else {
                        item.setBlockInfo(blockList.size() + " songs");
                    }
                    blockItems.add(item);
                }
                break;
            }
        }
        return blockItems;
    }
    private class ReadMediaStoreTask extends AsyncTask<Void, Void, Void> {

        RelativeLayout progressLayout;

        @Override
        protected void onPreExecute() {
            progressLayout = findViewById(R.id.main_progressLayout);
            progressLayout.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            Process.setThreadPriority(Process.THREAD_PRIORITY_DISPLAY);
            final Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
            final String[] cursor_cols = {
                    MediaStore.Audio.Media._ID,
                    MediaStore.Audio.Media.ARTIST,
                    MediaStore.Audio.Media.ALBUM,
                    MediaStore.Audio.Media.TITLE,
                    MediaStore.Audio.Media.DATA,
                    MediaStore.Audio.Media.ALBUM_ID,
                    MediaStore.Audio.Media.DURATION};
            final String where = MediaStore.Audio.Media.IS_MUSIC + "!=0";

            final Cursor cursor = getContentResolver().query(uri, cursor_cols, where, null, null);
            if (cursor != null) {
                try {
                    double count = cursor.getCount();
                    while (cursor.moveToNext()) {
                        MusicFile musicFile = new MusicFile();

                        String artist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));
                        String album = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM));
                        String title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
                        String data = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
                        if (!data.endsWith("mp3")) continue;
                        String albumId = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID));
                        String id = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID));
                        Uri albumArtUri = ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart"), Long.decode(albumId));
                        musicFile.setAlbum(album);
                        musicFile.setArtist(artist);
                        musicFile.setRealPath(data);
                        musicFile.setTitle(title);
                        musicFile.setAlbum_id(Long.parseLong(albumId));
                        musicFile.setArtworkUri(albumArtUri);

                        adapterData.add(musicFile);
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
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            switch (state) {
                case SIMPLE: {
                    recyclerView.setAdapter(adapter);
                    adapter.setNewData(adapterData);
                    break;
                }
                case GROUP_ALBUM: {
                    recyclerView.setAdapter(blockAdapter);
                    blockAdapter.setNewData(createBlockData(adapterData, ListState.GROUP_ALBUM));
                    break;
                }
                case GROUP_ARTIST: {
                    recyclerView.setAdapter(blockAdapter);
                    blockAdapter.setNewData(createBlockData(adapterData, ListState.GROUP_ARTIST));
                    break;
                }
            }
            sortAdapter(stateSort.ordinal());
            TransitionManager.beginDelayedTransition(progressLayout);
            progressLayout.setVisibility(View.GONE);
            recyclerView.scheduleLayoutAnimation();
        }
    }

}
