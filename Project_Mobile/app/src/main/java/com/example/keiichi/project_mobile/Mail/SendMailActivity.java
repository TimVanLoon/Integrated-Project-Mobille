package com.example.keiichi.project_mobile.Mail;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.keiichi.project_mobile.Calendar.AddEventActivity;
import com.example.keiichi.project_mobile.Calendar.AttendeeActivity;
import com.example.keiichi.project_mobile.Calendar.CalendarActivity;
import com.example.keiichi.project_mobile.Calendar.ListEventsActivity;
import com.example.keiichi.project_mobile.Contacts.ContactsDetailsActivity;
import com.example.keiichi.project_mobile.Contacts.RoomDetailsActivity;
import com.example.keiichi.project_mobile.Contacts.UserDetailsActivity;
import com.example.keiichi.project_mobile.DAL.POJOs.Contact;
import com.example.keiichi.project_mobile.DAL.POJOs.EmailAddress;
import com.example.keiichi.project_mobile.DAL.POJOs.User;
import com.example.keiichi.project_mobile.MainActivity;
import com.example.keiichi.project_mobile.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

import jp.wasabeef.richeditor.RichEditor;

public class SendMailActivity extends AppCompatActivity {

    final private String URL_POSTADRESS = "https://graph.microsoft.com/v1.0/me/sendMail";
    private List<String> mailList = new ArrayList<>();
    private List<String> ccMailList = new ArrayList<>();
    private Toolbar myToolbar;
    private TextView MailAdress;
    private TextView Subject;
    private RichEditor MailBody;
    private String emailAddress;
    private String accessToken;
    private String userName;
    private String userEmail;
    private String fromContactDetailsActivity;
    private String fromRoomActivity;
    private String fromUserActivity;
    private String fromRecipientActivity;
    private RichEditor editor;
    private MenuItem sendItem;
    private Contact contact;
    private EmailAddress room;
    private User user;
    private ImageView plusContactIcon;
    private ImageView plusCCIcon;
    private TextView CCtje;
    private String allMails = "";
    private String allCCMails = "";
    private JsonArrayBuilder toRecipientsArray;
    private JsonArrayBuilder ccArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_mail);

        accessToken = getIntent().getStringExtra("AccessToken");
        userName = getIntent().getStringExtra("userName");
        userEmail = getIntent().getStringExtra("userEmail");
        fromContactDetailsActivity = getIntent().getStringExtra("fromContactDetailsActivity");
        fromRoomActivity = getIntent().getStringExtra("fromRoomActivity");
        fromUserActivity = getIntent().getStringExtra("fromUserActivity");
        fromRecipientActivity = getIntent().getStringExtra("fromRecipientActivity");

        MailAdress = findViewById(R.id.TextMailAdress);
        Subject = findViewById(R.id.TextMailSubject);
        MailBody = findViewById(R.id.editor);
        plusContactIcon = (ImageView) findViewById(R.id.plusContactIcon);
        plusCCIcon = (ImageView) findViewById(R.id.plusCCIcon);
        CCtje = findViewById(R.id.ccMailInput);

        myToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);

        // VOEG BACK BUTTON TOE AAN ACTION BAR
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        Intent intent = getIntent();

        contact = (Contact) getIntent().getSerializableExtra("contact");
        room = (EmailAddress) getIntent().getSerializableExtra("room");
        user = (User) getIntent().getSerializableExtra("user");

        emailAddress = intent.getStringExtra("emailAddress");

        mailList = (List<String>)getIntent().getSerializableExtra("mailList");
        ccMailList = (List<String>)getIntent().getSerializableExtra("ccMailList");

        if (emailAddress != null){
            MailAdress.setText(emailAddress);
        }

        plusContactIcon.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                Intent intentRecipient = new Intent(SendMailActivity.this, RecipientActivity.class);
                intentRecipient.putExtra("AccessToken", accessToken);
                intentRecipient.putExtra("userName", userName);
                intentRecipient.putExtra("userEmail", userEmail);
                intentRecipient.putExtra("plusReceiver", "yes");

                if(!MailAdress.getText().toString().isEmpty()){
                    mailList = new ArrayList<>();

                    for (String string : MailAdress.getText().toString().split("\\s+")){
                        mailList.add(string);
                    }
                }

                if(!CCtje.getText().toString().isEmpty()){
                    ccMailList = new ArrayList<>();

                    for (String string : CCtje.getText().toString().split("\\s+")){
                        ccMailList.add(string);
                    }
                }

                intentRecipient.putExtra("mailList",(Serializable) mailList);
                intentRecipient.putExtra("ccMailList",(Serializable) ccMailList);

                startActivity(intentRecipient);

                SendMailActivity.this.finish();
            }
        });

        plusCCIcon.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                Intent intentRecipient = new Intent(SendMailActivity.this, RecipientActivity.class);
                intentRecipient.putExtra("AccessToken", accessToken);
                intentRecipient.putExtra("userName", userName);
                intentRecipient.putExtra("userEmail", userEmail);
                intentRecipient.putExtra("plusCC", "yes");

                if(!MailAdress.getText().toString().isEmpty()){
                    mailList = new ArrayList<>();

                    for (String string : MailAdress.getText().toString().split("\\s+")){
                        mailList.add(string);
                    }
                }

                if(!CCtje.getText().toString().isEmpty()){
                    ccMailList = new ArrayList<>();

                    for (String string : CCtje.getText().toString().split("\\s+")){
                        ccMailList.add(string);
                    }
                }

                intentRecipient.putExtra("mailList",(Serializable) mailList);
                intentRecipient.putExtra("ccMailList",(Serializable) ccMailList);

                startActivity(intentRecipient);

                SendMailActivity.this.finish();
            }
        });

        if(fromRecipientActivity != null){

            if(mailList != null){
                for (String mail: mailList){
                    allMails += mail + " ";
                }
                MailAdress.setText(allMails);
            }

            if(ccMailList != null){
                for (String mail: ccMailList){
                    allCCMails += mail + " ";
                }
                CCtje.setText(allCCMails);
            }

        }

    }

    private void SendMail() throws JSONException {
        System.out.println(MailBody.getHtml());
        RequestQueue queue = Volley.newRequestQueue(this);

        if(accessToken == null){
            Intent logout = new Intent(SendMailActivity.this, MainActivity.class);
            logout.putExtra("AccessToken", accessToken);
            logout.putExtra("userName", userName);
            logout.putExtra("userEmail", userEmail);

            startActivity(logout);

            SendMailActivity.this.finish();
        }

        final JSONObject jsonObject = new JSONObject(buildJsonMail());

        JsonObjectRequest objectRequest = new JsonObjectRequest(Request.Method.POST, URL_POSTADRESS, jsonObject,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Toast.makeText(getApplicationContext(), "Mail sent!", Toast.LENGTH_SHORT).show();
                        System.out.println(response.toString());
                    }

                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.e("Error: ", error.getMessage());
                error.printStackTrace();
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + accessToken);

                return headers;
            }

        };

        queue.add(objectRequest);

    }

    private String buildJsonMail() {

        if(!CCtje.getText().toString().isEmpty()){
            toRecipientsArray = createToRecipientsArray();
            ccArray = createCCArray();

            JsonObjectBuilder factory = Json.createObjectBuilder()
                    .add("message", Json.createObjectBuilder().
                            add("subject", Subject.getText().toString()).
                            add("body", Json.createObjectBuilder().
                                    add("contentType", "Text").
                                    add("content", Html.fromHtml(MailBody.getHtml()).toString())).
                            add("toRecipients", toRecipientsArray).
                            add("ccRecipients", ccArray)
                    );
            return factory.build().toString();

        } else {
            toRecipientsArray = createToRecipientsArray();

            JsonObjectBuilder factory = Json.createObjectBuilder()
                    .add("message", Json.createObjectBuilder().
                            add("subject", Subject.getText().toString()).
                            add("body", Json.createObjectBuilder().
                                    add("contentType", "Text").
                                    add("content", Html.fromHtml(MailBody.getHtml()).toString())).
                            add("toRecipients", toRecipientsArray)
                    );
            return factory.build().toString();

        }

    }

    private JsonArrayBuilder createCCArray() {

        JsonArrayBuilder BaatsCC = Json.createArrayBuilder();
        for (String string :  CCtje.getText().toString().split("\\s+")){
            BaatsCC.add(
                    Json.createObjectBuilder().
                            add("emailAddress", Json.createObjectBuilder().
                                    add("address",string)));
        }

        return BaatsCC;
    }

    private JsonArrayBuilder createToRecipientsArray() {
        JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
        for (String string :  MailAdress.getText().toString().split("\\s+")){
            arrayBuilder.add(
                    Json.createObjectBuilder().
                            add("emailAddress", Json.createObjectBuilder().
                                    add("address",string)));
        }

        return arrayBuilder;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.send_navigation, menu);
        MenuItem addItem = menu.findItem(R.id.action_send);

        sendItem = menu.findItem(R.id.action_send);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){

        switch(item.getItemId()){

            // WANNEER BACK BUTTON WORDT AANGEKLIKT (<-)
            case android.R.id.home:

                if(fromContactDetailsActivity == null && fromRoomActivity == null && fromUserActivity == null){

                    Intent intentListMails = new Intent(SendMailActivity.this, ListMailsActvity.class);
                    intentListMails.putExtra("AccessToken", accessToken);
                    intentListMails.putExtra("userName", userName);
                    intentListMails.putExtra("userEmail", userEmail);

                    startActivity(intentListMails);

                    SendMailActivity.this.finish();

                } else if(fromRoomActivity == null && fromUserActivity == null) {

                    Intent intentContactDetails = new Intent(SendMailActivity.this, ContactsDetailsActivity.class);
                    intentContactDetails.putExtra("AccessToken", accessToken);
                    intentContactDetails.putExtra("userName", userName);
                    intentContactDetails.putExtra("userEmail", userEmail);
                    intentContactDetails.putExtra("contact", contact);

                    startActivity(intentContactDetails);

                    SendMailActivity.this.finish();

                } else if(fromUserActivity == null && fromContactDetailsActivity == null){

                    Intent intentContactDetails = new Intent(SendMailActivity.this, RoomDetailsActivity.class);
                    intentContactDetails.putExtra("AccessToken", accessToken);
                    intentContactDetails.putExtra("userName", userName);
                    intentContactDetails.putExtra("userEmail", userEmail);
                    intentContactDetails.putExtra("room", room);

                    startActivity(intentContactDetails);

                    SendMailActivity.this.finish();

                } else if(fromRoomActivity == null && fromContactDetailsActivity == null){

                    Intent intentContactDetails = new Intent(SendMailActivity.this, UserDetailsActivity.class);
                    intentContactDetails.putExtra("AccessToken", accessToken);
                    intentContactDetails.putExtra("userName", userName);
                    intentContactDetails.putExtra("userEmail", userEmail);
                    intentContactDetails.putExtra("user", user);

                    startActivity(intentContactDetails);

                    SendMailActivity.this.finish();

                }


                return true;

            case R.id.action_send:
                try {

                    if(!MailAdress.getText().toString().isEmpty()){
                        SendMail();

                        sendItem.setEnabled(false);

                        Intent intentListMails = new Intent(SendMailActivity.this, ListMailsActvity.class);
                        intentListMails.putExtra("AccessToken", accessToken);
                        intentListMails.putExtra("userName", userName);
                        intentListMails.putExtra("userEmail", userEmail);

                        startActivity(intentListMails);

                        SendMailActivity.this.finish();
                        //SendMailActivity.this.finish();
                    } else {
                        MailAdress.setError("Required field!");
                    }


                } catch (JSONException e) {
                    e.printStackTrace();
                }

                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed(){

        if(fromContactDetailsActivity == null && fromRoomActivity == null && fromUserActivity == null){

            Intent intentListMails = new Intent(SendMailActivity.this, ListMailsActvity.class);
            intentListMails.putExtra("AccessToken", accessToken);
            intentListMails.putExtra("userName", userName);
            intentListMails.putExtra("userEmail", userEmail);

            startActivity(intentListMails);

            SendMailActivity.this.finish();

        } else if(fromRoomActivity == null && fromUserActivity == null) {

            Intent intentContactDetails = new Intent(SendMailActivity.this, ContactsDetailsActivity.class);
            intentContactDetails.putExtra("AccessToken", accessToken);
            intentContactDetails.putExtra("userName", userName);
            intentContactDetails.putExtra("userEmail", userEmail);
            intentContactDetails.putExtra("contact", contact);

            startActivity(intentContactDetails);

            SendMailActivity.this.finish();

        } else if(fromUserActivity == null && fromContactDetailsActivity == null){

            Intent intentContactDetails = new Intent(SendMailActivity.this, RoomDetailsActivity.class);
            intentContactDetails.putExtra("AccessToken", accessToken);
            intentContactDetails.putExtra("userName", userName);
            intentContactDetails.putExtra("userEmail", userEmail);
            intentContactDetails.putExtra("room", room);

            startActivity(intentContactDetails);

            SendMailActivity.this.finish();

        } else if(fromRoomActivity == null && fromContactDetailsActivity == null){

            Intent intentContactDetails = new Intent(SendMailActivity.this, UserDetailsActivity.class);
            intentContactDetails.putExtra("AccessToken", accessToken);
            intentContactDetails.putExtra("userName", userName);
            intentContactDetails.putExtra("userEmail", userEmail);
            intentContactDetails.putExtra("user", user);

            startActivity(intentContactDetails);

            SendMailActivity.this.finish();

        }

    }

    public void minimizeApp() {
        Intent intentListMails = new Intent(SendMailActivity.this, ListMailsActvity.class);
        intentListMails.putExtra("AccessToken", accessToken);
        intentListMails.putExtra("userName", userName);
        intentListMails.putExtra("userEmail", userEmail);

        startActivity(intentListMails);

        SendMailActivity.this.finish();
    }

}
