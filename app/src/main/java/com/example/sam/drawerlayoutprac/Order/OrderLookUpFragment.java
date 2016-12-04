package com.example.sam.drawerlayoutprac.Order;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.example.sam.drawerlayoutprac.BuildConfig;
import com.example.sam.drawerlayoutprac.Common;
import com.example.sam.drawerlayoutprac.MainActivity;
import com.example.sam.drawerlayoutprac.MustLoginFragment;
import com.example.sam.drawerlayoutprac.Partner.VO.OrdVO;
import com.example.sam.drawerlayoutprac.R;
import com.example.sam.drawerlayoutprac.Util;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Created by cuser on 2016/11/23.
 */

public class OrderLookUpFragment extends MustLoginFragment {
    private static final String TAG = "OrderLookUpFragment";
    public static boolean ordNowPressed = true;
    public static boolean ordOldPressed = false;
    List<OrdVO> myOrdList;
    RecyclerView myRvOrd;
    Button btnOrdNow;
    Button btnOrdOld;
    RecyclerView.Adapter<OrderLookUpNowAdapter.MyViewHolder> myAdapter_now;
    RecyclerView.Adapter<OrderLookUpOldAdapter.MyViewHolder> myAdapter_old;
    static HashMap<String, String> ordStatusConverter = new HashMap<>();

    public static boolean skipOnResume = false;
    // 使用firebase remote config - 01
    private FirebaseRemoteConfig mFirebaseRemoteConfig;
    public static long ordDuration; // millesecond

    public static boolean isFreshNeeded = false;

    static {
        ordStatusConverter.put("0", "已下單");
        ordStatusConverter.put("1", "主動取消");
        ordStatusConverter.put("2", "已入住");
        ordStatusConverter.put("3", "已繳費");
        ordStatusConverter.put("4", "逾時取消");
    }

