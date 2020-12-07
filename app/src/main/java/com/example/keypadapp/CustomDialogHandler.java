package com.example.keypadapp;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

public class CustomDialogHandler extends AlertDialog.Builder {

    private String title = "알림";
    private String message;

    public CustomDialogHandler(Context context) {
        super(context);
        this.message = "";
        this.setTitle(this.title);
        this.setMessage(this.message);
    }

    public CustomDialogHandler(Context context, String message) {
        super(context);
        this.message = message;
        this.setTitle(this.title);
        this.setMessage(this.message);
    }

    public CustomDialogHandler(Context context, String title, String message) {
        super(context);
        this.title = title;
        this.message = message;
        this.setTitle(this.title);
        this.setMessage(this.message);
    }

    @Override
    public AlertDialog.Builder setTitle(CharSequence title) {
        return super.setTitle(title);
    }

    @Override
    public AlertDialog.Builder setMessage(CharSequence message) {
        return super.setMessage(message);
    }

    @Override
    public AlertDialog.Builder setPositiveButton(CharSequence text, DialogInterface.OnClickListener listener) {
        return super.setPositiveButton(text, listener);
    }

    @Override
    public AlertDialog.Builder setNegativeButton(CharSequence text, DialogInterface.OnClickListener listener) {
        return super.setNegativeButton(text, listener);
    }
}
