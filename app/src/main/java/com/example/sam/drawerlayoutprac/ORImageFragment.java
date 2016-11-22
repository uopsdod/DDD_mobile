package com.example.sam.drawerlayoutprac;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

/**
 * Created by cuser on 2016/11/22.
 */

public class ORImageFragment extends CommonFragment {
    private ImageView ivQRCode;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_order_cash, container, false);
        ivQRCode = (ImageView) view.findViewById(R.id.ivQRCode);
        return view;
    }
}
