package com.example.sam.drawerlayoutprac.Order;

import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;
import android.view.WindowManager;
import android.widget.ImageView;

import com.example.sam.drawerlayoutprac.Common;
import com.example.sam.drawerlayoutprac.MainActivity;
import com.example.sam.drawerlayoutprac.OrderGetImageTask;
import com.example.sam.drawerlayoutprac.R;

import java.util.concurrent.ExecutionException;

import static android.view.Window.FEATURE_NO_TITLE;

/**
 * Created by cuser on 2016/11/27.
 */

public class OrdLookUpNowCheckQRActivity extends Activity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(FEATURE_NO_TITLE);
        setContentView(R.layout.fragment_qrcode_image);
        ImageView qr_image = (ImageView) findViewById(R.id.ivQRCode);
        Bundle bundle = getIntent().getExtras();
        String ordId = (String)bundle.get("ordId");

        showQRCode(qr_image,ordId);
    }


    private void showQRCode(ImageView aQrImage, String aOrdId){
        if(Common.networkConnected(OrdLookUpNowCheckQRActivity.this)){
            String url = Common.URL + "/android/ord/ord.do";
            String memId = MainActivity.pref.getString("memId", null);

            WindowManager windowManager = (WindowManager) OrdLookUpNowCheckQRActivity.this.getSystemService(Context.WINDOW_SERVICE);
            Display display = windowManager.getDefaultDisplay();
            Point point = new Point();
            display.getSize(point);
            int width = point.x;
            int height = point.y;
            int imageSize = width < height ? width : height;
            if(memId != null){
                try {
                    new OrderGetImageTask(aQrImage).execute(url, memId, imageSize, aOrdId).get();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
