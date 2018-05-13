package com.example.seanb.webwarden;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageButton;

public class ManageDevices extends AppCompatActivity implements View.OnClickListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_devices);

        ImageButton websiteAccess = findViewById(R.id.imageButton12);
        ImageButton profileBT = findViewById(R.id.imageButton10);
        ImageButton accessControlBT = findViewById(R.id.imageButton11);
        ImageButton mainMenu = findViewById(R.id.imageButton9);

        accessControlBT.setOnClickListener(this);
        profileBT.setOnClickListener(this);
        mainMenu.setOnClickListener(this);
        websiteAccess.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.imageButton11:
                Intent access= new Intent(this,AccessControl.class);
                startActivity(access);
                break;
            case R.id.imageButton9:
                Intent main= new Intent(this,MainMenu.class);
                startActivity(main);
                break;
            case R.id.imageButton10:
                Intent profile= new Intent(this,Profile.class);
                startActivity(profile);
                break;
            case R.id.imageButton12:
                Intent website= new Intent(this,WebsiteAccess.class);
                startActivity(website);
                break;


        }
    }
}
