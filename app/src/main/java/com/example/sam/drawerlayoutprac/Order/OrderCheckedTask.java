package com.example.sam.drawerlayoutprac.Order;


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

public class OrderCheckedTask extends AsyncTask<Object, Integer, Boolean>{
    private final static String TAG = "OrderCheckedTask";
    private final static String ACTION = "checked";
    @Override
    protected Boolean doInBackground(Object... params) {
        String url = params[0].toString();
        String ordId = params[1].toString();
        String key = params[2].toString();
        String jsonIn;
        boolean check;
        JsonObject jsonObject = new JsonObject();

        jsonObject.addProperty("action",ACTION);
        jsonObject.addProperty("ordId", ordId);
        jsonObject.addProperty("key", key);
        try{
            jsonIn = getRemoteData(url, jsonObject.toString());
            if(jsonIn.equals("true")){
                check = true;
            }else{
                check = false;
            }
        }catch (IOException e){
            Log.e(TAG, e.toString());
            return null;
        }

        return check;
    }

    private String getRemoteData(String url, String jsonOut) throws IOException {
        StringBuilder jsonIn = new StringBuilder();
        HttpURLConnection connection = (HttpURLConnection) (new URL(url).openConnection());
        connection.setDoInput(true);
        connection.setDoOutput(true);
        connection.setUseCaches(false);
        connection.setRequestMethod("POST");
        connection.setRequestProperty("charset","UTF-8");
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream()));
        //getOutputStream 建立 Request物件
        bw.write(jsonOut);
        Log.d(TAG, "jsonOut:" + jsonOut);
        bw.close(); //送出請求
        int responseCode = connection.getResponseCode();

        if(responseCode == 200){
            BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            //取得回來的 Response物件
            String line;
            while((line = br.readLine()) != null){
                jsonIn.append(line);
            }
        }else{
            Log.d(TAG,"reponse code: " + responseCode);
        }
        connection.disconnect();
        Log.d(TAG, "jsonIn:" + jsonIn);
        return jsonIn.toString();
    }
}
