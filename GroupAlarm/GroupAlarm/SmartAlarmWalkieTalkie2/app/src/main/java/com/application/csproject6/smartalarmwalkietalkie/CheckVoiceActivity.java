package com.application.csproject6.smartalarmwalkietalkie;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.ParseObject;
import com.parse.ParseUser;

import net.daum.mf.speech.api.SpeechRecognizeListener;
import net.daum.mf.speech.api.SpeechRecognizerClient;
import net.daum.mf.speech.api.SpeechRecognizerManager;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

public class CheckVoiceActivity extends Activity implements View.OnClickListener, SpeechRecognizeListener{

    private SpeechRecognizerClient client;
    public static final String APIKEY = "e287479ae78f8f4f0bcaefbfd6dae56c";
    public static String EXTRA_KEY_RESULT_ARRAY = "result_array";

    public static int PASS_THE_TEST = 1;
    public static int FAIL_THE_TEST = 2;
    public static int BEFORE_THE_TEST = 0;
    public ParseObject current_group;
    public static int status = BEFORE_THE_TEST;


    final String filepath = Environment.getExternalStorageDirectory().getPath();

    IalarmControllerInterface acl;
    SampleApplication myApp = null;
    ArrayList<String> testSet = new ArrayList<>(Arrays.asList("소환사의 협곡에 오신것을 \n환영합니다", "산토끼 토끼야 \n어디를 가느냐", "게으른 자여 \n개미에게 배우라","깡총깡총 뛰어서\n어디를 가느냐","구매하신 영수증을\n지참 하셔야 합니다","당신은 무엇을 위해\n살아갑니까"));



    Intent sIntent; //service Intent
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String groupName = getIntent().getStringExtra("groupName");
        for(ParseObject group : ((SampleApplication) getApplication()).getGroupList()){
            if( group.getObjectId().equals(groupName)){
                current_group = group;
                break;
            }
        }

        AudioManager am = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
        am.setStreamVolume(AudioManager.STREAM_MUSIC,15,0);
        ActionBar bar = getActionBar();
        ((SampleApplication)getApplication()).setCurrent_group(current_group);

        bar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        bar.setCustomView(R.layout.cust_actionbar);

        setContentView(R.layout.activity_checkvoice);

        String textOfview = testSet.get((int) (Math.random()*testSet.size()));

        TextView tvText = (TextView)findViewById(R.id.viewText);
        tvText.setText(textOfview);

        ParseUser user = ParseUser.getCurrentUser();
        user.put("status", BEFORE_THE_TEST);
        user.saveInBackground();


        //Daum NewTone API 珥덇린?뷀솕
        myApp = (SampleApplication) getApplicationContext();
        SpeechRecognizerManager.getInstance().initializeLibrary(this);

        findViewById(R.id.checkVoiceBtn).setOnClickListener(this);
        findViewById(R.id.stopCheck).setOnClickListener(this);

        deletePlayDir();
        sIntent = new Intent(getApplicationContext(), ReceivingMessage.class);
        startService(sIntent);

        setButtonsStatus(true);
    }

    protected void onResume(){
        super.onResume();
        Intent it = new Intent(this,alarmController.class);
        bindService(it,sC,BIND_AUTO_CREATE);

    }

    protected void onPause(){
        super.onPause();
        unbindService(sC);
    }

    ServiceConnection sC = new ServiceConnection(){
        public void onServiceConnected(ComponentName name, IBinder service){
            Log.i("onServiceConnected","called!!!!!");
            acl = IalarmControllerInterface.Stub.asInterface(service);
        }
        public void onServiceDisconnected(ComponentName name){
            acl = null;
        }
    };



