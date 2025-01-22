package com.example.foodpicker;

import static android.provider.BaseColumns._ID;
import static com.example.foodpicker.Constants.ISACTIVE;
import static com.example.foodpicker.Constants.TITLE;
import static com.example.foodpicker.Constants.TABLE_NAME;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Random;

public class MainActivity extends AppCompatActivity {
    private EventsData events;
    // Define possible slot results
    private TextView tvSlot;
    private ImageButton add_btn, list_btn, settingBtn;
    private Random random = new Random();
    private Handler handler = new Handler();
    private boolean isSpinning = false;
    private ArrayList<String> items;
    SeekBar verticalSeekBar;
    Cursor cursor;
    private SoundPool soundPool;
    private int endSoundId;

    private int spinDelayStatic = 5;
    private int durationStatic = 2000;
    private int spinDelay;
    private int currentIndex = 0;
    private final int highSpeed = 1;
    private final int midSpeed = 5;
    private final int lowSpeed = 40;
    private final int highDuration = 4000;
    private final int midDuration = 2000;
    private final int lowDuration = 1000;
    int startTime;


    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME) // ใช้สำหรับเกม
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();
        soundPool = new SoundPool.Builder()
                .setMaxStreams(1)
                .setAudioAttributes(audioAttributes)
                .build();
        endSoundId = soundPool.load(this, R.raw.end, 1);

        settingBtn = findViewById(R.id.setting_btn);
        settingBtn.setOnClickListener(v -> {
            showSettingPopup();
        });
        add_btn = findViewById(R.id.item_add);
        add_btn.setOnClickListener(v -> {
            showAddPopup();
        });
        verticalSeekBar = findViewById(R.id.verticalSeekBar);
        tvSlot = findViewById(R.id.slotText);
        events = new EventsData(MainActivity.this);
        cursor = getEvents();
        //items = new ArrayList<>();
        items = getSlotItem(cursor);
        cursor.close();
        verticalSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {}

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                if (isSpinning) {
                    seekBar.setProgress(seekBar.getProgress()); // คืนค่าเดิม
                }
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (!isSpinning) {
                    if (seekBar.getProgress() == seekBar.getMax()) {
                        cursor = getEvents();
                        items.clear();
                        items = getSlotItem(cursor);
                        cursor.close();
                        isSpinning = true;
                        verticalSeekBar.setEnabled(false);
                        spinSlot(seekBar);
                    }
                }
            }
        });
        list_btn = findViewById(R.id.item_list);
        list_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeActivity();
            }
        });

    }

    private void changeActivity(){
        LayoutInflater inflater = LayoutInflater.from(this);
        View popupView = inflater.inflate(R.layout.delete_popup_layout, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(popupView);
        builder.setTitle("Go to list" + " ?");
        builder.setPositiveButton(R.string.ok, (dialog, which) -> {
            Intent intent = new Intent(MainActivity.this, ListActivity.class);
            startActivity(intent);
        });
        builder.setNegativeButton(R.string.cancel, (dialog, which) -> {
            dialog.dismiss();
        });
        builder.create().show();
    }

    private void spinSlot(SeekBar seekBar) {
        isSpinning = true;
        spinDelay = spinDelayStatic;
        int duration = durationStatic;

        int firstStage = duration/40;
        int secondStage = duration/13;
        int thirdStage = duration/7;

        startTime = 0;

        handler.post(new Runnable() {
            @Override
            public void run() {
                //items = getSlotItem(cursor);
                ObjectAnimator animator = ObjectAnimator.ofFloat(tvSlot, "translationY", 0f, 100f);
                animator.setDuration(spinDelay); // ระยะเวลาของการเลื่อน
                animator.setInterpolator(new LinearInterpolator());
                animator.start();

                animator.addListener(new android.animation.Animator.AnimatorListener() {
                    @Override
                    public void onAnimationEnd(android.animation.Animator animation) {
                        if(!items.isEmpty()){
                            currentIndex = random.nextInt(items.size());
                            tvSlot.setText(items.get(currentIndex));
                        }

                        tvSlot.setTranslationY(-100f);
                        ObjectAnimator resetAnimator = ObjectAnimator.ofFloat(tvSlot, "translationY", -100f, 0f);
                        resetAnimator.setDuration(spinDelay);
                        resetAnimator.setInterpolator(new LinearInterpolator());
                        resetAnimator.start();
                    }

                    @Override
                    public void onAnimationStart(android.animation.Animator animation) {}

                    @Override
                    public void onAnimationCancel(android.animation.Animator animation) {}

                    @Override
                    public void onAnimationRepeat(android.animation.Animator animation) {}
                });

                startTime += spinDelay;

                if (spinDelay < firstStage) {
                    spinDelay += 5;
                } else if (spinDelay < secondStage) {
                    spinDelay += (int)(spinDelay * 0.2);
                } else if (spinDelay < thirdStage) {
                    spinDelay += (int)(spinDelay * 0.3);
                } else {
                    spinDelay += 80;
                    if (startTime >= duration) {
                        isSpinning = false;
                        ObjectAnimator resetAnimator = ObjectAnimator.ofInt(seekBar, "progress", seekBar.getProgress(), 0);
                        resetAnimator.setDuration(500);
                        resetAnimator.start();
                    }
                }

                // ตรวจสอบสถานะการหมุน
                if (isSpinning) {
                    handler.postDelayed(this, spinDelay + spinDelay); // รอจนจบแอนิเมชันก่อนหมุนรอบใหม่
                } else {
                    //soundPool.stop(spinningSoundId);
                    verticalSeekBar.setEnabled(true);
                    soundPool.play(endSoundId, 1, 1, 1, 0, 1);
                }
            }
        });
    }

    private void addEvent(String Item) {
        SQLiteDatabase db = events.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(TITLE, Item);
        values.put(ISACTIVE, 1);
        db.insert(TABLE_NAME, null, values);
    }

    private ArrayList<String> getSlotItem(Cursor cursor) {
        ArrayList<String> myArrList = new ArrayList<>();
        while (cursor.moveToNext()) {
            if(cursor.getString(2).equals("1")) myArrList.add(cursor.getString(1)); // index 1 คือคอลัมน์ที่ 2
        }
        return myArrList;
    }

    private Cursor getEvents() {
        String[] FROM = {_ID, TITLE, ISACTIVE};
        SQLiteDatabase db = events.getReadableDatabase();
        Cursor cursor = db.query(TABLE_NAME, FROM, null, null, null, null, null);
        return cursor;
    }
    private void showSettingPopup(){

        LayoutInflater inflater = LayoutInflater.from(this);
        View popupView = inflater.inflate(R.layout.setting_popup_layout, null);
        TextView speedText = popupView.findViewById(R.id.speed_text);
        TextView durationText = popupView.findViewById(R.id.duration_text);
        if(spinDelayStatic == highSpeed){
            speedText.setText(R.string.high);
        }
        else if(spinDelayStatic == midSpeed){
            speedText.setText(R.string.medium);
        }
        else if(spinDelayStatic == lowSpeed){
            speedText.setText(R.string.low);
        }
        if(durationStatic == lowDuration){
            durationText.setText(R.string.low);
        }
        else if(durationStatic == midDuration){
            durationText.setText(R.string.medium);
        }
        else if(durationStatic == highDuration){
            durationText.setText(R.string.high);
        }
        Button speedIncreaseBtn = popupView.findViewById(R.id.speed_increase_btn);
        Button speedDecreaseBtn = popupView.findViewById(R.id.speed_decrease_btn);
        Button durationIncreaseBtn = popupView.findViewById(R.id.duration_increase_btn);
        Button durationDecreaseBtn = popupView.findViewById(R.id.duration_decrease_btn);

        speedIncreaseBtn.setOnClickListener( v -> {
            if(spinDelayStatic == midSpeed){
                spinDelayStatic = highSpeed;
                speedText.setText(R.string.high);
            }
            else if(spinDelayStatic == lowSpeed){
                spinDelayStatic = midSpeed;
                speedText.setText(R.string.medium);
            }
        });
        speedDecreaseBtn.setOnClickListener( v -> {
            if(spinDelayStatic == midSpeed){
                spinDelayStatic = lowSpeed;
                speedText.setText(R.string.low);
            }
            else if(spinDelayStatic == highSpeed){
                spinDelayStatic = midSpeed;
                speedText.setText(R.string.medium);
            }
        });
        durationIncreaseBtn.setOnClickListener( v -> {
            if(durationStatic == midDuration){
                durationStatic = highDuration;
                durationText.setText(R.string.high);
            }
            else if(durationStatic == lowDuration){
                durationStatic = midDuration;
                durationText.setText(R.string.medium);
            }
        });
        durationDecreaseBtn.setOnClickListener( v -> {
            if(durationStatic == midDuration){
                durationStatic = lowDuration;
                durationText.setText(R.string.low);
            }
            else if(durationStatic == highDuration){
                durationStatic = midDuration;
                durationText.setText(R.string.medium);
            }
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(popupView);
        builder.setTitle(R.string.setting);

        builder.setNegativeButton(R.string.ok, (dialog, which) -> {
            dialog.dismiss();
        });
        builder.create().show();
    }

    private void showAddPopup() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View popupView = inflater.inflate(R.layout.popup_layout, null);

        EditText inputField = popupView.findViewById(R.id.etInput);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(popupView);
        builder.setTitle(R.string.enter_item);

        builder.setPositiveButton(R.string.ok, (dialog, which) -> {
            String inputText = inputField.getText().toString();
            Toast.makeText(this, getString(R.string.add_toast) + inputText, Toast.LENGTH_SHORT).show();
            addEvent(inputText);
            cursor = getEvents();
            items.clear();
            items = getSlotItem(cursor);
            cursor.close();
        });

        builder.setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss());

        builder.create().show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (soundPool != null) {
            soundPool.release();
            soundPool = null;
        }
    }

}
