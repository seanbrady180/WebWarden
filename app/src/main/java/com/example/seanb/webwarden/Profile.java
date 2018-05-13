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

public class Profile extends AppCompatActivity implements View.OnClickListener{
    private String username;
    private SharedPreferences sharedPreferences;
    private ResultSet resultSet1;
    private ResultSet resultSet2;
    private SharedPreferences.Editor myEditor;
    private Button editUser;
    private EditText userText;
    private Editable userTxt;
    private Editable editable;
    private Button editPassword;
    private EditText passwordText;
    private Editable pwTxt;
    private Connection connection;
    private int pwLength;
    private SecureRandom r;
    private String newUsername;

    private char[] newUserPw = new char[50];
    private byte[] UserNewSalt;
    private String newEncocdedUSerSalt;
    private String newEncodedUserHash;


    private String encocdedUSerSalt;
    private String encodedUserHash;

    private char[] userInputPW = new char[50];
    private byte[] salt;
    private String userEncodedPw;


    private String databasePW = "";
    private String databaseUN = "";
    private String PW = "";
    private final String DB_URL = "jdbc:mysql://webwarden.ck4ehi6goau1.eu-west-1.rds.amazonaws.com:3306/proxy";
    private String query1 = "Select user_password,user_salt from proxy.users where username = ?";
    private String query2 = "Update proxy.users Set user_password = ?, user_salt = ? where username = ?";
    private String query3 = "Update proxy.users Set username = ? where username = ?";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        sharedPreferences = getSharedPreferences("user",MODE_PRIVATE);
        myEditor = sharedPreferences.edit();
        myEditor = sharedPreferences.edit();
        username = sharedPreferences.getString("username","");


        ImageButton websiteAccess = findViewById(R.id.imageButton);
        ImageButton accessControlBT = findViewById(R.id.imageButton3);
        ImageButton mainMenu = findViewById(R.id.imageButton4);

        editUser = findViewById(R.id.editUsernameBT);
        editPassword = findViewById(R.id.editPasswordBT);

        userText = findViewById(R.id.editUserET);
        passwordText = findViewById(R.id.editPwET);





