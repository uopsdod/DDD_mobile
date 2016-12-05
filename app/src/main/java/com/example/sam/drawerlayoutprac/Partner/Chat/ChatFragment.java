package com.example.sam.drawerlayoutprac.Partner.Chat;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.example.sam.drawerlayoutprac.Common;
import com.example.sam.drawerlayoutprac.MainActivity;
import com.example.sam.drawerlayoutprac.CommonFragment;
import com.example.sam.drawerlayoutprac.Partner.PartnerFragment;
import com.example.sam.drawerlayoutprac.Partner.TokenIdWebSocket;
import com.example.sam.drawerlayoutprac.Partner.VO.MemVO;
import com.example.sam.drawerlayoutprac.Partner.PartnerGetOneImageTask;
import com.example.sam.drawerlayoutprac.Partner.PartnerGetOneTextTask;
import com.example.sam.drawerlayoutprac.Partner.VO.PartnerMsg;
import com.example.sam.drawerlayoutprac.Partner.PartnerGoBackState;
import com.example.sam.drawerlayoutprac.R;
import com.example.sam.drawerlayoutprac.Util;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_17;
import org.java_websocket.exceptions.WebsocketNotConnectedException;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * Created by cuser on 2016/11/8.
 */

public class ChatFragment extends CommonFragment {
    public static final String TAG = "WebSocket Chat - ";


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
    public List<PartnerMsg> partnerMsgList;
    // 用於有新訊息時，告訴adapter要更新資料到view上面了
    public PartnerChatListAdapter partnerChatAdapter;
    // 優化訊息視窗讀取順暢度
    static public Map<String, Bitmap> profileMap = new HashMap<>();
    static public Map<String, String> nameMap = new HashMap<>();

    private int msgCountAdded = 0;

