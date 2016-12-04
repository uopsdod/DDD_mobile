package com.example.sam.drawerlayoutprac.Partner;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.example.sam.drawerlayoutprac.Common;
import com.example.sam.drawerlayoutprac.MainActivity;
import com.example.sam.drawerlayoutprac.R;
import com.example.sam.drawerlayoutprac.Util;
import com.google.gson.JsonObject;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;

import static com.example.sam.drawerlayoutprac.Partner.MyFirebaseMessagingService.TAG;

public class TestFragment extends Fragment {
    View rootView;
    EditText memIdView;
    Button btnView;
    Button btn_AllSell;

    @Override
    public void onResume() {
        super.onResume();
        MainActivity.actionbar.setTitle("測試頁面");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        this.rootView = inflater.inflate(R.layout.fragment_blank, container, false);
        btn_AllSell = (Button)rootView.findViewById(R.id.btn_AllSell);
        btn_AllSell.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Util.showToast(getContext(),"AllSell clicked");
                allSell();
            }
        });

        return this.rootView;
    }



    public static void allSell(){
        Thread myThread = new Thread(new Runnable() {
            @Override
            public void run() {
                JsonObject jsonObject = new JsonObject();
                String url = Common.URL + "/android/AndroidForDevelop";
                String action = "AllSell";
                jsonObject.addProperty("action", action);

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
                Log.d("Common","responseCode: " + responseCode);


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
