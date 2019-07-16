package com.example.noteapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.example.noteapp.NoteKeeperDatabaseContract.CourseInfoEntry;
import com.example.noteapp.NoteKeeperDatabaseContract.NoteInfoEntry;

import java.util.List;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        LoaderManager.LoaderCallbacks<Cursor> {

    public static final int LOADER_NOTES = 1;
    private RecyclerView mRecyclerItems;
    private LinearLayoutManager mNotesLayoutManager;
    private GridLayoutManager mCoursesLayoutManager;
    private CourseRecyclerAdapter mCourseRecyclerAdapter;
    private NoteRecyclerAdapter noteRecyclerAdapter;
    private NoteKeeperOpenHelper mDbOpenHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mDbOpenHelper = new NoteKeeperOpenHelper(this);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        PreferenceManager.setDefaultValues(this, R.xml.pref_general, false);
        PreferenceManager.setDefaultValues(this, R.xml.pref_notification, false);
        PreferenceManager.setDefaultValues(this, R.xml.pref_data_sync, false);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        initializeDisplayContent();
    }

    @Override
    protected void onDestroy() {
        mDbOpenHelper.close();
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //adapterNotes.notifyDataSetChanged();
        //noteRecyclerAdapter.notifyDataSetChanged();
        //loadNotes();
        getLoaderManager().restartLoader(LOADER_NOTES, null, (android.app.LoaderManager.LoaderCallbacks<Object>) this);
        updateNavHeader();
    }

    private void loadNotes() {
        SQLiteDatabase db = mDbOpenHelper.getReadableDatabase();

        final String[] noteColumns = {
                NoteInfoEntry.COLUMN_NOTE_TITLE,
                NoteInfoEntry.COLUMN_COURSE_ID,
                NoteInfoEntry._ID
        };

        String noteOrderBy = NoteInfoEntry.COLUMN_COURSE_ID + "," + NoteInfoEntry.COLUMN_NOTE_TITLE;

        final Cursor noteCursor = db.query(NoteInfoEntry.TABLE_NAME, noteColumns,
                null, null, null, null, noteOrderBy);

        noteRecyclerAdapter.changeCursor(noteCursor);
    }

    private void updateNavHeader() {

        NavigationView navigationView = findViewById(R.id.nav_view);
        View headerView = navigationView.getHeaderView(0);

        TextView txtUsername = headerView.findViewById(R.id.text_username);
        TextView txtEmail = headerView.findViewById(R.id.text_email_address);

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        String username = pref.getString("user_display_name", "");
        String emailAddress = pref.getString("user_email_address", "");

        txtUsername.setText(username);
        txtEmail.setText(emailAddress);
    }


    private void displayNotes() {
        mRecyclerItems.setLayoutManager(mNotesLayoutManager);
        mRecyclerItems.setAdapter(noteRecyclerAdapter);


        selectNavigationMenuItem(R.id.nav_notes);
    }

    private void selectNavigationMenuItem(int id) {
        NavigationView navigationView =  findViewById(R.id.nav_view);
        Menu menu = navigationView.getMenu();
        menu.findItem(id).setChecked(true);
    }

    private void initializeDisplayContent() {

//        final ListView listView = findViewById(R.id.list_note);
//
//        List<NoteInfo> notes = DataManager.getInstance().getNotes();
//        adapterNotes = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, notes);
//
//        listView.setAdapter(adapterNotes);
//
//        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                Intent intent = new Intent(NoteListActivity.this, NoteActivity.class);
//                //NoteInfo note = (NoteInfo) listView.getItemAtPosition(position);
//                intent.putExtra(NoteActivity.NOTE_ID, position);
//                startActivity(intent);
//            }
//        });

        DataManager.loadFromDatabase(mDbOpenHelper);

        mRecyclerItems =   findViewById(R.id.list_items);
        mNotesLayoutManager = new LinearLayoutManager(this);
        //mCoursesLayoutManager = new GridLayoutManager(this, getResources().getInteger(R.integer.course_grid_span));
        mCoursesLayoutManager = new GridLayoutManager(this, 2);
        //List<NoteInfo> notes = DataManager.getInstance().getNotes();
        noteRecyclerAdapter = new NoteRecyclerAdapter(this, null);

        List<CourseInfo> courses = DataManager.getInstance().getCourses();
        mCourseRecyclerAdapter = new CourseRecyclerAdapter(this, courses);

        displayNotes();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main2, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_notes) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

            handleShare();
        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void handleShare() {

    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int i, @Nullable Bundle bundle) {
        CursorLoader loader = null;
        if(i == LOADER_NOTES)
            loader = createLoaderNotes();
        return null;
    }

    private CursorLoader createLoaderNotes() {
        return new CursorLoader(this){
            @Override
            public Cursor loadInBackground() {
                SQLiteDatabase db = mDbOpenHelper.getReadableDatabase();

                final String[] noteColumns = {
                        NoteInfoEntry.getQName(NoteInfoEntry._ID),
                        NoteInfoEntry.COLUMN_NOTE_TITLE,
                        //NoteInfoEntry.getQName(NoteInfoEntry.COLUMN_COURSE_ID),
                        CourseInfoEntry.COLUMN_COURSE_TITLE
                };

                String noteOrderBy = CourseInfoEntry.COLUMN_COURSE_TITLE + "," + NoteInfoEntry.COLUMN_NOTE_TITLE;

//                String tablesWithJoin = NoteInfoEntry.TABLE_NAME +" JOIN "+ CourseInfoEntry.TABLE_NAME +" ON"
//                        + NoteInfoEntry.TABLE_NAME + "." + NoteInfoEntry.COLUMN_COURSE_ID + " = " + CourseInfoEntry.TABLE_NAME + " . "
//                        + CourseInfoEntry.COLUMN_COURSE_ID;
                String tablesWithJoin = NoteInfoEntry.TABLE_NAME +" JOIN "+ CourseInfoEntry.TABLE_NAME +" ON "
                        + NoteInfoEntry.getQName( NoteInfoEntry.COLUMN_COURSE_ID) + " = "
                        + CourseInfoEntry.getQName(CourseInfoEntry.COLUMN_COURSE_ID);

                return db.query(tablesWithJoin, noteColumns,
                        null, null, null, null, noteOrderBy);
            }
        };
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor cursor) {
        if(loader.getId() == LOADER_NOTES){
            noteRecyclerAdapter.changeCursor(cursor);
        }

    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        if(loader.getId() == LOADER_NOTES){
            noteRecyclerAdapter.changeCursor(null);
        }
    }
}
