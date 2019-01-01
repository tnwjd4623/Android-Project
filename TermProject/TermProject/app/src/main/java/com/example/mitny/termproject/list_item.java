package com.example.mitny.termproject;

public class list_item {
    private int image1;
    private String content1;//글내용
    private String temp1;//온도
    private int temp2;//온도 느낌
    private String imageroute;//이미지 저장 위치

    public int getImage1() {
        return image1;
    }

    public String getContent1() {
        return content1;
    }

    public String getTemp1() {
        return temp1;
    }

    public void setImage1(int image1) {
        this.image1 = image1;
    }

    public void setContent1(String content1) {
        this.content1 = content1;
    }

    public void setTemp1(String temp1) {
        this.temp1 = temp1;
    }

    public void setTemp2(int temp2) {
        this.temp2 = temp2;
    }

    public int getTemp2() {
        return temp2;
    }

    public void setImageroute(String imageroute){this.imageroute = imageroute;}

    public String getImageroute(){return this.imageroute;}

    public list_item(String imageroute, String content1, String temp1, int temp2) {
        this.imageroute = imageroute;
        //this.image1 = image1;
        this.content1 = content1;
        this.temp1 = temp1;
        this.temp2 = temp2;

    }
}