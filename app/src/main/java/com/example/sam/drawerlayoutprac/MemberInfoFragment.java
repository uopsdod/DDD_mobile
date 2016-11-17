package com.example.sam.drawerlayoutprac;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.sam.drawerlayoutprac.Hotel.HotelFragment;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by cuser on 2016/11/17.
 */

public class MemberInfoFragment extends MustLoginFragment {
    TextView memName, memGender, memLiveBudget, memIntro;
    ImageView ivPhoto;
    Button btSubmit, btLogout;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_member_info, container, false);
        memName = (TextView) view.findViewById(R.id.memName);
        memGender = (TextView) view.findViewById(R.id.memGender);
        memLiveBudget = (TextView) view.findViewById(R.id.memLiveBudget);
        memIntro = (TextView) view.findViewById(R.id.memIntro);
        ivPhoto = (ImageView) view.findViewById(R.id.ivPhoto);
        btSubmit = (Button) view.findViewById(R.id.btSubmit);
        btLogout = (Button) view.findViewById(R.id.btLogout);

        btSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Fragment fragment = new MemberUpdateFragment();
                Util.switchFragment(MemberInfoFragment.this, fragment);
            }
        });

        btLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //會員登出
                SharedPreferences pref2 = getActivity().getSharedPreferences(Common.PREF_FILE,
                        MODE_PRIVATE);
                boolean login2 = pref2.getBoolean("login",false);
                if(login2){
                    pref2.edit().remove("userName")
                            .remove("password")
                            .remove("memId")
                            .remove("login").apply();
                    Fragment fragment = new HotelFragment();
                    Util.switchFragment(getActivity(), fragment);
                    Util.showToast(getContext(),"登出了喔! 屁孩");
                }
            }
        });
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        showMemInfo();
    }

    private void showMemInfo(){
        SharedPreferences pref = getActivity().getSharedPreferences(Common.PREF_FILE,
                MODE_PRIVATE);
        String  id = pref.getString("memId", null);
        Log.d("-----****--", "id" + id);
        if(id != null){
            if(Common.networkConnected(getActivity())){
                String url = Common.URL + "/android/mem.do";
                MemVO memVO = null;
                try{
                   memVO = new MemGetOneTask().execute(url, id).get();
                }catch(Exception e){
                    Log.e("MemberInfo", e.toString());
                }

                memName.setText(memVO.getMemName());
                memLiveBudget.setText(memVO.getMemLiveBudget().toString());
                memIntro.setText(memVO.getMemIntro());
                if(memVO.getMemGender().equals("F") || memVO.getMemGender().equals("f")){
                    memGender.setText("女");
                }else{
                    memGender.setText("男");
                }
            }
        }

    }
}
