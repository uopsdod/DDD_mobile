package com.example.sam.drawerlayoutprac.Order;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.sam.drawerlayoutprac.Common;
import com.example.sam.drawerlayoutprac.CommonFragment;
import com.example.sam.drawerlayoutprac.MustLoginFragment;
import com.example.sam.drawerlayoutprac.R;

/**
 * Created by cuser on 2016/11/24.
 */

public class OrderLookUpRatingFragment extends DialogFragment
{

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.ord_lookup_rating_02, null);
        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(view)
                // Add action buttons
                .setPositiveButton("Sign in",
                        new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int id)
                            {
                            }
                        }).setNegativeButton("Cancel", null);
        return builder.create();
    }
}