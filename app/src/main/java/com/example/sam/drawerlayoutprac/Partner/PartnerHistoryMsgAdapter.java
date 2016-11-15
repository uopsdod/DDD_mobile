package com.example.sam.drawerlayoutprac.Partner;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.sam.drawerlayoutprac.Common;
import com.example.sam.drawerlayoutprac.R;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Created by cuser on 2016/11/13.
 */

public class PartnerHistoryMsgAdapter extends BaseAdapter {
    private Context context;
    private List<PartnerMsg> partnerMsgList;

    public PartnerHistoryMsgAdapter(Context aContext, List<PartnerMsg> aMyList) {
        this.context = aContext;
        this.partnerMsgList = aMyList;
    }

    @Override
    public int getCount() {
        return this.partnerMsgList.size();
    }

    @Override
    public PartnerMsg getItem(int position) {
        return this.partnerMsgList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    // LIST如何知道要呼叫幾次getView()方法?
    @Override
    public View getView(int aPosition, View aConvertView, ViewGroup parent) {
        aConvertView = View.inflate(this.context, R.layout.chat_history_list_item, null);
        final PartnerMsg data = this.partnerMsgList.get(aPosition);
        final ViewHolder holder = new ViewHolder();
//        // 拿MemId，用來判斷是要用左邊還是右邊的對話框(bubble)
//        SharedPreferences preferences_r = this.context.getSharedPreferences(Common.PREF_FILE, this.context.MODE_PRIVATE);
//        String memId = preferences_r.getString("memId", null);
//
        // 開始設定viewHolder:
        holder.img_profile = (ImageView)aConvertView.findViewById(R.id.img_profile);
        holder.txt_topic = (TextView)aConvertView.findViewById(R.id.txt_topic);
        holder.txt_date = (TextView)aConvertView.findViewById(R.id.txt_date);
        holder.txt_lastmsg = (TextView)aConvertView.findViewById(R.id.txt_lastmsg);
        holder.txt_unread_count = (TextView)aConvertView.findViewById(R.id.txt_unread_count);
        aConvertView.setTag(holder);


        // 開始binding data to viewholder:
        String url = Common.URL + "/android/live2/partner.do";
        MemVO memVO = null;
        // 判斷對方會員的memId - 由於資料庫sql指令的關係，此list資料是依據發訊息者或是接收訊息者其中一方是自己本身的話就放入list，
        // 因此我們在這裡得做額外判斷
        SharedPreferences preferences_r = this.context.getSharedPreferences(Common.PREF_FILE,this.context.MODE_PRIVATE);
        String memId =preferences_r.getString("memId",null);
        String toMemId = null;
        if (!data.getMemChatToMemId().toString().equals(memId)){
            toMemId = data.getMemChatToMemId().toString();
        }else{
            toMemId = data.getMemChatMemId().toString();
        }
        // end of 判斷對方會員的memId
        try {
            memVO = new PartnerGetOneTask().execute(url,toMemId).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        holder.txt_topic.setText(memVO.getMemName());
        String lastMsg = data.getMemChatContent().toString();
        if (lastMsg.length() > 13){
            lastMsg = lastMsg.substring(0,10);
        }
        Log.d("lastMsg - ", ""+lastMsg.length());
        holder.txt_lastmsg.setText(lastMsg);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String date = sdf.format(data.getMemChatDate());
        holder.txt_date.setText(date);
        Bitmap bitmap = BitmapFactory.decodeByteArray(memVO.getMemProfile(),0,memVO.getMemProfile().length);
        holder.img_profile.setImageBitmap(bitmap);
        return aConvertView;
    }// end

    static class ViewHolder {
        ImageView img_profile;
        TextView txt_topic;
        TextView txt_date;
        TextView txt_lastmsg;
        TextView txt_unread_count;
    }
}












