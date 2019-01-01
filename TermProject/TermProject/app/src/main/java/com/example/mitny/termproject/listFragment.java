package com.example.mitny.termproject;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import static android.support.v4.content.PermissionChecker.checkSelfPermission;

public class listFragment extends Fragment {
    listAdapter adapter;                             //리스트어뎁터
    HashMap<String, list_item> mMap;
    HashMap<String, String> mUrl;

    ListView listView;//리스트 출력하는 레이아웃
    TextView weather_textView;
    TextView gps_textView;
    ImageView weatherImage;
    private GPS GPS;
    ImageButton refreshbutton;
    String[] content = new String[7];
    ImageView recommendimg, recommendtempimg;
    TextView recommendtemp, recommendcontent;
    LinearLayout recommenditem;

    FirebasePost[] contentlist = null;
    FirebasePost recommend = null;
    String con;
    int feelicon;
    String temp;
    String photoname;

    TextView recommendname;
    private FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();
    private DatabaseReference mReference = mDatabase.getReference();

    public listFragment() {

    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.list_fragment, container, false);
        listView = (ListView)view.findViewById(R.id.listview1);     //리스트 뷰 출력되는 곳
        weather_textView= (TextView)view.findViewById(R.id.weatherText);
        gps_textView = (TextView)view.findViewById(R.id.gpsText);
        weatherImage = (ImageView)view.findViewById(R.id.weatherImage);
        refreshbutton = (ImageButton)view.findViewById(R.id.refreshbutton);
        recommendcontent = (TextView)view.findViewById(R.id.recommendcontent);
        recommendimg = (ImageView)view.findViewById(R.id.recommendimage);
        recommendtemp = (TextView) view.findViewById(R.id.recommendtemp);
        recommendtempimg = (ImageView)view.findViewById(R.id.recommendtempimg);
        recommenditem = (LinearLayout)view.findViewById(R.id.recommenditem);
        recommendname = (TextView)view.findViewById(R.id.recommendname);

        mMap = new HashMap<String, list_item>();            //사진 url 제외 글 정보 저장
        mUrl = new HashMap<String, String>();               //url 정보 저장

        GPS = new GPS(getContext());

