package com.example.sam.drawerlayoutprac;

import android.os.AsyncTask;
import android.util.Log;

import com.example.sam.drawerlayoutprac.Hotel.HotelVO;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by cuser on 2016/11/8.
 */

public class HotelMemCheckTask extends AsyncTask<Object, Void, HotelVO> {
    private String TAG = "MemCheckTask";
    private String ACTION = "hotelMemCheck";

    @Override
    protected HotelVO doInBackground(Object... params) {
        String url = params[0].toString();
        String userName = params[1].toString();
        String password = params[2].toString();
        String jsonIn;
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("action", ACTION);
        jsonObject.addProperty("userName", userName);
        jsonObject.addProperty("password", password);
        try{
            jsonIn = getRemoteData(url, jsonObject.toString());
            Log.d(TAG, "jsonIn : " + jsonIn);
        }catch(IOException e){
            Log.e(TAG, e.toString());
            return null;
        }
        Gson gson = new Gson();
        return gson.fromJson(jsonIn, HotelVO.class);
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
        bw.write(jsonOut);
        Log.d(TAG, "jsonOut: " + jsonOut);
        bw.close();

        int responseCode = connection.getResponseCode();

        if (responseCode == 200) {
            BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line;
            while ((line = br.readLine()) != null) {
                jsonIn.append(line);
            }
        } else {
            Log.d(TAG, "response code: " + responseCode);
        }
        connection.disconnect();
        Log.d(TAG, "jsonIn: " + jsonIn);
        return jsonIn.toString();
    }
}
