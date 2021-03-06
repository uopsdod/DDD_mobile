package com.example.sam.drawerlayoutprac.Member;


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

public class MemUpdateTask extends AsyncTask<Object, Void, Void> {
    private final String TAG = "MemUpdateTask";

    @Override
    protected Void doInBackground(Object... params) {
        String url = params[0].toString();
        String action = params[1].toString();
        MemVO memVO = (MemVO) params[2];
        String result;
        // 從資料庫拉出來的Date要經過轉型，若沒轉型就算有資料也無法辨識
        //server那邊轉，這裡也要轉
        Gson gson = new GsonBuilder().setDateFormat("yyyy-mm-dd").create();

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("action", action);
        jsonObject.addProperty("memVO", gson.toJson(memVO));
//        if(params[3] != null){
//            String imageBase64 = params[3].toString();
//            jsonObject.addProperty("imageBase64", imageBase64);
//        }
        try{
            getRemoteData(url, jsonObject.toString());
        }catch (IOException e){
            Log.e(TAG, e.toString());
            return null;
        }
       return null;
    }

    private String getRemoteData(String url, String jsonOut) throws IOException {
        StringBuilder sb = new StringBuilder();
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setDoInput(true); // allow inputs
        connection.setDoOutput(true); // allow outputs
        connection.setUseCaches(false); // do not use a cached copy
        connection.setRequestMethod("POST");
        connection.setRequestProperty("charset", "UTF-8");
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream()));
        bw.write(jsonOut);
        Log.d(TAG, "jsonOut: " + jsonOut);
        bw.close();

        int responseCode = connection.getResponseCode();

        if (responseCode == 200) {
            BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
        } else {
            Log.d(TAG, "response code: " + responseCode);
        }
        connection.disconnect();
        Log.d(TAG, "jsonIn: " + sb);
        return sb.toString();
    }
}
