package com.example.sam.drawerlayoutprac.Partner;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.ImageView;

import com.example.sam.drawerlayoutprac.Partner.Chat.ChatFragment;
import com.example.sam.drawerlayoutprac.R;
import com.example.sam.drawerlayoutprac.Util;
import com.google.android.gms.maps.model.Marker;

public abstract class OnInfoWindowElemTouchListener implements OnTouchListener {
    private final View view;
//    private final Drawable bgDrawableNormal;
//    private final Drawable bgDrawablePressed;
    private final Handler handler = new Handler();

    private Marker marker;
    public boolean pressed = false;

    private Context context;
    private Fragment fragment;
    private String toMemId;

    public OnInfoWindowElemTouchListener(View view, Context aContext, Fragment aPartnerMapFragment) {
        this.view = view;
        this.context = aContext;
        this.fragment = aPartnerMapFragment;
//        this.bgDrawableNormal = bgDrawableNormal;
//        this.bgDrawablePressed = bgDrawablePressed;
    }

    public void setMarker(Marker marker) {
        this.marker = marker;
    }

    @Override
    public boolean onTouch(View vv, MotionEvent event) {
        if (0 <= event.getX() && event.getX() <= view.getWidth() &&
                0 <= event.getY() && event.getY() <= view.getHeight()) {
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    startPress();
                    break;

                // We need to delay releasing of the view a little so it shows the pressed state on the screen
                case MotionEvent.ACTION_UP:
                    handler.postDelayed(confirmClickRunnable, 150);
                    break;

                case MotionEvent.ACTION_CANCEL:
                    endPress();
                    break;
                default:
                    break;
            }
        } else {
            // If the touch goes outside of the view's area
            // (like when moving finger out of the pressed button)
            // just release the press
            endPress();
        }
        return false;
    }

    private void startPress() {
        Log.d("infoWindow", "startPress-btn pressed");
        //Util.showToast(context,"startPress-btn pressed - toMemId: " + this.toMemId);
        Fragment tmpFragment = new ChatFragment();
        Bundle bundle = new Bundle();
        bundle.putString("ToMemId",this.toMemId);
        Log.d("infoWindow","startPress toMemId: " + this.toMemId);
        tmpFragment.setArguments(bundle);
        Util.switchFragment(fragment,tmpFragment);
//        if (!pressed) {
//            pressed = true;
//            handler.removeCallbacks(confirmClickRunnable);
//
////            view.setBackgroundResource(R.drawable.button_profile_46);
////            view.setImageResource();
////            view.setImageDrawable(getResources().getDrawable(R.drawable.monkey, getApplicationContext().getTheme()));
////            view.setImageResource(com.google.android.gms.R.drawable);
////            view.setBackground(bgDrawablePressed);
//            if (marker != null)
//                marker.showInfoWindow();
//        } else {
//            Log.d("infoWindow", "startPress-btn UNpressed");
////            view.setBackgroundResource(R.drawable.star);
//            pressed = false;
//            handler.removeCallbacks(confirmClickRunnable);
////            view.setBackground(bgDrawableNormal);
//        }
    }

    private boolean endPress() {
        Log.d("infoWindow", "endPress-btn pressed");
        if (pressed) {
            this.pressed = false;
            handler.removeCallbacks(confirmClickRunnable);
//            view.setBackground(bgDrawableNormal);
            if (marker != null)
                marker.showInfoWindow();
            return true;
        } else
            return false;
    }

    private final Runnable confirmClickRunnable = new Runnable() {
        public void run() {
            if (endPress()) {
                onClickConfirmed(view, marker);
            }
        }
    };

    /**
     * This is called after a successful click
     */
    protected abstract void onClickConfirmed(View v, Marker marker);

    public String getToMemId() {
        return toMemId;
    }

    public void setToMemId(String toMemId) {
        this.toMemId = toMemId;
    }
}