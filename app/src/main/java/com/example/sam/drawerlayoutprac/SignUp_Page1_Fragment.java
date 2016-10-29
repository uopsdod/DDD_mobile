package com.example.sam.drawerlayoutprac;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

/**
 * Created by cuser on 2016/10/12.
 */
public class SignUp_Page1_Fragment extends Fragment {
    Button btSubmit;
    EditText editText;
    Fragment fragment;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_signup_page1, container, false);
        editText = (EditText) view.findViewWithTag("edtext");
        editText.setOnKeyListener(new edFocus());
        btSubmit = (Button) view.findViewById(R.id.btSubmit);

        btSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fragment = new SignUp_Page2_Fragment();
                Util.switchFragment(SignUp_Page1_Fragment.this, fragment);
            }
        });
        return view;

    }


    public class edFocus implements View.OnKeyListener {

        @Override
        public boolean onKey(View view, int keyCode, KeyEvent event) {
            if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);

                getFocus();
            }
            return false;
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        getFocus();
    }


    private void getFocus() {
        getView().setFocusableInTouchMode(true);
        getView().requestFocus();
        getView().setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK) {
                    FragmentManager fm = getActivity().getSupportFragmentManager();
                    fm.popBackStack();
                    return true;
                }
                return false;
            }
        });
    }


}
