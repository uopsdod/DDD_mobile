package com.example.sam.drawerlayoutprac;

import android.content.Context;
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

import java.util.List;


/**
 * Created by cuser on 2016/11/17.
 */

public class MemberWishFragment extends MustLoginFragment {
    String TAG = "MemWishFragment";
    RecyclerView myRvWish;
    List<RoomVO> roomVOList = null;
    TextView status_text;
    private MemWishAdapter myAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_member_wish, container, false);
        status_text = (TextView) view.findViewById(R.id.status_text);
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
                    holder.tvPrice.setTextColor(getResources().getColor(R.color.notice_color));
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
                Util.showToast(getActivity(), "You need login mother fucker!");
            }
        }

    }
}