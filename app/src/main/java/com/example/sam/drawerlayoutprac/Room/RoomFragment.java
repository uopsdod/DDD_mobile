package com.example.sam.drawerlayoutprac.Room;


import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.sam.drawerlayoutprac.Common;
import com.example.sam.drawerlayoutprac.CommonFragment;
import com.example.sam.drawerlayoutprac.Hotel.HotelFragment;
import com.example.sam.drawerlayoutprac.MainActivity;
import com.example.sam.drawerlayoutprac.Member.MemberFragment;
import com.example.sam.drawerlayoutprac.R;
import com.example.sam.drawerlayoutprac.Util;
import com.example.sam.drawerlayoutprac.WishDeleteTask;
import com.example.sam.drawerlayoutprac.WishGetOneTask;
import com.example.sam.drawerlayoutprac.WishInsertTask;
import com.example.sam.drawerlayoutprac.WishVO;

import java.util.List;
import java.util.concurrent.ExecutionException;

public class RoomFragment extends CommonFragment {
    private String TAG = "RoomFragment";
    private TextView tvRoomName, tvFacilitiesDetail, tvPrice, status_text;
    private String RoomId;
    private ImageView imageView, ivLike, ivUnLike;
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
        status_text = (TextView) view.findViewById(R.id.status_text);
        imageView = (ImageView) view.findViewById(R.id.imageView);
        ivLike = (ImageView) view.findViewById(R.id.ivLike);
        ivUnLike = (ImageView) view.findViewById(R.id.ivUnLike);
        rv_RoomImage = (RecyclerView) view.findViewById(R.id.rv_RoomImage);

        //按下中空愛心，會把這間房間加入至當前會員的願望清單，並把圖示變更為實心的愛心
        ivUnLike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ivUnLike.setVisibility(View.INVISIBLE);
                ivLike.setVisibility(View.VISIBLE);
                if (Common.networkConnected(getActivity())) {
                    String url = Common.URL + "/android/Wish/wish.do";
                    String id = MainActivity.pref.getString("memId", null);
                    //判斷使用者是否為會員，是會員就加入至願望清單
                    if (id != null) {
                        try {
                            new WishInsertTask().execute(url, id, RoomId).get();
                        } catch (Exception e) {
                            Log.d(TAG, e.toString());
                        }
                    } else {
                        //若非會員，跳出提示請使用者登入或註冊
                        new AlertDialog.Builder(getActivity())
                                .setCancelable(false) // 讓使用者不能點擊旁邊取消
                                .setTitle("你沒有權限進入此頁")
                                .setMessage("請登入後繼續")
                                .setPositiveButton("現在登入", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
//                            Util.showToast(getContext(),"登入pressed");
                                        MemberFragment.switchFromLoginPage = true;
                                        Util.switchFragment(RoomFragment.this, new MemberFragment());
                                    }
                                })
                                .setNegativeButton("暫時不要", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Util.showToast(getContext(), "暫時不要pressed");
                                        Util.switchFragment(getActivity(), new HotelFragment());
                                    }
                                })
                                .show();
                    }
                }
            }
        });
        //當圖示為實心愛心時，按下實心愛心，圖示會變更成空心愛心，並把這間房間從當前會員的願望清單中移除
        ivLike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ivUnLike.setVisibility(View.VISIBLE);
                ivLike.setVisibility(View.INVISIBLE);
                //把這間房間，從當前會員的用望清單中移除
                if (Common.networkConnected(getActivity())) {
                    String url = Common.URL + "/android/Wish/wish.do";
                    String id = MainActivity.pref.getString("memId", null);
                    try {
                        new WishDeleteTask().execute(url, id, RoomId).get();
                    } catch (Exception e) {
                        Log.d(TAG, e.toString());
                    }
                }
            }
        });
        rv_RoomImage.setLayoutManager(new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.HORIZONTAL));

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        showRoomDetail();
        showAllRoomPhoto();
        showFirstRoomPhoto();
        isLiked();
