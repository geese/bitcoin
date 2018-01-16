package com.sixgeese.bitcoininvestment;

import android.app.Dialog;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.sixgeese.bitcoininvestment.utils.HttpJsonParser;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.prefs.Preferences;

public class MainActivity extends AppCompatActivity {
    private String url = "https://api.coinbase.com/v2/prices/spot";
    private Map<String, String> theParams;
    private static final String KEY_DATA = "data";
    private static final String KEY_AMOUNT = "amount";
    private static final String KEY_INITIAL_INVESTMENT = "init_invest";
    private static final String KEY_INITIAL_SPOT_PRICE = "init_spot";
    private String data;
    private double initialInvestmentValue;
    private double currentInvestmentValue;
    private double fraction;


    private TextView tvw_spotPrice;
    private TextView tvw_initialInvestmentValue;
    private TextView tvw_currentInvestmentValue;
    private ImageView refresh;
    private ImageView editInitInvestment;

    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        prefs = getSharedPreferences(null, MODE_PRIVATE);
        editor = prefs.edit();

        if (!prefs.contains(KEY_INITIAL_INVESTMENT))
            editor.putFloat(KEY_INITIAL_INVESTMENT, 200f).apply();

        if (!prefs.contains(KEY_INITIAL_SPOT_PRICE))
            editor.putFloat(KEY_INITIAL_SPOT_PRICE, 13900f).apply();

        initialInvestmentValue = (double)prefs.getFloat(KEY_INITIAL_INVESTMENT, 200f);
        fraction = initialInvestmentValue/(double)prefs.getFloat(KEY_INITIAL_SPOT_PRICE, 13900f);

        theParams = new HashMap<>();
        theParams.put("currency", "USD");

        refresh = findViewById(R.id.refresh);
        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new FetchSpotPrice().execute();
            }
        });

        editInitInvestment = findViewById(R.id.edit_init_investment);
        editInitInvestment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                View editView = getLayoutInflater().inflate(R.layout.dialog_initial_investment, null);

                builder.setView(editView);
                final AlertDialog dialog = builder.create();

                final EditText editInitInv = editView.findViewById(R.id.edit_init_investment_dialog);
                Button saveInitInv = editView.findViewById(R.id.save_init_investment_dialog_button);
                Button cancelSaveInitInv = editView.findViewById(R.id.cancel_save_init_investment_dialog_button);

                editInitInv.setHint(String.format(Locale.US, "%,.2f", prefs.getFloat(KEY_INITIAL_INVESTMENT, 200f)));

                saveInitInv.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        float newInitInvest = Float.parseFloat(editInitInv.getText().toString());
                        tvw_initialInvestmentValue.setText(String.format(Locale.US, "%,.2f", newInitInvest));
                        editor.putFloat(KEY_INITIAL_INVESTMENT, newInitInvest).apply();
                        initialInvestmentValue = (double)prefs.getFloat(KEY_INITIAL_INVESTMENT, 200f);
                        dialog.dismiss();
                        new FetchSpotPrice().execute();
                    }
                });


                dialog.show();
            }
        });



        Log.d("debug", "Fraction: " + fraction);

        tvw_spotPrice = findViewById(R.id.bitcoin_spot_price);
        tvw_spotPrice.setText("Spot Price");

        tvw_initialInvestmentValue = findViewById(R.id.initial_investment_amount);
        tvw_currentInvestmentValue = findViewById(R.id.investment_value);

        //Call the AsyncTask
        new FetchSpotPrice().execute();

    }

    private class FetchSpotPrice extends AsyncTask<String, String, String> {
        JSONObject response;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            /*//Display progress bar
            pDialog = new ProgressDialog(MainActivity.this);
            pDialog.setMessage("Loading Data.. Please wait...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();*/
        }

        @Override
        protected String doInBackground(String... params) {
            HttpJsonParser jsonParser = new HttpJsonParser();
            response = jsonParser.makeHttpRequest(url,"GET",theParams);
            try {
                data = response.getString(KEY_DATA);
                Log.d("debug", "Data: " + data);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute(String result) {
            //pDialog.dismiss();
            runOnUiThread(new Runnable() {
                public void run() {

                try {
                    JSONObject spotPriceObj =  response.getJSONObject(KEY_DATA);
                    Log.d("debug", "got here");
                    //fetch the currentInvestmentValue from response
                    double spotPrice = spotPriceObj.getDouble(KEY_AMOUNT);
                    Log.d("debug", "spotPrice: " + spotPrice);

                    tvw_spotPrice.setText(String.format(Locale.US, "%,.2f", spotPrice));
                    tvw_currentInvestmentValue.setText(String.format(Locale.US, "%,.2f", spotPrice *fraction));

                } catch (JSONException e) {
                    e.printStackTrace();
                }



                }
            });
        }
    }


}

