package com.example.mitny.termproject;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class jjimFragment extends Fragment {
    listAdapter adapter;                             //리스트어뎁터
    HashMap<String, list_item> mMap;        //리스트 아이템들 추가하는 곳
    HashMap<String, String> mUrl;
    ListView listView;                               //리스트 출력하는 레이아웃
    FirebasePost[] contentlist = null;
    String[] jjimlist = null;
    String user = FirebaseAuth.getInstance().getCurrentUser().getUid();

    private FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();
    private DatabaseReference mReference = mDatabase.getReference();

    String con;
    int feelicon;
    String temp;
    String photoname;

    public jjimFragment() {
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.jjim_fragment, container, false);
        listView = (ListView)view.findViewById(R.id.favoritelist);

        mMap = new HashMap<String, list_item>();
        mUrl = new HashMap<String, String>();               //url 정보 저장

        //db, storage연동
        final DatabaseReference mDatabase;
        mDatabase = FirebaseDatabase.getInstance().getReference();
        StorageReference mStorageRef;
        mStorageRef = FirebaseStorage.getInstance().getReference();

        mDatabase.child("userlist").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Iterator<DataSnapshot> iterator = dataSnapshot.child(user).child("jjimlist").getChildren().iterator();
                int count = (int)dataSnapshot.child(user).child("jjimlist").getChildrenCount();
                jjimlist = new String[count];
                int i = 0;
                while(i < count){
                    Object jjimpost = iterator.next().getKey();
                    jjimlist[i] = String.valueOf(jjimpost);
                    i++;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        //글 내용 가져오기
        mDatabase.child("contentlist").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //iterator로 자식들 순회하면서 정보 가져옴
                Iterator<DataSnapshot> iterator = dataSnapshot.getChildren().iterator();
                //count = 글 개수
                int count = (int)dataSnapshot.getChildrenCount();
                contentlist = new FirebasePost[count];
                int i = 0;
                int k = 0;
                while(i < count){
                    //정보를 FirebasePost 형식으로 바로 저장후 배열에 넣음
                    FirebasePost fb = iterator.next().getValue(FirebasePost.class);
                    for(int j = 0; j < jjimlist.length; j++){
                        if(jjimlist[j].equals(fb.getPostName())){
                            contentlist[k] = fb;
                            k++;
                        }
                    }
                    i++;
                }

                for(int j = 0; j < k; j++) {
                    setCont(j, contentlist[j]);

                    StorageReference mStorageRef;
                    mStorageRef = FirebaseStorage.getInstance().getReference();
                    //해당 위치의 사진의 이름으로 접근
                    //StorageReference spaceRef = mStorageRef.child(list_itemArrayList.get(position).getImageroute().substring(1));

                    final StorageReference mRef = mStorageRef.child(mMap.get("content" + j).getImageroute().substring(1));
                    final int finalJ = j;

                    //스토리지에서 이미지 다운 URL 가져옴
                    mRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            //성공시 URL 저장, 어댑터 view 변경
                            String downloadurl = uri.toString();
                            mUrl.put("Url"+ finalJ, downloadurl);
                            adapter = new listAdapter(mMap);
                            adapter.setURL(mUrl);
                            listView.setAdapter(adapter);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            //실패시(이미지 없을 때) URL ="" 이미지 없는 채로 어댑터 view 변경
                            String downloadurl = "";
                            mUrl.put("Url"+ finalJ, downloadurl);
                            adapter = new listAdapter(mMap);
                            adapter.setURL(mUrl);
                            listView.setAdapter(adapter);
                        }
                    });
                }
                k = 0;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getActivity(), contents.class);
                intent.putExtra("title", contentlist[position].getPostName());
                Log.d("myTitle", contentlist[position].getPostName());
                intent.putExtra("child", mMap.get("content"+position).getImageroute().substring(1));
                startActivity(intent);
            }
        });

        return view;
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
    public void setCont(int id, FirebasePost fp){
        //불러온 글 내용으로 아이템 배열에 추가
        this.con = fp.context;
        int feel = fp.feel;
        this.feelicon = selectfeelicon(feel);
        this.temp = fp.temperature;
        this.photoname = fp.photo;


        this.mMap.put("content"+id, new list_item(photoname, con, temp, feelicon));
    }
}