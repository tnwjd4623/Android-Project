package com.example.mitny.termproject;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static android.content.Intent.ACTION_MEDIA_SCANNER_SCAN_FILE;

public class modify extends AppCompatActivity {
    public static final int f_cold = 1;
    public static final int f_littlecold = 2;
    public static final int f_good = 3;
    public static final int f_littlehot = 4;
    public static final int f_hot = 5;
    FirebasePost firebasePost;

    //최종저장 내역
    public String save_temp;
    public String save_clothes;//글내용
    public String save_comment;//코멘트
    public String save_photourl;//사진url
    public int save_feel;
    public String save_filename;//저장사진이름
    public String save_name;//db저장시제목(임시)
    public String save_user;

    public String title;
    public String url;

    ImageButton wbutton1, wbutton2, wbutton3, wbutton4, wbutton5; //날씨아이콘버튼
    int weatherfeel;
    EditText clothes, comment;

    ImageView photo;
    Button upload;
    Button save;

    private static final int PICK_FROM_CAMERA = 0;
    private static final int PICK_FROM_ALBUM = 1;
    private static final int CROP_FROM_IMAGE = 2;

    private Uri mImageCaptureUri;
    private String absoultePath;
    ByteArrayOutputStream out = null;



    public modify() {
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.modify);

        photo = (ImageView)findViewById(R.id.photo);
        upload = (Button)findViewById(R.id.upload);
        save = (Button)findViewById(R.id.save);
        wbutton1 = (ImageButton)findViewById(R.id.button);
        wbutton2 = (ImageButton)findViewById(R.id.button2);
        wbutton3 = (ImageButton)findViewById(R.id.button3);
        wbutton4 = (ImageButton)findViewById(R.id.button4);
        wbutton5 = (ImageButton)findViewById(R.id.button5);
        clothes = (EditText)findViewById(R.id.editText);
        comment = (EditText)findViewById(R.id.editText2);

        final Intent intent = getIntent();

        title = intent.getStringExtra("title");        //글제목
        url = intent.getStringExtra("url");            //사진 URL

        save_filename = url;

        Log.d("MyReceiver", title+"\n"+url);
        final DatabaseReference mDatabase;
        mDatabase = FirebaseDatabase.getInstance().getReference();

        //글 내용 가져오기
        mDatabase.child("contentlist").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //iterator로 자식들 순회하면서 정보 가져옴
                final Iterator<DataSnapshot> iterator = dataSnapshot.getChildren().iterator();
                    //같은 제목 나올때 까지 순회
                    while(iterator.hasNext()) {
                        DataSnapshot d = iterator.next();
                        if(d.getKey().equals(title)) {
                            firebasePost = d.getValue(FirebasePost.class);
                            break;
                        }
                    }
                //기존의 정보로 setText
                clothes.setText(firebasePost.context);
                comment.setText(firebasePost.comment);

                StorageReference mStorageRef;
                mStorageRef = FirebaseStorage.getInstance().getReference();
                //해당 위치의 사진의 이름으로 접근
                //StorageReference spaceRef = mStorageRef.child(list_itemArrayList.get(position).getImageroute().substring(1));

                final StorageReference mRef = mStorageRef.child(url);

