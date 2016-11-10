package com.example.sam.drawerlayoutprac.Hotel;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.sam.drawerlayoutprac.Common;
import com.example.sam.drawerlayoutprac.R;
import com.example.sam.drawerlayoutprac.Util;

import java.util.List;

public class HotelFragment extends Fragment {
    String TAG = "HotelFragment";
    RecyclerView myRvSpot;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup, Bundle bundle) {
        super.onCreateView(inflater, viewGroup, bundle);

        View myLayout = inflater.inflate(R.layout.fragment_hotel, viewGroup, false);
        myRvSpot = (RecyclerView) myLayout.findViewById(R.id.rv_hotel);
        getActivity().findViewById(R.id.floatingBtn).setVisibility(View.VISIBLE);

        myRvSpot.setLayoutManager(new LinearLayoutManager(getActivity()));
        return myLayout;
    }

    private void  showAllHotel(){
        if(Common.networkConnected(getActivity())){
            String url = Common.URL + "/android/hotel.do";
            List<HotelVO> hotel = null;
            try{
                hotel = new HotelGetAllTask().execute(url).get();
            }catch(Exception e){
                Log.e(TAG, e.toString());
            }
            if(hotel == null || hotel.isEmpty()){
                Util.showToast(getActivity(), "No hotel fonnd");
            }else{
                myRvSpot.setAdapter(new SpotAdapter(getActivity(), hotel));
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        showAllHotel();
    }
    private class SpotAdapter extends RecyclerView.Adapter<SpotAdapter.MyViewHolder> {
        private Context context;
        private List<HotelVO> myListSpot;
        private LayoutInflater myLayoutInflater;

        public SpotAdapter(Context context, List<HotelVO> myListSpot) {
            this.context = context;
            this.myListSpot = myListSpot;
            myLayoutInflater = LayoutInflater.from(context);
        }

        // customed ViewHolder - 裝容器用
        public class MyViewHolder extends RecyclerView.ViewHolder {
            ImageView ivImage;
            TextView tvHotel;
            TextView tvPrice;

            // 把拿到的到個view的資料一個個存好成實體變數
            public MyViewHolder(View itemView) {
                super(itemView); // 必須使用這個父建構式，因為RecyclerView.ViewHolder沒有空參數的建構式 - 用父類別的實體變數來指向這個View物件,第90行會用到
                float f = (float) 0.6;
                this.ivImage = (ImageView) itemView.findViewById(R.id.ivImage);
                this.tvHotel = (TextView) itemView.findViewById(R.id.tvHotel);
                this.tvPrice = (TextView) itemView.findViewById(R.id.tvPrice);
                this.ivImage.setAlpha(f); // 設定圖片透明度 (float)
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
            final HotelVO hotelVO = myListSpot.get(position);
            final String HotelId = hotelVO.getHotelId();
            String url = Common.URL + "/android/hotel.do";
            int imageSize = 250;
            new HotelGetImageTask(holder.ivImage).execute(url, HotelId, imageSize);
            holder.tvHotel.setText(hotelVO.getHotelName());
//            holder.tvPrice.setText(Integer.toString(mySpot.getPrice()) + "$");
            holder.itemView.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View view) {
                    Fragment fragment = new HotelInfoFragment();
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("hotelVO", hotelVO);
                    fragment.setArguments(bundle);
                    Util.switchFragment(HotelFragment.this, fragment);
                }
            });
        }
    }// end class SpotAdapter

}
