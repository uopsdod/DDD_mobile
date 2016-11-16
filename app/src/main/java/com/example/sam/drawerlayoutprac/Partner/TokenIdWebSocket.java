package com.example.sam.drawerlayoutprac.Partner;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.sam.drawerlayoutprac.Common;
import com.example.sam.drawerlayoutprac.Partner.Chat.ChatFragment;
import com.example.sam.drawerlayoutprac.Partner.VO.PartnerMsg;
import com.google.gson.Gson;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_17;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

/**
 * Created by cuser on 2016/11/11.
 */

public class TokenIdWebSocket {

    Activity activity; //
    Context context; // 給MyFirebaseInstanceIDService用
    URI uri;
    ChatFragment partnerChatFragment;
    List<PartnerMsg> partnerMsgList;
    public TokenIdWebSocket(Context aContext) {
        this.context = aContext;
    }
    public TokenIdWebSocket(Activity aActivity) {
        this.activity = aActivity;
    }
    public TokenIdWebSocket(Activity aActivity, ChatFragment aPartnerChatFragment) {
        this.activity = aActivity;
        this.partnerChatFragment = aPartnerChatFragment;
    }
    public void sendTokenIdToServer(){

        SharedPreferences preferences_r = this.activity.getSharedPreferences(Common.PREF_FILE, this.context.MODE_PRIVATE);
        String memId = null;
        String tokenId = null;

        if (preferences_r != null) {
            memId = preferences_r.getString("memId", null);
            tokenId = preferences_r.getString("tokenId", null);
        }
        if (memId != null && tokenId != null) {
            URI uri = null;
            try {
                uri = new URI(ChatFragment.URL_Chatroom);
            } catch (URISyntaxException e) {
                Log.e(ChatFragment.TAG, e.toString());
            }
            this.uri = uri;
            PartnerMsg partnerMsg = new PartnerMsg();
            partnerMsg.setAction("uploadTokenId");
            partnerMsg.setTokenId(tokenId);
            partnerMsg.setMemChatMemId(memId);
            new MyWebSocketClient(partnerMsg).connect();
        }
    }

    public WebSocketClient bindMemIdWithSession(){
        WebSocketClient tmpWebSocketClient = null;
        SharedPreferences preferences_r = this.activity.getSharedPreferences(Common.PREF_FILE, this.activity.MODE_PRIVATE);
        String memId = null;
        String tokenId = null;

        if (preferences_r != null) {
            memId = preferences_r.getString("memId", null);
            tokenId = preferences_r.getString("tokenId", null);
        }
        if (memId != null) {
            URI uri = null;
            try {
                uri = new URI(ChatFragment.URL_Chatroom);
            } catch (URISyntaxException e) {
                Log.e(ChatFragment.TAG, e.toString());
            }
            this.uri = uri;
            PartnerMsg partnerMsg = new PartnerMsg();
            partnerMsg.setAction("bindMemIdWithSession");
            partnerMsg.setMemChatMemId(memId);
            tmpWebSocketClient = new MyWebSocketClient(partnerMsg);
            tmpWebSocketClient.connect();
        }
        return tmpWebSocketClient;
    }


    private class MyWebSocketClient extends WebSocketClient{
        PartnerMsg partnerMsg;

        public MyWebSocketClient(PartnerMsg aPartnerMsg){
            super(TokenIdWebSocket.this.uri,new Draft_17());
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
            if ("uploadTokenId".equals(this.partnerMsg.getAction())){
                Log.d("TokenIdWebSocket - ", "fcm - receive from server: " + message);
                this.close();
                return;
            }
            // 即時通訊時使用
            if (partnerChatFragment != null){
                final PartnerMsg partnerMsg = new PartnerMsg();
                partnerMsg.setMemChatContent(message);
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        partnerChatFragment.partnerMsgList.add(partnerMsg);
                        partnerChatFragment.partnerChatAdapter.notifyDataSetChanged();
                    }
                });
            }
        }

        @Override
        public void onClose(int code, String reason, boolean remote) {

        }

        @Override
        public void onError(Exception ex) {

        }
    }


}