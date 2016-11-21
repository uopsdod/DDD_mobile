package com.example.sam.drawerlayoutprac.Hotel;

import android.app.Activity;
import android.util.Log;

import com.example.sam.drawerlayoutprac.Partner.Chat.ChatFragment;
import com.example.sam.drawerlayoutprac.Partner.VO.PartnerMsg;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_17;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;

/**
 * Created by cuser on 2016/11/21.
 */

public class HotelMapWebsocket extends WebSocketClient{
        URI uri;
        Activity activity;

        public HotelMapWebsocket(URI aUri,Activity aActivity ) {
            super(aUri, new Draft_17());
            this.uri = aUri;
            this.activity = aActivity;
        }

        @Override
        public void onOpen(ServerHandshake handshakedata) {
            Log.d("hotelMapWebsocket - ", " open websocket successfully ");
        }

        @Override
        public void onMessage(String message) {
            // 即時通訊時使用
            // 處理Oracle Timestamp型態與gson之間的格式問題
//            Gson gson = new GsonBuilder()
//                    .setDateFormat("yyyy-MM-dd hh:mm:ss.S")
//                    .create();
            // end of 處理Oracle Timestamp型態與gson之間的格式問題
            Log.d("HotelMapWebsocket", "" + message);
//            final PartnerMsg partnerMsg = gson.fromJson(message, PartnerMsg.class);
            // 將data放入partnerMsgList,並更新UI畫面
            // Try - 使用AsynTask加快訊息的接收
//            Thread myThread = new Thread(new Runnable() {
//                @Override
//                public void run() {
//                    try {
//                        HotelMapWebsocket.this.activity.runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//
//                            }
//                        });
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                }
//            });
//            myThread.start();
//            try {
//                myThread.join();
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
        }

        @Override
        public void onClose(int code, String reason, boolean remote) {

        }

        @Override
        public void onError(Exception ex) {

        }
}
