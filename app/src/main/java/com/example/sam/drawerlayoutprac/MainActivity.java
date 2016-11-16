package com.example.sam.drawerlayoutprac;

import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.sam.drawerlayoutprac.Hotel.HotelFragment;
import com.example.sam.drawerlayoutprac.Partner.Chat.PartnerChatFragment;
import com.example.sam.drawerlayoutprac.Partner.PartnerFragment;
import com.example.sam.drawerlayoutprac.Partner.HistoryMsg.PartnerHistoryMsgFragment;
import com.example.sam.drawerlayoutprac.Partner.TestFragment;
import com.example.sam.drawerlayoutprac.Partner.TokenIdWebSocket;

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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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

        // fcm testing;
        // 狀況一(在這邊處理): 確定有登入 + 已有tokenId
        // 狀況二(在登入後處理)：尚未登入 + 已有tokenId
        // 狀況三(在FirebaseInstanceIdService::onTokenRefresed()方法中處理)：尚未登入 + tokenId還在更新中


        //this.floatingBtn.setVisibility(View.VISIBLE);
        // 印出所有key-value pairs
//            for (String key : fcmBundle.keySet()) {
//                String value = fcmBundle.get(key).toString();
//                Log.d(TAG, "fcm - Key: " + key + " Value: " + value);
//            }
        new TokenIdWebSocket(this).sendTokenIdToServer();
        // fcm - 當使用者點擊notification
        Bundle fcmBundle = getIntent().getExtras();
        if (fcmBundle != null) {
            String fromMemId = (String) fcmBundle.get("fromMemId");
            if (fromMemId != null) {
                this.floatingBtn.setVisibility(View.INVISIBLE);
                fromMemId = fromMemId.trim();
                Fragment fragment = new PartnerChatFragment();
                Bundle bundle = new Bundle();
                bundle.putString("ToMemId", fromMemId);
                fragment.setArguments(bundle);
                Util.switchFragment(this, fragment);
                return;
            }
        }
        // 使用設定預設首頁 - HotelFragment.java
        inigDrawerBody();

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
//        this.actionBarMenu.findItem(R.id.action_bar_message).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
//            @Override
//            public boolean onMenuItemClick(MenuItem item) {
//                Util.showToast(MainActivity.this,"action_bar_message clicked");
//                return false;
//            }
//        });
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
                Fragment fragment = null;
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
                    case R.id.my_member:
                        SharedPreferences pref = getSharedPreferences(Common.PREF_FILE,
                                MODE_PRIVATE);
                        boolean login = pref.getBoolean("login",false);
                        if(login){
                            fragment = new MemberUpdateFragment();
                            Util.switchFragment(MainActivity.this, fragment);
                            Util.showToast(getApplicationContext(),"要修改資料喔! 屁孩");
                        }else{
                            fragment = new MemberFragment();
                            Util.switchFragment(MainActivity.this, fragment);
                            Util.showToast(getApplicationContext(),"還沒登入喔! 屁孩");
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
                    case R.id.my_member_update:
                        break;

                    case R.id.my_member_hotel:
                        fragment = new HotelMemberFragment();
                        Util.switchFragment(MainActivity.this, fragment);
                        break;

                    case R.id.info:
                        fragment = new OrdInfoFragment();
                        Util.switchFragment(MainActivity.this, fragment);
                        break;

                    case R.id.logOut:
                        SharedPreferences pref2 = getSharedPreferences(Common.PREF_FILE,
                                MODE_PRIVATE);
                        boolean login2 = pref2.getBoolean("login",false);
                        if(login2){
                            pref2.edit().clear().apply();
                            Util.showToast(getApplicationContext(),"登出了喔! 屁孩");
                        }else{
                            Util.showToast(getApplicationContext(),"還沒登入喔! 屁孩");
                        }
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
        Fragment fragment = new HotelFragment();
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

    // 以下為測試
    public void testFragmentClicked(View view) {
//        LinearLayout linearLayout = (LinearLayout) view.getParent();
//        EditText editText = (EditText) linearLayout.findViewById(R.id.memId_yo);
////        Util.showToast(getApplicationContext(),"testFragmentClicked:  " + editText.getText().toString());
//        // 把假memId放入preferences_yo
//        SharedPreferences preferences_w = getSharedPreferences("preferences_yo", MODE_PRIVATE);
//        preferences_w.edit()
//                .putString("memId_yo", editText.getText().toString())
//                .apply();

        // 從preferences_yo讀取假memId
        SharedPreferences preferences_r = getSharedPreferences(Common.PREF_FILE, MODE_PRIVATE);
        String memid_test = null;
        if (preferences_r != null){
            memid_test = preferences_r.getString("memId", null);
        }
        if (memid_test != null){
            Util.showToast(getApplicationContext(), "Who is logged:  " + memid_test);
            preferences_r.edit().remove("memId").apply();
        }
        if (preferences_r.getString("memId", null) == null){
            Util.showToast(getApplicationContext(), "log out:  " + memid_test);
        }
        // 之後繼續-如果登出，就要去跟MsgCenter講，把我的tokenId資訊拿掉


    }

}
