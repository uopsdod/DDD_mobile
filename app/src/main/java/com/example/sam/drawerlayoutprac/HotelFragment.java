package com.example.sam.drawerlayoutprac;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.ForwardingListener;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class HotelFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup, Bundle bundle){
        super.onCreateView(inflater,viewGroup,bundle);

        // get data
        List<Spot> myListSpot = getSpots();
        // get view
        View myLayout = inflater.inflate(R.layout.fragment_hotel,viewGroup,false);
        RecyclerView myRvSpot = (RecyclerView)myLayout.findViewById(R.id.rv_hotel);
        getActivity().findViewById(R.id.floatingBtn).setVisibility(View.VISIBLE);

        if (myRvSpot != null){
            myRvSpot.setLayoutManager(new LinearLayoutManager(getActivity()));
            myRvSpot.setAdapter(new SpotAdapter(getActivity(), myListSpot));
        }
        // wrap up
        return myLayout;
    }

    private class SpotAdapter extends RecyclerView.Adapter<SpotAdapter.MyViewHolder> {
        private Context context;
        private List<Spot> myListSpot;
        private LayoutInflater myLayoutInflater;

        public SpotAdapter(Context context, List<Spot> myListSpot) {
            this.context = context;
            this.myListSpot = myListSpot;
            myLayoutInflater = LayoutInflater.from(context);
        }

        // customed ViewHolder - 裝容器用
        public class MyViewHolder extends RecyclerView.ViewHolder{
            ImageView ivImage;
            TextView tvHotel;
            TextView tvPrice;
            // 把拿到的到個view的資料一個個存好成實體變數
            public MyViewHolder(View itemView) {
                super(itemView); // 必須使用這個父建構式，因為RecyclerView.ViewHolder沒有空參數的建構式 - 用父類別的實體變數來指向這個View物件,第90行會用到
                this.ivImage = (ImageView) itemView.findViewById(R.id.ivImage);
                this.tvHotel = (TextView) itemView.findViewById(R.id.tvHotel);
                this.tvPrice = (TextView) itemView.findViewById(R.id.tvPrice);
            }
        }

        @Override
        public SpotAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = myLayoutInflater.inflate(R.layout.rv_item_hotel, parent, false);
            return new SpotAdapter.MyViewHolder(view);
        }

        @Override
        public int getItemCount() {
            return myListSpot.size();
        }

        @Override
        public void onBindViewHolder(SpotAdapter.MyViewHolder holder, int position) {
            Spot mySpot = myListSpot.get(position);

            holder.ivImage.setImageResource(mySpot.getimgId());
            holder.tvHotel.setText(mySpot.getHotelName());
            holder.tvPrice.setText(Integer.toString(mySpot.getPrice()) + "$");
            holder.itemView.setOnClickListener(new View.OnClickListener(){

                @Override
                public void onClick(View view) {
                    Fragment fragment = new HotelInfoFragment();
                    Util.switchFragment(HotelFragment.this,fragment);
                }
            });
        }
    }// end class SpotAdapter

    private List<Spot> getSpots() {
        List<Spot> spots = new ArrayList<>();
        spots.add(new Spot(R.drawable.hotel1,"福華飯店",300));
        spots.add(new Spot(R.drawable.hotel2,"青年旅館",100));
        spots.add(new Spot(R.drawable.hotel3,"我家三樓房間",50));
        return spots;
    }
}
