package com.example.noteapp;

import android.provider.BaseColumns;

public final class NoteKeeperDatabaseContract {
    private NoteKeeperDatabaseContract() {}

    public static final class CourseInfoEntry implements BaseColumns {

        public static final String TABLE_NAME = "course_info";
        public static final String COLUMN_COURSE_ID = "course_id";
        public static final String COLUMN_COURSE_TITLE = "course_title";

        public static final String INDEX1 = TABLE_NAME + "_index1";
        public static final String SQL_CREATE_INDEX1 =
                "CREATE INDEX " + INDEX1 + " ON " + TABLE_NAME +
                        "(" + COLUMN_COURSE_TITLE + ")";

        public static final String getQName(String columnName){

            return TABLE_NAME + "." + columnName;
        }

        public static  final String SQL_CREATE_TABLE = "CREATE TABLE "+ TABLE_NAME + " (" +
                _ID + " INTEGER PRIMARY KEY, " +
                COLUMN_COURSE_ID + " TEXT NOT NULL UNIQUE, " + COLUMN_COURSE_TITLE + " TEXT NOT NULL)";

    }

    public static final class NoteInfoEntry implements BaseColumns {

        public static final String TABLE_NAME = "note_info";
        public static final String COLUMN_NOTE_TITLE = "note_title";
        public static final String COLUMN_NOTE_TEXT = "note_text";
        public static final String COLUMN_COURSE_ID = "course_id";

        public static final String INDEX1 = TABLE_NAME + "_index1";
        public static final String SQL_CREATE_INDEX1 =
                "CREATE INDEX " + INDEX1 + " ON " + TABLE_NAME +
                        "(" + COLUMN_NOTE_TITLE + ")";

        public static final String getQName(String columnName){

            return TABLE_NAME + "." + columnName;
        }

        public static  final String SQL_CREATE_TABLE = "CREATE TABLE "+ TABLE_NAME + " (" +
                _ID + " INTEGER PRIMARY KEY, " +
                COLUMN_NOTE_TITLE + " TEXT NOT NULL, " + COLUMN_NOTE_TEXT + " TEXT, " + COLUMN_COURSE_ID + " TEXT NOT NULL)";

    }
}
