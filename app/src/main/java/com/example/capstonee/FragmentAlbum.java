package com.example.capstonee;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import com.example.capstonee.Model.Infomation;
import com.example.capstonee.Model.Login;
import com.example.capstonee.Model.Picture;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static android.app.Activity.RESULT_OK;

public class FragmentAlbum extends Fragment {
    //Album
    View v;
    RecyclerView recyclerView;
    RecyclerPhotoViewAdapter recyclerViewAdapter;
    private Animation fab_open, fab_close;
    private Boolean isFabOpen = false;
    private FloatingActionButton fab, fab1, fab2;
    private Boolean isPermission = true;
    private static final int REQUEST_IMAGE_CAPTURE = 672;
    private Uri photoUri;

    public FragmentAlbum() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.album_fragment, container, false);
        recyclerView = v.findViewById(R.id.album_recyclerview);
        getData();
        StaggeredGridLayoutManager manager = new StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL);
        recyclerViewAdapter = new RecyclerPhotoViewAdapter(getContext());
        recyclerView.setLayoutManager(manager);
        recyclerView.setAdapter(recyclerViewAdapter);

        fab_open = AnimationUtils.loadAnimation(getActivity(), R.anim.fab_open);
        fab_close = AnimationUtils.loadAnimation(getActivity(), R.anim.fab_close);

        fab = v.findViewById(R.id.fab_main);
        fab1 = v.findViewById(R.id.fab_sub1);
        fab2 = v.findViewById(R.id.fab_sub2);

        tedPermission();

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                anim();
                Toast.makeText(getContext(), "Main", Toast.LENGTH_SHORT).show();
            }
        });
        fab1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                anim();
                Log.v("알림", "사진촬영 선택");
                if(isPermission) openCamera();
            }
        });
        fab2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                anim();
                Toast.makeText(getContext(), "Modify or Delete", Toast.LENGTH_SHORT).show();

            }
        });
        return v;
    }
    private void tedPermission(){
        PermissionListener permissionListener = new PermissionListener() {
            @Override
            public void onPermissionGranted() {
                isPermission = true;
            }

            @Override
            public void onPermissionDenied(List<String> deniedPermissions) {
                isPermission = false;
            }
        };

        TedPermission.with(getActivity())
                .setPermissionListener(permissionListener)
                .setRationaleMessage(getResources().getString(R.string.permission_2))
                .setDeniedMessage(getResources().getString(R.string.permission_1))
                .setPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA, Manifest.permission.ACCESS_FINE_LOCATION)
                .check();
    }
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }
    private void openCamera() {
        try {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
                File photoFile = null;
                photoFile = createImageFile();

                if (photoFile != null) {
                    photoUri = FileProvider.getUriForFile(getActivity(), "com.example.capstonee.provider", photoFile);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                    startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
                }
            }

        } catch (IOException e) { }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK)
            UploadPicture_alert();
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "TEST_" + timeStamp + "_";
        File storageDir = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );
        return image;
    }
    // 사진 업로드 할 지 물어보는 알람
    public void UploadPicture_alert() {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("사진 업로드");
        builder.setMessage("사진을 Cloud에 업로드 하시겠습니까?\n'아니오' 선택 시 사진은 삭제됩니다.");
        builder.setPositiveButton("예",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        uploadPicture();
                    }
                });
        builder.setNegativeButton("아니오",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(getActivity(), "취소 되었습니다.", Toast.LENGTH_LONG).show();
                    }
                });

        builder.show();
    }

    private void uploadPicture() {
        // 진행상황 보여줌.
        final ProgressDialog progressDialog = new ProgressDialog(getActivity());
        progressDialog.setTitle("업로드중...");
        progressDialog.show();

        // 현재 시간 + png
        final String filename = new SimpleDateFormat("yyyyMMHH_mmss").format(new Date());

        // 사용자 폴더에 사진 파일 저장을 위한 서버 저장 공간 참조 가져옴.
        StorageReference storageRef = Infomation.getAlbum(Login.getUserID() + "/" + filename);

        // 서버에 사진 업로드
        storageRef.putFile(photoUri)
                //성공시
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        try {

                            progressDialog.dismiss(); //업로드 진행 Dialog 상자 닫기

                            final DatabaseReference pictureRef = Infomation.getAlbumData(Login.getUserID()).push();

                            final Picture picture = new Picture();
                            picture.setFileName(filename);
                            String gps[] = new GPS().currentLocation(getContext(), getActivity());

                            picture.setGpsProvider(gps[0]);
                            picture.setLongitude(Double.parseDouble(gps[1]));
                            picture.setLatitude(Double.parseDouble(gps[2]));
                            picture.setAltitude(Double.parseDouble(gps[3]));
                            picture.setUploadID(Login.getUserID());
                            picture.setPictureID(pictureRef.getKey());
                            picture.setUri(photoUri.toString());
                            picture.setDeleted(false);

                            Log.v("간닷", Login.getUserID());
                            pictureRef.setValue(picture);

                            if (picture.getLatitude() != -1) {
                                Thread thread = new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            Log.e("Thread 시작", "");
                                            String location = GPS.getAdrress(picture.getLatitude(), picture.getLongitude());
                                            Log.e("location", location);
                                            picture.setLocation(location);
                                            pictureRef.setValue(picture);


                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                });
                                thread.start();
                            }
                            Toast.makeText(getContext(), "업로드 완료!", Toast.LENGTH_SHORT).show();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                })
                //실패시
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(getContext(), "업로드 실패!", Toast.LENGTH_SHORT).show();
                    }
                })
                //진행중
                .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                        @SuppressWarnings("VisibleForTests") //이걸 넣어 줘야 아랫줄에 에러가 사라진다. 넌 누구냐?
                                double progress = (100 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                        //dialog에 진행률을 퍼센트로 출력해 준다
                        progressDialog.setMessage("Uploaded " + ((int) progress) + "% ...");
                    }
                });
    }
    private void getData() {
        // 내 앨범 데이터 참조 가져오기
        Log.d("LOGIGIN", Login.getUserID());
        final DatabaseReference albumDataRef = Infomation.getAlbumData(Login.getUserID());
        albumDataRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    // DB에 추가된 picture 정보를 가져온다.
                    Picture picture = snapshot.getValue(Picture.class);
                    Log.d("picture info", picture.getFileName()+" "+picture.getUri());
                    // 해당 파일은 내 ID / 파일 이름 에 존재
                    String filePath = Login.getUserID() + "/" + picture.getFileName();
                    // 그 파일의 참조 가져옴
                    final StorageReference albumRef = Infomation.getAlbum(filePath);
                    Log.d("filePath", filePath);
                    //Url을 다운받기
                    albumRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Log.d("addItem", uri.toString());
                            recyclerViewAdapter.addItem(uri.toString());
                            recyclerViewAdapter.notifyDataSetChanged();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(getContext(), "다운로드 실패", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    public void anim() {

        if (isFabOpen) {
            fab1.startAnimation(fab_close);
            fab2.startAnimation(fab_close);
            fab1.setClickable(false);
            fab2.setClickable(false);
            isFabOpen = false;
        } else {
            fab1.startAnimation(fab_open);
            fab2.startAnimation(fab_open);
            fab1.setClickable(true);
            fab2.setClickable(true);
            isFabOpen = true;
        }
    }
}
