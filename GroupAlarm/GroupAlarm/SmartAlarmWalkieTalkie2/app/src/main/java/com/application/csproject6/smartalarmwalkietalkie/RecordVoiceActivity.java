package com.application.csproject6.smartalarmwalkietalkie;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;
import java.util.Timer;
import java.util.TimerTask;

public class RecordVoiceActivity extends Activity {

    soundController voiceRecorder = null;
    ProgressBar progressbar;
    public static Activity RecordActivity;
    int progress=0;
    Thread thread;
    Timer timer;
    Button recBtn;
    boolean isRecording;
    int recording;
    public class timerTask extends TimerTask{
        @Override
        public void run(){
            if(isRecording!=true){
                Log.i("timerTask", "why stopRecord??");
            }
            else {
                isRecording = false;
                Log.i("timerTask", "stopRecording");
                voiceRecorder.stopRecord();
                Intent nextActivity = new Intent(getApplicationContext(), listenVoiceActivity.class);
                startActivity(nextActivity);
            }
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActionBar bar = getActionBar();

        RecordActivity = RecordVoiceActivity.this;
        bar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        bar.setCustomView(R.layout.cust_actionbar);
        bar.setTitle("메세지를 녹음해주세요");

        setContentView(R.layout.activity_recordvoice);
        progressbar = (ProgressBar)findViewById(R.id.startProgress);
        progressbar.setVisibility(ProgressBar.GONE);

        recBtn = (Button) findViewById(R.id.recBtn);
        isRecording = false;
        voiceRecorder = new soundController();


    }

    protected void onResume(){
        super.onResume();

        progressbar.setVisibility(ProgressBar.GONE);

        timer = new Timer();

        //버튼을 뗄 때 녹음 종료
        recBtn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_UP) {
                    if(isRecording != true){
                        Log.i("ButtonUP","why stopRecording??");
                        return true;
                    }
                    else{
                        isRecording = false;
                        Log.i("ButtonUP","stopRecording");
                        timer.cancel();
                        voiceRecorder.stopRecord();
                        progressbar.setVisibility(ProgressBar.GONE);
                        Toast.makeText(getApplicationContext(),"Recording Stop", Toast.LENGTH_SHORT).show();
                        Intent nextActivity = new Intent(getApplicationContext(),listenVoiceActivity.class);
                        startActivity(nextActivity);
                    }
                }else if(event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_CANCEL){
                    recording=0;
                    progressbar.setVisibility(ProgressBar.VISIBLE);
                    Toast.makeText(getApplicationContext(),"Recording Start", Toast.LENGTH_SHORT).show();
                    voiceRecorder.startRecord();
                    isRecording = true;
                    timer.schedule(new timerTask(), 8000);
                }


                return false;
            }
        });
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
