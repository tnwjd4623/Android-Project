package com.example.mitny.termproject;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import static android.app.Activity.RESULT_OK;
import static android.content.Intent.ACTION_MEDIA_SCANNER_SCAN_FILE;

public class writeFragment extends Fragment{
    public static final int f_cold = 1;
    public static final int f_littlecold = 2;
    public static final int f_good = 3;
    public static final int f_littlehot = 4;
    public static final int f_hot = 5;

    //최종저장 내역
    public String save_temp;
    public String save_clothes;//글내용
    public String save_comment;//코멘트
    public String save_photourl;//사진url
    public int save_feel;
    public String save_filename;//저장사진이름
    public String save_name;//db저장시제목(임시)
    public String save_user;

    ImageButton wbutton1, wbutton2, wbutton3, wbutton4, wbutton5; //날씨아이콘버튼
    int weatherfeel;
    EditText clothes, comment;

    TextView weatherTextView;
    TextView gpsTextView;
    private GPS GPS;
    ImageButton refreshButton;
    ImageView photo;
    Button upload;
    Button save;

    private static final int PICK_FROM_CAMERA = 0;
    private static final int PICK_FROM_ALBUM = 1;
    private static final int CROP_FROM_IMAGE = 2;

    private Uri mImageCaptureUri;
    private String absoultePath;
    ByteArrayOutputStream out = null;



    public writeFragment() {
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.write_fragment, container, false);
        weatherTextView = (TextView)view.findViewById(R.id.weatherText);
        gpsTextView = (TextView)view.findViewById(R.id.gpsText);
        refreshButton = (ImageButton)view.findViewById(R.id.w_refreshbutton);
        photo = (ImageView)view.findViewById(R.id.photo);
        upload = (Button)view.findViewById(R.id.upload);
        save = (Button)view.findViewById(R.id.save);
        wbutton1 = (ImageButton)view.findViewById(R.id.button);
        wbutton2 = (ImageButton)view.findViewById(R.id.button2);
        wbutton3 = (ImageButton)view.findViewById(R.id.button3);
        wbutton4 = (ImageButton)view.findViewById(R.id.button4);
        wbutton5 = (ImageButton)view.findViewById(R.id.button5);
        clothes = (EditText)view.findViewById(R.id.editText);
        comment = (EditText)view.findViewById(R.id.editText2);

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

        refreshButton.setOnClickListener(new View.OnClickListener() {
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

                new AlertDialog.Builder(getContext())
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
                writeNewPost();
            }
        });
        return view;
    }

    public void setWeather(String weather) {
        save_temp = weather;
        weatherTextView.setText(weather+"°C");
    }
    public void setGPS(String gps) {
        gpsTextView.setText(gps);
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
            getActivity().sendBroadcast(intent);
            out.flush();
            out.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void writeNewPost() {//글 쓰기
        DatabaseReference mDatabase;
        mDatabase = FirebaseDatabase.getInstance().getReference();
        StorageReference mStorageRef;
        mStorageRef = FirebaseStorage.getInstance().getReference();
        //db, storage 연동
        String key = mDatabase.child("contentlist").push().getKey();
        //온도, 느낌, 내용, 코멘트, 사진을 저장한 이름 순으로 넘겨줌
        FirebasePost post = new FirebasePost(save_user, save_temp, save_feel, save_clothes, save_comment, save_filename);
        post.toMap();
        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/contentlist/"+save_name, post);
        mDatabase.updateChildren(childUpdates);
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

        Toast.makeText(getContext(), "게시글을 성공적으로 업로드하였습니다.", Toast.LENGTH_SHORT).show();
    }
}
