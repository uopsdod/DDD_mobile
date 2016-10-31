package com.example.sam.drawerlayoutprac;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class HotelInfoFragment extends Fragment{
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);


        //get Data
        List<Spot> mySpot = getSpot();
        //get View
        View view = inflater.inflate(R.layout.fragment_hotelinfo, container, false);
        RecyclerView rv_hotelInfo = (RecyclerView) view.findViewById(R.id.rv_hotelDatail);
        if(rv_hotelInfo != null){
            rv_hotelInfo.setLayoutManager(new LinearLayoutManager(getActivity()));
            rv_hotelInfo.setAdapter(new SpotAdapter(getActivity(), mySpot));
        }
        //warp up
        return rv_hotelInfo;
    }

    private class SpotAdapter extends  RecyclerView.Adapter<SpotAdapter.ViewHolder>{
        private Context context;
        private List<Spot> list;
        private LayoutInflater inflater;

        public SpotAdapter(Context context, List<Spot> list){
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
            final Spot myspot = list.get(position);
            holder.ivImage.setImageResource(myspot.getimgId());
            holder.tvHotel.setText(String.valueOf(myspot.getHotelName()));
            holder.tvPrice.setText(Integer.toString(myspot.getPrice()) + "$");
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
