package com.example.sam.drawerlayoutprac.Partner;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.sam.drawerlayoutprac.Common;
import com.example.sam.drawerlayoutprac.R;

import java.util.List;
import java.util.Map;

/**
 * Created by cuser on 2016/11/13.
 */

public class PartnerChatAdapter extends BaseAdapter {
    private Context context;
    private List<PartnerMsg> partnerMsgList;

    public PartnerChatAdapter(Context aContext,List<PartnerMsg> aMyList) {
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

        // 拿MemId，用來判斷是要用左邊還是右邊的對話框(bubble)
        SharedPreferences preferences_r = this.context.getSharedPreferences(Common.PREF_FILE,this.context.MODE_PRIVATE);
        String memId = preferences_r.getString("memId", null);

        final ViewHolder holder;
        final PartnerMsg data = this.partnerMsgList.get(aPosition);
//        if (aConvertView == null) {
            holder = new ViewHolder();
            if (data.getMemChatMemId().equals(memId)) { // 是自己的訊息
                aConvertView = View.inflate(this.context, R.layout.chat_right_container, null);
            } else {                                    // 是對方的訊息
                aConvertView = View.inflate(this.context, R.layout.chat_left_container, null);
            }
            holder.chat_container = (LinearLayout)aConvertView.findViewById(R.id.chat_container);
            holder.text_container = (RelativeLayout)aConvertView.findViewById(R.id.text_container);
            holder.img_thumbnail = (ImageView)aConvertView.findViewById(R.id.img_thumbnail);
            holder.txt_name = (TextView)aConvertView.findViewById(R.id.txt_name);
            holder.container_bubble = (RelativeLayout)aConvertView.findViewById(R.id.container_bubble);
            holder.txt = (TextView)aConvertView.findViewById(R.id.txt);
            holder.txt_status = (TextView)aConvertView.findViewById(R.id.txt_status);
            holder.txt_time = (TextView)aConvertView.findViewById(R.id.txt_time);
            aConvertView.setTag(holder);
//        }
//        else {
//            holder = (ViewHolder) aConvertView.getTag();
//        }

        // 開始binding data to viewholder:
        holder.txt.setText(data.getMemChatContent().toString());
        return aConvertView;
    }// end

    static class ViewHolder {
        LinearLayout chat_container;
        RelativeLayout text_container;
        ImageView img_thumbnail;
        TextView txt_name;
        RelativeLayout container_bubble;
        TextView txt;
        TextView txt_status;
        TextView txt_time;

    }
}












