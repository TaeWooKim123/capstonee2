package com.example.capstonee;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.text.SimpleDateFormat;


public class CapchaTwActivity extends AppCompatActivity {
    private Captcha captcha;
    private Button btnMode;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_captcha_main);
        captcha = (Captcha) findViewById(R.id.captCha);
        btnMode = (Button) findViewById(R.id.btn_mode);
        btnMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (captcha.getMode() == Captcha.MODE_BAR) {
                    captcha.setMode(Captcha.MODE_NONBAR);
                    btnMode.setText("슬라이드 모드");
                } else {
                    captcha.setMode(Captcha.MODE_BAR);
                    btnMode.setText("드래그 앤 드랍 모드");
                }
            }
        });
        captcha.setCaptchaListener(new Captcha.CaptchaListener() {
            @Override
            public String onAccess(long time) {
                Toast.makeText(CapchaTwActivity.this, "성공. 재로그인하세요.", Toast.LENGTH_SHORT).show();
                SignIn.correctNumber=0;
                onBackPressed();
                return "성공";
            }
            @Override
            public String onFailed(int count) {
                Toast.makeText(CapchaTwActivity.this, "실패 횟수" + count, Toast.LENGTH_SHORT).show();
                return "인증 실패";
            }
            @Override
            public String onMaxFailed() {
                Toast.makeText(CapchaTwActivity.this, "계정이 차단됩니다.", Toast.LENGTH_SHORT).show();
                SignIn.correctNumber=-1;
                SignIn.loginStop=true;
                SignIn.failureTime = System.currentTimeMillis();
                onBackPressed();
                return "계정 차단";
            }
        });
    }
    boolean isCat = true;
    public void changePicture(View view){
        if(isCat){
            captcha.setBitmap("http://img4.imgtn.bdimg.com/it/u=2091068830,1003707060&fm=200&gp=0.jpg");
        }else{
            captcha.setBitmap(R.mipmap.cat);
        }
        isCat=!isCat;
    }
    boolean isSeekbar1 = false;
    public void changeProgressDrawable(View view){
        if(isSeekbar1){
            captcha.setSeekBarStyle(R.drawable.po_seekbar,R.drawable.thumb);
        }else{
            captcha.setSeekBarStyle(R.drawable.po_seekbar1,R.drawable.thumb1);
        }
        isSeekbar1=!isSeekbar1;
    }
    //현재 시간구하기 위해서 정의
    public static String getCurrentTime(String timeFormat){
        return new SimpleDateFormat(timeFormat).format(System.currentTimeMillis());
    }
}
