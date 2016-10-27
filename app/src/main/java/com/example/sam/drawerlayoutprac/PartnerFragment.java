package com.example.sam.drawerlayoutprac;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by cuser on 2016/10/9.
 */
public class PartnerFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup, Bundle bundle){
        super.onCreateView(inflater,viewGroup,bundle);
        View view = inflater.inflate(R.layout.fragment_partner, viewGroup, false);
        return view;
    }
}
