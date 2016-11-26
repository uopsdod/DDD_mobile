package com.example.sam.drawerlayoutprac.Order;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.sam.drawerlayoutprac.Common;
import com.example.sam.drawerlayoutprac.Hotel.HotelGetImageTask;
import com.example.sam.drawerlayoutprac.Partner.VO.MemRepVO;
import com.example.sam.drawerlayoutprac.Partner.VO.OrdVO;
import com.example.sam.drawerlayoutprac.R;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.ExecutionException;

/**
 * Created by cuser on 2016/11/23.
 */
public class OrderLookUpNowAdapter extends RecyclerView.Adapter<OrderLookUpNowAdapter.MyViewHolder> {
    private Context context;
    private LayoutInflater myLayoutInflater;
    private List<OrdVO> myOrdList;
    private static final long ordDuration = 60000; // millesecond

    public OrderLookUpNowAdapter(Context aContext, List<OrdVO> aOrdList) {
        this.context = aContext;
        this.myLayoutInflater = LayoutInflater.from(aContext);
        this.myOrdList = aOrdList;
    }

    // customed ViewHolder - 裝容器用
    public class MyViewHolder extends RecyclerView.ViewHolder {
        // 把拿到的到個view的資料一個個存好成實體變數
        TextView ord_hotel_name;
        TextView ord_room_name;
        TextView ord_price;
        TextView ord_status;
        TextView countdown_time;
        ImageView ord_hotel_img;
        Button ord_check_qr;
        Button ord_cancel;

        public MyViewHolder(View itemView) {
            super(itemView); // 必須使用這個父建構式，因為RecyclerView.ViewHolder沒有空參數的建構式 - 用父類別的實體變數來指向這個View物件,第90行會用到
            this.ord_hotel_name = (TextView) itemView.findViewById(R.id.ord_hotel_name);
            this.ord_room_name = (TextView) itemView.findViewById(R.id.ord_room_name);
            this.ord_price = (TextView) itemView.findViewById(R.id.ord_price);
            this.ord_status = (TextView) itemView.findViewById(R.id.ord_status);
            this.countdown_time = (TextView) itemView.findViewById(R.id.countdown_time);
            this.ord_hotel_img = (ImageView) itemView.findViewById(R.id.ord_hotel_img);
            this.ord_check_qr = (Button) itemView.findViewById(R.id.ord_check_qr);
            this.ord_cancel = (Button) itemView.findViewById(R.id.ord_cancel);
        }
    }

