package com.example.seanb.webwarden;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.InputType;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import org.apache.commons.codec.binary.android.Base64;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;

public class MainMenu extends AppCompatActivity implements View.OnClickListener{
    private Button accessControl;
    private Button profile;
    private Button websiteAccess;
    private Button signOut;

    private String username;
    private String acPW;
    private Connection connection;
    private ResultSet resultSet1;
    private ResultSet resultSet2;
    private ResultSet resultSet3;
    private ResultSet resultSet4;
    private ResultSet resultSet5;
    private int pwLength;
    private Boolean featuresDisabled = false;
    private Button reSync;
    private Boolean runThread = true;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor myEditor;

    private String encodedProxyPW;
    private byte[] proxySalt;
    private char[] userProxyPw = new char[50];
    private String userEncodedProxyPW;
    private String encodedProxySalt;
    private String syncedNameResult;
    private Editable edtProxyPw;

    private String wardenValue;
    private Boolean userChoose = null;

    private String encodedAcPW;
    private String encodedSalt;



    private String databasePW = "";
    private String databaseUN = "";
    private String PW = "";
    private final String DB_URL = "jdbc:mysql://webwarden.ck4ehi6goau1.eu-west-1.rds.amazonaws.com:3306/proxy";
    private String query1 = "Select username from proxy.synced_users where username = ?";
    private String query2 = "Select ap_password from proxy.proxy ";
    private String query3 = "Select salt from proxy.proxy";
    private String query4 = "Insert into proxy.synced_users(username) values(?)";
    private String query6 = "Select warden from proxy.users where username = ?";
    private String query7 = "Update proxy.users Set warden = ? where username = ?";
    private String query9 = "Update proxy.users Set warden = ? where warden = ?";
    private String query8 = "Select ac_password,ac_salt from proxy.users where username = ?";





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //new Thread(new Notification()).start();
        super.onCreate(savedInstanceState);
        sharedPreferences = getSharedPreferences("user",MODE_PRIVATE);
        myEditor = sharedPreferences.edit();
        username = sharedPreferences.getString("username","");
        acPW = sharedPreferences.getString("acPW","");
        if(username.equals("")){
            finish();
            Intent website= new Intent(this,SignIn.class);
            startActivity(website);
        }else{
            setContentView(R.layout.activity_main_menu);
            deviceSync();
            checkWardenStatus();
            if(acPW.equals("")){
                getAccessControl();
            }else{
                runServices();
            }



            profile = findViewById(R.id.profileBT);
            accessControl = findViewById(R.id.accessControlBt);
            websiteAccess = findViewById(R.id.webSiteAccessBT);
            signOut = findViewById(R.id.signOutBT);
            reSync = findViewById(R.id.reSync);



            profile.setOnClickListener(this);
            accessControl.setOnClickListener(this);
            websiteAccess.setOnClickListener(this);
            signOut.setOnClickListener(this);
            reSync.setOnClickListener(this);
        }
    }


    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.profileBT:
                if(!featuresDisabled){
                    Intent mangage= new Intent(this,Profile.class);
                    startActivity(mangage);
                }
                break;
            case R.id.accessControlBt:
                if(!featuresDisabled){
                    Intent mangage= new Intent(this,AccessControl.class);
                    startActivity(mangage);
                }
                break;
            case R.id.webSiteAccessBT:
                if(!featuresDisabled){
                    Intent mangage= new Intent(this,WebsiteAccess.class);
                    startActivity(mangage);
                }
                break;
            case R.id.signOutBT:
                    myEditor.putString("acPW","");
                    myEditor.putString("acSalt","");
                    myEditor.putString("username","");
                    myEditor.apply();
                    stopService(new Intent(this, MyService.class));
                    Intent mangage= new Intent(this,SignIn.class);
                    startActivity(mangage);
                    finish();
                break;
            case R.id.reSync:
                reSync();
                break;

        }
    }



    private void deviceSync(){


        databasePassword();

        new Thread(){
            public void run(){
                synchronized (ThreadLock.lock2){
                    System.out.println("Thread1 started");
                    try {

                        Class.forName("com.mysql.jdbc.Driver");
                        connection = DriverManager.getConnection(DB_URL, databaseUN, new String(PW));
                        PreparedStatement pstmt = connection.prepareStatement(query1);
                        PreparedStatement pstmt2 = connection.prepareStatement(query2);
                        PreparedStatement pstmt3 = connection.prepareStatement(query3);
                        pstmt.setString(1, username);

                        connection.setAutoCommit(false);

                        resultSet1 = pstmt.executeQuery();
                        while (resultSet1.next()) {
                            syncedNameResult = resultSet1.getString("username");
                        }

                        resultSet2 = pstmt2.executeQuery();
                        while(resultSet2.next()){
                            encodedProxyPW = resultSet2.getString("ap_password");
                        }

                        resultSet3 = pstmt3.executeQuery();
                        while(resultSet3.next()){
                            encodedProxySalt = resultSet3.getString("salt");
                        }

                        connection.commit();
                        //connection.close();
                    } catch (ClassNotFoundException ex) {
                        System.out.println(ex);
                        //Logger.getLogger(ProxyMain.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (SQLException ex) {
                        System.out.println(ex);
                        //Logger.getLogger(ProxyMain.class.getName()).log(Level.SEVERE, null, ex);
                    }

                    if(syncedNameResult == null){
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                showMessage1();
                            }
                        });

                        while(edtProxyPw == null){

                        }
                        pwLength = edtProxyPw.length();
                        edtProxyPw.getChars(0,pwLength-1,userProxyPw,0);

                        proxySalt = decodeSalt(encodedProxySalt);
                        userEncodedProxyPW = hashPassword(userProxyPw,proxySalt);

                        if(userEncodedProxyPW.equals(encodedProxyPW)){
                            try {

                                Class.forName("com.mysql.jdbc.Driver");
                                PreparedStatement pstmt = connection.prepareStatement(query4);

                                pstmt.setString(1, username);

                                connection.setAutoCommit(false);

                                pstmt.executeUpdate();

                                connection.commit();
                                connection.close();
                            } catch (ClassNotFoundException ex) {
                                System.out.println(ex);
                                //Logger.getLogger(ProxyMain.class.getName()).log(Level.SEVERE, null, ex);
                            } catch (SQLException ex) {
                                System.out.println(ex);
                                //Logger.getLogger(ProxyMain.class.getName()).log(Level.SEVERE, null, ex);
                            }

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(MainMenu.this,"Device Synced", Toast.LENGTH_LONG).show();
                                }
                            });

                        }else {
                            edtProxyPw = null;
                            try {
                                connection.close();
                            } catch (SQLException e) {
                                e.printStackTrace();
                            }

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    showMessage3();
                                }
                            });
                            runThread = false;
                        }

                    }else{
                        try {
                            connection.close();
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    }

                    ThreadLock.lock2.notify();
                }


            }
        }.start();


    }

    private void checkWardenStatus(){
        if(PW.equals("")){
            databasePassword();
        }
        new Thread(){

            public void run(){
                synchronized (ThreadLock.lock2){
                    userChoose = null;
                    if(runThread){
                        try {

                            Class.forName("com.mysql.jdbc.Driver");
                            connection = DriverManager.getConnection(DB_URL, databaseUN, new String(PW));
                            PreparedStatement pstmt = connection.prepareStatement(query6);

                            pstmt.setString(1, username);

                            connection.setAutoCommit(false);

                            resultSet4 = pstmt.executeQuery();
                            while (resultSet4.next()) {
                                wardenValue = resultSet4.getString("warden");
                            }


                            connection.commit();
                        } catch (ClassNotFoundException ex) {
                            System.out.println(ex);
                            //Logger.getLogger(ProxyMain.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (SQLException ex) {
                            System.out.println(ex);
                            //Logger.getLogger(ProxyMain.class.getName()).log(Level.SEVERE, null, ex);
                        }

                        if(wardenValue.equals("false")){

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    showMessage2();
                                }
                            });
                            while(userChoose == null){

                            }

                            if(userChoose){

                                try {
                                    Class.forName("com.mysql.jdbc.Driver");
                                    PreparedStatement pstmt = connection.prepareStatement(query7);
                                    pstmt.setString(1, "true");
                                    pstmt.setString(2, username);


                                    connection.setAutoCommit(false);


                                    pstmt.executeUpdate();

                                    connection.commit();
                                    connection.close();
                                } catch (ClassNotFoundException e) {
                                    e.printStackTrace();
                                } catch (SQLException e) {
                                    e.printStackTrace();
                                }


                            }else{
                                try {
                                    connection.close();
                                } catch (SQLException e) {
                                    e.printStackTrace();
                                }
                            }

                        }
                    }else{
                        System.out.println("Thread didnt run!!!");
                    }
                    ThreadLock.lock2.notify();
                }
            }
        }.start();
    }

    private void getAccessControl(){
        if(PW.equals("")){
            databasePassword();
        }
        new Thread(){
            public void run(){
                synchronized (ThreadLock.lock2){
                    System.out.println("Thread3 started");
                    if(runThread){
                        try {

                            Class.forName("com.mysql.jdbc.Driver");
                            connection = DriverManager.getConnection(DB_URL, databaseUN, new String(PW));
                            PreparedStatement pstmt = connection.prepareStatement(query8);

                            pstmt.setString(1, username);

                            connection.setAutoCommit(false);

                            resultSet5 = pstmt.executeQuery();
                            while (resultSet5.next()) {
                                encodedAcPW = resultSet5.getString("ac_password");
                                encodedSalt = resultSet5.getString("ac_salt");
                            }


                            connection.commit();
                        } catch (ClassNotFoundException ex) {
                            System.out.println(ex);
                            //Logger.getLogger(ProxyMain.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (SQLException ex) {
                            System.out.println(ex);
                            //Logger.getLogger(ProxyMain.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        myEditor.putString("acPW",encodedAcPW);
                        myEditor.putString("acSalt",encodedSalt);
                        myEditor.apply();
                        if(isMyServiceRunning(MyService.class)){
                            stopService(new Intent(MainMenu.this, MyService.class));
                        }
                        startService(new Intent(MainMenu.this, MyService.class));


                    }else{
                        System.out.println("Thread didnt run!!!");
                    }
                    ThreadLock.lock2.notify();
                }
            }
        }.start();
    }

    private void runServices(){
        new Thread(){
            public void run(){
                synchronized (ThreadLock.lock2){
                    System.out.println("Thread3 started");
                    if(runThread){
                        if(isMyServiceRunning(MyService.class)){
                            stopService(new Intent(MainMenu.this, MyService.class));
                        }
                        startService(new Intent(MainMenu.this, MyService.class));
                    }

                }
            }
        }.start();
    }

    private void reSync(){
        new Thread(){
            public void run(){
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showMessage1();
                    }
                });

                while(edtProxyPw == null){

                }
                pwLength = edtProxyPw.length();
                edtProxyPw.getChars(0,pwLength-1,userProxyPw,0);

                proxySalt = decodeSalt(encodedProxySalt);
                userEncodedProxyPW = hashPassword(userProxyPw,proxySalt);

                if(userEncodedProxyPW.equals(encodedProxyPW)){
                    try {

                        Class.forName("com.mysql.jdbc.Driver");
                        connection = DriverManager.getConnection(DB_URL, databaseUN, new String(PW));
                        PreparedStatement pstmt = connection.prepareStatement(query4);

                        pstmt.setString(1, username);

                        connection.setAutoCommit(false);

                        pstmt.executeUpdate();

                        connection.commit();
                        connection.close();
                    } catch (ClassNotFoundException ex) {
                        System.out.println(ex);
                        //Logger.getLogger(ProxyMain.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (SQLException ex) {
                        System.out.println(ex);
                        //Logger.getLogger(ProxyMain.class.getName()).log(Level.SEVERE, null, ex);
                    }

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainMenu.this,"Device Synced", Toast.LENGTH_LONG).show();
                            reSync.setVisibility(View.INVISIBLE);
                        }
                    });
                    featuresDisabled = false;
                    runThread = true;
                    checkWardenStatus();
                    getAccessControl();


                }else {
                    edtProxyPw = null;
                    try {
                        connection.close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showMessage3();
                        }
                    });
                }
            }
        }.start();

    }

    private void databasePassword(){
        try {
            try {
                databaseUN = Util.getProperty("username",getApplicationContext());
                databasePW = Util.getProperty("password",getApplicationContext());
            } catch (IOException e) {
                e.printStackTrace();
            }

            MessageDigest sha = MessageDigest.getInstance("SHA-1");
            byte[] key = {'#', '9', 'F', '2', 'd', 'Y', 'V', 'H', '5', 'e', ']', '=', 'x', 't', '8', '(', '%', '8', 'w', 'J', '}', '#', '9', 'F', '2', 'd', 'Y', 'V', 'H', '5', 'e', ']', '=', 'x', 't', '8', '(', '%', '8', 'w', 'J'};
            key = sha.digest(key);
            key = Arrays.copyOf(key, 16);
            SecretKeySpec sKey = new SecretKeySpec(key, "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5PADDING");
            cipher.init(Cipher.DECRYPT_MODE, sKey);
            PW = new String(cipher.doFinal(Base64.decodeBase64(databasePW)));
            key = null;

        } catch (NoSuchAlgorithmException ex) {
            System.out.println(ex);
            //LoginLogger.error("NoSuchAlgorithmException: "+ex);
        } catch (NoSuchPaddingException ex) {
            System.out.println(ex);
            //LoginLogger.error("NoSuchPaddingException: "+ex);
        } catch (InvalidKeyException ex) {
            System.out.println(ex);
            //LoginLogger.error("InvalidKeyException: "+ex);
        } catch (IllegalBlockSizeException ex) {
            System.out.println(ex);
            //LoginLogger.error("IllegalBlockSizeException: "+ex);
        } catch (BadPaddingException ex) {
            System.out.println(ex);
            //LoginLogger.error("BadPaddingException: "+ex);
        }

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

    @Override
    public void onDestroy(){
        super.onDestroy();
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    private void showMessage1(){
        AlertDialog alertDialog = new AlertDialog.Builder(MainMenu.this).create();
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.setTitle("Please Enter Proxy Password");
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        alertDialog.setView(input);
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Sync",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        edtProxyPw = input.getText();
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }

    private void showMessage2(){
        AlertDialog alertDialog = new AlertDialog.Builder(MainMenu.this).create();
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.setTitle("WebWarden");
        alertDialog.setMessage("Do you want to become warden?");
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Decline",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        userChoose = false;
                        dialog.dismiss();
                    }
                });
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Accept",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        userChoose = true;
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }

    private void showMessage3(){
        AlertDialog alertDialog = new AlertDialog.Builder(MainMenu.this).create();
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.setTitle("Incorrect Sync Password");
        alertDialog.setMessage("Features Disabled");
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        featuresDisabled = true;
                        reSync.setVisibility(View.VISIBLE);
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }


}
