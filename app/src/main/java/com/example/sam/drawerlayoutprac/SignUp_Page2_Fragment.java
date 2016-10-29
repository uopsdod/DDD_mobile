package com.example.sam.drawerlayoutprac;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

/**
 * Created by cuser on 2016/10/12.
 */
public class SignUp_Page2_Fragment extends Fragment {
    Button btSubmit;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_signup_page2_, container, false);
        btSubmit = (Button) view.findViewById(R.id.btSubmit);
        btSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Fragment fragment = new HotelFragment();
                Util.switchFragment(getActivity(),fragment); // 回到首頁，清除所有Fragment stack
            }
        });
        return view;
    }
}
