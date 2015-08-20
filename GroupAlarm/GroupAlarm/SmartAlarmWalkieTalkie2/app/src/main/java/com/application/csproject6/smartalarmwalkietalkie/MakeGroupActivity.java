package com.application.csproject6.smartalarmwalkietalkie;

import android.app.ActionBar;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.List;

public class MakeGroupActivity extends Activity {
    private EditText newAlarmName;
    private DatePicker datePicker;
    private TimePicker timePicker;
    IalarmControllerInterface acl;
    Intent nextActivity;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActionBar bar = getActionBar();


        bar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        bar.setCustomView(R.layout.cust_actionbar);
        bar.setTitle("그룹 만들기");

        setContentView(R.layout.activity_makegroup);

        newAlarmName = (EditText)findViewById(R.id.newAlarmName);
        datePicker = (DatePicker)findViewById(R.id.datePicker);
        timePicker = (TimePicker)findViewById(R.id.timePicker);

        Button settingAlarmBtn = (Button) findViewById(R.id.settingAlarmBtn);


        nextActivity = new Intent(getApplicationContext(), SelectGroupActivity.class);
        settingAlarmBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
               ParseUser.getCurrentUser().put("status", CheckVoiceActivity.BEFORE_THE_TEST);
               makeNewGroup();
            }
        });

    }

    protected void onResume(){
        super.onResume();
        Intent it = new Intent(this,alarmController.class);
        bindService(it, sC, BIND_AUTO_CREATE);

    }
    @Override
    public void onBackPressed() {
        setResult(1,nextActivity);
        finish();
    }
    protected void onPause(){
        super.onPause();
        unbindService(sC);
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

    public void makeNewGroup(){
        final String groupName = newAlarmName.getText().toString();

        if(groupName.equals("")) {
            Toast.makeText(getApplicationContext(), "그룹 이름을 지정해주세요",Toast.LENGTH_SHORT).show();
        }
        else{
            ParseQuery<ParseObject> query = ParseQuery.getQuery("group");
            query.whereEqualTo("name", groupName);
            query.findInBackground(new FindCallback<ParseObject>() {
                public void done(List<ParseObject> groupList, ParseException e) {
                    if (e == null) {
                        if(groupList.isEmpty()){
                            final int year = datePicker.getYear(); final int month = datePicker.getMonth(); final int day = datePicker.getDayOfMonth();
                            final int hour = timePicker.getCurrentHour(); final int minute = timePicker.getCurrentMinute();


                            ParseUser curUser = ParseUser.getCurrentUser();

                            final ParseObject newGroup = new ParseObject("group");
                            curUser.add("joinGroup", newGroup);

                            newGroup.add("member", curUser);

                            newGroup.put("name", groupName);
                            newGroup.put("year",year);newGroup.put("month",month);newGroup.put("day", day);
                            newGroup.put("hour",hour);newGroup.put("minute", minute);
                            nextActivity.putExtra("name", groupName);
                            nextActivity.putExtra("hour", hour);
                            nextActivity.putExtra("minute", minute);
                            Log.i("makeGroup()", groupName + ": " + year + "/" + month + "/" + day + " " + hour + ":" + minute + "\n");
                            curUser.saveInBackground();
                            newGroup.saveInBackground(new SaveCallback() {
                                @Override
                                public void done(ParseException e) {
                                    try {
                                        acl.addAlarmT(groupName, year, month, day, hour, minute, newGroup.getObjectId());
                                    } catch (RemoteException ee) {
                                        ee.printStackTrace();
                                    }
                                    setResult(0, nextActivity);
                                    finish();
                                }
                            });


                        }
                        else{
                            Toast.makeText(getApplicationContext(), "Group "+ groupName + "Already Exists",Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.d("score", "Error: " + e.getMessage());
                    }
                }
            });
        }
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
}
