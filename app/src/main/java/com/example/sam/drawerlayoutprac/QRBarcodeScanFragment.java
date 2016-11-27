package com.example.sam.drawerlayoutprac;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.HashMap;
import java.util.concurrent.ExecutionException;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;

/**
 * Created by cuser on 2016/11/21.
 */

public class QRBarcodeScanFragment extends CommonFragment {
    private static final String PACKAGE = "com.google.zxing.client.android";
    private static final int REQUEST_BARCODE_SCAN = 0;
    private TextView tvMessage, tvResult;
    private Button btScan;
    private String[] str;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_qrcode, container, false);
        tvMessage = (TextView) view.findViewById(R.id.tvMessage);
        tvResult = (TextView) view.findViewById(R.id.tvResult);
        btScan = (Button) view.findViewById(R.id.btScan);
        btScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("QRQRQRQR", "btScan clicked");
                Intent intent = new Intent("com.google.zxing.client.android.SCAN");
                try{
                    startActivityForResult(intent, REQUEST_BARCODE_SCAN);
                }catch(ActivityNotFoundException e){
                    showDownloadDialog();
                }
            }
        });
        return view;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent){
        Log.d("QRQRQRQR", "onActivityResult");
        if(requestCode == REQUEST_BARCODE_SCAN){
            String message = "";
            if(resultCode == RESULT_OK){
                String contents = intent.getStringExtra("SCAN_RESULT");
                String url = Common.URL + "/android/ord/ord.do";
                str = contents.split(",");
                String ordId = str[11];
                String key = str[15];
                HashMap<String, String> checkdStatus = new HashMap<>();
                boolean check;
                checkdStatus.put("0", "訂單確認失敗");
                checkdStatus.put("1", "訂單確認成功，未付款");
                Log.d("QRQRQRQR", str[0]);
                Log.d("QRQRQRQR", str[1]);
                Log.d("QRQRQRQR", str[2]);
                Log.d("QRQRQRQR", str[3]);
                Log.d("QRQRQRQR", str[4]);
                Log.d("QRQRQRQR", str[5]);
                Log.d("QRQRQRQR", str[6]);
                Log.d("QRQRQRQR", str[7]);
                Log.d("QRQRQRQR", str[8]);
                Log.d("QRQRQRQR", str[9]);
                Log.d("QRQRQRQR", str[10]);
                Log.d("QRQRQRQR", str[11]);
                Log.d("QRQRQRQR", str[12]);
                Log.d("QRQRQRQR", str[13]);
                Log.d("QRQRQRQR", str[14]);
                Log.d("QRQRQRQR", str[15]);
                if(Common.networkConnected(getActivity())){
                    try {
                        check = new OrderCheckedTask().execute(url, ordId, key).get();
                        Log.d("RRRRRR", "check" + check);
                        if(check == true){
                            tvResult.setText(checkdStatus.get("1"));
                        }else{
                            tvResult.setText(checkdStatus.get("0"));
                        }
                    } catch (Exception e) {
                        Util.showToast(getContext(), "Orderfragment" + e.toString());
                    }
                }
            }else if(resultCode == RESULT_CANCELED){
                tvMessage.setText("Scan was Cancelled");
            }

            tvMessage.setText(str[0] + "\n" + str[1] + "\n" + str[2] + str[3] + "\n" + str[4] + str[5] + "\n" + str[6] + str[7] + "\n" + str[8] + str[9]);


        }
    }

    private void showDownloadDialog() {
        AlertDialog.Builder downloadDialog = new AlertDialog.Builder(getContext());
        downloadDialog.setTitle("No Barcode Scanner Found");
        downloadDialog
                .setMessage("Please download and install Barcode Scanner!");
        downloadDialog.setPositiveButton("Yes",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Uri uri = Uri.parse("market://search?q=pname:"
                                + PACKAGE);
                        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                        try {
                            startActivity(intent);
                        } catch (ActivityNotFoundException ex) {
                            Log.e(ex.toString(),
                                    "Play Store is not installed; cannot install Barcode Scanner");
                        }
                    }
                });
        downloadDialog.setNegativeButton("No",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                });
        downloadDialog.show();
    }
}
