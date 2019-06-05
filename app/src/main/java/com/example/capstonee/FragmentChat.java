package com.example.capstonee;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.capstonee.Model.Chatting;

import java.util.ArrayList;
import java.util.List;

public class FragmentChat extends Fragment {

    View v;
    private RecyclerView recyclerView;
    private List<Chatting> lstChatting;
    public FragmentChat() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.chat_fragment, container, false);
        recyclerView = v.findViewById(R.id.chat_recyclerview);
        RecyclerViewAdapter recyclerViewAdapter = new RecyclerViewAdapter(getContext(), lstChatting);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(recyclerViewAdapter);
        return v;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        lstChatting = new ArrayList<>();
        lstChatting.add(new Chatting("NoMuHyeon", "12345", R.drawable.nomu));
        lstChatting.add(new Chatting("KimDaeJoong", "67890", R.drawable.kimdae));
    }
}
