package com.example.sam.drawerlayoutprac.Partner;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_17;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONObject;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by cuser on 2016/11/11.
 */

public class TokenIdWebSocket {

    Map<String, String> dataMap;
    Context context;
    URI uri;

    public TokenIdWebSocket(Context aContext) {
        this.context = aContext;
    }
    public void sendTokenIdToServer(){
        SharedPreferences preferences_r = this.context.getSharedPreferences("preferences_yo", this.context.MODE_PRIVATE);
        String memid_yo = null;
        String tokenId = null;
        if (preferences_r != null) {
            memid_yo = preferences_r.getString("memId_yo", null);
            tokenId = preferences_r.getString("tokenId", null);
        }
        if (memid_yo != null && tokenId != null) {
            URI uri = null;
            try {
                uri = new URI(PartnerChatFragment.URL_Chatroom);
            } catch (URISyntaxException e) {
                Log.e(PartnerChatFragment.TAG, e.toString());
            }
            this.uri = uri;
            Map<String, String> dataMap = new HashMap<>();
            dataMap.put("action", "uploadTokenId");
            dataMap.put("tokenId", tokenId);
            dataMap.put("memId", memid_yo);
            this.dataMap = dataMap; // 重點
            new TokenIdWebSocketClient().connect();
        }
    }


    private class TokenIdWebSocketClient extends WebSocketClient{
        
        public TokenIdWebSocketClient(){
            super(TokenIdWebSocket.this.uri,new Draft_17());
        }

        @Override
        public void onOpen(ServerHandshake handshakedata) {
            this.send(new JSONObject(TokenIdWebSocket.this.dataMap).toString());
            Log.d("TokenIdWebSocket","token Sent to the server");
        }

        @Override
        public void onMessage(String message) {
            this.close();
        }

        @Override
        public void onClose(int code, String reason, boolean remote) {

        }

        @Override
        public void onError(Exception ex) {

        }   
    }
}