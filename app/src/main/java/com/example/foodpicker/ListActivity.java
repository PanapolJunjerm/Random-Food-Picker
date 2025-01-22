package com.example.foodpicker;

import static android.provider.BaseColumns._ID;
import static com.example.foodpicker.Constants.ISACTIVE;
import static com.example.foodpicker.Constants.TABLE_NAME;
import static com.example.foodpicker.Constants.TITLE;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.HashMap;

public class ListActivity extends AppCompatActivity {

    private EventsData events;
    private Cursor cursor;
    private ImageButton home_btn, active_btn, deactive_btn, delete_all_btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list);

        //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        home_btn = findViewById(R.id.return_to_home);
        active_btn = findViewById(R.id.active_all_btn);
        deactive_btn = findViewById(R.id.deactive_all_btn);
        delete_all_btn = findViewById(R.id.delete_all_btn);
        events = new EventsData(ListActivity.this);
        updateList();
        home_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeActivity();
            }
        });
        active_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                activeAllPopup();
            }
        });
        deactive_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deactiveAllPopup();
            }
        });
        delete_all_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteAllPopup();
            }
        });
    }

    private void updateList(){
        cursor = getEvents();
        showEvents(cursor);
        cursor.close();
    }

    private Cursor getEvents() {
        String[] FROM = {_ID, TITLE, ISACTIVE};
        SQLiteDatabase db = events.getReadableDatabase();
        Cursor cursor = db.query(TABLE_NAME, FROM, null, null, null, null, null);
        return cursor;
    }

    private void toggleActive(String id, String isActive) {
        SQLiteDatabase db = events.getWritableDatabase();
        ContentValues values = new ContentValues();
        if(isActive.equals("0")){
            values.put("isActive", 1);
        }
        else values.put("isActive", 0);

        db.update("events", values, "_ID=?", new String[]{id});
        updateList();

    }

    private void changeActivity(){
        LayoutInflater inflater = LayoutInflater.from(this);
        View popupView = inflater.inflate(R.layout.delete_popup_layout, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(popupView);
        builder.setTitle("Go to home" + " ?");
        builder.setPositiveButton(R.string.ok, (dialog, which) -> {
            Intent intent = new Intent(ListActivity.this, MainActivity.class);
            startActivity(intent);
        });
        builder.setNegativeButton(R.string.cancel, (dialog, which) -> {
            dialog.dismiss();
        });
        builder.create().show();
    }


    private void showEvents(Cursor cursor){
        final ListView listView = findViewById(R.id.listView);
        final ArrayList<HashMap<String, String>> MyArrList = new ArrayList<>();
        HashMap<String, String> map;
        while(cursor.moveToNext()){
            map = new HashMap<String, String>();
            map.put("id", String.valueOf(cursor.getInt(0)));
            map.put("title", cursor.getString(1));
            map.put("isActive", cursor.getString(2));
            MyArrList.add(map);
        }
        SimpleAdapter simpleAdapter = new SimpleAdapter(
                ListActivity.this,
                MyArrList,
                R.layout.actity_column,
                new String[]{"id", "title", "isActive", "delete"},
                new int[]{R.id.col_id, R.id.col_title,R.id.col_active, R.id.col_del});

        simpleAdapter.setViewBinder(new SimpleAdapter.ViewBinder() {
            @Override
            public boolean setViewValue(View view, Object data, String textRepresentation) {
                Log.d("ViewBinder", "Processing View ID: " + view.getId());
                if (view.getId() == R.id.col_active) {
                    Log.d("ViewBinder", "Processing col_active");
                    Button btn = (Button) view;
                    String isActiveValue = (String) data;

                    if ("1".equals(isActiveValue)) {
                        btn.setBackgroundColor(getResources().getColor(android.R.color.holo_green_light));
                        btn.setText(R.string.active);
                    } else {
                        btn.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
                        btn.setText(R.string.inactive);
                    }

                    btn.setOnClickListener(v -> {

                        int position = listView.getPositionForView(view);
                        if (position != ListView.INVALID_POSITION) {

                            String id = MyArrList.get(position).get("id");
                            toggleActive(id, isActiveValue);

                        }
                    });

                    return true;
                }
                else if (view.getId() == R.id.col_del) {
                    Log.d("ViewBinder", "Processing col_delete");
                    Button btn = (Button) view;

                    btn.setOnClickListener(v -> {
                        int position = listView.getPositionForView(v);
                        if (position != ListView.INVALID_POSITION) {
                            String id = MyArrList.get(position).get("id");
                            String title = MyArrList.get(position).get("title");
                            deletePopup(id, title);
                        }
                    });

                    return true;
                }
                Log.d("ViewBinder", "No matching View ID found");
                return false;
            }
        });
        listView.setAdapter(simpleAdapter);
    }
    private void deleteEvent(String id, String title) {
        SQLiteDatabase db = events.getWritableDatabase();
        int rowsDeleted = db.delete("events", "_ID=?", new String[]{id});
        if (rowsDeleted > 0) {
            Toast.makeText(this, getString(R.string.deleted) + " " + title + " " + getString(R.string.successfully) , Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, getString(R.string.delete_fail) + " " + title, Toast.LENGTH_SHORT).show();
        }
        db.execSQL("UPDATE " + TABLE_NAME + " SET _ID = _ID - 1 WHERE _ID > ?", new String[]{id});
        updateList();
    }
    private void deleteAll(){
        SQLiteDatabase db = events.getWritableDatabase();
        int rowDeleted = db.delete(TABLE_NAME, null, null);
        Toast.makeText(this, getString(R.string.deleted) + " " + rowDeleted + " " + getString(R.string.rows), Toast.LENGTH_SHORT).show();
        updateList();
    }
    private void activeAll(){
        SQLiteDatabase db = events.getWritableDatabase();
        //db.execSQL("UPDATE " + TABLE_NAME + " SET " + ISACTIVE + " = 1", null);
        ContentValues values = new ContentValues();
        values.put("ISACTIVE", 1);
        db.update(TABLE_NAME, values, null, null);
        updateList();
    }
    private void deactiveAll(){
        SQLiteDatabase db = events.getWritableDatabase();
        //db.execSQL("UPDATE " + TABLE_NAME + " SET " + ISACTIVE + " = 0", null);
        ContentValues values = new ContentValues();
        values.put("ISACTIVE", 0);
        db.update(TABLE_NAME, values, null, null);
        updateList();
    }

    private void deletePopup(String id, String title){
        LayoutInflater inflater = LayoutInflater.from(this);
        View popupView = inflater.inflate(R.layout.delete_popup_layout, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(popupView);
        builder.setTitle(getString(R.string.delete) + title + " ?");
        builder.setPositiveButton(R.string.ok, (dialog, which) -> {
            deleteEvent(id, title);
        });
        builder.setNegativeButton(R.string.cancel, (dialog, which) -> {
            dialog.dismiss();
        });
        builder.create().show();
    }
    private void deleteAllPopup(){
        LayoutInflater inflater = LayoutInflater.from(this);
        View popupView = inflater.inflate(R.layout.delete_popup_layout, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(popupView);
        builder.setTitle(getString(R.string.delete_all) + " ?");
        builder.setPositiveButton(R.string.ok, (dialog, which) -> {
            deleteAll();
        });
        builder.setNegativeButton(R.string.cancel, (dialog, which) -> {
            dialog.dismiss();
        });
        builder.create().show();
    }
    private void activeAllPopup(){
        LayoutInflater inflater = LayoutInflater.from(this);
        View popupView = inflater.inflate(R.layout.delete_popup_layout, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(popupView);
        builder.setTitle(getString(R.string.activate_all) + " ?");
        builder.setPositiveButton(R.string.ok, (dialog, which) -> {
            activeAll();
        });
        builder.setNegativeButton(R.string.cancel, (dialog, which) -> {
            dialog.dismiss();
        });
        builder.create().show();
    }
    private void deactiveAllPopup(){
        LayoutInflater inflater = LayoutInflater.from(this);
        View popupView = inflater.inflate(R.layout.delete_popup_layout, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(popupView);
        builder.setTitle(getString(R.string.deactivate_all) + " ?");
        builder.setPositiveButton(R.string.ok, (dialog, which) -> {
            deactiveAll();
        });
        builder.setNegativeButton(R.string.cancel, (dialog, which) -> {
            dialog.dismiss();
        });
        builder.create().show();
    }
}
