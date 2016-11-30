package com.example.sam.drawerlayoutprac.Partner;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ListView;

import com.example.sam.drawerlayoutprac.Common;
import com.example.sam.drawerlayoutprac.Hotel.HotelGetLowestPriceVO;
import com.example.sam.drawerlayoutprac.Hotel.HotelSearchVO;
import com.example.sam.drawerlayoutprac.Partner.VO.MemCoordVO;
import com.example.sam.drawerlayoutprac.R;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by cuser on 2016/11/4.
 */

public class MemCoordGetAllTextTask extends AsyncTask<Object, Void, List<MemCoordVO>> {
    private final static String ACTION = "uploadCoord";
    private final static String TAG = "MemCoordGetAllTextTask";
    @Override
    protected List<MemCoordVO> doInBackground(Object... params) {
        List<MemCoordVO> dataList = new ArrayList<>();
        MemCoordVO memCoordVO = (MemCoordVO)params[0];
        StringBuilder url_sb = new StringBuilder();
        url_sb.append(Common.URL + "/android/live2/memCoord.do")
                .append("?action=" + ACTION)
                .append("&memId=" + memCoordVO.getMemId())
                .append("&memLat=" + memCoordVO.getMemLat())
                .append("&memLng=" + memCoordVO.getMemLng());
        Log.d(TAG,"url_sb - " + url_sb.toString());
        String jsonIn = null;
        JsonObject jsonObject = new JsonObject();
        try{
            jsonIn = getRemoteData(url_sb.toString(), jsonObject.toString());
        }catch (IOException e){
            Log.e(TAG, e.toString());
            return null;
        }
        Log.d(TAG,"jsonIn:" + jsonIn);

        JSONArray jsonArray = null;
        try {
            jsonArray = new JSONArray(jsonIn);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject tmpJsonObject = null;
            MemCoordVO myVO = new MemCoordVO();
            try {
                tmpJsonObject = jsonArray.getJSONObject(i);

                // MemVO attributes:
                myVO.setMemId(tmpJsonObject.get("memId").toString());
                myVO.setMemName(tmpJsonObject.get("memName").toString());
                myVO.setMemIntro(tmpJsonObject.get("memIntro").toString());
                myVO.setMemGender(tmpJsonObject.get("memGender").toString()); // f, m

                // MemCoordVO attributes:
                myVO.setMemLat(Double.parseDouble(tmpJsonObject.get("memLat").toString()));
                myVO.setMemLng(Double.parseDouble(tmpJsonObject.get("memLng").toString()));
                myVO.setMemDis(Double.parseDouble(tmpJsonObject.get("memDis").toString()));

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