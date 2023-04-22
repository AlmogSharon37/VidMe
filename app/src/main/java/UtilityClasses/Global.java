package UtilityClasses;

import Networking.NetworkThread;

public class Global {

    public static int FIRST_TIME_CONNECT_SERVER = 1;
    public static NetworkThread networkThread;



    public static void FIRST_TIME_HOME(){
        if(FIRST_TIME_CONNECT_SERVER == 0)
            FIRST_TIME_CONNECT_SERVER ++;
        else FIRST_TIME_CONNECT_SERVER = 0;
    }
}