// ...

    private void setButtonsStatus(boolean enabled) {
        findViewById(R.id.checkVoiceBtn).setEnabled(enabled);
        findViewById(R.id.stopCheck).setEnabled(!enabled);
    }

    public void onDestroy() {
        super.onDestroy();

        //DAUM NEWTONE API ?댁젣
        SpeechRecognizerManager.getInstance().finalizeLibrary();
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

    @Override
    public void onClick(View v) {
        int id = v.getId();

        String serviceType = SpeechRecognizerClient.SERVICE_TYPE_DICTATION;

        Log.i("SpeechSampleActivity", "serviceType : " + serviceType);

        // ?뚯꽦?몄떇 踰꾪듉 listener
        if (id == R.id.checkVoiceBtn) {
            SpeechRecognizerClient.Builder builder = new SpeechRecognizerClient.Builder().
                    setApiKey(APIKEY).
                    setServiceType(serviceType);


            //fix-SH
            if(myApp.music.isPlaying())
                myApp.stopMusic();


            stopService(sIntent);

            client = builder.build();

            client.setSpeechRecognizeListener(this);
            client.startRecording(true);

            setButtonsStatus(false);
        }


        // ?뚯꽦?몄떇 以묒?踰꾪듉 listener
        else if (id == R.id.stopCheck) {
            findViewById(R.id.stopCheck).setEnabled(false);
            if (client != null) {
                client.stopRecording();
            }
        }

    }
    @Override
    public void onReady() {
        //TODO implement interface DaumSpeechRecognizeListener method
    }

    @Override
    public void onBeginningOfSpeech() {
        //TODO implement interface DaumSpeechRecognizeListener method
    }

    @Override
    public void onEndOfSpeech() {
        //TODO implement interface DaumSpeechRecognizeListener method
    }

    @Override
    public void onError(int errorCode, String errorMsg) {
        //TODO implement interface DaumSpeechRecognizeListener method
        Log.e("SpeechSampleActivity", "onError"+errorMsg);
        ParseUser user = ParseUser.getCurrentUser();
        user.put("status", FAIL_THE_TEST);
        user.saveInBackground();
        Handler mHandler = new Handler(Looper.getMainLooper());
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // ?댁슜
                Toast.makeText(getApplicationContext(), "FAIL TO TEST, TRY AGAIN", Toast.LENGTH_SHORT).show();
                Button checkBtn = (Button) findViewById(R.id.checkVoiceBtn);
                checkBtn.setText("Try Again");
                setButtonsStatus(true);
            }
        }, 0);
        //fix-SH

        myApp.startMusic(myApp.selectFile(getApplicationContext()));
        startService(sIntent);


        client = null;
    }

    @Override
    public void onPartialResult(String s) {
        //TODO implement interface DaumSpeechRecognizeListener method
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            ArrayList<String> results = data.getStringArrayListExtra(EXTRA_KEY_RESULT_ARRAY);

            final StringBuilder builder = new StringBuilder();

            for (String result : results) {
                builder.append(result);
                builder.append("\n");
            }

            new AlertDialog.Builder(this).
                    setMessage(builder.toString()).
                    setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }).
                    show();
        }

    }
    @Override
    public void onResults(Bundle results) {
        final StringBuilder builder = new StringBuilder();
        Log.i("SpeechSampleActivity", "onResults");

        ArrayList<String> texts = results.getStringArrayList(SpeechRecognizerClient.KEY_RECOGNITION_RESULTS);
        ArrayList<Integer> confs = results.getIntegerArrayList(SpeechRecognizerClient.KEY_CONFIDENCE_VALUES);


        Log.e("TEXT:",texts.get(0));

        //?뺥솗???뺤씤
        TextView givenTextView = (TextView)findViewById(R.id.viewText);
        String givenText = (String) givenTextView.getText();
        String givenString = givenText.replace("\n","").replaceAll(" ","");
        String getString = null;

        Log.e("TEXT:",givenString);
        for(int i=0; i<texts.size(); i++){
            getString = texts.get(i).replace("\n","").replaceAll(" ","");
            if(LevenshteinDistance.computeLevenshteinDistance(getString,givenString)
                <= 2){
                status = PASS_THE_TEST;
                break;
            }else{
                status = FAIL_THE_TEST;
            }
        }

        if(status == PASS_THE_TEST){
            ParseUser user = ParseUser.getCurrentUser();
            user.put("status", PASS_THE_TEST);
            user.saveInBackground();


            Intent next =  new Intent(getApplicationContext(), GroupAlarmActivity.class);
            startActivity(next);
            finish();
        }else if(status == FAIL_THE_TEST){

            ParseUser user = ParseUser.getCurrentUser();
            user.put("status", FAIL_THE_TEST);
            user.saveInBackground();
            Handler mHandler = new Handler(Looper.getMainLooper());
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    // ?댁슜
                    Toast.makeText(getApplicationContext(), "FAIL TO TEST, TRY AGAIN", Toast.LENGTH_SHORT).show();
                    Button checkBtn = (Button) findViewById(R.id.checkVoiceBtn);
                    checkBtn.setText("Try Again");
                    setButtonsStatus(true);
                }
            }, 0);
            //fix-SH

            myApp.startMusic(myApp.selectFile(getApplicationContext()));
            startService(sIntent);


        }
        client = null;
    }

    @Override
    public void onAudioLevel(float v) {
        //TODO implement interface DaumSpeechRecognizeListener method
    }

    @Override
    public void onFinished() {
        Log.i("SpeechSampleActivity", "onFinished");
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                return true;
            case KeyEvent.KEYCODE_HOME:
                return true;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                return true;
            case KeyEvent.KEYCODE_VOLUME_MUTE:
                return true;
            case KeyEvent.KEYCODE_VOLUME_UP:
                return true;
        }
        return super.onKeyDown(keyCode, event);
    }
    public void deletePlayDir(){
        File dir = new File(filepath + "/"+soundController.AUDIO_PLAYING_FOLDER);
        if(dir.exists()){
            File[] file_list =dir.listFiles();
            for (File file_will_be : file_list){
                file_will_be.delete();
            }
            dir.delete();
        }
    }

    static class LevenshteinDistance {
        private static int minimum(int a, int b, int c) {
            return Math.min(Math.min(a, b), c);
        }

        public static int computeLevenshteinDistance(String str1,String str2) {
            int[][] distance = new int[str1.length() + 1][str2.length() + 1];

            for (int i = 0; i <= str1.length(); i++)
                distance[i][0] = i;
            for (int j = 1; j <= str2.length(); j++)
                distance[0][j] = j;

            for (int i = 1; i <= str1.length(); i++)
                for (int j = 1; j <= str2.length(); j++)
                    distance[i][j] = minimum(
                            distance[i - 1][j] + 1,
                            distance[i][j - 1] + 1,
                            distance[i - 1][j - 1] + ((str1.charAt(i - 1) == str2.charAt(j - 1)) ? 0 : 1));

            return distance[str1.length()][str2.length()];
        }
    }

}