package com.example.sam.drawerlayoutprac.Hotel;

import android.os.AsyncTask;
import android.util.Log;

import com.example.sam.drawerlayoutprac.Common;
import com.example.sam.drawerlayoutprac.CommonFragment;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by cuser on 2016/11/21.
 */

public class HotelGetLowestPriceTask extends AsyncTask<Object, Integer, List<HotelGetLowestPriceVO>> {
    private final static String ACTION = "search";
    @Override
    protected List<HotelGetLowestPriceVO> doInBackground(Object... params) {
        List<HotelGetLowestPriceVO> dataList = new ArrayList<>();
        //String url = params[0].toString();
        String url = Common.URL + "/HotelRoomSearch";
        String jsonIn = null;
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("fromMobile","fromMobile");
        jsonObject.addProperty("action","search");

        jsonObject.addProperty("city","桃園市");
        jsonObject.addProperty("zone","中壢區");
        jsonObject.addProperty("hotelRatingResult","0");
        jsonObject.addProperty("roomCapacity","2");
        jsonObject.addProperty("Price","$1 - $10000");



        try{
            jsonIn = getRemoteData(url, jsonObject.toString());
        }catch (IOException e){
            Log.e("HotelGetLowestPriceTask", e.toString());
            return null;
        }
        Log.d("HotelGetLowestPriceTask","jsonIn:" + jsonIn);

        JSONArray jsonArray = null;
        try {
            jsonArray = new JSONArray(jsonIn);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject tmpJsonObject = null;
            HotelGetLowestPriceVO myVO = new HotelGetLowestPriceVO();
            try {
                tmpJsonObject = jsonArray.getJSONObject(i);
                myVO.setHotelId(tmpJsonObject.get("hotelId").toString());
                myVO.setHotelLat(tmpJsonObject.getString("hotelLat"));
                myVO.setHotelLon(tmpJsonObject.getString("hotelLon"));
                myVO.setHotelCheapestRoomId(tmpJsonObject.getString("roomBottomId"));
                myVO.setHotelCheapestRoomPrice(tmpJsonObject.getString("bottomPrice"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            dataList.add(myVO);
        }

//        Gson gson = new Gson();
//        Type listType = new TypeToken<List<HotelGetLowestPriceVO>>(){} .getType();
//        return gson.fromJson(jsonIn, listType);
        return dataList;
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
        Log.d("HotelGetLowestPriceTask", "jsonOut:" + jsonOut);
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
            Log.d("HotelGetLowestPriceTask","reponse code: " + responseCode);
        }
        connection.disconnect();
        //Log.d("HotelGetLowestPriceTask", "jsonIn:" + jsonIn);
        return jsonIn.toString();
    }

}
