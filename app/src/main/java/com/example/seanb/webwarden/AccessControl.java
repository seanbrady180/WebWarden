package com.example.seanb.webwarden;

import android.app.AlertDialog;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import org.apache.commons.codec.binary.android.Base64;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
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

public class AccessControl extends AppCompatActivity implements View.OnClickListener {
    private String username;
    private Button proxyPwBT;
    private EditText proxyPwET;
    private EditText accessPwET;
    private Button accessPwBT;
    private Editable edtProxyPw;
    private Editable edtAccessPw;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor myEditor;



    private int pwLength;
    private char[] newProxyPw = new char[50];
    private byte[] proxyNewSalt;
    private String newEncocdedProxySalt;
    private String newEncodedProxyHash;

    private char[] newAccesPw = new char[50];
    private byte[] accessNewSalt;
    private String newEncocdedAccessSalt;
    private String newEncodedAccessHash;


    private String acPW;
    private String acSalt;
    private char[] userInputPW = new char[50];
    private Editable editable;
    private byte[] salt;
    private String userEncodedPw;

    private SecureRandom r;
    private Connection connection;
    private String databasePW = "";
    private String databaseUN = "";
    private String PW = "";
    private final String DB_URL = "jdbc:mysql://webwarden.ck4ehi6goau1.eu-west-1.rds.amazonaws.com:3306/proxy";
    private String query1 = "Update proxy.proxy Set ap_password = ?, salt = ?";
    private String query2 = "Update proxy.users Set ac_password = ?, ac_salt = ? where username = ?";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPreferences = getSharedPreferences("user",MODE_PRIVATE);
        myEditor = sharedPreferences.edit();
        acPW = sharedPreferences.getString("acPW","");
        acSalt = sharedPreferences.getString("acSalt","");
        username = sharedPreferences.getString("username","");
        setContentView(R.layout.activity_access_control);


        ImageButton profileBT = findViewById(R.id.imageButton4);
        ImageButton websiteAccess = findViewById(R.id.imageButton2);
        ImageButton mainMenu = findViewById(R.id.imageButton3);

        proxyPwBT = findViewById(R.id.editProxyBT);
        proxyPwET = findViewById(R.id.proxyPasswordET);
        accessPwBT = findViewById(R.id.accessControlBT);
        accessPwET = findViewById(R.id.accessControlET);




