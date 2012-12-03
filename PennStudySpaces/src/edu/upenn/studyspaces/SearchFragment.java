package edu.upenn.studyspaces;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.DialogFragment;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;

public class SearchFragment extends SherlockFragment {

    private Button searchButton;
    private Button findNowButton;
    private TextView numPeopleTextView;
    private SeekBar numPeopleSlider;
    private CheckBox isPrivateCheckBox;
    private CheckBox whiteboardCheckBox;
    private CheckBox computerCheckBox;
    private CheckBox projectorCheckBox;

    private CheckBox engineeringCheckBox;
    private CheckBox whartonCheckBox;
    private CheckBox libraryCheckBox;
    private CheckBox otherCheckBox;

    private CheckBox reservableCheckBox;

    private Button startTimeButton;
    private Button endTimeButton;
    private Button dateButton;

    private Location location;
    private SearchOptions searchOptions;
    private SharedPreferences sharedPreferences;

    static final int START_TIME_DIALOG_ID = 0;
    static final int END_TIME_DIALOG_ID = 1;
    static final int DATE_DIALOG_ID = 2;
    static final String SEARCH_PREFERENCES = "searchPreferences";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message message) {
            /**
             * Creating a bundle object to pass currently set Time to the
             * fragment
             */
            Bundle bundle = message.getData();

            /** Getting the Hour of day from bundle */
            int mHour = bundle.getInt("hour");

            /** Getting the Minute of the hour from bundle */
            int mMinute = bundle.getInt("minute");
            int fixedMinute = mMinute;// fixMinute(mMinute);

            switch (message.what) {
            case START_TIME_DIALOG_ID:
                searchOptions.setStartHour(mHour);
                searchOptions.setStartMinute(fixedMinute);
                break;
            case END_TIME_DIALOG_ID:
                searchOptions.setEndHour(mHour);
                searchOptions.setEndMinute(fixedMinute);
                break;
            case DATE_DIALOG_ID:
                int dayOfMonth = bundle.getInt("day");
                int monthOfYear = bundle.getInt("month");
                int year = bundle.getInt("year");
                int fixedYear = fixYear(year);

                searchOptions.setYear(fixedYear);
                searchOptions.setMonth(monthOfYear);
                searchOptions.setDay(dayOfMonth);
                break;
            }

            updateTimeAndDateDisplays();
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View layout = inflater.inflate(R.layout.search,
                container, false);
        captureViewElements(layout);

        return layout;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        checkConnection();

        sharedPreferences = this.getActivity().getSharedPreferences(
                SEARCH_PREFERENCES, 0);

        searchButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                onSearchButtonClick(v);

            }
        });

        findNowButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                onFindNowButtonClick(v);
            }
        });

        searchOptions = new SearchOptions();

        setUpNumberOfPeopleSlider();

        int numberOfPeople = sharedPreferences.getInt("numberOfPeople", -1);
        if (numberOfPeople != -1) {
            numPeopleSlider.setProgress(numberOfPeople - 1);
        }

        resetTimeAndDateData();

        setUpCheckBoxes();
        setUpPrivate();

        updateTimeAndDateDisplays();

        // submit GPS update schedule
        LocationManager locationManager = (LocationManager) this.getActivity()
                .getSystemService(Context.LOCATION_SERVICE);
        Criteria _criteria = new Criteria();
        // _criteria.setAccuracy(Criteria.ACCURACY_LOW);
        PendingIntent _pIntent = PendingIntent.getBroadcast(this.getActivity()
                .getApplicationContext(), 0, this.getActivity().getIntent(), 0);
        try {
            locationManager.requestLocationUpdates(5, 20, _criteria, _pIntent);
        } catch (IllegalArgumentException e) {
            Log.e("SearchActivity", "GPS probably turned off", e);
        }
        try {
            _pIntent.send();
        } catch (CanceledException e) {
            Log.e("SearchActivity",
                    "Problem sending GPS location update request", e);
        }

        // get current GPS location
        String provider = locationManager.getBestProvider(_criteria, true);
        location = null;
        if (provider != null) {
            location = locationManager.getLastKnownLocation(provider);
        }

        // add a click listener to the button
        startTimeButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                DialogFragment startTimeDialog = new TimePickerFragment(
                        mHandler, START_TIME_DIALOG_ID, searchOptions
                                .getStartHour(), searchOptions.getStartMinute());
                startTimeDialog.show(getFragmentManager(), "startTimePicker");
            }
        });
        endTimeButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                DialogFragment startTimeDialog = new TimePickerFragment(
                        mHandler, END_TIME_DIALOG_ID, searchOptions
                                .getEndHour(), searchOptions.getEndMinute());
                startTimeDialog.show(getFragmentManager(), "endTimePicker");
            }
        });
        dateButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                DialogFragment dateDialog = new DatePickerFragment(mHandler,
                        searchOptions.getMonth(), searchOptions.getDay(),
                        searchOptions.getYear());
                dateDialog.show(getFragmentManager(), "dateTimePicker");
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();

        // Access the default SharedPreferences

        // The SharedPreferences editor - must use commit() to submit changes
        SharedPreferences.Editor editor = sharedPreferences.edit();
        //
        // Edit the saved preferences
        editor.putBoolean("private", searchOptions.getPrivate());
        editor.putBoolean("computer", searchOptions.getComputer());
        editor.putBoolean("projector", searchOptions.getProjector());
        editor.putBoolean("engineering", searchOptions.getEngi());
        editor.putBoolean("library", searchOptions.getLib());
        editor.putBoolean("wharton", searchOptions.getWhar());
        editor.putBoolean("others", searchOptions.getOth());
        editor.putInt("numberOfPeople", searchOptions.getNumberOfPeople());
        editor.putBoolean("whiteboard", searchOptions.getWhiteboard());
        editor.commit();
    }

    private void setUpNumberOfPeopleSlider() {
        numPeopleSlider
                .setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
                    public void onStopTrackingTouch(SeekBar seekBar) {
                    }

                    public void onProgressChanged(SeekBar seekBar,
                            int progress, boolean fromUserTouch) {
                        searchOptions.setNumberOfPeople(progress + 1);
                        updateNumberOfPeopleDisplay();
                    }

                    public void onStartTrackingTouch(SeekBar seekBar) {
                    }
                });

        searchOptions.setNumberOfPeople(numPeopleSlider.getProgress() + 1);

        updateNumberOfPeopleDisplay();
    }

    private void setUpCheckBoxes() {

        engineeringCheckBox.setChecked(sharedPreferences.getBoolean(
                "engineering", true));
        whartonCheckBox.setChecked(sharedPreferences
                .getBoolean("wharton", true));
        libraryCheckBox.setChecked(sharedPreferences
                .getBoolean("library", true));
        otherCheckBox.setChecked(sharedPreferences.getBoolean("others", true));

        reservableCheckBox.setChecked(sharedPreferences.getBoolean(
                "reservable", true));

    }

    private void setUpPrivate() {

        isPrivateCheckBox.setChecked(sharedPreferences.getBoolean("private",
                false));
        whiteboardCheckBox.setChecked(sharedPreferences.getBoolean(
                "whiteboard", false));
        computerCheckBox.setChecked(sharedPreferences.getBoolean("computer",
                false));
        projectorCheckBox.setChecked(sharedPreferences.getBoolean("projector",
                false));
    }

    private void updateNumberOfPeopleDisplay() {
        String personPeopleString;
        if (searchOptions.getNumberOfPeople() == 1) {
            personPeopleString = " person";
        } else {
            personPeopleString = " people";
        }
        numPeopleTextView.setText(Integer.toString(searchOptions
                .getNumberOfPeople()) + personPeopleString);
    }

    private void roundCalendar(Calendar c) {
        if (c.get(Calendar.MINUTE) >= 1 && c.get(Calendar.MINUTE) <= 29) {
            c.add(Calendar.MINUTE, 30 - c.get(Calendar.MINUTE));
        }
        if (c.get(Calendar.MINUTE) >= 31 && c.get(Calendar.MINUTE) <= 59) {
            c.add(Calendar.MINUTE, 60 - c.get(Calendar.MINUTE));
        }
    }

    // TODO: Not sure if this is needed. Works fine without fixing minutes.
    @SuppressWarnings("unused")
    private int fixMinute(int minute) {
        int fixedMinute = minute;
        if (minute >= 31 && minute <= 45) {
            fixedMinute = 0; // Going up.
        } else if (minute >= 1 && minute <= 15) {
            fixedMinute = 30; // Going down.
        } else if (minute >= 46 && minute <= 59) {
            fixedMinute = 30; // Going up.
        } else if (minute >= 16 && minute <= 29) {
            fixedMinute = 0; // Going down.
        }
        return fixedMinute;
    }

    private int fixYear(int year) {
        Calendar c = Calendar.getInstance();
        if (year < c.get(Calendar.YEAR)) {
            year = c.get(Calendar.YEAR);
        }
        return year;
    }

    private String convertTo12HourFormat(int hour, int minute) {
        SimpleDateFormat displayFormat = new SimpleDateFormat("hh:mm a", Locale.US);
        SimpleDateFormat parseFormat = new SimpleDateFormat("HH:mm", Locale.US);
        String time = hour + ":" + minute;
        Date date;
        String convertedTime = "";
        try {
            date = parseFormat.parse(time);
            convertedTime = displayFormat.format(date);
        } catch (ParseException e) {
            Log.e("Time", "Couldn't parse time to 12 hour format.");
        }
        return convertedTime;
    }

    private void updateStartTimeText() {

        String convertedTime = convertTo12HourFormat(
                searchOptions.getStartHour(), searchOptions.getStartMinute());

        startTimeButton.setText(convertedTime);
    }

    private void updateEndTimeText() {
        String convertedTime = convertTo12HourFormat(
                searchOptions.getEndHour(), searchOptions.getEndMinute());

        endTimeButton.setText(convertedTime);
    }

    private String convertDateFormat(int month, int day, int year) {
        SimpleDateFormat displayFormat = new SimpleDateFormat(
                "EEE, MMM d, yyyy", Locale.US);
        SimpleDateFormat parseFormat = new SimpleDateFormat("M-d-y", Locale.US);

        String time = month + 1 + "-" + day + "-" + year;
        Date date;
        String convertedTime = "";
        try {
            date = parseFormat.parse(time);
            convertedTime = displayFormat.format(date);
        } catch (ParseException e) {
            Log.e("Time", "Couldn't parse date.");
        }
        return convertedTime;
    }

    private void updateDateText() {
        String convertedTime = convertDateFormat(searchOptions.getMonth(),
                searchOptions.getDay(), searchOptions.getYear());
        dateButton.setText(convertedTime);
    }

    // Updates search options then delivers intent
    public void onSearchButtonClick(View view) {
        if (checkConnection()) {
            putDataInSearchOptionsObject();
            Intent i = new Intent(this.getActivity().getBaseContext(),
                    StudySpaceListActivity.class);
            // Put your searchOption class here
            i.putExtra("SEARCH_OPTIONS", (Serializable) searchOptions);
            // ends this activity
            startActivity(i);
        }
    }

    private void putDataInSearchOptionsObject() {
        searchOptions.setNumberOfPeople(numPeopleSlider.getProgress() + 1);
        searchOptions.setPrivate(isPrivateCheckBox.isChecked());
        searchOptions.setWhiteboard(whiteboardCheckBox.isChecked());
        searchOptions.setComputer(computerCheckBox.isChecked());
        searchOptions.setProjector(projectorCheckBox.isChecked());

        searchOptions.setEngi(engineeringCheckBox.isChecked());
        searchOptions.setWhar(whartonCheckBox.isChecked());
        searchOptions.setLib(libraryCheckBox.isChecked());
        searchOptions.setOth(otherCheckBox.isChecked());

        searchOptions.setReservable(reservableCheckBox.isChecked());
    }

    private void resetTimeAndDateData() {
        // Initialize the date and time data based on the current time:
        final Calendar cStartTime = Calendar.getInstance();
        roundCalendar(cStartTime);
        searchOptions.setStartHour(cStartTime.get(Calendar.HOUR_OF_DAY));
        searchOptions.setStartMinute(cStartTime.get(Calendar.MINUTE));
        final Calendar cEndTime = Calendar.getInstance();
        cEndTime.add(Calendar.HOUR_OF_DAY, 1);
        roundCalendar(cEndTime);
        searchOptions.setEndHour(cEndTime.get(Calendar.HOUR_OF_DAY));
        searchOptions.setEndMinute(cEndTime.get(Calendar.MINUTE));
        searchOptions.setYear(cStartTime.get(Calendar.YEAR));
        searchOptions.setMonth(cStartTime.get(Calendar.MONTH));
        searchOptions.setDay(cStartTime.get(Calendar.DAY_OF_MONTH));
    }

    private void updateTimeAndDateDisplays() {
        updateStartTimeText();
        updateEndTimeText();
        updateDateText();
    }

    private void captureViewElements(View layout) {

        // General:
        searchButton = (Button) layout.findViewById(R.id.searchButton);
        findNowButton = (Button) layout.findViewById(R.id.findNowButton);
        numPeopleTextView = (TextView) layout
                .findViewById(R.id.numberOfPeopleTextView);
        numPeopleSlider = (SeekBar) layout
                .findViewById(R.id.numberOfPeopleSlider);
        isPrivateCheckBox = (CheckBox) layout
                .findViewById(R.id.privateCheckBox);
        whiteboardCheckBox = (CheckBox) layout
                .findViewById(R.id.whiteboardCheckBox);
        computerCheckBox = (CheckBox) layout
                .findViewById(R.id.computerCheckBox);
        projectorCheckBox = (CheckBox) layout
                .findViewById(R.id.projectorCheckBox);

        engineeringCheckBox = (CheckBox) layout.findViewById(R.id.engibox);
        whartonCheckBox = (CheckBox) layout.findViewById(R.id.whartonbox);
        libraryCheckBox = (CheckBox) layout.findViewById(R.id.libbox);
        otherCheckBox = (CheckBox) layout.findViewById(R.id.otherbox);

        reservableCheckBox = (CheckBox) layout
                .findViewById(R.id.reservableCheckBox);

        // Time and date:
        startTimeButton = (Button) layout.findViewById(R.id.pickStartTime);
        endTimeButton = (Button) layout.findViewById(R.id.pickEndTime);
        dateButton = (Button) layout.findViewById(R.id.pickDate);

    }

    public void onFavoritesButtonClick(View view) {
        if (checkConnection()) {
            putDataInSearchOptionsObject();
            Intent i = new Intent(this.getActivity().getBaseContext(),
                    FavoritesFragment.class);
            startActivity(i);
        }
    }

    public void onFindNowButtonClick(View view) {
        if (checkConnection()) {
            // reinitialize the date and time to current value
            resetTimeAndDateData();
            searchOptions.setNumberOfPeople(numPeopleSlider.getProgress());
            searchOptions.setPrivate(false);
            searchOptions.setWhiteboard(false);
            searchOptions.setComputer(false);
            searchOptions.setProjector(false);
            searchOptions.setEngi(true);
            searchOptions.setWhar(true);
            searchOptions.setLib(true);
            searchOptions.setOth(true);

            // get the list of all the study spaces
            ArrayList<StudySpace> studySpaces = ((APIAccessor) getActivity()
                    .getApplication()).getStudySpaces();
            ArrayList<StudySpace> ss_list = new ArrayList<StudySpace>();
            SharedPreferences favorites = this.getActivity()
                    .getSharedPreferences(
                            StudySpaceListActivity.FAV_PREFERENCES, 0);
            Map<String, ?> items = favorites.getAll();
            Preferences preferences = new Preferences(); // Change this when
                                                         // bundle is
            // implemented.
            for (String s : items.keySet()) {
                // boolean fav = favorites.getBoolean(s, false);
                if (Boolean.parseBoolean(items.get(s).toString())) {
                    preferences.addFavorites(s);
                }
            }
            ss_list.addAll(studySpaces);

            // filter them by location
            StudySpaceListAdapter ss_adapter = new StudySpaceListAdapter(
                    getActivity(), R.layout.sslistitem, ss_list, searchOptions);
            ss_adapter.setLocation(location);
            ss_adapter.filterSpaces();
            ss_adapter.updateFavorites(preferences);

            // call the study space details activity with the first study space
            // of the list (it is the closest)
            Intent i = new Intent(getActivity(), StudySpaceDetails.class);
            i.putExtra("STUDYSPACE", (StudySpace) ss_adapter.getItem(0));
            i.putExtra("PREFERENCES", preferences);
            getActivity().setResult(Activity.RESULT_OK, i);
            // ends this activity
            startActivity(i);
        }
    }

    private boolean checkConnection() {
        ConnectivityManager connMgr = (ConnectivityManager) this.getActivity()
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        boolean connection = true;
        if (networkInfo == null || !networkInfo.isConnected()) {
            Context context = this.getActivity().getApplicationContext();
            Toast toast = Toast.makeText(context, "", Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.setText("No network connection available.");
            toast.show();
            connection = false;
        }
        return connection;
    }

    public static class TimePickerFragment extends DialogFragment implements
            TimePickerDialog.OnTimeSetListener {

        private Handler mHandler;
        private int mHour;
        private int mMinute;
        int mType;

        public TimePickerFragment(Handler handler, int type, int hour,
                int minute) {
            mHandler = handler;
            mType = type;
            mHour = hour;
            mMinute = minute;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {

            return new TimePickerDialog(getActivity(), this, mHour, mMinute,
                    DateFormat.is24HourFormat(getActivity()));
        }

        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            mHour = hourOfDay;
            mMinute = minute;

            Bundle bundle = new Bundle();
            bundle.putInt("hour", mHour);
            bundle.putInt("minute", mMinute);
            Message message = new Message();
            message.setData(bundle);

            if (mType == START_TIME_DIALOG_ID)
                message.what = START_TIME_DIALOG_ID;

            else if (mType == END_TIME_DIALOG_ID)
                message.what = END_TIME_DIALOG_ID;

            mHandler.sendMessage(message);
        }
    }

    public static class DatePickerFragment extends DialogFragment implements
            DatePickerDialog.OnDateSetListener {
        private Handler mHandler;

        int mMonth;
        int mDay;
        int mYear;

        public DatePickerFragment(Handler handler, int month, int day, int year) {
            mHandler = handler;
            mMonth = month;
            mDay = day;
            mYear = year;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {

            return new DatePickerDialog(getActivity(), this, mYear, mMonth,
                    mDay);
        }

        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear,
                int dayOfMonth) {
            mDay = dayOfMonth;
            mMonth = monthOfYear;
            mYear = year;

            /**
             * Creating a bundle object to pass currently set date to the
             * fragment
             */
            Bundle bundle = new Bundle();

            bundle.putInt("day", mDay);
            bundle.putInt("month", mMonth);
            bundle.putInt("year", mYear);

            Message message = new Message();
            message.setData(bundle);
            message.what = DATE_DIALOG_ID;
            mHandler.sendMessage(message);

        }

    }

}