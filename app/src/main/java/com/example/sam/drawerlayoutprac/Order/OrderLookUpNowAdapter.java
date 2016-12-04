package com.example.sam.drawerlayoutprac.Order;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.sam.drawerlayoutprac.BuildConfig;
import com.example.sam.drawerlayoutprac.Common;
import com.example.sam.drawerlayoutprac.Hotel.HotelGetImageTask;
import com.example.sam.drawerlayoutprac.MainActivity;
import com.example.sam.drawerlayoutprac.Partner.VO.MemRepVO;
import com.example.sam.drawerlayoutprac.Partner.VO.OrdVO;
import com.example.sam.drawerlayoutprac.R;
import com.example.sam.drawerlayoutprac.Util;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * Created by cuser on 2016/11/23.
 */
public class OrderLookUpNowAdapter extends RecyclerView.Adapter<OrderLookUpNowAdapter.MyViewHolder> {
    private static final String TAG = "OrderLookUpNowAdapter";
    private static final int serverTimeZoneHour = 8;
    private Activity activity;
    private Context context;
    private LayoutInflater myLayoutInflater;
    private List<OrdVO> myOrdList;
    //private static long ordDuration; // millesecond

    private FirebaseRemoteConfig mFirebaseRemoteConfig;

    public OrderLookUpNowAdapter(Context aContext, List<OrdVO> aOrdList, Activity aActivity) {
        this.context = aContext;
        this.myLayoutInflater = LayoutInflater.from(aContext);
        this.myOrdList = aOrdList;
        this.activity = aActivity;
        //getOrdDuration();
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

        //啟動倒數:
        activateCountDown(ordVO,holder);

        // 開始將data bind 到 view 上面:
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
        holder.ord_check_qr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Util.showToast(context,"qr_check clicked");
                Intent intent = new Intent(context,OrdLookUpNowCheckQRActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("ordId", ordVO.getOrdId());
                intent.putExtras(bundle);
                context.startActivity(intent);
            }
        });

        // 取消訂單
        holder.ord_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Util.showToast(context,"ord_cancel clicked");
                Intent intent = new Intent(context,OrdLookUpNowCancelActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("ordId", ordVO.getOrdId());
                intent.putExtras(bundle);
                context.startActivity(intent);
            }
        });
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


    private void activateCountDown(OrdVO aOrdVO, OrderLookUpNowAdapter.MyViewHolder aHolder) {
        // 完成項目: 可動態依據客戶端timezone，進行倒數
        // 完成一半項目: 尚且無法確切得知server是否有使用DST，目前只是暫時先用土法方法解決
        // 未完成項目: 尚且無法動態抓取server的 timezone hours

        // 注意 - 記得調整 private static final int serverTimeZoneHour = ?; 要對到Server的timezone
        Log.d("timeTest-server"," Timezone " + OrderLookUpNowAdapter.serverTimeZoneHour);

        // 取得終止時間：
        Long finalTime = aOrdVO.getOrdDate().getTime() + OrderLookUpFragment.ordDuration;
        //Log.d("timeTest-finalTime",""+aOrdVO.getOrdDate().getTime());
        Log.d("timeTest-finalTime" , ""+TimeUnit.HOURS.convert(aOrdVO.getOrdDate().getTime(), TimeUnit.MILLISECONDS) + " hours)");
        // end of 取得終止時間

        // 取得currTime - 此時間會是UTC+客戶端timezone時間
        long currTime = new java.util.Date().getTime();
        //Log.d("timeTest-currTimeZone" , ""+currTime+" milliseconds");
        Log.d("timeTest-currTimeZone" , ""+TimeUnit.HOURS.convert(currTime, TimeUnit.MILLISECONDS) + " hours)");
        // end of 取得currTime

        // 取得timeToAlter時間 - 此時間為客戶端timezone hours和server端timezone hours的差距:
        long timeToAlter = getMillisecondsToAlter();
        //Log.d("timeTest-timeToAlter" , ""+timeToAlter+" milliseconds");
        Log.d("timeTest-timeToAlter" , ""+TimeUnit.HOURS.convert(timeToAlter, TimeUnit.MILLISECONDS) + " hours)");
        // end of 取得timeToAlter時間

        // 修改currTime - 為了要讓本地時間能取的跟server端同一個時區中的毫秒數，且還保留著當下時間的精確毫秒數
        currTime += timeToAlter; // 減之前要加上去的
        // end of 修改currTime

        // 取得offset - 用在最後要在holder.countdown_time.setText(df.format(remainedTime-totalOffset));
        // 因為在使用者的機器上，就算最後給6000毫秒，系統還是會自動加上他的timezone hours
        // 為了要避免此情況發生，所以要在最後顯示時扣掉客戶端設備的timezone hours:
        Calendar myCalendar = new GregorianCalendar();
        int gmtZoneOffset = myCalendar.get(Calendar.ZONE_OFFSET);
        int gmtDSTOffset = myCalendar.get(Calendar.DST_OFFSET);
        long totalOffset = gmtZoneOffset + gmtDSTOffset;
        //Log.d("timeTest-offSet" , ""+totalOffset+" milliseconds");
        Log.d("timeTest-offSet" , ""+TimeUnit.HOURS.convert(totalOffset, TimeUnit.MILLISECONDS) + " hours)");
        // end of 取得offset

        // 計算剩餘時間
        Long remainedTime = Math.abs(finalTime - currTime);
        // end of 計算剩餘時間

        // deal with DST(日光節省時間):
        if (remainedTime > 3600000){
            remainedTime -= 3600000;
            Log.d("timeTest-DST" , ""+"因為DST，減掉一個小時");
        }
        //Log.d("timeTest-remainedTime" , ""+remainedTime+" milliseconds");
        Log.d("timeTest-remainedTime" , ""+TimeUnit.HOURS.convert(remainedTime, TimeUnit.MILLISECONDS) + " hours)");
        // end of deal with DST(日光節省時間)

        // 第一次秀出倒數時間
        DateFormat df = new SimpleDateFormat("HH:mm:ss"); // HH 24小時
        aHolder.countdown_time.setText(df.format(remainedTime-totalOffset));
        // end of 第一次秀出倒數時間

        // 啟動倒數Thread
        Thread myThread = new Thread(new myRunnable(remainedTime,aHolder,totalOffset));
        myThread.start();
        // end of 啟動倒數Thread

        // 拿現在時間:
//        String currTimeZone = getCurrTimeZone();
//        Calendar cSchedStartCal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
//        long gmtTime = cSchedStartCal.getTime().getTime();
//        long timezoneAlteredTime = gmtTime + TimeZone.getTimeZone(currTimeZone).getRawOffset();
//        Calendar cSchedStartCal1 = Calendar.getInstance(TimeZone.getTimeZone(currTimeZone));
//        cSchedStartCal1.setTimeInMillis(timezoneAlteredTime);
//        long currTime = cSchedStartCal1.getTime().getTime();

    }// end of activateCountDown() method

    private class myRunnable implements Runnable {
        Long remainedTime;
        OrderLookUpNowAdapter.MyViewHolder holder;
        long totalOffset;

        public myRunnable(Long aRemainedTime, OrderLookUpNowAdapter.MyViewHolder aHolder, long aTotalOffset) {
            remainedTime = aRemainedTime;
            holder = aHolder;
            totalOffset = aTotalOffset;
        }
        @Override
        public void run() {
            final DateFormat df = new SimpleDateFormat("HH:mm:ss"); // HH 24小時
            while(remainedTime > 1000){
                Log.d("timeTest-remainedTime", ""+remainedTime);
                Log.d("timeTest-remainedTime" , ""+TimeUnit.HOURS.convert(remainedTime, TimeUnit.MILLISECONDS) + " hours)");
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Log.e("OrderLookUpNowAdapter", e.toString());
                }
                holder.countdown_time.post(new Runnable() {
                    @Override
                    public void run() {
                        holder.countdown_time.setText(df.format(remainedTime-totalOffset));
                    }
                });

                remainedTime -= 1000L;
            }// end of while
            holder.countdown_time.post(new Runnable() {
                @Override
                public void run() {
                    holder.countdown_time.setText(df.format(0L-totalOffset));
                }
            });
            holder.ord_status.post(new Runnable() {
                @Override
                public void run() {
                    OrderLookUpFragment.isFreshNeeded = true;
                    holder.ord_status.setText("逾時取消");
                    giveStatusColor(holder.ord_status, "逾時取消");
                    //holder.ord_check_qr.setCompoundDrawables(context.getResources().getDrawable(android.R.drawable.ic_dialog_email),null,null,null);
                    holder.ord_check_qr.setCompoundDrawablesWithIntrinsicBounds( R.drawable.qr_codes_grey_24dp, 0, 0, 0);
                    holder.ord_check_qr.setEnabled(false);
                    holder.ord_cancel.setCompoundDrawablesWithIntrinsicBounds( R.drawable.cancel_grey_24dp, 0, 0, 0);
                    holder.ord_cancel.setEnabled(false);

                }
            });


        }
    }// end of Runnable

    private long getMillisecondsToAlter(){
        Calendar cal = Calendar.getInstance();
        long milliDiff = cal.get(Calendar.ZONE_OFFSET) + cal.get(Calendar.DST_OFFSET);
        int houdDiff = (int)TimeUnit.HOURS.convert(milliDiff, TimeUnit.MILLISECONDS);
        Log.d("timeTest","houdDiff - " + houdDiff);
        // Got local offset, now loop through available timezone id(s).
        long hourToAlter = OrderLookUpNowAdapter.serverTimeZoneHour - houdDiff;
//        switch (houdDiff){
//            case 3:
//                hourToAlter = 5;
//                break;
//            case 4:
//                hourToAlter = 4;
//                break;
//            case 8:
//                hourToAlter = 0;
//                break;
//        }
        long millisecondToAlter = hourToAlter * 3600000;
        return millisecondToAlter;
    }// end of getMillisecondsToAlter() method



}// end class SpotAdapter