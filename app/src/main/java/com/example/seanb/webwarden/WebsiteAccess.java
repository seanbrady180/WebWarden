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

import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

public class WebsiteAccess extends AppCompatActivity implements View.OnClickListener{
    private Button addWebsite;
    private Button removeWebsite;
    private Button viewWebsites;
    private EditText add;
    private EditText remove;
    private String website = "";
    private String newAddress;
    private URL url;
    private HttpURLConnection urlConnection;
    private Boolean incorrectAddress = false;
    private int pwLength;
    private SharedPreferences sharedPreferences;
    private ResultSet resultSet1;
    private int deleteResult;
    private ArrayList<String> siteList = new ArrayList<>();

    private Socket host ;
    private DataOutputStream os;
    private BufferedWriter bw;

    private String acPW;
    private String acSalt;
    private char[] userInputPW = new char[50];
    private Editable editable;
    private byte[] salt;
    private String userEncodedPw;

    private Connection connection;
    private String databasePW = "";
    private String databaseUN = "";
    private String PW = "";
    private final String DB_URL = "jdbc:mysql://webwarden.ck4ehi6goau1.eu-west-1.rds.amazonaws.com:3306/proxy";
    private String query1 = "Insert into blocked_sites(site) values(?)";
    private String query2 = "delete from proxy.blocked_sites where site = ?";
    private String query3 = "Select * from proxy.blocked_sites";





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPreferences = getSharedPreferences("user",MODE_PRIVATE);
        acPW = sharedPreferences.getString("acPW","");
        acSalt = sharedPreferences.getString("acSalt","");
        setContentView(R.layout.activity_website_access);
        ImageButton profileBT = findViewById(R.id.imageButton6);
        ImageButton accessControlBT = findViewById(R.id.imageButton7);
        ImageButton mainMenu = findViewById(R.id.imageButton5);

        addWebsite = findViewById(R.id.addWebsite);
        removeWebsite = findViewById(R.id.removeWebsite);
        viewWebsites = findViewById(R.id.viewSites);
        add = findViewById(R.id.addWebsiteET);
        remove = findViewById(R.id.removeWebsiteET);




