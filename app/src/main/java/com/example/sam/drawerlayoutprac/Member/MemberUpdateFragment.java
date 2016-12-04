package com.example.sam.drawerlayoutprac.Member;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.sam.drawerlayoutprac.Common;
import com.example.sam.drawerlayoutprac.Hotel.HotelFragment;
import com.example.sam.drawerlayoutprac.MainActivity;
import com.example.sam.drawerlayoutprac.R;
import com.example.sam.drawerlayoutprac.Util;

import java.io.ByteArrayOutputStream;
import java.util.HashSet;
import java.util.Set;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.MODE_PRIVATE;
import static java.lang.Integer.parseInt;


public class MemberUpdateFragment extends Fragment {
    private MemVO memVO;
    EditText etIntro, etLiveBudget, etName;
    TextView tvMemName, tvMemAccount;
    Button btSubmit, btChangePhoto;
    ImageView ivPhoto, ivMemPhoto;
    RadioGroup rgGender;
    RadioButton rbMale, rbFemale;
    TextInputLayout tilName, tilLiveBudget, tilIntro;
    byte[] image, originalImage;
    Bitmap finalBitmap = null;
    private DrawerLayout drawerLayout;
    private static final int REQUEST_PICK_PICTURE = 2;
    private final static int REQ_PERMISSIONS = 0;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_signup_page2_, container, false);

        drawerLayout = (DrawerLayout) getActivity().findViewById(R.id.drawer_layout);
        NavigationView navigationView = (NavigationView) drawerLayout.findViewById(R.id.navigation_view);
        View v1 = navigationView.getHeaderView(0);
        ivMemPhoto = (ImageView) v1.findViewById(R.id.ivMemPhoto);
        tvMemName = (TextView) v1.findViewById(R.id.tvMemName);
        tvMemAccount = (TextView) v1.findViewById(R.id.tvMemAccount);

        ivPhoto = (ImageView) view.findViewById(R.id.ivPhoto);
        etName = (EditText) view.findViewById(R.id.etName);
        etIntro = (EditText) view.findViewById(R.id.etIntro);
        etLiveBudget = (EditText) view.findViewById(R.id.etLiveBudget);
        rgGender = (RadioGroup) view.findViewById(R.id.rgGender);
        rbMale = (RadioButton) view.findViewById(R.id.rbMale);
        rbFemale = (RadioButton) view.findViewById(R.id.rbFemale);
        btSubmit = (Button) view.findViewById(R.id.btSubmit);
        tilName = (TextInputLayout) view.findViewById(R.id.tilName);
        tilLiveBudget = (TextInputLayout) view.findViewById(R.id.tilLiveBudget);
        tilIntro = (TextInputLayout) view.findViewById(R.id.tilIntro);
        btChangePhoto = (Button) view.findViewById(R.id.btChangePhoto);
        getActivity().findViewById(R.id.floatingBtn).setVisibility(View.INVISIBLE);

        //Name 檢查
        etName.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                String name = etName.getText().toString().trim();
                if(name.length()<=0){
                    tilName.setError("TwId can not be empty");
                    return;
                }else{
                    tilName.setError(null);
                }
            }
        });
        //LiveBudget 檢查
        etLiveBudget.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                Integer liveBudget=null;
                if(String.valueOf(etLiveBudget.getText()).length()<=0){//先檢查有無打字，沒有就顯示提示訊息
                    tilLiveBudget.setError("TwId can not bdfsdfsdfe empty");
                    return;
                }else {
                    // 有打字之後，再把它轉型成數字 (不能打文字或沒打字，會造成轉型失敗)
//                    liveBudget = Integer.parseInt(String.valueOf(etLiveBudget.getText()));
                    tilLiveBudget.setError(null);
                }
            }
        });
        //Intro 檢查
        etIntro.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                String intro = etIntro.getText().toString().trim();
                if(intro.length()<=0){
                    tilIntro.setError("TwId can not be empty");
                    return;
                }else{
                    tilIntro.setError(null);
                }
            }
        });
        rgGender.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener(){

            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
                RadioButton radioButton = (RadioButton) radioGroup.findViewById(checkedId);
                String checked = radioButton.getHint().toString();
                memVO.setMemGender(checked);
            }
        });
        btSubmit.setOnClickListener(new UpdateFinishClick());

        btChangePhoto.setOnClickListener(new changePhotoClick());
        return view;
    }

    private class changePhotoClick implements View.OnClickListener{
        //選擇更換的照片
        @Override
        public void onClick(View view) {
            askPermissions();
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, REQUEST_PICK_PICTURE);

        }
    }

    @Override
    public void onStart() {
        super.onStart();
        showMemInfo();
        showMemProfile();
    }
    //問User同不同意這個APP使用他手機的相簿功能
    private void askPermissions() {
        String[] permissions = {
                Manifest.permission.READ_EXTERNAL_STORAGE
        };

        Set<String> permissionsRequest = new HashSet<>();
        for (String permission : permissions) {
            int result = ContextCompat.checkSelfPermission(getActivity(), permission);
            if (result != PackageManager.PERMISSION_GRANTED) {
                permissionsRequest.add(permission);
            }
        }

        if (!permissionsRequest.isEmpty()) {ActivityCompat.requestPermissions(getActivity(), permissionsRequest.toArray(new String[permissionsRequest.size()]),
                    REQ_PERMISSIONS);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQ_PERMISSIONS:
                for (int result : grantResults) {
                    if (result != PackageManager.PERMISSION_GRANTED) {
                        String text = getString(R.string.text_ShouldGrant);
                        Toast.makeText(getActivity(), text, Toast.LENGTH_LONG).show();
                        getActivity().finish();
                        return;
                    }
                }
                break;
        }
    }
    //取得手機相簿內的圖片
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("requestCode",""+requestCode);
        Log.d("resultCode",""+resultCode);
        if(resultCode == RESULT_OK){
            switch(requestCode){
                case REQUEST_PICK_PICTURE:
                    Uri uri = data.getData();
                    String[] columns = {MediaStore.Images.Media.DATA};
                    Cursor cursor = getContext().getContentResolver().query(uri, columns, null, null, null);
                    Log.d("uri",uri.toString());
                    Log.d("columns",columns.toString());
                    Log.d("cursor",""+cursor.getCount());

                    if(cursor.moveToFirst()){
                        String imagePath = cursor.getString(0);
                        cursor.close();
                        Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
                        Log.d("12132",bitmap.toString());

                        //設定圖片要顯示的大小(寬度、高度)
                        WindowManager windowManager = (WindowManager) getActivity().getSystemService(Context.WINDOW_SERVICE);
                        Display display = windowManager.getDefaultDisplay();
                        Point point = new Point();
                        display.getSize(point);
                        int width = point.x;
                        int height = point.y;
                        int imageSide = width < height ? width : height;

                        bitmap.setDensity(imageSide);//設定圖片的大小，與剛剛設定的  imageSide 大小一致
                        ivPhoto.setImageBitmap(bitmap);
                        ByteArrayOutputStream out = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 10, out);
                        image = out.toByteArray();
                    }
                    break;
            }
        }
    }

    private void showMemInfo(){
        SharedPreferences pref = getActivity().getSharedPreferences(Common.PREF_FILE,
                MODE_PRIVATE);
        String  id = pref.getString("memId", null);
        Log.d("-----****--", "id" + id);
        if(id != null){
            if(Common.networkConnected(getActivity())){
                String url = Common.URL + "/android/mem.do";
                try{
                    memVO = new MemGetOneTask().execute(url, id).get();
                }catch(Exception e){
                    Log.e("MemberInfo", e.toString());
                }

                etName.setText(memVO.getMemName());
                etLiveBudget.setText(memVO.getMemLiveBudget().toString());
                etIntro.setText(memVO.getMemIntro());
                if(memVO.getMemGender().equals("F") || memVO.getMemGender().equals("f")){
                    rbFemale.setChecked(true);
                }else{
                    rbMale.setChecked(true);
                }
            }
        }
    }

    private void showMemProfile(){
        SharedPreferences pref = getActivity().getSharedPreferences(Common.PREF_FILE,
                MODE_PRIVATE);
        String id =pref.getString("memId", null);
        if(id != null){
            if(Common.networkConnected(getActivity())){
                String url = Common.URL + "/android/mem.do";
                int imageSize = 250;
                Bitmap bitmap = null;
                try{
                    bitmap = new MemberGetImageTask(ivPhoto).execute(url, id, imageSize).get();
                }catch(Exception e){
                    Log.e("MemberInfo", e.toString());
                }
                // bitmap 轉形成 byte[]
                if(bitmap != null){
                    ivPhoto.setImageBitmap(bitmap);
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                    originalImage = out.toByteArray();
                }else{
                    ivPhoto.setImageResource(R.drawable.profile_default);
                }

            }
        }
    }

    private class UpdateFinishClick implements View.OnClickListener{
        @Override
        public void onClick(View view) {
            String name = etName.getText().toString().trim();
            String memId = MainActivity.pref.getString("memId", null);
            int imageSize = 250;
            Integer LiveBudget = parseInt(String.valueOf(etLiveBudget.getText()));
            String Intro = etIntro.getText().toString().trim();
            if(image == null){
                memVO.setMemProfile(originalImage);
            }else{
                memVO.setMemProfile(image);
            }

            if(Common.networkConnected(getActivity())){
                String url = Common.URL + "/android/mem.do";
                memVO.setMemName(name);
                memVO.setMemLiveBudget(LiveBudget);
                memVO.setMemIntro(Intro);
//                memVO.setMemProfile(image);
//                String imageBase64 = Base64.encodeToString(image, Base64.DEFAULT);
                String action = "Update";
//                new MemUpdateTask().execute(url, action, memVO, imageBase64);
                new MemUpdateTask().execute(url, action, memVO);
                if(memId != null){
                    new MemberGetImageTask(ivMemPhoto).execute(url, memId, imageSize);
                    tvMemName.setText(memVO.getMemName());
                    tvMemAccount.setText(memVO.getMemAccount());
                }
                Util.showToast(getActivity(), "Update success");
            }else{
                Util.showToast(getActivity(), "No network connection available");
            }
            Log.d("AAAAAA", "memVO" + memVO);
            Fragment fragment = new HotelFragment();
            Util.switchFragment(getActivity(),fragment); // 回到首頁，清除所有Fragment stack
        }
    }
}
