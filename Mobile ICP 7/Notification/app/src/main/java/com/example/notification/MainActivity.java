// Created by Vijaya Yeruva on 11/20/2020
// Reference: https://developer.android.com/guide/topics/ui/notifiers/notifications

package com.example.notification;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.view.View;
import android.widget.CalendarView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class MainActivity extends AppCompatActivity {
    TextView dateView;
    CalendarView calendarView;
    //Button btnCreateEvent;
    int YEAR, MONTH, DAY;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        dateView = findViewById(R.id.dateView);
        calendarView = findViewById(R.id.calendarView);
        //btnCreateEvent = findViewById(R.id.btnCreateEvent);

        // Set calendar to current date
        calendarView.setDate(Calendar.getInstance().getTimeInMillis());

        // sets initial string for dateView
        final DateFormat df = DateFormat.getDateInstance(DateFormat.LONG);
        dateView.setText(df.format(calendarView.getDate()));

        // Change date string in dateView when user selects a new date
        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
                // Creates object to hold formatting for dates
                Date date = new GregorianCalendar(year, month, dayOfMonth).getTime();
                YEAR = year; MONTH = month; DAY = dayOfMonth;

                dateView.setText(df.format(date));
            }
        });

        }


        public void open(View view){
        //function used when + button is clicked
            Calendar cal = Calendar.getInstance();

            //will set the year, month, day variables
            calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
                @Override
                public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
                    // Create new date object from parameters for long date format
                    Date date = new GregorianCalendar(year, month, dayOfMonth).getTime();
                    YEAR = year; MONTH = month; DAY = dayOfMonth;

                    //dateView.setText(df.format(date));
                }
            });
            //used to open google calenar app
            Intent intent = new Intent(Intent.ACTION_INSERT,
                    CalendarContract.Events.CONTENT_URI);
            // Add the calendar event details
            intent.putExtra(CalendarContract.Events.TITLE, "Launch!");
            intent.putExtra(CalendarContract.Events.DESCRIPTION,
                    "Learn Java Android Coding");
            intent.putExtra(CalendarContract.Events.EVENT_LOCATION,
                    "UMKC");
            Calendar startTime = Calendar.getInstance();
            startTime.set(YEAR, MONTH, DAY);
            intent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME,
                    startTime.getTimeInMillis());
            intent.putExtra(CalendarContract.EXTRA_EVENT_ALL_DAY, true);
            // Use the Calendar app to add the new event.
            startActivity(intent);
        }
    }