//        rv_RoomImage.setAdapter(new RoomImageRecyclerViewAdapter(getActivity()));
    }

    //取得房間內的第一張照片
    private void showFirstRoomPhoto() {
        if (Common.networkConnected(getActivity())) {
            String url = Common.URL + "/android/room.do";
            String id = RoomId;
            int imageSize = 250;
            new RoomGetImageTask(imageView).execute(url, id, imageSize);
        }
    }

    //取得房間內的所有照片
    private void showAllRoomPhoto() {
        if (Common.networkConnected(getActivity())) {
            String url = Common.URL + "/android/room.do";
            String id = RoomId;
            List<String> roomVOList = null;
            int imageSize = 250;
            try {
                roomVOList = new RoomGetOneAllPhotoTask().execute(url, id, imageSize).get();
            } catch (Exception e) {
                Log.e(TAG, e.toString());
            }
            if (roomVOList == null || roomVOList.isEmpty()) {
                Util.showToast(getActivity(), "No roomVOList Found!!");
            } else {
                rv_RoomImage.setAdapter(new RoomImageRecyclerViewAdapter(getActivity(), roomVOList));
            }
        }
    }

    //顯示這間房間的細節
    private void showRoomDetail() {
        if (Common.networkConnected(getActivity())) {
            String url = Common.URL + "/android/room.do";
            String id = RoomId;
            RoomVO roomVO = null;
            try {
                roomVO = new RoomGetOneTask().execute(url, id).get();
            } catch (Exception e) {
                Log.e(TAG, e.toString());
            }
            if (roomVO == null) {
                Util.showToast(getActivity(), "No hotel fonnd");
            } else {
                tvRoomName.setText(roomVO.getRoomName());
                if(roomVO.getRoomPrice() == null || roomVO.getRoomPrice().equals(0)){
                    status_text.setVisibility(View.VISIBLE);
                    tvPrice.setVisibility(View.GONE);
                }else{
                    tvPrice.setText(roomVO.getRoomPrice().toString());
                    Util.showToast(getActivity(), "***********************");
                }

                tvFacilitiesDetail.setText(roomVO.getRoomFun() + "\n" + roomVO.getRoomMeal() + "\n" + roomVO.getRoomSleep() + "\n" + roomVO.getRoomFacility() + "\n" + roomVO.getRoomSweetFacility());
            }
        }
    }


    private class RoomImageRecyclerViewAdapter extends RecyclerView.Adapter<RoomImageRecyclerViewAdapter.MyViewHolder> {
        private LayoutInflater layoutInflater;
        private List<String> roomPhotoList;

        public RoomImageRecyclerViewAdapter(Context context, List<String> roomVOList) {
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
            //點擊後，把點選的照片換到上方變成大圖片
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    imageView.setImageBitmap(finalBitmap);
                }
            });
        }

        public class MyViewHolder extends RecyclerView.ViewHolder {
            ImageView rvImage;

            public MyViewHolder(View itemView) {
                super(itemView);
                rvImage = (ImageView) itemView.findViewById(R.id.rvImage);
            }
        }
    }

    //檢查這間房間是否有被會員加入志願望清單
    private void isLiked() {
        if (Common.networkConnected(getActivity())) {
            String url = Common.URL + "/android/Wish/wish.do";
            String id = MainActivity.pref.getString("memId", null);
            WishVO wishVO = null;
            if (id != null) {
                try {
                    wishVO = new WishGetOneTask().execute(url, id, RoomId).get();
                } catch (Exception e) {
                    Log.d(TAG, e.toString());
                }
            }
            if (wishVO == null) {
                //沒有，則顯示中空的愛心
                ivLike.setVisibility(View.INVISIBLE);
                ivUnLike.setVisibility(View.VISIBLE);
            } else if (wishVO.getWishRoomId().equals(RoomId)) {
                //有的話，就顯示實心的愛心
                ivLike.setVisibility(View.VISIBLE);
                ivUnLike.setVisibility(View.INVISIBLE);
            }
        }
    }

}
