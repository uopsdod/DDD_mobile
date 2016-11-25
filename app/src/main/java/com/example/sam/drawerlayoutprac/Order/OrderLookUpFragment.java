package com.example.sam.drawerlayoutprac.Order;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.sam.drawerlayoutprac.Common;
import com.example.sam.drawerlayoutprac.MustLoginFragment;
import com.example.sam.drawerlayoutprac.Partner.VO.OrdVO;
import com.example.sam.drawerlayoutprac.R;
import com.example.sam.drawerlayoutprac.Util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Created by cuser on 2016/11/23.
 */

public class OrderLookUpFragment extends MustLoginFragment {

    public static boolean ordNowPressed = true;
    public static boolean ordOldPressed = false;
    List<OrdVO> myOrdList;
    RecyclerView myRvOrd;
    Button btnOrdNow;
    Button btnOrdOld;
    RecyclerView.Adapter<OrderLookUpNowAdapter.MyViewHolder> myAdapter_now;
    RecyclerView.Adapter<OrderLookUpOldAdapter.MyViewHolder> myAdapter_old;
    static HashMap<String, String> ordStatusConverter = new HashMap<>();

    public static boolean afterRatingOrReportCanceled = false;

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
        Log.d("OrderLookUpFragment", "onResuemd() called");
        if (ordNowPressed) {

        } else if (afterRatingOrReportCanceled) {
            afterRatingOrReportCanceled = false;
            // do nothing
        } else if (ordOldPressed) {
            OrderLookUpFragment.this.myOrdList = getOrdOldList();
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
        // 預設: 開啟現有訂單
        btnOrdOld.setTextColor(ContextCompat.getColor(getContext(), R.color.grey01));
        btnOrdNow.setTextColor(ContextCompat.getColor(getContext(), R.color.black));

        // 設定recycler view
        myRvOrd.setLayoutManager(new LinearLayoutManager(getActivity()));

        // 拿此會員的所有訂單:
        OrderLookUpFragment.this.myOrdList = getOrdOldList();

        // setOnclickListener
        btnOrdNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Util.showToast(getContext(),"now ord clicked");
                updateOrdNowList();
            }
        });
        btnOrdOld.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateOrdOldList();
            }
        });


        return rootView;
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
            if (!(myVO.getOrdStatus().equals("0") || myVO.getOrdStatus().equals("3"))) {
                itr.remove();
            }
        }
        myAdapter_now = new OrderLookUpNowAdapter(getContext(), tmpOrdVOList);
        myRvOrd.setAdapter(myAdapter_now);


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

    private List<OrdVO> getOrdOldList() {
        List<OrdVO> myOrdList = null;
        try {
            String url = Common.URL + "/android/ord/ord.do";
            String action = OrdGetAllOldTask.GETALL_OLD;
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
            if (myVO.getOrdStatus().equals("0") || myVO.getOrdStatus().equals("3")) {
                itr.remove();
            }
        }
        myAdapter_old = new OrderLookUpOldAdapter(getContext(), tmpOrdVOList);
        myRvOrd.setAdapter(myAdapter_old);

    }

//    private void getOrdNowList() {
//        List<OrdVO> myOrdList = null;
//        try {
//            String url = Common.URL + "/android/ord/ord.do";
//            String action = OrdGetAllOldTask.GETALL_NOW;
//            SharedPreferences preferences_r = getActivity().getSharedPreferences(Common.PREF_FILE, getActivity().MODE_PRIVATE);
//            String memId = preferences_r.getString("memId", null);
//            myOrdList = new OrdGetAllOldTask().execute(url, action, memId).get();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        } catch (ExecutionException e) {
//            e.printStackTrace();
//        }
//        Log.d("OrderLookUpFragment ", " Now size - " + myOrdList.size());
//        //myAdapter = new OrderLookUpNowAdapter(getContext(), myOrdList);
//        //myRvOrd.setAdapter(myAdapter);
//    }
}
