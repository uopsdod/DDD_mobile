package com.example.sam.drawerlayoutprac.Partner;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.sam.drawerlayoutprac.Common;
import com.example.sam.drawerlayoutprac.Hotel.HotelGetLowestPriceTask;
import com.example.sam.drawerlayoutprac.Hotel.HotelGetLowestPriceVO;
import com.example.sam.drawerlayoutprac.Hotel.HotelSearchVO;
import com.example.sam.drawerlayoutprac.MainActivity;
import com.example.sam.drawerlayoutprac.MustLoginFragment;
import com.example.sam.drawerlayoutprac.Partner.VO.MemCoordVO;
import com.example.sam.drawerlayoutprac.R;
import com.example.sam.drawerlayoutprac.Util;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.data.BitmapTeleporter;
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
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.MapsInitializer;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;

/**
 * Created by cuser on 2016/10/31.
 */

public class PartnerMapFragment extends MustLoginFragment {

    // 版面配置
    private static int map_bottom_padding = 150;
    private static int map_right_padding = 32;
    private static float floatingBtnY = MainActivity.floatingBtn.getY();

    // googleMap:
    MapView mMapView;
    private GoogleMap googleMap;
    private GoogleApiClient googleApiClient;
    private Location lastLocation;

    // Markers:
    private HashMap<Marker, MemCoordVO> markerMap = new HashMap<>();
    private Marker CurrLocationMarker;

    private String memId; // onCreate

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
        // 取得memId:
        SharedPreferences preference_r = getActivity().getSharedPreferences(Common.PREF_FILE, Context.MODE_PRIVATE);
        PartnerMapFragment.this.memId = preference_r.getString("memId", null);

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
                    Log.d("HotelMapFragment", "init lastLacation: " + PartnerMapFragment.this.lastLocation);
                    // end of 第一次: 取得最新位置

