package com.example.sam.drawerlayoutprac.Hotel;


import android.app.Activity;
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

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft;
import org.java_websocket.drafts.Draft_17;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class HotelInfoFragment extends CommonFragment implements Serializable {
    private static final String TAG = "HotelInfoFragment";
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView rv_hotelInfo;
    private TextView tvStatus;
    private ImageView ivHotelBig;
    private TextView tvHotelName, tvHotelCity, tvHotelCounty, tvHotelRoad, tvHotelPhone, tvHotelIntro;
    private String hotelId;
    private HotelRoomPriceSocket hotelRoomPriceScoket;
    private float aFloat = (float) 0.6;
    private List<RoomVO> room;
    RecyclerView.Adapter<SpotAdapter.ViewHolder> myAdapter;


    @Override
    public void onResume() {
        super.onResume();
        showHotelInfo();
        showRoomInfo();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 取得動態價格
        getDynamicPrice();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        hotelId = getArguments().getString("hotelId");
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
    }

    @Override
    public void onPause() {
        super.onPause();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (hotelRoomPriceScoket != null) {
            hotelRoomPriceScoket.close();
            Log.d("hotelRoomPricesocket "," close websocket");
        }
    }

    private void showHotelInfo() {
        if (Common.networkConnected(getActivity())) {
            String url = Common.URL + "/android/hotel.do";
            String id = hotelId;
            HotelVO hotelVO = null;
            int imageSize = 250;
            try {
                hotelVO = new HotelGetOneTask().execute(url, id).get();
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
            String id = hotelId;

            try {
                room = new RoomGetAllTask().execute(url, id).get();
            } catch (Exception e) {
                Log.e(TAG, e.toString());
            }
            if (room == null || room.isEmpty()) {
                tvStatus.setVisibility(View.VISIBLE);
                Util.showToast(getActivity(), "No Room fonnd");
            } else {
                myAdapter = new SpotAdapter(getActivity(), room);
                rv_hotelInfo.setAdapter(myAdapter);
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
            TextView tvRoomName, tvPrice;

            public ViewHolder(View itemView) {
                super(itemView);
                ivImage = (ImageView) itemView.findViewById(R.id.ivImage);
                tvRoomName = (TextView) itemView.findViewById(R.id.tvRoomName);
                tvPrice = (TextView) itemView.findViewById(R.id.tvPrice);
                ivImage.setAlpha(aFloat);
            }
        }

        @Override
        public SpotAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = inflater.inflate(R.layout.rv_item_room, parent, false);
            return new ViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(SpotAdapter.ViewHolder holder, int position) {
            RoomVO myspot = list.get(position);
            final String roomId = myspot.getRoomId();
            String url = Common.URL + "/android/room.do";
            int imageSize = 250;
            Bitmap bitmap = null;
            try {
                bitmap = new RoomGetImageTask(null).execute(url, roomId, imageSize).get();
            } catch (Exception e) {
                Log.e(TAG, e.toString());
            }
            if (bitmap != null) {
                holder.ivImage.setImageBitmap(bitmap);
            } else {
                holder.ivImage.setImageResource(R.drawable.search);
            }
            holder.tvRoomName.setText(myspot.getRoomName());
            holder.tvPrice.setText("$" + Integer.toString(myspot.getRoomPrice()));
            holder.itemView.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View view) {
                    Fragment fragment = new RoomFragment();
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("roomId", roomId);
                    bundle.putSerializable("hotelId", hotelId);
                    fragment.setArguments(bundle);
                    Util.switchFragment(HotelInfoFragment.this, fragment);
                    Util.showToast(getContext(), "roomId" + roomId);
                }
            });
        }

        @Override
        public int getItemCount() {
            return list.size();
        }


    }

    //取得動態的價格
    private void getDynamicPrice() {
        URI uri = null;
        try {
            uri = new URI(Common.URL_DYNAMICPRICE);
        } catch (URISyntaxException e) {
            Log.d(TAG, e.toString());
        }
        hotelRoomPriceScoket = new HotelRoomPriceSocket(uri, getActivity());
        if (hotelRoomPriceScoket != null) {
            hotelRoomPriceScoket.connect();
        }
    }

    public class HotelRoomPriceSocket extends WebSocketClient {
        URI uri;
        Activity activity;

        public HotelRoomPriceSocket(URI serverUri, Activity activity) {
            super(serverUri, new Draft_17());
            uri = serverUri;
            this.activity = activity;
        }

        @Override
        public void onOpen(ServerHandshake handshakedata) {
            Log.d("hotelRoomPricesocket - ", " open websocket successfully ");
        }

        @Override
        public void onMessage(final String message) {
            Thread myThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    activity.runOnUiThread(new Runnable() {
                                               @Override
                                               public void run() {
                                                   //從websoclet取得動態價錢的資料
                                                   try {
                                                       JSONObject jsonObject = new JSONObject(message);
                                                       //解開從websocket上拿到的JSONObject物件
                                                       JSONArray jsonArray = jsonObject.getJSONArray("Bag");
                                                       JSONArray jsonArrayRoom = null;
                                                       Log.d(TAG, jsonArray.toString());
                                                       //把List<RoomVO> room 內的資料， 逐一取出來

                                                       //再解開從JSONObject上拿到的JSONArray並逐一取出
                                                       for (int i = 0; i < jsonArray.length(); i++) {
                                                           jsonArrayRoom = jsonArray.getJSONArray(i);
                                                           //比對RoomId是否相同，若相同就從解析出來的JSONArray動態價格資料，裝進 roomVO裡面
                                                           Log.d("hotelRoomPricesocket - ",jsonArrayRoom.get(0) + "," + jsonArrayRoom.get(1));
                                                           for (RoomVO roomVO: HotelInfoFragment.this.room){
                                                               if (jsonArrayRoom.getString(0).equals(roomVO.getRoomId())) {
                                                                   roomVO.setRoomPrice((Integer) jsonArrayRoom.get(1));
                                                               }
                                                           }

//                                                           for (int j = 0; i<HotelInfoFragment.this.room.size(); j++) {
//                                                               myVO = HotelInfoFragment.this.room.get(i);
//                                                               if (jsonArrayRoom.getString(0).equals(myVO.getRoomId())) {
//                                                                   Log.d("hotelRoomPricesocket - ", ""+jsonArrayRoom.get(0));
//                                                                   //myVO.setRoomPrice((Integer) jsonArrayRoom.get(1));
//                                                                   //myAdapter.notifyDataSetChanged();
//                                                               }
//                                                           }
                                                       }
                                                   } catch (JSONException e) {
                                                       e.printStackTrace();
                                                   }

                                                   //等資料更新完後，再更新UI
                                                   myAdapter.notifyDataSetChanged();
                                               } // end of run method
                                           }// end of Runnable
                    );// end of activity.runOnUiThread
                } // end of run method
            } // end of Runnable

            ); // end of myThread
            myThread.start();

        }

        @Override
        public void onClose(int code, String reason, boolean remote) {

        }

        @Override
        public void onError(Exception ex) {

        }
    }

}
