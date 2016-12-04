package com.example.sam.drawerlayoutprac.Member;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.sam.drawerlayoutprac.Common;
import com.example.sam.drawerlayoutprac.Hotel.HotelFragment;
import com.example.sam.drawerlayoutprac.MainActivity;
import com.example.sam.drawerlayoutprac.MustLoginFragment;
import com.example.sam.drawerlayoutprac.Partner.TokenIdWebSocket;
import com.example.sam.drawerlayoutprac.R;
import com.example.sam.drawerlayoutprac.Util;

import static android.content.Context.MODE_PRIVATE;

public class MemberInfoFragment extends MustLoginFragment {
    TextView memName, memGender, memLiveBudget, memIntro, tvMemName, tvMemAccount;
    ImageView ivPhoto, ivMemPhoto;
    Button btSubmit, btLogout;
    MemVO memVO;
    private DrawerLayout drawerLayout;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_member_info, container, false);
        getActivity().findViewById(R.id.floatingBtn).setVisibility(View.INVISIBLE);

        //取得navigetionView的Header與物件
        drawerLayout = (DrawerLayout) getActivity().findViewById(R.id.drawer_layout);
        final NavigationView navigationView = (NavigationView) drawerLayout.findViewById(R.id.navigation_view);
        View v1 = navigationView.getHeaderView(0);
        ivMemPhoto = (ImageView) v1.findViewById(R.id.ivMemPhoto);
        tvMemName = (TextView) v1.findViewById(R.id.tvMemName);
        tvMemAccount = (TextView) v1.findViewById(R.id.tvMemAccount);

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
                boolean login = MainActivity.pref.getBoolean("login",false);
                if(login){
                    // 當使用者登出，要去跟server說把我的tokenId去掉，以免被人騷擾:
                    new TokenIdWebSocket(getContext()).removeTokenIdFromServer();

                    MainActivity.pref.edit().remove("userName")
                            .remove("password")
                            .remove("memId")
                            .remove("login").apply();
                    //當使用者登出時，把navigationViewHeader所顯示的會員資訊設為下面的值
                    ivMemPhoto.setImageResource(R.drawable.profile_default);
                    tvMemName.setText("訪客");
                    tvMemAccount.setText("點選登入");

                    Fragment fragment = new MemberFragment();
                    Util.switchFragment(getActivity(), fragment);
                    //Util.showToast(getContext(),"登出了喔! ");
                }
            }
        });
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        showMemInfo();
        showMemProfile();
    }

    private void showMemInfo(){
        SharedPreferences pref = getActivity().getSharedPreferences(Common.PREF_FILE,
                MODE_PRIVATE);
        String  id = pref.getString("memId", null);
        Log.d("-----****--", "id" + id);
        if(id != null){
            if(Common.networkConnected(getActivity())){
                String url = Common.URL + "/android/mem.do";
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

    private void showMemProfile(){
        String id =MainActivity.pref.getString("memId", null);
        if(id != null){
            if(Common.networkConnected(getActivity())){
                String url = Common.URL + "/android/mem.do";
                int imageSize = 250;
                try{
                     new MemberGetImageTask(ivPhoto).execute(url, id, imageSize).get();
                }catch(Exception e){
                    Log.e("MemberInfo", e.toString());
                }
            }
        }
    }
}
