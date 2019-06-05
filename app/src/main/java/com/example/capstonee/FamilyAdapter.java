package com.example.capstonee;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.capstonee.Model.Infomation;
import com.example.capstonee.Model.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class FamilyAdapter extends RecyclerView.Adapter<FamilyAdapter.ItemViewHolder> {
    // adapter에 들어갈 list 입니다.
    ArrayList<String> familyMemberIDList = new ArrayList<>();
    Context context;
    View view;

    public FamilyAdapter(Context context) {
        this.context = context;
    }


    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // LayoutInflater를 이용하여 전 단계에서 만들었던 item.xml을 inflate 시킵니다.
        // return 인자는 ViewHolder 입니다.
        view = LayoutInflater.from(parent.getContext()).inflate(R.layout.family_list_item, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        // Item을 하나, 하나 보여주는(bind 되는) 함수입니다.
        holder.onBind(familyMemberIDList.get(position));
    }

    @Override
    public int getItemCount() {
        // RecyclerView의 총 개수 입니다.
        return familyMemberIDList.size();
    }

    public void addItem(String value) {
        // 외부에서 item을 추가시킬 함수입니다.
        familyMemberIDList.add(value);
    }

    public void remove(String userID){
        familyMemberIDList.remove(familyMemberIDList.indexOf(userID));
    }

    // RecyclerView의 핵심인 ViewHolder 입니다.
    // 여기서 subView를 setting 해줍니다.
    class ItemViewHolder extends RecyclerView.ViewHolder {

        private String familyMemberID;
        private TextView familyTitle_textView;

        ItemViewHolder(View view) {
            super(view);

            familyTitle_textView = view.findViewById(R.id.user_id);

            view.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    familyAdd_alert(familyMemberID);
                    return false;
                }
            });

        }

        void onBind(String familyMemberID) {
            this.familyMemberID = familyMemberID;
            familyTitle_textView.setText(familyMemberID);
        }

    }

    private void familyAdd_alert(final String familyMemberID) {
        final AlertDialog.Builder ad = new AlertDialog.Builder(context);

        ad.setTitle("가족 삭제");       // 제목 설정
        ad.setMessage("선택한 가족을 퇴출시키겠습니까?\n한번 퇴출된 가족은 되돌릴 수 없습니다.");   // 내용 설정

        // 확인 버튼 설정
        ad.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                final DatabaseReference userRef = Infomation.getDatabase("User").child(familyMemberID);
                userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        User user = dataSnapshot.getValue(User.class);
                        String familyID = user.getFamilyID();
                        user.setFamilyID("");
                        userRef.setValue(user);

                        // family DB 삭제
                        final DatabaseReference familyRef = Infomation.getDatabase("Family").child(familyID);
                        familyRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                for(DataSnapshot d : dataSnapshot.getChildren())
                                    if(d.getValue().toString().equals(familyMemberID))
                                        familyRef.child(d.getKey()).removeValue();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });


                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        });

        // 취소 버튼 설정
        ad.setNegativeButton("No", new DialogInterface.OnClickListener() {
            private DialogInterface dialog;
            private int which;

            @Override
            public void onClick(DialogInterface dialog, int which) {
                this.dialog = dialog;
                this.which = which;
            }
        });

        // 창 띄우기
        ad.show();

    }

}
