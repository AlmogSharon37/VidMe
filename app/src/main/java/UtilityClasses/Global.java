package UtilityClasses;


import Networking.MediaThread;
import Networking.NetworkThread;

public class Global {

    public static int FIRST_TIME_CONNECT_SERVER = 1;
    public static NetworkThread networkThread;
    public static MediaThread mediaThread;


    public static int networkServerPort = 8820, mediaServerPort = 8821;
    public static String networkServerIp;



    public static void FIRST_TIME_HOME(){
        if(FIRST_TIME_CONNECT_SERVER == 0)
            FIRST_TIME_CONNECT_SERVER ++;
        else FIRST_TIME_CONNECT_SERVER = 0;
    }
}
