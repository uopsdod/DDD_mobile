package com.example.sam.drawerlayoutprac.Partner;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.icu.text.MessagePattern;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.example.sam.drawerlayoutprac.Common;
import com.example.sam.drawerlayoutprac.R;
import com.example.sam.drawerlayoutprac.Util;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.yalantis.euclid.library.EuclidState;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_17;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutionException;

/**
 * Created by cuser on 2016/11/8.
 */

public class PartnerChatFragment extends Fragment {
    public static final String TAG = "WebSocket Chat - ";
    public static final String URL_Chatroom = "ws://10.0.2.2:8081/DDD_web/android/live2/MsgCenter";

    private static final String USER_NAME = "會員一號";
    private static final String KEY_MEMID = "memId";
    private static final String KEY_MESSAGE = "message";
    private static String toMemId;
    private WebSocketClient myWebSocketClient;

    private View rootView;
    private ListView chatContent;
    private Button btnSend;
    private EditText msg;

    // history message
    List<PartnerMsg> partnerMsgList;

    PartnerChatAdapter partnerChatAdapter;

    static public Map<String, Bitmap> profileMap = new HashMap<>();
    static public Map<String, String> nameMap = new HashMap<>();

    @Override
    public void onResume() {
        super.onResume();
        backBtnPressed();

        // 建立Websocket連線 - bindMemIdWithSession
        // myWebSocketClient = new TokenIdWebSocket(getActivity(), PartnerChatFragment.this).bindMemIdWithSession();
        myWebSocketClient = new PartnerChatWebSocket().bindMemIdWithSession();

        // end of 建立Websocket連線

        // 設定send監聽器
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newMsg = PartnerChatFragment.this.msg.getText().toString();
                Util.showToast(getContext(), PartnerChatFragment.TAG + PartnerChatFragment.this.msg.getText().toString());
                if (newMsg.trim().isEmpty()) {
                    Util.showToast(getContext(), "message is empty.");
                    return;
                }
                SharedPreferences preferences_r = getActivity().getSharedPreferences(Common.PREF_FILE, getActivity().MODE_PRIVATE);
                String memid = preferences_r.getString("memId", null);
                PartnerMsg partnerMsg = new PartnerMsg();
                partnerMsg.setAction("chat");
                partnerMsg.setMemChatMemId(memid);
                partnerMsg.setMemChatToMemId(PartnerChatFragment.this.toMemId);
                partnerMsg.setMemChatContent(newMsg);
                // 在自己頁面顯示聊天視窗:
                addMsgScrollDown(partnerMsg);
                // end of 在自己頁面顯示聊天視窗

                if (myWebSocketClient != null) {
                    Gson gson = new Gson();
                    String partnerMsgGson = gson.toJson(partnerMsg);
                    myWebSocketClient.send(partnerMsgGson);
                }
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup, Bundle bundle) {
        super.onCreateView(inflater, viewGroup, bundle);
        this.rootView = inflater.inflate(R.layout.chat_containers, viewGroup, false);
        this.chatContent = (ListView) this.rootView.findViewById(R.id.chat_contents);
        this.msg = (EditText) this.rootView.findViewById(R.id.et_message);
        this.btnSend = (Button) this.rootView.findViewById(R.id.btn_send);
        // 拿toMemId的會員Id
        Bundle myBundle = getArguments();
        this.toMemId = (String) myBundle.get("ToMemId");
        Util.showToast(getContext(), "toMemId: " + this.toMemId);
        // 拿memId自己的大頭照
        SharedPreferences preferences_r = getActivity().getSharedPreferences(Common.PREF_FILE,Context.MODE_PRIVATE);
        String memId = preferences_r.getString("memId",null);
        String url = Common.URL + "/android/live2/partner.do";
        int imageSize = 100;
        Bitmap bitmap_memId = null;
        try {
            bitmap_memId = new PartnerGetImageTask(null).execute(url, memId, imageSize).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        profileMap.put(memId,bitmap_memId);
        // 拿memId自己的姓名:
        MemVO memVO_memId = null;
        try {
            memVO_memId = new PartnerGetOneTextTask().execute(url, memId).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        this.nameMap.put(memId,memVO_memId.getMemName());

        // 拿toMemId的會員大頭照
        Bitmap bitmap_toMemId = null;
        try {
            bitmap_toMemId = new PartnerGetImageTask(null).execute(url, this.toMemId, imageSize).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        profileMap.put(this.toMemId,bitmap_toMemId);
        // 拿toMemId的會員的姓名:
        MemVO memVO_toMemId = null;
        try {
            memVO_toMemId = new PartnerGetOneTextTask().execute(url, this.toMemId).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        this.nameMap.put(this.toMemId,memVO_toMemId.getMemName());

        // this.chatContent(ListView) - setAdapter here
        initMsgHistoryList();

        return this.rootView;
    }// end of onCreateView

    private void initMsgHistoryList() {

        String uri = Common.URL + "/android/live2/PartnerMsgController";
        try {
            this.partnerMsgList = new PartnerChatGetMsgTask(getContext(), PartnerChatFragment.this.toMemId).execute(uri).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        if (this.partnerMsgList != null && this.partnerMsgList.size() > 0) {
            Log.d("PartnerChatFragment", "fcm - " + this.partnerMsgList.get(0).getMemChatContent());
        }
        this.partnerChatAdapter = new PartnerChatAdapter(getContext(), this.partnerMsgList);
        this.chatContent.setAdapter(partnerChatAdapter);
        // scroll to the bottom:
        this.chatContent.post(new Runnable(){
            @Override
            public void run(){
                PartnerChatFragment.this.chatContent.setSelection(PartnerChatFragment.this.partnerChatAdapter.getCount() - 1);
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        PartnerChatFragment.this.myWebSocketClient.close();
        Log.d(PartnerChatFragment.TAG, " fcm - myWebSocketClient is closed via onPause()");
    }


    private void backBtnPressed() {
        getView().setFocusableInTouchMode(true);
        getView().requestFocus();
        getView().setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK) {
                    // close websocket
                    PartnerChatFragment.this.myWebSocketClient.close();
                    Log.d(PartnerChatFragment.TAG, "myWebSocketClient is closed via back button");
                    // 回到上一個Fragment或是離開app
                    FragmentManager fm = PartnerChatFragment.this.getFragmentManager();
                    if (fm.getBackStackEntryCount() > 0) {
                        fm.popBackStack();
                        return true;
                    }
                }// end if
                return false;
            }
        });
    }// end of backBtnPressed


    private class PartnerChatWebSocket {
        URI uri;

        public PartnerChatWebSocket() {
        }

        public WebSocketClient bindMemIdWithSession() {
            WebSocketClient tmpWebSocketClient = null;
            SharedPreferences preferences_r = getActivity().getSharedPreferences(Common.PREF_FILE, getActivity().MODE_PRIVATE);
            String memId = null;
            if (preferences_r != null) {
                memId = preferences_r.getString("memId", null);
            }
            if (memId != null) {
                URI uri = null;
                try {
                    uri = new URI(PartnerChatFragment.URL_Chatroom);
                } catch (URISyntaxException e) {
                    Log.e(PartnerChatFragment.TAG, e.toString());
                }
                this.uri = uri;
                PartnerMsg partnerMsg = new PartnerMsg();
                partnerMsg.setAction("bindMemIdWithSession");
                partnerMsg.setMemChatMemId(memId);
                tmpWebSocketClient = new PartnerChatWebSocket.MyWebSocketClient(partnerMsg);
                tmpWebSocketClient.connect();
            }
            return tmpWebSocketClient;
        }// end of bindMemIdWithSession


        private class MyWebSocketClient extends WebSocketClient {
            PartnerMsg partnerMsg;

            public MyWebSocketClient(PartnerMsg aPartnerMsg) {
                super(PartnerChatWebSocket.this.uri, new Draft_17());
                this.partnerMsg = aPartnerMsg;
            }

            @Override
            public void onOpen(ServerHandshake handshakedata) {
                Gson gson = new Gson();
                // 再判斷看要不要用
//            Gson gson = new GsonBuilder()
//                    .setDateFormat("yyyy-MM-dd hh:mm:ss.S")
//                    .create();
                String partnerMsgGson = gson.toJson(this.partnerMsg);
                this.send(partnerMsgGson);
                Log.d("TokenIdWebSocket - ", "fcm - sent to Server(" + this.partnerMsg.getAction() + "): " + partnerMsgGson);
            }

            @Override
            public void onMessage(String message) {
                // 即時通訊時使用
                // 處理Oracle Timestamp型態與gson之間的格式問題
                Gson gson = new GsonBuilder()
                        .setDateFormat("yyyy-MM-dd hh:mm:ss.S")
                        .create();
                // end of 處理Oracle Timestamp型態與gson之間的格式問題
                Log.d(TAG, "fcm - " + message);
                final PartnerMsg partnerMsg = gson.fromJson(message, PartnerMsg.class);
                // 將data放入partnerMsgList,並更新UI畫面
                // Try - 使用AsynTask加快訊息的接收
                Thread myThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
//                            Thread.sleep(1000 * (new Random().nextInt(3) + 1));
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    addMsgScrollDown(partnerMsg);
                                }
                            });
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
                myThread.start();
                try {
                    myThread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onClose(int code, String reason, boolean remote) {

            }

            @Override
            public void onError(Exception ex) {

            }
        }// end of MyWebSocketClient


    }// end of PartnerChatWebSocket

    private void addMsgScrollDown(PartnerMsg partnerMsg){
        PartnerChatFragment.this.partnerMsgList.add(partnerMsg);
        PartnerChatFragment.this.partnerChatAdapter.notifyDataSetChanged();
        // scroll down to the bottom:
        PartnerChatFragment.this.chatContent.post(new Runnable(){
            @Override
            public void run(){
                PartnerChatFragment.this.chatContent.setSelection(PartnerChatFragment.this.partnerChatAdapter.getCount() - 1);
            }
        });
    }

}