    @Override
    public void onResume() {
        super.onResume();
        MainActivity.actionbar.setTitle("我的訂單");
        Log.d("OrderLookUpFragment", "onResuemd() called");
        // 驗證登入-防止crash
        SharedPreferences preferences_r = getActivity().getSharedPreferences(Common.PREF_FILE, getActivity().MODE_PRIVATE);
        String memId = null;
        if (preferences_r != null) {
            memId = preferences_r.getString("memId", null);
        }
        if (memId == null) {
            return;
        }
        // end of 驗證登入-防止crash

        if (skipOnResume) {
            skipOnResume = false; // do nothing
        } else if (ordNowPressed) {
            OrderLookUpFragment.this.myOrdList = getOrdList();
            updateOrdNowList();
        } else if (ordOldPressed) {
            OrderLookUpFragment.this.myOrdList = getOrdList();
            updateOrdOldList();
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup, Bundle bundle) {
        super.onCreateView(inflater, viewGroup, bundle);
        // 將floating btn設定隱藏
        getActivity().findViewById(R.id.floatingBtn).setVisibility(View.INVISIBLE);
        // find views
        View rootView = inflater.inflate(R.layout.fragment_ord_lookup, viewGroup, false);
        myRvOrd = (RecyclerView) rootView.findViewById(R.id.rv_ord);
        btnOrdNow = (Button) rootView.findViewById(R.id.btnOrdNow);
        btnOrdOld = (Button) rootView.findViewById(R.id.btnOrdOld);

        // 設定recycler view
        myRvOrd.setLayoutManager(new LinearLayoutManager(getActivity()));

        // 拿此會員的所有訂單:
        // OrderLookUpFragment.this.myOrdList = getOrdOldList();

        // setOnclickListener
        btnOrdNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isFreshNeeded){
                    isFreshNeeded = false;
                    OrderLookUpFragment.this.myOrdList = getOrdList();
                }
                //Util.showToast(getContext(), "now ord clicked");
                updateOrdNowList();
            }
        });
        btnOrdOld.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isFreshNeeded){
                    isFreshNeeded = false;
                    OrderLookUpFragment.this.myOrdList = getOrdList();
                }
                updateOrdOldList();
            }
        });

        // 預設: 開啟現有訂單
        ordOldPressed = false;
        ordNowPressed = true;


        return rootView;
    }


    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ordOldPressed = false;
        ordNowPressed = true;
    }

    private List<OrdVO> getOrdList() {
        List<OrdVO> myOrdList = null;
        try {
            String url = Common.URL + "/android/ord/ord.do";
            String action = OrdGetAllOldTask.GETALL;
            SharedPreferences preferences_r = getActivity().getSharedPreferences(Common.PREF_FILE, getActivity().MODE_PRIVATE);
            String memId = preferences_r.getString("memId", null);
            myOrdList = new OrdGetAllOldTask().execute(url, action, memId).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        Log.d("OrderLookUpFragment", " Old size - " + myOrdList.size());
        return myOrdList;
    }

    private void updateOrdNowList() {
        ordOldPressed = false;
        ordNowPressed = true;
        btnOrdOld.setTextColor(ContextCompat.getColor(getContext(), R.color.grey01));
        btnOrdNow.setTextColor(ContextCompat.getColor(getContext(), R.color.black));

        List<OrdVO> tmpOrdVOList = new ArrayList<>(myOrdList.size());
        for (OrdVO item : myOrdList)
            try {
                tmpOrdVOList.add((OrdVO) item.clone());
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }
        Iterator<OrdVO> itr = tmpOrdVOList.iterator();
        while (itr.hasNext()) {
            OrdVO myVO = itr.next();
            // 0.已下單 1.主動取消 2.已入住 3.已繳費 4.逾時取消
            if (myVO.getOrdStatus().equals("1") || myVO.getOrdStatus().equals("2") || myVO.getOrdStatus().equals("4")) {
                itr.remove();
            }
        }
        myAdapter_now = new OrderLookUpNowAdapter(getContext(), tmpOrdVOList, getActivity());
        // 使用firebase remote config 02
        getOrdDuration(myRvOrd,myAdapter_now);



    }

    private void updateOrdOldList() {
        ordOldPressed = true;
        ordNowPressed = false;
        btnOrdNow.setTextColor(ContextCompat.getColor(getContext(), R.color.grey01));
        btnOrdOld.setTextColor(ContextCompat.getColor(getContext(), R.color.black));
        List<OrdVO> tmpOrdVOList = new ArrayList<>(myOrdList.size());
        for (OrdVO item : myOrdList)
            try {
                tmpOrdVOList.add((OrdVO) item.clone());
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }
        Iterator<OrdVO> itr = tmpOrdVOList.iterator();
        while (itr.hasNext()) {
            OrdVO myVO = itr.next();
            Log.d("OrderLookUpFragment", "myVO.getOrdStatus() - " + myVO.getOrdStatus());
            // 0.已下單 1.主動取消 2.已入住 3.已繳費 4.逾時取消
            if (myVO.getOrdStatus().equals("0") || myVO.getOrdStatus().equals("3")) {
                itr.remove();
            }
        }
        myAdapter_old = new OrderLookUpOldAdapter(getContext(), tmpOrdVOList);
        myRvOrd.setAdapter(myAdapter_old);

    }

    // firebase remote config - 02
    private void getOrdDuration(final RecyclerView myRvOrd,final RecyclerView.Adapter<OrderLookUpNowAdapter.MyViewHolder> myAdapter_now) {
        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setDeveloperModeEnabled(BuildConfig.DEBUG)
                .build();
        mFirebaseRemoteConfig.setConfigSettings(configSettings);
        mFirebaseRemoteConfig.setDefaults(R.xml.remote_config_defaults);
        OrderLookUpFragment.ordDuration = mFirebaseRemoteConfig.getLong("ord_duration");
        Log.d(TAG, "duration local: " + OrderLookUpFragment.ordDuration+"");
        //Log.d(TAG, "duration local: " +  mFirebaseRemoteConfig.getLong("ord_duration")+"");



        long cacheExpiration = 3600; // 1 hour in seconds.
        // If in developer mode cacheExpiration is set to 0 so each fetch will retrieve values from
        // the server.
        if (mFirebaseRemoteConfig.getInfo().getConfigSettings().isDeveloperModeEnabled()) {
            cacheExpiration = 0;
        }

        mFirebaseRemoteConfig.fetch(cacheExpiration)
                .addOnCompleteListener(getActivity(), new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            //Toast.makeText(getActivity(), "Fetch Succeeded",Toast.LENGTH_SHORT).show();

                            // Once the config is successfully fetched it must be activated before newly fetched
                            // values are returned.
                            mFirebaseRemoteConfig.activateFetched();
                        } else {
                            //Toast.makeText(getActivity(), "Fetch Failed",Toast.LENGTH_SHORT).show();
                        }
                        // 在進入adapter前先拿到最新的ordDuration
                        OrderLookUpFragment.ordDuration = mFirebaseRemoteConfig.getLong("ord_duration");
                        Log.d(TAG, "duration after fetch: " + OrderLookUpFragment.ordDuration+"");
                        //Log.d(TAG, "duration fater fetch: " +  mFirebaseRemoteConfig.getLong("ord_duration")+"");

                        // 最後設定:
                        myRvOrd.setAdapter(myAdapter_now);

                    }
                });
    }// end of getOrdDuration


}
