package com.example.sam.drawerlayoutprac.Partner;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.sam.drawerlayoutprac.Common;
import com.example.sam.drawerlayoutprac.Util;

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

        SharedPreferences preferences_r = this.context.getSharedPreferences(Common.PREF_FILE, this.context.MODE_PRIVATE);
        String memId = null;
        String tokenId = null;

        if (preferences_r != null) {
            memId = preferences_r.getString("memId", null);
            tokenId = preferences_r.getString("tokenId", null);
        }
        if (memId != null && tokenId != null) {
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
            dataMap.put("memId", memId);
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
            Log.d("TokenIdWebSocket","tokenId and memid sent to Server: "
                  + TokenIdWebSocket.this.dataMap.get("memId") + " - "
                  + TokenIdWebSocket.this.dataMap.get("tokenId") );
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