package com.application.csproject6.smartalarmwalkietalkie;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.parse.ParseObject;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.List;

public class alarmController extends Service {
    private class alarm{
        String alarmName;
        int year,month,day;
        int hour,minute;

        alarm(String aN, int aY, int aMo, int aD, int aH, int aMi){
            alarmName = aN;
            year = aY; month = aMo; day = aD;
            hour = aH; minute = aMi;
        }
    }

    List<alarm> alarmList;
    List<PendingIntent> pendingIntentList;
    AlarmManager aManager;

    public void onCreate(){
        aManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
    }

    public alarmController() {
        alarmList = new ArrayList<alarm>();
        pendingIntentList = new ArrayList<PendingIntent>();
    }

    IalarmControllerInterface.Stub ACL = new IalarmControllerInterface.Stub(){

        public void addAlarm() throws RemoteException{
            Log.i("alarmcontroller","addAlarm()");
              ParseUser currUser = ParseUser.getCurrentUser();
              Collection<ParseObject> groups_c = currUser.getList("joinGroup");

                int i=0;
                if(groups_c!=null) {
                    for (ParseObject group : groups_c){
                        try{group.fetchIfNeeded();}
                        catch(Exception e){e.printStackTrace();return;}

                        String groupName = (String)group.get("name");
                        int year = (int)group.get("year");int month = (int)group.get("month");int day = (int)group.get("day");
                        int hour = (int)group.get("hour");int minute = (int)group.get("minute");

                        alarm a = new alarm(groupName,year,month,day,hour,minute);

                        GregorianCalendar calendar = new GregorianCalendar();
                        calendar.set(year,month,day,hour,minute,0);

                        long alarmTime = calendar.getTimeInMillis();
                        long currTime = System.currentTimeMillis();
                        if(currTime > alarmTime){ Log.i("addAlarm()","already passed"); continue;}
                        else{Log.i("addAlarm()","at " + year + "/"+ month + "/"+ day + " " + hour + ":"+ minute + "(" + alarmTime +")");}

                        ((SampleApplication) getApplication()).setCurrent_group(group);

                        Intent alarmIntent = new Intent(getApplicationContext(), alarmReceiver.class);
                        alarmIntent.putExtra("groupName",group.getObjectId());
                        PendingIntent tempPendingIntent = PendingIntent.getBroadcast(getApplicationContext(),i,alarmIntent,PendingIntent.FLAG_ONE_SHOT);
                        pendingIntentList.add(i, tempPendingIntent);
                        alarmList.add(i, a);
                        aManager.set(AlarmManager.RTC_WAKEUP, alarmTime ,tempPendingIntent);
                        i++;
                    }
                }
        }

        public void clearAlarm() throws RemoteException{
            Log.i("clearAlarm","clear " + pendingIntentList.size());
            int i;
            for(i=0;i<pendingIntentList.size();i++){
                aManager.cancel(pendingIntentList.get(i));
            }
            pendingIntentList = new ArrayList<PendingIntent>();
            alarmList = new ArrayList<alarm>();
        }

        public void addAlarmT(String aN, int aY, int aMo, int aD, int aH, int aMi, String groupId) throws RemoteException{
            Log.i("alarmController", "call alarmAddT()");
            GregorianCalendar calendar = new GregorianCalendar();

            alarm a = new alarm(aN,aY,aMo,aD,aH,aMi);

            int totalAlarm = alarmList.size();
            int totalPendingIntent = pendingIntentList.size();

            if(totalAlarm!=totalPendingIntent){
                Log.i("alarmController","why different pendings and alarms?");
            }

            calendar.set(aY,aMo,aD,aH,aMi,0);


            long alarmTime = calendar.getTimeInMillis();
            long currTime = System.currentTimeMillis();
            if(currTime > alarmTime){
                Log.i("addAlarmT()","already passed");
                return;
            }
            else{Log.i("addAlarmT()","at " + aY + "/"+ aMo + "/"+ aD + " " + aH + ":"+ aMi + "(" + alarmTime +")");}



            Intent alarmIntent = new Intent(getApplicationContext(), alarmReceiver.class);
            alarmIntent.putExtra("groupName",groupId);


            PendingIntent tempPendingIntent = PendingIntent.getBroadcast(getApplicationContext(),totalAlarm,alarmIntent,PendingIntent.FLAG_ONE_SHOT);

            pendingIntentList.add(totalAlarm,tempPendingIntent);
            alarmList.add(totalAlarm,a);

            aManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis() ,tempPendingIntent);

        }

        public void stopAlarm() throws RemoteException{
            int i;
            for(i=0;i<alarmList.size();i++) {
                aManager.cancel(pendingIntentList.get(i));
            }
        }

        public void stopAlarmT() throws RemoteException{
            SampleApplication myApp = (SampleApplication)getApplicationContext();
            myApp.music.stop();
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return ACL;
    }
}
