package com.example.sam.drawerlayoutprac.Order;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.sam.drawerlayoutprac.CommonFragment;
import com.example.sam.drawerlayoutprac.R;

/**
 * Created by cuser on 2016/11/22.
 */

public class OrderPayFragment extends CommonFragment {
    private Button btSubmit, btQRDisplay;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_order_cash, container, false);
        btSubmit = (Button) view.findViewById(R.id.btSubmit);
        return view;
    }

    private class onClick implements View.OnClickListener{

        @Override
        public void onClick(View view) {


        }
    }
}
