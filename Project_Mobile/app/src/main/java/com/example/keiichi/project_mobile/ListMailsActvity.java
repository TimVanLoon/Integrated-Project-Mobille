package com.example.keiichi.project_mobile;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
// import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.microsoft.identity.client.AuthenticationCallback;
import com.microsoft.identity.client.AuthenticationResult;
import com.microsoft.identity.client.MsalClientException;
import com.microsoft.identity.client.MsalException;
import com.microsoft.identity.client.MsalServiceException;
import com.microsoft.identity.client.MsalUiRequiredException;
import com.microsoft.identity.client.PublicClientApplication;
import com.microsoft.identity.client.User;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ListMailsActvity extends AppCompatActivity {

    final static String CLIENT_ID = "d3b60662-7768-4a50-b96f-eb1dfcc7ec8d";
    final static String SCOPES[] = {
            "https://graph.microsoft.com/Mail.Send",
            "https://graph.microsoft.com/Mail.ReadWrite",
            "https://graph.microsoft.com/Calendars.ReadWrite",
            "https://graph.microsoft.com/Calendars.Read",
            "https://graph.microsoft.com/Contacts.Read",
            "https://graph.microsoft.com/Calendars.ReadWrite"};

    //final static String MSGRAPH_URL = "https://graph.microsoft.com/v1.0/me";
    final static String MSGRAPH_URL = "https://graph.microsoft.com/v1.0/me/mailFolders('Inbox')/messages?$top=25";
    //final static String MSGRAPH_URL = "https://outlook.office365.com/api/v2.0/me/MailFolders('inbox')/messages";
    final static String CHANNEL_ID = "my_channel_01";

    private ListView mListview;
    private View mailLayout;

    private String accessToken;

    BottomNavigationView mBottomNav;


    /* UI & Debugging Variables */
    private static final String TAG = MainActivity.class.getSimpleName();
    // Button callGraphButton;
    Button signOutButton;
    Button toSendMailActivity;

    /* Azure AD Variables */
    private PublicClientApplication sampleApp;
    private AuthenticationResult authResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_mails);

        mListview = (ListView) findViewById(R.id.ListViewMails);
        signOutButton = (Button) findViewById(R.id.clearCache);
        toSendMailActivity = (Button) findViewById(R.id.ButtonSendMail);

       // addNotification();

        mBottomNav = (BottomNavigationView) findViewById(R.id.navigation);

        Menu menu = mBottomNav.getMenu();
        MenuItem menuItem = menu.getItem(0);
        menuItem.setChecked(true);

        mBottomNav.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                switch(item.getItemId()) {

                    case R.id.action_calendar:
                        Intent intentCalendar = new Intent(ListMailsActvity.this, CalendarActivity.class);
                        intentCalendar.putExtra("AccessToken", accessToken);
                        startActivity(intentCalendar);
                        break;
                    case R.id.action_mail:

                        break;
                    case R.id.action_user:
                        Intent intentContacts = new Intent(ListMailsActvity.this, ContactsActivity.class);
                        intentContacts.putExtra("AccessToken", accessToken);
                        startActivity(intentContacts);
                        break;

                }

                return false;
            }
        });

        toSendMailActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toSendMailActivity();
            }
        });
        signOutButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                onSignOutClicked();
            }
        });

  /* Configure your sample app and save state for this activity */
        sampleApp = null;
        if (sampleApp == null) {
            sampleApp = new PublicClientApplication(
                    this.getApplicationContext(),
                    CLIENT_ID);
        }

  /* Attempt to get a user and acquireTokenSilent
   * If this fails we do an interactive request
   */
        List<User> users = null;

        try {
            users = sampleApp.getUsers();

            if (users != null && users.size() == 1) {
          /* We have 1 user */

                sampleApp.acquireTokenSilentAsync(SCOPES, users.get(0), getAuthSilentCallback());
            } else {
          /* We have no user */

          /* Let's do an interactive request */
                sampleApp.acquireToken(this, SCOPES, getAuthInteractiveCallback());
            }
        } catch (MsalClientException e) {
            Log.d(TAG, "MSAL Exception Generated while getting users: " + e.toString());

        } catch (IndexOutOfBoundsException e) {
            Log.d(TAG, "User at this position does not exist: " + e.toString());
        }

        onCallGraphClicked();


    }


