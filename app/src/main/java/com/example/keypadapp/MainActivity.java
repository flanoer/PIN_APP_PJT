package com.example.keypadapp;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.text.InputType;
import android.util.Log;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.widget.EditText;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    // 로그 태그
    private final String TAG = this.getClass().getName();

    EditText editText;
    NaruKeypad keypad;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); //R 은 리소스 아래 layout 에 activitymain 이 있다

        editText = (EditText) findViewById(R.id.textIn);
        keypad = (NaruKeypad) findViewById(R.id.keypad);
        keypad.setActivity(MainActivity.this);

        // prevent system keyboard from appearing when EditText is tapped
        editText.setRawInputType(InputType.TYPE_CLASS_TEXT);
        editText.setTextIsSelectable(false);
        editText.setCursorVisible(false);
        editText.setClickable(false);
        editText.setShowSoftInputOnFocus(false);


        // pass the InputConnection from the EditText to the keyboard
        InputConnection ic = editText.onCreateInputConnection(new EditorInfo());

        keypad.setInputConnection(ic);
    }
}