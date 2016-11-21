package com.example.sam.drawerlayoutprac.Hotel;

import android.os.AsyncTask;
import android.util.Log;

import com.example.sam.drawerlayoutprac.Common;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

/**
 * Created by cuser on 2016/11/21.
 */

public class HotelGetLowestPriceVO {
    private String hotelId;
    private String hotelLat;
    private String hotelLon;
    private String hotelCheapestRoomId;
    private String hotelCheapestRoomPrice;

    public String getHotelId() {
        return hotelId;
    }

    public void setHotelId(String hotelId) {
        this.hotelId = hotelId;
    }

    public String getHotelLat() {
        return hotelLat;
    }

    public void setHotelLat(String hotelLat) {
        this.hotelLat = hotelLat;
    }

    public String getHotelLon() {
        return hotelLon;
    }

    public void setHotelLon(String hotelLon) {
        this.hotelLon = hotelLon;
    }

    public String getHotelCheapestRoomId() {
        return hotelCheapestRoomId;
    }

    public void setHotelCheapestRoomId(String hotelCheapestRoomId) {
        this.hotelCheapestRoomId = hotelCheapestRoomId;
    }

    public String getHotelCheapestRoomPrice() {
        return hotelCheapestRoomPrice;
    }

    public void setHotelCheapestRoomPrice(String hotelCheapestRoomPrice) {
        this.hotelCheapestRoomPrice = hotelCheapestRoomPrice;
    }
}