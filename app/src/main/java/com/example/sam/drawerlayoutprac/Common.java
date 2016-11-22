package com.example.sam.drawerlayoutprac;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by cuser on 2016/10/29.
 */

public class Common {
    //Android官方模擬器連結本機web server可以直接使用 http://10.0.2.2
    //localhost版本:
    public final static String URL = "http://10.0.2.2:8081/DDD_web";
//    public final static String URL = "http://10.120.25.4:8081/DDD_web";
    public static String URL_Partner = Common.URL + "/android/live2/partner.do";
    public static String URL_PartnerMsgController = Common.URL + "/android/live2/PartnerMsgController";
    public static final String URL_Chatroom = "ws://10.0.2.2:8081/DDD_web/android/live2/MsgCenter";
    public final static String URL_DYNAMICPRICE = "ws://10.0.2.2:8081/DDD_web/MyEchoServer/myName01/1";

    // Amazon AWS版本:
//    public final static String URL = "http://ec2-54-186-77-188.us-west-2.compute.amazonaws.com:8081/DDD_web";
//    public static String URL_Partner = Common.URL + "/android/live2/partner.do";
//    public static String URL_PartnerMsgController = Common.URL + "/android/live2/PartnerMsgController";
//    public static final String URL_Chatroom = "ws://ec2-54-186-77-188.us-west-2.compute.amazonaws.com:8081/DDD_web/android/live2/MsgCenter";


    public final static String PREF_FILE = "preference";
    public final static String PREF_FILE_Hotel = "preference";

    public static boolean networkConnected(Activity activity) {
        ConnectivityManager conManager =
                (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = conManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }
}
