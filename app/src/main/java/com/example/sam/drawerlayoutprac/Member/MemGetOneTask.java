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



public class MemGetOneTask extends AsyncTask<Object, Void, MemVO> {
    private String TAG = "MemGetOneTask";
    private String ACTION = "getOne";
    @Override
    protected MemVO doInBackground(Object... params) {
        String url = params[0].toString();
        String id = params[1].toString();
        String jsonIn;
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("action",ACTION);
        jsonObject.addProperty("id", id);
        try{
            jsonIn = getRemoteData(url, jsonObject.toString());
        }catch (IOException e){
            Log.e(TAG, e.toString());
            return null;
        }

        // 從資料庫拉出來的Date要經過轉型，若沒轉型就算有資料也無法辨識
        Gson gson = new GsonBuilder().setDateFormat("yyyy-mm-dd").create();
        return gson.fromJson(jsonIn, MemVO.class);
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
