package com.example.sam.drawerlayoutprac.Partner.HistoryMsg;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.example.sam.drawerlayoutprac.Common;
import com.example.sam.drawerlayoutprac.MainActivity;
import com.example.sam.drawerlayoutprac.Partner.Chat.PartnerChatFragment;
import com.example.sam.drawerlayoutprac.Partner.PartnerCommonFragment;
import com.example.sam.drawerlayoutprac.Partner.VO.PartnerMsg;
import com.example.sam.drawerlayoutprac.R;
import com.example.sam.drawerlayoutprac.Util;

import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Created by cuser on 2016/11/14.
 */

public class PartnerHistoryMsgFragment extends PartnerCommonFragment {
    public static final String TAG = "WebSocket Chat - ";

    private LinearLayout rootView;
    private ListView listView;
    private String memId;
    private List<PartnerMsg> partnerHistoryMsgsList;

    @Override
    public void onResume() {
        super.onResume();
        backBtnPressed();
        MainActivity.floatingBtn.setVisibility(View.INVISIBLE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup, Bundle bundle) {
        super.onCreateView(inflater, viewGroup, bundle);
        this.rootView = (LinearLayout)inflater.inflate(R.layout.chat_history_containers, viewGroup, false);
        this.listView = (ListView)this.rootView.findViewById(R.id.chat_history_listview);
        // 拿memId的會員Id
        SharedPreferences preferences_r = getActivity().getSharedPreferences(Common.PREF_FILE,getActivity().MODE_PRIVATE);
        this.memId =  preferences_r.getString("memId", null);

        // this.chatContent(ListView) - setAdapter here
        initListView();
        return this.rootView;
    }// end of onCreateView

    private void initListView() {
        String uri = Common.URL + "/android/live2/PartnerMsgController";
        try {
            this.partnerHistoryMsgsList = new PartnerHistoryMsgGetMsgTask(getContext()).execute(uri).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        this.listView.setAdapter(new PartnerHistoryMsgAdapter(getContext(),this.partnerHistoryMsgsList));

        this.listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                PartnerMsg partnerMsg = (PartnerMsg)parent.getItemAtPosition(position);
                //MainActivity.floatingBtn.setVisibility(View.INVISIBLE);
                String toMemId = null; // partnerMsg.getMemChatToMemId();
                if (!partnerMsg.getMemChatToMemId().equals(memId)){
                    toMemId = partnerMsg.getMemChatToMemId().toString();
                }else{
                    toMemId = partnerMsg.getMemChatMemId().toString();
                }
                Fragment fragment = new PartnerChatFragment();
                Bundle bundle = new Bundle();
                bundle.putString("ToMemId", toMemId);
                fragment.setArguments(bundle);
                Util.switchFragment(PartnerHistoryMsgFragment.this, fragment);
            }
        });
    }


    @Override
    public void onPause() {
        super.onPause();
    }


    private void backBtnPressed() {
        getView().setFocusableInTouchMode(true);
        getView().requestFocus();
        getView().setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK) {
                    // 回到上一個Fragment或是離開app
                    FragmentManager fm = PartnerHistoryMsgFragment.this.getFragmentManager();
                    if (fm.getBackStackEntryCount() > 0) {
                        fm.popBackStack();
                        return true;
                    }
                }// end if
                return false;
            }
        });
    }// end of backBtnPressed

}
