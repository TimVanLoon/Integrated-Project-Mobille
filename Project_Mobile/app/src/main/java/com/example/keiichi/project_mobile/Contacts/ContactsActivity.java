package com.example.keiichi.project_mobile.Contacts;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.LightingColorFilter;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ActionMode;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;
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
import com.example.keiichi.project_mobile.Calendar.CalendarActivity;
import com.example.keiichi.project_mobile.DAL.POJOs.Contact;
import com.example.keiichi.project_mobile.DAL.POJOs.ContactFolder;
import com.example.keiichi.project_mobile.DAL.POJOs.EmailAddress;
import com.example.keiichi.project_mobile.DAL.POJOs.MailFolder;
import com.example.keiichi.project_mobile.DAL.POJOs.Message;
import com.example.keiichi.project_mobile.DAL.POJOs.PhysicalAddress;
import com.example.keiichi.project_mobile.DAL.POJOs.User;
import com.example.keiichi.project_mobile.Mail.ListMailsActvity;
import com.example.keiichi.project_mobile.Mail.MailAdapter;
import com.example.keiichi.project_mobile.Mail.RecyclerTouchListener;
import com.example.keiichi.project_mobile.MainActivity;
import com.example.keiichi.project_mobile.R;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.SectionDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import javax.json.Json;
import javax.json.JsonObjectBuilder;

public class ContactsActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener, Serializable {

    private MenuItem searchItem;
    private BottomNavigationView mBottomNav;
    private Toolbar myToolbar;
    private SearchView searchView;
    private ContactAdapter contactAdapter;
    private RoomAdapter roomAdapter;
    private UserAdapter userAdapter;
    private List<Contact> contacts = new ArrayList<>();
    private List<EmailAddress> rooms = new ArrayList<>();
    private List<User> users = new ArrayList<>();
    private List<Contact> contactsFiltered;
    private List<EmailAddress> emailList;
    private String accessToken;
    private String userName;
    private String userEmail;
    private String id;
    private String currentContactFolderId;
    private int navIdentifier;
    private boolean multiSelect = false;
    private boolean actionModeEnabled = false;
    private Contact testContact;
    private ImageView mImageView;
    private Drawer drawer;
    private RecyclerView contactsRecyclerView;
    private int contactsClickedCount = 0;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ActionMode contactActionMode;
    private List<Contact> selectedContacts = new ArrayList<>();
    private ArrayList<Integer> selectedItems = new ArrayList<>();
    private List<ContactFolder> contactFolders = new ArrayList<>();
    List<String> navigationItems = new ArrayList<>();
    final static String MSGRAPH_URL = "https://graph.microsoft.com/v1.0/me/contacts?$orderBy=displayName&$top=500&$count=true";
    final static String MSGRAPH_URL_FOTO = "https://graph.microsoft.com/beta/me/contacts/";
    final static String MSGRAPH_URL_FOTO2 = "/photo/$value";
    final private String URL_DELETE = "https://graph.microsoft.com/beta/me/contacts/";
    final static String URL_CONTACTFOLDERS = "https://graph.microsoft.com/v1.0/me/contactFolders/";
    final static String URL_ROOMS = "https://graph.microsoft.com/beta/me/findRooms?$top=999&$count=true";
    final static String URL_USERS = "https://graph.microsoft.com/v1.0/users?$orderBy=displayName&$top=999&$count=true";
    final static String USER_SEARCH = "https://graph.microsoft.com/v1.0/users?$filter=startswith(givenName,'";

