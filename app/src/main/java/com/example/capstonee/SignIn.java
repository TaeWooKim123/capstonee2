package com.example.capstonee;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.example.capstonee.Model.Login;
import com.example.capstonee.Model.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rengwuxian.materialedittext.MaterialEditText;

public class SignIn extends AppCompatActivity {
    //캡차를 위한 변수들
    static int correctNumber;
    static boolean loginStop = false;
    static long failureTime;
    long retryTime;

    EditText logId, logPassword;
    CheckBox edtCheck;
    Button btnSignIn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        logPassword = (MaterialEditText)findViewById(R.id.logPassword);
        logId = (MaterialEditText)findViewById(R.id.logId);
        edtCheck = findViewById(R.id.edtCheck);
        btnSignIn = findViewById(R.id.btnSignIn);

        //Init Firebase
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        //디비에 해당 아이디가 있는지 탐색하기 위한 DB 레퍼런스
        final DatabaseReference IsIdExist = database.getReference("User");

        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final ProgressDialog mDialog = new ProgressDialog(SignIn.this);
                mDialog.setMessage("잠시만 기다리세요...");
                mDialog.show();

                IsIdExist.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        String ID = logId.getText().toString();
                        String Password = logPassword.getText().toString();
                        Log.d("LOOOOG", dataSnapshot.toString());
                        if(dataSnapshot.child(ID).exists()){
                            //아이디 있음?
                            User user = dataSnapshot.child(ID).getValue(User.class);
                            if(Password.equals(user.getPassword())){
                                if(loginStop == true && correctNumber == -1) {
                                    retryTime = System.currentTimeMillis();
                                    //long failuretime = (long)Long.parseLong(failureTime);
                                    //long retrytime = (long)Long.parseLong(retryTime);
                                    Log.d("실패시간 : ", Long.toString(failureTime));
                                    Log.d("재시도시간 : ", Long.toString(retryTime));
                                    long time = (long) ((retryTime - failureTime) / 1000.0);
                                    if (time > 30) {
                                        loginStop = false;
                                        correctNumber = 0;
                                    } else {
                                        String msg = String.format("%d초 후에 다시 시도하세요.", 30 - (time));
                                        Toast.makeText(SignIn.this, msg, Toast.LENGTH_SHORT).show();
                                    }
                                }
                                else { //비밀번호 일치?
                                    if(edtCheck.isChecked()){
                                        //자동로그인 할거임?
                                        SharedPreferences pref = getSharedPreferences("pref", MODE_PRIVATE);
                                        SharedPreferences.Editor editor = pref.edit();
                                        editor.putString("userID", ID);
                                        editor.putString("userPassword", Password);
                                        editor.apply();
                                    }
                                    Toast.makeText(SignIn.this, user.getName()+"님 환영합니다!!", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(SignIn.this, MainActivity.class);
                                    Login.setID(ID);
                                    Login.setName(user.getName());
                                    Login.setPassword(Password);
                                    Login.setPhone(user.getPhone());
                                    Login.setBirth(user.getBirthDate());
                                    Login.setVisit(user.getVisited());
                                    Login.setFamilyID(user.getFamilyID());
                                    startActivity(intent);
                                }
                            }
                            else{
                                if(loginStop == true && correctNumber == -1){
                                    retryTime = System.currentTimeMillis();
                                    //long failuretime = (long)Long.parseLong(failureTime);
                                    //long retrytime = (long)Long.parseLong(retryTime);
                                    Log.d("실패시간 : ", Long.toString(failureTime));
                                    Log.d("재시도시간 : ", Long.toString(retryTime));
                                    long time = (long) ((retryTime-failureTime)/1000.0);
                                    if(time > 30) {
                                        loginStop=false;
                                        correctNumber =0;
                                    }else{
                                        String msg = String.format("%d초 후에 다시 시도하세요.", 30-(time));
                                        Toast.makeText(SignIn.this, msg, Toast.LENGTH_SHORT).show();
                                    }
                                }else{
                                    correctNumber++;
                                    String msg = String.format("비밀번호가 틀렸습니다.\n%d회. 3회 실패시 인증 확인", correctNumber);
                                    if (correctNumber == 1 || correctNumber == 2)
                                        Toast.makeText(SignIn.this, msg, Toast.LENGTH_SHORT).show();

                                    if (correctNumber == 3) {
                                        //캡차 띄우기.
                                        Toast.makeText(SignIn.this, "자동로그인 방지를 위해 인증화면으로 이동합니다.", Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(SignIn.this, CapchaTwActivity.class);
                                        startActivity(intent);
                                    }
                                }
                            }
                        }else{
                            //아이디 없음
                            Toast.makeText(SignIn.this, "아이디가 존재하지 않습니다.", Toast.LENGTH_SHORT).show();
                        }
                        mDialog.dismiss();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        });
    }
}
