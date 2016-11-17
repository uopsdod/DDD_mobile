package com.example.sam.drawerlayoutprac;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by cuser on 2016/10/29.
 */

public class Common {
    //   Android官方模擬器連結本機web server可以直接使用 http://10.0.2.2
    //public static String URL = "http://10.0.2.2:8081/TextToJson_Web/SearchServlet";
    //public final static String URL = "http://10.0.2.2:8081/TextToJson_Web/Partner";
    public final static String URL = "http://10.0.2.2:8081/DDD_web";
    public static String URL_Partner = Common.URL + "/android/live2/partner.do";
    public static String URL_PartnerMsgController = Common.URL + "/android/live2/PartnerMsgController";


    public final static String PREF_FILE = "preference";

    public static boolean networkConnected(Activity activity) {
        ConnectivityManager conManager =
                (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = conManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }
}
