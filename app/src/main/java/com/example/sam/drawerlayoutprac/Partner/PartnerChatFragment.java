package com.example.sam.drawerlayoutprac.Partner;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.sam.drawerlayoutprac.R;
import com.yalantis.euclid.library.EuclidState;

/**
 * Created by cuser on 2016/11/8.
 */

public class PartnerChatFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup, Bundle bundle) {
        super.onCreateView(inflater, viewGroup, bundle);
        View view = inflater.inflate(R.layout.chat_containers, viewGroup, false);
        return view;
    }// end of onCreateView


    private void backBtnPressed() {
        getView().setFocusableInTouchMode(true);
        getView().requestFocus();
        getView().setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK) {
                    //Util.showToast(getContext(),"backBtnPressed");
                    FragmentManager fm = PartnerChatFragment.this.getFragmentManager();
                    fm.popBackStack();
                }
                return false;
            }
        });
    }
}
