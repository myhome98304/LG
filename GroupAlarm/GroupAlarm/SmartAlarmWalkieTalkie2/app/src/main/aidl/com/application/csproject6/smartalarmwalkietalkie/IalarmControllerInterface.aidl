// IalarmControllerInterface.aidl
package com.application.csproject6.smartalarmwalkietalkie;

// Declare any non-default types here with import statements

interface IalarmControllerInterface {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    void addAlarm();
    void addAlarmT(String aN,int aY,int aMo,int aD,int aH,int aMi,String groupId);
    void stopAlarm();
    void stopAlarmT();
    void clearAlarm();
    }
