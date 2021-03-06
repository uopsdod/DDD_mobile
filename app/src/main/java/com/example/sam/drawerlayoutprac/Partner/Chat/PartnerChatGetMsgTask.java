package com.example.sam.drawerlayoutprac.Partner.Chat;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import com.example.sam.drawerlayoutprac.Common;
import com.example.sam.drawerlayoutprac.Partner.VO.PartnerMsg;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

/**
 * Created by cuser on 2016/11/4.
 */
// BaseAdapter - notifyDataSetChanged();
public class PartnerChatGetMsgTask extends AsyncTask<String, Void, List<PartnerMsg>> {
    private final static String TAG = "PartnerChatGetMsgTask";
    private Context context;
    private ProgressDialog progressDialog;
    private String toMemId;

    public PartnerChatGetMsgTask(Context aContext, String aToMemId){
        this.context = aContext;
        this.toMemId = aToMemId;
    }


    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        progressDialog = new ProgressDialog(this.context);
        progressDialog.setMessage("Loading...");
        progressDialog.show();
    }

    @Override
    protected List<PartnerMsg> doInBackground(String... params) {
        String url = params[0]; // 傳入的Common.URL字串
        SharedPreferences preferences_r = this.context.getSharedPreferences(Common.PREF_FILE,this.context.MODE_PRIVATE);
        String memid = preferences_r.getString("memId", null);
        String jsonIn;
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("action", "getAll"); // 在這邊控制請求參數
        jsonObject.addProperty("memId", memid); // 在這邊控制請求參數
        jsonObject.addProperty("toMemId", this.toMemId); // 在這邊控制請求參數
        try {
            jsonIn = getRemoteData(url, jsonObject.toString());
        } catch (IOException e) {
            Log.e(TAG, "fcm - " + e.toString());
            return null;
        }

        // 處理Oracle Timestamp型態與gson之間的格式問題
        Gson gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd hh:mm:ss.S")
                .create();
        // end of // 處理Oracle Timestamp型態與gson之間的格式問題
        // 請server那邊把MemCharVO都處理好成PartnerMsg之後，再傳過來
        Type listType = new TypeToken<List<PartnerMsg>>() { }.getType();


        // 回傳至onPostExecute(List<MemVO> items) - 備註:此為 UI main thread在呼叫的
        return gson.fromJson(jsonIn, listType);
    }

    @Override
    protected void onPostExecute(List<PartnerMsg> items) {
        progressDialog.cancel();
    }

    private String getRemoteData(String url, String jsonOut) throws IOException {
        StringBuilder jsonIn = new StringBuilder();
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setDoInput(true); // allow inputs
        connection.setDoOutput(true); // allow outputs
        connection.setUseCaches(false); // do not use a cached copy
        connection.setRequestMethod("POST");
        connection.setRequestProperty("charset", "UTF-8");
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream()));
        bw.write(jsonOut); // 塞入請求參數
        //Log.d(TAG, "jsonOut: " + jsonOut);
        bw.close(); // 送出request

        int responseCode = connection.getResponseCode();
        // 確認能與server建立Socket連線
        if (responseCode == 200) {
            // connection.getInputStream() - 等待server回應，如果還沒有回應就hold在這邊
            BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line;
            while ((line = br.readLine()) != null) {
                jsonIn.append(line); // 重點:把server的資料拿到手
            }
        } else {
            Log.d(TAG, "response code: " + responseCode);
        }
        connection.disconnect();
        //(TAG, "jsonIn: " + jsonIn);
        return jsonIn.toString();
    }
}