    @Override
    public void onResume() {
        super.onResume();
        PartnerFragment.backBtnPressed_fromChat = PartnerGoBackState.SWITCH_VIA_NAVIGATIONBAR; // 0: 從頭開始, 1: 正常回來, 2: 不正常回來(預設狀態)
        backBtnPressed();
        MainActivity.floatingBtn.setVisibility(View.INVISIBLE);

        // 建立Websocket連線 - bindMemIdWithSession
        // myWebSocketClient = new TokenIdWebSocket(getActivity(), ChatFragment.this).bindMemIdWithSession();
        myWebSocketClient = new PartnerChatWebSocket().bindMemIdWithSession();

        // end of 建立Websocket連線

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup, Bundle bundle) {
        super.onCreateView(inflater, viewGroup, bundle);
        this.rootView = inflater.inflate(R.layout.chat_containers, viewGroup, false);
        this.chatContent = (ListView) this.rootView.findViewById(R.id.chat_contents);
        this.msg = (EditText) this.rootView.findViewById(R.id.et_message);
        this.btnSend = (Button) this.rootView.findViewById(R.id.btn_send);

        // 拿兩會員的memId以及大頭照 - 加快訊息視窗讀取順暢度
        initTwoMemData();
        // this.chatContent(ListView) - setAdapter here
        initMsgHistoryList();

        // 設定send監聽器
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newMsg = ChatFragment.this.msg.getText().toString();
                //Util.showToast(getContext(), ChatFragment.TAG + ChatFragment.this.msg.getText().toString());
                if (newMsg.trim().isEmpty()) {
                    Util.showToast(getContext(), "message is empty.");
                    return;
                }
                SharedPreferences preferences_r = getActivity().getSharedPreferences(Common.PREF_FILE, getActivity().MODE_PRIVATE);
                String memid = preferences_r.getString("memId", null);
                PartnerMsg partnerMsg = new PartnerMsg();
                partnerMsg.setAction("chat");
                partnerMsg.setMemChatMemId(memid);
                partnerMsg.setMemChatToMemId(ChatFragment.this.toMemId);
                partnerMsg.setMemChatContent(newMsg);
                partnerMsg.setMemChatDate(new Timestamp(new java.util.Date().getTime()));
                partnerMsg.setFromMobile("fromMobile");
                // 在自己頁面顯示聊天視窗:
                addMsgScrollDown(partnerMsg);
                // end of 在自己頁面顯示聊天視窗

                if (myWebSocketClient != null) {
                    Log.d(TAG, "myWebSocketClient.getConnection() - " + myWebSocketClient.getConnection());
//                    Gson gson = new Gson();
                    Gson gson = new GsonBuilder()
                            .setDateFormat("yyyy-MM-dd hh:mm:ss.S")
                            .create(); // 注意:如果VO中有Date,Timestamp，就要Server,Client端規格一致
                    String partnerMsgGson = gson.toJson(partnerMsg);
                    try {
                        myWebSocketClient.send(partnerMsgGson);
                        Log.d(ChatFragment.TAG, "" + partnerMsgGson);
                    } catch (WebsocketNotConnectedException e) {
                        // 如果websocket連線已中斷，幫使用者偷偷的再重新連線一次，並將訊息再次寄出 1
                        Util.showToast(getContext(), "Not connected to Server. Please relogin.");
                        new TokenIdWebSocket(getContext()).sendTokenIdToServer();
                            // 若在bindMemIdWithSession(partnerMsg);有放入partnerMsg物件，則代表這次的binding要加上resend的動作 2
                        myWebSocketClient = new PartnerChatWebSocket().bindMemIdWithSession(partnerMsg);
                    }

                }
            }
        });

        return this.rootView;
    }// end of onCreateView

    private void initTwoMemData() {
        // 拿toMemId的會員Id
        Bundle myBundle = getArguments();
        this.toMemId = (String) myBundle.get("ToMemId");
        //Util.showToast(getContext(), "toMemId: " + this.toMemId);
        // 拿memId自己的會員Id
        SharedPreferences preferences_r = getActivity().getSharedPreferences(Common.PREF_FILE, Context.MODE_PRIVATE);
        String memId = preferences_r.getString("memId", null);
        // 拿memId自己的大頭照
        String url = Common.URL_Partner;
        int imageSize = 150;
        Bitmap bitmap_memId = getProfileBigmap(url, memId, imageSize);

        profileMap.put(memId, bitmap_memId);
        // 拿memId自己的姓名:
        MemVO memVO_memId = getMemVO(url, memId);
        this.nameMap.put(memId, memVO_memId.getMemName());

        // 拿toMemId的會員大頭照
        Bitmap bitmap_toMemId = getProfileBigmap(url, this.toMemId, imageSize);
        profileMap.put(this.toMemId, bitmap_toMemId);
        // 拿toMemId的會員的姓名:
        MemVO memVO_toMemId = getMemVO(url, this.toMemId);
        this.nameMap.put(this.toMemId, memVO_toMemId.getMemName());
    }

    private void initMsgHistoryList() {

        String uri = Common.URL_PartnerMsgController;
        try {
            this.partnerMsgList = new PartnerChatGetMsgTask(getContext(), ChatFragment.this.toMemId).execute(uri).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        if (this.partnerMsgList != null && this.partnerMsgList.size() > 0) {
            Log.d(ChatFragment.TAG, "" + this.partnerMsgList.get(0).getMemChatContent());
        }
        this.partnerChatAdapter = new PartnerChatListAdapter(getContext(), this.partnerMsgList);
        this.chatContent.setAdapter(partnerChatAdapter);
        // scroll to the bottom:
        this.chatContent.post(new Runnable() {
            @Override
            public void run() {
                ChatFragment.this.chatContent.setSelection(ChatFragment.this.partnerChatAdapter.getCount() - 1);
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        ChatFragment.this.myWebSocketClient.close();
        Log.d(ChatFragment.TAG, "myWebSocketClient is closed via onPause()");

        SharedPreferences pref = getContext().getSharedPreferences(Common.PREF_FILE, Context.MODE_PRIVATE);
        String memId = pref.getString("memId", null);
        String msgCountMax = pref.getString("msgCountMax"+memId+toMemId,null);
        String msgCountCurr = Integer.toString(Integer.parseInt(msgCountMax) + msgCountAdded);
//        msgCountMax = Integer.toString(Integer.parseInt(msgCountMax) + msgCountAdded);
        pref.edit().putString("msgCountCurr"+toMemId+memId,msgCountCurr).apply();
        pref.edit().putString("msgCountCurr"+memId+toMemId,msgCountCurr).apply();
        msgCountAdded = 0;
    }

    private class PartnerChatWebSocket {
        URI uri;

        public PartnerChatWebSocket() {

        }
        public WebSocketClient bindMemIdWithSession(){
            return bindMemIdWithSession(null);
        }

        public WebSocketClient bindMemIdWithSession(PartnerMsg aPartnerMsgResend) {
            WebSocketClient tmpWebSocketClient = null;
            SharedPreferences preferences_r = getActivity().getSharedPreferences(Common.PREF_FILE, getActivity().MODE_PRIVATE);
            String memId = null;
            if (preferences_r != null) {
                memId = preferences_r.getString("memId", null);
            }
            if (memId != null) {
                URI uri = null;
                try {
                    uri = new URI(Common.URL_Chatroom);
                } catch (URISyntaxException e) {
                    Log.e(ChatFragment.TAG, e.toString());
                }
                this.uri = uri;
                PartnerMsg partnerMsg = new PartnerMsg();
                partnerMsg.setAction("bindMemIdWithSession");
                partnerMsg.setMemChatMemId(memId);
                partnerMsg.setMemChatToMemId(ChatFragment.this.toMemId);
                // 將要resend的物件放入建構式中 3
                tmpWebSocketClient = new PartnerChatWebSocket.MyWebSocketClient(partnerMsg, aPartnerMsgResend);
                tmpWebSocketClient.connect();
            }
            return tmpWebSocketClient;
        }// end of bindMemIdWithSession


        private class MyWebSocketClient extends WebSocketClient {
            PartnerMsg partnerMsg;
            PartnerMsg partnerMsgResend;

            public MyWebSocketClient(PartnerMsg aPartnerMsg, PartnerMsg aPartnerMsgResend) {
                super(PartnerChatWebSocket.this.uri, new Draft_17());
                this.partnerMsg = aPartnerMsg;
                // 將要resend的物件用實體變數接起來 4
                this.partnerMsgResend = aPartnerMsgResend;
            }

            @Override
            public void onOpen(ServerHandshake handshakedata) {
                Gson gson = new Gson();
                // 再判斷看要不要用
//             Gson gson = new GsonBuilder()
//                    .setDateFormat("yyyy-MM-dd hh:mm:ss.S")
//                    .create();

                // 在此判斷如是否有要resend的物件，如果有則在要透過bindMemIdWithSession中加入resend字串 5
                // Server端會收到有著resend字串的Message，透過bindMemIdWithSession中判斷後，如果有則會再將message寄回來 6
                if (MyWebSocketClient.this.partnerMsgResend != null) {
                    this.partnerMsg.setResend("resend");
                }
                String partnerMsgGson = gson.toJson(this.partnerMsg);
                this.send(partnerMsgGson);
                Log.d(ChatFragment.TAG, "" + this.partnerMsg.getAction() + "): " + partnerMsgGson);


            }

            @Override
            public void onMessage(String message) {
                // 即時通訊時使用
                // 處理Oracle Timestamp型態與gson之間的格式問題
                Gson gson = new GsonBuilder()
                        .setDateFormat("yyyy-MM-dd hh:mm:ss.S")
                        .create();
                // end of 處理Oracle Timestamp型態與gson之間的格式問題
                Log.d(TAG, "" + message);
                final PartnerMsg partnerMsg = gson.fromJson(message, PartnerMsg.class);

                // 如果是重新連線要重送的話:
                // 判斷傳回來的的Message是否有resend字串，如果有則進行重送 7
                if (partnerMsg.getResend() != null) {
                    String partnerMsgResendGson = gson.toJson(MyWebSocketClient.this.partnerMsgResend);
                    MyWebSocketClient.this.send(partnerMsgResendGson);
                    Log.d(TAG,"resend msg " + partnerMsgResendGson);
                    return;
                }


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
                Log.d(ChatFragment.TAG, " myWebSocketClient is closed via onClose()");
            }

            @Override
            public void onError(Exception ex) {

            }
        }// end of MyWebSocketClient


    }// end of PartnerChatWebSocket

    private void addMsgScrollDown(PartnerMsg partnerMsg) {
        ChatFragment.this.partnerMsgList.add(partnerMsg);
        ChatFragment.this.partnerChatAdapter.notifyDataSetChanged();
        msgCountAdded++;
        // scroll down to the bottom:
        ChatFragment.this.chatContent.post(new Runnable() {
            @Override
            public void run() {
                ChatFragment.this.chatContent.setSelection(ChatFragment.this.partnerChatAdapter.getCount() - 1);
            }
        });
    }

    private Bitmap getProfileBigmap(String aUrl, String aMemId, Integer aImageSize) {
        Bitmap bitmap_memId = null;
        try {
            bitmap_memId = new PartnerGetOneImageTask(null).execute(aUrl, aMemId, aImageSize).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return bitmap_memId;
    }

    private MemVO getMemVO(String aUrl, String aMemId) {
        MemVO memVO_memId = null;
        try {
            memVO_memId = new PartnerGetOneTextTask().execute(aUrl, aMemId).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return memVO_memId;
    }


    private void backBtnPressed() {
        getView().setFocusableInTouchMode(true);
        getView().requestFocus();
        getView().setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // 為了以下這行而複寫backBtnPressed()
                PartnerFragment.backBtnPressed_fromChat = PartnerGoBackState.BACKBTN_PRESSED;
                if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK) {
                    // 回到上一個Fragment或是離開app
                    FragmentManager fm = ChatFragment.this.getFragmentManager();
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


/* 測試驗收流程

1. 會員01登入:
* 檢查是否上傳token
2. 會員01向會員02傳訊息：
* 檢查是否建立session
3. 會員01保持登入，將server重啟，會員01再向會員02傳訊息：
* 檢查是否跑resend流程
* 檢查是否再次上傳token
* 檢查是否再次建立session
4. 會員01保持登入，會員02也登入:
* 檢查前面的訊息是否皆能再會員02的歷史訊息中看到
5. 會員01保持登入，會員02也登入，但會員02不在聊天頁面，會員01向會員02傳訊息:
* 檢查會員02是否收到FCM notification
* 檢查會員02點擊notification後是否跳轉到聊天頁面
6. 會員01保持登入，會員02也登入，且兩者都在同一個聊天頁面，會員01與會員02互相傳訊息:
* 檢查會員02與會員01是否能即時收到訊息
7. 會員01保持登入，會員02也保持登入，但改成會員01向會員03傳訊息:
* 檢查是否會員02"不會"收到notification
* 檢查是否會員02也"不會"收到即時訊息

 */