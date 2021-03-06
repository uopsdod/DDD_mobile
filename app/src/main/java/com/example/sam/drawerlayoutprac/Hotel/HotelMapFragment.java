package com.example.sam.drawerlayoutprac.Hotel;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.location.Location;
import android.os.Bundle;
import android.os.HandlerThread;
import android.os.Process;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.sam.drawerlayoutprac.Common;
import com.example.sam.drawerlayoutprac.CommonFragment;
import com.example.sam.drawerlayoutprac.MainActivity;
import com.example.sam.drawerlayoutprac.Partner.Chat.ChatFragment;
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

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_17;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.logging.Handler;

/**
 * Created by cuser on 2016/11/21.
 */

public class HotelMapFragment extends CommonFragment {

    // 版面配置
    private static int map_bottom_padding = 150;
    private static int map_bottom_padding_withWindow = map_bottom_padding + 440;
    private static int map_right_padding = 32;

    private static float floatingBtnY = MainActivity.floatingBtn.getY();
    private static float floatingBtnY_withWindow = floatingBtnY - 415;

    // googleMap:
    private GoogleMap googleMap;
    private GoogleApiClient googleApiClient;
    private Location lastLocation;

    // Markers:
    private HashMap<Marker, HotelGetLowestPriceVO> markerMap = new HashMap<>();
    private String currClickedMarkerHotelId;
    private Marker CurrLocationMarker;

    // 拿取動態價格:
    private HotelMapWebsocket hotelMapWebsocket;


    // Views:
    private MapView mMapView;
    private ImageView hotelImg;
    private TextView hotelName;
    private TextView hotelPrice;
    private RelativeLayout hotelBlock;

    // Thread:
    Thread myThread;
    HandlerThread myHandlerThread;
    public static volatile boolean isRunning;

