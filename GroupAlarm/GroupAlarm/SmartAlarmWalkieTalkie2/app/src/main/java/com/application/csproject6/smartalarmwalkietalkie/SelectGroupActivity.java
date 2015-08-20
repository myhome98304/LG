package com.application.csproject6.smartalarmwalkietalkie;

import android.app.ActionBar;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

public class SelectGroupActivity extends FragmentActivity {
    IalarmControllerInterface acl;
    private Button makeGrButton;
    private EditText joinGroup_Name;
    private Button joinGButton;
    private ListView groupListView;
    private group_Adapter adapter;
    private ParseUser user;
    private Button delGroupButton;
    double time;
    ServiceConnection sC;
    private boolean alarmInit;
    String newGroupName;
    int hour;
    int minute;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i("SelectGroup","onCreate");
        super.onCreate(savedInstanceState);
        ActionBar bar = getActionBar();
        alarmInit = false;
        time = System.currentTimeMillis();

        bar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        bar.setCustomView(R.layout.cust_actionbar);

        setContentView(R.layout.activity_selectgroup);
        joinGButton = (Button)findViewById(R.id.groupJoin);
        joinGroup_Name = (EditText)findViewById(R.id.joinGroup);
        makeGrButton = (Button) findViewById(R.id.makeGroupBtn);
        groupListView = (ListView)findViewById(R.id.groupList);
        delGroupButton = (Button)findViewById(R.id.deleteGroup);
        adapter = new group_Adapter();
        user = ParseUser.getCurrentUser();



        ((SampleApplication) getApplicationContext()).music = new MediaPlayer();

