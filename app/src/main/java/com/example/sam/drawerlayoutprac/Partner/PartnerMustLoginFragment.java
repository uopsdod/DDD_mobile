package com.example.sam.drawerlayoutprac.Partner;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;

import com.example.sam.drawerlayoutprac.Common;
import com.example.sam.drawerlayoutprac.Hotel.HotelFragment;
import com.example.sam.drawerlayoutprac.MainActivity;
import com.example.sam.drawerlayoutprac.MemberFragment;
import com.example.sam.drawerlayoutprac.Partner.HistoryMsg.PartnerHistoryMsgFragment;
import com.example.sam.drawerlayoutprac.R;
import com.example.sam.drawerlayoutprac.Util;

/**
 * Created by cuser on 2016/11/15.
 */

public class PartnerMustLoginFragment extends PartnerCommonFragment {
    // BUG - 不知道為什麼其他頁面也會被影響!

    @Override
    public void onResume() {
        super.onResume();
        Log.d("MustLoginFragment","switchFromLoginPage: " + MemberFragment.switchFromLoginPage);
        // 預防crush: 當使用者看到登入彈跳視窗->點擊'現在登入'->然後又調皮的從navigation跳到其他頁面
        // 因為透過switchFragment(Activity, Fragment)方法會把所有在stack的Fragment popout
        // 而poppout的過程又會呼叫onResumed()方法，會因此讓警示燈入訊息不斷出現而造成crush。
        // 這個MemberFragment.switchFromLoginPage會配合在MemberFragmetnk的onResumed()方法中設定
        // 一旦使用者是從登入頁面->navigation->其他頁面的話，原本放在stack的那個Fragment就不會往下面的code走
        if (MemberFragment.switchFromLoginPage == true){
            MemberFragment.switchFromLoginPage = false;
            return;
        }
        SharedPreferences preferences_r = getActivity().getSharedPreferences(Common.PREF_FILE, getActivity().MODE_PRIVATE);
        String memid = null;
        if (preferences_r != null){
            memid = preferences_r.getString("memId", null);
        }
        if (memid == null){
            Util.showToast(getContext(),"You must login to proceed");
            new AlertDialog.Builder(getActivity())
                    .setTitle("你沒有權限進入此頁")
                    .setMessage("請登入後繼續")
                    .setPositiveButton("現在登入", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
//                            Util.showToast(getContext(),"登入pressed");
                            MemberFragment.switchFromLoginPage = true;
                            Util.switchFragment(PartnerMustLoginFragment.this,new MemberFragment());
                        }
                    })
                    .setNegativeButton("暫時不要", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Util.showToast(getContext(),"暫時不要pressed");
                            Util.switchFragment(getActivity(),new HotelFragment());
                        }
                    })
                    .show();



        }
    }


}
