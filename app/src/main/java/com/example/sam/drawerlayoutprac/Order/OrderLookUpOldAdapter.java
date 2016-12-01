package com.example.sam.drawerlayoutprac.Order;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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
import com.example.sam.drawerlayoutprac.Hotel.HotelFragment;
import com.example.sam.drawerlayoutprac.Hotel.HotelGetImageTask;
import com.example.sam.drawerlayoutprac.Hotel.HotelGetLowestPriceVO;
import com.example.sam.drawerlayoutprac.Hotel.HotelInfoFragment;
import com.example.sam.drawerlayoutprac.MainActivity;
import com.example.sam.drawerlayoutprac.Partner.VO.MemRepVO;
import com.example.sam.drawerlayoutprac.Partner.VO.OrdVO;
import com.example.sam.drawerlayoutprac.R;
import com.example.sam.drawerlayoutprac.Util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.zip.Inflater;

/**
 * Created by cuser on 2016/11/23.
 */
public class OrderLookUpOldAdapter extends RecyclerView.Adapter<OrderLookUpOldAdapter.MyViewHolder> {
    private Context context;
    private LayoutInflater myLayoutInflater;
    private List<OrdVO> myOrdList;

    public OrderLookUpOldAdapter(Context aContext, List<OrdVO> aOrdList ) {
        this.context = aContext;
        this.myLayoutInflater = LayoutInflater.from(aContext);
        this.myOrdList = aOrdList;
    }

    // customed ViewHolder - 裝容器用
    public class MyViewHolder extends RecyclerView.ViewHolder {
        // 把拿到的到個view的資料一個個存好成實體變數
        TextView ord_hotel_name;
        TextView ord_price;
        TextView ord_status;
        TextView ord_checktime_title;
        TextView ord_checktime;
        ImageView ord_hotel_img;
        Button ord_rating;
        Button ord_report_badhotel;

        public MyViewHolder(View itemView) {
            super(itemView); // 必須使用這個父建構式，因為RecyclerView.ViewHolder沒有空參數的建構式 - 用父類別的實體變數來指向這個View物件,第90行會用到
            this.ord_hotel_name = (TextView) itemView.findViewById(R.id.ord_hotel_name);
            this.ord_price = (TextView) itemView.findViewById(R.id.ord_price);
            this.ord_status = (TextView) itemView.findViewById(R.id.ord_status);
            this.ord_checktime_title = (TextView) itemView.findViewById(R.id.ord_checktime_title);
            this.ord_checktime = (TextView) itemView.findViewById(R.id.ord_checktime);
            this.ord_hotel_img = (ImageView) itemView.findViewById(R.id.ord_hotel_img);
            this.ord_rating = (Button) itemView.findViewById(R.id.ord_rating);
            this.ord_report_badhotel = (Button) itemView.findViewById(R.id.ord_report_badhotel);
        }
    }