    // onMapReady是在myConnectionCallBacks呼叫的，成功連線後才註冊此物件
    // mMapView.getMapAsync(HotelMapFragment.this.myOnMapReadyCallback);
    private OnMapReadyCallback myOnMapReadyCallback = new OnMapReadyCallback() {

        @Override
        public void onMapReady(GoogleMap mMap) {
            HotelMapFragment.this.googleMap = mMap;
            // init Map
            initMap();
            // 回到自己現在位置
            goToCurrPosition();
            // 顯示馬上到現在位置的按鈕
            showGoToCurrPositionBtn();
            // 顯示所有hotel的marker
            showHotelMarkers();
            // 取得動態價格
            getDynamicPrice();
        }// end of onMapReady

    };// end of myOnMapReadyCallback


    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
        this.googleApiClient.connect();
        MainActivity.floatingBtn.setVisibility(View.VISIBLE);
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
        myHandlerThread = new HandlerThread("longlifeThread", Process.THREAD_PRIORITY_BACKGROUND);
        myHandlerThread.start();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.hotel_map_fragment, container, false);
        HotelMapFragment.this.hotelImg = (ImageView) rootView.findViewById(R.id.ivImage);
        HotelMapFragment.this.hotelName = (TextView) rootView.findViewById(R.id.tvHotel);
        HotelMapFragment.this.hotelPrice = (TextView) rootView.findViewById(R.id.tvPrice);
        HotelMapFragment.this.hotelBlock = (RelativeLayout) rootView.findViewById(R.id.hotel_block);


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
        MainActivity.floatingBtn.setY(HotelMapFragment.floatingBtnY);
        if (hotelMapWebsocket != null){
            hotelMapWebsocket.close();
        }
        HotelMapFragment.isRunning = false;

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
        myHandlerThread.quit();
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

    private Marker placeMarkerAt(LatLng aLatLng, String aPrice) {

        Bitmap.Config conf = Bitmap.Config.ARGB_8888;
        Bitmap bmp = Bitmap.createBitmap(400, 100, conf);
        Canvas canvas1 = new Canvas(bmp);

// paint defines the text color, stroke width and size
        Paint color = new Paint();
        color.setTextSize(35);
        color.setColor(Color.BLACK);

// modify canvas
        canvas1.drawBitmap(BitmapFactory.decodeResource(getResources(),
                R.drawable.hotel_price05), 70, 0, color);
        canvas1.drawText("$" + aPrice, 115, 55, color);

// add marker to Map
        return HotelMapFragment.this.googleMap.addMarker(new MarkerOptions()
                .position(aLatLng)
                .icon(BitmapDescriptorFactory.fromBitmap(bmp))
                // Specifies the anchor to be at a particular point in the marker image.
                .anchor(0.5f, 1));
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

    private void getDynamicPrice() {
        //取得動態價格變動:
        HotelMapFragment.isRunning = true;
        URI uri = null;
        try {
            uri = new URI(Common.URL_DYNAMICPRICE);
        } catch (URISyntaxException e) {
            Log.e(ChatFragment.TAG, e.toString());
        }
        HotelMapFragment.this.hotelMapWebsocket = new HotelMapWebsocket(uri, getActivity());
        if (hotelMapWebsocket != null) {
            hotelMapWebsocket.connect();
        }
    }

    private void initMap() {
        HotelMapFragment.this.googleMap.getUiSettings().setZoomControlsEnabled(true);
        // public final void setPadding (int left, int top, int right, int bottom)
        HotelMapFragment.this.googleMap.setPadding(0, 0, HotelMapFragment.map_right_padding, HotelMapFragment.map_bottom_padding);

        HotelMapFragment.this.googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                HotelMapFragment.this.currClickedMarkerHotelId = null;
                HotelMapFragment.this.hotelBlock.setVisibility(View.INVISIBLE);
                HotelMapFragment.this.googleMap.setPadding(0, 0, HotelMapFragment.map_right_padding, HotelMapFragment.this.map_bottom_padding);
                MainActivity.floatingBtn.setY(HotelMapFragment.floatingBtnY);
            }
        });
    }

    private void showHotelMarkers() {
        HotelMapFragment.this.markerMap.clear();
        //放上hotel Marker 並且 取得第一次各旅館的最低房價:
        List<HotelGetLowestPriceVO> hotelLowestPriceList = null;
        try {
            HotelSearchVO myVO = new HotelSearchVO();
            myVO.setCity("桃園市");
            myVO.setZone("中壢區");
            myVO.setHotelRatingResult("0");
            myVO.setPrice("$1 - $10000");
            myVO.setRoomCapacity("2");

            hotelLowestPriceList = new HotelGetLowestPriceTask().execute(myVO).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
//            Log.d("HotelMapFragment", hotelLowestPriceList.get(0).getHotelCheapestRoomPrice());

        for (HotelGetLowestPriceVO myVO : hotelLowestPriceList) {
            LatLng latlng = new LatLng(Double.parseDouble(myVO.getHotelLat()), Double.parseDouble(myVO.getHotelLon()));
            Marker tmpMarker = placeMarkerAt(latlng, myVO.getHotelCheapestRoomPrice());
            HotelMapFragment.this.markerMap.put(tmpMarker, myVO);
        }

        // 設定Marker點擊事件
        MyMarkerListener myMarkerListener = new MyMarkerListener();
        HotelMapFragment.this.googleMap.setOnMarkerClickListener(myMarkerListener);
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
                moveToLocation(latLng, 15);
                return false;
            }
        });
    }

    private void moveToLocation(LatLng aLatLng, int aZoomSize) {
        CameraPosition cameraPosition = new CameraPosition.Builder().target(aLatLng).zoom(aZoomSize).build();
        googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        //googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

    }

    private void goToCurrPosition() {
        LatLng latLng = new LatLng(HotelMapFragment.this.lastLocation.getLatitude(), HotelMapFragment.this.lastLocation.getLongitude());
        //Place current location marker
        if (HotelMapFragment.this.CurrLocationMarker != null) {
            HotelMapFragment.this.CurrLocationMarker.remove();
        }
        HotelMapFragment.this.CurrLocationMarker = placeMemMarkerAt(latLng);
        // For zooming automatically to the location of the marker
        moveToLocation(latLng, 15);
    }

    public class HotelMapWebsocket extends WebSocketClient {
        URI uri;
        Activity activity;

        public HotelMapWebsocket(URI aUri, Activity aActivity) {
            super(aUri, new Draft_17());
            this.uri = aUri;
            this.activity = aActivity;
        }

        @Override
        public void onOpen(ServerHandshake handshakedata) {
            Log.d("hotelMapWebsocket - ", " open websocket successfully ");
            //this.send("text From mobile 02");
        }

        @Override
        public void onMessage(String message) {
            // 不知道為何，必須將websocket放在Fragment下面，才能夠抓到message
            android.os.Handler handler = new android.os.Handler(myHandlerThread.getLooper());
            handler.post(r1);

        }// end of onMessage

        @Override
        public void onClose(int code, String reason, boolean remote) {

        }

        @Override
        public void onError(Exception ex) {

        }

        Runnable r1 = new Runnable() {
            @Override
            public void run() {
                try {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (HotelMapFragment.isRunning) {
                                //Log.d("hotelMapWebsocket - ", "run on Ui Thread");
                                // 清除全部的marker
                                HotelMapFragment.this.googleMap.clear();
                                //HotelMapFragment.this.hotelMapView.getOverlay().clear();

                                // 放上新的markers:
                                showHotelMarkers();
                                // 把原本自己現在位置marker放回去
                                LatLng latLng = new LatLng(HotelMapFragment.this.lastLocation.getLatitude(), HotelMapFragment.this.lastLocation.getLongitude());
                                //Place current location marker
                                if (HotelMapFragment.this.CurrLocationMarker != null) {
                                    HotelMapFragment.this.CurrLocationMarker.remove();
                                }
                                HotelMapFragment.this.CurrLocationMarker = placeMemMarkerAt(latLng);


                                // change price on Marker window:
                                //Log.d("hotelMapWebsocket - ","hotelId: "+ HotelMapFragment.this.currClickedMarkerHotelId);
                                if (HotelMapFragment.this.currClickedMarkerHotelId != null) {
                                    Collection<HotelGetLowestPriceVO> collection = HotelMapFragment.this.markerMap.values();
                                    Iterator<HotelGetLowestPriceVO> itr = collection.iterator();
                                    while (itr.hasNext()) {
                                        HotelGetLowestPriceVO myVO = itr.next();
                                        //Log.d("hotelMapWebsocket - ","hotelId - to compare: "+ myVO.getHotelId());
                                        if (HotelMapFragment.this.currClickedMarkerHotelId.equals(myVO.getHotelId())) {
                                            HotelMapFragment.this.hotelPrice.setText(myVO.getHotelCheapestRoomPrice());
                                        }
                                    }
                                }


//                                MyMarkerListener myMarkerListener = new MyMarkerListener();
//                                HotelMapFragment.this.googleMap.setOnMarkerClickListener(myMarkerListener);
                            }
                        }// end of isRuuning while

                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };


    }// end of websocket

    private void uploadCurrentPosToServer() {
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
                            // 如果是第一次安裝app，且為第一次開啟googla map，防止沒有自動定位的情況
                            if (HotelMapFragment.this.lastLocation == null){
                                HotelMapFragment.this.lastLocation = aLocation;
                                mMapView.getMapAsync(HotelMapFragment.this.myOnMapReadyCallback);
                                Log.d("PartnerMapFragment", "init lastLocation onLocationChanged callback");
                                uploadCurrentPosToServer();
                                return;
                            }

                            HotelMapFragment.this.lastLocation = aLocation;
                            uploadCurrentPosToServer();
                            Log.d("PartnerMapFragment", "changed lastLocation: " + HotelMapFragment.this.lastLocation);

                        }
                    };
                    // end of 設定locationListener監聽器物件

                    // 總totla註冊-設定監聽位置是否改變監聽器:
                    LocationServices.FusedLocationApi.requestLocationUpdates(HotelMapFragment.this.googleApiClient,
                            locationRequest,
                            locationListener);
                }// end of onConnected()

                @Override
                public void onConnectionSuspended(int i) {
                    Util.showToast(getContext(), "GoogleApiClient connection suspended");
                }
            };

    public class MyMarkerListener implements GoogleMap.OnMarkerClickListener, GoogleMap.OnInfoWindowClickListener {
        @Override
        public boolean onMarkerClick(Marker aMarker) {
            // 設定資料上Layout
            final HotelGetLowestPriceVO myVO = HotelMapFragment.this.markerMap.get(aMarker);
            // 防止連點同一個marker時crash
            if (myVO == null) {
                Util.showToast(getContext(),"myVO is null");
                return true;
            }
            HotelMapFragment.this.currClickedMarkerHotelId = myVO.getHotelId(); // 紀錄下來，用來判斷是否window視窗已開啟，之後動態價格更新時拿用　- change price on Marker window:
            HotelMapFragment.this.hotelBlock.setVisibility(View.VISIBLE);
            HotelMapFragment.this.hotelName.setText(myVO.getHotelName());
            HotelMapFragment.this.hotelPrice.setText(myVO.getHotelCheapestRoomPrice());
            String url = Common.URL + "/android/hotel.do";
            int imageSize = 250;
            new HotelGetImageTask(HotelMapFragment.this.hotelImg).execute(url, myVO.getHotelId(), imageSize);

            // 調整原來版面位置:
            HotelMapFragment.this.googleMap.setPadding(0, 0, HotelMapFragment.map_right_padding, HotelMapFragment.map_bottom_padding_withWindow);
            MainActivity.floatingBtn.setY(HotelMapFragment.floatingBtnY_withWindow);

            // 設定點擊事件,進到旅館詳細頁面:
            String url_markerClicked = Common.URL + "/android/hotel.do";
            //final HotelVO hotelVO = new HotelGetOneTask().execute(url_markerClicked,myVO.getHotelId()).get();
            HotelMapFragment.this.hotelBlock.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Fragment fragment = new HotelInfoFragment();
                    Bundle bundle = new Bundle();
                    bundle.putString("hotelId", myVO.getHotelId());
                    //bundle.putSerializable("hotelVO", hotelVO);
                    fragment.setArguments(bundle);
                    Util.switchFragment(HotelMapFragment.this, fragment);
                }
            });


            return true; // return true 取消預設的事件
        }

        @Override
        public void onInfoWindowClick(Marker marker) {
//                showToast(marker.getTitle());
        }
    }


}
