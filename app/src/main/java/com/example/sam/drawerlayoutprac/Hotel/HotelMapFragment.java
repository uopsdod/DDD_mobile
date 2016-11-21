package com.example.sam.drawerlayoutprac.Hotel;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.sam.drawerlayoutprac.Common;
import com.example.sam.drawerlayoutprac.CommonFragment;
import com.example.sam.drawerlayoutprac.MainActivity;
import com.example.sam.drawerlayoutprac.Partner.PartnerMapFragment;
import com.example.sam.drawerlayoutprac.R;
import com.example.sam.drawerlayoutprac.Util;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;

/**
 * Created by cuser on 2016/11/21.
 */

public class HotelMapFragment extends CommonFragment {

    MapView mMapView;
    private GoogleMap googleMap;
    private GoogleApiClient googleApiClient;
    private Location lastLocation;
    private Marker CurrLocationMarker;
    private GoogleApiClient.ConnectionCallbacks myConnectionCallBacks =
            new GoogleApiClient.ConnectionCallbacks() {

                @Override
                public void onConnected(@Nullable Bundle bundle) {
                    Log.i("PartnerMapFragment", "GoogleApiClient connected.");
                    // 第一次: 取得最新位置
                    if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                            && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    HotelMapFragment.this.lastLocation = LocationServices.FusedLocationApi.getLastLocation(HotelMapFragment.this.googleApiClient);
                    Log.d("HotelMapFragment", "init lastLacation: " + HotelMapFragment.this.lastLocation);
                    // end of 第一次: 取得最新位置

                    // 自動移到自己位置 (特別注意: 一定要在確認拿到lastLocation之後，才用mMapView.getMapAsync，不然會crush)
                    if (lastLocation != null) {
                        mMapView.getMapAsync(HotelMapFragment.this.myOnMapReadyCallback);// end of getMapAsync
                    } else {
                        Log.d("HotelMapFragment", "can't get lastLocation");
                    }// end of if

                    // 設定定位請求參數
                    LocationRequest locationRequest = LocationRequest.create()
                            // 設定使用GPS定位
                            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                            // 設定多久查詢一次定位，單位為毫秒
                            .setInterval(1000)
                            // 設定離上次定位達多少公尺後，才到表更新位置
                            .setSmallestDisplacement(1000);
                    // end of 設定定位請求參數

                    // 設定locationListener監聽器物件
                    LocationListener locationListener = new LocationListener() {
                        @Override
                        public void onLocationChanged(Location aLocation) {
                            HotelMapFragment.this.lastLocation = aLocation;
                            uploadCurrentPosToServer();
                            Log.d("PartnerMapFragment", "changed lastLacation: " + HotelMapFragment.this.lastLocation);

                        }
                    };
                    // end of 設定locationListener監聽器物件

                    // 總totla註冊-設定監聽位置是否改變監聽器:
                    LocationServices.FusedLocationApi.requestLocationUpdates(HotelMapFragment.this.googleApiClient,
                            locationRequest,
                            locationListener);


//                    //移動到現在位置
//                    LatLng latLng = new LatLng(PartnerMapFragment.this.lastLocation.getLatitude(), PartnerMapFragment.this.lastLocation.getLongitude());
//                    //Place current location marker
//                    PartnerMapFragment.this.CurrLocationMarker = placeMarkerAt(latLng);
//                    // For zooming automatically to the location of the marker
//                    moveToLocation(latLng,12);
//
//                    // 將初始位置資訊傳給server:
//                    uploadCurrentPosToServer();


                }// end of onConnected()

                @Override
                public void onConnectionSuspended(int i) {
                    Util.showToast(getContext(), "GoogleApiClient connection suspended");
                }
            };

    private void uploadCurrentPosToServer() {
    }


    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
        this.googleApiClient.connect();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (this.googleApiClient == null) {
            this.googleApiClient = new GoogleApiClient.Builder(getContext())
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(myConnectionCallBacks)
                    //.addOnConnectionFailedListener(onConnetionFailListener)
                    .build();
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.hotel_map_fragment, container, false);

        mMapView = (MapView) rootView.findViewById(R.id.hotelMap);
        mMapView.onCreate(savedInstanceState); //接收傳入此Fragment
        mMapView.onResume(); // needed to get the map to display immediately

        // 設定floatingBtn click lisner - 讓它返回到上一個Fragment
        setUpFloatingBtn();
        // end - 設定floatingBtn click lisner

        try {
            // 如果要使用CameraUpdateFactory，要確保兩件事情:
            // 1. map物件不為空
            // 2. 使用MapsInitializer:
            //    Initializes the Google Maps Android API so that its classes are ready for use.
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }


