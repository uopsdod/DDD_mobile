package com.example.sam.drawerlayoutprac.Room;


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

public class RoomGetImageTask extends AsyncTask<Object, String, Bitmap>{
    private String TAG = "RoomGetImageTask";
    private String ACTION = "getImage";
    private WeakReference<ImageView> imageViewWeakReference;

    public RoomGetImageTask(ImageView imageView){
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
            bitmap = getRemoteImage(url, jsonObject.toString());
        }catch(IOException e){
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
        ImageView imageView = imageViewWeakReference.get();
        if (imageView != null) {
            if (bitmap != null) {
                imageView.setImageBitmap(bitmap);
            } else {
                imageView.setImageResource(R.drawable.search);
            }
        }
        super.onPostExecute(bitmap);
    }

    private Bitmap getRemoteImage(String url, String jsonOut) throws IOException {
        Bitmap bitmap = null;
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setDoInput(true);
        connection.setDoOutput(true);
        connection.setUseCaches(false);
        connection.setRequestMethod("POST");
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream()));
        bw.write(jsonOut);
        Log.d(TAG, "jsonOut: " + jsonOut);
        bw.close();

        int responseCode = connection.getResponseCode();
        if(responseCode == 200){
            bitmap = BitmapFactory.decodeStream(connection.getInputStream());
        }else{
            Log.d(TAG, "response code: " + responseCode);
        }
        connection.disconnect();
        return bitmap;
    }
}
