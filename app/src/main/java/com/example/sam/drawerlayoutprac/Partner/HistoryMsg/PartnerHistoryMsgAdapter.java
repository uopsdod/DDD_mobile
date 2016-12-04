package com.example.sam.drawerlayoutprac.Partner.HistoryMsg;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.sam.drawerlayoutprac.Common;
import com.example.sam.drawerlayoutprac.Partner.PartnerGetOneImageTask;
import com.example.sam.drawerlayoutprac.Partner.PartnerGetOneTextTask;
import com.example.sam.drawerlayoutprac.Partner.VO.MemVO;
import com.example.sam.drawerlayoutprac.Partner.PartnerGetOneMemVOTask;
import com.example.sam.drawerlayoutprac.Partner.VO.PartnerMsg;
import com.example.sam.drawerlayoutprac.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Created by cuser on 2016/11/13.
 */

public class PartnerHistoryMsgAdapter extends BaseAdapter {
    private static String TAG = "PartHistMsgAdapter";
    private Context context;
    private List<PartnerMsg> partnerMsgList;
    private static String profileImgDirPath;
    private static String profileImgDirPath_name = "profileImg";

    public PartnerHistoryMsgAdapter(Context aContext, List<PartnerMsg> aMyList) {
        Log.d("ParHisMsgAdapter", ""+"constructor called");
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
        Log.d("ParHisMsgAdapter", "getView - called");
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
        String url = Common.URL_Partner;
        MemVO memVO = null;
        // 判斷對方會員的memId - 由於資料庫sql指令的關係，此list資料是依據發訊息者或是接收訊息者其中一方是自己本身的話就放入list，
        // 因此我們在這裡得做額外判斷
        SharedPreferences preferences_r = this.context.getSharedPreferences(Common.PREF_FILE,this.context.MODE_PRIVATE);
        String memId =preferences_r.getString("memId",null);
        String toMemId = null;
        if (!data.getMemChatToMemVO().getMemId().toString().equals(memId)){
            toMemId = data.getMemChatToMemVO().getMemId().toString();
        }else{
            toMemId = data.getMemChatMemVO().getMemId().toString();
        }
        // end of 判斷對方會員的memId
        try {
            //memVO = new PartnerGetOneMemVOTask().execute(url,toMemId).get();
            memVO = new PartnerGetOneTextTask().execute(url,toMemId).get();
//            String dirPath_name = "profileImg";
            String fileName = toMemId + ".jpg";
            Bitmap bitmap = loadImageFromStorage(PartnerHistoryMsgAdapter.profileImgDirPath,fileName);
            if (bitmap != null){
                Log.d(TAG, fileName  + "exists");
                holder.img_profile.setImageBitmap(bitmap);
            }else{
                Log.d(TAG, fileName  + "fetched from server");
                int imageSize = 100;
                bitmap = new PartnerGetOneImageTask().execute(url, toMemId, imageSize).get();
                holder.img_profile.setImageBitmap(bitmap);
                PartnerHistoryMsgAdapter.profileImgDirPath = saveToInternalStorage(bitmap,profileImgDirPath_name,fileName);
                Log.d(TAG,profileImgDirPath);
            }
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
//        Log.d("lastMsg - ", ""+lastMsg.length());
        holder.txt_lastmsg.setText(lastMsg);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String date = sdf.format(data.getMemChatDate());
        holder.txt_date.setText(date);
//        Bitmap bitmap = BitmapFactory.decodeByteArray(memVO.getMemProfile(),0,memVO.getMemProfile().length);
//        holder.img_profile.setImageBitmap(bitmap);
        return aConvertView;
    }// end

    static class ViewHolder {
        ImageView img_profile;
        TextView txt_topic;
        TextView txt_date;
        TextView txt_lastmsg;
        TextView txt_unread_count;
    }

    private Bitmap loadImageFromStorage(String aDirPath, String aFileName)
    {
        Bitmap bitmap = null;
        try {
            File f = new File(aDirPath, aFileName);
            bitmap = BitmapFactory.decodeStream(new FileInputStream(f));
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        return bitmap;

    }

    private String saveToInternalStorage(Bitmap aBitmapImage, String aDirPath, String aFileName){
        ContextWrapper cw = new ContextWrapper(context);
        // path to /data/data/yourapp/app_data/imageDir
        File directory = cw.getDir(aDirPath, Context.MODE_PRIVATE);
        // Create imageDir
        File mypath = new File(directory, aFileName);

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(mypath);
            // Use the compress method on the BitMap object to write image to the OutputStream
            aBitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fos);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return directory.getAbsolutePath();
    }

}