        if(GPS.isGetLocation()) {
            double latitude = GPS.getLatitude();
            double longitude = GPS.getLongitude();

            String str = GPS.getGrid(latitude, longitude);
            StringTokenizer t = new StringTokenizer(str, ",");

            int X = Integer.parseInt(t.nextToken());
            int Y = Integer.parseInt(t.nextToken());
            WeatherAPI(X, Y);
            MapAPI(latitude, longitude);

        }
        else {
            GPS.showSettingsAlert();;
        }
        
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getActivity(), contents.class);
                intent.putExtra("position", position);
                intent.putExtra("child", mMap.get("content"+position).getImageroute().substring(1));
                startActivity(intent);
            }
        });
        recommenditem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(recommend != null){
                    Intent intent = new Intent(getActivity(), contents.class);
                    String name = recommendname.getText().toString();
                    Log.d("recommendname", name);
                    intent.putExtra("title", name);
                    intent.putExtra("child", recommend.photo);
                    startActivity(intent);
                }
            }
        });

        refreshbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GPS = new GPS(getContext());
                if(GPS.isGetLocation()) {
                    double latitude = GPS.getLatitude();
                    double longitude = GPS.getLongitude();

                    String str = GPS.getGrid(latitude, longitude);
                    StringTokenizer t = new StringTokenizer(str, ",");

                    int X = Integer.parseInt(t.nextToken());
                    int Y = Integer.parseInt(t.nextToken());
                    WeatherAPI(X, Y);
                    MapAPI(latitude, longitude);

                }
                else {
                    GPS.showSettingsAlert();;
                }
            }
        });

        return view;
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == 0) {
            onResume();
        }
    }

    public void setWeather(String weather) {
        weather_textView.setText(weather+"°C");
    }
    public void setGPS(String gps) {
        gps_textView.setText(gps);
    }
    public void setDescription(String description) {
        description = description.toLowerCase();
        if(description.equals("0")){
            weatherImage.setImageResource(R.drawable.clear);
        }else if(description.contains("1")){
            weatherImage.setImageResource(R.drawable.rain);
        }else if(description.contains("2")){
            weatherImage.setImageResource(R.drawable.rain_snow);
        }else{
            weatherImage.setImageResource(R.drawable.snow);
        }
    }

    //날씨 API 호출
    public void WeatherAPI(int X, int Y) {
        String API_Key = "mL5xQ8wYroWD4EAKfbSfoa9coea5YnX0n8iCN9ph4PWpPTWzNWglQPiXYyAGW%2B9WqVy52swX0B7TlN8fqiXo8w%3D%3D";
        Date date = new Date(System.currentTimeMillis());
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH");

        String getDate = dateFormat.format(date);
        String getTime = timeFormat.format(date);
        int hour = Integer.parseInt(getTime)-1;

        if(hour>=10) {
            getTime = hour+"00";
        }
        else if(hour <10) {
            getTime = "0"+hour+"00";
        }

        String urlStr = "http://newsky2.kma.go.kr/service/SecndSrtpdFrcstInfoService2/ForecastGrib?ServiceKey="+API_Key+"&base_date="+getDate+"&base_time="+getTime+"&nx="+X+"&ny="+Y+"&_type=json";
        ReceiveWeatherTask receiveWeatherTask = new ReceiveWeatherTask();
        receiveWeatherTask.execute(urlStr);
    }
    private class ReceiveWeatherTask extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }
        @Override
        protected String doInBackground(String... datas) {
            try {
                HttpURLConnection conn = (HttpURLConnection)new URL(datas[0]).openConnection();
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(10000);
                conn.connect();

                if(conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    InputStream is = conn.getInputStream();
                    InputStreamReader reader = new InputStreamReader(is);
                    BufferedReader in = new BufferedReader(reader);

                    String readed;
                    while((readed = in.readLine()) != null) {
                        return readed;
                    }
                }
                else {
                    return "";
                }
            }catch (Exception e) {
                e.printStackTrace();
            }
            return "";
        }
        @Override
        protected void onPostExecute(String result) {
            Log.d("postexe1", result);
            try {
                JSONObject jsonObject = new JSONObject(result);
                String str = jsonObject.getJSONObject("response").getJSONObject("body").getJSONObject("items").getJSONArray("item").getJSONObject(3).getString("obsrValue").toString();

                setWeather(str);
                String rain = jsonObject.getJSONObject("response").getJSONObject("body").getJSONObject("items").getJSONArray("item").getJSONObject(0).getString("obsrValue").toString();
                setDescription(rain);

                DatabaseReference mDatabase;
                mDatabase = FirebaseDatabase.getInstance().getReference();
                final StorageReference mStorageRef;
                mStorageRef = FirebaseStorage.getInstance().getReference();

                //글 내용 가져오기
                mDatabase.child("contentlist").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        //iterator로 자식들 순회하면서 정보 가져옴
                        Iterator<DataSnapshot> iterator = dataSnapshot.getChildren().iterator();
                        //count = 글 개수
                        int count = (int)dataSnapshot.getChildrenCount();
                        contentlist = new FirebasePost[count+1];
                        String nowtemperature = weather_textView.getText().toString();
                        float nowtemp = Float.parseFloat(nowtemperature.substring(0,nowtemperature.length()-3));
                        int i = 0;
                        while(i < count){
                            //정보를 FirebasePost 형식으로 바로 저장후 배열에 넣음
                            FirebasePost fb = iterator.next().getValue(FirebasePost.class);
                            contentlist[i] = fb;

                            String dbtemperature = fb.temperature;
                            float dbtemp = Float.parseFloat(dbtemperature);

                            if(nowtemp + 5 >= dbtemp && nowtemp - 5 <= dbtemp){
                                if(recommend == null) recommend = fb;
                                else if(fb.good > recommend.good) recommend = fb;
                            }
                            i++;
                        }

                        if(recommend == null){
                            recommendcontent.setText("현재 기온에 알맞은 추천 글이 없습니다.");
                        }else{
                            recommendname.setText(recommend.getPostName());
                            recommendcontent.setText(recommend.context);
                            recommendtemp.setText(recommend.temperature);
                            recommendtempimg.setImageResource(selectfeelicon(recommend.feel));

                            StorageReference reRef = mStorageRef.child(recommend.photo);
                            reRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    String downloadurl = uri.toString();
                                    Glide.with(getContext()).load(downloadurl).into(recommendimg);
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    String downloadurl = "";
                                }
                            });
                        }

                        for(int j = 0; j < count; j++) {
                            setCont(j, contentlist[j]);

                            StorageReference mStorageRef;
                            mStorageRef = FirebaseStorage.getInstance().getReference();


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
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    //지도 API
    public void MapAPI(double latitude, double longitude) {
        try {
            String geocode = URLEncoder.encode(longitude+","+latitude, "UTF-8");
            String url = "https://openapi.naver.com/v1/map/reversegeocode?query="+geocode;
            ReceiveMapTask receiveMapTask = new ReceiveMapTask();
            receiveMapTask.execute(url);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }
    private class ReceiveMapTask extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... datas) {
            try {
                String clientId = "jZawGNWf455qVfd4tYvK";
                String clientSecret = "mJCq1wh55u";
                HttpURLConnection conn = (HttpURLConnection) new URL(datas[0]).openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("X-Naver-Client-Id", clientId);
                conn.setRequestProperty("X-Naver-Client-Secret", clientSecret);

                int responseCode = conn.getResponseCode();
                BufferedReader br;
                if (responseCode == 200) { // 정상 호출
                    br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                } else {  // 에러 발생
                    br = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
                }
                String inputLine;
                String readed = "";
                while ((inputLine = br.readLine()) != null) {
                    readed += inputLine;
                }
                br.close();
                return readed;

            } catch (Exception e) {
                e.printStackTrace();
            }
            return "";
        }

        @Override
        protected void onPostExecute(String result) {
            try {
                JSONObject jsonObject = new JSONObject(result);
                String str = jsonObject.getJSONObject("result").getJSONArray("items").getJSONObject(0).getString("address").toString();
                setGPS(str);
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
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



    @Override
    public void onResume() {
        super.onResume();
        mMap = new HashMap<String, list_item>();            //사진 url 제외 글 정보 저장
        mUrl = new HashMap<String, String>();               //url 정보 저장

        GPS = new GPS(getContext());
        if(GPS.isGetLocation()) {
            double latitude = GPS.getLatitude();
            double longitude = GPS.getLongitude();

            String str = GPS.getGrid(latitude, longitude);
            StringTokenizer t = new StringTokenizer(str, ",");

            int X = Integer.parseInt(t.nextToken());
            int Y = Integer.parseInt(t.nextToken());
            WeatherAPI(X, Y);
            MapAPI(latitude, longitude);

        }
        else {
            GPS.showSettingsAlert();;
        }


        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getActivity(), contents.class);
                intent.putExtra("position", position);
                intent.putExtra("child", mMap.get("content"+position).getImageroute().substring(1));
                startActivity(intent);
            }
        });



        refreshbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GPS = new GPS(getContext());
                if(GPS.isGetLocation()) {
                    double latitude = GPS.getLatitude();
                    double longitude = GPS.getLongitude();

                    String str = GPS.getGrid(latitude, longitude);
                    StringTokenizer t = new StringTokenizer(str, ",");

                    int X = Integer.parseInt(t.nextToken());
                    int Y = Integer.parseInt(t.nextToken());
                    WeatherAPI(X, Y);
                    MapAPI(latitude, longitude);

                }
                else {
                    GPS.showSettingsAlert();;
                }
            }
        });
    }
}
