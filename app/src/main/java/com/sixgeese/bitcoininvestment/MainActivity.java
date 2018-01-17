package com.sixgeese.bitcoininvestment;

import android.app.Dialog;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sixgeese.bitcoininvestment.utils.HttpJsonParser;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.prefs.Preferences;

public class MainActivity extends AppCompatActivity
        implements InvestmentDialogFragment.NoticeInvestmentDialogListener,
                    BuyinDialogFragment.NoticeBuyinDialogListener {
    private String url = "https://api.coinbase.com/v2/prices/spot";
    private Map<String, String> theParams;
    private static final String KEY_DATA = "data";
    private static final String KEY_AMOUNT = "amount";
    private static final String KEY_INITIAL_INVESTMENT = "init_invest";
    private static final String KEY_INITIAL_SPOT_PRICE = "init_spot";
    private static final String KEY_BUYIN_PRICE = "buyin_price";
    private String data;
    private double initialInvestmentValue;
    private double currentInvestmentValue;
    private double buyInPrice;//price of 1 Bitcoin when we invested
    private double fraction;


    private TextView tvw_spotPrice;
    private TextView tvw_initialInvestmentValue;
    private TextView tvw_currentInvestmentValue;
    private TextView tvw_buyinPrice;

    private RelativeLayout img_refresh;
    private RelativeLayout img_editInitInvestment;
    private RelativeLayout img_editBuyinPrice;


    private ImageView refresh;
    private ImageView editInitInvestment;
    private ImageView editBuyinPrice;

    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;

    @Override
    public void onInvestmentDialogPositiveClick(DialogFragment dialog) {
        Float newValue = dialog.getArguments().getFloat("value");
        Log.d("value: ",  newValue+"");

        editor.putFloat(KEY_INITIAL_INVESTMENT, newValue).apply();

        initialInvestmentValue = (double)newValue;
        tvw_initialInvestmentValue.setText(String.format(Locale.US, "$%,.2f", initialInvestmentValue));
        fraction = initialInvestmentValue/(double)prefs.getFloat(KEY_BUYIN_PRICE, 13900f);

        new FetchSpotPrice().execute();
    }

    @Override
    public void onBuyinDialogPositiveClick(DialogFragment dialog) {
        Float newValue = dialog.getArguments().getFloat("value");
        Log.d("value: ",  newValue+"");

        editor.putFloat(KEY_BUYIN_PRICE, newValue).apply();

        buyInPrice = (double)newValue;
        tvw_buyinPrice.setText(String.format(Locale.US, "$%,.2f", buyInPrice));
        fraction = (double)prefs.getFloat(KEY_INITIAL_INVESTMENT, 200f)/buyInPrice;

        new FetchSpotPrice().execute();
    }

    @Override
    public void onInvestmentDialogNegativeClick(DialogFragment dialog) {}
    @Override
    public void onBuyinDialogNegativeClick(DialogFragment dialog) {}

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

        if (!prefs.contains(KEY_BUYIN_PRICE))
            editor.putFloat(KEY_BUYIN_PRICE, 13900f).apply();

        initialInvestmentValue = (double)prefs.getFloat(KEY_INITIAL_INVESTMENT, 200f);
        buyInPrice = (double)prefs.getFloat(KEY_BUYIN_PRICE, 13900f);
        fraction = initialInvestmentValue/buyInPrice;

        theParams = new HashMap<>();
        theParams.put("currency", "USD");

        img_refresh = findViewById(R.id.img_refresh);
        img_refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new FetchSpotPrice().execute();
            }
        });

        img_editInitInvestment = findViewById(R.id.img_edit_init_investment);
        img_editInitInvestment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle bundle = new Bundle();
                bundle.putFloat("investment", prefs.getFloat(KEY_INITIAL_INVESTMENT, 200f));
                DialogFragment dialog = new InvestmentDialogFragment();
                dialog.setArguments(bundle);
                dialog.show(getSupportFragmentManager(), "investment");
            }
        });

        img_editBuyinPrice = findViewById(R.id.img_edit_buy_in);
        img_editBuyinPrice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle bundle = new Bundle();
                bundle.putFloat("buyin", prefs.getFloat(KEY_BUYIN_PRICE, 13900f));
                DialogFragment dialog = new BuyinDialogFragment();
                dialog.setArguments(bundle);
                dialog.show(getSupportFragmentManager(), "buyin");
            }
        });


        Log.d("debug", "Fraction: " + fraction);

        tvw_spotPrice = findViewById(R.id.bitcoin_spot_price);
        tvw_spotPrice.setText("Spot Price");

        tvw_initialInvestmentValue = findViewById(R.id.initial_investment_amount);
        tvw_currentInvestmentValue = findViewById(R.id.investment_value);
        tvw_buyinPrice = findViewById(R.id.bitcoin_price_buy_in);

        tvw_initialInvestmentValue.setText(String.format(Locale.US, "$%,.2f", prefs.getFloat(KEY_INITIAL_INVESTMENT, 200f)));
        tvw_buyinPrice.setText(String.format(Locale.US, "$%,.2f", prefs.getFloat(KEY_BUYIN_PRICE, 13900f)));

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

                    tvw_spotPrice.setText(String.format(Locale.US, "$%,.2f", spotPrice));
                    tvw_currentInvestmentValue.setText(String.format(Locale.US, "$%,.2f", spotPrice *fraction));

                } catch (JSONException e) {
                    e.printStackTrace();
                }



                }
            });
        }
    }


}

