package com.example.sam.drawerlayoutprac.Hotel;

/**
 * Created by cuser on 2016/11/23.
 */

public class HotelSearchVO {
    private String city; // "桃園市"
    private String zone; // "中壢區"
    private String hotelRatingResult; // "0"
    private String roomCapacity; // "2"
    private String Price; // "$1 - $10000"


    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getZone() {
        return zone;
    }

    public void setZone(String zone) {
        this.zone = zone;
    }

    public String getHotelRatingResult() {
        return hotelRatingResult;
    }

    public void setHotelRatingResult(String hotelRatingResult) {
        this.hotelRatingResult = hotelRatingResult;
    }

    public String getRoomCapacity() {
        return roomCapacity;
    }

    public void setRoomCapacity(String roomCapacity) {
        this.roomCapacity = roomCapacity;
    }

    public String getPrice() {
        return Price;
    }

    public void setPrice(String price) {
        Price = price;
    }
}
