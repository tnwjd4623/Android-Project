package com.example.mitny.termproject;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

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
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.StringTokenizer;

public class My_ListFragment extends Fragment {
    listAdapter adapter;                             //리스트어뎁터
    HashMap<String, list_item> mMap;
    HashMap<String, String> mUrl;

    ListView listView;                               //리스트 출력하는 레이아웃
    TextView weather_textView;
    TextView gps_textView;
    ImageView weatherImage;
    private GPS GPS;
    ImageButton refreshbutton;
    String[] content = new String[7];
    String user = FirebaseAuth.getInstance().getCurrentUser().getUid();

    FirebasePost[] contentlist = null;
    String con;
    int feelicon;
    String temp;
    String photoname;

    private FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();
    private DatabaseReference mReference = mDatabase.getReference();

    public My_ListFragment() {

    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.my_list_fragment, container, false);
        listView = (ListView)view.findViewById(R.id.listview1);     //리스트 뷰 출력되는 곳
        weather_textView= (TextView)view.findViewById(R.id.weatherText);
        gps_textView = (TextView)view.findViewById(R.id.gpsText);
        weatherImage = (ImageView)view.findViewById(R.id.weatherImage);
        refreshbutton = (ImageButton)view.findViewById(R.id.refreshbutton);

        mMap = new HashMap<String, list_item>();            //사진 url 제외 글 정보 저장
        mUrl = new HashMap<String, String>();               //url 정보 저장

        //db, storage연동
        final DatabaseReference mDatabase;
        mDatabase = FirebaseDatabase.getInstance().getReference();
        StorageReference mStorageRef;
        mStorageRef = FirebaseStorage.getInstance().getReference();

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
                    String uid = fb.userid;
                    if(uid.equals(user)){
                        contentlist[k] = fb;
                        k++;
                    }
                    //contentlist[i] = iterator.next().getValue(FirebasePost.class);
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


}
