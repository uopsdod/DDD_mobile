package com.example.sam.drawerlayoutprac.Room;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.sam.drawerlayoutprac.Common;
import com.example.sam.drawerlayoutprac.Hotel.HotelGetOneTask;
import com.example.sam.drawerlayoutprac.Hotel.HotelVO;
import com.example.sam.drawerlayoutprac.R;
import com.example.sam.drawerlayoutprac.Util;

public class RoomFragment extends Fragment {
    private String TAG = "RoomFragment";
    private TextView tvRoomName,tvFacilitiesDetail, tvPrice;
    private String RoomId ;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_room, container, false);
        getActivity().findViewById(R.id.floatingBtn).setVisibility(View.INVISIBLE);
        RoomId = getArguments().getString("RoomId");
        tvRoomName = (TextView) view.findViewById(R.id.tvRoomName);
        tvPrice = (TextView) view.findViewById(R.id.tvPrice);
        tvFacilitiesDetail = (TextView) view.findViewById(R.id.tvFacilitiesDetail);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        showRoomDetail();
    }

    private void showRoomDetail() {
        if (Common.networkConnected(getActivity())) {
            String url = Common.URL + "/android/room.do";
            String id = RoomId;
            RoomVO room = null;
            try {
                room = new RoomGetOneTask().execute(url, id).get();
            } catch (Exception e) {
                Log.e(TAG, e.toString());
            }
            if (room == null) {
                Util.showToast(getActivity(), "No hotel fonnd");
            } else {
                tvRoomName.setText(room.getRoomName());
                tvPrice.setText(room.getRoomPrice().toString());
                tvFacilitiesDetail.setText(room.getRoomFun()+"\n"+room.getRoomMeal()+"\n"+room.getRoomSleep()+"\n"+room.getRoomFacility()+"\n"+room.getRoomSweetFacility());

            }
        }

    }
}