        accessControlBT.setOnClickListener(this);
        mainMenu.setOnClickListener(this);
        websiteAccess.setOnClickListener(this);
        editUser.setOnClickListener(this);
        editPassword.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        switch(view.getId()){

            case R.id.imageButton4:
                Intent main= new Intent(this,MainMenu.class);
                startActivity(main);
                break;
            case R.id.imageButton3:
                Intent access= new Intent(this,AccessControl.class);
                startActivity(access);
                break;
            case R.id.imageButton:
                Intent website= new Intent(this,WebsiteAccess.class);
                startActivity(website);
                break;
            case R.id.editPasswordBT:
                editPassword();
                break;
            case R.id.editUsernameBT:
                editUsername();
                break;

        }

    }

    private void editUsername(){
        new Thread(){
            public void run(){
                if(userText.getText().length() == 0){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showMessage6();
                        }
                    });
                    return;
                }
                if(PW.equals("")){
                    databasePassword();
                }

                newUsername = userText.getText().toString();
                boolean queryFail = false;
                try {

                    Class.forName("com.mysql.jdbc.Driver");
                    connection = DriverManager.getConnection(DB_URL, databaseUN, new String(PW));
                    PreparedStatement pstmt = connection.prepareStatement(query3);

                    pstmt.setString(1, newUsername);
                    pstmt.setString(2, username);

                    connection.setAutoCommit(false);

                    pstmt.executeUpdate();

                    connection.commit();
                    connection.close();
                } catch (ClassNotFoundException ex) {
                    System.out.println(ex);
                    queryFail = true;
                    //Logger.getLogger(ProxyMain.class.getName()).log(Level.SEVERE, null, ex);
                } catch (SQLException ex) {
                    System.out.println(ex);
                    queryFail = true;
                    //Logger.getLogger(ProxyMain.class.getName()).log(Level.SEVERE, null, ex);
                }

                if(queryFail){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(Profile.this,"Error Occurred", Toast.LENGTH_LONG).show();
                        }
                    });
                    userText.setText("");

                }else{
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(Profile.this,"Username Updated", Toast.LENGTH_LONG).show();
                        }
                    });
                    userText.setText("");
                    myEditor.putString("username",newUsername);
                    myEditor.apply();
                }



            }
        }.start();
    }

    private void editPassword(){
        new Thread(){
            public void run(){
                if(PW.equals("")){
                    databasePassword();
                }

                if(passwordText.getText().length() == 0){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showMessage3();
                        }
                    });
                    return;
                }

                boolean queryFail = false;
                try {

                    Class.forName("com.mysql.jdbc.Driver");
                    connection = DriverManager.getConnection(DB_URL, databaseUN, new String(PW));
                    PreparedStatement pstmt = connection.prepareStatement(query1);

                    pstmt.setString(1, username);

                    connection.setAutoCommit(false);

                    resultSet1 = pstmt.executeQuery();
                    while(resultSet1.next()){
                        encodedUserHash = resultSet1.getString("user_password");
                        encocdedUSerSalt = resultSet1.getString("user_salt");
                    }

                    connection.commit();
                } catch (ClassNotFoundException ex) {
                    System.out.println(ex);
                    queryFail = true;
                    //Logger.getLogger(ProxyMain.class.getName()).log(Level.SEVERE, null, ex);
                } catch (SQLException ex) {
                    System.out.println(ex);
                    queryFail = true;
                    //Logger.getLogger(ProxyMain.class.getName()).log(Level.SEVERE, null, ex);
                }

                if(queryFail){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(Profile.this,"Error Occurred", Toast.LENGTH_LONG).show();
                        }
                    });
                    return;
                }

                editable = null;
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
                salt = decodeSalt(encocdedUSerSalt);

                userEncodedPw = hashPassword(userInputPW,salt);


                if(!userEncodedPw.equals(encodedUserHash)){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showMessage5();
                        }
                    });

                }else{

                    pwTxt = passwordText.getText();
                    pwLength = pwTxt.length();
                    pwTxt.getChars(0,pwLength-1,newUserPw,0);

                    if(!passwordCheck(newUserPw,true)){
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                showMessage2();
                                passwordText.setText("");
                            }
                        });

                        return;
                    }

                    UserNewSalt = createSalt();
                    newEncocdedUSerSalt = Base64.encodeBase64String(UserNewSalt);

                    newEncodedUserHash =hashPassword(newUserPw,UserNewSalt);

                    try {

                        Class.forName("com.mysql.jdbc.Driver");
                        connection = DriverManager.getConnection(DB_URL, databaseUN, new String(PW));
                        PreparedStatement pstmt = connection.prepareStatement(query2);

                        pstmt.setString(1, newEncodedUserHash);
                        pstmt.setString(2, newEncocdedUSerSalt);
                        pstmt.setString(3, username);

                        connection.setAutoCommit(false);

                        pstmt.executeUpdate();


                        connection.commit();
                        connection.close();
                    } catch (ClassNotFoundException ex) {
                        System.out.println(ex);
                        queryFail = true;
                        //Logger.getLogger(ProxyMain.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (SQLException ex) {
                        System.out.println(ex);
                        queryFail = true;
                        //Logger.getLogger(ProxyMain.class.getName()).log(Level.SEVERE, null, ex);
                    }

                    if(queryFail){
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(Profile.this,"Error Occurred", Toast.LENGTH_LONG).show();
                            }
                        });
                        try {
                            connection.close();
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                        return;
                    }else{
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(Profile.this,"Password Updated", Toast.LENGTH_LONG).show();
                            }
                        });
                    }




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

    private byte[] createSalt(){
        r = new SecureRandom();
        byte[] salty = new byte[32];
        r.nextBytes(salty);
        return salty;
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
        AlertDialog alertDialog = new AlertDialog.Builder(Profile.this).create();
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.setTitle("Sign in Password");
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
    private Boolean passwordCheck(char[] pw,boolean userPass) {
        Boolean test1 = false;
        Boolean test2 = false;
        Boolean test3 = false;
        Boolean test4 = false;
        Boolean finalResult = false;

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


        pw = null;
        return finalResult;
    }

    private void showMessage2(){
        AlertDialog alertDialog = new AlertDialog.Builder(Profile.this).create();
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



    private void showMessage3(){
        AlertDialog alertDialog = new AlertDialog.Builder(Profile.this).create();
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
        AlertDialog alertDialog = new AlertDialog.Builder(Profile.this).create();
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

    private void showMessage5(){
        AlertDialog alertDialog = new AlertDialog.Builder(Profile.this).create();
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.setTitle("Incorrect SignIn Password");
        alertDialog.setMessage("Password incorrect Edit Denied");
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }

    private void showMessage6(){
        AlertDialog alertDialog = new AlertDialog.Builder(Profile.this).create();
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.setTitle("Username Field Empty");
        alertDialog.setMessage("The username field cannot be blank");
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }


}
