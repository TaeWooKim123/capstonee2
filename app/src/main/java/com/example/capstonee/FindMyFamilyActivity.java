package com.example.capstonee;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.capstonee.Model.Infomation;
import com.example.capstonee.Model.Login;
import com.example.capstonee.Model.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

public class FindMyFamilyActivity extends Activity {
    EditText familyID, familyPW;
    Button noIDButton, enrollButton;
    String ID, PW;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_find_my_family);
        familyID = findViewById(R.id.familyID);
        familyPW = findViewById(R.id.familyPW);
        noIDButton = findViewById(R.id.noIDButton);
        enrollButton = findViewById(R.id.enrollButton);

        //가족 ID가 없을 때
        noIDButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.putExtra("keep", true);
                setResult(RESULT_CANCELED, intent);
                finish();
            }
        });

        //가족 ID 등록시
        enrollButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ID = familyID.getText().toString();
                PW = familyPW.getText().toString();
                if (ID.equals(Login.getUserID())) {
                    Toast.makeText(FindMyFamilyActivity.this, "자신의 ID가 아닌 가족의 ID를 입력하세요.", Toast.LENGTH_SHORT).show();
                } else {
                    DatabaseReference findUser = Infomation.getDatabase("User");
                    findUser.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.child(ID).exists()) {
                                User user = dataSnapshot.child(ID).getValue(User.class);
                                if (!PW.equals(user.getPassword())) {
                                    Toast.makeText(FindMyFamilyActivity.this, "비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show();
                                } else {
                                    Intent intent = new Intent();
                                    intent.putExtra("keep", false);
                                    setResult(RESULT_OK, intent);
                                    finish();
                                }
                            } else {
                                Toast.makeText(FindMyFamilyActivity.this, "없는 아이디 입니다.", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                        }
                    });
                }
            }
        });
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //바깥레이어 클릭시 안닫히게
        return event.getAction() != MotionEvent.ACTION_OUTSIDE;
    }

    @Override
    public void onBackPressed(){ //뒤로가기 못하게
    }
}