        groupUpdate();
        groupListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                ParseObject group = (ParseObject) groupListView.getItemAtPosition(position);
                Intent nextActivity = new Intent(getApplicationContext(), GroupAlarmActivity.class);
                SampleApplication app = (SampleApplication) getApplication();
                app.setCurrent_group(group);
                startActivity(nextActivity);
            }
        });

        makeGrButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Toast.makeText(getApplicationContext(), "go to makeGroup", Toast.LENGTH_SHORT).show();
                Intent nextActivity = new Intent(getApplicationContext(), MakeGroupActivity.class);
                startActivityForResult(nextActivity, 0);
            }
        });

        joinGButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String name = joinGroup_Name.getText().toString();
                if(!name.equals("")){
                    ParseQuery<ParseObject> query = ParseQuery.getQuery("group");
                    query.whereEqualTo("name", name);
                    query.findInBackground(new FindCallback<ParseObject>() {
                        @Override
                        public void done(List<ParseObject> groupList, ParseException e) {

                            if (e == null) {
                                if(groupList.isEmpty())
                                    Toast.makeText(getApplicationContext(),"There is no group \""+name+"\"!",Toast.LENGTH_LONG).show();
                                else {
                                    ParseObject group = groupList.get(0);
                                    if (group.getList("member").contains(ParseUser.getCurrentUser())) {
                                        Toast.makeText(getApplicationContext(), "You are already in \"" + name + "\"!", Toast.LENGTH_LONG).show();
                                    } else {
                                        group.add("member", ParseUser.getCurrentUser());
                                        ParseUser.getCurrentUser().add("joinGroup", group);
                                        adapter.add(group);
                                        adapter.notifyDataSetChanged();
                                        Intent nextActivity = new Intent(getApplicationContext(), GroupAlarmActivity.class);
                                        SampleApplication app = (SampleApplication) getApplication();
                                        try {
                                            acl.addAlarmT(group.get("name").toString(), group.getInt("year"), group.getInt("month"), group.getInt("day"), group.getInt("hour"), group.getInt("minute"),group.getObjectId());
                                        } catch (RemoteException ee) {
                                            ee.printStackTrace();
                                        }
                                        if(app.getGroupList()==null){
                                            app.setGroupList(new ArrayList<ParseObject>());
                                        }

                                        app.getGroupList().add(group);
                                        app.setCurrent_group(group);

                                        ParseUser.getCurrentUser().put("status", CheckVoiceActivity.BEFORE_THE_TEST);
                                        startActivity(nextActivity);
                                    }
                                    group.saveInBackground();
                                }
                            }
                        }
                    }

                );}
            }
        });

        delGroupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                for(int i=adapter.group_List.size()-1;i>-1;i--){
                    ParseObject group = adapter.group_List.remove(i);
                    if(group.getList("member").size()==1){
                        ParseUser.getCurrentUser().getList("joinGroup").remove(group);
                        ParseUser.getCurrentUser().saveInBackground();
                        group.deleteEventually();
                    }
                    else{
                        ParseUser.getCurrentUser().getList("joinGroup").remove(group);
                        ParseUser.getCurrentUser().saveInBackground();
                        group.getList("member").remove(ParseUser.getCurrentUser());
                        group.saveInBackground();
                    }
                    try {
                        acl.clearAlarm();
                    }
                    catch(RemoteException e){
                        e.printStackTrace();
                    }
                }
                adapter.notifyDataSetChanged();

            }
        });

        sC = new ServiceConnection(){
            public void onServiceConnected(ComponentName name, IBinder service){
                Log.i("onServiceConnected", "called!!!!! : ");

                StackTraceElement[] stackTrace = new Exception().getStackTrace();
                Log.i("serviconnected",stackTrace.toString());

                acl = IalarmControllerInterface.Stub.asInterface(service);

                if(alarmInit == false) {
                    try {
                        Log.i("in SelecGroup", "clearAlarm()");
                        acl.clearAlarm();
                        Log.i("in SelecGroup", "addAlarm(initialization)");
                        acl.addAlarm();
                        alarmInit = true;
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }

            }
            public void onServiceDisconnected(ComponentName name){
                acl = null;
            }
        };
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==0){
            newGroupName = data.getStringExtra("name");
            hour = data.getIntExtra("hour",0);
            minute = data.getIntExtra("minute",0);
        }
    }
    //INjae
    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    @Override
    public void onRestart(){
        super.onRestart();
        groupUpdate();
    }


    protected void onResume(){
        super.onResume();

        Intent it = new Intent(this,alarmController.class);
        bindService(it, sC, BIND_AUTO_CREATE);
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

    class group_Adapter extends BaseAdapter {
        private ArrayList<ParseObject> group_List;

        public group_Adapter() {
            group_List = new ArrayList<>();
        }
        @Override
        public int getCount() {
            return group_List.size();
        }

        @Override
        public Object getItem(int i) {
            return group_List.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final int pos = position;
            final Context context = parent.getContext();

            // Ʈ 鼭  ȭ鿡  ʴ  converView null ·
            if ( convertView == null ) {
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.list_item, parent, false);

                // TextView  position ڿ ߰
                final TextView groupName = (TextView) convertView.findViewById(R.id.group_name);
                final Date alarmTime;
                final SimpleDateFormat formatter = new SimpleDateFormat( "알람시간 : HH시 mm분", Locale.KOREA );
                final TextView time = (TextView) convertView.findViewById(R.id.alarm_time);
                final TextView number = (TextView) convertView.findViewById(R.id.number_people);
                final String dTime;
                ParseQuery<ParseObject> query = ParseQuery.getQuery("group");
                if(group_List.get(pos).getObjectId()==null){
                    groupName.setText(newGroupName);
                    time.setText("알람시간 : "+hour+"시 "+minute+"분");
                    number.setText(String.valueOf("총 인원 :            "+"1명"));
                }
                query.getInBackground(group_List.get(pos).getObjectId(), new GetCallback<ParseObject>() {
                    @Override
                    public void done(ParseObject parseObject, ParseException e) {
                        try{
                            Date alarmTime = calcTime(parseObject);
                            String dTime = formatter.format(alarmTime);
                            groupName.setText(parseObject.get("name").toString());
                            time.setText(dTime);
                            number.setText(String.valueOf("총 인원 :            " + num_People(parseObject)+"명"));
                        }
                        catch(Exception ee){

                        }

                    }
                });
            }
            return convertView;
        }

        public void add(ParseObject _msg) {
            group_List.add(_msg);
        }
        public ParseObject get(int position){
            return group_List.get(position);
        }

        public Date calcTime(ParseObject group){
            int year = (int)group.get("year");int month = (int)group.get("month");int day = (int)group.get("day");
            int hour = (int)group.get("hour");int minute = (int)group.get("minute");
            Log.i("calcTime()", year + "/" + month + "/" + day + " " + hour + ":" + minute);
            GregorianCalendar calendar = new GregorianCalendar();
            calendar.set(year, month, day, hour, minute);

            return calendar.getTime();
        }

        public int num_People(ParseObject group){
            List<ParseObject> groups = group.getList("member");
            return groups.size();
        }
    }


    public void groupUpdate(){

        List<ParseObject> groups = ParseUser.getCurrentUser().getList("joinGroup");
        if(groups != null){
            ((SampleApplication)getApplication()).setGroupList((ArrayList)groups);
        }

        if(groups!=null){
            for(ParseObject group: groups){
                try{
                    group.fetchInBackground();
                }
                catch (Exception e){

                }

                if(!adapter.group_List.contains(group)) {
                    adapter.add(group);

                }
            }
            adapter.notifyDataSetChanged();

        }
        groupListView.setAdapter(adapter);
    }
    public void onDestroy() {
        super.onDestroy();
        ((SampleApplication)getApplicationContext()).music.release();
    }
}