        profileBT.setOnClickListener(this);
        accessControlBT.setOnClickListener(this);
        mainMenu.setOnClickListener(this);
        addWebsite.setOnClickListener(this);
        removeWebsite.setOnClickListener(this);
        viewWebsites.setOnClickListener(this);
    }





    @Override
    public void onClick(View view) {
        switch(view.getId()){

            case R.id.imageButton6:
                Intent profile= new Intent(this,Profile.class);
                startActivity(profile);
                break;
            case R.id.imageButton7:
                Intent access= new Intent(this,AccessControl.class);
                startActivity(access);
                break;
            case R.id.imageButton5:
                Intent menu= new Intent(this,MainMenu.class);
                startActivity(menu);
                break;
            case R.id.addWebsite:
                addSite();
                break;
            case R.id.removeWebsite:
                removeSite();
                break;
            case R.id.viewSites:
                getSites();
                break;

        }
    }

    private void addSite(){

        new Thread(){
            public void run(){
                website = add.getText().toString();
                if(website.contains("https://")){
                    StringBuffer change = new StringBuffer(website);
                    newAddress = change.substring(8,website.length());
                    try {
                        url = new URL(website);
                        urlConnection = (HttpURLConnection) url.openConnection();
                        urlConnection.disconnect();
                    } catch (MalformedURLException e) {
                        incorrectAddress = true;
                        e.printStackTrace();
                    } catch (IOException e) {
                        incorrectAddress = true;
                        e.printStackTrace();
                    }

                }else if(website.contains("http://")){
                    StringBuffer change = new StringBuffer(website);
                    newAddress = change.substring(7,website.length());
                    try {
                        url = new URL(website);
                        urlConnection = (HttpURLConnection) url.openConnection();
                        urlConnection.disconnect();
                    } catch (MalformedURLException e) {
                        incorrectAddress = true;
                        e.printStackTrace();
                    } catch (IOException e) {
                        incorrectAddress = true;
                        e.printStackTrace();
                    }
                }else{
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showMessage1();
                        }
                    });
                    return;
                }

                if(incorrectAddress){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showMessage1();
                        }
                    });
                    return;
                }
                boolean updateFail = false;
                if(PW.equals("")){
                    databasePassword();
                }
                try {

                    Class.forName("com.mysql.jdbc.Driver");
                    connection = DriverManager.getConnection(DB_URL, databaseUN, new String(PW));
                    PreparedStatement pstmt = connection.prepareStatement(query1);

                    pstmt.setString(1, newAddress);

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
                            Toast.makeText(WebsiteAccess.this,"Error Occurred", Toast.LENGTH_LONG).show();
                        }
                    });
                }else{
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(WebsiteAccess.this,"Site added", Toast.LENGTH_LONG).show();
                        }
                    });
                    try {
                        host = new Socket("192.168.0.39",3000);
                        os = new DataOutputStream(host.getOutputStream());
                        bw = new BufferedWriter(new OutputStreamWriter(os));

                        bw.write("add site\n");
                        bw.write(newAddress+"\n");
                        bw.flush();

                        os.close();
                        host.close();

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();
    }

    private void removeSite(){
        new Thread(){
            public void run(){
                editable = null;
                if(remove.getText().length() == 0){
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
                System.out.println("User input Hash:"+userEncodedPw);

                if(!userEncodedPw.equals(acPW)){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showMessage5();
                        }
                    });

                }else{
                    website = remove.getText().toString();
                    boolean updateFail = false;
                    if(PW.equals("")){
                        databasePassword();
                    }
                    try {

                        Class.forName("com.mysql.jdbc.Driver");
                        connection = DriverManager.getConnection(DB_URL, databaseUN, new String(PW));
                        PreparedStatement pstmt = connection.prepareStatement(query2);

                        pstmt.setString(1, website);

                        connection.setAutoCommit(false);

                        pstmt.execute();
                        deleteResult = pstmt.getUpdateCount();


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

                    if((updateFail) || (deleteResult == 0)){
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                showMessage2();
                            }
                        });
                    }else{
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(WebsiteAccess.this,"Site Deleted", Toast.LENGTH_LONG).show();
                            }
                        });

                        try {
                            host = new Socket("192.168.0.39",3000);
                            os = new DataOutputStream(host.getOutputStream());
                            bw = new BufferedWriter(new OutputStreamWriter(os));

                            bw.write("remove site\n");
                            bw.write(website+"\n");
                            bw.flush();

                            os.close();
                            host.close();

                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }
                }
            }
        }.start();
    }

    private void getSites(){
        new Thread(){
            public void run(){
                boolean updateFail = false;
                if(PW.equals("")){
                    databasePassword();
                }
                try {

                    Class.forName("com.mysql.jdbc.Driver");
                    connection = DriverManager.getConnection(DB_URL, databaseUN, new String(PW));
                    PreparedStatement pstmt = connection.prepareStatement(query3);


                    connection.setAutoCommit(false);

                    resultSet1 = pstmt.executeQuery();
                    while (resultSet1.next()) {
                        siteList.add(resultSet1.getString("site"));
                    }

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
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showMessage6();
                    }
                });

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
        AlertDialog alertDialog = new AlertDialog.Builder(WebsiteAccess.this).create();
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.setTitle("Incorrect Address");
        alertDialog.setMessage("Please enter a valid URL");
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }

    private void showMessage2(){
        AlertDialog alertDialog = new AlertDialog.Builder(WebsiteAccess.this).create();
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.setTitle("No site");
        alertDialog.setMessage("Could not find the site");
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }

    private void showMessage3(){
        AlertDialog alertDialog = new AlertDialog.Builder(WebsiteAccess.this).create();
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.setTitle("Incorrect Site");
        alertDialog.setMessage("The site field cannot be blank");
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }

    private void showMessage4(){
        AlertDialog alertDialog = new AlertDialog.Builder(WebsiteAccess.this).create();
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
        AlertDialog alertDialog = new AlertDialog.Builder(WebsiteAccess.this).create();
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

    private void showMessage6(){
        CharSequence[] sites = siteList.toArray(new CharSequence[siteList.size()]);
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(WebsiteAccess.this);

        //alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.setTitle("Incorrect Access Password");
        alertDialog.setItems( sites, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        alertDialog.setNeutralButton("Ok",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        AlertDialog alert = alertDialog.create();
        //alert.setCanceledOnTouchOutside(false);
        alertDialog.show();
    }

}
