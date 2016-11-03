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

import com.squareup.picasso.Picasso;
import com.yalantis.euclid.library.EuclidActivity;
import com.yalantis.euclid.library.EuclidListAdapter;

import java.util.List;
import java.util.Map;

/**
 * Created by cuser on 2016/10/29.
 */

public class PartnerList extends ArrayAdapter<Map<String, Object>> {
    public static final String KEY_PROFILE = "profile";
    public static final String KEY_MEMID = "MEMID";
    public static final String KEY_NAME = "name";
    public static final String KEY_DESCRIPTION_SHORT = "description_short";
    public static final String KEY_DESCRIPTION_FULL = "description_full";
    private static final int CIRCLE_RADIUS_DP = 50;

    private final LayoutInflater mInflater;
    private List<Map<String, Object>> mData;

    public PartnerList(Context context, int layoutResourceId, List<Map<String, Object>> data) {
        super(context, layoutResourceId, data);
        mData = data;
        mInflater = LayoutInflater.from(context);
    }
    // LIST如何知道要呼叫幾次getView()方法
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final PartnerList.ViewHolder viewHolder;
        // 開始拿view
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.list_item_partner, parent, false);
            viewHolder = new PartnerList.ViewHolder();
            viewHolder.mListItemProfile = (ImageView) convertView.findViewById(R.id.image_view_profile);
            viewHolder.mViewOverlay = convertView.findViewById(R.id.view_avatar_overlay);
            viewHolder.mListItemName = (TextView) convertView.findViewById(R.id.text_view_name);
            viewHolder.mListItemDescription = (TextView) convertView.findViewById(R.id.text_view_description);
            convertView.setTag(viewHolder); // ? what for?
        } else {
            viewHolder = (PartnerList.ViewHolder) convertView.getTag(); // ? what for?
        }

        // 把圖片data放上view
//        Picasso.with(getContext()).load((Integer) mData.get(position).get(KEY_PROFILE))
//                .resize(PartnerFragment.sScreenWidth, PartnerFragment.sProfileImageHeight).centerCrop()
//                .placeholder(com.yalantis.euclid.library.R.color.black)
//                .into(viewHolder.mListItemAvatar);

//      viewHolder.mListItemAvatar = (ImageView) mData.get(position).get(KEY_PROFILE);
        // 開始binding data
        // 每次都開另一個thread去抓圖片
        String url = Common.URL + "/live2/Partner";
        Integer memId = Integer.parseInt(mData.get(position).get(KEY_MEMID).toString().toUpperCase());
        Integer imageSize = 250;
        new PartnerGetImageTask(viewHolder.mListItemProfile).execute(url, memId, imageSize);//
        viewHolder.mListItemName.setText(mData.get(position).get(KEY_NAME).toString().toUpperCase());
        viewHolder.mListItemDescription.setText((String) mData.get(position).get(KEY_DESCRIPTION_SHORT));
        viewHolder.mViewOverlay.setBackground(PartnerFragment.sOverlayShape);

        return convertView;
    }



    static class ViewHolder {
        View mViewOverlay;
        ImageView mListItemProfile;
        TextView mListItemName;
        TextView mListItemDescription;
    }

    private ShapeDrawable buildAvatarCircleOverlay() {
        int radius = 666;
        ShapeDrawable overlay = new ShapeDrawable(new RoundRectShape(null,
                new RectF(
                        PartnerFragment.sScreenWidth / 2 - dpToPx(CIRCLE_RADIUS_DP * 2),
                        com.yalantis.euclid.library.R.dimen.height_profile_image / 2 - dpToPx(CIRCLE_RADIUS_DP * 2),
                        PartnerFragment.sScreenWidth / 2 - dpToPx(CIRCLE_RADIUS_DP * 2),
                        com.yalantis.euclid.library.R.dimen.height_profile_image / 2 - dpToPx(CIRCLE_RADIUS_DP * 2)),
                new float[]{radius, radius, radius, radius, radius, radius, radius, radius}));
        overlay.getPaint().setColor(getContext().getResources().getColor(com.yalantis.euclid.library.R.color.gray));

        return overlay;
    }

    public int dpToPx(int dp) {
        return Math.round((float) dp * getContext().getResources().getDisplayMetrics().density);
    }



}