package com.example.sam.drawerlayoutprac;

import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.example.sam.drawerlayoutprac.Hotel.HotelMapFragment;
import com.example.sam.drawerlayoutprac.Room.RoomVO;
import com.example.sam.drawerlayoutprac.Hotel.HotelGetOneTask;
import com.example.sam.drawerlayoutprac.Hotel.HotelVO;
import com.example.sam.drawerlayoutprac.Room.RoomGetOneTask;

import java.net.URI;
import java.util.concurrent.ExecutionException;

/**
 * Created by cuser on 2016/11/22.
 */

public class OrderFragment extends CommonFragment {
    private Button btQRDisplay, btSubmit;
    private String roomId, hotelId, ordId;
    private HotelVO hotelVO;
    private RoomVO roomVO;
    private TextView tvHotelName, tvHotelCity, tvHotelCounty, tvHotelRoad, tvHotelPhone, tvRoomName, tvRoomPrice;
    private  String memId = MainActivity.pref.getString("memId", null);
    int price;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        roomId = getArguments().getString("RoomId");
        hotelId = getArguments().getString("hotelId");
        View view = inflater.inflate(R.layout.fragment_order_cash, container, false);
        btQRDisplay = (Button) view.findViewById(R.id.btQRDisplay);
        btSubmit = (Button) view.findViewById(R.id.btSubmit);
        tvHotelName = (TextView) view.findViewById(R.id.tvHotelName);
        tvHotelCity = (TextView) view.findViewById(R.id.tvHotelCity);
        tvHotelCounty = (TextView) view.findViewById(R.id.tvHotelCounty);
        tvHotelRoad = (TextView) view.findViewById(R.id.tvHotelRoad);
        tvHotelPhone = (TextView) view.findViewById(R.id.tvHotelPhone);
        tvRoomName = (TextView) view.findViewById(R.id.tvRoomName);
        tvRoomPrice = (TextView) view.findViewById(R.id.tvRoomPrice);

        btQRDisplay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Fragment fragment = new ORImageFragment();
                Bundle bundle = new Bundle();
                bundle.putSerializable("ordId", ordId);
                fragment.setArguments(bundle);
                Util.switchFragment(OrderFragment.this, fragment);
            }
        });

        btSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(Common.networkConnected(getActivity())){
                    String urlOrder = Common.URL + "/android/ord/ord.do";
                    try {
                        ordId = new OrderAddOneTask().execute(urlOrder, hotelId, roomId, memId, price).get();
                    } catch (Exception e) {
                        Util.showToast(getContext(), "Orderfragment" + e.toString());
                    }
                }

            }
        });
        return view;
    }



    @Override
    public void onStart() {
        super.onStart();
        hotelInfo();
    }

    private void hotelInfo(){
        if(Common.networkConnected(getActivity())){
            String urlHotel = Common.URL + "/android/hotel.do";
            String urlRoom = Common.URL + "/android/room.do";
            try{
                hotelVO = new HotelGetOneTask().execute(urlHotel, hotelId).get();
                roomVO = new RoomGetOneTask().execute(urlRoom, roomId).get();
                price = roomVO.getRoomPrice();

            }catch (Exception e) {
                Util.showToast(getContext(), "Orderfragment" + e.toString());
            }
            Log.d("Ord1125","OrderFragment" +ordId);

            tvHotelName.setText(hotelVO.getHotelName());
            tvHotelCity.setText(hotelVO.getHotelCity());
            tvHotelCounty.setText(hotelVO.getHotelCounty());
            tvHotelRoad.setText(hotelVO.getHotelRoad());
            tvHotelPhone.setText(hotelVO.getHotelPhone());
            tvRoomName.setText(roomVO.getRoomName());
            tvRoomPrice.setText(roomVO.getRoomPrice().toString());
        }
    }

}
