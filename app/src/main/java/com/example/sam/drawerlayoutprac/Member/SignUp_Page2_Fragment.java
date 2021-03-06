package com.example.sam.drawerlayoutprac.Member;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
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
import android.widget.Toast;

import com.example.sam.drawerlayoutprac.Common;
import com.example.sam.drawerlayoutprac.CommonFragment;
import com.example.sam.drawerlayoutprac.Hotel.HotelFragment;
import com.example.sam.drawerlayoutprac.R;
import com.example.sam.drawerlayoutprac.Util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashSet;
import java.util.Set;

import static android.app.Activity.RESULT_OK;
import static java.lang.Integer.parseInt;


public class SignUp_Page2_Fragment extends CommonFragment {
    private MemVO memVO;
    EditText etIntro, etLiveBudget, etName;
    Button btSubmit, btChangePhoto;
    ImageView ivPhoto;
    RadioGroup rgGender;
    TextInputLayout tilName, tilLiveBudget, tilIntro;
    byte[] image;
    private Uri imageUri, cropImageUri;
    private static final int REQUEST_PICK_PICTURE = 2;
    private final static int REQ_PERMISSIONS = 0;
    private static final int REQUEST_CROP = 3;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        memVO = (MemVO) getArguments().getSerializable("memVO");
        View view = inflater.inflate(R.layout.fragment_signup_page2_, container, false);
        ivPhoto = (ImageView) view.findViewById(R.id.ivPhoto);
        etName = (EditText) view.findViewById(R.id.etName);
        etIntro = (EditText) view.findViewById(R.id.etIntro);
        etLiveBudget = (EditText) view.findViewById(R.id.etLiveBudget);
        rgGender = (RadioGroup) view.findViewById(R.id.rgGender);
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
//                    Integer.parseInt(String.valueOf(etLiveBudget.getText()));
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
        btSubmit.setOnClickListener(new InsertFinishClick());

        btChangePhoto.setOnClickListener(new changePhotoClick());
        return view;
    }

    private class changePhotoClick implements View.OnClickListener{

        @Override
        public void onClick(View view) {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, REQUEST_PICK_PICTURE);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        askPermissions();
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
                    imageUri = data.getData();
                    crop();
                    break;

                case REQUEST_CROP:
                    Log.d("tag", "REQUEST_CROP: " + cropImageUri.toString());
                    String[] columns = {MediaStore.Images.Media.DATA};
                    Cursor cursor = getContext().getContentResolver().query(imageUri, columns, null, null, null);
                    Log.d("12132","131231");

                    if(cursor.moveToFirst()){
                        cursor.close();
                        Bitmap bitmap = null;
                        try {
                            bitmap = BitmapFactory.decodeStream(getContext().getContentResolver().openInputStream(cropImageUri));
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
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
                        Boolean r = bitmap==null;
                        Log.d("12132",r.toString());
                        ivPhoto.setImageBitmap(bitmap);
                        ByteArrayOutputStream out = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 10, out);
                        image = out.toByteArray();
                    }
                    break;
            }
        }
    }

    private void crop() {
        cropImageUri = getImageUri("image_cropped.jpg");
        // take care of exceptions
        try {
            // call the standard crop action intent (the user device may not support it)
            Intent cropIntent = new Intent("com.android.camera.action.CROP");
            // set image source Uri and type
            cropIntent.setDataAndType(imageUri, "image/*");
            // send crop message
            cropIntent.putExtra("crop", "true");
            // aspect ratio of the cropped area
            cropIntent.putExtra("aspectX", 1);
            cropIntent.putExtra("aspectY", 1);
            // output with and height
            cropIntent.putExtra("outputX", 500);
            cropIntent.putExtra("outputY", 500);
            // whether keep original aspect ratio
            cropIntent.putExtra("scale", true);
            cropIntent.putExtra(MediaStore.EXTRA_OUTPUT, cropImageUri);
            // whether return data by the intent
            cropIntent.putExtra("return-data", false);
            // start the activity - we handle returning in onActivityResult
            startActivityForResult(cropIntent, REQUEST_CROP);
        }
        // respond to users whose devices do not support the crop action
        catch (ActivityNotFoundException anfe) {
            Util.showToast(getContext(),"NotFoundException");
        }
    }

    private Uri getImageUri(String fileName) {
        File dir = Environment
                .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File imageFile = new File(dir, fileName);
        return Uri.fromFile(imageFile);
    }

    private class InsertFinishClick implements View.OnClickListener{
        @Override
        public void onClick(View view) {
            String name = etName.getText().toString().trim();
            Integer LiveBudget = parseInt(String.valueOf(etLiveBudget.getText()));
            String Intro = etIntro.getText().toString().trim();
            if(image == null){
                memVO.setMemProfile(null);
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
                String action = "Insert";
//                new MemUpdateTask().execute(url, action, memVO, imageBase64);
                new MemUpdateTask().execute(url, action, memVO);
            }else{
                Util.showToast(getActivity(), "No network connection available");
            }
            Log.d("AAAAAA", "memVO" + memVO);
            Fragment fragment = new HotelFragment();
            Util.switchFragment(getActivity(),fragment); // 回到首頁，清除所有Fragment stack
        }
    }
}
