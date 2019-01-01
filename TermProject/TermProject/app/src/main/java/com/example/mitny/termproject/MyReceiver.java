package com.example.mitny.termproject;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

public class MyReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();
        String s = bundle.getString("DATA");

        Toast.makeText(context, String.format("%s를 수신했습니다.", s),
                Toast.LENGTH_SHORT).show();
    }
}
