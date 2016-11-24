package com.example.sam.drawerlayoutprac;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.sam.drawerlayoutprac.Room.RoomFragment;
import com.example.sam.drawerlayoutprac.Room.RoomVO;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft;
import org.java_websocket.drafts.Draft_17;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;


/**
 * Created by cuser on 2016/11/17.
 */

public class MemberWishFragment extends MustLoginFragment {
    String TAG = "MemWishFragment";
    RecyclerView myRvWish;
    List<RoomVO> roomVOList = null;
    private  MemWishAdapter myAdapter;
    private MemWishAdapter.RoomPriceWebSocket roomPriceWebSocket;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_member_wish, container, false);
        myRvWish = (RecyclerView) view.findViewById(R.id.rv_hotel);
        getActivity().findViewById(R.id.floatingBtn).setVisibility(View.INVISIBLE);

        myRvWish.setLayoutManager(new LinearLayoutManager(getActivity()));
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        showAllWish();
    }

    @Override
    public void onPause() {
        super.onPause();
        if(roomPriceWebSocket != null){
            roomPriceWebSocket.close();
        }
    }

    private void showAllWish() {

        if (Common.networkConnected(getActivity())) {

            String url = Common.URL + "/android/Wish/wish.do";
            String id = MainActivity.pref.getString("memId", null);
            if (id != null) {
                try {
                    roomVOList = new WishGetAllTask().execute(url, id).get();
                } catch (Exception e) {
                    Log.d(TAG, e.toString());
                }
                if (roomVOList == null || roomVOList.isEmpty()) {
                    Util.showToast(getActivity(), "wishVO not found!!");
                } else {
                    myAdapter = new MemWishAdapter(getActivity(), roomVOList);
                    myRvWish.setAdapter(myAdapter);
                }
            } else {
                Util.showToast(getActivity(), "You need login mother fucker!");
            }
        }
    }





    private  class MemWishAdapter extends RecyclerView.Adapter<MemWishAdapter.MyViewHolder> {
        private Context context;
        private List<RoomVO> myWishVO;
        private LayoutInflater layoutInflater;

        public MemWishAdapter(Context context, List<RoomVO> myWishVO) {
            this.context = context;
            this.myWishVO = myWishVO;
            layoutInflater = LayoutInflater.from(context);
            getDynamicPrice();
        }

        public class MyViewHolder extends RecyclerView.ViewHolder {
            ImageView ivImage;
            TextView tvHotel;
            TextView tvPrice;
            Button btDelete;
            float f = 0.7f;
            public MyViewHolder(View itemView) {
                super(itemView);
                this.ivImage = (ImageView) itemView.findViewById(R.id.ivImage);
                this.tvHotel = (TextView) itemView.findViewById(R.id.tvHotel);
                this.tvPrice = (TextView) itemView.findViewById(R.id.tvPrice);
                this.btDelete = (Button) itemView.findViewById(R.id.btDelete);
                this.ivImage.setAlpha(f);
            }
        }

        @Override
        public MemWishAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = layoutInflater.inflate(R.layout.rv_item_wish, parent, false);
            return new MemWishAdapter.MyViewHolder(view);
        }

        @Override
        public int getItemCount() {
            return myWishVO.size();
        }

        @Override
        public void onBindViewHolder(MemWishAdapter.MyViewHolder holder, final int position) {
            RoomVO roomVO = myWishVO.get(position);
            final String roomId = roomVO.getRoomId();
            final String id = MainActivity.pref.getString("memId", null);
            final String url = Common.URL + "/android/Wish/wish.do";
            int imageSize = 250;

            if (id != null) {
                new WishGetImageTask(holder.ivImage).execute(url, roomId, imageSize);
                holder.tvHotel.setText(roomVO.getRoomName());
                if(roomVO.getRoomPrice().equals(0)){
                    holder.tvPrice.setText("今日尚未上架本房間");
//                    holder.tvPrice.setTextColor(getResources().getColor(R.color.notice_color)); //設顯示文字為紅色
                    holder.tvPrice.setTextColor(Color.parseColor("#FF0000")); //設顯示文字為紅色
                }else{
                    holder.tvPrice.setText(roomVO.getRoomPrice().toString());
                }
                holder.btDelete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        new WishDeleteTask().execute(url,id, roomId);
                        myWishVO.remove(myWishVO.get(position)); //每次刪除一個，就從myWishVO裡面移除選取的目標
                        myAdapter.notifyDataSetChanged(); // 當myWishVO有被更動(新增、修改、刪除) 時，就重新刷新一次頁面
                    }
                });
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Fragment fragment = new RoomFragment();
                        Bundle bundle = new Bundle();
                        bundle.putSerializable("RoomId", roomId);
                        fragment.setArguments(bundle);
                        Util.switchFragment(MemberWishFragment.this, fragment);
                    }
                });
            } else {
                Util.showToast(getContext(), "You need login mother fucker!");
            }
        }

        private void getDynamicPrice() {
            URI uri = null;
            try {
                uri = new URI(Common.URL_DYNAMICPRICE);
            } catch (URISyntaxException e) {
                Log.d(TAG, e.toString());
            }
            roomPriceWebSocket = new MemWishAdapter.RoomPriceWebSocket(uri, getActivity());
            if(roomPriceWebSocket != null){
                roomPriceWebSocket.connect();
            }
        }

        private  class RoomPriceWebSocket extends WebSocketClient{
            URI uri;
            Activity activity;

            public RoomPriceWebSocket(URI serverUri, Activity activity) {
                super(serverUri, new Draft_17());
                uri =serverUri;
                this.activity = activity;
            }

            @Override
            public void onOpen(ServerHandshake handshakedata) {
                Log.d("MemberWishFragment - ", " open websocket successfully ");
            }

            @Override
            public void onMessage(final String message) {
                Thread myThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                try{
                                    JSONObject jsonObject = new JSONObject(message);
                                    JSONArray jsonArray = jsonObject.getJSONArray("Bag");
                                    JSONArray jsonArrayPrice;
                                    String id = null;
//                                for(int i = 0; i<roomVOList.size(); i++){
//                                    RoomVO roomVO = roomVOList.get(i);
//                                    for(int j = 0; j<jsonArray.length(); j++){
//                                        jsonArrayPrice = jsonArray.getJSONArray(j);
//                                        if(jsonArrayPrice.get(0).equals(roomVO.getRoomId())){
//                                            roomVO.setRoomPrice((Integer) jsonArrayPrice.get(1));
//                                        }
//                                    }
                                    for(int j = 0; j<jsonArray.length(); j++){
                                        jsonArrayPrice = jsonArray.getJSONArray(j);
                                        for(RoomVO roomVO : myWishVO){
                                            id = roomVO.getRoomId();
                                            if(jsonArrayPrice.get(0).equals(id) && !id.isEmpty()){
                                                roomVO.setRoomPrice((Integer) jsonArrayPrice.get(1));
                                                myAdapter.notifyDataSetChanged();
                                                MyViewHolder holder = new MyViewHolder(getView());
                                                holder.tvPrice.setText(roomVO.getRoomPrice().toString());
                                            }
                                        }
                                    }

                                }catch (JSONException e){
                                    Util.showToast(getContext(), e.toString());
                                }

                            }
                        });
                    }
                });
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
}