//
// App callbacks for MSAL
// ======================
// getActivity() - returns activity so we can acquireToken within a callback
// getAuthSilentCallback() - callback defined to handle acquireTokenSilent() case
// getAuthInteractiveCallback() - callback defined to handle acquireToken() case
//

    public Activity getActivity() {
        return this;
    }

    /* Callback method for acquireTokenSilent calls
     * Looks if tokens are in the cache (refreshes if necessary and if we don't forceRefresh)
     * else errors that we need to do an interactive request.
     */
    private AuthenticationCallback getAuthSilentCallback() {
        return new AuthenticationCallback() {
            @Override
            public void onSuccess(AuthenticationResult authenticationResult) {
            /* Successfully got a token, call Graph now */
                Log.d(TAG, "Successfully authenticated");

                //Print auth result om te dubbel checken
                System.out.println(Arrays.toString(authenticationResult.getScope()));

            /* Store the authResult */
                authResult = authenticationResult;

                // accesstoken in var steken
                accessToken = authResult.getAccessToken();

            /* call graph */
                callGraphAPI();

            /* update the UI to post call Graph state */
                updateSuccessUI();
            }

            @Override
            public void onError(MsalException exception) {
            /* Failed to acquireToken */
                Log.d(TAG, "Authentication failed: " + exception.toString());

                if (exception instanceof MsalClientException) {
                /* Exception inside MSAL, more info inside MsalError.java */
                } else if (exception instanceof MsalServiceException) {
                /* Exception when communicating with the STS, likely config issue */
                } else if (exception instanceof MsalUiRequiredException) {
                /* Tokens expired or no session, retry with interactive */
                }
            }

            @Override
            public void onCancel() {
            /* User canceled the authentication */
                Log.d(TAG, "User cancelled login.");
            }
        };
    }


    /* Callback used for interactive request.  If succeeds we use the access
         * token to call the Microsoft Graph. Does not check cache
         */
    private AuthenticationCallback getAuthInteractiveCallback() {
        return new AuthenticationCallback() {
            @Override
            public void onSuccess(AuthenticationResult authenticationResult) {
            /* Successfully got a token, call graph now */
                Log.d(TAG, "Successfully authenticated");
                Log.d(TAG, "ID Token: " + authenticationResult.getIdToken());
                Log.d(TAG, "Acces Token: " + authenticationResult.getAccessToken());

            /* Store the auth result */
                authResult = authenticationResult;

            /* call Graph */
                callGraphAPI();

            /* update the UI to post call Graph state */
                updateSuccessUI();
            }

            @Override
            public void onError(MsalException exception) {
            /* Failed to acquireToken */
                Log.d(TAG, "Authentication failed: " + exception.toString());

                if (exception instanceof MsalClientException) {
                /* Exception inside MSAL, more info inside MsalError.java */
                } else if (exception instanceof MsalServiceException) {
                /* Exception when communicating with the STS, likely config issue */
                }
            }

            @Override
            public void onCancel() {
            /* User canceled the authentication */
                Log.d(TAG, "User cancelled login.");
            }
        };
    }

    /* Set the UI for successful token acquisition data */
    private void updateSuccessUI() {
        signOutButton.setVisibility(View.VISIBLE);
        findViewById(R.id.welcome).setVisibility(View.VISIBLE);
        ((TextView) findViewById(R.id.welcome)).setText("Welcome, " +
                authResult.getUser().getName());
    }

    /* Use MSAL to acquireToken for the end-user
     * Callback will call Graph api w/ access token & update UI
     */
    private void onCallGraphClicked() {
        sampleApp.acquireToken(getActivity(), SCOPES, getAuthInteractiveCallback());
    }

    /* Handles the redirect from the System Browser */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        sampleApp.handleInteractiveRequestRedirect(requestCode, resultCode, data);
    }

    /* Use Volley to make an HTTP request to the /me endpoint from MS Graph using an access token */
    private void callGraphAPI() {
        Log.d(TAG, "Starting volley request to graph");

    /* Make sure we have a token to send to graph */
        if (authResult.getAccessToken() == null) {
            return;
        }

        RequestQueue queue = Volley.newRequestQueue(this);
        JSONObject parameters = new JSONObject();

        try {
            parameters.put("key", "value");
        } catch (Exception e) {
            Log.d(TAG, "Failed to put parameters: " + e.toString());
        }
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, MSGRAPH_URL,
                parameters, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
            /* Successfully called graph, process data and send to UI */
                Log.d(TAG, "Response: " + response.toString());

                try {
                    updateGraphUI(response);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "Error: " + error.toString());
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + authResult.getAccessToken());
                return headers;
            }
        };

        Log.d(TAG, "Adding HTTP GET to Queue, Request: " + request.toString());

        request.setRetryPolicy(new DefaultRetryPolicy(
                3000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(request);
    }

    /* Sets the Graph response */
    private void updateGraphUI(JSONObject graphResponse) throws JSONException {

        //beetje kloten met mails
        System.out.println(graphResponse);
        JSONArray mailJsonArray = null;
        //haal mails binnen
        try {
            mailJsonArray = (JSONArray) graphResponse.get("value");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        assert mailJsonArray != null;
        JSONObject object = mailJsonArray.getJSONObject(1);
        System.out.println(object.get("from"));



        MailAdapter mailAdapter = new MailAdapter(this, mailJsonArray);
        mListview.setAdapter(mailAdapter);
        final JSONArray finalMailJsonArray = mailJsonArray;
        mListview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Intent showMail = new Intent(ListMailsActvity.this, DisplayMailActivity.class);
                try {
                    showMail.putExtra("mailObjext", finalMailJsonArray.getString(position));
                    showMail.putExtra("accestoken", authResult.getAccessToken());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                startActivity(showMail);
            }
        });


    }

    /* Clears a user's tokens from the cache.
 * Logically similar to "sign out" but only signs out of this app.
 */
    private void onSignOutClicked() {

    /* Attempt to get a user and remove their cookies from cache */
        List<User> users = null;

        try {
            users = sampleApp.getUsers();

            if (users == null) {
            /* We have no users */

            } else if (users.size() == 1) {
            /* We have 1 user */
            /* Remove from token cache */
                sampleApp.remove(users.get(0));
                updateSignedOutUI();

            } else {
            /* We have multiple users */
                for (int i = 0; i < users.size(); i++) {
                    sampleApp.remove(users.get(i));
                }
            }

            Toast.makeText(getBaseContext(), "Signed Out!", Toast.LENGTH_SHORT)
                    .show();

        } catch (MsalClientException e) {
            Log.d(TAG, "MSAL Exception Generated while getting users: " + e.toString());

        } catch (IndexOutOfBoundsException e) {
            Log.d(TAG, "User at this position does not exist: " + e.toString());
        }
    }

    /* Set the UI for signed-out user */
    private void updateSignedOutUI() {
        //callGraphButton.setVisibility(View.VISIBLE);
        signOutButton.setVisibility(View.INVISIBLE);
        findViewById(R.id.welcome).setVisibility(View.INVISIBLE);
    }

    private void toSendMailActivity() {
        Intent intent = new Intent(this, SendMailActivity.class);
        intent.putExtra("accestoken", authResult.getAccessToken());

        startActivity(intent);
    }

    /*
    private void addNotification() {
        android.support.v4.app.NotificationCompat.Builder notification =
                new NotificationCompat.Builder(this)
                        .setContentTitle("Hey boo")
                        .setContentText("Wanna cuddle?")
                        .setSmallIcon(R.drawable.bootje);


        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        notification.setContentIntent(contentIntent);

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(0, notification.build());
    }

*/

}
