package com.example.mitny.termproject;

import java.util.HashMap;
import java.util.Map;

public class userlistPost {
    public String[] goodlist;
    public String[] jjimlist;

    public userlistPost(){

    }

    public userlistPost(String[] goodlist){
        this.goodlist = goodlist;
        this.jjimlist = null;
    }

    public userlistPost(String[] goodlist, String[] jjimlist){
        this.goodlist = goodlist;
        this.jjimlist = jjimlist;
    }

    public Map<String, Object> toMap(){
        HashMap<String, Object> result = new HashMap<>();
        result.put("goodlist", goodlist);
        result.put("jjimlist", jjimlist);
        return result;
    }
}
