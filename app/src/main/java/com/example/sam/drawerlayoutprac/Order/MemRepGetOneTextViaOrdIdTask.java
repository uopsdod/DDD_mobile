package com.example.sam.drawerlayoutprac.Order;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ListView;

import com.example.sam.drawerlayoutprac.Common;
import com.example.sam.drawerlayoutprac.Partner.PartnerListAdapter;
import com.example.sam.drawerlayoutprac.Partner.VO.MemRepVO;
import com.example.sam.drawerlayoutprac.Partner.VO.MemVO;
import com.example.sam.drawerlayoutprac.R;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by cuser on 2016/11/4.
 */

public class MemRepGetOneTextViaOrdIdTask extends AsyncTask<String, Void, MemRepVO> {
    private final static String TAG = "MemRepGetOne";
    private Context context;

    public MemRepGetOneTextViaOrdIdTask(Context context){
        this.context = context;
    }


    @Override
    protected MemRepVO doInBackground(String... params) {
        String url = Common.URL + "/android/memRep/memRep.do";
        String ordId = params[0];
        String jsonIn = null;
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("action", "findByMemRepOrdId"); // 在這邊控制請求參數
        jsonObject.addProperty("ordId", ordId);

        try {
            jsonIn = getRemoteData(url, jsonObject.toString());
        } catch (IOException e) {
            Log.e(TAG, e.toString());
            return null;
        }

        // 處理Oracle Date型態與gson之間的格式問題
        Gson gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd HH:mm:ss")
                .create();
        //Type listType = new TypeToken<List<MemVO>>() { }.getType();
        // end of // 處理Oracle Date型態與gson之間的格式問題

        // 回傳至onPostExecute(List<MemVO> items) - 備註:此為 UI main thread在呼叫的
        try{
            // 這邊要注意傳過來的字串中，不能有圖片資料，如果有的話會傳失敗，而產生Exception
            MemRepVO memRepVO = gson.fromJson(jsonIn, MemRepVO.class);
            return memRepVO;
        }catch (Exception e){
            e.printStackTrace();
        }
        Log.d("MemRepGetOne", "got here");
        return null;
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
        Log.d(TAG, "jsonIn: " + jsonIn);
        return jsonIn.toString();
    }
}