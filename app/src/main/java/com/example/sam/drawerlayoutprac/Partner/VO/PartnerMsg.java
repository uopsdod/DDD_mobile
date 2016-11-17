package com.example.sam.drawerlayoutprac.Partner.VO;

import com.example.sam.drawerlayoutprac.Partner.VO.MemChatVO;

/**
 * Created by cuser on 2016/11/11.
 */
// 這個類別現在被兩個.java使用:
// 1. TokenIdWebSocket.java
// 2. ChatFragment.java
public class PartnerMsg extends MemChatVO {
    String action;
    String tokenId;
    // MemChatVO
//    private String memChatChatId; // NOT NULL
//    private String memChatMemId; // NOT NULL
//    private java.sql.Timestamp memChatDate;
//    private String memChatContent;
//    private byte[] memChatPic;
//    private String memChatStatus;
    // End of MemChatVO

    public PartnerMsg(){
    }
//    // 測試:看能不能這樣直接copy
//    public PartnerMsg(MemChatVO aMemChatVO) throws InvocationTargetException, IllegalAccessException {
//        BeanUtils.copyProperties(this,aMemChatVO);
//    }

    public PartnerMsg(String action, String tokenId, String toMemId) {
        this.action = action;
        this.tokenId = tokenId;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getTokenId() {
        return tokenId;
    }

    public void setTokenId(String tokenId) {
        this.tokenId = tokenId;
    }
}
