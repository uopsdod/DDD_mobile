package com.example.sam.drawerlayoutprac.Order;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.example.sam.drawerlayoutprac.Common;
import com.example.sam.drawerlayoutprac.Partner.VO.OrdVO;
import com.example.sam.drawerlayoutprac.R;
import com.google.gson.JsonObject;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.util.concurrent.ExecutionException;

import static android.view.Window.FEATURE_NO_TITLE;
import static com.example.sam.drawerlayoutprac.Partner.MyFirebaseMessagingService.TAG;

/**
 * Created by cuser on 2016/11/27.
 */
public class OrdLookUpNowCancelActivity extends Activity {
    private String ordId;
    private OrdVO ordVO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(FEATURE_NO_TITLE);
        setContentView(R.layout.ord_lookup_cancel);
        Button btn_confirm = (Button) findViewById(R.id.btn_confirm);
        Button btn_cancel = (Button) findViewById(R.id.btn_cancel);


        Bundle bundle = getIntent().getExtras();
        this.ordId = (String)bundle.get("ordId");
        try {
            this.ordVO = new OrderGetOneTextTask().execute(this.ordId).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        btn_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("OrdLookUpNowCancel: ", ordId);
                String ordMsgNo = OrdLookUpNowCancelActivity.this.ordVO.getOrdMsgNo();
                uploadDataToServer(ordId,ordMsgNo);
                finish();
            }
        });

        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OrderLookUpFragment.skipOnResume = true;
                finish();
            }
        });

    }

    private void uploadDataToServer(final String aOrdId, final String aOrdMsgNo) {
        Thread myThread = new Thread(new Runnable() {
            @Override
            public void run() {
                JsonObject jsonObject = new JsonObject();
                String url = Common.URL + "/android/ord/ord.do";
                String action = "cancel";
                jsonObject.addProperty("action", action);
                jsonObject.addProperty("ordId", aOrdId);
                jsonObject.addProperty("ordMsgNo", aOrdMsgNo);

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
                Log.d("OrdLookUpNowCancel","responseCode: " + responseCode);


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
