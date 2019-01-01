package com.example.mitny.termproject;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

public class listAdapter extends BaseAdapter{
    HashMap<String, list_item> mMap;
    HashMap<String, String> mUrl;

    TextView content1;
    TextView temp1;
    ImageView image1;
    ImageView temp2;
    String photoname;

    public listAdapter(HashMap<String, list_item> map) {
        this.mMap = new HashMap<String, list_item>(map);
    }
    public void setURL(HashMap<String, String > mUrl) {
        this.mUrl = new HashMap<String, String>(mUrl);

    }
    @Override
    public int getCount() {
        return mMap.size();
    }

    @Override
    public Object getItem(int position) {
        return mMap.get("content"+position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        final Context context = parent.getContext();
        if(convertView==null){//xml파일에 있는거 연동
            convertView = LayoutInflater.from(context).inflate(R.layout.item, null);
            content1 = (TextView) convertView.findViewById(R.id.content1);  //제목
            temp1 = (TextView)convertView.findViewById(R.id.temp1);     //날씨
            image1 = (ImageView)convertView.findViewById(R.id.image1);      //옷사진
            temp2 = (ImageView)convertView.findViewById(R.id.temp2);        //느낌 사진
        }

        content1.setText(mMap.get("content"+position).getContent1());
        temp1.setText(mMap.get("content"+position).getTemp1());
        temp2.setImageResource(mMap.get("content"+position).getTemp2());
        photoname = mMap.get("content"+position).getImageroute();
        Glide.with(context).load(mUrl.get("Url"+position)).into(image1);

        return convertView;
    }

}