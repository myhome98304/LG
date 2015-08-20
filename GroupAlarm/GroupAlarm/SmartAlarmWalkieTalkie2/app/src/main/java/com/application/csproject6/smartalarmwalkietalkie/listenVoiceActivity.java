package com.application.csproject6.smartalarmwalkietalkie;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Hoon on 15. 5. 20..
 */
public class listenVoiceActivity extends Activity
{
    MediaPlayer music = new MediaPlayer();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActionBar bar = getActionBar();


        bar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        bar.setCustomView(R.layout.cust_actionbar);
        bar.setTitle("Message");

        setContentView(R.layout.activity_listenvoice);


        AudioManager am = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
        am.setStreamVolume(AudioManager.STREAM_MUSIC, 15, 0);

        ImageButton playBtn = (ImageButton) findViewById(R.id.play);
        ImageButton stopBtn  = (ImageButton) findViewById(R.id.stop);
        Button tryBtn= (Button) findViewById(R.id.tryBtn);
        Button sendBtn= (Button) findViewById(R.id.sendBtn);
        final File file = selectFile();

        playBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                music.reset();

                try {
                    FileInputStream fis = new FileInputStream(file);
                    FileDescriptor fd = fis.getFD();
                    music.setDataSource(fd);
                    music.prepare();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                music.start();
            }
        });

        stopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                music.stop();
            }
        });

        tryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("listenVoice","tryBtn to deleteFile");
                music.stop();
                file.delete();

                finish();/*
                Intent recordActivity = new Intent(getApplicationContext(), RecordVoiceActivity.class);
                startActivity(recordActivity);*/
            }
        });


        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ParseUser curUser = ParseUser.getCurrentUser();

                ParseQuery<ParseObject> soundQuery = ParseQuery.getQuery("voiceMessage");
                ParseObject current_group = ((SampleApplication) getApplication()).getCurrent_group();
                soundQuery.whereEqualTo("group",current_group);
                soundQuery.whereEqualTo("sender",curUser);
                soundQuery.findInBackground(new FindCallback<ParseObject>() {
                    @Override
                    public void done(List<ParseObject> list, ParseException e) {
                        if(list == null || list.isEmpty()) {
                            Log.i("sending","Nothing to delete");
                            return;
                        }else{
                            for (ParseObject obj : list) {
                                try {
                                    Log.i("sending","Something deleted");
                                    obj.delete();
                                } catch (ParseException e1) {
                                    e1.printStackTrace();
                                }
                            }
                        }
                    }
                });


                ArrayList<String> nextUser = ((SampleApplication) getApplicationContext()).LazyUserList;
                ParseObject newVoiceMsg = new ParseObject("voiceMessage");

                newVoiceMsg.put("sender", curUser);
                for(String lazyuser : nextUser){
                    newVoiceMsg.add("receiver",lazyuser);
                }

                //File file = selectFile();
                FileInputStream fis;
                try {
                    fis = new FileInputStream(file);
                }
                catch(FileNotFoundException e){
                    e.printStackTrace();
                    return;
                }
                byte[] buffer = new byte[(int)file.length()];
                try {
                    fis.read(buffer, 0, buffer.length);
                    fis.close();
                }
                catch(IOException e){
                    e.printStackTrace();
                    return;
                }

                ParseFile pfile = new ParseFile("sendFile",buffer);
                newVoiceMsg.put("message", pfile);
                newVoiceMsg.put("group", ((SampleApplication) getApplication()).getCurrent_group());
                newVoiceMsg.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e == null) {
                            file.delete();
                        }
                    }
                });


                Log.i("listenVoice","sendBtn to deleteFile");
                deleteFile();

                Log.i("listenvoice", "RActivity Finish");
                RecordVoiceActivity RActivity = (RecordVoiceActivity)RecordVoiceActivity.RecordActivity;
                RActivity.finish();
                Log.i("listenvoice", "finish");
                finish();

            }
        });

    }
    public File selectFile(){
        soundController sdc = new soundController();
        String filepath = Environment.getExternalStorageDirectory().getPath();
        File dir = new File(filepath, sdc.AUDIO_RECORDER_FOLDER);
        File[] fileList = dir.listFiles();
        File file = fileList[0];

        return file;
    }
    public void deleteFile(){
        File file = selectFile();
        file.delete();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_login, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}