package com.example.sam.drawerlayoutprac.Hotel;

import android.app.Activity;
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
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.sam.drawerlayoutprac.Common;
import com.example.sam.drawerlayoutprac.CommonFragment;
import com.example.sam.drawerlayoutprac.MainActivity;
import com.example.sam.drawerlayoutprac.Partner.Chat.ChatFragment;
import com.example.sam.drawerlayoutprac.Partner.PartnerFragment;
import com.example.sam.drawerlayoutprac.Partner.PartnerMapFragment;
import com.example.sam.drawerlayoutprac.R;
import com.example.sam.drawerlayoutprac.Util;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_17;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class HotelFragment extends CommonFragment {
    String TAG = "HotelFragment";
    RecyclerView myRvSpot;
    List<HotelGetLowestPriceVO> myHotelGetLowestPriceVOList;
    RecyclerView.Adapter<SpotAdapter.MyViewHolder> myAdapter;
    HotelPriceWebsocket myHotelPriceWebsocket;

    @Override
    public void onResume() {
        super.onResume();
        myHotelGetLowestPriceVOList = getLowestPriceEachHotel();
        showAllHotel();
        URI uri = null;
        try {
            uri = new URI(Common.URL_DYNAMICPRICE);
        } catch (URISyntaxException e) {
            Log.e(ChatFragment.TAG, e.toString());
        }
        myHotelPriceWebsocket = new HotelPriceWebsocket(uri,getActivity());
        if (myHotelPriceWebsocket != null){
            myHotelPriceWebsocket.connect();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup, Bundle bundle) {
        super.onCreateView(inflater, viewGroup, bundle);

        View myLayout = inflater.inflate(R.layout.fragment_hotel, viewGroup, false);
        myRvSpot = (RecyclerView) myLayout.findViewById(R.id.rv_hotel);
        getActivity().findViewById(R.id.floatingBtn).setVisibility(View.VISIBLE);

        myRvSpot.setLayoutManager(new LinearLayoutManager(getActivity()));

        setFloatingBtnClickListener();
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
                myAdapter = new SpotAdapter(getActivity(), myHotelGetLowestPriceVOList);
                myRvSpot.setAdapter(myAdapter);
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    private class SpotAdapter extends RecyclerView.Adapter<SpotAdapter.MyViewHolder> {
        private Context context;
        //private List<HotelVO> myListSpot;
        private List<HotelGetLowestPriceVO> myHotelListInAdapter;
        private LayoutInflater myLayoutInflater;

        public SpotAdapter(Context context, List<HotelGetLowestPriceVO> myHotelList) {
            this.context = context;
            this.myHotelListInAdapter = myHotelList;
            myLayoutInflater = LayoutInflater.from(context);
        }

        // customed ViewHolder - 裝容器用
        public class MyViewHolder extends RecyclerView.ViewHolder {
            ImageView ivImage;
            TextView tvHotel;
            TextView tvPrice;
            // rating stars:
            List<ImageView> stars = new ArrayList<>();


            // 把拿到的到個view的資料一個個存好成實體變數
            public MyViewHolder(View itemView) {
                super(itemView); // 必須使用這個父建構式，因為RecyclerView.ViewHolder沒有空參數的建構式 - 用父類別的實體變數來指向這個View物件,第90行會用到
                float f = (float) 0.6;
                this.ivImage = (ImageView) itemView.findViewById(R.id.ivImage);
                this.tvHotel = (TextView) itemView.findViewById(R.id.tvHotel);
                this.tvPrice = (TextView) itemView.findViewById(R.id.tvPrice);
                this.ivImage.setAlpha(f); // 設定圖片透明度 (float)
                // rating stars:
                stars.add((ImageView) itemView.findViewById(R.id.star1));
                stars.add((ImageView) itemView.findViewById(R.id.star2));
                stars.add((ImageView) itemView.findViewById(R.id.star3));
                stars.add((ImageView) itemView.findViewById(R.id.star4));
                stars.add((ImageView) itemView.findViewById(R.id.star5));

            }
        }

        @Override
        public SpotAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = myLayoutInflater.inflate(R.layout.rv_item_hotel, parent, false);
            return new SpotAdapter.MyViewHolder(view);
        }

        @Override
        public int getItemCount() {
            return myHotelListInAdapter.size();
        }

        @Override
        public void onBindViewHolder(SpotAdapter.MyViewHolder holder, int position) {
            final HotelGetLowestPriceVO hotelListVO = myHotelListInAdapter.get(position);
            final String HotelId = hotelListVO.getHotelId();
            String url = Common.URL + "/android/hotel.do";
            int imageSize = 250;
            new HotelGetImageTask(holder.ivImage).execute(url, HotelId, imageSize);
            holder.tvHotel.setText(hotelListVO.getHotelName());
            holder.tvPrice.setText(hotelListVO.getHotelCheapestRoomPrice());
            // 設定星星數
            Integer starNo = Integer.parseInt(hotelListVO.getHotelRatingResult());
            int index = 0;
            for (; index < starNo; index++) {
                holder.stars.get(index).setVisibility(View.VISIBLE);
            }
            for (; index < 5; index++) {
                holder.stars.get(index).setVisibility(View.INVISIBLE);
            }


//            holder.tvPrice.setText(Integer.toString(mySpot.getPrice()) + "$");
            holder.itemView.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View view) {
                    Fragment fragment = new HotelInfoFragment();
                    Bundle bundle = new Bundle();
                    bundle.putString("hotelId",hotelListVO.getHotelId());
                    //bundle.putSerializable("hotelVO", hotelListVO);
                    fragment.setArguments(bundle);
                    Util.switchFragment(HotelFragment.this, fragment);
                }
            });
        }

    }// end class SpotAdapter



    // onCreateView設定:
    private void setFloatingBtnClickListener() {
        // set up floatingBtn click Listener
        MainActivity.floatingBtn.setVisibility(View.VISIBLE);
        MainActivity.floatingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Util.showToast(getContext(), "ftBtn clicked");
                android.support.v4.app.Fragment fragment = new HotelMapFragment();
                Util.switchFragment(HotelFragment.this, fragment);
            }
        });

    }


    public class HotelPriceWebsocket extends WebSocketClient {
        URI uri;
        Activity activity;

        public HotelPriceWebsocket(URI aUri,Activity aActivity ) {
            super(aUri, new Draft_17());
            this.uri = aUri;
            this.activity = aActivity;
        }

        @Override
        public void onOpen(ServerHandshake handshakedata) {
            Log.d("HotelPriceWebsocket - ", " open websocket successfully ");
        }

        @Override
        public void onMessage(String message) {
            // 不知道為何，必須將websocket放在Fragment下面，才能夠抓到message

            Log.d("HotelPriceWebsocket - ", message);
            Thread myThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Log.d("HotelPriceWebsocket - ", "run on Ui Thread");
                                // 去抓取每間旅館的最低房價，並更新到原本的list上面
                                List<HotelGetLowestPriceVO> tmpHotelList = getLowestPriceEachHotel();
                                for (HotelGetLowestPriceVO myVO_changed: tmpHotelList) {
                                    for (HotelGetLowestPriceVO myVO: HotelFragment.this.myHotelGetLowestPriceVOList) {
                                        if (myVO.getHotelId().equals(myVO_changed.getHotelId())) {
                                            myVO.setHotelCheapestRoomPrice(myVO_changed.getHotelCheapestRoomPrice());
                                        }
                                    }
                                }
                                HotelFragment.this.myAdapter.notifyDataSetChanged();

                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            myThread.start();


        }// end of onMessage

        @Override
        public void onClose(int code, String reason, boolean remote) {

        }

        @Override
        public void onError(Exception ex) {

        }
    }// end HotelPriceWebsocket

    public List<HotelGetLowestPriceVO> getLowestPriceEachHotel(){
        List<HotelGetLowestPriceVO> hotelLowestPriceList = null;
        try {
            HotelSearchVO myVO = new HotelSearchVO();
            myVO.setCity("桃園市");
            myVO.setZone("中壢區");
            myVO.setHotelRatingResult("0");
            myVO.setPrice("$1 - $10000");
            myVO.setRoomCapacity("2");
            hotelLowestPriceList = new HotelGetLowestPriceTask().execute(myVO).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return hotelLowestPriceList;
    }

}
