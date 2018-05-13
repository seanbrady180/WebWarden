package com.example.seanb.webwarden;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.InputType;
import android.util.DisplayMetrics;
import android.view.View;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.codec.binary.android.Base64;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Date;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class SiteControl extends AppCompatActivity implements View.OnClickListener{

    private String device;
    private String wesbite;
    private ArrayList<String> foundWords = new ArrayList();
    private SharedPreferences sharedPreferences;
    private Boolean startMain;
    private long startTime;
    private long openTime;
    private long timePassed;
    private Date start;
    private Date open;
    private Editable editable;
    private int pwLength;
    private Boolean correctPassword = true;

    private String acPW;
    private String acSalt;
    private char[] userInputPW = new char[50];
    private byte[] salt;
    private String userEncodedPw;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        openTime = System.currentTimeMillis();
        setContentView(R.layout.activity_site_control);

        sharedPreferences = getSharedPreferences("user",MODE_PRIVATE);
        acPW = sharedPreferences.getString("acPW","");
        acSalt = sharedPreferences.getString("acSalt","");

        Button allowBt = findViewById(R.id.allowBT);
        Button blockBt = findViewById(R.id.blockBT);

        allowBt.setOnClickListener(this);
        blockBt.setOnClickListener(this);

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;
        int height = dm.heightPixels;


        wesbite = getIntent().getStringExtra("website");
        startTime = getIntent().getLongExtra("timeStart",0);
        System.out.println("Start time:"+startTime);
        System.out.println("Open time"+openTime);

        if(startTime != 0){
            start = new Date(startTime);
            open = new Date(openTime);

            timePassed = open.getTime() - start.getTime();

            if(timePassed > 30000){
                Toast.makeText(this, "Notification Expired", Toast.LENGTH_LONG).show();
                finish();
            }
        }

        TextView websitetv = (TextView) findViewById(R.id.websiteTV);
        WebView webview = (WebView) findViewById(R.id.webView);
        websitetv.setText(wesbite);
        webview.setWebViewClient(new WebViewClient());

        webview.getSettings().setLoadWithOverviewMode(true);
        webview.getSettings().setJavaScriptEnabled(true); // enable javascript
        webview.loadUrl(wesbite);

    }

    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.allowBT:
                getAccessPassword();
                break;
            case R.id.blockBT:
                Intent broadcast2 = new Intent("getting_data");
                broadcast2.putExtra("value", "block");
                sendBroadcast(broadcast2);
                finish();
                break;
        }
    }

    private class WebViewClient extends android.webkit.WebViewClient{
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }
    }

    private void getAccessPassword(){

                AlertDialog alertDialog = new AlertDialog.Builder(SiteControl.this).create();
                alertDialog.setCanceledOnTouchOutside(false);
                alertDialog.setTitle("SignIn Password");
                final EditText input = new EditText(SiteControl.this);
                input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                alertDialog.setView(input);
                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Enter",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                editable = input.getText();
                                pwLength = editable.length();
                                editable.getChars(0,pwLength-1,userInputPW,0);
                                salt = decodeSalt(acSalt);

                                userEncodedPw = hashPassword(userInputPW,salt);

                                if(!userEncodedPw.equals(acPW)){
                                    correctPassword = false;
                                }
                                if(correctPassword){
                                    Intent broadcast1 = new Intent("getting_data");
                                    broadcast1.putExtra("value", "allow");
                                    sendBroadcast(broadcast1);
                                    finish();
                                }else{
                                    Intent broadcast1 = new Intent("getting_data");
                                    broadcast1.putExtra("value", "block");
                                    sendBroadcast(broadcast1);
                                    finish();
                                }
                                dialog.dismiss();
                            }
                        });
                alertDialog.show();
    }

    private void showMessage1(){
        AlertDialog alertDialog = new AlertDialog.Builder(SiteControl.this).create();
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.setTitle("SignIn Password");
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        alertDialog.setView(input);
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Enter",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        editable = input.getText();
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }


    private byte[] decodeSalt(String encodedSalt){
        byte [] s;
        s = Base64.decodeBase64(encodedSalt.getBytes());
        return s;
    }

    private String hashPassword(char[] pw,byte[] salter){
        SecretKeyFactory skf = null;
        String hashString = "";

        try {
            skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            PBEKeySpec spec = new PBEKeySpec(pw, salter, 3, 256);
            SecretKey key = skf.generateSecret(spec);
            hashString= Base64.encodeBase64String(key.getEncoded());
        } catch (NoSuchAlgorithmException e) {
            System.out.println("NoSuchAlgorithmException");
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            System.out.println("InvalidKeySpecException");
            e.printStackTrace();
        }
        return hashString;
    }


}
