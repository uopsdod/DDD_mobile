package com.example.sam.drawerlayoutprac;

import android.content.Context;
import android.graphics.RectF;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;
import java.util.Map;

/**
 * Created by cuser on 2016/10/29.
 */

public class PartnerListAdapter extends ArrayAdapter<Map<String, Object>> {
    public static final String KEY_MEMID = "memId";
    public static final String KEY_NAME = "name";
    public static final String KEY_DESCRIPTION_SHORT = "description_short";
    public static final String KEY_DESCRIPTION_FULL = "description_full";
    private static final int CIRCLE_RADIUS_DP = 50;

    private Context context;
    private final LayoutInflater mInflater;
    private List<Map<String, Object>> mData;

    public PartnerListAdapter(Context context, int layoutResourceId, List<Map<String, Object>> data) {
        super(context, layoutResourceId, data);
        this.mData = data;
        this.context = context;
        this.mInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public Map<String, Object> getItem(int position) {
        return mData.get(position);
    }

    // LIST如何知道要呼叫幾次getView()方法?
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final PartnerListAdapter.ViewHolder viewHolder;
        // 開始拿view
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.list_item_partner, parent, false);
            viewHolder = new PartnerListAdapter.ViewHolder();
            viewHolder.mListItemProfile = (ImageView) convertView.findViewById(R.id.image_view_profile);
            viewHolder.mViewOverlay = convertView.findViewById(R.id.view_avatar_overlay);
            viewHolder.mListItemName = (TextView) convertView.findViewById(R.id.text_view_name);
            viewHolder.mListItemDescription = (TextView) convertView.findViewById(R.id.text_view_description);
            convertView.setTag(viewHolder); // ? what for?
        } else {
            viewHolder = (PartnerListAdapter.ViewHolder) convertView.getTag(); // ? what for?
        }
        // end of 開始拿view

        // 開始binding data
        String url = Common.URL + "/live2/Partner";
        Integer memId = Integer.parseInt(mData.get(position).get(KEY_MEMID).toString().toUpperCase());
        Integer imageSize = 300;
            // 每次都開另一個thread去抓圖片
        new PartnerGetImageTask(viewHolder.mListItemProfile).execute(url, memId, imageSize);
        viewHolder.mListItemName.setText(mData.get(position).get(KEY_NAME).toString().toUpperCase());
        viewHolder.mListItemDescription.setText((String) mData.get(position).get(KEY_DESCRIPTION_SHORT));
            // 將大頭貼設成圓的
        viewHolder.mViewOverlay.setBackground(buildAvatarCircleOverlay());
        // end of binding data

        return convertView;
    }

    static class ViewHolder {
        View mViewOverlay;
        ImageView mListItemProfile;
        TextView mListItemName;
        TextView mListItemDescription;
    }


    // 把大頭貼變成圓的 - 方法1
    private ShapeDrawable buildAvatarCircleOverlay() {
        int radius = 666;
        int sScreenWidth = this.context.getResources().getDisplayMetrics().widthPixels;
        int sProfileImageHeight = this.context.getResources().getDimensionPixelSize(R.dimen.height_profile_image);

        ShapeDrawable overlay = new ShapeDrawable(new RoundRectShape(null,
                new RectF(
                        sScreenWidth / 2 - dpToPx(getCircleRadiusDp() * 2),
                        sProfileImageHeight / 2 - dpToPx(getCircleRadiusDp() * 2),
                        sScreenWidth / 2 - dpToPx(getCircleRadiusDp() * 2),
                        sProfileImageHeight / 2 - dpToPx(getCircleRadiusDp() * 2)),
                new float[]{radius, radius, radius, radius, radius, radius, radius, radius}));
        overlay.getPaint().setColor(this.context.getResources().getColor(com.yalantis.euclid.library.R.color.gray));

        return overlay;
    }
    // 把大頭貼變成圓的 - 方法2
    public int dpToPx(int dp) {
        return Math.round((float) dp * this.context.getResources().getDisplayMetrics().density);
    }
    // 把大頭貼變成圓的 - 方法3
    protected int getCircleRadiusDp() {
        return CIRCLE_RADIUS_DP;
    }

}