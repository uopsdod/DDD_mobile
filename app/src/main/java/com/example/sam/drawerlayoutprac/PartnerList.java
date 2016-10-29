package com.example.sam.drawerlayoutprac;

import android.content.Context;
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
    public static final String KEY_AVATAR = "avatar";
    public static final String KEY_NAME = "name";
    public static final String KEY_DESCRIPTION_SHORT = "description_short";
    public static final String KEY_DESCRIPTION_FULL = "description_full";

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
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.list_item_partner, parent, false);
            viewHolder = new PartnerList.ViewHolder();
            viewHolder.mViewOverlay = convertView.findViewById(com.yalantis.euclid.library.R.id.view_avatar_overlay);
            viewHolder.mListItemAvatar = (ImageView) convertView.findViewById(com.yalantis.euclid.library.R.id.image_view_avatar);
            viewHolder.mListItemName = (TextView) convertView.findViewById(com.yalantis.euclid.library.R.id.text_view_name);
            viewHolder.mListItemDescription = (TextView) convertView.findViewById(com.yalantis.euclid.library.R.id.text_view_description);
            convertView.setTag(viewHolder); // ? what for?
        } else {
            viewHolder = (PartnerList.ViewHolder) convertView.getTag(); // ? what for?
        }
        int sScreenWidth = getContext().getResources().getDisplayMetrics().widthPixels;
        int sProfileImageHeight = getContext().getResources().getDimensionPixelSize(com.yalantis.euclid.library.R.dimen.height_profile_image);
        Picasso.with(getContext()).load((Integer) mData.get(position).get(KEY_AVATAR))
                .resize(sScreenWidth, sProfileImageHeight).centerCrop()
                .placeholder(com.yalantis.euclid.library.R.color.blue)
                .into(viewHolder.mListItemAvatar);

        viewHolder.mListItemName.setText(mData.get(position).get(KEY_NAME).toString().toUpperCase());
        viewHolder.mListItemDescription.setText((String) mData.get(position).get(KEY_DESCRIPTION_SHORT));
        viewHolder.mViewOverlay.setBackground(EuclidActivity.sOverlayShape);

        return convertView;
    }

    static class ViewHolder {
        View mViewOverlay;
        ImageView mListItemAvatar;
        TextView mListItemName;
        TextView mListItemDescription;
    }
}