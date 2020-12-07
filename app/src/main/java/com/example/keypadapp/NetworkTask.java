package com.example.keypadapp;

import android.content.ContentValues;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;

public class NetworkTask extends AsyncTask<Void, Void, String> {

    // 로그 태그
    private final String TAG = this.getClass().getName();

    String url;
    ContentValues values;
    Callback callback;
    RequestHttpURLConnection requestHttpURLConnection;

    public NetworkTask(String url, ContentValues values, Callback callback){
        this.url = url;
        this.values = values;
        this.callback = callback;
        if(requestHttpURLConnection == null) { requestHttpURLConnection = new RequestHttpURLConnection(); }
    }

    @Override
    protected String doInBackground(Void... params){
        String result = "";
        result = requestHttpURLConnection.request(this.url, this.values, this.callback);
        return result;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void onPostExecute(String result) {
        // 통신이 완료되면 호출됩니다.
        // 결과에 따른 UI 수정 등은 여기서 합니다.
        // Toast.makeText(getApplicationContext(), result, Toast.LENGTH_LONG).show();
        //callback.callback(result);
        Log.i(TAG,"result === "+result);
    }

    public boolean isConnected(Context ct){
        ConnectivityManager cm = (ConnectivityManager)ct.getSystemService(ct.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        if(ni != null && ni.isConnected()){
            return true;
        } else {
            return false;
        }
    }
}
