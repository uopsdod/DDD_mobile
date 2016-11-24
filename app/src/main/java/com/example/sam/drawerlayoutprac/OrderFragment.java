package com.example.sam.drawerlayoutprac;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
    private Button btCash, btCriditCard;
    private String roomId, hotelId;
    private HotelVO hotelVO;
    private RoomVO roomVO;
    private TextView tvHotelName, tvHotelCity, tvHotelCounty, tvHotelRoad, tvHotelPhone, tvRoomName, tvRoomPrice;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        roomId = getArguments().getString("RoomId");
        hotelId = getArguments().getString("hotelId");
        View view = inflater.inflate(R.layout.fragment_order, container, false);
        btCash = (Button) view.findViewById(R.id.btCash);
        btCriditCard = (Button) view.findViewById(R.id.btCrditCard);
        tvHotelName = (TextView) view.findViewById(R.id.tvHotelName);
        tvHotelCity = (TextView) view.findViewById(R.id.tvHotelCity);
        tvHotelCounty = (TextView) view.findViewById(R.id.tvHotelCounty);
        tvHotelRoad = (TextView) view.findViewById(R.id.tvHotelRoad);
        tvHotelPhone = (TextView) view.findViewById(R.id.tvHotelPhone);
        tvRoomName = (TextView) view.findViewById(R.id.tvRoomName);
        tvRoomPrice = (TextView) view.findViewById(R.id.tvRoomPrice);

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

    @Override
    public void onStart() {
        super.onStart();
        hotelInfo();
    }

    private void hotelInfo(){
        if(Common.networkConnected(getActivity())){
            String urlHotel = Common.URL + "/android/hotel.do";
            String urlRoom = Common.URL + "/android/room.do";
            String urlOrder = Common.URL + "/android/order.do";
            try{
                hotelVO = new HotelGetOneTask().execute(urlHotel, hotelId).get();
                roomVO = new RoomGetOneTask().execute(urlRoom, roomId).get();
            }catch (Exception e) {
                Util.showToast(getContext(), "Orderfragment" + e.toString());
            }
            tvHotelName.setText(hotelVO.getHotelName());
            tvHotelCity.setText(hotelVO.getHotelCity());
            tvHotelCounty.setText(hotelVO.getHotelCounty());
            tvHotelRoad.setText(hotelVO.getHotelRoad());
            tvHotelPhone.setText(hotelVO.getHotelPhone());
            tvRoomName.setText(roomVO.getRoomName());
            tvRoomPrice.setText(roomVO.getRoomPrice().toString());
        }
    }

//    private void makeOrder(){
//
//    }
}