    /* UI & Debugging Variables */
    private static final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);

        myToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);

        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(this);

        contactsRecyclerView = findViewById(R.id.contactsRecyclerView);

        accessToken = getIntent().getStringExtra("AccessToken");
        userName = getIntent().getStringExtra("userName");
        userEmail = getIntent().getStringExtra("userEmail");
        id = getIntent().getStringExtra("id");

        mBottomNav = (BottomNavigationView) findViewById(R.id.navigation);

        Menu menu = mBottomNav.getMenu();
        MenuItem menuItem = menu.getItem(2);
        menuItem.setChecked(true);

        mBottomNav.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                switch (item.getItemId()) {

                    case R.id.action_calendar:
                        Intent intentCalendar = new Intent(ContactsActivity.this, CalendarActivity.class);
                        intentCalendar.putExtra("AccessToken", accessToken);
                        intentCalendar.putExtra("userName", userName);
                        intentCalendar.putExtra("userEmail", userEmail);

                        startActivity(intentCalendar);

                        ContactsActivity.this.finish();

                        break;
                    case R.id.action_mail:
                        Intent intentMail = new Intent(ContactsActivity.this, ListMailsActvity.class);
                        intentMail.putExtra("AccessToken", accessToken);
                        intentMail.putExtra("userName", userName);
                        intentMail.putExtra("userEmail", userEmail);

                        startActivity(intentMail);

                        ContactsActivity.this.finish();

                        break;
                    case R.id.action_user:

                        break;

                }

                return false;
            }
        });

        contactFolders.add(new ContactFolder("", "", ""));
        buildDrawer(userName, userEmail, myToolbar, contactFolders);

        getContacts();

    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.my_action_bar_items_contacts, menu);
        MenuItem addItem = menu.findItem(R.id.action_add);

        searchItem = menu.findItem(R.id.action_search);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {


            case R.id.action_add:
                Intent intentAddContact = new Intent(ContactsActivity.this, AddContactActivity.class);
                intentAddContact.putExtra("AccessToken", accessToken);
                intentAddContact.putExtra("userName", userName);
                intentAddContact.putExtra("userEmail", userEmail);
                intentAddContact.putExtra("currentContactFolderId", currentContactFolderId);

                startActivity(intentAddContact);

                ContactsActivity.this.finish();

                return true;

            case R.id.action_more:

                View menuItemView = findViewById(R.id.action_more);

                Context wrapper = new ContextThemeWrapper(getApplicationContext(), R.style.YOURSTYLE);

                final PopupMenu popupMenu = new PopupMenu(wrapper, menuItemView);

                popupMenu.inflate(R.menu.contact_folder_options);

                popupMenu.show();

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {

                        Menu menu = popupMenu.getMenu();

                        switch(menuItem.getItemId()){
                            case R.id.action_create_folder:

                                showInputDialog();

                                break;

                        }

                        return false;
                    }
                });

                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);
        }
    }

    public interface ClickListener {
        void onClick(View view, int position);

        void onLongClick(View view, int position);
    }

    private ActionMode.Callback actionModeCallback = new ActionMode.Callback() {
        @Override
        public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
            MenuInflater menuInflater = getMenuInflater();
            menuInflater.inflate(R.menu.delete_navigation, menu);
            multiSelect = true;
            actionModeEnabled = true;
            contactActionMode = actionMode;
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
            if(menuItem.getItemId() == R.id.action_delete){
                try {
                    deleteContacts(selectedItems);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                actionModeEnabled = false;
                actionMode.finish();
                return true;
            }
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode actionMode) {
            multiSelect = false;
            actionModeEnabled = false;
            contactsClickedCount = 0;

            for (Integer item : selectedItems) {
                contactsRecyclerView.getChildAt(item).setBackgroundColor(Color.TRANSPARENT);
            }

            selectedContacts.clear();

        }
    };

    /* Use Volley to make an HTTP request to the /me endpoint from MS Graph using an access token */
    private void getContacts() {
        Log.d(TAG, "Starting volley request to graph");
        Log.d(TAG, accessToken);

    /* Make sure we have a token to send to graph */
        if (accessToken == null) {
            Intent logout = new Intent(ContactsActivity.this, MainActivity.class);
            logout.putExtra("AccessToken", accessToken);
            logout.putExtra("userName", userName);
            logout.putExtra("userEmail", userEmail);

            startActivity(logout);

            ContactsActivity.this.finish();
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
                loadContactData();
                loadContactFolderData();
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + accessToken);
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

        // Test de response
        JSONArray contactsJsonArray = null;

        // Haal de contacten binnen
        try {
            contactsJsonArray = (JSONArray) graphResponse.get("value");

            JSONObject contactList = graphResponse;

            JSONArray contactArray = contactList.getJSONArray("value");

            System.out.println("test response: " + contactArray);

            // VUL POJO
            Type listType = new TypeToken<List<Contact>>() {
            }.getType();

            contacts = new Gson().fromJson(String.valueOf(contactArray), listType);

            contactAdapter = new ContactAdapter(this, contacts);
            contactsRecyclerView.setAdapter(contactAdapter);

            getContactFolders();

        } catch (JSONException e) {
            e.printStackTrace();
        }
        assert contactsJsonArray != null;

        RecyclerView.LayoutManager manager = new LinearLayoutManager(getApplicationContext());
        contactsRecyclerView.setLayoutManager(manager);
        contactsRecyclerView.setItemAnimator(new DefaultItemAnimator());
        contactsRecyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));

        contactAdapter = new ContactAdapter(this, contacts);
        contactsRecyclerView.setAdapter(contactAdapter);

        saveContactData();

        contactsRecyclerView.addOnItemTouchListener(new RecyclerTouchListener(getApplicationContext(), contactsRecyclerView, new ListMailsActvity.ClickListener() {
            @Override
            public void onClick(View view, int position) {
                if (actionModeEnabled) {
                    selectedItem(position);

                } else {

                    if (contacts.size() != 0) {

                        Contact contact = contactAdapter.getItemAtPosition(position);

                        Intent showContactDetails = new Intent(ContactsActivity.this, ContactsDetailsActivity.class);

                        showContactDetails.putExtra("userEmail", userEmail);
                        showContactDetails.putExtra("AccessToken", accessToken);
                        showContactDetails.putExtra("userName", userName);
                        showContactDetails.putExtra("contact", contact);
                        showContactDetails.putExtra("contactId", contact.getId());

                        startActivity(showContactDetails);

                        ContactsActivity.this.finish();

                    } else {
                        Toast.makeText(getApplicationContext(), "Empty contact list!", Toast.LENGTH_SHORT).show();
                    }

                }

            }

            @Override
            public void onLongClick(View view, int position) {
                view.startActionMode(actionModeCallback);
                selectedItem(position);
            }
        }));

        searchView = (SearchView) searchItem.getActionView();

        searchView.setQueryHint("Search by name...");

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                contactAdapter.getFilter().filter(s);

                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                contactAdapter.getFilter().filter(s);

                return true;
            }

        });

    }

    public void setFilter(List<Contact> contactFiltered) {

        contactsFiltered = new ArrayList<>();
        contactsFiltered.addAll(contactFiltered);
        contactAdapter.notifyDataSetChanged();
    }

    /* Use Volley to make an HTTP request to the /me endpoint from MS Graph using an access token */
    private void getRooms() {
        Log.d(TAG, "Starting volley request to graph");
        Log.d(TAG, accessToken);

    /* Make sure we have a token to send to graph */
        if (accessToken == null) {
            Intent logout = new Intent(ContactsActivity.this, MainActivity.class);
            logout.putExtra("AccessToken", accessToken);
            logout.putExtra("userName", userName);
            logout.putExtra("userEmail", userEmail);

            startActivity(logout);

            ContactsActivity.this.finish();
        }

        RequestQueue queue = Volley.newRequestQueue(this);
        JSONObject parameters = new JSONObject();

        try {
            parameters.put("key", "value");
        } catch (Exception e) {
            Log.d(TAG, "Failed to put parameters: " + e.toString());
        }
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, URL_ROOMS,
                parameters, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
            /* Successfully called graph, process data and send to UI */
                Log.d(TAG, "Response: " + response.toString());

                try {
                    updateRooms(response);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "Error: " + error.toString());
                loadRoomData();
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + accessToken);
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
    private void updateRooms(JSONObject graphResponse) throws JSONException {

        // Haal de rooms binnen
        try {
            JSONObject roomList = graphResponse;

            JSONArray roomArray = roomList.getJSONArray("value");


            // VUL POJO
            Type listType = new TypeToken<List<EmailAddress>>() {
            }.getType();

            rooms = new Gson().fromJson(String.valueOf(roomArray), listType);

            roomAdapter = new RoomAdapter(this, rooms);
            contactsRecyclerView.setAdapter(roomAdapter);

            saveRoomData();

        } catch (JSONException e) {
            e.printStackTrace();
        }

        contactsRecyclerView.addOnItemTouchListener(new RecyclerTouchListener(getApplicationContext(), contactsRecyclerView, new ListMailsActvity.ClickListener() {
            @Override
            public void onClick(View view, int position) {
                if (actionModeEnabled) {
                    selectedItem(position);

                } else {

                    if (rooms.size() != 0) {

                        EmailAddress room = roomAdapter.getItemAtPosition(position);

                        Intent showRoomDetails = new Intent(ContactsActivity.this, RoomDetailsActivity.class);

                        showRoomDetails.putExtra("userEmail", userEmail);
                        showRoomDetails.putExtra("AccessToken", accessToken);
                        showRoomDetails.putExtra("userName", userName);
                        showRoomDetails.putExtra("room", room);

                        startActivity(showRoomDetails);

                        ContactsActivity.this.finish();

                    } else {
                        Toast.makeText(getApplicationContext(), "Empty room list!", Toast.LENGTH_SHORT).show();
                    }

                }

            }

            @Override
            public void onLongClick(View view, int position) {
                view.startActionMode(actionModeCallback);
                selectedItem(position);
            }
        }));

        searchView = (SearchView) searchItem.getActionView();

        searchView.setQueryHint("Search by name...");

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                roomAdapter.getFilter().filter(s);

                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                roomAdapter.getFilter().filter(s);

                return true;
            }

        });

    }

    /* Use Volley to make an HTTP request to the /me endpoint from MS Graph using an access token */
    private void getUsers() {
        Log.d(TAG, "Starting volley request to graph");
        Log.d(TAG, accessToken);

    /* Make sure we have a token to send to graph */
        if (accessToken == null) {
            Intent logout = new Intent(ContactsActivity.this, MainActivity.class);
            logout.putExtra("AccessToken", accessToken);
            logout.putExtra("userName", userName);
            logout.putExtra("userEmail", userEmail);

            startActivity(logout);

            ContactsActivity.this.finish();
        }

        RequestQueue queue = Volley.newRequestQueue(this);
        JSONObject parameters = new JSONObject();

        try {
            parameters.put("key", "value");
        } catch (Exception e) {
            Log.d(TAG, "Failed to put parameters: " + e.toString());
        }
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, URL_USERS,
                parameters, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
            /* Successfully called graph, process data and send to UI */
                Log.d(TAG, "Response: " + response.toString());

                try {
                    updateUsers(response);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "Error: " + error.toString());
                loadUserData();
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + accessToken);
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
    private void updateUsers(JSONObject graphResponse) throws JSONException {

        // Haal de rooms binnen
        try {
            JSONObject userList = graphResponse;

            JSONArray userArray = userList.getJSONArray("value");

            // VUL POJO
            Type listType = new TypeToken<List<User>>() {
            }.getType();

            users = new Gson().fromJson(String.valueOf(userArray), listType);

            userAdapter = new UserAdapter(this, users);
            contactsRecyclerView.setAdapter(userAdapter);
            userAdapter.notifyDataSetChanged();

            saveUserData();

        } catch (JSONException e) {
            e.printStackTrace();
        }

        contactsRecyclerView.addOnItemTouchListener(new RecyclerTouchListener(getApplicationContext(), contactsRecyclerView, new ListMailsActvity.ClickListener() {
            @Override
            public void onClick(View view, int position) {
                if (actionModeEnabled) {
                    selectedItem(position);

                } else {

                    if (users.size() != 0) {

                        User user = userAdapter.getItemAtPosition(position);

                        Intent showUserDetails = new Intent(ContactsActivity.this, UserDetailsActivity.class);

                        showUserDetails.putExtra("userEmail", userEmail);
                        showUserDetails.putExtra("AccessToken", accessToken);
                        showUserDetails.putExtra("userName", userName);
                        showUserDetails.putExtra("user", user);

                        startActivity(showUserDetails);

                        ContactsActivity.this.finish();

                    } else {
                        Toast.makeText(getApplicationContext(), "Empty user list!", Toast.LENGTH_SHORT).show();
                    }

                }

            }

            @Override
            public void onLongClick(View view, int position) {
                view.startActionMode(actionModeCallback);
                selectedItem(position);
            }
        }));

        searchView = (SearchView) searchItem.getActionView();

        searchView.setQueryHint("Search by name...");

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                searchUser(s);

                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                userAdapter.getFilter().filter(s);

                return true;
            }

        });

    }

    @Override
    public void onRefresh() {

        swipeRefreshLayout.setRefreshing(true);

        if(navIdentifier == 4){

            getContactsFromFolder(currentContactFolderId);

        }else if(navIdentifier == 1){

            getContacts();

        } else if(navIdentifier == 2){

            getUsers();

        } else if(navIdentifier == 3){

            rooms.add(new EmailAddress("", ""));

            getRooms();

        }

        swipeRefreshLayout.setRefreshing(false);

    }

    // PATCH REQUEST VOOR DELETEN CONTACTPERSOON
    private void deleteContacts(ArrayList<Integer> selectedItems) throws JSONException {

        this.selectedItems = selectedItems;

        for (Integer integer : selectedItems) {
            RequestQueue queue = Volley.newRequestQueue(this);

            Contact contact = contacts.get(integer);

            String postAddress = URL_DELETE + contact.getId();

            StringRequest stringRequest = new StringRequest(Request.Method.DELETE, postAddress,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            Toast.makeText(getApplicationContext(), "Contacts deleted!", Toast.LENGTH_SHORT).show();
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
                    headers.put("Content-Type", "application/json; charset=utf-8");

                    return headers;
                }

            };

            queue.add(stringRequest);

        }

        contactAdapter.notifyDataSetChanged();

        int DELAY_TIME=2000;

        //start your animation
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                //this code will run after the delay time which is 2 seconds.

                getContacts();
            }
        }, DELAY_TIME);
    }

    private void selectedItem(Integer item) {
        if (multiSelect) {
            if (selectedItems.contains(item)) {
                selectedItems.remove(item);
                contactsRecyclerView.getChildAt(item).setBackgroundColor(Color.TRANSPARENT);
                contactsClickedCount--;
                contactActionMode.setTitle(contactsClickedCount+ " Selected");

                if(contactsClickedCount == 0){
                    contactActionMode.finish();
                    actionModeEnabled = false;
                }
            } else {
                selectedItems.add(item);
                contactsRecyclerView.getChildAt(item).setBackgroundColor(Color.LTGRAY);
                contactsClickedCount++;
                contactActionMode.setTitle(contactsClickedCount+ " Selected");
            }
        }
    }

    /* Use Volley to make an HTTP request to the /me endpoint from MS Graph using an access token */
    private void getContactFolders() {
        Log.d(TAG, "Starting volley request to graph");
        Log.d(TAG, accessToken);

    /* Make sure we have a token to send to graph */
        if (accessToken == null) {
            Intent logout = new Intent(ContactsActivity.this, MainActivity.class);
            logout.putExtra("AccessToken", accessToken);
            logout.putExtra("userName", userName);
            logout.putExtra("userEmail", userEmail);

            startActivity(logout);

            ContactsActivity.this.finish();
        }

        RequestQueue queue = Volley.newRequestQueue(this);
        JSONObject parameters = new JSONObject();

        try {
            parameters.put("key", "value");
        } catch (Exception e) {
            Log.d(TAG, "Failed to put parameters: " + e.toString());
        }
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, URL_CONTACTFOLDERS,
                parameters, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
            /* Successfully called graph, process data and send to UI */
                Log.d(TAG, "Response: " + response.toString());

                try {
                    updateFolders(response);
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
                headers.put("Authorization", "Bearer " + accessToken);
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
    private void updateFolders(JSONObject graphResponse) throws JSONException {

        // Test de response
        JSONArray contactsJsonArray = null;

        // Haal de contactFolders binnen
        try {
            JSONObject contactList = graphResponse;

            JSONArray folders = contactList.getJSONArray("value");

            // VUL POJO
            Type listType = new TypeToken<List<ContactFolder>>() {
            }.getType();

            contactFolders = new Gson().fromJson(String.valueOf(folders), listType);

            saveContactFolderData();

            buildDrawer(userName, userEmail, myToolbar, contactFolders);

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public void getContactsFromFolder(String id){

        Log.d(TAG, "Starting volley request to graph");
        Log.d(TAG, accessToken);

    /* Make sure we have a token to send to graph */
        if (accessToken == null) {
            Intent logout = new Intent(ContactsActivity.this, MainActivity.class);
            logout.putExtra("AccessToken", accessToken);
            logout.putExtra("userName", userName);
            logout.putExtra("userEmail", userEmail);

            startActivity(logout);

            ContactsActivity.this.finish();
        }

        RequestQueue queue = Volley.newRequestQueue(this);
        JSONObject parameters = new JSONObject();

        try {
            parameters.put("key", "value");
        } catch (Exception e) {
            Log.d(TAG, "Failed to put parameters: " + e.toString());
        }

        String getUrl = URL_CONTACTFOLDERS + id + "/contacts";

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, getUrl,
                parameters, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
            /* Successfully called graph, process data and send to UI */
                Log.d(TAG, "Response: " + response.toString());

                try {
                    updateWithContactFolder(response);

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
                headers.put("Authorization", "Bearer " + accessToken);
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

    private void updateWithContactFolder(JSONObject graphResponse) throws JSONException {

        try {
            //haal mails binnen

            JSONObject contactList = graphResponse;

            JSONArray contactArray = contactList.getJSONArray("value");

            System.out.println("test response: " + contactArray);


            // VUL POJO
            Type listType = new TypeToken<List<Contact>>() {
            }.getType();

            contacts = new Gson().fromJson(String.valueOf(contactArray), listType);

            contactAdapter = new ContactAdapter(this, contacts);
            contactsRecyclerView.setAdapter(contactAdapter);

            contactAdapter.notifyDataSetChanged();

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private void setActionBarTitle(String title, Toolbar toolbar) {

        toolbar.setTitle(title);
        toolbar.setTitleTextColor(ContextCompat.getColor(this, R.color.white));
        toolbar.setSubtitleTextColor(ContextCompat.getColor(this, R.color.white));
        // THIS LINE REMOVES ANNOYING LEFT MARGIN
        toolbar.setTitleMarginStart(30);

    }

    /* Use Volley to make an HTTP request to the /me endpoint from MS Graph using an access token */
    private void searchUser(String name) {
        Log.d(TAG, "Starting volley request to graph");
        Log.d(TAG, accessToken);

    /* Make sure we have a token to send to graph */
        if (accessToken == null) {
            Intent logout = new Intent(ContactsActivity.this, MainActivity.class);
            logout.putExtra("AccessToken", accessToken);
            logout.putExtra("userName", userName);
            logout.putExtra("userEmail", userEmail);

            startActivity(logout);

            ContactsActivity.this.finish();
        }

        RequestQueue queue = Volley.newRequestQueue(this);
        JSONObject parameters = new JSONObject();

        try {
            parameters.put("key", "value");
        } catch (Exception e) {
            Log.d(TAG, "Failed to put parameters: " + e.toString());
        }

        String searchUrl = USER_SEARCH + name + "')";

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, searchUrl,
                parameters, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
            /* Successfully called graph, process data and send to UI */
                Log.d(TAG, "Response: " + response.toString());

                try {
                    updateUsers(response);
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
                headers.put("Authorization", "Bearer " + accessToken);
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

    // POST REQUEST VOOR NIEWE CONTACTSPERSOON
    private void createContactFolder(String name) throws JSONException {
        RequestQueue queue = Volley.newRequestQueue(this);

        final JSONObject jsonObject = new JSONObject(builJsonName(name));

        JsonObjectRequest objectRequest = new JsonObjectRequest(Request.Method.POST, URL_CONTACTFOLDERS, jsonObject,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Toast.makeText(getApplicationContext(), "Contact folder created!", Toast.LENGTH_SHORT).show();
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

        int DELAY_TIME=2000;

        //start your animation
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                //this code will run after the delay time which is 2 seconds.

                getContactFolders();

            }
        }, DELAY_TIME);

    }

    private String builJsonName(String name) {
        JsonObjectBuilder factory = Json.createObjectBuilder()

                .add("displayName", name);

        return factory.build().toString();
    }

    protected void showInputDialog() {

        // get prompts.xml view
        LayoutInflater layoutInflater = LayoutInflater.from(ContactsActivity.this);
        View promptView = layoutInflater.inflate(R.layout.input_dialog, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(ContactsActivity.this);
        alertDialogBuilder.setView(promptView);

        final EditText editText = (EditText) promptView.findViewById(R.id.edittext);
        // setup a dialog window
        alertDialogBuilder.setCancelable(false)
                .setPositiveButton("Create", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        try {
                            createContactFolder(editText.getText().toString());
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

        // create an alert dialog
        final AlertDialog alert = alertDialogBuilder.create();

        alert.setOnShowListener( new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface arg0) {
                alert.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.BLACK);
                alert.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.BLACK);
            }
        });

        alert.show();
        alert.getWindow().getDecorView().getBackground().setColorFilter(new LightingColorFilter(0xFF000000, Color.WHITE));

    }

    public void buildDrawer(String name, String email, Toolbar toolbar, List<ContactFolder> contactFolders){

        ArrayList<IDrawerItem> drawerItems = new ArrayList<>();

        SectionDrawerItem sectionDrawerItem = new SectionDrawerItem().withName("Contact Folders").withDivider(false);

        drawerItems.add(sectionDrawerItem);

        int identifier = 4;

        for(ContactFolder contactFolder : contactFolders) {
            PrimaryDrawerItem item = new PrimaryDrawerItem();

            item.withName(contactFolder.getDisplayName());
            item.withTag(contactFolder);
            item.withTextColor(Color.BLACK);
            item.withIdentifier(identifier);
            identifier++;
            drawerItems.add(item);
        }

        ColorGenerator generator = ColorGenerator.MATERIAL;

        int color2 = generator.getColor(userName.substring(0,1));

        final TextDrawable drawable = TextDrawable.builder()
                .buildRound(userName.substring(0,1), color2); // radius in px

        AccountHeader headerResult = new AccountHeaderBuilder()
                .withActivity(this)
                .withHeaderBackground(R.color.colorWhite)
                .withSelectionListEnabledForSingleProfile(false)
                .withTextColor(Color.BLACK)
                .addProfiles(
                        new ProfileDrawerItem().withName(name).withEmail(email).withIcon(drawable)
                )
                .withOnAccountHeaderListener(new AccountHeader.OnAccountHeaderListener() {
                    @Override
                    public boolean onProfileChanged(View view, IProfile profile, boolean currentProfile) {
                        return false;
                    }
                })
                .withDividerBelowHeader(true)
                .build();

        drawer = new DrawerBuilder()
                .withActivity(this)
                .withToolbar(toolbar)
                .withTranslucentStatusBar(false)
                .withAccountHeader(headerResult)
                .withDrawerItems(drawerItems)
                .addDrawerItems(
                        new SectionDrawerItem().withName("Directories"),
                        new PrimaryDrawerItem().withName("Contacts").withIdentifier(1),
                        new PrimaryDrawerItem().withName("All Users").withIdentifier(2),
                        new PrimaryDrawerItem().withName("All Rooms").withIdentifier(3)

                )
                .withSelectedItem(1)
                .addStickyDrawerItems(
                        new SecondaryDrawerItem().withName("Log out").withIdentifier(999)
                )
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        if (drawerItem instanceof PrimaryDrawerItem){

                            if(drawerItem.getIdentifier() > 3){

                                navIdentifier = 4;

                                ContactFolder contactFolder = (ContactFolder) drawerItem.getTag();

                                currentContactFolderId = contactFolder.getId();

                                System.out.println("id xd: " + currentContactFolderId);

                                getContactsFromFolder(currentContactFolderId);

                                String folderName = ((PrimaryDrawerItem) drawerItem).getName().getText().toString();

                                setActionBarTitle(folderName, myToolbar);

                            } else if(drawerItem.getIdentifier() == 1){
                                navIdentifier = 1;
                                getContacts();
                                setActionBarTitle("My Contacts", myToolbar);

                            } else if(drawerItem.getIdentifier() == 2){
                                navIdentifier = 2;
                                getUsers();
                                setActionBarTitle("All Users", myToolbar);

                            } else if(drawerItem.getIdentifier() == 3){
                                navIdentifier = 3;
                                rooms.add(new EmailAddress("", ""));
                                getRooms();
                                setActionBarTitle("All Rooms", myToolbar);

                            }

                        } else if(drawerItem.getIdentifier() == 999){
                            //onSignOutClicked();
                            Intent logout = new Intent(ContactsActivity.this, MainActivity.class);
                            logout.putExtra("AccessToken", accessToken);
                            logout.putExtra("userName", userName);
                            logout.putExtra("userEmail", userEmail);

                            startActivity(logout);

                            ContactsActivity.this.finish();

                            Toast.makeText(getBaseContext(), "Signed Out!", Toast.LENGTH_SHORT).show();
                        }
                        return false;
                    }
                })
                .build();

    }

    private void saveContactData(){
        SharedPreferences sharedPreferences = getSharedPreferences("shared preferences contacts", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(contacts);

        editor.putString("contact list", json);
        editor.apply();

    }

    private void loadContactData(){
        SharedPreferences sharedPreferences = getSharedPreferences("shared preferences contacts", MODE_PRIVATE);

        if(sharedPreferences.contains("contact list")) {
            Gson gson = new Gson();
            String json = sharedPreferences.getString("contact list", null);
            Type type = new TypeToken<ArrayList<Contact>>() {}.getType();
            contacts = gson.fromJson(json, type);

            RecyclerView.LayoutManager manager = new LinearLayoutManager(getApplicationContext());
            contactsRecyclerView.setLayoutManager(manager);
            contactsRecyclerView.setItemAnimator(new DefaultItemAnimator());
            contactsRecyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));

            contactAdapter = new ContactAdapter(this, contacts);
            contactsRecyclerView.setAdapter(contactAdapter);

            searchView = (SearchView) searchItem.getActionView();

            searchView.setQueryHint("Search: Inbox...");

            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String s) {
                    contactAdapter.getFilter().filter(s);

                    return true;
                }

                @Override
                public boolean onQueryTextChange(String s) {
                    contactAdapter.getFilter().filter(s);

                    return true;
                }

            });

            contactsRecyclerView.addOnItemTouchListener(new RecyclerTouchListener(getApplicationContext(), contactsRecyclerView, new ListMailsActvity.ClickListener() {
                @Override
                public void onClick(View view, int position) {
                    if (actionModeEnabled) {
                        selectedItem(position);

                    } else {

                        if (contacts.size() != 0) {

                            Contact contact = contactAdapter.getItemAtPosition(position);

                            Intent showContactDetails = new Intent(ContactsActivity.this, ContactsDetailsActivity.class);

                            showContactDetails.putExtra("userEmail", userEmail);
                            showContactDetails.putExtra("AccessToken", accessToken);
                            showContactDetails.putExtra("userName", userName);
                            showContactDetails.putExtra("contact", contact);
                            showContactDetails.putExtra("contactId", contact.getId());

                            startActivity(showContactDetails);

                            ContactsActivity.this.finish();

                        } else {
                            Toast.makeText(getApplicationContext(), "Empty contact list!", Toast.LENGTH_SHORT).show();
                        }

                    }

                }

                @Override
                public void onLongClick(View view, int position) {
                    view.startActionMode(actionModeCallback);
                    selectedItem(position);
                }
            }));

        }

        if(contacts == null){
            contacts = new ArrayList<>();
        }

    }

    private void saveUserData(){
        SharedPreferences sharedPreferences = getSharedPreferences("shared preferences users", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(users);

        editor.putString("user list", json);
        editor.apply();

    }

    private void loadUserData(){
        SharedPreferences sharedPreferences = getSharedPreferences("shared preferences users", MODE_PRIVATE);

        if(sharedPreferences.contains("user list")){
            Gson gson = new Gson();
            String json = sharedPreferences.getString("user list", null);
            Type type = new TypeToken<ArrayList<User>>() {}.getType();
            users = gson.fromJson(json, type);

            RecyclerView.LayoutManager manager = new LinearLayoutManager(getApplicationContext());
            contactsRecyclerView.setLayoutManager(manager);
            contactsRecyclerView.setItemAnimator(new DefaultItemAnimator());
            contactsRecyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));

            userAdapter = new UserAdapter(this, users);
            contactsRecyclerView.setAdapter(userAdapter);
            userAdapter.notifyDataSetChanged();

            searchView = (SearchView) searchItem.getActionView();

            searchView.setQueryHint("Search by name...");

            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String s) {
                    searchUser(s);

                    return true;
                }

                @Override
                public boolean onQueryTextChange(String s) {
                    userAdapter.getFilter().filter(s);

                    return true;
                }

            });

            contactsRecyclerView.addOnItemTouchListener(new RecyclerTouchListener(getApplicationContext(), contactsRecyclerView, new ListMailsActvity.ClickListener() {
                @Override
                public void onClick(View view, int position) {
                    if (actionModeEnabled) {
                        selectedItem(position);

                    } else {

                        if (users.size() != 0) {

                            User user = userAdapter.getItemAtPosition(position);

                            Intent showUserDetails = new Intent(ContactsActivity.this, UserDetailsActivity.class);

                            showUserDetails.putExtra("userEmail", userEmail);
                            showUserDetails.putExtra("AccessToken", accessToken);
                            showUserDetails.putExtra("userName", userName);
                            showUserDetails.putExtra("user", user);

                            startActivity(showUserDetails);

                            ContactsActivity.this.finish();

                        } else {
                            Toast.makeText(getApplicationContext(), "Empty user list!", Toast.LENGTH_SHORT).show();
                        }

                    }

                }

                @Override
                public void onLongClick(View view, int position) {
                    view.startActionMode(actionModeCallback);
                    selectedItem(position);
                }
            }));

        }

        if(users == null){
            users    = new ArrayList<>();
        }

    }

    private void saveRoomData(){
        SharedPreferences sharedPreferences = getSharedPreferences("shared preferences rooms", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(rooms);

        editor.putString("room list", json);
        editor.apply();

    }

    private void loadRoomData(){

        SharedPreferences sharedPreferences = getSharedPreferences("shared preferences rooms", MODE_PRIVATE);

        if(sharedPreferences.contains("room list")){
            Gson gson = new Gson();
            String json = sharedPreferences.getString("room list", null);
            Type type = new TypeToken<ArrayList<EmailAddress>>() {}.getType();
            rooms = gson.fromJson(json, type);

            roomAdapter = new RoomAdapter(this, rooms);
            contactsRecyclerView.setAdapter(roomAdapter);
            roomAdapter.notifyDataSetChanged();

            searchView = (SearchView) searchItem.getActionView();

            searchView.setQueryHint("Search by name...");

            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String s) {
                    roomAdapter.getFilter().filter(s);

                    return true;
                }

                @Override
                public boolean onQueryTextChange(String s) {
                    roomAdapter.getFilter().filter(s);

                    return true;
                }

            });

            contactsRecyclerView.addOnItemTouchListener(new RecyclerTouchListener(getApplicationContext(), contactsRecyclerView, new ListMailsActvity.ClickListener() {
                @Override
                public void onClick(View view, int position) {
                    if (actionModeEnabled) {
                        selectedItem(position);

                    } else {

                        if (rooms.size() != 0) {

                            EmailAddress room = roomAdapter.getItemAtPosition(position);

                            Intent showRoomDetails = new Intent(ContactsActivity.this, RoomDetailsActivity.class);

                            showRoomDetails.putExtra("userEmail", userEmail);
                            showRoomDetails.putExtra("AccessToken", accessToken);
                            showRoomDetails.putExtra("userName", userName);
                            showRoomDetails.putExtra("room", room);

                            startActivity(showRoomDetails);

                            ContactsActivity.this.finish();

                        } else {
                            Toast.makeText(getApplicationContext(), "Empty room list!", Toast.LENGTH_SHORT).show();
                        }

                    }

                }

                @Override
                public void onLongClick(View view, int position) {
                    view.startActionMode(actionModeCallback);
                    selectedItem(position);
                }
            }));

        }

        if(rooms == null){
            rooms    = new ArrayList<>();
        }

    }

    private void saveContactFolderData(){
        SharedPreferences sharedPreferences = getSharedPreferences("shared preferences contactfolders", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(contactFolders);

        editor.putString("contactfolder list", json);
        editor.apply();

    }

    private void loadContactFolderData(){

        SharedPreferences sharedPreferences = getSharedPreferences("shared preferences contactfolders", MODE_PRIVATE);

        if(sharedPreferences.contains("contactfolder list")){
            Gson gson = new Gson();
            String json = sharedPreferences.getString("contactfolder list", null);
            Type type = new TypeToken<ArrayList<ContactFolder>>() {}.getType();
            contactFolders = gson.fromJson(json, type);

            buildDrawer(userName, userEmail, myToolbar, contactFolders);

        }

        if(contactFolders == null){
            contactFolders    = new ArrayList<>();
        }

    }

    @Override
    public void onBackPressed(){
        minimizeApp();
    }

    public void minimizeApp() {
        Intent startMain = new Intent(Intent.ACTION_MAIN);
        startMain.addCategory(Intent.CATEGORY_HOME);
        startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(startMain);
    }

}
