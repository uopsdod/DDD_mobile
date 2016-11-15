package com.example.sam.drawerlayoutprac.Room;


import android.content.Context;
import android.graphics.Bitmap;
import android.icu.util.BuddhistCalendar;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.example.sam.drawerlayoutprac.Common;
import com.example.sam.drawerlayoutprac.Hotel.HotelGetOneTask;
import com.example.sam.drawerlayoutprac.Hotel.HotelVO;
import com.example.sam.drawerlayoutprac.R;
import com.example.sam.drawerlayoutprac.Util;

import java.util.List;
import java.util.concurrent.ExecutionException;

public class RoomFragment extends Fragment {
    private String TAG = "RoomFragment";
    private TextView tvRoomName,tvFacilitiesDetail, tvPrice;
    private String RoomId;
    private ImageView imageView;
    private RecyclerView rv_RoomImage;
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
        imageView = (ImageView) view.findViewById(R.id.imageView);
        rv_RoomImage = (RecyclerView) view.findViewById(R.id.rv_RoomImage);
        rv_RoomImage.setLayoutManager(new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.HORIZONTAL));

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        showRoomDetail();
        showAllRoomPhoto();
        showFirstRoomPhoto();
//        rv_RoomImage.setAdapter(new RoomImageRecyclerViewAdapter(getActivity()));
    }

    private void showFirstRoomPhoto(){
        if (Common.networkConnected(getActivity())) {
            String url = Common.URL + "/android/room.do";
            String id = RoomId;
            int imageSize = 250;
            new RoomGetImageTask(imageView) .execute(url, id, imageSize);
        }
    }

    private void showAllRoomPhoto(){
        if(Common.networkConnected(getActivity())){
            String url = Common.URL + "/android/room.do";
            String id = RoomId;
            List<String> roomVOList = null;
            int imageSize = 250;
            try{
                roomVOList = new RoomGetOneTask2().execute(url, id, imageSize).get();
            }catch(Exception e){
                Log.e(TAG,e.toString());
            }
            if(roomVOList == null || roomVOList.isEmpty()){
                Util.showToast(getActivity(),"No roomVOList Found!!");
            }else{
                rv_RoomImage.setAdapter(new RoomImageRecyclerViewAdapter(getActivity(), roomVOList));
            }
        }
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

    private class RoomImageRecyclerViewAdapter extends RecyclerView.Adapter<RoomImageRecyclerViewAdapter.MyViewHolder>{
        private LayoutInflater layoutInflater;
        private List<String> roomPhotoList;

        public RoomImageRecyclerViewAdapter(Context context, List<String> roomVOList){
            layoutInflater = layoutInflater.from(context);
            this.roomPhotoList = roomVOList;
        }

        @Override
        public int getItemCount() {
            return roomPhotoList.size();
        }

        @Override
        public RoomImageRecyclerViewAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = layoutInflater.inflate(R.layout.rv_roomimage_item, parent, false);
            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(RoomImageRecyclerViewAdapter.MyViewHolder holder, int position) {
//            final RoomPhotoVO roomVO = roomPhotoList.get(position);
            String url = Common.URL + "/android/room.do";
            String id = roomPhotoList.get(position);
            Bitmap bitmap = null;
            int imageSize = 250;
            try {
                bitmap = new RoomGetAllImageTask(holder.rvImage).execute(url, id, imageSize).get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
            final Bitmap finalBitmap = bitmap;
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    imageView.setImageBitmap(finalBitmap);
                }
            });
        }

        public class MyViewHolder extends RecyclerView.ViewHolder{
            ImageView rvImage;

            public MyViewHolder(View itemView) {
                super(itemView);
                rvImage = (ImageView) itemView.findViewById(R.id.rvImage);
            }
        }
    }
}