    @Override
    public OrderLookUpNowAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = myLayoutInflater.inflate(R.layout.rv_item_ord_now, parent, false);
        return new OrderLookUpNowAdapter.MyViewHolder(view);
    }

    @Override
    public int getItemCount() {
        return this.myOrdList.size();
    }

    @Override
    public void onBindViewHolder(final OrderLookUpNowAdapter.MyViewHolder holder, int position) {
        Log.d("OrderLookUpNowAdapter", "onBindViewHolder");
        final OrdVO ordVO = this.myOrdList.get(position);

        // 計算剩餘時間：
        Long finalTime = ordVO.getOrdDate().getTime() + OrderLookUpNowAdapter.ordDuration;
            // 拿現在時間:
        Calendar cSchedStartCal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        long gmtTime = cSchedStartCal.getTime().getTime();
        long timezoneAlteredTime = gmtTime + TimeZone.getTimeZone("Asia/Taipei").getRawOffset();
        Calendar cSchedStartCal1 = Calendar.getInstance(TimeZone.getTimeZone("Asia/Taipei"));
        cSchedStartCal1.setTimeInMillis(timezoneAlteredTime);
        long currTime = cSchedStartCal1.getTime().getTime();
            // end of 拿現在時間
        Long remainedTime = null;
        if (finalTime > currTime) {
            remainedTime = finalTime - currTime;
        }else{
            remainedTime = 0L;
        }
        //Log.d("OrderLookUpNowAdapter","remainedTime: " + remainedTime);

        DateFormat df = new SimpleDateFormat("HH:mm:ss"); // HH 24小時
        holder.countdown_time.setText(df.format(remainedTime));
        Thread myThread = new Thread(new myRunnable(remainedTime,holder.countdown_time));
        myThread.start();


        String hotelName = ordVO.getOrdHotelVO().getHotelName();
        if (hotelName.length() > 6) {
            hotelName = hotelName.substring(0, 5) + "..";
        }
        holder.ord_hotel_name.setText(hotelName);
        Log.d("OrderLookUpNowAdapter", "ordVO.getOrdRoomVO().getRoomName() - " + ordVO.getOrdRoomVO().getRoomName());
        holder.ord_room_name.setText(ordVO.getOrdRoomVO().getRoomName());
        holder.ord_price.setText("$" + Integer.toString(ordVO.getOrdPrice()));
        // 訂單狀態
        String ordStatus = OrderLookUpFragment.ordStatusConverter.get(ordVO.getOrdStatus());
        holder.ord_status.setText(ordStatus);
        giveStatusColor(holder.ord_status, ordStatus);


//        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//        holder.ord_checktime.setText(df.format(ordVO.getOrdLiveDate()));
        // 設定圖片
        final String HotelId = ordVO.getOrdHotelVO().getHotelId();
        String url = Common.URL + "/android/hotel.do";
        int imageSize = 850;
        new HotelGetImageTask(holder.ord_hotel_img).execute(url, HotelId, imageSize);
        // 查看QR Code:
//        if (ordVO.getOrdRatingStarNo() == null) {
//            holder.ord_rating.setText("給予評價");
//            //holder.ord_rating.setPadding();
//            holder.ord_rating.setCompoundDrawablesWithIntrinsicBounds( R.drawable.star_golden_24dp, 0, 0, 0);
//            holder.ord_rating.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    Intent intent = new Intent(context,OrdLookUpOldRatingActivity.class);
//                    Bundle bundle = new Bundle();
//                    bundle.putString("ordId", ordVO.getOrdId());
//                    intent.putExtras(bundle);
//                    context.startActivity(intent);
//                }
//            });
//        }else{
//            holder.ord_rating.setText("已評價");
//            holder.ord_rating.setPressed(true);
//            holder.ord_rating.setEnabled(false);
//        }

        // 取消訂單
//        MemRepVO memRepVO = null;
//        //String ordId = "2016111003";
//        String ordId = ordVO.getOrdId();
//        //Log.d("OrderLookUpNowAdapter", "ordId - " + ordId);
//        try {
//            memRepVO = new MemRepGetOneTextViaOrdIdTask(this.context).execute(ordId).get();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        } catch (ExecutionException e) {
//            e.printStackTrace();
//        }
//
//        if (memRepVO == null) {
//            holder.ord_report_badhotel.setText("檢舉廠商");
//            //holder.ord_rating.setPadding();
//            holder.ord_report_badhotel.setCompoundDrawablesWithIntrinsicBounds( R.drawable.stop_red_24dp, 0, 0, 0);
//            holder.ord_report_badhotel.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    //Util.showToast(context,"檢舉廠商 clicked");
//
//                    Intent intent = new Intent(context,OrdLookUpOldReportActivity.class);
//                    Bundle bundle = new Bundle();
//                    bundle.putString("ordId", ordVO.getOrdId());
//                    intent.putExtras(bundle);
//                    context.startActivity(intent);
//                }
//            });
//        }else{
//            holder.ord_report_badhotel.setText("已檢舉");
//            holder.ord_report_badhotel.setPressed(true);
//            holder.ord_report_badhotel.setEnabled(false);
//        }


    }

    private void giveStatusColor(TextView aTextView, String aStatus) {
        switch (aStatus) {
            case "已下單":
                aTextView.setTextColor(ContextCompat.getColor(context, R.color.green));
                break;
            case "主動取消":
                aTextView.setTextColor(ContextCompat.getColor(context, R.color.red));
                break;
            case "已入住":
                aTextView.setTextColor(ContextCompat.getColor(context, R.color.sub1_color));
                break;
            case "已繳費":
                aTextView.setTextColor(ContextCompat.getColor(context, R.color.sub1_color));
                break;
            case "逾時取消":
                aTextView.setTextColor(ContextCompat.getColor(context, R.color.red));
                break;
        }
    }

    private class myRunnable implements Runnable {
        Long remainedTime;
        TextView remainedTimeView;
//        private boolean isContinue = true;
//
//        public void terminate() {
//            isContinue = false;
//        }

        public myRunnable(Long aRemainedTime, TextView aRemainedTimeView) {
            remainedTime = aRemainedTime;
            remainedTimeView = aRemainedTimeView;
        }
        @Override
        public void run() {
            final DateFormat df = new SimpleDateFormat("HH:mm:ss"); // HH 24小時
            while(remainedTime > 1000){
                Log.d("OrderLookUpNowAdapter","remainedTime - " + remainedTime);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Log.e("OrderLookUpNowAdapter", e.toString());
                }
                remainedTimeView.post(new Runnable() {
                    @Override
                    public void run() {
                        remainedTimeView.setText(df.format(remainedTime));
                    }
                });

                remainedTime -= 1000L;
            }// end of while
            remainedTimeView.post(new Runnable() {
                @Override
                public void run() {
                    remainedTimeView.setText(df.format(0L));
                }
            });


        }
    }

}// end class SpotAdapter