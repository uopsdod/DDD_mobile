package com.example.sam.drawerlayoutprac.Partner;


import org.apache.commons.beanutils.BeanUtils;

import java.lang.reflect.InvocationTargetException;
import java.security.Timestamp;

/**
 * Created by cuser on 2016/11/11.
 */
// 這個類別現在被兩個.java使用:
// 1. TokenIdWebSocket.java
// 2. PartnerChatFragment.java
public class PartnerMsg{
    String action;
    String tokenId;
    String toMemId;
    // MemChatVO
    private String memChatChatId; // NOT NULL
    private String memChatMemId; // NOT NULL
    private java.sql.Timestamp memChatDate;
    private String memChatContent;
    private byte[] memChatPic;
    // End of MemChatVO

    public PartnerMsg(){
    }
//    // 測試:看能不能這樣直接copy
//    public PartnerMsg(MemChatVO aMemChatVO) throws InvocationTargetException, IllegalAccessException {
//        BeanUtils.copyProperties(this,aMemChatVO);
//    }
    public PartnerMsg(String action, String tokenId, String toMemId, String memChatChatId, String memChatMemId, java.sql.Timestamp memChatDate, String memChatContent, byte[] memChatPic) {
        this.action = action;
        this.tokenId = tokenId;
        this.toMemId = toMemId;
        this.memChatChatId = memChatChatId;
        this.memChatMemId = memChatMemId;
        this.memChatDate = memChatDate;
        this.memChatContent = memChatContent;
        this.memChatPic = memChatPic;
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

    public String getToMemId() {
        return toMemId;
    }

    public void setToMemId(String toMemId) {
        this.toMemId = toMemId;
    }

    public String getMemChatChatId() {
        return memChatChatId;
    }

    public void setMemChatChatId(String memChatChatId) {
        this.memChatChatId = memChatChatId;
    }

    public String getMemChatMemId() {
        return memChatMemId;
    }

    public void setMemChatMemId(String memChatMemId) {
        this.memChatMemId = memChatMemId;
    }

    public java.sql.Timestamp getMemChatDate() {
        return memChatDate;
    }

    public void setMemChatDate(java.sql.Timestamp memChatDate) {
        this.memChatDate = memChatDate;
    }

    public String getMemChatContent() {
        return memChatContent;
    }

    public void setMemChatContent(String memChatContent) {
        this.memChatContent = memChatContent;
    }

    public byte[] getMemChatPic() {
        return memChatPic;
    }

    public void setMemChatPic(byte[] memChatPic) {
        this.memChatPic = memChatPic;
    }
}
