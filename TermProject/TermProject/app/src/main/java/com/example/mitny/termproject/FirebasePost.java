package com.example.mitny.termproject;

import java.util.HashMap;
import java.util.Map;

public class FirebasePost {
    public String userid;
    public String temperature;//온도
    public int feel;//온도 느낌
    public String context;//글내용
    public String comment;//코멘트
    public String photo;//사진저장주소
    public int good;//도움됐어요 수

    public FirebasePost(){

    }

    public String getPostName(){
        String postName = "t"+photo.substring(13,26);
        return postName;
    }

    public FirebasePost(String temperature, int feel, String context, String comment, String photo){
        this.temperature = temperature;
        this.feel = feel;
        this.context = context;
        this.comment = comment;
        this.photo = photo;
        this.good = 0;
    }

    public FirebasePost(String userid, String temperature, int feel, String context, String comment, String photo){
        this.userid = userid;
        this.temperature = temperature;
        this.feel = feel;
        this.context = context;
        this.comment = comment;
        this.photo = photo;
        this.good = 0;
    }

    //이름, 값으로 매칭해줌
    public Map<String, Object> toMap(){
        HashMap<String, Object> result = new HashMap<>();
        result.put("userid", userid);
        result.put("temperature", temperature);
        result.put("feel", feel);
        result.put("context", context);
        result.put("comment", comment);
        result.put("photo", photo);
        result.put("good", good);
        return result;
    }
}