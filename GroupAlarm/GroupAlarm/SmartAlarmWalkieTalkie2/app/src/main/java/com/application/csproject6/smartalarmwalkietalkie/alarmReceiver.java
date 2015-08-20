package com.application.csproject6.smartalarmwalkietalkie;

/**
 * Created by hs on 2015-05-26.
 */

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.util.Log;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static com.facebook.FacebookSdk.getApplicationContext;

public class alarmReceiver extends BroadcastReceiver {
    SampleApplication myApp = null;

    public void onReceive(Context context, Intent intent){
        long currTime = System.currentTimeMillis();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date resultdate = new Date(currTime);
        System.out.println();

        Log.i("alarmReceiver", "at " + sdf.format(resultdate) + "(" + currTime + ")");

        myApp = (SampleApplication)getApplicationContext();
        myApp.startMusic(selectFile(context));

        String groupName = intent.getStringExtra("groupName");
        Intent i = new Intent(context, CheckVoiceActivity.class);
        i.putExtra("groupName",groupName);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(i);
    }



    public AssetFileDescriptor selectFile(Context context){
        try {
            AssetFileDescriptor descriptor = context.getAssets().openFd("alarm02.mp3");
            return descriptor;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


}
