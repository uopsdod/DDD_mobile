package com.example.sam.drawerlayoutprac;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.RectF;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import com.yalantis.euclid.library.EuclidListAdapter;

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
 * Created by cuser on 2016/10/9.
 */
public class PartnerFragment extends Fragment {

    private final static String TAG = "SearchActivity";

    private ProgressDialog progressDialog;
    AsyncTask retrievePartnerTask;
    ListView listview;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup, Bundle bundle) {
        super.onCreateView(inflater, viewGroup, bundle);
        View view = inflater.inflate(R.layout.fragment_partner, viewGroup, false);
        listview = (ListView) view.findViewById(R.id.list_partner);


        // get data
            // 檢查使用者是否有連線功能
        if (Common.networkConnected(getActivity())) {
            // send request to server and get the response - 重點在new這個動作
            String url = Common.URL + "/live2/Partner";
            Log.d("url", url);
            retrievePartnerTask = new RetrievePartnerTask().execute(url);
        } else {
            Util.showToast(getActivity(), "no network");
        }
        // end of get data

        // set up floatingBtn click Listener
        LinearLayout ll_view = (LinearLayout) viewGroup.getParent();
        CoordinatorLayout cdl_view = (CoordinatorLayout) ll_view.getParent();
        FloatingActionButton floatingBtn = (FloatingActionButton) cdl_view.findViewById(R.id.floatingBtn);

        floatingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Util.showToast(getContext(), "ftBtn clicked");
                Fragment fragment = new PartnerMapFragment();
                Util.switchFragment(PartnerFragment.this, fragment);
            }
        });

        return view;
    }

    class RetrievePartnerTask extends AsyncTask<String, Void, List<MemVO>> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(PartnerFragment.this.getActivity());
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
                profileMap.put(PartnerList.KEY_MEMID, myVO.getMemId());
                profileMap.put(PartnerList.KEY_NAME, myVO.getMemName());
                profileMap.put(PartnerList.KEY_DESCRIPTION_SHORT, myVO.getMemIntro());
                profileMap.put(PartnerList.KEY_DESCRIPTION_FULL, myVO.getMemIntro());
                profilesList.add(profileMap);
            }
            // 放入ListView的Adapter
            listview.setAdapter(new PartnerList(getContext(), R.layout.list_item, profilesList));

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
}