        profileBT.setOnClickListener(this);
        websiteAccess.setOnClickListener(this);
        mainMenu.setOnClickListener(this);
        proxyPwBT.setOnClickListener(this);
        accessPwBT.setOnClickListener(this);

    }

    private byte[] createSalt(){
        r = new SecureRandom();
        byte[] salty = new byte[32];
        r.nextBytes(salty);
        return salty;
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
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        }
        pw = null;
        salter = null;
        return hashString;
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

    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.imageButton3:
                Intent main= new Intent(this,MainMenu.class);
                startActivity(main);
                break;
            case R.id.imageButton4:
                Intent profile= new Intent(this,Profile.class);
                startActivity(profile);
                break;
            case R.id.imageButton2:
                Intent website= new Intent(this,WebsiteAccess.class);
                startActivity(website);
                break;
            case R.id.editProxyBT:
                editProxyPw();
                break;
            case R.id.accessControlBT:
                editAccessPw();
                break;

        }
    }

    private void editProxyPw(){
        new Thread(){
            public void run(){
                editable = null;
                if(proxyPwET.getText().length() == 0){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showMessage3();
                        }
                    });
                    return;
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showMessage4();
                    }
                });

                while(editable == null){
                    //System.out.println("Editable:"+editable);
                }

                pwLength = editable.length();
                editable.getChars(0,pwLength-1,userInputPW,0);
                salt = decodeSalt(acSalt);

                userEncodedPw = hashPassword(userInputPW,salt);
                System.out.println(userEncodedPw);
                System.out.println(acPW);

                if(!userEncodedPw.equals(acPW)){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showMessage5();
                        }
                    });

                }else{
                    edtProxyPw = proxyPwET.getText();
                    pwLength = edtProxyPw.length();
                    edtProxyPw.getChars(0,pwLength-1,newProxyPw,0);


                    proxyNewSalt = createSalt();
                    newEncocdedProxySalt = Base64.encodeBase64String(proxyNewSalt);


                    newEncodedProxyHash = hashPassword(newProxyPw,proxyNewSalt);

                    boolean updateFail = false;
                    if(PW.equals("")){
                        databasePassword();
                    }
                    try {
                        Class.forName("com.mysql.jdbc.Driver");
                        connection = DriverManager.getConnection(DB_URL, databaseUN, new String(PW));
                        PreparedStatement pstmt = connection.prepareStatement(query1);
                        pstmt.setString(1, newEncodedProxyHash);
                        pstmt.setString(2, newEncocdedProxySalt);


                        connection.setAutoCommit(false);
                        pstmt.executeUpdate();
                        System.out.println();
                        connection.commit();
                        connection.close();
                    } catch (ClassNotFoundException ex) {
                        System.out.println(ex);
                        updateFail = true;
                        //Logger.getLogger(ProxyMain.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (SQLException ex) {
                        System.out.println(ex);
                        updateFail = true;
                        //Logger.getLogger(ProxyMain.class.getName()).log(Level.SEVERE, null, ex);
                    }

                    if(updateFail){
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(AccessControl.this,"Error occurred", Toast.LENGTH_LONG).show();
                            }
                        });
                    }else{
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(AccessControl.this,"Password Changed", Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                }
            }


        }.start();
    }

    private void editAccessPw(){
        new Thread(){
            public void run(){
                editable = null;
                if(accessPwET.getText().length() == 0){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showMessage3();
                        }
                    });
                    return;
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showMessage4();
                    }
                });

                while(editable == null){
                    //System.out.println("Editable:"+editable);
                }

                pwLength = editable.length();
                editable.getChars(0,pwLength-1,userInputPW,0);
                salt = decodeSalt(acSalt);

                userEncodedPw = hashPassword(userInputPW,salt);
                System.out.println(userEncodedPw);
                System.out.println(acPW);

                if(!userEncodedPw.equals(acPW)){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showMessage5();
                        }
                    });

                }else{
                    edtAccessPw = accessPwET.getText();
                    pwLength = edtAccessPw.length();
                    edtAccessPw.getChars(0,pwLength-1,newAccesPw,0);


                    accessNewSalt = createSalt();
                    newEncocdedAccessSalt = Base64.encodeBase64String(accessNewSalt);


                    newEncodedAccessHash = hashPassword(newAccesPw,accessNewSalt);

                    boolean updateFail = false;
                    if(PW.equals("")){
                        databasePassword();
                    }
                    try {
                        Class.forName("com.mysql.jdbc.Driver");
                        connection = DriverManager.getConnection(DB_URL, databaseUN, new String(PW));
                        PreparedStatement pstmt = connection.prepareStatement(query2);
                        pstmt.setString(1, newEncodedAccessHash);
                        pstmt.setString(2, newEncocdedAccessSalt);
                        pstmt.setString(3, username);


                        connection.setAutoCommit(false);
                        pstmt.executeUpdate();
                        connection.commit();
                        connection.close();
                    } catch (ClassNotFoundException ex) {
                        System.out.println(ex);
                        updateFail = true;
                        //Logger.getLogger(ProxyMain.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (SQLException ex) {
                        System.out.println(ex);
                        updateFail = true;
                        //Logger.getLogger(ProxyMain.class.getName()).log(Level.SEVERE, null, ex);
                    }

                    if(updateFail){
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(AccessControl.this,"Error occurred", Toast.LENGTH_LONG).show();
                            }
                        });
                    }else{
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(AccessControl.this,"Password Changed", Toast.LENGTH_LONG).show();
                            }
                        });

                        myEditor.putString("acPW",newEncodedAccessHash);
                        myEditor.putString("acSalt",newEncocdedAccessSalt);
                        acPW = sharedPreferences.getString("acPW","");
                        acSalt = sharedPreferences.getString("acSalt","");
                        myEditor.apply();
                    }
                }
            }


        }.start();
    }



    private byte[] decodeSalt(String encodedSalt){
        byte [] s;
        s = Base64.decodeBase64(encodedSalt.getBytes());
        return s;
    }

    private void showMessage3(){
        AlertDialog alertDialog = new AlertDialog.Builder(AccessControl.this).create();
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.setTitle("Password Field Empty");
        alertDialog.setMessage("The password field cannot be blank");
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }

    private void showMessage4(){
        AlertDialog alertDialog = new AlertDialog.Builder(AccessControl.this).create();
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.setTitle("Access Control Password");
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

    private void showMessage5(){
        AlertDialog alertDialog = new AlertDialog.Builder(AccessControl.this).create();
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.setTitle("Incorrect Access Password");
        alertDialog.setMessage("Password incorrect Access Denied");
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }



}
