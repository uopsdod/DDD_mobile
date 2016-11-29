package com.example.sam.drawerlayoutprac;

import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.example.sam.drawerlayoutprac.Hotel.HotelFragment;
import com.example.sam.drawerlayoutprac.Member.MemberFragment;
import com.example.sam.drawerlayoutprac.Member.MemberInfoFragment;
import com.example.sam.drawerlayoutprac.Order.OrderLookUpFragment;
import com.example.sam.drawerlayoutprac.Order.OrderLookUpNowAdapter;
import com.example.sam.drawerlayoutprac.Partner.Chat.ChatFragment;
import com.example.sam.drawerlayoutprac.Partner.PartnerFragment;
import com.example.sam.drawerlayoutprac.Partner.TestFragment;
import com.example.sam.drawerlayoutprac.Partner.TokenIdWebSocket;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

import org.java_websocket.client.WebSocketClient;

import java.util.HashSet;
import java.util.Set;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    DrawerLayout drawerLayout;
    ActionBarDrawerToggle actionBarDrawerToggle;
    WebSocketClient webSocketClientTmp;
    public static FloatingActionButton floatingBtn;
    public static boolean floatingBtnPressed = false;
    public static Menu actionBarMenu;
    public static SharedPreferences pref;
    public static SharedPreferences pref_Hotel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 登入登出用:
        this.pref = this.getSharedPreferences(Common.PREF_FILE, MODE_PRIVATE);
        //廠商會員登入登出用
        this.pref_Hotel = this.getSharedPreferences(Common.PREF_FILE_Hotel, MODE_PRIVATE);
        //findViews
        this.floatingBtn = (FloatingActionButton) findViewById(R.id.floatingBtn);

        // toolbar  setup
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        myToolbar.setBackgroundColor(ContextCompat.getColor(this, R.color.sub1_color));
        myToolbar.setTitleTextColor(ContextCompat.getColor(this, R.color.white));
        setSupportActionBar(myToolbar);

        // 左上角button-step1
        setUpActionBar();
        // 左邊拉出視窗顯現
        initDrawer();

        // FCM
        // 狀況一(在這邊處理): 確定有登入 + 已有tokenId
        // 狀況二(在登入後處理)：尚未登入 + 已有tokenId
        // 狀況三(在FirebaseInstanceIdService::onTokenRefresed()方法中處理)：尚未登入 + tokenId還在更新中
        new TokenIdWebSocket(this).sendTokenIdToServer();
        // fcm - 當使用者點擊notification,直接跳轉到聊天訊息視窗
        Bundle fcmBundle = getIntent().getExtras();
        if (fcmBundle != null) {
            String fromMemId = (String) fcmBundle.get("fromMemId");
            if (fromMemId != null) {
                this.floatingBtn.setVisibility(View.INVISIBLE);
                fromMemId = fromMemId.trim();
                android.support.v4.app.Fragment fragment = new ChatFragment();
                Bundle bundle = new Bundle();
                bundle.putString("ToMemId", fromMemId);
                fragment.setArguments(bundle);
                Util.switchFragment(this, fragment);
                return;
            }
        }
        // end of FCM

        // 使用設定預設首頁 - HotelFragment.java
        inigDrawerBody();

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // button-step3
        actionBarDrawerToggle.syncState();
    }

    @Override
    protected void onStart() {
        super.onStart();
        askPermissions();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu aMenu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        this.actionBarMenu = aMenu;
        getMenuInflater().inflate(R.menu.action_bar_menu, this.actionBarMenu);
        return true;
    }

    private void setUpActionBar() {
        ActionBar actionbar = getSupportActionBar();
        if (actionbar != null) {
            actionbar.setDisplayHomeAsUpEnabled(true);
        }
    }

    private void initDrawer() {
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        final NavigationView navigationView = (NavigationView) drawerLayout.findViewById(R.id.navigation_view);

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            Menu menu = navigationView.getMenu();

            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                View popMenu = findViewById(R.id.my_member);
                item.setChecked(true);
                drawerLayout.closeDrawers(); // important step
                android.support.v4.app.Fragment fragment = null;
                switch (item.getItemId()) {
                    case R.id.lookfor_hotel:
                        //showToast("hotel clicked");
                        fragment = new HotelFragment();
                        Util.switchFragment(MainActivity.this, fragment);
                        break;

                    case R.id.lookfor_partner:
                        //showToast("partner clicked");
                        fragment = new PartnerFragment();
                        Util.switchFragment(MainActivity.this, fragment);
                        break;

                    case R.id.my_wishlist:
                        fragment = new MemberWishFragment();
                        Util.switchFragment(MainActivity.this, fragment);
                        break;

                    case R.id.my_member:

                        boolean login = pref.getBoolean("login",false);
                        if(login){
                            //如果已經登入，就轉到會員資料的頁面
                            fragment = new MemberInfoFragment();
                            Util.switchFragment(MainActivity.this, fragment);
                        }else{
                            //若還沒登入，就轉到會員登入、註冊頁面
                            fragment = new MemberFragment();
                            Util.switchFragment(MainActivity.this, fragment);
                        }
//                        PopupMenu popupMenu = new PopupMenu(navigationView.getContext(), popMenu);
//                        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
//                            @Override
//                            public boolean onMenuItemClick(MenuItem item) {
//                                return true;
//                            }
//                        });
//                        popupMenu.show();
                        break;

                    case R.id.my_member_hotel:
                        String hotelAccount = pref_Hotel.getString("userName", null);
                        if(hotelAccount == null){
                            fragment = new HotelMemberFragment();
                            Util.switchFragment(MainActivity.this, fragment);
                        }else{
                            fragment = new QRBarcodeScanFragment();
                            Util.switchFragment(MainActivity.this, fragment);
                        }

                        break;

                    case R.id.info:
                        fragment = new OrderInfoFragment();
                        Util.switchFragment(MainActivity.this, fragment);
                        break;

                    case R.id.my_order:
                        fragment = new OrderLookUpFragment();
                        Util.switchFragment(MainActivity.this, fragment);
                        break;

                    case R.id.test_yo:
//                        Intent intent = new Intent(getApplication(),EuclidTest.class);
//                        startActivity(intent);
                        fragment = new TestFragment();
                        Util.switchFragment(MainActivity.this, fragment);
                        break;

                }

                return false;
            }
        });

        // button-step2
        // 讓左上角的按鈕和DrawerLayout的動作同步，並同時製作轉動效果
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);

    }// end InitDrawer


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        NavigationView navigationView = (NavigationView) drawerLayout.findViewById(R.id.navigation_view);

        // button-step4
        switch (item.getItemId()) {
            case android.R.id.home: // for home button at the top left corner
                if (drawerLayout.isDrawerOpen(navigationView)) {
                    drawerLayout.closeDrawer(navigationView);
                } else {
                    drawerLayout.openDrawer(navigationView);
                }
                return true;
//            case R.id.action_bar_message:
//                Util.showToast(this,"hey menu item clicked");
//                return true;
        }
//        return onOptionsItemSelected(item);
        return false;
    }

    private void inigDrawerBody() {
        android.support.v4.app.Fragment fragment = new HotelFragment();
        Util.switchFragment(MainActivity.this, fragment);
    }

    private void askPermissions() {
        String[] permissions = {
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
        };

        Set<String> permissionsRequest = new HashSet<>();
        for (String permission : permissions) {
            int result = ContextCompat.checkSelfPermission(this, permission);
            if (result != PackageManager.PERMISSION_GRANTED) {
                permissionsRequest.add(permission);
            }
        }

        if (!permissionsRequest.isEmpty()) {
            ActivityCompat.requestPermissions(this,
                    permissionsRequest.toArray(new String[permissionsRequest.size()]),
                    REQ_PERMISSIONS);
        }
    }

    private static final int REQ_PERMISSIONS = 0;

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQ_PERMISSIONS:
                String text = "";
                for (int i = 0; i < grantResults.length; i++) {
                    if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                        text += permissions[i] + "\n";
                    }
                }
                if (!text.isEmpty()) {
                    text += "text_NotGranted";
                    Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }


}
