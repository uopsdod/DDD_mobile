package com.example.sam.drawerlayoutprac.Order;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import com.example.sam.drawerlayoutprac.Common;
import com.example.sam.drawerlayoutprac.Partner.VO.MemVO;
import com.example.sam.drawerlayoutprac.Partner.VO.OrdVO;
import com.example.sam.drawerlayoutprac.R;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;

public class OrderGetOneTextTask extends AsyncTask<Object /*傳進來的參數*/, String/*進度條的顯示*/, OrdVO /*最後輸出的結果*/> {
    private String TAG = "OrderGetOneTextTask";
    private String ACTION = "getOneText";

    @Override
    public OrdVO doInBackground(Object... params) {
        String url = Common.URL + "/android/ord/ord.do";
        String ordId = params[0].toString();

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("action", ACTION);
        jsonObject.addProperty("ordId", ordId);
        Log.d(TAG, "ordId" + ordId);

        String jsonIn = null;

        try {
            jsonIn = getRemoteData(url, jsonObject.toString());
        } catch (IOException e) {
            Log.e(TAG, e.toString());
            return null;
        }
        Log.d("OrderGetOneTextTask", jsonIn);
        // 處理Oracle Date型態與gson之間的格式問題
        Gson gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd HH:mm:ss")
                .create();
        // end of // 處理Oracle Date型態與gson之間的格式問題

        // 回傳至onPostExecute(List<MemVO> items) - 備註:此為 UI main thread在呼叫的
        return gson.fromJson(jsonIn, OrdVO.class);

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
