package com.example.sam.drawerlayoutprac.Partner;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;

import com.example.sam.drawerlayoutprac.MainActivity;
import com.example.sam.drawerlayoutprac.Partner.HistoryMsg.PartnerHistoryMsgFragment;
import com.example.sam.drawerlayoutprac.R;
import com.example.sam.drawerlayoutprac.Util;

/**
 * Created by cuser on 2016/11/15.
 */

public class PartnerCommonFragment extends Fragment {
    @Override
    public void onCreate (Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        // testing
        setHasOptionsMenu(true);
    }

    @Override
    public void onResume() {
        super.onResume();
        backBtnPressed();
    }

    @Override
    public boolean onOptionsItemSelected (MenuItem item){
        switch (item.getItemId()) {
            case R.id.action_bar_message : {
//                Util.showToast(getContext(),"PartnerCommonFragment menu");
//                Fragment fragment = new PartnerHistoryMsgFragment();
//                Util.switchFragment(this,fragment);
//                Log.i(TAG, "menu - Save from fragment");
                // 開啟歷史訊息列表:
                Log.d("floatingBtnPressed", ""+MainActivity.floatingBtnPressed);
                if (!MainActivity.floatingBtnPressed) {
                    MainActivity.floatingBtnPressed = true;
                    Util.showToast(getContext(),"PartnerCommonFragment menu opened");
                    Fragment fragment = new PartnerHistoryMsgFragment();
                    Util.switchFragment(this,fragment);
                // 關閉歷史訊息列表:
                } else {
                    MainActivity.floatingBtnPressed = false;
                    Util.showToast(getContext(),"PartnerCommonFragment menu closed");
                    FragmentManager fm = getFragmentManager();
                    if (fm.getBackStackEntryCount() > 0) {
                        fm.popBackStack();
                    }
//                    else {
//                /*
//                 * KeyEvent kdown = new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_BACK);
//                    Activity.dispatchKeyEvent(kdown);
//                */
//                    }
                }
                return true;
            }
        }
        return true;
    }

    private void backBtnPressed() {
        getView().setFocusableInTouchMode(true);
        getView().requestFocus();
        getView().setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK) {
                    // 回到上一個Fragment或是離開app
                    FragmentManager fm = PartnerCommonFragment.this.getFragmentManager();
                    if (fm.getBackStackEntryCount() > 0) {
                        fm.popBackStack();
                        return true;
                    }
                }// end if
                return false;
            }
        });
    }// end of backBtnPressed
}
