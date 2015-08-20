package com.application.csproject6.smartalarmwalkietalkie;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.ParseObject;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

public class GroupAlarmActivity extends Activity {
    ParseObject group;
    TextView groupName;
    private user_Adapter adapter;
    private ListView listview;
    SampleApplication my_app;
    double time;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        my_app =(SampleApplication) getApplicationContext();
        super.onCreate(savedInstanceState);
        ActionBar bar = getActionBar();
        time=System.currentTimeMillis();
        bar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        bar.setCustomView(R.layout.cust_actionbar);
        setContentView(R.layout.activity_groupalarm);

        Collection<ParseObject> groups = ParseUser.getCurrentUser().getList("joinGroup");
        Intent intent = getIntent();
        updateListView();

        groupName = (TextView)findViewById(R.id.groupName);
        groupName.setText(group.get("name").toString());





        Button recordVoiceBtn = (Button) findViewById(R.id.recordVoiceBtn);

        if(ParseUser.getCurrentUser().getInt("status") != CheckVoiceActivity.PASS_THE_TEST)
            recordVoiceBtn.setEnabled(false);

        recordVoiceBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(),"go to recordVoice", Toast.LENGTH_LONG).show();
                Intent nextActivity = new Intent(getApplicationContext(),RecordVoiceActivity.class);
                startActivity(nextActivity);
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
    public void onRestart(){
        super.onStart();
        updateListView();
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
    public void onBackPressed() {
        super.onBackPressed();
        this.finish();
    }


    @Override
    protected void onResume() {
        Log.i("GroupAlarm", "onResume");
        super.onResume();
        updateListView();
    }

    class user_Adapter extends BaseAdapter {
        private ArrayList<ParseUser> user_List;

        public user_Adapter() {
            user_List = new ArrayList<>();
        }
        @Override
        public int getCount() {
            return user_List.size();
        }

        @Override
        public Object getItem(int i) {
            return user_List.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final int pos = position;
            final Context context = parent.getContext();

            // ? ?  ??  ?  converView null 째짠
            if ( convertView == null ) {
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.user_status, parent, false);

                // TextView  position ? ?

                ParseUser user = user_List.get(position);
                int status = user.getInt("status");
                if(status==0){
                    TextView name = (TextView) convertView.findViewById(R.id.sleepUser);
                    name.setText(user.get("name").toString());
                    my_app.LazyUserList.add(user.getObjectId());
                }
                else if(status == 2){
                    TextView name = (TextView) convertView.findViewById(R.id.hesitateUser);
                    name.setText(user.get("name").toString());
                    my_app.LazyUserList.add(user.getObjectId());
                }
                else {
                    TextView name = (TextView) convertView.findViewById(R.id.wakeupUser);
                    name.setText(user.get("name").toString());
                }
            }
            return convertView;
        }


        // ??▣? ? ?
        public void add(ParseUser _msg) {
            user_List.add(_msg);
        }
        public ParseUser get(int position){
            return user_List.get(position);
        }
    }

    public void updateListView(){
        Date current = new Date();
        group = ((SampleApplication)getApplication()).getCurrent_group();


        try{
            group.fetch();
        }
        catch(Exception e){

        }


        List<ParseUser> users = group.getList("member");
        adapter = new user_Adapter();
        listview = (ListView)findViewById(R.id.userList);
        my_app.LazyUserList = new ArrayList<>();

        if(users!=null){
            int i = 0;
            for(ParseUser user: users){

                try{
                    user.fetch();
                }
                catch(Exception e){

                }

                if(!user.getObjectId().equals(ParseUser.getCurrentUser().getObjectId())) {
                    adapter.add(user);
                    i++;
                }
            }
            if(i != 0){
                adapter.notifyDataSetChanged();
            }

        }
        listview.setAdapter(adapter);
    }
}