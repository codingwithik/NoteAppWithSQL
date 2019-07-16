package com.example.noteapp;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.noteapp.NoteKeeperDatabaseContract.CourseInfoEntry;
import com.example.noteapp.NoteKeeperDatabaseContract.NoteInfoEntry;

import java.util.List;

public class NoteRecyclerAdapter extends RecyclerView.Adapter<NoteRecyclerAdapter.ViewHolder> {

    private final Context mContext;
    //private final List<NoteInfo> mNotes;
    private Cursor mCursor;
    private final LayoutInflater layoutInflater;
    private int mCoursePos;
    private int mNoteTitlePos;
    private int mIdPos;

    public NoteRecyclerAdapter(Context mContext, Cursor cursor) {
        this.mContext = mContext;
        layoutInflater = LayoutInflater.from(mContext);
        this.mCursor = cursor;
        populateColumnPosition();
    }

    private void populateColumnPosition() {
        if(mCursor == null)
            return;

        //Get column indexes from mCursor
        mCoursePos = mCursor.getColumnIndex(CourseInfoEntry.COLUMN_COURSE_TITLE);
        mNoteTitlePos = mCursor.getColumnIndex(NoteInfoEntry.COLUMN_NOTE_TITLE);
        mIdPos = mCursor.getColumnIndex(NoteInfoEntry._ID);
    }

    public void changeCursor(Cursor cursor){

        if(mCursor == null)
            mCursor.close();

        mCursor = cursor;
        populateColumnPosition();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View itemView = layoutInflater.inflate(R.layout.item_note_list, viewGroup, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {

        mCursor.moveToPosition(i);
        String course = mCursor.getString(mCoursePos);
        String noteTitle = mCursor.getString(mNoteTitlePos);
        int id = mCursor.getInt(mIdPos);

        //NoteInfo note = mNotes.get(i);
        viewHolder.textCourse.setText(course);
        viewHolder.textTilte.setText(noteTitle);
        viewHolder.mId = id;

    }

    @Override
    public int getItemCount() {
        return mCursor == null ? 0: mCursor.getCount();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        public final TextView textTilte;
        public final TextView textCourse;
        public int mId;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textCourse = itemView.findViewById(R.id.text_course);
            textTilte = itemView.findViewById(R.id.text_title);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Intent intent = new Intent(mContext, NoteActivity.class);
                    intent.putExtra(NoteActivity.NOTE_ID, mId);
                    mContext.startActivity(intent);

                }
            });
        }
    }
}
