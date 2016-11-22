package com.example.sam.drawerlayoutprac;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

/**
 * Created by cuser on 2016/11/22.
 */

public class OrderFragment extends CommonFragment {
    private Button btCash, btCriditCard;
    private String roomId, hotelId;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        roomId = getArguments().getString("roomId");
        hotelId = getArguments().getString("hotelId");
        View view = inflater.inflate(R.layout.fragment_order, container, false);
        btCash = (Button) view.findViewById(R.id.btCash);
        btCriditCard = (Button) view.findViewById(R.id.btCrditCard);

        btCash.setOnClickListener(new onClick());
        btCriditCard.setOnClickListener(new onClick());
        return view;
    }

    private class onClick implements View.OnClickListener{

        @Override
        public void onClick(View view) {
            Util.switchFragment(OrderFragment.this, new OrderPayFragment());
        }
    }

//    private void orderNow(){
//        if(Common.networkConnected(getActivity())){
//            String url = Common.URL + "/android/order.do";
//
//        }
//    }
}
