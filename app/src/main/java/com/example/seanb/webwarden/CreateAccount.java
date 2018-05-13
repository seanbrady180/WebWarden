package com.example.seanb.webwarden;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
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
import java.util.Enumeration;
import java.util.Properties;
import java.util.Random;
import java.security.SecureRandom;
import org.apache.commons.codec.binary.android.Base64;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

public class CreateAccount extends AppCompatActivity implements View.OnClickListener{
    private String username;
    private char[] userPass= new char[50];
    private char[] acPass= new char[50];
    private String hashedUserPass;
    private String hashedAcPass;
    private Editable userPassword;
    private Editable acPassword;
    private EditText usernameET;
    private EditText passwordET;
    private EditText acPassET;
    private int pwLength;
    private int acpLength;
    private String userIP = "";
    private Random r;
    private byte[] salt;
    private String encodedSalt;
    private byte[] ac_salt;
    private String encodedAcSalt;
    private SecretKeySpec sKey;
    private Cipher cipher;
    private MessageDigest sha = null;
    private Connection connection;
    private String databasePW = "";
    private String databaseUN = "";
    private String PW = "";
    private final String DB_URL = "jdbc:mysql://webwarden.ck4ehi6goau1.eu-west-1.rds.amazonaws.com:3306/proxy";
    private String query1 = "Insert into proxy.users(username, user_password, ac_password, user_ip, user_salt,ac_salt, warden) values (?,?,?,?,?,?,?);";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);

         usernameET = (EditText)findViewById(R.id.usernameET);
         passwordET = (EditText)findViewById(R.id.passwordET);
         acPassET = findViewById(R.id.accessControlET);
        Button createAccount = findViewById(R.id.createBT);

        createAccount.setOnClickListener(this);

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

    private String getIpAddress() {
        try {
            for (Enumeration en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = (NetworkInterface) en.nextElement();
                for (Enumeration enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = (InetAddress) enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()&&inetAddress instanceof Inet4Address) {
                        String ipAddress=inetAddress.getHostAddress().toString();

                        return ipAddress;
                    }
                }
            }
        } catch (SocketException ex) {
            System.out.println(ex);
        }
        return null;
    }

    private Boolean passwordCheck(char[] pw,boolean userPass) {
        Boolean test1 = false;
        Boolean test2 = false;
        Boolean test3 = false;
        Boolean test4 = false;
        Boolean finalResult = false;

        if(userPass){
            if (pw.length <10 ) {
                return finalResult;
            }

            for (char a : pw) {
                if (Character.isDigit(a)) {
                    test1 = true;

                }

                if (Character.isLowerCase(a)) {
                    test2 = true;

                }

                if (Character.isUpperCase(a)) {
                    test3 = true;

                }
            }
            if (test1 && test2 && test3) {
                finalResult = true;
            }
        }else{
            if (pw.length <6 ) {
                return finalResult;
            }

            for (char a : pw) {
                if (Character.isDigit(a)) {
                    test1 = true;

                }else{
                    test1 = false;
                    return finalResult;
                }

                if (test1) {
                    finalResult = true;
                }
            }

        }

        pw = null;
        return finalResult;
    }

    private void showMessage1(){
        AlertDialog alertDialog = new AlertDialog.Builder(CreateAccount.this).create();
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.setTitle("Invalid User Password");
        alertDialog.setMessage("Password must be longer than 9 characters,contain at least one digit, upper and lower case letter");
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }

    private void showMessage2(){
        AlertDialog alertDialog = new AlertDialog.Builder(CreateAccount.this).create();
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.setTitle("Invalid Access Password");
        alertDialog.setMessage("Password must be longer than 5 characters and contain only numbers");
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }

    private void showMessage3(){
        AlertDialog alertDialog = new AlertDialog.Builder(CreateAccount.this).create();
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.setTitle("Invalid Username");
        alertDialog.setMessage("Username is blank");
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }


    public void onClick(View view) {
        switch(view.getId()){
            case R.id.createBT:
                new Thread(){
                   public void run(){
                       username= usernameET.getText().toString();

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
                           if(acPassET.length() == 0){
                               runOnUiThread(new Runnable() {
                                   @Override
                                   public void run() {
                                       showMessage2();
                                       acPassET.setText("");
                                   }
                               });

                               return;
                           }


                       userIP = getIpAddress();

                       userPassword = passwordET.getText();
                       pwLength = userPassword.length();
                       userPassword.getChars(0,pwLength-1,userPass,0);

                       if(!passwordCheck(userPass,true)){
                           runOnUiThread(new Runnable() {
                               @Override
                               public void run() {
                                   showMessage1();
                                   passwordET.setText("");
                               }
                           });

                           return;
                       }

                       acPassword = acPassET.getText();
                       acpLength = acPassword.length();
                       acPassword.getChars(0,acpLength-1,acPass,0);

                       if(!passwordCheck(acPass,false)){
                           runOnUiThread(new Runnable() {
                               @Override
                               public void run() {
                                   showMessage2();
                                   acPassET.setText("");
                               }
                           });

                           return;
                       }

                       salt = createSalt();
                       ac_salt = createSalt();
                       encodedSalt = Base64.encodeBase64String(salt);
                       encodedAcSalt = Base64.encodeBase64String(ac_salt);


                       hashedUserPass = hashPassword(userPass,salt);
                       hashedAcPass = hashPassword(acPass,ac_salt);


                       try {
                           databaseUN = Util.getProperty("username",getApplicationContext());
                           databasePW = Util.getProperty("password",getApplicationContext());
                       } catch (IOException e) {
                           e.printStackTrace();
                       }


                       try {
                           byte[] key = {'#', '9', 'F', '2', 'd', 'Y', 'V', 'H', '5', 'e', ']', '=', 'x', 't', '8', '(', '%', '8', 'w', 'J', '}', '#', '9', 'F', '2', 'd', 'Y', 'V', 'H', '5', 'e', ']', '=', 'x', 't', '8', '(', '%', '8', 'w', 'J'};
                           sha = MessageDigest.getInstance("SHA-1");
                           key = sha.digest(key);
                           key = Arrays.copyOf(key, 16);
                           sKey = new SecretKeySpec(key, "AES");
                           cipher = Cipher.getInstance("AES/ECB/PKCS5PADDING");
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

                       try {
                           Class.forName("com.mysql.jdbc.Driver");
                           connection = DriverManager.getConnection(DB_URL, databaseUN, new String(PW));
                           PreparedStatement pstmt = connection.prepareStatement(query1);
                           pstmt.setString(1, username);
                           pstmt.setString(2, hashedUserPass);
                           pstmt.setString(3, hashedAcPass);
                           pstmt.setString(4, userIP);
                           pstmt.setString(5, encodedSalt);
                           pstmt.setString(6, encodedAcSalt);
                           pstmt.setString(7, "false");

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

                       userPassword = null;
                       userPass = new char[50];
                       acPassword = null;
                       acPass = new char[50];
                       salt = null;
                       ac_salt = null;


                       runOnUiThread(new Runnable() {
                           @Override
                           public void run() {
                               usernameET.setText("");
                               passwordET.setText("");
                               acPassET.setText("");
                               Toast.makeText(CreateAccount.this,"Account Created", Toast.LENGTH_LONG).show();

                           }
                       });

                   }
                }.start();


                break;
        }
    }

}
