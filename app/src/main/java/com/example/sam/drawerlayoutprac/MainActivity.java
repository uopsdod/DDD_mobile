package com.example.sam.drawerlayoutprac;

import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.icu.text.MessagePattern;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
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
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.sam.drawerlayoutprac.Hotel.HotelFragment;
import com.example.sam.drawerlayoutprac.Partner.MyFirebaseInstanceIDService;
import com.example.sam.drawerlayoutprac.Partner.PartnerChatFragment;
import com.example.sam.drawerlayoutprac.Partner.PartnerFragment;
import com.example.sam.drawerlayoutprac.Partner.TestFragment;
import com.google.firebase.iid.FirebaseInstanceId;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_17;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONObject;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    DrawerLayout drawerLayout;
    ActionBarDrawerToggle actionBarDrawerToggle;
    WebSocketClient webSocketClientTmp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // toolbar   setup
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        myToolbar.setBackgroundColor( ContextCompat.getColor(this, R.color.sub1_color));
        myToolbar.setTitleTextColor(ContextCompat.getColor(this, R.color.white));
        setSupportActionBar(myToolbar);

        // button-step1
        setUpActionBar();

        initDrawer();
        inigDrawerBody();

        // fcm testing;
        SharedPreferences preferences_r = getSharedPreferences("preferences_yo",MODE_PRIVATE);
        String memid_yo = null;
        String tokenId = null;
        if (preferences_r != null){
            memid_yo = preferences_r.getString("memId_yo", null);
            tokenId = preferences_r.getString("tokenId", null);
        }
        // 狀況一(在這邊處理): 確定有登入 + 已有tokenId
        // 狀況二(在登入後處理)：尚未登入 + 已有tokenId
        // 狀況三(在FirebaseInstanceIdService::onTokenRefresed()方法中處理)：尚未登入 + tokenId還在更新中
        if (memid_yo != null && tokenId != null){
            URI uri = null;
            try {
                uri = new URI(PartnerChatFragment.URL_Chatroom);
            } catch (URISyntaxException e) {
                Log.e(PartnerChatFragment.TAG, e.toString());
            }
            this.webSocketClientTmp = new TmpWebSocketClient(uri);
            this.webSocketClientTmp.connect();
        }
    }

    @Override
    public void onPostCreate(Bundle savedInstanceState){
        super.onPostCreate(savedInstanceState);
        // button-step3
        actionBarDrawerToggle.syncState();
        if (webSocketClientTmp != null) {
            Map<String, String> map = new HashMap<>();
            SharedPreferences preferences_r = getSharedPreferences("preferences_yo",MODE_PRIVATE);
            String memid_yo = preferences_r.getString("memId_yo", null);
            // Get token
            String token = FirebaseInstanceId.getInstance().getToken();
            map.put("action", "uploadTokenId");
            map.put("tokenId", token);
            map.put("memId", memid_yo);
            webSocketClientTmp.send(new JSONObject(map).toString());
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        askPermissions();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.action_bar_menu, menu);
        return true;
    }


    private void setUpActionBar() {
        ActionBar actionbar = getSupportActionBar();
        if (actionbar != null){
            actionbar.setDisplayHomeAsUpEnabled(true);
        }
    }


    private void initDrawer(){
        drawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        NavigationView navigationView = (NavigationView)drawerLayout.findViewById(R.id.navigation_view);

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                item.setChecked(true);
                drawerLayout.closeDrawers(); // important step
                Fragment fragment = null;
                switch (item.getItemId()){
                    case R.id.lookfor_hotel:
                        //showToast("hotel clicked");
                        fragment = new HotelFragment();
                        Util.switchFragment(MainActivity.this,fragment);
                        break;
                    case R.id.lookfor_partner:
                        //showToast("partner clicked");
                        fragment = new PartnerFragment();
                        Util.switchFragment(MainActivity.this,fragment);
                        break;
                    case R.id.my_member:
                        fragment = new MemberFragment();
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
    public boolean onOptionsItemSelected(MenuItem item){
        NavigationView navigationView = (NavigationView)drawerLayout.findViewById(R.id.navigation_view);

        // button-step4
        switch(item.getItemId()){
            case android.R.id.home: // for home button at the top left corner
            if (drawerLayout.isDrawerOpen(navigationView)){
                drawerLayout.closeDrawer(navigationView);
            }else {
                drawerLayout.openDrawer(navigationView);
            }
//            if (drawerLayout.isDrawerOpen(GravityCompat.START)){
//                drawerLayout.closeDrawer(GravityCompat.START);
//            }else {
//                drawerLayout.openDrawer(GravityCompat.START);
//            }
            return true;
        }
        return onOptionsItemSelected(item);
    }

    private void inigDrawerBody() {
        Fragment fragment = new HotelFragment();
        Util.switchFragment(MainActivity.this,fragment);
    }

    private void showToast(String msg){
        Toast.makeText(getApplicationContext(),
                msg,
                Toast.LENGTH_SHORT)
                .show();
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
    public void testFragmentClicked(View view){
        LinearLayout linearLayout = (LinearLayout)view.getParent();
        EditText editText = (EditText)linearLayout.findViewById(R.id.memId_yo);
//        Util.showToast(getApplicationContext(),"testFragmentClicked:  " + editText.getText().toString());
        // 把假memId放入preferences_yo
        SharedPreferences preferences_w = getSharedPreferences("preferences_yo", MODE_PRIVATE);
        preferences_w.edit()
                   .putString("memId_yo",editText.getText().toString())
                   .apply();

        // 從preferences_yo讀取假memId
        SharedPreferences preferences_r = getSharedPreferences("preferences_yo", MODE_PRIVATE);
        String memid_yo = preferences_r.getString("memId_yo","no memId found");
        Util.showToast(getApplicationContext(),"memid_yo:  " + memid_yo);


    }

    public class TmpWebSocketClient extends WebSocketClient {
        public TmpWebSocketClient(URI serverURI) {
            super(serverURI,new Draft_17());
        }

        @Override
        public void onOpen(ServerHandshake handshakedata) {

        }

        @Override
        public void onMessage(String message) {
            Log.d("fcm - ", "onMessage: " + message);
            this.close();
        }

        @Override
        public void onClose(int code, String reason, boolean remote) {

        }

        @Override
        public void onError(Exception ex) {

        }
    }
}
