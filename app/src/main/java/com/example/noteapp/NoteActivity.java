package com.example.noteapp;

import android.content.ContentValues;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v4.app.LoaderManager;
import android.content.Intent;
import android.support.v4.content.Loader;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.content.CursorLoader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;

import com.example.noteapp.NoteKeeperDatabaseContract.NoteInfoEntry;

import java.util.List;

import static com.example.noteapp.NoteKeeperDatabaseContract.*;

public class NoteActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String NOTE_ID = "com.example.noteapp.NOTE_ID";
    public static final String ORIGINAL_NOTE_COURSE_ID = "com.example.noteapp.ORIGINAL_NOTE_COURSE_ID";
    public static final String ORIGINAL_NOTE_TITLE = "com.example.noteapp.ORIGINAL_NOTE_TITLE";
    public static final String ORIGINAL_NOTE_TEXT = "com.example.noteapp.ORIGINAL_NOTE_TEXT";
    public static final int LOADER_COURSES = 1;
    public static final int ID_NOT_SET = -1;
    public static final int LOADER_NOTES = 0;
    private NoteInfo note;
    private boolean mIsNewNote;
    private Spinner spinner;
    private EditText textNoteTitle;
    private EditText textNoteText;
    private int notePosition;
    private boolean isCanceling;
    private String mOriginalNoteCourseId;
    private String mOriginalNoteText;
    private String mOriginalNoteTitle;
    private int mNoteId;
    private NoteKeeperOpenHelper mDbOpenHelper;
    private Cursor mNoteCursor;
    private int mCourseIdPos;
    private int mNoteTextPos;
    private int mNoteTitlePos;
    private SimpleCursorAdapter mAdapterCourses;
    private boolean mCoursesQueryFinished;
    private boolean mNotesQueryFinished;

    @Override
    protected void onDestroy() {
        mDbOpenHelper.close();
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mDbOpenHelper = new NoteKeeperOpenHelper(this);

        spinner = findViewById(R.id.spinner_courses);

        //List<CourseInfo> courses = DataManager.getInstance().getCourses();
        //ArrayAdapter<CourseInfo> adapterCourses = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, courses);

        mAdapterCourses = new SimpleCursorAdapter(this, android.R.layout.simple_spinner_item, null,
                new String[]{CourseInfoEntry.COLUMN_COURSE_TITLE},
                new int[] {android.R.id.text1}, 0);

        mAdapterCourses.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(mAdapterCourses);

        //loadCourseData();

        getLoaderManager().initLoader(LOADER_COURSES, null, (android.app.LoaderManager.LoaderCallbacks<Object>) this);

        readDisplayStateValues();

        if(savedInstanceState == null){

            saveOriginalStateValues();
        }else{

            restoreOriginalNoteValues(savedInstanceState);
        }


        textNoteTitle = findViewById(R.id.text_note_title);
        textNoteText = findViewById(R.id.text_note_text);

        if(!mIsNewNote)
            //displayNote();
            //loadNoteData();
            getLoaderManager().initLoader(LOADER_NOTES, null, (android.app.LoaderManager.LoaderCallbacks<Object>) this);

    }

    private void loadCourseData() {

        SQLiteDatabase db = mDbOpenHelper.getReadableDatabase();
        String[] courseColumns = {
                CourseInfoEntry.COLUMN_COURSE_TITLE,
                CourseInfoEntry.COLUMN_COURSE_ID,
                CourseInfoEntry._ID
        };

        Cursor cursor = db.query(CourseInfoEntry.TABLE_NAME, courseColumns, null, null,
                null, null, CourseInfoEntry.COLUMN_COURSE_TITLE);

        mAdapterCourses.changeCursor(cursor);
    }

    private void loadNoteData() {

        SQLiteDatabase db = mDbOpenHelper.getReadableDatabase();

        String courseId = "android_intents";
        String titleStart = "dynamic";

        //String selection = NoteInfoEntry.COLUMN_COURSE_ID + " = ? AND "
        //        + NoteInfoEntry.COLUMN_NOTE_TITLE + " LIKE ?";

        String selection = NoteInfoEntry._ID + " = ? ";

        String[] selectionArgs = {Integer.toString(mNoteId)};

        String[] noteColumns = {
                NoteInfoEntry.COLUMN_COURSE_ID,
                NoteInfoEntry.COLUMN_NOTE_TITLE,
                NoteInfoEntry.COLUMN_NOTE_TEXT
        };

        mNoteCursor = db.query(NoteInfoEntry.TABLE_NAME, noteColumns, selection, selectionArgs,
                null, null, null);

        mCourseIdPos = mNoteCursor.getColumnIndex(NoteInfoEntry.COLUMN_COURSE_ID);
        mNoteTitlePos = mNoteCursor.getColumnIndex(NoteInfoEntry.COLUMN_NOTE_TITLE);
        mNoteTextPos = mNoteCursor.getColumnIndex(NoteInfoEntry.COLUMN_NOTE_TEXT);

        mNoteCursor.moveToNext();
        displayNote();
    }

    private void restoreOriginalNoteValues(Bundle savedInstanceState) {
        mOriginalNoteCourseId = savedInstanceState.getString(ORIGINAL_NOTE_COURSE_ID);
        mOriginalNoteTitle = savedInstanceState.getString(ORIGINAL_NOTE_TITLE);
        mOriginalNoteText = savedInstanceState.getString(ORIGINAL_NOTE_TEXT);
    }

    private void saveOriginalStateValues() {
        if(mIsNewNote){
            return;
        }

        mOriginalNoteCourseId = note.getCourse().getCourseId();
        mOriginalNoteTitle = note.getTitle();
        mOriginalNoteText = note.getText();
    }

    private void displayNote() {
        String courseId = mNoteCursor.getString(mCourseIdPos);
        String noteTitle = mNoteCursor.getString(mNoteTitlePos);
        String noteText = mNoteCursor.getString(mNoteTextPos);

        //List<CourseInfo> courses = DataManager.getInstance().getCourses();
        //CourseInfo course = DataManager.getInstance().getCourse(courseId);
        int courseIndex = getIndexOfCourseId(courseId);
        spinner.setSelection(courseIndex);
        textNoteTitle.setText(noteTitle);
        textNoteText.setText(noteText);

    }

    private int getIndexOfCourseId(String courseId) {
        Cursor cursor = mAdapterCourses.getCursor();
        int courseIdPos = cursor.getColumnIndex(CourseInfoEntry.COLUMN_COURSE_ID);
        int courseRowIndex = 0;

        boolean more = cursor.moveToFirst();
        while(more){
            String cursorCourseId = cursor.getString(courseIdPos);
            if(courseId.equals(cursorCourseId)){
                break;
            }

            courseRowIndex++;

            more = cursor.moveToNext();
        }
        return courseRowIndex;
    }

    private void readDisplayStateValues() {
        Intent intent = getIntent();
        mNoteId = intent.getIntExtra(NOTE_ID, ID_NOT_SET);
        mIsNewNote = mNoteId == ID_NOT_SET;
        if(!mIsNewNote){

            createNewNote();

        }else{

            //note = DataManager.getInstance().getNotes().get(mNoteId);
        }

    }

    private void createNewNote() {
        //DataManager dm = DataManager.getInstance();
        //notePosition = dm.createNewNote();
        //note = dm.getNotes().get(notePosition);

        ContentValues values = new ContentValues();
        values.put(NoteInfoEntry.COLUMN_COURSE_ID, "");
        values.put(NoteInfoEntry.COLUMN_NOTE_TITLE, "");
        values.put(NoteInfoEntry.COLUMN_NOTE_TEXT, "");

        SQLiteDatabase db = mDbOpenHelper.getWritableDatabase();
        mNoteId = (int) db.insert(NoteInfoEntry.TABLE_NAME, null, values);
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(ORIGINAL_NOTE_COURSE_ID, mOriginalNoteCourseId);
        outState.putString(ORIGINAL_NOTE_TITLE, mOriginalNoteTitle);
        outState.putString(ORIGINAL_NOTE_TEXT, mOriginalNoteText);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_send_email) {
            sendEmail();
            return true;
        }else if (id == R.id.action_cancel) {
            isCanceling = true;
            finish();
        }else if (id == R.id.action_next) {

            moveNext();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem item = menu.findItem(R.id.action_next);
        int lastNoteIndex = DataManager.getInstance().getNotes().size() - 1;
        item.setEnabled(mNoteId < lastNoteIndex);
        return super.onPrepareOptionsMenu(menu);
    }

    private void moveNext() {
        saveNote();

        ++mNoteId;
        note = DataManager.getInstance().getNotes().get(mNoteId);

        saveOriginalStateValues();
        displayNote();
        invalidateOptionsMenu();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(isCanceling){
            if(mIsNewNote) {
                //DataManager.getInstance().removeNote(notePosition);
                deleteNoteFromDatabase();
            }else{
                storePreviousNoteValues();
            }
        }else{

            saveNote();
        }

    }

    private void deleteNoteFromDatabase() {
        final String selection = NoteInfoEntry._ID + " = ?";
        final String[] selectionArgs = {Integer.toString(mNoteId)};

        AsyncTask task = new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] objects) {
                SQLiteDatabase db = mDbOpenHelper.getWritableDatabase();
                db.delete(NoteInfoEntry.TABLE_NAME, selection, selectionArgs);

                return null;
            }
        };

        task.execute();


    }

    private void storePreviousNoteValues() {
        CourseInfo course = DataManager.getInstance().getCourse(mOriginalNoteCourseId);
        note.setCourse(course);
        note.setTitle(mOriginalNoteTitle);
        note.setText(mOriginalNoteText);
    }

    private void saveNote() {
        //note.setCourse((CourseInfo) spinner.getSelectedItem());
        String courseId = selectedCourseId();
        String noteTitle = textNoteTitle.getText().toString();
        String noteText = textNoteText.getText().toString();
        saveNoteToDatabase(courseId, noteTitle, noteText);
    }

    private String selectedCourseId() {

        int selectedPosition = spinner.getSelectedItemPosition();
        Cursor cursor = mAdapterCourses.getCursor();
        cursor.moveToPosition(selectedPosition);
        int courseIdPos = cursor.getColumnIndex(CourseInfoEntry.COLUMN_COURSE_ID);
        String courseId = cursor.getString(courseIdPos);
        return courseId;
    }

    private void saveNoteToDatabase(String courseId, String noteTitle, String noteText){

        String selection = NoteInfoEntry._ID + " = ?";
        String[] selectionArgs = {Integer.toString(mNoteId)};

        ContentValues values = new ContentValues();
        values.put(NoteInfoEntry.COLUMN_COURSE_ID, courseId);
        values.put(NoteInfoEntry.COLUMN_NOTE_TITLE, noteTitle);
        values.put(NoteInfoEntry.COLUMN_NOTE_TEXT, noteText);

        SQLiteDatabase db = mDbOpenHelper.getWritableDatabase();
        db.update(NoteInfoEntry.TABLE_NAME, values, selection, selectionArgs);
    }

    private void sendEmail() {
        CourseInfo course = (CourseInfo) spinner.getSelectedItem();
        String subject = textNoteTitle.getText().toString();
        String text = "Checkout what I learned in the pluralsight course \n" +
                course.getTitle() +"\"\n" + textNoteText.getText().toString();

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("message/rfc2822");
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        intent.putExtra(Intent.EXTRA_TEXT, text);
        startActivity(intent);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        CursorLoader loader = null;

        if(id == LOADER_NOTES)
            loader = createLoaderNotes();

        else if(id == LOADER_COURSES)
            loader = createLoaderCourses();

        return loader;
    }

    private CursorLoader createLoaderCourses() {
        mCoursesQueryFinished = false;
        Uri uri = Uri.parse("content://com.example.noteapp.provider");
        String[] courseColumns = {
                CourseInfoEntry.COLUMN_COURSE_TITLE,
                CourseInfoEntry.COLUMN_COURSE_ID,
                CourseInfoEntry._ID
        };
//        return  new CursorLoader(this, uri, courseColumns, null, null, CourseInfoEntry.COLUMN_COURSE_TITLE);
        return new CursorLoader(this){
            @Override
            public Cursor loadInBackground() {
                SQLiteDatabase db = mDbOpenHelper.getReadableDatabase();
                String[] courseColumns = {
                        CourseInfoEntry.COLUMN_COURSE_TITLE,
                        CourseInfoEntry.COLUMN_COURSE_ID,
                        CourseInfoEntry._ID
                };

                return db.query(CourseInfoEntry.TABLE_NAME, courseColumns, null, null,
                        null, null, CourseInfoEntry.COLUMN_COURSE_TITLE);

            }
        };
    }


    private CursorLoader createLoaderNotes() {
        mNotesQueryFinished = false;
        return new CursorLoader(this){
            @Override
            public Cursor loadInBackground() {

                SQLiteDatabase db = mDbOpenHelper.getReadableDatabase();

                String courseId = "android_intents";
                String titleStart = "dynamic";

                //String selection = NoteInfoEntry.COLUMN_COURSE_ID + " = ? AND "
                //        + NoteInfoEntry.COLUMN_NOTE_TITLE + " LIKE ?";

                String selection = NoteInfoEntry._ID + " = ? ";

                String[] selectionArgs = {Integer.toString(mNoteId)};

                String[] noteColumns = {
                        NoteInfoEntry.COLUMN_COURSE_ID,
                        NoteInfoEntry.COLUMN_NOTE_TITLE,
                        NoteInfoEntry.COLUMN_NOTE_TEXT
                };

                return db.query(NoteInfoEntry.TABLE_NAME, noteColumns, selection, selectionArgs,
                        null, null, null);

            }
        };
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor cursor) {
        if(loader.getId() == LOADER_NOTES)
            loaderFinishedNotes(cursor);
        else if(loader.getId() == LOADER_COURSES){
            mAdapterCourses.changeCursor(cursor);
            mCoursesQueryFinished = true;
            displayNoteWhenQueriesFinished();
        }
    }

    private void loaderFinishedNotes(Cursor cursor) {

        mNoteCursor = cursor;
        mCourseIdPos = mNoteCursor.getColumnIndex(NoteInfoEntry.COLUMN_COURSE_ID);
        mNoteTitlePos = mNoteCursor.getColumnIndex(NoteInfoEntry.COLUMN_NOTE_TITLE);
        mNoteTextPos = mNoteCursor.getColumnIndex(NoteInfoEntry.COLUMN_NOTE_TEXT);

        mNoteCursor.moveToNext();
        mNotesQueryFinished = true;
        //displayNote();
        displayNoteWhenQueriesFinished();
    }

    private void displayNoteWhenQueriesFinished() {
        if(mNotesQueryFinished && mCoursesQueryFinished)
            displayNote();
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        if(loader.getId() == LOADER_NOTES){
            if(mNoteCursor != null)
                mNoteCursor.close();
        }else if(loader.getId() == LOADER_COURSES){
            mAdapterCourses.changeCursor(null);
        }
    }


}
