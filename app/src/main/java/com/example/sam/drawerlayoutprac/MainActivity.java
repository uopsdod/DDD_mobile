package com.example.sam.drawerlayoutprac;

import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    DrawerLayout drawerLayout;
    ActionBarDrawerToggle actionBarDrawerToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // toolbar  setup
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        myToolbar.setBackgroundColor(0xFF3c0c60);
        myToolbar.setTitleTextColor(0xFFFFFFFF);
        setSupportActionBar(myToolbar);


        // button-step1
        setUpActionBar();

        initDrawer();
        inigDrawerBody();
    }

    @Override
    public void onPostCreate(Bundle savedInstanceState){
        super.onPostCreate(savedInstanceState);
        // button-step3
        actionBarDrawerToggle.syncState();
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
                        showToast("hotel clicked");
                        fragment = new HotelFragment();
                        switchFragment(R.id.drawer_layout_body,fragment);
                        break;
                    case R.id.lookfor_partner:
                        showToast("partner clicked");
                        fragment = new PartnerFragment();
                        switchFragment(R.id.drawer_layout_body,fragment);
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
        switchFragment(R.id.drawer_layout_body,fragment);
    }
    private void switchFragment(int layoutId, Fragment fragment){
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(layoutId,fragment);
        fragmentTransaction.commit();
    }
    private void showToast(String msg){
        Toast.makeText(getApplicationContext(),
                msg,
                Toast.LENGTH_SHORT)
                .show();
    }

}
