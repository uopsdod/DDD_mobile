package com.example.sam.drawerlayoutprac.Partner;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import com.example.sam.drawerlayoutprac.R;
import com.google.gson.JsonObject;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;


/**
 * Created by cuser on 2016/11/3.
 */
public class PartnerGetOneImageTask extends AsyncTask<Object, Integer, Bitmap> {
    private final static String TAG = "PartnerGetOneImageTask";
    private final static String ACTION = "getImage";
    // 使用WeakReference，讓圖片只要滿足一項條件，就釋放記憶體。ex. 參考=null
    private final WeakReference<ImageView> imageViewWeakReference;
    public PartnerGetOneImageTask(ImageView imageView) {
        this.imageViewWeakReference = new WeakReference<>(imageView);
    }
    public PartnerGetOneImageTask() {
        this.imageViewWeakReference = null;
    }

    @Override
    protected Bitmap doInBackground(Object... params) {
        String url = params[0].toString();
        int memId = Integer.parseInt(params[1].toString());
        int imageSize = Integer.parseInt(params[2].toString());
        // 設定請求參數
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("action", ACTION);
        jsonObject.addProperty("memId", memId);
        jsonObject.addProperty("imageSize", imageSize);
        // end of 設定請求參數

        Bitmap bitmap;
        try {
            bitmap = getRemoteImage(url, jsonObject.toString());
        } catch (IOException e) {
            Log.e(TAG, e.toString());
            return null;
        }
        return bitmap;
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        if (isCancelled()) {
            bitmap = null;
        }
        ImageView imageView = null;
        if (imageViewWeakReference != null){
            imageView = imageViewWeakReference.get();
        }
        if (imageView != null) {
            if (bitmap != null) {
                imageView.setImageBitmap(bitmap);
            } else {
                imageView.setImageResource(R.drawable.mem10000001);
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
        bw.write(jsonOut);
        //Log.d(TAG, "jsonOut: " + jsonOut);
        bw.close();

        int responseCode = connection.getResponseCode();

        if (responseCode == 200) {
            bitmap = BitmapFactory.decodeStream(connection.getInputStream());
        } else {
            Log.d(TAG, "response code: " + responseCode);
        }
        connection.disconnect();
        return bitmap;
    }
}