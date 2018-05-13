package com.example.seanb.webwarden;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import org.apache.commons.codec.binary.android.Base64;

import java.io.IOException;
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

public class SignIn extends AppCompatActivity implements View.OnClickListener{
    private String username;
    private char[] userPass= new char[50];
    private String hashedInputPass;
    private String hashedUserPass;
    private EditText usernameET;
    private EditText passwordET;
    private Editable userPassword;
    private int pwLength;
    private String encodedSalt;
    private byte[] userSalt;
    private Connection connection;
    private ResultSet results;
    private String databasePW = "";
    private String databaseUN = "";
    private String PW = "";
    private final String DB_URL = "jdbc:mysql://webwarden.ck4ehi6goau1.eu-west-1.rds.amazonaws.com:3306/proxy";
    private String query1 = "Select user_password,user_salt from proxy.users where username = ?";




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        usernameET = findViewById(R.id.usernameET);
        passwordET = findViewById(R.id.passwordET);


        Button signIn = findViewById(R.id.signInBT);
        Button create = findViewById(R.id.createBT);
        signIn.setOnClickListener(this);
        create.setOnClickListener(this);

    }

    private void databasePassword(){
        try {

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

    private void showMessage1(){
        AlertDialog alertDialog = new AlertDialog.Builder(SignIn.this).create();
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.setTitle("Invalid User Password");
        alertDialog.setMessage("Password field is blank");
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }

    private void showMessage2(){
        AlertDialog alertDialog = new AlertDialog.Builder(SignIn.this).create();
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.setTitle("Invalid USer Credentials");
        alertDialog.setMessage("Password or username are incorrect");
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }

    private void showMessage3(){
        AlertDialog alertDialog = new AlertDialog.Builder(SignIn.this).create();
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.setTitle("Invalid Username");
        alertDialog.setMessage("Username field is blank");
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.signInBT:
                new Thread(){
                    public void run(){


                        username = usernameET.getText().toString();
                        if(username.equals("")){
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    showMessage3();
                                    usernameET.setText("");
                                }
                            });
                            return;
                        }

                        if(passwordET.length() == 0){
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    showMessage1();
                                    passwordET.setText("");
                                }
                            });

                            return;
                        }
                        userPassword = passwordET.getText();
                        pwLength = userPassword.length();
                        userPassword.getChars(0,pwLength-1,userPass,0);

                        try {
                            databaseUN = Util.getProperty("username",getApplicationContext());
                            databasePW = Util.getProperty("password",getApplicationContext());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        databasePassword();

                        try {
                            Class.forName("com.mysql.jdbc.Driver");
                            connection = DriverManager.getConnection(DB_URL, databaseUN, new String(PW));
                            PreparedStatement pstmt = connection.prepareStatement(query1);
                            pstmt.setString(1, username);

                            connection.setAutoCommit(false);

                            results = pstmt.executeQuery();
                            while (results.next()) {
                                hashedUserPass = results.getString("user_password");
                                encodedSalt = results.getString("user_salt");
                            }

                            connection.commit();
                            connection.close();
                        } catch (ClassNotFoundException ex) {
                            System.out.println(ex);
                            //Logger.getLogger(ProxyMain.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (SQLException ex) {
                            System.out.println(ex);
                            //Logger.getLogger(ProxyMain.class.getName()).log(Level.SEVERE, null, ex);
                        }

                        if(hashedUserPass == null || encodedSalt == null){
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    showMessage2();
                                }
                            });
                            return;
                        }


                        userSalt = decodeSalt(encodedSalt);
                        hashedInputPass = hashPassword(userPass,userSalt);
                        System.out.println(hashedInputPass);

                        if(!hashedInputPass.equals(hashedUserPass)){
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    showMessage2();
                                }
                            });
                            return;
                        }

                        SharedPreferences sharedPreferences = getSharedPreferences("user",MODE_PRIVATE);
                        SharedPreferences.Editor myEditor = sharedPreferences.edit();
                        myEditor.putString("username",username);
                        myEditor.apply();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Intent sign= new Intent(SignIn.this,MainMenu.class);
                                startActivity(sign);
                                finish();
                            }
                        });


                    }
                }.start();

                break;
            case R.id.createBT:
                Intent create= new Intent(this,CreateAccount.class);
                startActivity(create);
                break;
        }
    }
}
