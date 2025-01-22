package com.example.foodpicker;

import static android.provider.BaseColumns._ID;

import static com.example.foodpicker.Constants.ISACTIVE;
import static com.example.foodpicker.Constants.TABLE_NAME;
import static com.example.foodpicker.Constants.TITLE;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class EventsData extends SQLiteOpenHelper {
    public EventsData(Context ctx){
        super(ctx, "events.db", null, 1);
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_NAME + " ("
                + _ID + " INTEGER PRIMARY KEY, "
                + TITLE + " TEXT, "
                + ISACTIVE + " INTEGER);"
        );
        db.execSQL("INSERT INTO " + TABLE_NAME + " (" + _ID + ", " + TITLE + ", " + ISACTIVE + ") VALUES (1, 'noodle', 1);");
        db.execSQL("INSERT INTO " + TABLE_NAME + " (" + _ID + ", " + TITLE + ", " + ISACTIVE + ") VALUES (2, 'fried rice', 1);");
        db.execSQL("INSERT INTO " + TABLE_NAME + " (" + _ID + ", " + TITLE + ", " + ISACTIVE + ") VALUES (3, 'melon', 1);");
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
        db.execSQL("DROP TABLE IF EXISTS events");
        onCreate(db);
    }
}
