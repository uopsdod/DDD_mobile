package com.example.sam.drawerlayoutprac.Order;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;

import com.example.sam.drawerlayoutprac.CommonFragment;
import com.example.sam.drawerlayoutprac.ExpandableListAdapter;
import com.example.sam.drawerlayoutprac.R;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by cuser on 2016/11/28.
 */

public class OrderInfoFragment extends CommonFragment {
    List<String> groupList;
    List<String> childList;
    Map<String, List<String>> laptopCollection;
    ExpandableListView expListView;

    @Override
    public void onResume() {
        super.onResume();
        MainActivity.actionbar.setTitle("訂房須知");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_ordinfo, container, false);
        createGroupList();
        createCollection();
        expListView = (ExpandableListView) view.findViewById(R.id.laptop_list);
        final ExpandableListAdapter expListAdapter = new ExpandableListAdapter(getActivity(), groupList, laptopCollection);
        expListView.setAdapter(expListAdapter);
        getActivity().findViewById(R.id.floatingBtn).setVisibility(View.INVISIBLE);

        //按下內容，會跳出含有內容的Toast
//        expListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
//            @Override
//            public boolean onChildClick(ExpandableListView expandableListView, View view, int groupPosition, int childPosition, long l) {
//                final String selected = (String) expListAdapter.getChild(groupPosition, childPosition);
//                Util.showToast(getActivity(), selected);
//                return true;
//            }
//        });
        return view;
    }

    private void createGroupList(){

        //標題文字
        groupList = new ArrayList<String>();
        groupList.add("房價包括什麼?");
        groupList.add("我可以使用信用卡預訂客房嗎？");
        groupList.add("我如何能知道我的預訂已經確定了？");
        groupList.add("哪裡可以找到住宿的聯絡資訊？");
        groupList.add("如何查詢住宿費用？");
        groupList.add("我要如何取消我的訂單?");
        groupList.add("若我無法在預定時間內抵達，飯店會預留房間或是懲罰嗎?");
        groupList.add("我該如何使用篩選條件？");
    }

    private void createCollection(){
        //裡面的內容
        String[] qaPrice = {"房價包含各房型下所列設施，點選客房名稱即可查看。" +
                "將會顯示房價是否包含早餐或稅費等其他費用。您也可以查看住宿確認函，或登入帳戶瞭解詳情。"};
        String[] qaCrideit = {"大部分的旅館，都有信用卡付費的機制。" +
                "但本網站為避免信用卡扣款糾紛等問題，" +
                "故付費方式以現場付費為主。" +
                "若未來使用信用卡的用戶居多，會在之後的版本增加此功能，造成不便請見諒!"};
        String[] qaOrderCheck = {"完成預訂後您會看到確認頁面，我們也將發送預訂確認函給您。" +
                "住宿確認函包含所有訂單資訊、訂單編號及驗證碼。如果您需要與客服團隊聯繫，請提供訂單編號及驗證碼。" +
                "您也可於線上查看確認函，登入帳號查看訂單詳情或修改預訂。"};
        String[] qaHotelInfo = {"預訂前，如果您對住宿方有疑問，請參見該住宿頁面。" +
                "如果這些信息還不能解答您的疑問，請通過電話或電子郵件聯系我們的客戶服務團隊，我們會向住宿方轉達您的問題。" +
                " 預訂完成後，住宿方的聯系方式會列在您的預訂確認頁面和預訂確認郵件中，您也可以在管理預訂欄目中查看。"};
        String[] qaRoomPrice = {"網站將列出可預訂的住宿，每筆搜尋結果旁均會標明住宿費用。" +
                "因是特價房間，同一間住宿的費用亦將有所差異。"};
        String[] qaCancelOrder = {"可以在\"我的訂單\"內的\"現有訂單\"，點選\"取消訂單\"。"};
        String[] qaDelate = {"倘若在三十分鐘內，未辦理入住程序，便將您的預訂取消。" +
                "因這是特價的房間，為了維護其他使用者與旅館業者的權益。" +
                "恕不會幫您預留房間，請見諒! "+
                "倘若多次訂房卻逾時不到，會將您設為黑名單處理。"};
        String[] qaSearch = {"在搜尋住宿時如果想要縮小選擇範圍，您可以使用搜尋篩選條件。" +
                "地區 ：選擇您想預訂的旅館所在地區 ，例如，台北市。" +
                "價格 ：選擇您想入住的房間價格區間。" +
                "人數  : 選擇您想入住的房間大小。" +
                "評分 :  選擇你想入住的旅館的評價分數。"};

        laptopCollection = new LinkedHashMap<String, List<String>>();

        for(String laptop : groupList){
            //判斷哪個標題被點擊，就顯示該標題內湖和的內容
            if(laptop.equals("房價包括什麼?")){
                loadChild(qaPrice);
            }else if(laptop.equals("我可以使用信用卡預訂客房嗎？")){
                loadChild(qaCrideit);
            }else if(laptop.equals("我如何能知道我的預訂已經確定了？")){
                loadChild(qaOrderCheck);
            }else if(laptop.equals("哪裡可以找到住宿的聯絡資訊？")){
                loadChild(qaHotelInfo);
            }else if(laptop.equals("如何查詢住宿費用？")){
                loadChild(qaRoomPrice);
            }else if(laptop.equals("我要如何取消我的訂單?")){
                loadChild(qaCancelOrder);
            }else if(laptop.equals("若我無法在預定時間內抵達，飯店會預留房間或是懲罰嗎?")){
                loadChild(qaDelate);
            }else if(laptop.equals("我該如何使用篩選條件？")){
                loadChild(qaSearch);
            }

            laptopCollection.put(laptop, childList);
        }
    }

    private void loadChild(String[] laptopModels){
        childList = new ArrayList<String>();
        for(String model : laptopModels){
            childList.add(model);
        }
    }

    private void setGroupIndicatorToRight() {
        /* Get the screen width */
        DisplayMetrics dm = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;

        expListView.setIndicatorBounds(width - getDipsFromPixel(35), width
                - getDipsFromPixel(5));
    }

    // Convert pixel to dip
    public int getDipsFromPixel(float pixels) {
        // Get the screen's density scale
        final float scale = getResources().getDisplayMetrics().density;
        // Convert the dps to pixels, based on density scale
        return (int) (pixels * scale + 0.5f);
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getActivity().getMenuInflater().inflate(R.menu.activity_main, menu);
//        return true;
//    }
}
