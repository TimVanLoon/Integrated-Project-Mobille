package com.example.keiichi.project_mobile;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

<<<<<<< HEAD
import com.google.firebase.iid.FirebaseInstanceId;
import com.pusher.client.Pusher;
import com.pusher.client.PusherOptions;
import com.pusher.client.channel.Channel;
import com.pusher.client.channel.SubscriptionEventListener;
=======
import com.example.keiichi.project_mobile.Mail.ListMailsActvity;
>>>>>>> cdd0178b5710967fa761be5ba5c81a4a9ab23a30

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Intent intent = new Intent(this, ListMailsActvity.class);
        startActivity(intent);



    }

    public void login(View view){
        Intent intent = new Intent(this, ListMailsActvity.class);
        startActivity(intent);

    }

}
