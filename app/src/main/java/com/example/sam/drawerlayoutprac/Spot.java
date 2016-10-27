package com.example.sam.drawerlayoutprac;

import java.io.Serializable;

/**
 * Created by cuser on 2016/9/12.
 */
public class Spot implements Serializable{
    private int imgId;
    private String hotelName;
    private int price;

    public Spot(int pic, String hotelName, int price) {
        this.imgId = pic;
        this.hotelName = hotelName;
        this.price = price;
    }

    public int getimgId() {
        return imgId;
    }

    public void setimgId(int pic) {
        this.imgId = pic;
    }

    public String getHotelName() {
        return hotelName;
    }

    public void setHotelName(String hotelName) {
        this.hotelName = hotelName;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }
}
