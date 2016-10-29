package com.example.sam.drawerlayoutprac;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class MemberFragment extends Fragment {
    TextView tvTroLogin;
    EditText etUserName, etPassword;
    Button btLogin, btSignUp;
    Fragment fragment;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.member_fragment, container, false);

        btLogin = (Button) view.findViewById(R.id.btLogin);
        btSignUp = (Button) view.findViewById(R.id.btSignUp);
        btLogin.setOnClickListener(new btClick());
        btSignUp.setOnClickListener(new btClick());

        return view;

    }

    public class btClick implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            if (view.equals(btLogin)) {
                fragment = new HotelFragment();
                Util.switchFragment(MemberFragment.this, fragment);
            } else {
                fragment = new SignUp_Page1_Fragment();
                Util.switchFragment(MemberFragment.this, fragment);

            }
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
                    int backStackCount = getActivity().getSupportFragmentManager().getBackStackEntryCount();
                    for (int i = 0; i < backStackCount; i++) {
                        // Get the back stack fragment id.
                        int backStackId = getActivity().getSupportFragmentManager().getBackStackEntryAt(i).getId();
                        fm.popBackStack(backStackId, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                    }
                    return true;
                }
                return false;
            }
        });

    }


}
