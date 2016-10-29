package com.example.sam.drawerlayoutprac;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by cuser on 2016/10/15.
 */
public class Util {

    // 當使用者是從Fragment到下一層Fragment的話，把上一個放入Fragment stack中
    public static void switchFragment(Fragment fragment_now, Fragment fragment) {
        FragmentManager fragmentManager = fragment_now.getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.drawer_layout_body, fragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }
    // 當使用者是從左邊drawerlayout進入到其中一個分區的話，清掉目前的所有Fragment stack
    // 程式上的意義：只要是從 Activity -> Fragment的話，清掉所有在stack中的Fragment物件
    public static void switchFragment(FragmentActivity activity, Fragment fragment) {
        FragmentManager fragmentManager = activity.getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        // clear all fragment stack
        int backStackCount = fragmentManager.getBackStackEntryCount();
        for (int i = 0; i < backStackCount; i++) {
            // Get the back stack fragment id.
            int backStackId = activity.getSupportFragmentManager().getBackStackEntryAt(i).getId();
            fragmentManager.popBackStack(backStackId, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }
        fragmentTransaction.replace(R.id.drawer_layout_body, fragment);
        fragmentTransaction.commit();
    }
}
