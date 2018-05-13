package com.example.seanb.webwarden;

import android.app.ActivityManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static java.lang.Thread.sleep;

public class MyService extends Service {
    int id;
    int number;
    NotificationCompat.Builder notification;
    int notID = 455;
    static int abc = 0;
    ServerSocket server;
    Socket socket;
    Boolean run = true;
    String userChoose = "";
    String result;
    BroadcastReceiver broadcastReceiver;
    DataOutputStream os;
    BufferedWriter bw;
    DataInputStream is;
    BufferedReader br;
    String device;
    String website;
    String badWord;
    ArrayList<String> foundWords = new ArrayList();

    @Override
    public void onCreate() {
        //new Thread(new Notification()).start();
        super.onCreate();

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (action.equalsIgnoreCase("getting_data")) {
                    userChoose = intent.getStringExtra("value");
                }
            }
        };

        IntentFilter intentFilter = new IntentFilter();
        // set the custom action
        intentFilter.addAction("getting_data"); //Action is just a string used to identify the receiver as there can be many in your app so it helps deciding which receiver should receive the intent.
        // register the receiver
        registerReceiver(broadcastReceiver, intentFilter);

        new Thread() {
            public void run() {


                while (run) {
                    synchronized (Notification.lock){
                        try {
                            if (id < 3) {
                                userChoose = "";
                                System.out.println("App Running!!");
                                String alert;
                                server = new ServerSocket(5000);
                                server.setSoTimeout(10000);
                                try {
                                    socket = server.accept();
                                    os = new DataOutputStream(socket.getOutputStream());
                                    bw = new BufferedWriter(new OutputStreamWriter(os));

                                    is = new DataInputStream(socket.getInputStream());
                                    br = new BufferedReader(new InputStreamReader(is));
                                    website = br.readLine();

                                    Intent intent = new Intent(MyService.this, SiteControl.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    intent.putExtra("website", website);
                                    intent.putExtra("startMainMenu",false);
                                    long def = 0;
                                    intent.putExtra("timeStart",def);
                                    startActivity(intent);

                                    try {
                                        sleep(20000);
                                    } catch (InterruptedException e1) {
                                        e1.printStackTrace();
                                    }

                                    System.out.println("User chose:"+userChoose);
                                    if(userChoose.equals("allow")){
                                        result = "allow";
                                    }else{
                                        result = "block";
                                    }

                                    bw.write(result);
                                    bw.flush();

                                    is.close();
                                    os.close();
                                    socket.close();
                                    server.close();


                                } catch (SocketTimeoutException e) {

                                    server.close();
                                }
                                Notification.lock.notify();

                            } else {
                                userChoose = "";
                                server = new ServerSocket(5000);
                                server.setSoTimeout(10000);
                                try {

                                    socket = server.accept();
                                    is = new DataInputStream(socket.getInputStream());
                                    br = new BufferedReader(new InputStreamReader(is));
                                    os = new DataOutputStream(socket.getOutputStream());
                                    bw = new BufferedWriter(new OutputStreamWriter(os));

                                    website = br.readLine();

                                    notification = new NotificationCompat.Builder(MyService.this, "channel_ID_2");
                                    notification.setAutoCancel(true);

                                    notification.setSmallIcon(R.drawable.wabutton);
                                    notification.setTicker("This is the ticker");
                                    notification.setWhen(System.currentTimeMillis());
                                    notification.setContentTitle("Webiste Access");
                                    notification.setContentText("Badd websites are being accessed");
                                    Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                                    notification.setSound(alarmSound);


                                    Intent intent = new Intent(MyService.this, SiteControl.class);
                                    SimpleDateFormat sd = new SimpleDateFormat("HH:mm:ss");
                                    intent.putExtra("website", website);
                                    intent.putExtra("startMainMenu",true);
                                    long timeStart = System.currentTimeMillis();
                                    intent.putExtra("timeStart",timeStart);

                                    PendingIntent pendingIntent = PendingIntent.getActivity(MyService.this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                                    notification.setContentIntent(pendingIntent);
                                    NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                                    nm.notify(notID, notification.build());
                                    notID++;

                                    try {
                                        sleep(25000);
                                    } catch (InterruptedException e1) {
                                        e1.printStackTrace();
                                    }

                                    if(userChoose.equals("allow")){
                                        result = "allow";
                                    }else{
                                        result = "block";
                                    }

                                    bw.write(result);
                                    bw.flush();

                                    is.close();
                                    os.close();
                                    socket.close();
                                    server.close();



                                } catch (SocketTimeoutException e) {

                                    server.close();
                                }


                            }

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        System.out.println("Thread free");
                    }

                }

            }

        }.start();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startid) {

        this.id = startid;
        String a = Integer.toString(startid);
        Toast.makeText(this, a, Toast.LENGTH_LONG).show();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {

        unregisterReceiver(broadcastReceiver);
        run = false;
        Toast.makeText(this, "Service Stopped", Toast.LENGTH_LONG).show();
        super.onDestroy();


    }




    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


}
