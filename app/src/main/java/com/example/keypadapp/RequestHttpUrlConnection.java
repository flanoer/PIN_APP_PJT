package com.example.keypadapp;

import android.content.ContentValues;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

class RequestHttpURLConnection {

    // 로그 태그
    private final String TAG = this.getClass().getName();

    public static String cookies = "";

    public String request(String _url, ContentValues _params, Callback callback) {
        HttpURLConnection urlConn = null;

        // URL 뒤에 붙여서 보낼 파라미터.
        String jsonStr = "";

        /**
         * 1. StringBuffer에 파라미터 연결
         * */
        // 보낼 데이터가 없으면 파라미터를 비운다.
        if (_params == null) {
            _params = new ContentValues();
            // 보낼 데이터가 있으면 파라미터를 채운다.
        } else {
            JSONObject json = new JSONObject();
            String key;
            Object value;

            for (Map.Entry<String, Object> parameter : _params.valueSet()) {
                key = parameter.getKey();
                value = parameter.getValue();
                if("method".equals(key)) continue;

                try {
                    json.put(key,value);
                } catch (Exception e){}
            }

            jsonStr = json.toString().replace("\n||\r\n||\r","");
            Log.i("RHUC","jsonStr check == "+jsonStr);
        }

        /**
         * 2. HttpURLConnection을 통해 web의 데이터를 가져온다.
         * */
        try {
            URL url = new URL(_url);
            urlConn = (HttpURLConnection) url.openConnection();

            // [2-1]. urlConn 설정.
            urlConn.setReadTimeout(10000);
            urlConn.setConnectTimeout(15000);
            urlConn.setRequestMethod(StringUtil.nvl(_params.get("method"),"GET")); // URL 요청에 대한 메소드 설정 : GET/POST.
            urlConn.setDoOutput(true);
            urlConn.setDoInput(true);
            urlConn.setRequestProperty("Content-Type", "application/json; utf-8");
            urlConn.setRequestProperty("Accept", "application/json");
            if(this.cookies != null && this.cookies.length() > 0){
                urlConn.setRequestProperty("Cookie",this.cookies);
            }
            Log.i(TAG,"cookies check == "+ this.cookies);

            //urlConn.connect();

            // [2-2]. parameter 전달 및 데이터 읽어오기.
            PrintWriter pw = new PrintWriter(new OutputStreamWriter(urlConn.getOutputStream()));
            pw.write(jsonStr);
            pw.flush(); // 출력 스트림을 flush. 버퍼링 된 모든 출력 바이트를 강제 실행.
            pw.close(); // 출력 스트림을 닫고 모든 시스템 자원을 해제.

            // [2-3]. 연결 요청 확인.
            // 실패 시 null을 리턴하고 메서드를 종료.
            if (urlConn.getResponseCode() != HttpURLConnection.HTTP_OK)
                return null;

            // [2-4]. 읽어온 결과물 리턴.
            // 요청한 URL의 출력물을 BufferedReader로 받는다.
            BufferedReader reader = new BufferedReader(new InputStreamReader(urlConn.getInputStream(), "UTF-8"));

            //최초 접속후 세션 유지용 쿠키 취득
            Map m = urlConn.getHeaderFields();
            Log.i(TAG,m.toString());
            if(m.containsKey("Set-Cookie")) {
                Collection c = (Collection)m.get("Set-Cookie");
                for(Iterator i = c.iterator(); i.hasNext(); ) {
                    this.cookies += (String)i.next();
                }
                Log.i(TAG,"initial network success save cookie check >>> "+this.cookies);
            }

            // 출력물의 라인과 그 합에 대한 변수.
            String line;
            String page = "";

            // 라인을 받아와 합친다.
            while ((line = reader.readLine()) != null) {
                page += line;
            }

            // 콜백
            callback.callback(page);
            return page;
        } catch (Exception e) { // for openConnection().
            Log.i(TAG,"통신 오류 발생");
            e.printStackTrace();
        } finally {
            if (urlConn != null)
                urlConn.disconnect();
        }
        return null;
    }
}