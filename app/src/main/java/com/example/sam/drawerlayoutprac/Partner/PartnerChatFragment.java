package com.example.sam.drawerlayoutprac.Partner;

import android.content.SharedPreferences;
import android.icu.text.MessagePattern;
import android.os.Bundle;
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

import com.example.sam.drawerlayoutprac.Common;
import com.example.sam.drawerlayoutprac.R;
import com.example.sam.drawerlayoutprac.Util;
import com.google.gson.Gson;
import com.yalantis.euclid.library.EuclidState;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_17;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

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
    private LinearLayout chatContet;
    private Button btnSend;
    private EditText msg;

    @Override
    public void onResume() {
        super.onResume();
        backBtnPressed();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup, Bundle bundle) {
        super.onCreateView(inflater, viewGroup, bundle);
        this.rootView = inflater.inflate(R.layout.chat_containers, viewGroup, false);
        this.chatContet = (LinearLayout) this.rootView.findViewById(R.id.chat_contents);
        this.msg = (EditText) this.rootView.findViewById(R.id.et_message);
        this.btnSend = (Button) this.rootView.findViewById(R.id.btn_send);

        // 拿toMemId的會員Id
        Bundle myBundle = getArguments();
        this.toMemId = (String)myBundle.get("memId");
        Util.showToast(getContext(),"toMemId: " + this.toMemId);


        // 建立Websocket連線 - bindMemIdWithSession
        myWebSocketClient = new TokenIdWebSocket(getContext()).bindMemIdWithSession();

        // end of 建立Websocket連線
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newMsg = PartnerChatFragment.this.msg.getText().toString();
                Util.showToast(getContext(), PartnerChatFragment.TAG + PartnerChatFragment.this.msg.getText().toString());
                if (newMsg.trim().isEmpty()) {
                    Util.showToast(getContext(), "message is empty.");
                    return;
                }
                SharedPreferences preferences_r = getActivity().getSharedPreferences(Common.PREF_FILE,getActivity().MODE_PRIVATE);
                String memid = preferences_r.getString("memId", null);
                PartnerMsg partnerMsg = new PartnerMsg();
                partnerMsg.setAction("chat");
                partnerMsg.setFromMemId(memid);
                partnerMsg.setToMemId(PartnerChatFragment.this.toMemId);
                partnerMsg.setMessage(newMsg);

                if (myWebSocketClient != null) {
                    Gson gson = new Gson();
                    String partnerMsgGson = gson.toJson(partnerMsg);
                    myWebSocketClient.send(partnerMsgGson);
                }
            }
        });

        //Util.showToast(getActivity().getApplicationContext(), "current memid:  " + memid);
        return this.rootView;
    }// end of onCreateView



    private void backBtnPressed() {
        getView().setFocusableInTouchMode(true);
        getView().requestFocus();
        getView().setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK) {
                    //Util.showToast(getContext(),"backBtnPressed");
                    FragmentManager fm = PartnerChatFragment.this.getFragmentManager();
                    fm.popBackStack();
                    // close websocket
                    PartnerChatFragment.this.myWebSocketClient.close();
                    Log.d(PartnerChatFragment.TAG,"myWebSocketClient is closed");
                }
                return true;
            }
        });
    }


    public class MyWebSocketClient extends WebSocketClient {
        Map<String, String> dataMap;

        public MyWebSocketClient(URI serverURI, Map<String, String> aDataMap) {
            super(serverURI, new Draft_17());
            this.dataMap = aDataMap;
        }

        @Override
        public void onOpen(ServerHandshake handshakedata) {
            Log.d(TAG, "onOpen: handshakedata.toString() = " + handshakedata.toString());
            if (myWebSocketClient != null) {
                myWebSocketClient.send(new JSONObject(this.dataMap).toString());
            }
        }

        @Override
        public void onMessage(final String message) {
            Log.d(TAG, "onMessage: " + message);
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        JSONObject jsonObject = new JSONObject(message);
                        String userName = jsonObject.get(KEY_MEMID).toString();
                        String message = jsonObject.get(KEY_MESSAGE).toString();
                        String text = userName + ": " + message + "\n";
                        Log.d(TAG, text);
                    } catch (JSONException e) {
                        Log.e(TAG, e.toString());
                    }
                }
            });
        }

        @Override
        public void onClose(int code, String reason, boolean remote) {
            String text = String.format(Locale.getDefault(),
                    "code = %d, reason = %s, remote = %b",
                    code, reason, remote);
            Log.d(TAG, "onClose: " + text);
        }

        @Override
        public void onError(Exception ex) {
            Log.d(TAG, "onError: exception = " + ex.toString());
        }
    }


}
