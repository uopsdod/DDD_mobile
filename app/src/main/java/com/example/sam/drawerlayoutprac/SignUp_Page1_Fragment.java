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
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;

import java.util.Calendar;

/**
 * Created by cuser on 2016/10/12.
 */
public class SignUp_Page1_Fragment extends Fragment {
    Button btSubmit, btDatePicker;
    private static EditText editText, etBirthYear, etBirthMonth, etBirthDate;
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
        editText = (EditText) view.findViewWithTag("edtext");
        editText.setOnKeyListener(new edFocus());
        btSubmit = (Button) view.findViewById(R.id.btSubmit);
        btDatePicker = (Button) view.findViewById(R.id.btDatePicker);

        btSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fragment = new SignUp_Page2_Fragment();
                Util.switchFragment(SignUp_Page1_Fragment.this, fragment);
            }
        });

        btDatePicker.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                DatePickerDialogFragment datePickerDialogFragment = new DatePickerDialogFragment();
                FragmentManager fm  =getFragmentManager();
                datePickerDialogFragment.show(fm, "datePicker");
            }
        });
        showNow();
        return view;
    }

    private void showNow() {
        Calendar calendar = Calendar.getInstance();
        mYear = calendar.get(Calendar.YEAR);
        mMonth = calendar.get(Calendar.MONTH);
        mDay = calendar.get(Calendar.DATE);
    }

    private static void updateDisPlay() {
        etBirthYear.setText(new StringBuilder().append(mYear));
        etBirthMonth.setText(new StringBuilder().append(pad(mMonth + 1)));
        etBirthDate.setText(new StringBuilder().append(mDay));
    }

    private static String pad(int number) {
        if (number >= 10) {
            return String.valueOf(number);
        }
        return "0" + String.valueOf(number);
    }

    public static class DatePickerDialogFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {
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
            updateDisPlay();
        }
    }

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


}
