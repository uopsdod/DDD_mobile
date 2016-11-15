package com.example.sam.drawerlayoutprac.Partner;

import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by cuser on 2016/11/4.
 */

public class PartnerGetOneTextTask extends AsyncTask<String, Void, MemVO> {
    private final static String TAG = "SearchActivity";

    public PartnerGetOneTextTask(){
    }


    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected MemVO doInBackground(String... params) {
        String url = params[0]; // 傳入的Common.URL字串
        String toMemID = params[1];
        String jsonIn;
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("action", "getOneText"); // 在這邊控制請求參數
        jsonObject.addProperty("toMemId", toMemID);
        try {
            jsonIn = getRemoteData(url, jsonObject.toString());
        } catch (IOException e) {
            Log.e(TAG, e.toString());
            return null;
        }
        Log.d("PartnerGetOneMemVOTask","Websocket" + jsonIn);
        // 處理Oracle Date型態與gson之間的格式問題
        Gson gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd HH:mm:ss")
                .create();
        // end of // 處理Oracle Date型態與gson之間的格式問題

        // 回傳至onPostExecute(List<MemVO> items) - 備註:此為 UI main thread在呼叫的
        return gson.fromJson(jsonIn, MemVO.class);
    }

    @Override
    protected void onPostExecute(MemVO items) {
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