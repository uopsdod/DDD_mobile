package com.example.sam.drawerlayoutprac.Partner;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ListView;

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

public class PartnerGetTextTask extends AsyncTask<String, Void, List<MemVO>> {
    private final static String TAG = "SearchActivity";
    private Context context;
    private ListView listView;
    private ProgressDialog progressDialog;

    public PartnerGetTextTask(Context context, ListView listView){
        this.context = context;
        this.listView = listView;
    }


    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        progressDialog = new ProgressDialog(this.context);
        progressDialog.setMessage("Loading...");
        progressDialog.show();
    }

    @Override
    protected List<MemVO> doInBackground(String... params) {
        String url = params[0]; // 傳入的Common.URL字串
        String jsonIn;
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("action", "getAll"); // 在這邊控制請求參數
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
        Type listType = new TypeToken<List<MemVO>>() {
        }.getType();
        // end of // 處理Oracle Date型態與gson之間的格式問題

        // 回傳至onPostExecute(List<MemVO> items) - 備註:此為 UI main thread在呼叫的
        return gson.fromJson(jsonIn, listType);
    }

    @Override
    protected void onPostExecute(List<MemVO> items) {
        Map<String, Object> profileMap;
        List<Map<String, Object>> profilesList = new ArrayList<>();

        for (int i = 0; i < items.size(); i++) {
            MemVO myVO = items.get(i);

            profileMap = new HashMap<>();

            //放入文字資料
            profileMap.put(PartnerListAdapter.KEY_MEMID, myVO.getMemId());
            profileMap.put(PartnerListAdapter.KEY_NAME, myVO.getMemName());
            profileMap.put(PartnerListAdapter.KEY_DESCRIPTION_SHORT, myVO.getMemIntro());
            profileMap.put(PartnerListAdapter.KEY_DESCRIPTION_FULL, myVO.getMemIntro());
            profilesList.add(profileMap);
        }
        // 放入ListView的Adapter
        listView.setAdapter(new PartnerListAdapter(this.context, R.layout.list_item, profilesList));

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
        Log.d(TAG, "jsonOut: " + jsonOut);
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