                //스토리지에서 이미지 다운 URL 가져옴
                mRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        //성공시 URL 저장, imageView 변경
                        String downloadurl = uri.toString();
                        Glide.with(getApplicationContext()).load(downloadurl).into(photo);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //실패시(이미지 없을 때) URL ="" 이미지 없는 채로
                        String downloadurl = "";
                    }
                });
                //저장된 느낌으로 표시
                int db_feel = firebasePost.feel;
                switch (db_feel) {
                    case 1:
                        weatherfeel = f_cold;
                        reset(wbutton1, wbutton2, wbutton3, wbutton4, wbutton5);
                        wbutton1.setImageResource(R.drawable.coldpush);
                        break;
                    case 2:
                        weatherfeel = f_littlecold;
                        reset(wbutton1, wbutton2, wbutton3, wbutton4, wbutton5);
                        wbutton2.setImageResource(R.drawable.littlecoldpush);
                        break;
                    case 3:
                        weatherfeel = f_good;
                        reset(wbutton1, wbutton2, wbutton3, wbutton4, wbutton5);
                        wbutton3.setImageResource(R.drawable.goodpush);
                        break;
                    case 4:
                        weatherfeel = f_littlehot;
                        reset(wbutton1, wbutton2, wbutton3, wbutton4, wbutton5);
                        wbutton4.setImageResource(R.drawable.littlehotpush);
                        break;
                    case 5:
                        weatherfeel = f_hot;
                        reset(wbutton1, wbutton2, wbutton3, wbutton4, wbutton5);
                        wbutton5.setImageResource(R.drawable.hotpush);
                        break;

                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogInterface.OnClickListener albumListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        doTackAlbumAction();
                    }
                };
                DialogInterface.OnClickListener cancelListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                };

                new AlertDialog.Builder(getApplicationContext())
                        .setTitle("업로드할 이미지 선택")
                        .setPositiveButton("앨범선택", albumListener)
                        .setNegativeButton("취소", cancelListener)
                        .show();
            }
        });

        wbutton1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                weatherfeel = f_cold;
                reset(wbutton1, wbutton2, wbutton3, wbutton4, wbutton5);
                wbutton1.setImageResource(R.drawable.coldpush);
            }
        });

        wbutton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                weatherfeel = f_littlecold;
                reset(wbutton1, wbutton2, wbutton3, wbutton4, wbutton5);
                wbutton2.setImageResource(R.drawable.littlecoldpush);
            }
        });
        wbutton3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                weatherfeel = f_good;
                reset(wbutton1, wbutton2, wbutton3, wbutton4, wbutton5);
                wbutton3.setImageResource(R.drawable.goodpush);
            }
        });
        wbutton4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                weatherfeel = f_littlehot;
                reset(wbutton1, wbutton2, wbutton3, wbutton4, wbutton5);
                wbutton4.setImageResource(R.drawable.littlehotpush);
            }
        });
        wbutton5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                weatherfeel = f_hot;
                reset(wbutton1, wbutton2, wbutton3, wbutton4, wbutton5);
                wbutton5.setImageResource(R.drawable.hotpush);
            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                save_clothes = clothes.getText().toString();
                save_comment = comment.getText().toString();
                save_feel = weatherfeel;
                save_user = FirebaseAuth.getInstance().getCurrentUser().getUid();
                modifyPost();

                intent.putExtra("title", title);
                finish();
            }
        });

    }

    private void reset(ImageButton b1, ImageButton b2, ImageButton b3, ImageButton b4, ImageButton b5){
        b1.setImageResource(R.drawable.cold);
        b2.setImageResource(R.drawable.little_cold);
        b3.setImageResource(R.drawable.good);
        b4.setImageResource(R.drawable.little_hot);
        b5.setImageResource(R.drawable.hot);
    }
    //앨범에서 가져오기 선택시
    public void doTackAlbumAction(){
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
        startActivityForResult(intent, PICK_FROM_ALBUM);
    }
    //앨범에서가져오는부분
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode != RESULT_OK)
            return;
        switch (requestCode){
            case PICK_FROM_ALBUM:{
                mImageCaptureUri = data.getData();
                Log.d("todaywearing", mImageCaptureUri.getPath().toString());
                Intent intent = new Intent("com.android.camera.action.CROP");
                intent.setDataAndType(mImageCaptureUri, "image/*");

                intent.putExtra("scale", true);
                intent.putExtra("return-data", true);
                startActivityForResult(intent, CROP_FROM_IMAGE);
                break;
            }
            case CROP_FROM_IMAGE:{//사진을 크롭하는 부분, 크롭한 사진을 뷰에 띄워줌
                if(resultCode != RESULT_OK){
                    return;
                }
                final Bundle extras = data.getExtras();

                save_filename = "/todaywearing"+System.currentTimeMillis()+".jpg";
                save_name = "t"+System.currentTimeMillis();
                String filePath = Environment.getExternalStorageDirectory().getAbsolutePath()+save_filename;
                save_photourl = filePath;
                if(extras != null){
                    Bitmap cropphoto = extras.getParcelable("data");
                    photo.setImageBitmap(cropphoto);

                    storeCropImage(cropphoto, filePath);
                    absoultePath = filePath;
                    break;
                }
            }
            File f = new File(mImageCaptureUri.getPath());
            //if(f.exists())
            //    f.delete();
        }
    }
    private void storeCropImage(Bitmap bitmap, String filePath) {//크롭한 사진을 임시로 저장후 넘겨줌
        String dirPath = Environment.getExternalStorageDirectory().getAbsolutePath()+"/todaywearing";
        File directory = new File(dirPath);

        if(!directory.exists())
            directory.mkdir();
        File copyFile = new File(filePath);
        //BufferedOutputStream out = null;

        try{
            copyFile.createNewFile();
            //out = new BufferedOutputStream(new FileOutputStream(copyFile));
            out = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            Intent intent = new Intent(ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(copyFile));
            getApplicationContext().sendBroadcast(intent);
            out.flush();
            out.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void modifyPost() {//글 쓰기
        DatabaseReference mDatabase;
        mDatabase = FirebaseDatabase.getInstance().getReference();
        StorageReference mStorageRef;
        mStorageRef = FirebaseStorage.getInstance().getReference();

        //db, storage 연동
        String key = mDatabase.child("contentlist").push().getKey();
        //온도, 느낌, 내용, 코멘트, 사진을 저장한 이름 순으로 넘겨줌
       /* FirebasePost post = new FirebasePost(save_user, save_temp, save_feel, save_clothes, save_comment, save_filename);
        post.toMap();*/

        DatabaseReference postRef = FirebaseDatabase.getInstance().getReference().child("contentlist/" + title);
        Map<String, Object> childUpdates = new HashMap<>();

        childUpdates.put("comment", save_comment);
        childUpdates.put("context", save_clothes);
        childUpdates.put("feel", save_feel);
        childUpdates.put("photo", save_filename);

        postRef.updateChildren(childUpdates);
        //디비에 글 내용 저장


        photo.setDrawingCacheEnabled(true);
        photo.buildDrawingCache();
        Bitmap bitmap = ((BitmapDrawable) photo.getDrawable()).getBitmap();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();

        StorageReference photoRef = mStorageRef.child(save_filename);
        UploadTask uploadTask = photoRef.putBytes(data);

        //storage에 사진 저장(firebase 가이드 참조 바람)
        //https://firebase.google.com/docs/storage/web/upload-files?hl=ko
    }
}

