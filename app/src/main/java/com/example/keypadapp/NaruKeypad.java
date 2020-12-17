package com.example.keypadapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AnimationSet;
import android.view.inputmethod.InputConnection;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.Nullable;


import org.apche.commons.android.codec.binary.Base64;
import org.json.JSONObject;

import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.crypto.Cipher;

public class NaruKeypad extends LinearLayout implements View.OnClickListener {

    // 키패드 암호화 알고리즘
    private static final String ALGORITHM = "RSA/ECB/PKCS1Padding";

    // 로그 태그
    private final String TAG = this.getClass().getName();

    // 숫자 키패드가 그려지는 레이아웃
    private LinearLayout lin1;
    private LinearLayout lin2;
    private LinearLayout lin3;

    // 숫자 키패드 저장용 리스트
    private List<ImageButton> btnList;
    
    // 기능 버튼
    private Button mButtonShfl;
    private Button mButtonDel;
    private Button mButtonOk;

    // 세션 확인용 토큰
    private String token;
    
    // 랜덤 핀코드(4 * 10)
    private String pinCode;
    
    // 암호화용 공개키
    private PublicKey publicKey;

    // 버튼 인풋값(1~9) 버퍼
    public StringBuffer bufferedInputData = new StringBuffer();

    // 버튼에 할당된 랜덤한 인풋값 버퍼(서버 전송용)
    public StringBuffer bufferedPinData = new StringBuffer();

    // 버튼 눌렀을때 화면에 어떤 버튼이 눌렸는지 보여주기 위한 객체
    SparseArray<String> keyValues = new SparseArray<String>();

    // 버튼 눌렀을때 실제로 서버에 전송할 버튼 데이터가 저장되있는 객체
    SparseArray<String> realDataValues = new SparseArray<String>();

    // 입력되는 창
    InputConnection inputConnection;

    //
    private Activity activity;

    //
    private NetworkTask networkTask;
    Callback callback;

    // 키패드 생성자
    public NaruKeypad(Context context) {
        this(context, null, 0);
    }

