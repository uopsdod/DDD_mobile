package com.example.sam.drawerlayoutprac;

import android.content.Context;
import android.graphics.Point;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;

/**
 * Created by cuser on 2016/11/22.
 */

public class ORImageFragment extends CommonFragment {
    private ImageView ivQRCode;
    private String ordId;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        ordId = getArguments().getString("ordId");
        View view = inflater.inflate(R.layout.fragment_qrcode_image, container, false);
        ivQRCode = (ImageView) view.findViewById(R.id.ivQRCode);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        showQRCode();
    }

    private void showQRCode(){
        if(Common.networkConnected(getActivity())){
            String url = Common.URL + "/android/ord/ord.do";
            String id = MainActivity.pref.getString("memId", null);

//            WindowManager windowManager = (WindowManager) getActivity().getSystemService(Context.WINDOW_SERVICE);
//            Display display = windowManager.getDefaultDisplay();
//            Point point = new Point();
//            display.getSize(point);
//            int width = point.x;
//            int height = point.y;
//            int imageSide = width < height ? width : height;
            int imageSize = 250;
            if(id != null){
               new OrderGetImageTask(ivQRCode).execute(url, id, imageSize, ordId);
            }
        }
    }
}

