package com.example.sam.drawerlayoutprac;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.sam.drawerlayoutprac.Hotel.HotelFragment;
import com.example.sam.drawerlayoutprac.Room.RoomFragment;
import com.example.sam.drawerlayoutprac.Room.RoomVO;

import java.util.List;
import java.util.concurrent.ExecutionException;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by cuser on 2016/11/17.
 */

public class MemberWishFragment extends MustLoginFragment {
    String TAG = "MemWishFragment";
    RecyclerView myRvWish;

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

    private void showAllWish() {

        if (Common.networkConnected(getActivity())) {
//            SharedPreferences pref = getActivity().getSharedPreferences(Common.PREF_FILE,
//                    MODE_PRIVATE);
            String url = Common.URL + "/android/Wish/wish.do";
            String id = MainActivity.pref.getString("memId", null);
            List<RoomVO> roomVO = null;
            if (id != null) {
                try {
                    roomVO = new WishGetAllTask().execute(url, id).get();
                } catch (Exception e) {
                    Log.d(TAG, e.toString());
                }
                if (roomVO == null || roomVO.isEmpty()) {
                    Util.showToast(getActivity(), "wishVO not found!!");
                } else {
                    myRvWish.setAdapter(new MemWishAdapter(getActivity(), roomVO));
                }
            } else {
                Util.showToast(getActivity(), "You need login mother fucker!");
            }
        }
    }

    private class MemWishAdapter extends RecyclerView.Adapter<MemWishAdapter.MyViewHolder> {
        private Context context;
        private List<RoomVO> myWishVO;
        private LayoutInflater layoutInflater;

        public MemWishAdapter(Context context, List<RoomVO> myWishVO) {
            this.context = context;
            this.myWishVO = myWishVO;
            layoutInflater = LayoutInflater.from(context);
        }

        public class MyViewHolder extends RecyclerView.ViewHolder {
            ImageView ivImage;
            TextView tvHotel;
            TextView tvPrice;

            public MyViewHolder(View itemView) {
                super(itemView);
                this.ivImage = (ImageView) itemView.findViewById(R.id.ivImage);
                this.tvHotel = (TextView) itemView.findViewById(R.id.tvHotel);
                this.tvPrice = (TextView) itemView.findViewById(R.id.tvPrice);
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
        public void onBindViewHolder(MemWishAdapter.MyViewHolder holder, int position) {
//            SharedPreferences pref = getActivity().getSharedPreferences(Common.PREF_FILE,
//                    MODE_PRIVATE);
            RoomVO roomVO = myWishVO.get(position);
            String id = MainActivity.pref.getString("memId", null);
            String url = Common.URL + "/android/Wish/wish.do";
            int imageSize = 250;
            if (id != null) {
                new WishGetImageTask(holder.ivImage).execute(url, id, imageSize);
                holder.tvHotel.setText(roomVO.getRoomName());
                holder.tvPrice.setText(roomVO.getRoomPrice().toString());
            } else {
                Util.showToast(getActivity(), "You need login mother fucker!");
            }
        }

    }
}