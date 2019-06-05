package com.example.capstonee.Adapter;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.capstonee.R;
import com.example.capstonee.ShowPhotoActivity;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;


/****
 *  FragmentAlbum에서 사진을 보여주는데 쓰일 RecyclerViewAdapter
 */
public class RecyclerPhotoViewAdapter extends RecyclerView.Adapter<RecyclerPhotoViewAdapter.MyViewHolder> {
    private Context mContext;
    private List<String> mData = new ArrayList<>();

    public RecyclerPhotoViewAdapter(Context mContext){
        this.mContext = mContext;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        View view;
        LayoutInflater inflater = LayoutInflater.from(mContext);
        view = inflater.inflate(R.layout.cardview_item, viewGroup, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder myViewHolder, final int i) {
        myViewHolder.onBind(mData.get(i));
        myViewHolder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, ShowPhotoActivity.class);
                Log.d("mData.get(i)", mData.get(i));
                intent.putExtra("ImageUrl", mData.get(i));
                mContext.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }
    public void addItem(String uri) {
        // 외부에서 item을 추가시킬 함수입니다.
        mData.add(uri);
    }

    class MyViewHolder extends RecyclerView.ViewHolder{
        private ImageView photoImg;
        CardView cardView;

        private MyViewHolder(@NonNull View itemView) {
            super(itemView);

            photoImg = itemView.findViewById(R.id.album_image_id);
            cardView = itemView.findViewById(R.id.cardview_id);
        }

        void onBind(String uri){
            Picasso.with(mContext).load(uri).fit().centerInside().into(photoImg);
        }
    }
}