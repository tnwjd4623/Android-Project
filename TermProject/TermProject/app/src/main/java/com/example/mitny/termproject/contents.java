package com.example.mitny.termproject;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

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

import java.util.HashMap;
import java.util.Iterator;

public class contents extends AppCompatActivity {
    ImageView clothesimg, feel;
    ImageButton goodimg;
    TextView commentview, temperature, clothestext, goodnum;
    int position;
    FirebasePost firebasePost;
    String child;
    String title;

    //메뉴 바
    MenuItem jjim;
    MenuItem delete, modify;

    String postName;
    String user = FirebaseAuth.getInstance().getCurrentUser().getUid();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.contents_activity);
        Intent intent = getIntent();

        clothesimg = (ImageView) findViewById(R.id.clothesImage);
        feel = (ImageView) findViewById(R.id.feel);
        goodimg = (ImageButton) findViewById(R.id.goodimg);
        commentview = (TextView) findViewById(R.id.commentview);
        temperature = (TextView) findViewById(R.id.temperature);
        clothestext = (TextView) findViewById(R.id.clothesText);
        goodnum = (TextView) findViewById(R.id.goodnum);

        position = intent.getIntExtra("position", -1);
        child = intent.getStringExtra("child");
        title = intent.getStringExtra("title");


        Log.d("myReceive", position+"\n"+child);
        //db, storage연동
        final DatabaseReference mDatabase;
        mDatabase = FirebaseDatabase.getInstance().getReference();


        //글 내용 가져오기
        mDatabase.child("contentlist").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //iterator로 자식들 순회하면서 정보 가져옴
                final Iterator<DataSnapshot> iterator = dataSnapshot.getChildren().iterator();
                //count = 글 개수
                int count = position;
                int i = 0;
                if(position >= 0) {
                    while (i != position) {
                        //정보를 FirebasePost 형식으로 바로 저장후 배열에 넣음
                        firebasePost = iterator.next().getValue(FirebasePost.class);
                        i++;
                    }
                    firebasePost = iterator.next().getValue(FirebasePost.class);
                }
                else {
                    while(iterator.hasNext()) {
                        DataSnapshot d = iterator.next();
                        if(d.getKey().equals(title)) {
                            firebasePost = d.getValue(FirebasePost.class);
                            break;
                        }
                    }
                }

                StorageReference mStorageRef;
                mStorageRef = FirebaseStorage.getInstance().getReference();
                //해당 위치의 사진의 이름으로 접근
                //StorageReference spaceRef = mStorageRef.child(list_itemArrayList.get(position).getImageroute().substring(1));

                final StorageReference mRef = mStorageRef.child(child);

                //스토리지에서 이미지 다운 URL 가져옴
                mRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        //성공시 URL 저장, imageView 변경
                        String downloadurl = uri.toString();
                        Glide.with(getApplicationContext()).load(downloadurl).into(clothesimg);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //실패시(이미지 없을 때) URL ="" 이미지 없는 채로
                        String downloadurl = "";
                    }
                });


                //jjim.setImageResource(R.drawable.emptyheart);
                feel.setImageResource(selectfeelicon(firebasePost.feel));
                commentview.setText(firebasePost.comment);
                temperature.setText(firebasePost.temperature);
                clothestext.setText(firebasePost.context);
                goodnum.setText(String.valueOf(firebasePost.good));

                String postid = firebasePost.userid;            //글쓴이
                user = FirebaseAuth.getInstance().getCurrentUser().getUid();        //접속자
                postName = firebasePost.getPostName();
                
                if(postid.equals(user)) {
                    jjim.setVisible(false);
                }
                else {
                    jjim.setVisible(true);
                    delete.setVisible(false);
                    modify.setVisible(false);

                    DatabaseReference usergoodlist = FirebaseDatabase.getInstance().getReference().child("userlist");
                    usergoodlist.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if(dataSnapshot.hasChild(user)){
                                if(dataSnapshot.child(user).hasChild("jjimlist")){
                                    if(dataSnapshot.child(user).child("jjimlist").hasChild(postName)){
                                        jjim.setIcon(R.drawable.baseline_favorite_black_18dp);
                                    }else{
                                        jjim.setIcon(R.drawable.baseline_favorite_border_black_18dp);
                                    }
                                }else{
                                    jjim.setIcon(R.drawable.baseline_favorite_border_black_18dp);
                                }
                            }else{
                                jjim.setIcon(R.drawable.baseline_favorite_border_black_18dp);
                            }
                            dataSnapshot.getValue(userlistPost.class);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }

            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        //좋아요 기능
        goodimg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!firebasePost.userid.equals(user)) {
                    DatabaseReference usergoodlist = FirebaseDatabase.getInstance().getReference().child("userlist");
                    usergoodlist.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if(dataSnapshot.hasChild(user)){
                                if(dataSnapshot.child(user).hasChild("goodlist")){
                                    if(!dataSnapshot.child(user).child("goodlist").hasChild(postName)){
                                        DatabaseReference thisUserData = FirebaseDatabase.getInstance().getReference().child("userlist/"+user+"/goodlist");
                                        HashMap<String, Object> mMapp = new HashMap<>();
                                        mMapp.put(postName, "o");
                                        thisUserData.updateChildren(mMapp);

                                        DatabaseReference postRef = FirebaseDatabase.getInstance().getReference().child("contentlist/" + postName);
                                        HashMap<String, Object> mMap = new HashMap<>();
                                        int good = Integer.parseInt(goodnum.getText().toString()) + 1;
                                        mMap.put("good", good);
                                        postRef.updateChildren(mMap);
                                        recreate();
                                    }
                                }else{
                                    DatabaseReference thisUserData = FirebaseDatabase.getInstance().getReference().child("userlist/"+user+"/goodlist");
                                    HashMap<String, Object> mMapp = new HashMap<>();
                                    mMapp.put(postName, "o");
                                    thisUserData.updateChildren(mMapp);

                                    DatabaseReference postRef = FirebaseDatabase.getInstance().getReference().child("contentlist/" + postName);
                                    HashMap<String, Object> mMap = new HashMap<>();
                                    int good = Integer.parseInt(goodnum.getText().toString()) + 1;
                                    mMap.put("good", good);
                                    postRef.updateChildren(mMap);
                                    recreate();
                                }
                            }else{
                                DatabaseReference thisUserData = FirebaseDatabase.getInstance().getReference().child("userlist/"+user+"/goodlist");
                                HashMap<String, Object> mMapp = new HashMap<>();
                                mMapp.put(postName, "o");
                                thisUserData.updateChildren(mMapp);

                                DatabaseReference postRef = FirebaseDatabase.getInstance().getReference().child("contentlist/" + postName);
                                HashMap<String, Object> mMap = new HashMap<>();
                                int good = Integer.parseInt(goodnum.getText().toString()) + 1;
                                mMap.put("good", good);
                                postRef.updateChildren(mMap);
                                recreate();
                            }
                            dataSnapshot.getValue(userlistPost.class);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
            }
        });

    }
    public int selectfeelicon(int feel){
        int feelicon = R.drawable.good;
        switch (feel){
            case 1:
                feelicon = R.drawable.cold;
                break;
            case 2:
                feelicon = R.drawable.little_cold;
                break;
            case 3:
                feelicon = R.drawable.good;
                break;
            case 4:
                feelicon = R.drawable.little_hot;
                break;
            case 5:
                feelicon = R.drawable.hot;
                break;
        }
        return feelicon;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        delete = menu.findItem(R.id.delete);
        modify = menu.findItem(R.id.modify);
        jjim = menu.findItem(R.id.jjim);

        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.jjim:     //찜 눌렀을 때
                DatabaseReference usergoodlist = FirebaseDatabase.getInstance().getReference().child("userlist");
                usergoodlist.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.hasChild(user)){
                            if(dataSnapshot.child(user).hasChild("jjimlist")){
                                if(!dataSnapshot.child(user).child("jjimlist").hasChild(postName)){
                                    DatabaseReference thisUserData = FirebaseDatabase.getInstance().getReference().child("userlist/"+user+"/jjimlist");
                                    HashMap<String, Object> mMapp = new HashMap<>();
                                    mMapp.put(postName, "o");
                                    thisUserData.updateChildren(mMapp);
                                    jjim.setIcon(R.drawable.baseline_favorite_black_18dp);
                                }else{
                                    DatabaseReference postRef = FirebaseDatabase.getInstance().getReference().child("userlist/" + user+"/jjimlist");
                                    HashMap<String, Object> mMap = new HashMap<>();
                                    mMap.put(postName, null);
                                    postRef.updateChildren(mMap);
                                    recreate();
                                    jjim.setIcon(R.drawable.baseline_favorite_border_black_18dp);
                                }
                            }else{
                                DatabaseReference thisUserData = FirebaseDatabase.getInstance().getReference().child("userlist/"+user+"/jjimlist");
                                HashMap<String, Object> mMapp = new HashMap<>();
                                mMapp.put(postName, "o");
                                thisUserData.updateChildren(mMapp);
                                jjim.setIcon(R.drawable.baseline_favorite_black_18dp);
                            }
                        }else{
                            DatabaseReference thisUserData = FirebaseDatabase.getInstance().getReference().child("userlist/"+user+"/jjimlist");
                            HashMap<String, Object> mMapp = new HashMap<>();
                            mMapp.put(postName, "o");
                            thisUserData.updateChildren(mMapp);
                            jjim.setIcon(R.drawable.baseline_favorite_black_18dp);
                        }
                        dataSnapshot.getValue(userlistPost.class);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
                //if( 데이터베이스에 찜 목록에 없는 경우 -> 찜 추가, 하트 색칠)
               // jjim.setIcon(R.drawable.baseline_favorite_black_18dp);

                //if(데이터 베이스에 찜 목록에 있느 경우 -> 찜 취소 됨)
                //jjim.setIcon(R.drawable.baseline_favorite_border_black_18dp);

                return true;
            case R.id.delete:       //삭제 눌렀을 때
                DatabaseReference postRef = FirebaseDatabase.getInstance().getReference().child("contentlist/");
                HashMap<String, Object> mMap = new HashMap<>();
                mMap.put(postName, null);
                postRef.updateChildren(mMap);
                finish();
                return true;
            case R.id.modify:          //수정 눌렀을 때
                Intent intent = new Intent(getApplicationContext(), modify.class);
                intent.putExtra("title", postName);
                intent.putExtra("url", firebasePost.photo);
                Log.d("myUrl", firebasePost.photo);
                startActivity(intent);
                return true;

                default:
                    return super.onOptionsItemSelected(item);
        }
    }
    //수정에서 끝나서 다시 넘어올 때 새로고침
    @Override
    public void onResume() {
        super.onResume();
        Intent intent = getIntent();
        final String title = intent.getStringExtra("title");

        final DatabaseReference mDatabase;
        mDatabase = FirebaseDatabase.getInstance().getReference();
        //글 내용 가져오기
        mDatabase.child("contentlist").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //iterator로 자식들 순회하면서 정보 가져옴
                final Iterator<DataSnapshot> iterator = dataSnapshot.getChildren().iterator();
                //count = 글 개수
                int count = position;
                int i = 0;
                if (position >= 0) {
                    while (i != position) {
                        //정보를 FirebasePost 형식으로 바로 저장후 배열에 넣음
                        firebasePost = iterator.next().getValue(FirebasePost.class);
                        i++;
                    }
                    firebasePost = iterator.next().getValue(FirebasePost.class);
                } else {
                    while (iterator.hasNext()) {
                        DataSnapshot d = iterator.next();
                        if (d.getKey().equals(title)) {
                            firebasePost = d.getValue(FirebasePost.class);
                            break;
                        }
                    }
                }

                StorageReference mStorageRef;
                mStorageRef = FirebaseStorage.getInstance().getReference();
                //해당 위치의 사진의 이름으로 접근
                //StorageReference spaceRef = mStorageRef.child(list_itemArrayList.get(position).getImageroute().substring(1));

                final StorageReference mRef = mStorageRef.child(child);

                //스토리지에서 이미지 다운 URL 가져옴
                mRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        //성공시 URL 저장, imageView 변경
                        String downloadurl = uri.toString();
                        Glide.with(getApplicationContext()).load(downloadurl).into(clothesimg);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //실패시(이미지 없을 때) URL ="" 이미지 없는 채로
                        String downloadurl = "";
                    }
                });


                //jjim.setImageResource(R.drawable.emptyheart);
                feel.setImageResource(selectfeelicon(firebasePost.feel));
                commentview.setText(firebasePost.comment);
                temperature.setText(firebasePost.temperature);
                clothestext.setText(firebasePost.context);
                goodnum.setText(String.valueOf(firebasePost.good));

                String postid = firebasePost.userid;            //글쓴이
                user = FirebaseAuth.getInstance().getCurrentUser().getUid();        //접속자
                postName = firebasePost.getPostName();

                if (postid.equals(user)) {
                    jjim.setVisible(false);
                } else {
                    jjim.setVisible(true);
                    delete.setVisible(false);
                    modify.setVisible(false);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }


}