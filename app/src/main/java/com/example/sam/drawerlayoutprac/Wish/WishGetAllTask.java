package com.example.sam.drawerlayoutprac.Wish;

import android.os.AsyncTask;
import android.util.Log;

import com.example.sam.drawerlayoutprac.Room.RoomVO;
import com.google.gson.Gson;
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

public class WishGetAllTask extends AsyncTask<Object, String, List<RoomVO>>{
    private String TAG = "WishGetAllTask";
    private String ACTION = "getAll";
    @Override
    protected List<RoomVO> doInBackground(Object... params) {
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

        Gson gson = new Gson();
        Type listType = new TypeToken<List<RoomVO>>(){} .getType();//因要給.class檔，但因泛型關係所以用Type轉型成RoomVO.class檔
        return gson.fromJson(jsonIn, listType);
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
