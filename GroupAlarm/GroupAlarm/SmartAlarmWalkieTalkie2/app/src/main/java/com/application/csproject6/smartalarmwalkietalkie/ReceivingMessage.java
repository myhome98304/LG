package com.application.csproject6.smartalarmwalkietalkie;

import android.app.IntentService;
import android.content.Intent;
import android.os.Environment;
import android.util.Log;

import com.parse.FindCallback;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
/**
 * Created by Hoon on 15. 6. 3..
 */
public class ReceivingMessage extends IntentService {
    String TAG = "ReceivingMessage";
    final String filepath = Environment.getExternalStorageDirectory().getPath();

    public static int PASS_THE_TEST = 1;
    public static int FAIL_THE_TEST = 2;
    public static int BEFORE_THE_TEST = 0;
    public ParseObject current_group;

    public ReceivingMessage(){
        super("ReceivingMessage");
    }
    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG, "START");

        File file = new File(filepath, soundController.AUDIO_PLAYING_FOLDER);

        if (!file.exists()) {
            file.mkdirs();
        }
        ParseQuery<ParseObject> soundQuery = ParseQuery.getQuery("voiceMessage");
        ParseUser user = ParseUser.getCurrentUser();
        current_group=((SampleApplication)getApplication()).getCurrent_group();

        while(user.getInt("status") != PASS_THE_TEST)
        {
            Log.i("receivingMsg","while!!");
            ParseUser.getCurrentUser().put("status", user.getInt("status"));
            ParseUser.getCurrentUser().saveInBackground();
            soundQuery.whereEqualTo("group",current_group);
            soundQuery.whereEqualTo("receiver",user.getObjectId());
            soundQuery.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> list, ParseException e) {
                    if (list==null || list.isEmpty()) {
                        Log.i("receivingMsg", "emptyList");
                        return;
                    }
                    else {
                        if (e == null) {
                            for (ParseObject object : list) {
                                Log.i("receivingMsg", "savefile");
                                saveFile(object);
                            }
                            playMusic();

                        } else {
                            Log.d("score", "Error: " + e.getMessage());
                        }
                    }
                }
            });


            try {
                Thread.sleep(15000);
            } catch (InterruptedException e1) {
                e1.printStackTrace();
                ((SampleApplication)getApplicationContext()).stopMusic();
                return;
            }
        }

        Log.d(TAG,"END");
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy()");
        super.onDestroy();
    }


    public void saveFile(ParseObject object){
        final ParseObject obj = object;
        ParseFile parse_file = (ParseFile) object.get("message");
        parse_file.getDataInBackground(new GetDataCallback() {
            @Override
            public void done(byte[] bytes, ParseException e) {
                if (e == null) {
                    // data has the bytes for the resume
                    try {
                        // create temp file that will hold byte array
                        ParseUser sender = (ParseUser) obj.get("sender");
                        String filename = sender.getObjectId();
                        String currentFilePath = filepath + "/" + soundController.AUDIO_PLAYING_FOLDER + "/" + filename + ".wav";
                        File temp = new File(currentFilePath);
                        FileOutputStream fos = new FileOutputStream(temp);
                        fos.write(bytes);
                        fos.flush();
                        fos.close();

                        Log.d(TAG, "Saving File");

                    } catch (IOException ex) {
                        String s = ex.toString();
                        ex.printStackTrace();
                    }

                }
            }
        });
        return;
    }
    public void playMusic(){
        ((SampleApplication) getApplicationContext()).updateSongList();
        if(!((SampleApplication) getApplicationContext()).songs.isEmpty()){
            ((SampleApplication) getApplicationContext()).playSong(((SampleApplication) getApplicationContext()).songs.get(0));
        }

    }

}