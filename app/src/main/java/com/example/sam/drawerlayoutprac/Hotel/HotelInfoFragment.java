package com.example.sam.drawerlayoutprac.Hotel;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import android.widget.RatingBar;
import android.widget.TextView;

import com.example.sam.drawerlayoutprac.Common;
import com.example.sam.drawerlayoutprac.CommonFragment;
import com.example.sam.drawerlayoutprac.R;
import com.example.sam.drawerlayoutprac.Room.RoomFragment;
import com.example.sam.drawerlayoutprac.Room.RoomGetAllTask;
import com.example.sam.drawerlayoutprac.Room.RoomGetImageTask;
import com.example.sam.drawerlayoutprac.Room.RoomVO;
import com.example.sam.drawerlayoutprac.Util;

import java.io.Serializable;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class HotelInfoFragment extends CommonFragment implements Serializable {
    private static final String TAG = "HotelInfoFragment";
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView rv_hotelInfo;
    private TextView tvStatus;
    private ImageView ivHotelBig;
    private TextView tvHotelName, tvHotelCity, tvHotelCounty, tvHotelRoad, tvHotelPhone, tvHotelIntro;
    private HotelVO hotelVO;
    private float aFloat = (float) 0.6;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        String hotelId = getArguments().getString("hotelId");
        String url = Common.URL + "/android/hotel.do";
        try {
            hotelVO = new HotelGetOneTask().execute(url,hotelId).get();
//            hotelVO = (HotelVO) getArguments().getSerializable("hotelVO");
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }



        //get View
        View view = inflater.inflate(R.layout.fragment_hotelinfo, container, false);

        ivHotelBig = (ImageView) view.findViewById(R.id.ivHotel);
        tvHotelName = (TextView) view.findViewById(R.id.tvHotelName);
        tvHotelCity = (TextView) view.findViewById(R.id.tvHotelCity);
        tvHotelCounty = (TextView) view.findViewById(R.id.tvHotelCounty);
        tvHotelRoad = (TextView) view.findViewById(R.id.tvHotelRoad);
        tvHotelPhone = (TextView) view.findViewById(R.id.tvHotelPhone);
        tvHotelIntro = (TextView) view.findViewById(R.id.tvHotelIntro);
        tvStatus = (TextView) view.findViewById(R.id.tvStatus);

        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefreshLayout.setRefreshing(true);
                showRoomInfo();
                swipeRefreshLayout.setRefreshing(false);
            }
        });
        rv_hotelInfo = (RecyclerView) view.findViewById(R.id.rv_hotelDatail);
        getActivity().findViewById(R.id.floatingBtn).setVisibility(View.INVISIBLE);

        rv_hotelInfo.setLayoutManager(new LinearLayoutManager(getActivity()));

        //warp up
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        showHotelInfo();
        showRoomInfo();
    }

    private void showHotelInfo() {
        if (Common.networkConnected(getActivity())) {
            String url = Common.URL + "/android/hotel.do";
            String id = hotelVO.getHotelId();
            int imageSize = 250;
            try {
                new HotelGetImageTask(ivHotelBig).execute(url, id, imageSize);
            } catch (Exception e) {
                Log.e(TAG, e.toString());
            }
            if (hotelVO == null) {
                Util.showToast(getActivity(), "No hotel fonnd");
            } else {
                tvHotelName.setText(hotelVO.getHotelName());
                tvHotelCity.setText(hotelVO.getHotelCity());
                tvHotelCounty.setText(hotelVO.getHotelCounty());
                tvHotelPhone.setText(hotelVO.getHotelPhone());
                tvHotelRoad.setText(hotelVO.getHotelRoad());
                tvHotelIntro.setText(hotelVO.getHotelIntro());
            }
        }

    }

    private void showRoomInfo() {
        if (Common.networkConnected(getActivity())) {
            String url = Common.URL + "/android/room.do";
            String id = hotelVO.getHotelId();
            List<RoomVO> room = null;
            try {
                room = new RoomGetAllTask().execute(url, id).get();
            } catch (Exception e) {
                Log.e(TAG, e.toString());
            }
            if (room == null || room.isEmpty()) {
                tvStatus.setVisibility(View.VISIBLE);
                Util.showToast(getActivity(), "No Room fonnd");
            } else {
                rv_hotelInfo.setAdapter(new SpotAdapter(getActivity(), room));
            }
        }

    }

    private class SpotAdapter extends RecyclerView.Adapter<SpotAdapter.ViewHolder> {
        private Context context;
        private List<RoomVO> list;
        private LayoutInflater inflater;

        public SpotAdapter(Context context, List<RoomVO> list) {
            this.context = context;
            this.list = list;
            inflater = LayoutInflater.from(context);
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            ImageView ivImage;
            TextView tvHotel, tvPrice;

            public ViewHolder(View itemView) {
                super(itemView);
                ivImage = (ImageView) itemView.findViewById(R.id.ivImage);
                tvHotel = (TextView) itemView.findViewById(R.id.tvHotel);
                tvPrice = (TextView) itemView.findViewById(R.id.tvPrice);
                ivImage.setAlpha(aFloat);
            }
        }

        @Override
        public SpotAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = inflater.inflate(R.layout.rv_item_hotel, parent, false);
            return new ViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(SpotAdapter.ViewHolder holder, int position) {
            final RoomVO myspot = list.get(position);
            final String RoomId = myspot.getRoomId();
            final String hotelId = hotelVO.getHotelId();
            String url = Common.URL + "/android/room.do";
            int imageSize = 250;
            Bitmap bitmap = null;
            try {
                bitmap = new RoomGetImageTask(null).execute(url, RoomId, imageSize).get();
            } catch (Exception e) {
                Log.e(TAG, e.toString());
            }
            if (bitmap != null) {
                holder.ivImage.setImageBitmap(bitmap);
            } else {
                holder.ivImage.setImageResource(R.drawable.search);
            }
            holder.tvPrice.setText("$" + Integer.toString(myspot.getRoomPrice()));
            holder.itemView.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View view) {
                    Fragment fragment = new RoomFragment();
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("RoomId", RoomId);
                    bundle.putSerializable("hotelId", hotelId);
                    fragment.setArguments(bundle);
                    Util.switchFragment(HotelInfoFragment.this, fragment);
                }
            });
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

    }

}
