package com.example.sam.drawerlayoutprac;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.sam.drawerlayoutprac.Hotel.HotelFragment;
import com.example.sam.drawerlayoutprac.Hotel.HotelVO;
import com.example.sam.drawerlayoutprac.Partner.TokenIdWebSocket;

import static android.content.Context.MODE_PRIVATE;


public class HotelMemberFragment extends Fragment {
    private String TAG = "HotelMemberFragment";
    TextView tvTroLogin;
    TextInputLayout tilUserName, tilPassword;
    EditText etUserName, etPassword;
    Button btLogin;
    Fragment fragment;
    HotelVO hotelVO = new HotelVO();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_hotel_member, container, false);

        tilUserName = (TextInputLayout) view.findViewById(R.id.tilUserName);
        tilPassword = (TextInputLayout) view.findViewById(R.id.tilPassword);
        etUserName = (EditText) view.findViewById(R.id.etUserName);
        etPassword = (EditText) view.findViewById(R.id.etPassword);
        btLogin = (Button) view.findViewById(R.id.btLogin);
        btLogin.setOnClickListener(new btClick());
        getActivity().findViewById(R.id.floatingBtn).setVisibility(View.INVISIBLE);
        etUserName.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                String userName = etUserName.getText().toString().trim();
                if(userName.length() <= 0){
                    tilUserName.setError("User name is invalid");
                    return;
                }else{
                    tilUserName.setError(null);
                    return;
                }
            }
        });

        etPassword.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                String password = etPassword.getText().toString().trim();
                if(password.length() <= 0){
                    tilPassword.setError("Password is invalid");
                    return;
                }else{
                    tilPassword.setError(null);
                    return;
                }
            }
        });

        return view;
    }

    private boolean isUserValid(String userName, String password){
        String url = Common.URL + "/android/hotel.do";
        String chPassword = null;
        if(Common.networkConnected(getActivity())){
            try {
                hotelVO = new HotelMemCheckTask().execute(url, userName, password).get();
                Log.d(TAG, hotelVO.toString());
            } catch (Exception e) {
                Log.e(TAG, e.toString());
            }
        }
        if(hotelVO == null){
            return false;
        }
        return true;
    }

    public class btClick implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            if (view.equals(btLogin)) {
                String userName = etUserName.getText().toString().trim();
                String password = "AA104" + etPassword.getText().toString().trim();
                if(!isUserValid(userName,password)){
                    etUserName.setError("Account or Password is not valid");
                    etPassword.setError(null);
                    Util.showToast(getActivity(), "Login fail");
                    return;
                }else{
//                    String memId = memVO.getMemId().trim();
                    //用Context.getSharedPreferences()呼叫，並指定偏好設定檔名與模式
                    SharedPreferences prefHotel = getContext().getSharedPreferences(Common.PREF_FILE_Hotel, MODE_PRIVATE);
                    prefHotel.edit().putString("userName", userName)
                               .putString("password", password)
                               .putBoolean("login" , true)
                               .apply();
                    // 將會員Id與tokenId送到server
                    new TokenIdWebSocket(getActivity()).sendTokenIdToServer();
                    Util.showToast(getActivity(), "Login success");
                }

                fragment = new HotelFragment();
                Util.switchFragment(HotelMemberFragment.this, fragment);
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        SharedPreferences pref = getContext().getSharedPreferences(Common.PREF_FILE, MODE_PRIVATE);
        boolean login = pref.getBoolean("login", false);
        if(login){
           pref.getString("user", "");
           pref.getString("password", "");
        }else{
            Util.showToast(getActivity() ,"User Name or Password invalid");
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
