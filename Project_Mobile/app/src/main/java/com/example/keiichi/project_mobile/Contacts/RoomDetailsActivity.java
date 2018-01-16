package com.example.keiichi.project_mobile.Contacts;

import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.example.keiichi.project_mobile.DAL.POJOs.EmailAddress;
import com.example.keiichi.project_mobile.DAL.POJOs.User;
import com.example.keiichi.project_mobile.R;

public class RoomDetailsActivity extends AppCompatActivity {

    private EmailAddress room;
    private Toolbar myToolbar;
    private String accessToken;
    private String userName;
    private String userEmail;
    private String displayName;
    private ImageButton phoneButton;
    private ImageButton calendarButton;
    private ImageButton mailButton;
    private ImageButton smsButton;
    private ImageView profilePic;
    private TextView headerDisplayName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_details);

        phoneButton = (ImageButton) findViewById(R.id.phoneButton);
        calendarButton = (ImageButton) findViewById(R.id.calendarButton);
        mailButton = (ImageButton) findViewById(R.id.mailButton);
        smsButton = (ImageButton) findViewById(R.id.smsButton);
        profilePic = (ImageView) findViewById(R.id.profilePic);
        headerDisplayName = (TextView) findViewById(R.id.displayName);

        accessToken = getIntent().getStringExtra("AccessToken");
        userName = getIntent().getStringExtra("userName");
        userEmail = getIntent().getStringExtra("userEmail");
        room = (EmailAddress) getIntent().getSerializableExtra("room");

        displayName = room.getName();

        headerDisplayName.setText(displayName);

        ColorGenerator generator = ColorGenerator.MATERIAL;

        int color2 = generator.getColor(displayName.substring(0,1));

        TextDrawable drawable1 = TextDrawable.builder()
                .beginConfig()
                .width(50)  // width in px
                .height(50) // height in px
                .endConfig()
                .buildRect(displayName.substring(0,1), color2);

        profilePic.setImageDrawable(drawable1);

        smsButton.setColorFilter(Color.GRAY);
        phoneButton.setColorFilter(Color.GRAY);

        phoneButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                Toast.makeText(getApplicationContext(), "No Phone Number found!", Toast.LENGTH_SHORT).show();
            }
        });

        smsButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                Toast.makeText(getApplicationContext(), "No Phone Number found!", Toast.LENGTH_SHORT).show();
            }
        });

        // INITIALISEER ACTION BAR
        myToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);

        // VOEG BACK BUTTON TOE AAN ACTION BAR
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

    }

    // VOEG ICONS TOE AAN DE ACTION BAR
    @Override
    public boolean onCreateOptionsMenu(Menu menu){

        return super.onCreateOptionsMenu(menu);

    }

    // METHODE VOOR DE CLICKABLE ICOONTJES IN DE ACTION BAR
    @Override
    public boolean onOptionsItemSelected(MenuItem item){

        switch(item.getItemId()){

            // WANNEER BACK BUTTON WORDT AANGEKLIKT (<-)
            case android.R.id.home:
                Intent intentContacts = new Intent(RoomDetailsActivity.this, ContactsActivity.class);
                intentContacts.putExtra("AccessToken", accessToken);
                intentContacts.putExtra("userName", userName);
                intentContacts.putExtra("userEmail", userEmail);

                startActivity(intentContacts);

                RoomDetailsActivity.this.finish();

                return true;


            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);
        }
    }

}
