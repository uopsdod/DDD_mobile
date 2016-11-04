package com.example.sam.drawerlayoutprac;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class HotelInfoFragment extends Fragment implements Serializable{
    private static final String TAG = "HotelInfoFragment";
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView rv_hotelInfo;
    private ImageView ivHotelBig;
    private TextView tvHotelName, tvHotelCity, tvHotelCounty, tvHotelRoad, tvHotelPhone;
    private String hotelId;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        hotelId= getArguments().getString("hotelId");
        //get Data
        List<Spot> mySpot = getSpot();
        //get View
        View view = inflater.inflate(R.layout.fragment_hotelinfo, container, false);
        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener(){
            @Override
            public void onRefresh() {
                swipeRefreshLayout.setRefreshing(true);
                showHotelInfo();
                swipeRefreshLayout.setRefreshing(false);
            }
        });
        rv_hotelInfo = (RecyclerView) view.findViewById(R.id.rv_hotelDatail);
        getActivity().findViewById(R.id.floatingBtn).setVisibility(View.INVISIBLE);
        if(rv_hotelInfo != null){
            rv_hotelInfo.setLayoutManager(new LinearLayoutManager(getActivity()));

        }
        //warp up
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    private void showHotelInfo() {
        if(Common.networkConnected(getActivity())){
            String url = Common.URL + "/android/hotel.do";
            String id = hotelId;
            List<HotelVO> hotel = null;
            try{
                hotel = new HotelGetOneTask().execute(url, id).get();
            }catch(Exception e){
                Log.e(TAG, e.toString());
            }
            if(hotel == null || hotel.isEmpty()){
                Util.showToast(getActivity(), "No hotel fonnd");
            }else{
                rv_hotelInfo.setAdapter(new SpotAdapter(getActivity(), hotel));
            }
        }

    }

    private class SpotAdapter extends  RecyclerView.Adapter<SpotAdapter.ViewHolder>{
        private Context context;
        private List<HotelVO> list;
        private LayoutInflater inflater;

        public SpotAdapter(Context context, List<HotelVO> list){
            this.context = context;
            this.list = list;
            inflater = LayoutInflater.from(context);
        }

        public class ViewHolder extends RecyclerView.ViewHolder{
            ImageView ivImage;
            TextView tvHotel, tvPrice;

            public ViewHolder(View itemView) {
                super(itemView);
                ivImage = (ImageView) itemView.findViewById(R.id.ivImage);
                tvHotel = (TextView) itemView.findViewById(R.id.tvHotel);
                tvPrice = (TextView) itemView.findViewById(R.id.tvPrice);
            }
        }

        @Override
        public SpotAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = inflater.inflate(R.layout.rv_item_hotel, parent, false);
            return new ViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(SpotAdapter.ViewHolder holder, int position) {
            final HotelVO myspot = list.get(position);
//            holder.ivImage.setImageResource(myspot.getimgId());
            holder.tvHotel.setText(String.valueOf(myspot.getHotelName()));
//            holder.tvPrice.setText("$" + Integer.toString(myspot.getPrice()));
//            holder.itemView.setOnClickListener(new View.OnClickListener(){
//
//                @Override
//                public void onClick(View view) {
//                    Fragment fragment = new RoomFragment();
//                    Util.switchFragment(HotelInfoFragment.this, fragment);
//                }
//            });
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

    }

    private List<Spot> getSpot(){
        List<Spot> mySpot = new ArrayList<>();
        mySpot.add(new Spot(R.drawable.room03, "豪華雙人間", 2860));
        mySpot.add(new Spot(R.drawable.room02, "尊貴行政雙床間", 3802));
        mySpot.add(new Spot(R.drawable.room03, "一臥室公寓(4位成人)", 6858));
        return mySpot;
    }
}
