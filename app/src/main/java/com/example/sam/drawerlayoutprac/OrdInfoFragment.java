package com.example.sam.drawerlayoutprac;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by cuser on 2016/11/13.
 */

public class OrdInfoFragment extends CommonFragment{
    TextView tvInfo;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View myLayout = inflater.inflate(R.layout.fragment_ordinfo, container, false);
        getActivity().findViewById(R.id.floatingBtn).setVisibility(View.INVISIBLE);

        return myLayout;
    }
}