        return rootView;
    }



    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
        if (this.googleApiClient != null) {
            this.googleApiClient.disconnect();
            Log.i("PartnerMapFragment", "GoogleApiClient disconnected.");
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }

    private Marker placeMemMarkerAt(LatLng aLatLng) {
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(aLatLng);
        markerOptions.title("Current Position");
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
        return HotelMapFragment.this.googleMap.addMarker(markerOptions);
    }

    private Marker placeMarkerAt(LatLng aLatLng) {

        Bitmap.Config conf = Bitmap.Config.ARGB_8888;
        Bitmap bmp = Bitmap.createBitmap(400, 100, conf);
        Canvas canvas1 = new Canvas(bmp);

// paint defines the text color, stroke width and size
        Paint color = new Paint();
        color.setTextSize(35);
        color.setColor(Color.BLACK);

// modify canvas
        canvas1.drawBitmap(BitmapFactory.decodeResource(getResources(),
                R.drawable.hotel_price02), 70, 0, color);
        canvas1.drawText("Hotel Name!", 95, 60, color);

// add marker to Map
        return HotelMapFragment.this.googleMap.addMarker(new MarkerOptions()
                .position(aLatLng)
                .icon(BitmapDescriptorFactory.fromBitmap(bmp))
                // Specifies the anchor to be at a particular point in the marker image.
                .anchor(0.5f, 1));

        //現在版本:
//        MarkerOptions markerOptions = new MarkerOptions();
//        markerOptions.position(aLatLng);
//        markerOptions.title("Current Position");
//        //markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
//        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.hotel_price02));
//
//
//        return PartnerMapFragment.this.googleMap.addMarker(markerOptions);
    }

    private void moveToLocation(LatLng aLatLng, int aZoomSize) {
        CameraPosition cameraPosition = new CameraPosition.Builder().target(aLatLng).zoom(aZoomSize).build();
        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

    }

    private void setUpFloatingBtn() {
        MainActivity.floatingBtn.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.list_white05));
        MainActivity.floatingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Util.showToast(getContext(), "ftBtn clicked");
                MainActivity.floatingBtn.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.map_white));
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                fragmentManager.popBackStack();
            }
        });

    }


    private OnMapReadyCallback myOnMapReadyCallback = new OnMapReadyCallback() {

        @Override
        public void onMapReady(GoogleMap mMap) {
            HotelMapFragment.this.googleMap = mMap;
            // 回到自己現在位置
            goToCurrPosition();
            // 顯示馬上到現在位置的按鈕
            showGoToCurrPositionBtn();
            // 顯示所有hotel的marker
            showHotelMarkers();
            


        }// end of onMapReady

        private void showHotelMarkers() {
            List<HotelVO> hotelVOList = null;
            if(Common.networkConnected(getActivity())) {
                String url = Common.URL + "/android/hotel.do";
                try {
                    hotelVOList = new HotelGetAllTask().execute(url).get();
                } catch (Exception e) {
                    Log.e("HotelMapFragment", e.toString());
                }
                if(hotelVOList == null || hotelVOList.isEmpty()){
                    Util.showToast(getActivity(), "No hotel fonnd");
                }else{
                    Log.d("HotelMapFragment",""+hotelVOList.get(0).getHotelName());
                }
            }

            for (HotelVO myVO: hotelVOList){
                LatLng latlng = new LatLng(myVO.getHotelLat(),myVO.getHotelLon());
                placeMarkerAt(latlng);
            }


        }

        private void showGoToCurrPositionBtn() {
            if (ActivityCompat.checkSelfPermission(
                    getContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            googleMap.setMyLocationEnabled(true);
            // 設定點擊觸發事件
            googleMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
                @Override
                public boolean onMyLocationButtonClick() {
                    Log.d("PartnerMapFragment", "onMyLocationButtonClick");
                    LatLng latLng = new LatLng(HotelMapFragment.this.lastLocation.getLatitude(), HotelMapFragment.this.lastLocation.getLongitude());
                    //Place current location marker
                    if (HotelMapFragment.this.CurrLocationMarker != null) {
                        HotelMapFragment.this.CurrLocationMarker.remove();
                    }
                    ;
                    HotelMapFragment.this.CurrLocationMarker = placeMemMarkerAt(latLng);
                    // For zooming automatically to the location of the marker
                    moveToLocation(latLng, 12);
                    return false;
                }
            });
        }

        private void goToCurrPosition() {
            LatLng latLng = new LatLng(HotelMapFragment.this.lastLocation.getLatitude(), HotelMapFragment.this.lastLocation.getLongitude());
            //Place current location marker
            if (HotelMapFragment.this.CurrLocationMarker != null) {
                HotelMapFragment.this.CurrLocationMarker.remove();
            }
            HotelMapFragment.this.CurrLocationMarker = placeMemMarkerAt(latLng);
            // For zooming automatically to the location of the marker
            moveToLocation(latLng, 12);
        }


    };// end of myOnMapReadyCallback



}
