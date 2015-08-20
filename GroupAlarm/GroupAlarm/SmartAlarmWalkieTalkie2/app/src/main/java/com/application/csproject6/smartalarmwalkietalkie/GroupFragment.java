package com.application.csproject6.smartalarmwalkietalkie;

import android.app.ListFragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.parse.ParseObject;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by InJae on 2015-05-21.
 */
public class GroupFragment extends ListFragment {
    private ArrayAdapter<String> adapter;
    private ParseUser user;
    ArrayList<ParseObject> groupsObjects;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){

        user = ParseUser.getCurrentUser();

        ArrayList<String> groups =new ArrayList<>();
        groupsObjects = new ArrayList<>();
        Collection<ParseObject> groups_c = user.getList("joinGroup");

        if(groups_c!=null) {
            for (ParseObject group : groups_c){
                try{
                    group.fetchIfNeeded();
                }
                catch(Exception e){

                }

                    groups.add(group.get("name").toString());
                    groupsObjects.add(group);

            }
            SampleApplication app =(SampleApplication)getActivity().getApplication();
            app.setGroupList(groupsObjects);
        }
        else {
            Toast.makeText(getActivity().getApplicationContext(), "No groups", Toast.LENGTH_SHORT).show();

        }
        adapter = new ArrayAdapter<String>(inflater.getContext(), R.layout.group_fragment,R.id.empty,groups);
        setListAdapter(adapter);


        return super.onCreateView(inflater, container, savedInstanceState);
    }


    @Override
    public void onListItemClick(ListView l, View v, int pos, long id) {
        super.onListItemClick(l, v, pos, id);


        Toast.makeText(getActivity().getApplicationContext(),"go to groupAlarm", Toast.LENGTH_LONG).show();
        Intent nextActivity = new Intent(getActivity().getApplicationContext(), GroupAlarmActivity.class);
        SampleApplication app =(SampleApplication)getActivity().getApplication();
        app.setCurrent_group(groupsObjects.get(pos));
        startActivity(nextActivity);
    }

}
