package com.example.sam.drawerlayoutprac.Partner;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.sam.drawerlayoutprac.MainActivity;
import com.example.sam.drawerlayoutprac.MustLoginFragment;
import com.example.sam.drawerlayoutprac.R;
import com.example.sam.drawerlayoutprac.Util;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.MapsInitializer;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by cuser on 2016/10/31.
 */

public class PartnerMapFragment extends MustLoginFragment {

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
                    PartnerMapFragment.this.lastLocation = LocationServices.FusedLocationApi.getLastLocation(PartnerMapFragment.this.googleApiClient);
                    Log.d("PartnerMapFragment", "init lastLacation: " + PartnerMapFragment.this.lastLocation);
                    // end of 第一次: 取得最新位置

                    // 自動移到自己位置 (特別注意: 一定要在確認拿到lastLocation之後，才用mMapView.getMapAsync，不然會crush)
                    if (lastLocation != null) {
                        mMapView.getMapAsync(PartnerMapFragment.this.myOnMapReadyCallback);// end of getMapAsync
                    }else{
                        Log.d("PartnerMapFragment","can't get lastLocation");
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
                            PartnerMapFragment.this.lastLocation = aLocation;
                            uploadCurrentPosToServer();
                            Log.d("PartnerMapFragment", "changed lastLacation: " + PartnerMapFragment.this.lastLocation);

                        }
                    };
                    // end of 設定locationListener監聽器物件

                    // 總totla註冊-設定監聽位置是否改變監聽器:
                    LocationServices.FusedLocationApi.requestLocationUpdates(PartnerMapFragment.this.googleApiClient,
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
        View rootView = inflater.inflate(R.layout.partner_map_fragment, container, false);

        mMapView = (MapView) rootView.findViewById(R.id.partnerMap);
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

    private Marker placeMarkerAt(LatLng aLatLng) {
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(aLatLng);
        markerOptions.title("Current Position");
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
        return PartnerMapFragment.this.googleMap.addMarker(markerOptions);
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

    private OnMapReadyCallback myOnMapReadyCallback = new OnMapReadyCallback() {

        @Override
        public void onMapReady(GoogleMap mMap) {
            PartnerMapFragment.this.googleMap = mMap;
            // set up floatingBtn click Listener

            // 回到自己現在位置
            if (ActivityCompat.checkSelfPermission(
                    getContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            LatLng latLng = new LatLng(PartnerMapFragment.this.lastLocation.getLatitude(), PartnerMapFragment.this.lastLocation.getLongitude());
            //Place current location marker
            if (PartnerMapFragment.this.CurrLocationMarker != null) {
                PartnerMapFragment.this.CurrLocationMarker.remove();
            }
            PartnerMapFragment.this.CurrLocationMarker = placeMarkerAt(latLng);
            // For zooming automatically to the location of the marker
            moveToLocation(latLng, 12);


            // 顯示馬上到現在位置的按鈕
            googleMap.setMyLocationEnabled(true);
            // 設定點擊觸發事件
            googleMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
                @Override
                public boolean onMyLocationButtonClick() {
                    Log.d("PartnerMapFragment", "onMyLocationButtonClick");
                    LatLng latLng = new LatLng(PartnerMapFragment.this.lastLocation.getLatitude(), PartnerMapFragment.this.lastLocation.getLongitude());
                    //Place current location marker
                    if (PartnerMapFragment.this.CurrLocationMarker != null) {
                        PartnerMapFragment.this.CurrLocationMarker.remove();
                    }
                    ;
                    PartnerMapFragment.this.CurrLocationMarker = placeMarkerAt(latLng);
                    // For zooming automatically to the location of the marker
                    moveToLocation(latLng, 12);
                    return false;
                }
            });

        }
    };// end of myOnMapReadyCallback
}