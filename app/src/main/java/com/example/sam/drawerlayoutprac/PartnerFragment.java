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

    private static final int CIRCLE_RADIUS_DP = 50;
    private final static String TAG = "SearchActivity";

    private ProgressDialog progressDialog;
    AsyncTask retrievePartnerTask;
    ListView listview;
    public static int sScreenWidth; // 將大頭照變成圓的
    public static int sProfileImageHeight; // 將大頭照變成圓的
    public static ShapeDrawable sOverlayShape; // 將大頭照變成圓的

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup, Bundle bundle) {
        super.onCreateView(inflater, viewGroup, bundle);
        View view = inflater.inflate(R.layout.fragment_partner, viewGroup, false);
        listview = (ListView) view.findViewById(R.id.list_partner);
        // 把大頭貼變成圓的
        sScreenWidth = getResources().getDisplayMetrics().widthPixels;
        sProfileImageHeight = getResources().getDimensionPixelSize(R.dimen.height_profile_image);
        sOverlayShape = buildAvatarCircleOverlay();

        // get data
        if (networkConnected()) {
            // send request to server and get the response - 重點在new這個動作
            retrievePartnerTask = new RetrievePartnerTask().execute(Common.URL);
        } else {
            Util.showToast(getActivity(), "no network");
        }
        // end of get data

        // set up floatingBtn click Listener
        LinearLayout ll_view = (LinearLayout)viewGroup.getParent();
        CoordinatorLayout cdl_view = (CoordinatorLayout)ll_view.getParent();
        FloatingActionButton floatingBtn = (FloatingActionButton)cdl_view.findViewById(R.id.floatingBtn);

        floatingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showToast(getContext(),"ftBtn clicked");
                Fragment fragment = new PartnerMapFragment();
                Util.switchFragment(PartnerFragment.this,fragment);
            }
        });

        return view;
    }

    class RetrievePartnerTask extends AsyncTask<String, Void, List<PartnerVO>> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(PartnerFragment.this.getActivity());
            progressDialog.setMessage("Loading...");
            progressDialog.show();
        }

        @Override
        protected List<PartnerVO> doInBackground(String... params) {
            String url = params[0]; // 傳入的Common.URL字串
            String jsonIn;
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("param", "partner"); // 在這邊控制請求參數
            try {
                jsonIn = getRemoteData(url, jsonObject.toString());
            } catch (IOException e) {
                Log.e(TAG, e.toString());
                return null;
            }

            Gson gson = new Gson();
            Type listType = new TypeToken<List<PartnerVO>>() {}.getType();

            return gson.fromJson(jsonIn, listType);
        }

        @Override
        protected void onPostExecute(List<PartnerVO> items) {
            Map<String, Object> profileMap;
            List<Map<String, Object>> profilesList = new ArrayList<>();

            int[] avatars = getProfilePics();// 在還沒能從server拿到圖片前，先這樣擋著

                for (int i = 0; i < items.size(); i++) {
                    PartnerVO myVO = items.get(i);
                    profileMap = new HashMap<>();
                    profileMap.put(PartnerList.KEY_AVATAR, avatars[i]); // 這個目前是假資料
                    profileMap.put(PartnerList.KEY_NAME, myVO.getName());
                    profileMap.put(PartnerList.KEY_DESCRIPTION_SHORT, myVO.getIntroShort());
                    profileMap.put(PartnerList.KEY_DESCRIPTION_FULL, myVO.getIntroLong());
                    profilesList.add(profileMap);
                }

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


        private int[] getProfilePics() {
            int[] avatars = {
                    R.drawable.anastasia,
                    R.drawable.andriy,
                    R.drawable.dmitriy,
                    R.drawable.dmitry_96,
                    R.drawable.ed,
                    R.drawable.illya,
                    R.drawable.kirill,
                    R.drawable.konstantin,
                    R.drawable.oleksii,
                    R.drawable.pavel,
                    R.drawable.vadim};
            return avatars;
        }
    }

    // check if the device connect to the network
    private boolean networkConnected() {
        ConnectivityManager conManager =
                (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = conManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

    private ShapeDrawable buildAvatarCircleOverlay() {
        int radius = 666;
        ShapeDrawable overlay = new ShapeDrawable(new RoundRectShape(null,
                new RectF(
                        sScreenWidth / 2 - dpToPx(getCircleRadiusDp() * 2),
                        sProfileImageHeight / 2 - dpToPx(getCircleRadiusDp() * 2),
                        sScreenWidth / 2 - dpToPx(getCircleRadiusDp() * 2),
                        sProfileImageHeight / 2 - dpToPx(getCircleRadiusDp() * 2)),
                new float[]{radius, radius, radius, radius, radius, radius, radius, radius}));
        overlay.getPaint().setColor(getResources().getColor(com.yalantis.euclid.library.R.color.gray));

        return overlay;
    }

    public int dpToPx(int dp) {
        return Math.round((float) dp * getResources().getDisplayMetrics().density);
    }
    protected int getCircleRadiusDp() {
        return CIRCLE_RADIUS_DP;
    }



    private void showToast(Context context, String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }
}
