package com.example.sam.drawerlayoutprac.Order;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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
import com.example.sam.drawerlayoutprac.Partner.VO.OrdVO;
import com.example.sam.drawerlayoutprac.R;
import com.example.sam.drawerlayoutprac.Util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
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
        TextView ord_checktime;
        ImageView ord_hotel_img;
        Button ord_rating;
        Button ord_report_badhotel;

        public MyViewHolder(View itemView) {
            super(itemView); // 必須使用這個父建構式，因為RecyclerView.ViewHolder沒有空參數的建構式 - 用父類別的實體變數來指向這個View物件,第90行會用到
            this.ord_hotel_name = (TextView) itemView.findViewById(R.id.ord_hotel_name);
            this.ord_price = (TextView) itemView.findViewById(R.id.ord_price);
            this.ord_status = (TextView) itemView.findViewById(R.id.ord_status);
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
    public void onBindViewHolder(OrderLookUpOldAdapter.MyViewHolder holder, int position) {
        Log.d("OrderLookUpOldAdapter", "onBindViewHolder");
        final OrdVO ordVO = this.myOrdList.get(position);

        String hotelName = ordVO.getOrdHotelVO().getHotelName();
        if (hotelName.length() > 6){
            hotelName = hotelName.substring(0,5) + "..";
        }
        holder.ord_hotel_name.setText(hotelName);
        holder.ord_price.setText("$" + Integer.toString(ordVO.getOrdPrice()));
        String ordStatus = OrderLookUpFragment.ordStatusConverter.get(ordVO.getOrdStatus());
        holder.ord_status.setText(ordStatus);
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        holder.ord_checktime.setText(df.format(ordVO.getOrdLiveDate()));
        // 設定圖片
        final String HotelId = ordVO.getOrdHotelVO().getHotelId();
        String url = Common.URL + "/android/hotel.do";
        int imageSize = 850;
        new HotelGetImageTask(holder.ord_hotel_img).execute(url, HotelId, imageSize);



//        final String HotelId = hotelListVO.getHotelId();
//        String url = Common.URL + "/android/hotel.do";
//        int imageSize = 250;
//        new XXXXGetAllTask(holder.ivImage).execute(url, HotelId, imageSize);
//        holder.tvHotel.setText(hotelListVO.getHotelName());
//        holder.tvPrice.setText(hotelListVO.getHotelCheapestRoomPrice());
    }

}// end class SpotAdapter