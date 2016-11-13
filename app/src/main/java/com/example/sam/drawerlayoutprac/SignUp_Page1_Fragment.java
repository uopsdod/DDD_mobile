package com.example.sam.drawerlayoutprac;


import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.util.Patterns;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;

import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by cuser on 2016/10/12.
 */
public class SignUp_Page1_Fragment extends Fragment {
    private String TAG = "SignUp_Page1_Fragment";
    Button btSubmit, btBirthDatePicker, btDueDatePicker;
    private static EditText editText, etBirthYear, etBirthMonth, etBirthDate, etCardYear, etCardMonth;
    EditText etUserName, etPassword, etChPassword, etPhoneNumber, etIdNumber, etCardNumber, etChCardNumber;
    Fragment fragment;
    private static int mYear, mMonth, mDay;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_signup_page1, container, false);

        etBirthYear = (EditText) view.findViewById(R.id.etBirthYear);
        etBirthMonth = (EditText) view.findViewById(R.id.etBirthMonth);
        etBirthDate = (EditText) view.findViewById(R.id.etBirthDate);
        etCardYear = (EditText) view.findViewById(R.id.etCardYear);
        etCardMonth = (EditText) view.findViewById(R.id.etCardMonth);
        editText = (EditText) view.findViewWithTag("edtext");
        etUserName = (EditText) view.findViewById(R.id.etUserName);
        etPassword = (EditText) view.findViewById(R.id.etPassword);
        etChPassword = (EditText) view.findViewById(R.id.etChPassword);
        etPhoneNumber = (EditText) view.findViewById(R.id.etPhoneNumber);
        etIdNumber = (EditText) view.findViewById(R.id.etIdNumber);
        etCardNumber = (EditText) view.findViewById(R.id.etCardNumber);
        etChCardNumber = (EditText) view.findViewById(R.id.etChCardNumber);

        btDueDatePicker = (Button) view.findViewById(R.id.btDueDatePicker);
        btSubmit = (Button) view.findViewById(R.id.btSubmit);
        btBirthDatePicker = (Button) view.findViewById(R.id.btBirthDatePicker);
        getActivity().findViewById(R.id.floatingBtn).setVisibility(View.INVISIBLE);
        editText.setOnKeyListener(new edFocus());
        btSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String account = etUserName.getText().toString().trim();
                Log.d("account:", account);
                if(isEmail(account) == false){
                    etUserName.setError("Email is not a valid ");
                    return;
                }

                String password = etPassword.getText().toString().trim();
                String chPassword = etChPassword.getText().toString().trim();
                if(!password.equals(chPassword)){
                    etChPassword.setError("Password must be the same");
                    return;
                }

                String phoneNumber = etPhoneNumber.getText().toString().trim();
                String TwIdNumber = etIdNumber.getText().toString().trim();
                String cardNumber = etCardNumber.getText().toString().trim();
                String checkCardNumber = etChCardNumber.getText().toString().trim();
                String birthYear = etBirthYear.getText().toString();
                String birthMonth = etBirthMonth.getText().toString();
                String birthDate = etBirthDate.getText().toString();
                String cardYear = etCardYear.getText().toString();
                String cardMonth = etCardMonth.getText().toString();
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");

                    MemVO memVO = new MemVO();
                    memVO.setMemAccount(account);
                    memVO.setMemPsw(password);
                    memVO.setMemPhone(phoneNumber);
                    memVO.setMemCreditCheckNo(TwIdNumber);
                    memVO.setMemCreditCardNo(cardNumber);
                    memVO.setMemCreditCheckNo(checkCardNumber);
                    memVO.setMemCreditDueDate(cardYear+ "-" +cardMonth);
                try {
                    memVO.setMemBirthDate(new Date(simpleDateFormat.parse(birthYear+"-"+birthMonth+"-"+birthDate).getTime()));
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                fragment = new SignUp_Page2_Fragment();
                Bundle bundle = new Bundle();
                bundle.putSerializable("memVO", memVO);
                Log.d(TAG, "memVO" + memVO);
                fragment.setArguments(bundle);
                Util.switchFragment(SignUp_Page1_Fragment.this, fragment);
            }

        });

        //生日日期選擇
        btBirthDatePicker.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                BirthDatePickerDialogFragment datePickerDialogFragment = new BirthDatePickerDialogFragment();
                FragmentManager fm  =getFragmentManager();
                datePickerDialogFragment.show(fm, "datePicker");
            }
        });
        //信用卡到期日選擇
        btDueDatePicker.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                DueDatePickerDialogFragment datePickerDialogFragment = new DueDatePickerDialogFragment();
                FragmentManager fm  =getFragmentManager();
                datePickerDialogFragment.show(fm, "datePicker");
            }
        });
        showNow();
        return view;
    }
    //日期部分開始
    private void showNow() {
        Calendar calendar = Calendar.getInstance();
        mYear = calendar.get(Calendar.YEAR);
        mMonth = calendar.get(Calendar.MONTH);
        mDay = calendar.get(Calendar.DATE);
    }

    private static void updateBirth() {
        etBirthYear.setText(new StringBuilder().append(mYear));
        etBirthMonth.setText(new StringBuilder().append(pad(mMonth + 1)));
        etBirthDate.setText(new StringBuilder().append(mDay));
    }

    private static void updateDueDate() {
        etCardYear.setText(new StringBuilder().append(mYear));
        etCardMonth.setText(new StringBuilder().append(pad(mMonth + 1)));
    }

    private static String pad(int number) {
        if (number >= 10) {
            return String.valueOf(number);
        }
        return "0" + String.valueOf(number);
    }
    //生日部分的月曆
    public static class BirthDatePickerDialogFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {
        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(), this, mYear, mMonth, mDay);
            return datePickerDialog;
        }

        @Override
        public void onDateSet(DatePicker datePicker, int year, int month, int day) {
            mYear = year;
            mMonth = month;
            mDay = day;
            updateBirth();
        }
    }
    //信用卡到期日部分的月曆
    public static class DueDatePickerDialogFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {
        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(), this, mYear, mMonth, mDay);
            return datePickerDialog;
        }

        @Override
        public void onDateSet(DatePicker datePicker, int year, int month, int day) {
            mYear = year;
            mMonth = month;
            mDay = day;
            updateDueDate();
        }
    }
    //日期部分結束

    public class edFocus implements View.OnKeyListener {

        @Override
        public boolean onKey(View view, int keyCode, KeyEvent event) {
            if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);

                getFocus();
            }
            return false;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getFocus();
    }

    //取得TextView 的 Focus
    private void getFocus() {
        getView().setFocusableInTouchMode(true);
        getView().requestFocus();
        getView().setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK) {
                    FragmentManager fm = getActivity().getSupportFragmentManager();
                    fm.popBackStack();
                    return true;
                }
                return false;
            }
        });
    }

    private boolean isEmail(String account){
        if(account == null){
            return false;
        }
        return Patterns.EMAIL_ADDRESS.matcher(account).matches();
    }

}