                    // 自動移到自己位置 (特別注意: 一定要在確認拿到lastLocation之後，才用mMapView.getMapAsync，不然會crush)
                    if (lastLocation != null) {
                        mMapView.getMapAsync(PartnerMapFragment.this.myOnMapReadyCallback);// end of getMapAsync
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
                            // 如果是第一次安裝app，且為第一次開啟googla map，防止沒有自動定位的情況
                            if (PartnerMapFragment.this.lastLocation == null) {
                                PartnerMapFragment.this.lastLocation = aLocation;
                                mMapView.getMapAsync(PartnerMapFragment.this.myOnMapReadyCallback);
                                Log.d("PartnerMapFragment", "init lastLocation onLocationChanged callback");
                                uploadCurrentPosToServer();
                                return;
                            }

                            PartnerMapFragment.this.lastLocation = aLocation;
                            uploadCurrentPosToServer();
                            Log.d("PartnerMapFragment", "changed lastLocation: " + PartnerMapFragment.this.lastLocation);

                        }
                    };
                    // end of 設定locationListener監聽器物件

                    // 總totla註冊-設定監聽位置是否改變監聽器:
                    LocationServices.FusedLocationApi.requestLocationUpdates(PartnerMapFragment.this.googleApiClient,
                            locationRequest,
                            locationListener);
                }// end of onConnected()

                @Override
                public void onConnectionSuspended(int i) {
                    Util.showToast(getContext(), "GoogleApiClient connection suspended");
                }
            };

    private void initMap() {
        PartnerMapFragment.this.googleMap.getUiSettings().setZoomControlsEnabled(true);
        // public final void setPadding (int left, int top, int right, int bottom)
        PartnerMapFragment.this.googleMap.setPadding(0, 0, PartnerMapFragment.map_right_padding, PartnerMapFragment.map_bottom_padding);
        PartnerMapFragment.this.googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                //PartnerMapFragment.this.currClickedMarkerHotelId = null;
                //PartnerMapFragment.this.hotelBlock.setVisibility(View.INVISIBLE);
                PartnerMapFragment.this.googleMap.setPadding(0, 0, PartnerMapFragment.map_right_padding, PartnerMapFragment.this.map_bottom_padding);
                MainActivity.floatingBtn.setY(PartnerMapFragment.floatingBtnY);
            }
        });
    }

    private void goToCurrPosition() {
        LatLng latLng = new LatLng(PartnerMapFragment.this.lastLocation.getLatitude(), PartnerMapFragment.this.lastLocation.getLongitude());
        //Place current location marker
        if (PartnerMapFragment.this.CurrLocationMarker != null) {
            PartnerMapFragment.this.CurrLocationMarker.remove();
        }
        PartnerMapFragment.this.CurrLocationMarker = placeMemSelfMarkerAt(latLng);

        //Bitmap bitemap =  BitmapFactory.decodeStream(connection.getInputStream());
        //PartnerMapFragment.this.CurrLocationMarker.setIcon(BitmapDescriptorFactory.fromBitmap(bitemap));
        //PartnerMapFragment.this.CurrLocationMarker.setIcon(BitmapDescriptorFactory.HUE_MAGENTA);
        // For zooming automatically to the location of the marker
        moveToLocation(latLng, 16);
    }

    private Marker placeMemMarkerAt(LatLng aLatLng, String aMemId) {
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(aLatLng);
        markerOptions.title("Current Position");
        Bitmap bitmap = getMemProfileBitmap(aMemId);
        bitmap = getCircleBitmap05(bitmap);
        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(bitmap));
        return PartnerMapFragment.this.googleMap.addMarker(markerOptions);
    }

    private Marker placeMemSelfMarkerAt(LatLng aLatLng) {
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(aLatLng);
        markerOptions.title("Your Position");
        Bitmap bitmap = getMemProfileBitmap(PartnerMapFragment.this.memId);
        bitmap = getCircleBitmap05(bitmap);

        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(bitmap));
        markerOptions.anchor(0.5f,0.5f);
        return PartnerMapFragment.this.googleMap.addMarker(markerOptions);
    }

    private Bitmap getCircleBitmap05(Bitmap bitmap) {
        // Calculate the circular bitmap width with border
        int squareBitmapWidth = Math.min(bitmap.getWidth(), bitmap.getHeight());

        // Initialize a new instance of Bitmap
        Bitmap dstBitmap = Bitmap.createBitmap(
                squareBitmapWidth, // Width
                squareBitmapWidth, // Height
                Bitmap.Config.ARGB_8888 // Config
        );

        /*
            Canvas
                The Canvas class holds the "draw" calls. To draw something, you need 4 basic
                components: A Bitmap to hold the pixels, a Canvas to host the draw calls (writing
                into the bitmap), a drawing primitive (e.g. Rect, Path, text, Bitmap), and a paint
                (to describe the colors and styles for the drawing).
        */
        // Initialize a new Canvas to draw circular bitmap
        Canvas canvas = new Canvas(dstBitmap);

        // Initialize a new Paint instance
        Paint paint = new Paint();
        // 设置是否使用抗锯齿功能，会消耗较大资源，绘制图形速度会变慢。
        paint.setAntiAlias(true);

        /*
            Rect
                Rect holds four integer coordinates for a rectangle. The rectangle is represented by
                the coordinates of its 4 edges (left, top, right bottom). These fields can be accessed
                directly. Use width() and height() to retrieve the rectangle's width and height.
                Note: most methods do not check to see that the coordinates are sorted correctly
                (i.e. left <= right and top <= bottom).
        */
        /*
            Rect(int left, int top, int right, int bottom)
                Create a new rectangle with the specified coordinates.
        */
        // Initialize a new Rect instance
        Rect rect = new Rect(0, 0, squareBitmapWidth, squareBitmapWidth);

        /*
            RectF
                RectF holds four float coordinates for a rectangle. The rectangle is represented by
                the coordinates of its 4 edges (left, top, right bottom). These fields can be
                accessed directly. Use width() and height() to retrieve the rectangle's width and
                height. Note: most methods do not check to see that the coordinates are sorted
                correctly (i.e. left <= right and top <= bottom).
        */
        // Initialize a new RectF instance
        RectF rectF = new RectF(rect);

        /*
            public void drawOval (RectF oval, Paint paint)
                Draw the specified oval using the specified paint. The oval will be filled or
                framed based on the Style in the paint.

            Parameters
                oval : The rectangle bounds of the oval to be drawn

        */
        // Draw an oval shape on Canvas
        canvas.drawOval(rectF, paint);

        /*
            public Xfermode setXfermode (Xfermode xfermode)
                Set or clear the xfermode object.
                Pass null to clear any previous xfermode. As a convenience, the parameter passed
                is also returned.

            Parameters
                xfermode : May be null. The xfermode to be installed in the paint
            Returns
                xfermode
        */
        /*
            public PorterDuffXfermode (PorterDuff.Mode mode)
                Create an xfermode that uses the specified porter-duff mode.

            Parameters
                mode : The porter-duff mode that is applied

        */
        // PorterDuff.Mode有16種設定
        // 請參照-http://www.jcodecraeer.com/a/anzhuokaifa/androidkaifa/2014/1105/1907.html
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));

        // Calculate the left and top of copied bitmap
        float left = (squareBitmapWidth - bitmap.getWidth()) / 2;
        float top = (squareBitmapWidth - bitmap.getHeight()) / 2;

        /*
            public void drawBitmap (Bitmap bitmap, float left, float top, Paint paint)
                Draw the specified bitmap, with its top/left corner at (x,y), using the specified
                paint, transformed by the current matrix.

                Note: if the paint contains a maskfilter that generates a mask which extends beyond
                the bitmap's original width/height (e.g. BlurMaskFilter), then the bitmap will be
                drawn as if it were in a Shader with CLAMP mode. Thus the color outside of the

                original width/height will be the edge color replicated.

                If the bitmap and canvas have different densities, this function will take care of
                automatically scaling the bitmap to draw at the same density as the canvas.

            Parameters
                bitmap : The bitmap to be drawn
                left : The position of the left side of the bitmap being drawn
                top : The position of the top side of the bitmap being drawn
                paint : The paint used to draw the bitmap (may be null)
        */
        // Make a rounded image by copying at the exact center position of source image
        canvas.drawBitmap(bitmap, left, top, paint);

        // Free the native object associated with this bitmap.
        bitmap.recycle();

        // Return the circular bitmap
        return dstBitmap;
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
                LatLng latLng = new LatLng(PartnerMapFragment.this.lastLocation.getLatitude(), PartnerMapFragment.this.lastLocation.getLongitude());
                //Place current location marker
                if (PartnerMapFragment.this.CurrLocationMarker != null) {
                    PartnerMapFragment.this.CurrLocationMarker.remove();
                }
                ;
                PartnerMapFragment.this.CurrLocationMarker = placeMemSelfMarkerAt(latLng);
                // For zooming automatically to the location of the marker
                moveToLocation(latLng, 15);
                return false;
            }
        });
    }

    private void showMemMarkers() {
        PartnerMapFragment.this.markerMap.clear();
        //放上hotel Marker 並且 取得第一次各旅館的最低房價:
        List<MemCoordVO> memCoordVOList = null;
        try {
            MemCoordVO myVO = new MemCoordVO();
            myVO.setMemId(PartnerMapFragment.this.memId);
            myVO.setMemLat(PartnerMapFragment.this.lastLocation.getLatitude());
            myVO.setMemLng(PartnerMapFragment.this.lastLocation.getLongitude());

            // server端已將自己排除在此list外:
            memCoordVOList = new MemCoordGetAllTextTask().execute(myVO).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
//            Log.d("HotelMapFragment", hotelLowestPriceList.get(0).getHotelCheapestRoomPrice());

        for (MemCoordVO myVO : memCoordVOList) {
            LatLng latlng = new LatLng(myVO.getMemLat(), myVO.getMemLng());
            Marker tmpMarker = placeMemMarkerAt(latlng,myVO.getMemId());
            PartnerMapFragment.this.markerMap.put(tmpMarker, myVO);
        }

        // 設定Marker點擊事件
//        PartnerMapFragment.MyMarkerListener myMarkerListener = new PartnerMapFragment.MyMarkerListener();
//        PartnerMapFragment.this.googleMap.setOnMarkerClickListener(myMarkerListener);
    }


    private Bitmap getMemProfileBitmap(String aMemId) {
        String url = Common.URL_Partner;
        int imageSize = 200;
        Bitmap bitmap = null;
        try {
            bitmap = new PartnerGetOneImageTask().execute(url, aMemId, imageSize).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return bitmap;
    }





    // onMapReady是在myConnectionCallBacks呼叫的，成功連線後才註冊此物件
    // mMapView.getMapAsync(HotelMapFragment.this.myOnMapReadyCallback);
    private OnMapReadyCallback myOnMapReadyCallback = new OnMapReadyCallback() {

        @Override
        public void onMapReady(GoogleMap mMap) {
            PartnerMapFragment.this.googleMap = mMap;
            // init Map
            initMap();
            // 回到自己現在位置
            goToCurrPosition();
            // 顯示馬上到現在位置的按鈕
            showGoToCurrPositionBtn();
            // 顯示所有mem的markers
            showMemMarkers();
        }// end of onMapReady

    };// end of myOnMapReadyCallback
}