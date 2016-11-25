package com.example.sam.drawerlayoutprac.Order;

import android.content.SharedPreferences;
import android.os.Bundle;
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

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Created by cuser on 2016/11/23.
 */

public class OrderLookUpFragment extends MustLoginFragment {

    public static boolean ordNowPressed = true;
    public static boolean ordOldPressed = false;
    RecyclerView myRvOrd;
    Button btnOrdNow;
    Button btnOrdOld;
    RecyclerView.Adapter<OrderLookUpOldAdapter.MyViewHolder> myAdapter;
    static HashMap<String, String> ordStatusConverter = new HashMap<>();

    public static boolean afterRatingCanceled = false;

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
        if (ordNowPressed){

        }else if(afterRatingCanceled){
            afterRatingCanceled = false;
            // do nothing
        }else if (ordOldPressed){
            getOrdOldList();
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

        // setOnclickListener
        btnOrdNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ordOldPressed = false;
                ordNowPressed = true;
            }
        });
        btnOrdOld.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ordOldPressed = true;
                ordNowPressed = false;
                getOrdOldList();
            }
        });


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

    private void getOrdOldList() {
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
        Log.d("OrderLookUpFragment", " size - " + myOrdList.size());
        myAdapter = new OrderLookUpOldAdapter(getContext(), myOrdList);
        myRvOrd.setAdapter(myAdapter);
    }
}
