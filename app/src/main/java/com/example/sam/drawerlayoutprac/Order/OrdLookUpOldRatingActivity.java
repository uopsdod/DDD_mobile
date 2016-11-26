package com.example.sam.drawerlayoutprac.Order;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;

import com.example.sam.drawerlayoutprac.Common;
import com.example.sam.drawerlayoutprac.R;
import com.example.sam.drawerlayoutprac.Util;
import com.google.gson.JsonObject;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;

import static android.view.Window.FEATURE_NO_TITLE;
import static com.example.sam.drawerlayoutprac.Partner.MyFirebaseMessagingService.TAG;

public class OrdLookUpOldRatingActivity extends Activity {
    String ratingStarNo = "0";
    String comment = "";
    String ordId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(FEATURE_NO_TITLE);
        setContentView(R.layout.ord_lookup_rating);
        RatingBar ratingBar = (RatingBar) findViewById(R.id.ratingBar);
        final EditText editText = (EditText) findViewById(R.id.et_comment);
        Button btn_confirm = (Button) findViewById(R.id.btn_confirm);
        Button btn_cancel = (Button) findViewById(R.id.btn_cancel);
        ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                //Util.showToast(getApplicationContext(),""+rating);
                if ((rating == Math.floor(rating)) && !Double.isInfinite(rating)) {
                    ratingBar.setRating(rating);
                    ratingStarNo = Float.toString(rating);
                }else{
                    ratingBar.setRating((float)Math.floor(rating));
                    ratingStarNo = Double.toString(Math.floor(rating));
                }
            }
        });

        btn_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = getIntent().getExtras();
                ordId = (String)bundle.get("ordId");
                comment = editText.getText().toString();
                Log.d("OrdLookUpOldRating: ", ordId + " - " + ratingStarNo + " - " + comment);
                uploadDataToServer(ordId,ratingStarNo,comment);
                finish();
            }
        });

        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OrderLookUpFragment.afterRatingCanceled = true;
                finish();
            }
        });



    }

    private void uploadDataToServer(final String aOrdId, final String aRatingStarNo, final String aOrdRatingContent){
        Thread myThread = new Thread(new Runnable() {
            @Override
            public void run() {
                JsonObject jsonObject = new JsonObject();
                String url = Common.URL + "/android/ord/ord.do";
                String action = "updateRating";
                jsonObject.addProperty("action", action);
                jsonObject.addProperty("ordId", aOrdId);
                jsonObject.addProperty("ordRatingStarNo", aRatingStarNo);
                jsonObject.addProperty("ordRatingContent", aOrdRatingContent);

                HttpURLConnection connection = null;
                try {
                    connection = (HttpURLConnection) new URL(url).openConnection();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                connection.setDoInput(true); // allow inputs
                connection.setDoOutput(true); // allow outputs
                connection.setUseCaches(false); // do not use a cached copy
                try {
                    connection.setRequestMethod("POST");
                } catch (ProtocolException e) {
                    e.printStackTrace();
                }
                BufferedWriter bw = null;
                try {
                    bw = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                //getOutputStream 建立 Request物件
                try {
                    bw.write(jsonObject.toString());
                    Log.d(TAG, "jsonOut: " + jsonObject.toString());
                } catch (IOException e) {
                    e.printStackTrace();
                }

                try {
                    bw.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                int responseCode = 0;
                try {
                    responseCode = connection.getResponseCode();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Log.d("OrdLookUpOldRating","responseCode: " + responseCode);


                connection.disconnect();
            }
        });
        myThread.start();
        try {
            // 重要: 要交上去，才能保證資料已經存入資料庫，之後讀取時才能保障是最新的
            myThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
