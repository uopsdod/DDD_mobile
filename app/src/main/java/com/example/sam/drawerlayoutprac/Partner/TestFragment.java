package com.example.sam.drawerlayoutprac.Partner;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.example.sam.drawerlayoutprac.R;
import com.example.sam.drawerlayoutprac.Util;

public class TestFragment extends Fragment {
    View rootView;
    EditText memIdView;
    Button btnView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        this.rootView = inflater.inflate(R.layout.fragment_blank, container, false);
        this.memIdView = (EditText)rootView.findViewById(R.id.memId_yo);
        this.btnView = (Button)rootView.findViewById(R.id.btn_test_yo);

        return inflater.inflate(R.layout.fragment_blank, container, false);
    }


}
