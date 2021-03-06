package com.sixgeese.bitcoininvestment;


import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import java.util.Locale;


/**
 * A simple {@link Fragment} subclass.
 */
public class InvestmentDialogFragment extends DialogFragment {



    /* The activity that creates an instance of this dialog fragment must
    * implement this interface in order to receive event callbacks.
    * Each method passes the DialogFragment in case the host needs to query it. */
    public interface NoticeInvestmentDialogListener {
        public void onInvestmentDialogPositiveClick(DialogFragment dialog);
        public void onInvestmentDialogNegativeClick(DialogFragment dialog);
    }

    // Use this instance of the interface to deliver action events
    NoticeInvestmentDialogListener listener;

    // Override the Fragment.onAttach() method to instantiate the NoticeDialogListener
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            listener = (NoticeInvestmentDialogListener) context;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(context.toString()
                    + " must implement NoticeInvestmentDialogListener");
        }
    }


    public InvestmentDialogFragment() {
        // Required empty public constructor
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View customView = inflater.inflate(R.layout.fragment_investment_dialog, null);
        final EditText editText = customView.findViewById(R.id.edit_init_investment_dialog);
        float passedValue = getArguments().getFloat("investment");
        editText.setHint(String.format(Locale.US, "%,.2f",passedValue));

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        Dialog dialog =
            builder.setView(customView)
                    // Add action buttons
                    .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            float newValue = Float.parseFloat(editText.getText().toString());
                            Bundle bundle = new Bundle();
                            bundle.putFloat("value", newValue);
                            InvestmentDialogFragment.this.setArguments(bundle);
                            listener.onInvestmentDialogPositiveClick(InvestmentDialogFragment.this);
                        }
                    })
                    .setNegativeButton("Cancel",null)
                    .setCancelable(true)
                    .create();
        return dialog;
    }

}
