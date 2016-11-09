package com.example.sam.drawerlayoutprac.Hotel;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import com.example.sam.drawerlayoutprac.R;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;

public class HotelGetImageTask extends AsyncTask<Object /*傳進來的參數*/, String/*進度條的顯示*/, Bitmap /*最後輸出的結果*/> {
    private String TAG = "HotelGetImageTask";
    private String ACTION = "getImage";
    private WeakReference<ImageView> imageViewWeakReference;
    float aFloat;

    HotelGetImageTask(ImageView imageView){
        this.imageViewWeakReference = new WeakReference<>(imageView);
    }

    @Override
    protected Bitmap doInBackground(Object... params) {
        String url = params[0].toString();
        String id = params[1].toString();
        int imageSize = Integer.parseInt(params[2].toString());
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("action", ACTION);
        jsonObject.addProperty("id", id);
        jsonObject.addProperty("imageSize", imageSize);

        Bitmap bitmap;
        try{
            bitmap = getRemoteImage(url, jsonObject.toString());  // 拿到回傳的Bitmap
        }catch(IOException e){
            Log.e(TAG, e.toString());
            return null;
        }

        return bitmap;
    }



    @Override
    protected void onPostExecute(Bitmap bitmap) {
        aFloat = (float) 0.5;
        if (isCancelled()) {
            bitmap = null;
        }
        ImageView imageView = imageViewWeakReference.get();
        if (imageView != null) {
            if (bitmap != null) {
                imageView.setImageBitmap(bitmap);
                imageView.setAlpha(aFloat);  // 設定圖片透明度 (float)
            } else {
                imageView.setImageResource(R.drawable.search);
            }

        }
        super.onPostExecute(bitmap);
    }

    private Bitmap getRemoteImage(String url, String jsonOut) throws IOException {
        Bitmap bitmap = null;
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setDoInput(true); // allow inputs
        connection.setDoOutput(true); // allow outputs
        connection.setUseCaches(false); // do not use a cached copy
        connection.setRequestMethod("POST");
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream()));
        //getOutputStream 建立 Request物件
        bw.write(jsonOut);
        Log.d(TAG, "jsonOut: " + jsonOut);
        bw.close();

        int responseCode = connection.getResponseCode();

        if (responseCode == 200) {
            bitmap = BitmapFactory.decodeStream(connection.getInputStream()); ////取得回來的 Response物件 轉成 Bitmap  然後回傳
        } else {
            Log.d(TAG, "response code: " + responseCode);
        }
        connection.disconnect();
        return bitmap;
    }
}
