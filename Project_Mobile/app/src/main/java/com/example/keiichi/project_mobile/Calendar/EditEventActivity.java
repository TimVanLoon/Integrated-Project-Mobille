package com.example.keiichi.project_mobile.Calendar;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

import com.example.keiichi.project_mobile.R;

import org.json.JSONException;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class EditEventActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener{

    final private String URL_POSTADRESS = "https://graph.microsoft.com/v1.0/me/events/";

    private String [] DURATIONSPINNERLIST = {"0 Minutes", "15 Minutes", "30 Minutes", "45 Minutes", "1 Hour", "90 Minutes", " 2 Hours", "Entire day"};
    private String [] REMINDERSPINNERLIST = {"0 Minutes", "15 Minutes", "30 Minutes", "45 Minutes", "1 Hour", "90 Minutes", " 2 Hours", "3 Hours", "4 Hours", "8 Hours", "12 Hours",
            "1 Day", "2 Days", "3 Days", "1 Week", "2 Weeks"};
    private String [] DISPLAYASSPINNERLIST = {"Free", "Working elsewhere", "Tentative", "Busy", "Away"};
    private String [] REPEATSPINNERLIST = {"Never", "Each day", "Every sunday", "Every workday", "Day 31 of every month", "Ever last sunday", "Every 31st of december"};

    private Toolbar myToolbar;
    private Calendar start;
    private DatePickerDialog datePickerDialog;
    private EditText dateEvent;
    private EditText timeEvent;
    private EditText eventInput;
    private EditText locationInput;
    private EditText personalNotes;
    private TextView reminderTitle;
    private TextView displayAsTitle;
    private TextView repeatTitle;
    private TextView notesTitle;
    private Spinner durationSpinner;
    private Spinner reminderSpinner;
    private Spinner displayAsSpinner;
    private Spinner repeatSpinner;
    private boolean isCurrentDate;
    private boolean isCurrentTime;
    private String userName;
    private String userEmail;
    private String accessToken;
    private String id;
    private String showAs;
    private String subject;
    private String location;
    private String startDate;
    private String displayAs;
    private String notes;
    private String finalHourOfDay;
    private String finalMinuteOfHour;
    private int dayOfMonth;
    private int month;
    private int year;
    private int hourOfDay;
    private int minuteOfHour;
    private int duration;
    private int startingValueReminder;
    private int startingValueDisplayAs;
    private int startingValueDuration;
    private int startingValueRepeat;
    private int reminderMinutesBeforeStart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_event);

        accessToken = getIntent().getStringExtra("AccessToken");
        userName = getIntent().getStringExtra("userName");
        userEmail = getIntent().getStringExtra("userEmail");
        id= getIntent().getStringExtra("id");
        subject = getIntent().getStringExtra("subject");
        location = getIntent().getStringExtra("location");
        startDate = getIntent().getStringExtra("startDate");
        displayAs = getIntent().getStringExtra("displayAs");
        notes = getIntent().getStringExtra("notes");
        reminderMinutesBeforeStart = getIntent().getIntExtra("reminderMinutesBeforeStart", 0);

        // INITIALISEER DE INPUT FIELDS
        dateEvent = (EditText) findViewById(R.id.dateEvent);
        timeEvent = (EditText) findViewById(R.id.timeEvent);
        eventInput = (EditText) findViewById(R.id.eventInput);
        locationInput = (EditText) findViewById(R.id.locationInput);
        personalNotes = (EditText) findViewById(R.id.personalNotes);
        reminderTitle = (TextView) findViewById(R.id.reminderTitle);
        displayAsTitle = (TextView) findViewById(R.id.displayAsTitle);
        repeatTitle = (TextView) findViewById(R.id.repeatTitle);
        notesTitle = (TextView) findViewById(R.id.notesTitle);
        durationSpinner = (Spinner) findViewById(R.id.durationSpinner);
        reminderSpinner = (Spinner) findViewById(R.id.reminderSpinner);
        repeatSpinner = (Spinner) findViewById(R.id.repeatSpinner);
        displayAsSpinner = (Spinner) findViewById(R.id.displayAsSpinner);

        // VUL INPUTS MET DATA VAN CONTACT
        eventInput.setText(subject);
        locationInput.setText(location);
        personalNotes.setText(Html.fromHtml(notes));

        start = Calendar.getInstance();

        System.out.println("test date : " + startDate);

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
            Date date = sdf.parse(startDate);
            start.setTime(date);

            dayOfMonth = start.get(Calendar.DAY_OF_MONTH);
            month = start.get(Calendar.MONTH) + 1;
            year = start.get(Calendar.YEAR);

            minuteOfHour = start.get(Calendar.MINUTE);
            hourOfDay = start.get(Calendar.HOUR_OF_DAY);


        } catch (java.text.ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        if(hourOfDay <10) {
            finalHourOfDay = "0" + hourOfDay;
        } else {
            finalHourOfDay = String.valueOf(hourOfDay);
        }

        if(minuteOfHour<10){
            finalMinuteOfHour = "0"+ minuteOfHour;
        }  else {
            finalMinuteOfHour = String.valueOf(minuteOfHour);
        }

        dateEvent.setFocusable(false);
        dateEvent.setClickable(true);
        timeEvent.setFocusable(false);
        timeEvent.setClickable(true);
        dateEvent.setText(dayOfMonth + "-" + month + "-" + year);
        timeEvent.setText(finalHourOfDay + ":" + finalMinuteOfHour);

        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<String> adapterDuration = new ArrayAdapter<String>(this, R.layout.spinner_layout, DURATIONSPINNERLIST);
        // Specify the layout to use when the list of choices appears
        adapterDuration.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        durationSpinner.setAdapter(adapterDuration);
        //startingValue = adapterDuration.getPosition("1 Hour");
        //durationSpinner.setSelection(startingValue);

        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<String> adapterReminder = new ArrayAdapter<String>(this, R.layout.spinner_layout, REMINDERSPINNERLIST);
        // Specify the layout to use when the list of choices appears
        adapterReminder.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        reminderSpinner.setAdapter(adapterReminder);
        //startingValue = adapterReminder.getPosition("15 Minutes");
        //reminderSpinner.setSelection(startingValue);

        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<String> adapterDisplayAs = new ArrayAdapter<String>(this,R.layout.spinner_layout, DISPLAYASSPINNERLIST);
        // Specify the layout to use when the list of choices appears
        adapterDisplayAs.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        displayAsSpinner.setAdapter(adapterDisplayAs);
        //startingValue = adapterDisplayAs.getPosition("Busy");
        //displayAsSpinner.setSelection(startingValue);

        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<String> adapterRepeat = new ArrayAdapter<String>(this, R.layout.spinner_layout, REPEATSPINNERLIST);
        // Specify the layout to use when the list of choices appears
        adapterDisplayAs.setDropDownViewResource(android.R.layout.simple_expandable_list_item_1);
        // Apply the adapter to the spinner
        repeatSpinner.setAdapter(adapterRepeat);
        //startingValue = adapterRepeat.getPosition("Never");
        //repeatSpinner.setSelection(startingValue);

        switch(reminderMinutesBeforeStart){
            case 0:
                startingValueReminder = adapterReminder.getPosition("0 Minutes");
                reminderSpinner.setSelection(startingValueReminder);
                break;

            case 15:
                startingValueReminder = adapterReminder.getPosition("15 Minutes");
                reminderSpinner.setSelection(startingValueReminder);
                break;

            case 30:
                startingValueReminder = adapterReminder.getPosition("30 Minutes");
                reminderSpinner.setSelection(startingValueReminder);
                break;

            case 45:
                startingValueReminder = adapterReminder.getPosition("45 Minutes");
                reminderSpinner.setSelection(startingValueReminder);
                break;

            case 60:
                startingValueReminder = adapterReminder.getPosition("1 Hour");
                reminderSpinner.setSelection(startingValueReminder);
                break;

            case 90:
                startingValueReminder = adapterReminder.getPosition("90 Minutes");
                reminderSpinner.setSelection(startingValueReminder);
                break;

            case 120:
                startingValueReminder = adapterReminder.getPosition("2 Hours");
                reminderSpinner.setSelection(startingValueReminder);
                break;

            case 180:
                startingValueReminder = adapterReminder.getPosition("3 Hours");
                reminderSpinner.setSelection(startingValueReminder);
                break;

            case 240:
                startingValueReminder = adapterReminder.getPosition("4 Hours");
                reminderSpinner.setSelection(startingValueReminder);
                break;

            case 480:
                startingValueReminder = adapterReminder.getPosition("8 Hours");
                reminderSpinner.setSelection(startingValueReminder);
                break;

            case 720:
                startingValueReminder = adapterReminder.getPosition("12 Hours");
                reminderSpinner.setSelection(startingValueReminder);
                break;

            case 1440:
                startingValueReminder = adapterReminder.getPosition("1 Day");
                reminderSpinner.setSelection(startingValueReminder);
                break;

            case 2880:
                startingValueReminder = adapterReminder.getPosition("2 Days");
                reminderSpinner.setSelection(startingValueReminder);
                break;

            case 4320:
                startingValueReminder = adapterReminder.getPosition("3 Days");
                reminderSpinner.setSelection(startingValueReminder);
                break;

            case 10080:
                startingValueReminder = adapterReminder.getPosition("1 Week");
                reminderSpinner.setSelection(startingValueReminder);
                break;

            case 20160:
                startingValueReminder = adapterReminder.getPosition("2 Weeks");
                reminderSpinner.setSelection(startingValueReminder);
                break;
        }

        switch(displayAs){
            case "free":
                startingValueDisplayAs = adapterDisplayAs.getPosition("Free");
                displayAsSpinner.setSelection(startingValueDisplayAs);
                break;

            case "workingElsewhere":
                startingValueDisplayAs = adapterDisplayAs.getPosition("Working elsewhere");
                displayAsSpinner.setSelection(startingValueDisplayAs);
                break;

            case "tentative":
                startingValueDisplayAs = adapterDisplayAs.getPosition("Tentative");
                displayAsSpinner.setSelection(startingValueDisplayAs);
                break;

            case "busy":
                startingValueDisplayAs = adapterDisplayAs.getPosition("Busy");
                displayAsSpinner.setSelection(startingValueDisplayAs);
                break;

            case "oof":
                startingValueDisplayAs = adapterDisplayAs.getPosition("Away");
                displayAsSpinner.setSelection(startingValueDisplayAs);
                break;

        }

        // ZET CLICK EVENT OP DE DATE INPUT
        dateEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // calender class's instance and get current date , month and year from calender
                final Calendar c = Calendar.getInstance();
                int mYear = c.get(Calendar.YEAR); // current year
                int mMonth = c.get(Calendar.MONTH); // current month
                int mDay = c.get(Calendar.DAY_OF_MONTH); // current day
                // date picker dialog
                datePickerDialog = new DatePickerDialog(EditEventActivity.this,
                        new DatePickerDialog.OnDateSetListener() {

                            @Override
                            public void onDateSet(DatePicker view, int yearPicked,
                                                  int monthOfYearPicked, int dayOfMonthPicked) {

                                isCurrentDate = false;

                                setStartDate(year, monthOfYearPicked++, dayOfMonthPicked);

                                dayOfMonth = dayOfMonthPicked;
                                month = monthOfYearPicked;
                                year = yearPicked;

                                dateEvent.setText(dayOfMonth + "-" + month + "-" + year);

                            }
                        }, mYear, mMonth, mDay);
                datePickerDialog.show();

            }
        });

        timeEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar mcurrentTime = Calendar.getInstance();
                int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
                int minute = mcurrentTime.get(Calendar.MINUTE);
                TimePickerDialog mTimePicker;
                mTimePicker = new TimePickerDialog(EditEventActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {

                        isCurrentTime = false;

                        minuteOfHour = selectedMinute;
                        hourOfDay = selectedHour;

                        timeEvent.setText(String.format("%02d:%02d", selectedHour, selectedMinute));
                    }
                }, hour, minute, true);//Yes 24 hour time
                mTimePicker.setTitle("Select Time");
                mTimePicker.show();

            }
        });


        // INITIALISEER ACTION BAR
        myToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);

        // VOEG BACK BUTTONN TOE AAN ACTION BAR
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    // VOEG ICONS TOE AAN DE ACTION BAR
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.add_navigation, menu);


        return super.onCreateOptionsMenu(menu);
    }

    // METHODE VOOR DE CLICKABLE ICOONTJES IN DE ACTION BAR
    @Override
    public boolean onOptionsItemSelected(MenuItem item){

        switch(item.getItemId()){

            // WANNEER BACK BUTTON WORDT AANGEKLIKT (<-)
            case android.R.id.home:
                Intent intentCalendar = new Intent(EditEventActivity.this, EventDetailsActivity.class);
                intentCalendar.putExtra("AccessToken", accessToken);
                intentCalendar.putExtra("userName", userName);
                intentCalendar.putExtra("userEmail", userEmail);
                intentCalendar.putExtra("subject", subject);
                intentCalendar.putExtra("location", location);
                intentCalendar.putExtra("startDate", startDate);
                intentCalendar.putExtra("displayAs", displayAs);
                intentCalendar.putExtra("notes", notes);
                intentCalendar.putExtra("reminderMinutesBeforeStart", reminderMinutesBeforeStart);

                startActivity(intentCalendar);

                return true;

                /*
            // WANNEER SAVE ICON WORDT AANGEKLIKT
            case R.id.action_save:
                try {
                    //saveEvent();

                    int DELAY_TIME=2000;

                    //start your animation
                    new Timer().schedule(new TimerTask() {
                        @Override
                        public void run() {
                            //this code will run after the delay time which is 2 seconds.
                            Intent intentCalendar = new Intent(EditEventActivity.this, EventDetailsActivity.class);
                            intentCalendar.putExtra("AccessToken", accessToken);
                            intentCalendar.putExtra("userName", userName);
                            intentCalendar.putExtra("userEmail", userEmail);
                            startActivity(intentCalendar);
                        }
                    }, DELAY_TIME);



                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return true;
                */

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);
        }
    }

    private void setStartDate(int yearPicked, int monthPicked, int dayOfMonthPicked) {
        if (isCurrentDate) {
            //startDate = LocalDateTime.of(yearPicked, monthPicked, dayOfMonthPicked, startDate.getHour(), startDate.getMinute());
        } else {
            // endTime =
        }


    }

    private String getDayInString(int day) {
        switch (day) {
            case 0:
                return "Monday";

            case 1:
                return "Tuesday";


            case 2:
                return "Wednesday";


            case 3:
                return "Thursday";

            case 4:
                return "Friday";


            case 5:
                return "Saturday";

            case 6:
                return "Sunday";


            default:
                return "";


        }
    }

    public void onItemSelected(AdapterView<?> parent, View view,
                               int pos, long id) {
        // An item was selected. You can retrieve the selected item using
        parent.getItemAtPosition(pos);

        if(parent == durationSpinner){
            switch(pos){
                case 0:
                    duration = 0;
                    break;

                case 1:
                    duration = 15;
                    break;

                case 2:
                    duration = 30;
                    break;

                case 3:
                    duration = 45;
                    break;

                case 4:
                    duration = 60;
                    break;

                case 5:
                    duration = 90;
                    break;

                case 6:
                    duration = 120;
                    break;

                case 7:
                    duration = 1440 ;
                    break;

            }
        }

        if (parent == reminderSpinner){
            switch(pos){
                case 0:
                    reminderMinutesBeforeStart = 0;
                    break;

                case 1:
                    reminderMinutesBeforeStart = 15;
                    break;

                case 2:
                    reminderMinutesBeforeStart = 30;
                    break;

                case 3:
                    reminderMinutesBeforeStart = 45;
                    break;

                case 4:
                    reminderMinutesBeforeStart = 60;
                    break;

                case 5:
                    reminderMinutesBeforeStart = 90;
                    break;

                case 6:
                    reminderMinutesBeforeStart = 120;
                    break;

                case 7:
                    reminderMinutesBeforeStart = 180 ;
                    break;

                case 8:
                    reminderMinutesBeforeStart = 240 ;
                    break;

                case 9:
                    reminderMinutesBeforeStart = 480 ;
                    break;

                case 10:
                    reminderMinutesBeforeStart = 720 ;
                    break;

                case 11:
                    reminderMinutesBeforeStart = 1440 ;
                    break;

                case 12:
                    reminderMinutesBeforeStart = 2880 ;
                    break;

                case 13:
                    reminderMinutesBeforeStart = 4320 ;
                    break;

                case 14:
                    reminderMinutesBeforeStart = 10080 ;
                    break;

                case 15:
                    reminderMinutesBeforeStart = 20160 ;
                    break;

            }
        }

        if(parent == displayAsSpinner){
            switch(pos){
                case 0:
                    showAs = "Free";
                    break;

                case 1:
                    showAs = "WorkingElsewhere";
                    break;

                case 2:
                    showAs = "Tentative";
                    break;

                case 3:
                    showAs = "Busy";
                    break;

                case 4:
                    showAs = "Oof";
                    break;
            }
        }

        if(parent == repeatSpinner){
            switch(pos){
                case 0:
                    showAs = "Free";
                    break;

                case 1:
                    showAs = "WorkingElsewhere";
                    break;

                case 2:
                    showAs = "Tentative";
                    break;

                case 3:
                    showAs = "Busy";
                    break;

                case 4:
                    showAs = "Oof";
                    break;
            }
        }
    }

    public void onNothingSelected(AdapterView<?> parent) {
        // Another interface callback
    }
}