    public NaruKeypad(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public NaruKeypad(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    void setActivity(Activity activity){ this.activity = activity; }

    // 키패드 시작 메소드
    private void init(Context context, AttributeSet attrs) {
        Log.i(TAG,"NaruKeypad init method invoke");

        try {
            callback = new Callback() {
                @Override
                public void callback(Object object) {
                    try {
                        getRandomPinCodeCallback(object);
                    } catch (Exception e){
                        e.printStackTrace();
                    }
                }
            };

            // 토큰, 핀코드, 공개키 호출
            networkTask = new NetworkTask("http://192.168.0.117:8080/getRandomPinCode", null, callback);
            networkTask.execute();
        } catch(Exception e) {
            CustomDialogHandler cdh = new CustomDialogHandler(this.getContext(), "시스템 오류","통신 실패");
            cdh.setPositiveButton("확인",(dialog, which) -> {
                dialog.dismiss();
                activity.finish();
            });
            AlertDialog ad = cdh.create();
            ad.show();
        }

        // initialize buttons
        LayoutInflater.from(context).inflate(R.layout.keypad, this, true);

        // 숫자키패드
        lin1 = (LinearLayout) findViewById(R.id.row1);
        lin2 = (LinearLayout) findViewById(R.id.row2);
        lin3 = (LinearLayout) findViewById(R.id.row3);

        // 기능버튼
        mButtonShfl = (Button) findViewById(R.id.button_shuffle);
        mButtonDel = (Button) findViewById(R.id.button_delete);
        mButtonOk = (Button) findViewById(R.id.button_ok);

        // 기능버튼 클릭했을 때 동작
        mButtonDel.setOnClickListener(this);
        mButtonOk.setOnClickListener(this);
        mButtonShfl.setOnClickListener(this);
    }

    // 현재 객체(NaruKeypad) 내에 데이터 세팅
    public void setParentVar(String token, String pinCode, String encCertPK) {
        this.token = token;
        this.pinCode = pinCode;

        // 공개키 세팅
        try {
            byte[] bArr = Base64.decodeBase64(encCertPK);
            publicKey = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(bArr));
        } catch (Exception e) {
            Log.e("NaruKeypad","setParentVar generate publicKey Exception");
            e.printStackTrace();
        }
    }

    // 입력폼 객체 연결
    public void setInputConnection(InputConnection ic) {
        this.inputConnection = ic;
    }

    // 서버에서 받아온 이미지로 키패드 생성
    public void makeImgBtn() {
//    public void makeImgBtn(JSONArray pinImgArr) {
        Log.d("NaruKeypad","makeImgBtn invoke");
        Context ct = this.getContext();

        // 이미지버튼이 들어간 객체
        btnList = new ArrayList<ImageButton>();

        // 이미지버튼의 레이아웃 설정
        LayoutParams lp = new LayoutParams(0, LayoutParams.WRAP_CONTENT);
        lp.weight = 1;

        // 0~9 키패드 세팅
        int i = 0;
        for(int h = 0 ; h < 3 ; h++){
            int j = 0;
            int size = 4;
            for(; j < size ; j++) {
                ImageButton tmpBtn;
                if (h == 0)     tmpBtn = (ImageButton) lin1.getChildAt(j);
                else if(h == 1) tmpBtn = (ImageButton) lin2.getChildAt(j);
                else            tmpBtn = (ImageButton) lin3.getChildAt(j);
                if(!(h > 1 && j > 1)) {
                    keyValues.put(tmpBtn.getId(), ""+i/4);
                    realDataValues.put(tmpBtn.getId(), this.pinCode.substring(i,i+=4));
                }
                tmpBtn.setOnClickListener(this);
                btnList.add(tmpBtn);
            }
        }

        lin1.removeAllViewsInLayout();
        lin2.removeAllViewsInLayout();
        lin3.removeAllViewsInLayout();

        /*
        for(int j = 0 ; j < 10 ; j++){
            // 숫자 키패드 복호화
            Bitmap decodedByte = null;
            try {
                byte[] decodedString = Base64.decodeBase64(StringUtil.nvl(pinImgArr.get(j)));
                decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            } catch(JSONException e){
                Log.e("JSONException",e.getMessage());
                e.printStackTrace();
            }

            ImageButton tmpBtn = new ImageButton(ct);
            tmpBtn.setId(ImageButton.generateViewId());
            tmpBtn.setLayoutParams(lp);
            tmpBtn.setImageBitmap(decodedByte);
            tmpBtn.setBackgroundColor(Color.parseColor("WHITE"));
            tmpBtn.setOnClickListener(this);

            int resId = tmpBtn.getId();
            btnList.add(tmpBtn);
            keyValues.put(resId,""+j);
            realDataValues.put(resId, this.pinCode.substring(i,i+=4));
        }

        // 빈 키패드 복호화
        Bitmap decodedByte = null;
        try{
            byte[] decodedString = Base64.decodeBase64(StringUtil.nvl(pinImgArr.get(10)));
            decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
        } catch(JSONException e){
            Log.e("JSONException",e.getMessage());
            e.printStackTrace();
        }

        // 빈 키패드 세팅
        for(int j = 0 ; j < 2 ; j++){
            ImageButton tmpBtn = new ImageButton(ct);
            tmpBtn.setId(ImageButton.generateViewId());
            tmpBtn.setLayoutParams(lp);
            tmpBtn.setImageBitmap(decodedByte);
            tmpBtn.setBackgroundColor(Color.parseColor("WHITE"));
            btnList.add(tmpBtn);
        }
        */

        // 리스트 객체 내부에서 이미지버튼 리스트 섞기
        shuffleButton();
    }

    // 클릭 이벤트
    @Override
    public void onClick(View v) {
        // do nothing if the InputConnection has not been set yet
        if (inputConnection == null) return;

        int selBtnId = v.getId();

        if (selBtnId == R.id.button_delete) {
            // 누른 키패드가 삭제버튼일 때
            CharSequence selectedText = inputConnection.getSelectedText(0);
            if (TextUtils.isEmpty(selectedText)) {
                inputConnection.deleteSurroundingText(1, 0);
            } else {
                inputConnection.commitText("", 1);
            }
            if(bufferedPinData.length() > 0){
                bufferedPinData.delete(bufferedPinData.length()-4, bufferedPinData.length());
                bufferedInputData.delete(bufferedInputData.length()-1, bufferedInputData.length());
            }
        } else if(selBtnId == R.id.button_shuffle) {
            // 섞기 버튼 눌렀을 때
            // 레이아웃에서 이미지버튼들(AllViews) 삭제
            lin1.removeAllViewsInLayout();
            lin2.removeAllViewsInLayout();
            lin3.removeAllViewsInLayout();
            shuffleButton();
        } else if(selBtnId == R.id.button_ok){
            // 확인 버튼 눌렀을 때
            // 암호화 시작
            Cipher cipher = null;
            try {
                cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
                cipher.init(Cipher.ENCRYPT_MODE, this.publicKey);
                byte[] encByteArrData = cipher.doFinal(bufferedPinData.toString().getBytes());
                String returnData = Base64.encodeBase64String(encByteArrData);

                // 통신할 데이터 세팅
                ContentValues params = new ContentValues();
                params.put("token",this.token);
                params.put("pinData",returnData);
                params.put("method","POST");

                // 콜백 생성
                callback = new Callback() {
                    @Override
                    public void callback(Object object) {
                        try {
                            verifyDataCallback(object);
                        } catch(Exception e){
                            Log.i(TAG,e.getMessage());
                        }
                    }
                };

                // 통신
                NetworkTask networkTask = new NetworkTask("http://192.168.0.117:8080/verifyData", params, callback);
                networkTask.execute();
            } catch (Exception e){
                Log.i(TAG,e.getMessage());
                new AlertDialog.Builder(this.getContext()) // TestActivity 부분에는 현재 Activity의 이름 입력.
                    .setMessage("시스템 오류")     // 제목 부분 (직접 작성)
                    .setPositiveButton("확인", new DialogInterface.OnClickListener() {      // 버튼1 (직접 작성)
                        public void onClick(DialogInterface dialog, int which){
                            dialog.dismiss();
                            activity.finish();
                        }
                    }).show();
            }
        } else {
            // 숫자 키패드 입력시
            String inpValue = keyValues.get(selBtnId);
            String pinValue = realDataValues.get(selBtnId);
            bufferedPinData.append(pinValue);
            if(bufferedInputData.length() > 0){
                inputConnection.deleteSurroundingText(1, 0);
                inputConnection.commitText("●",1);
            }
            inputConnection.commitText(inpValue, 1);
            bufferedInputData.append(inpValue);

            Handler handler = new Handler(Looper.getMainLooper());
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    // Do the task...
                    inputConnection.deleteSurroundingText(1, 0);
                    inputConnection.commitText("●",1);
                }
            };

            handler.postDelayed(runnable, 500);
        }
    }

    // 섞기 버튼 눌렀을 때 동작하는 함수
    public void shuffleButton(){
        // 리스트 내부에서 섞음
        Collections.shuffle(btnList);

        // 섞은 데이터 화면에 재배열
        int k = 0;
        for(; k < 4; k++){
            lin1.addView(btnList.get(k));
        }

        for(; k < 8 ; k++){
            lin2.addView(btnList.get(k));
        }

        for(; k < 12 ; k++){
            lin3.addView(btnList.get(k));
        }
    }

    public void getRandomPinCodeCallback(Object object) throws Exception{
        JSONObject json = null;
        json = new JSONObject(object.toString());
        String token = StringUtil.nvl(json.get("token"));
        if(!"".equals(token)){
            String pinCode = StringUtil.nvl(json.get("pinCode"));
            String encCertPK = StringUtil.nvl(json.get("encCertPK"));
            this.setParentVar(token, pinCode, encCertPK);
            this.makeImgBtn();
            // 앱에 이미지를 넣기 어려울 경우..?
//            JSONArray pinImgArr = (JSONArray) json.get("pinImgList");
//            this.makeImgBtn(pinImgArr);
        }
    }

    // verifyData 통신 콜백
    public void verifyDataCallback(Object object) throws Exception {
        Log.i(TAG,"verifyDataCallback init >>> "+object.toString());
        JSONObject json = null;
        json = new JSONObject(StringUtil.nvl(object,""));
        AlertDialog.Builder adb;
        if(json.length() == 0){
            // 버튼1 (직접 작성)
            adb = new AlertDialog.Builder(this.getContext()) // TestActivity 부분에는 현재 Activity의 이름 입력.
                .setMessage("시스템 오류")     // 제목 부분 (직접 작성)
                .setPositiveButton("확인", (dialog, which) -> dialog.dismiss());
            AlertDialog ad = adb.create();
            ad.show();
        } else {
            String rtCode = StringUtil.nvl(json.get("code"));
            String rtMsg = StringUtil.nvl(json.get("msg"));
            Handler handler = new Handler(Looper.getMainLooper());
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    CustomDialogHandler cdh = new CustomDialogHandler(NaruKeypad.this.getContext(), "성공",rtMsg);
                    cdh.setPositiveButton("확인",(dialog, which) -> dialog.dismiss());
                    AlertDialog ad = cdh.create();
                    ad.show();
                }
            };
            handler.post(runnable);

        }
    }
}