    @Override
    public OrderLookUpOldAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = myLayoutInflater.inflate(R.layout.rv_item_ord_old, parent, false);
        return new OrderLookUpOldAdapter.MyViewHolder(view);
    }

    @Override
    public int getItemCount() {
        return this.myOrdList.size();
    }

    @Override
    public void onBindViewHolder(final OrderLookUpOldAdapter.MyViewHolder holder, int position) {
        Log.d("OrderLookUpOldAdapter", "onBindViewHolder");
        final OrdVO ordVO = this.myOrdList.get(position);

        String hotelName = ordVO.getOrdHotelVO().getHotelName();
        if (hotelName.length() > 6){
            hotelName = hotelName.substring(0,5) + "..";
        }
        holder.ord_hotel_name.setText(hotelName);
        holder.ord_price.setText("$" + Integer.toString(ordVO.getOrdPrice()));
        // 訂單狀態
        String ordStatus = OrderLookUpFragment.ordStatusConverter.get(ordVO.getOrdStatus());
        holder.ord_status.setText(ordStatus);
        giveStatusColor(holder.ord_status,ordStatus);
        if ("已入住".equals(ordStatus)){
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            if (ordVO.getOrdLiveDate() != null){
                holder.ord_checktime.setText(df.format(ordVO.getOrdLiveDate()));
            }else{
                holder.ord_checktime.setText("日期更新中");
            }

        }else{
            holder.ord_checktime_title.setVisibility(View.INVISIBLE);
            holder.ord_checktime.setVisibility(View.INVISIBLE);
        }
        // 設定圖片
        final String HotelId = ordVO.getOrdHotelVO().getHotelId();
        String url = Common.URL + "/android/hotel.do";
        int imageSize = 850;
        new HotelGetImageTask(holder.ord_hotel_img).execute(url, HotelId, imageSize);
        // 給予評價
        Log.d("OrderLookUpOldAdapter", "ordVO.getOrdRatingStarNo() - " + ordVO.getOrdRatingStarNo());
        if ("已入住".equals(ordStatus) && ordVO.getOrdRatingStarNo() == null) {
            holder.ord_rating.setText("給予評價");
            //holder.ord_rating.setPadding();
            holder.ord_rating.setCompoundDrawablesWithIntrinsicBounds( R.drawable.star_golden_24dp, 0, 0, 0);
            holder.ord_rating.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context,OrdLookUpOldRatingActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("ordId", ordVO.getOrdId());
                    intent.putExtras(bundle);
                    context.startActivity(intent);
                }
            });
        }else if("已入住".equals(ordStatus) && ordVO.getOrdRatingStarNo() != null){
            holder.ord_rating.setText("已評價");
            holder.ord_rating.setPressed(true);
            holder.ord_rating.setEnabled(false);
        }else{
            holder.ord_rating.setText("訂單取消");
            holder.ord_rating.setPressed(true);
            holder.ord_rating.setEnabled(false);
        }

        // 檢舉：
        MemRepVO memRepVO = null;
        //String ordId = "2016111003";
        String ordId = ordVO.getOrdId();
        Log.d("OrderLookUpOldAdapter", "ordId - " + ordId);
        try {
            memRepVO = new MemRepGetOneTextViaOrdIdTask(this.context).execute(ordId).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        Log.d("OrderLookUpOldAdapter","ordStatus: " + ordStatus);
        Log.d("OrderLookUpOldAdapter","memRepVO:=" + (memRepVO==null));
//        Log.d("OrderLookUpOldAdapter","memRepVO: " + memRepVO.getMemRepContent());
        if ("已入住".equals(ordStatus) && memRepVO == null) {
            holder.ord_report_badhotel.setText("檢舉廠商");
            //holder.ord_rating.setPadding();
            holder.ord_report_badhotel.setCompoundDrawablesWithIntrinsicBounds( R.drawable.stop_red_24dp, 0, 0, 0);
            holder.ord_report_badhotel.setEnabled(true);
            holder.ord_report_badhotel.setPressed(false);
            holder.ord_report_badhotel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Util.showToast(context,"檢舉廠商 clicked");

                    Intent intent = new Intent(context,OrdLookUpOldReportActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("ordId", ordVO.getOrdId());
                    intent.putExtras(bundle);
                    context.startActivity(intent);
                }
            });
        }else if ("已入住".equals(ordStatus)){
            Log.d("OrderLookUpOldAdapter","已檢舉");
            holder.ord_report_badhotel.setText("已檢舉");
            holder.ord_report_badhotel.setPressed(true);
            holder.ord_report_badhotel.setEnabled(false);
        }else{
            holder.ord_report_badhotel.setText("訂單取消");
            holder.ord_report_badhotel.setPressed(true);
            holder.ord_report_badhotel.setEnabled(false);
        }


    }

    private void giveStatusColor(TextView aTextView,String aStatus){
        switch (aStatus){
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



}// end class SpotAdapter