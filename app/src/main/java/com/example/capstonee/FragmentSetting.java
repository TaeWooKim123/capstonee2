package com.example.capstonee;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.capstonee.Model.Login;

public class FragmentSetting extends Fragment {
    View v;
    private ImageButton setInfo;
    TextView Username, Id;

    public FragmentSetting() { }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.setting_fragment, container, false);
        Username = v.findViewById(R.id.username);
        Username.setText(Login.getUserName());
        Id = v.findViewById(R.id.userid);
        setInfo = v.findViewById(R.id.setInfo);
        Id.setText(Login.getUserID());
        LinearLayout logout = v.findViewById(R.id.logout);
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alertdialog = new AlertDialog.Builder(getContext());
                alertdialog.setTitle("로그아웃");
                alertdialog.setMessage("로그아웃 하시겠습니까?");
                // 게시할 때
                alertdialog.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SharedPreferences pref = getActivity().getSharedPreferences("pref", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = pref.edit();
                        Log.d("LOGOUT : ", pref.getString("userID", null)+ " " +pref.getString("userPassword", null));
                        editor.remove("userID");
                        editor.remove("userPassword");
                        editor.apply();
                        Toast.makeText(getContext(), "로그아웃이 완료되었습니다.", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(getActivity(), SignActivity.class);
                        startActivity(intent);
                    }
                }).setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing
                    }
                });
                alertdialog.show();
            }
        });
        LinearLayout familyInfo = v.findViewById(R.id.familyInfo);
        familyInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), FamilyInformation.class);
                startActivity(intent);
            }
        });
        setInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), SetUserInformation.class);
                startActivity(intent);
            }
        });
        return v;
    }
}
