package edu.upenn.studyspaces;

import java.util.ArrayList;
import java.util.Map;

import android.app.ListActivity;
import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import edu.upenn.studyspaces.utilities.IntentCreators;
import edu.upenn.studyspaces.utilities.ReservationNotifier;

public class StudySpaceListActivity extends ListActivity {

    private ProgressDialog ss_ProgressDialog = null; // Dialog when loading
    private ArrayList<StudySpace> ss_list = null; // List containing available
                                                  // rooms
    private StudySpaceListAdapter ss_adapter; // Adapter to format list items
    private Runnable viewAvailableSpaces; // runnable to get available spaces
    public static final int ACTIVITY_ViewSpaceDetails = 1;
    public static final int ACTIVITY_SearchActivity = 2;
    private SearchOptions searchOptions; // create a default searchoption later
    private Preferences preferences;

    private SharedPreferences favorites;
    static final String FAV_PREFERENCES = "favoritePreferences";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sslist);

        this.searchOptions = (SearchOptions) getIntent().getSerializableExtra(
                "SEARCH_OPTIONS");
        favorites = getSharedPreferences(FAV_PREFERENCES, 0);

        LocationManager locationManager = (LocationManager) this
                .getSystemService(Context.LOCATION_SERVICE);

        // Request update location
        Criteria _criteria = new Criteria();
        PendingIntent _pIntent = PendingIntent.getBroadcast(
                getApplicationContext(), 0, getIntent(), 0);
        try {
            locationManager.requestSingleUpdate(_criteria, _pIntent);
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
        Location location = null;
        if (provider != null) {
            location = locationManager.getLastKnownLocation(provider);
        }

        try {
            ss_list = new ArrayList<StudySpace>(); // List to store StudySpaces
            ss_adapter = new StudySpaceListAdapter(this, R.layout.sslistitem,
                    ss_list, searchOptions);
            ss_adapter.setLocation(location);
            ss_adapter.filterSpaces();
            this.setListAdapter(this.ss_adapter); // Adapter to read list and
                                                  // display

            Map<String, ?> items = favorites.getAll();
            preferences = new Preferences(); // Change this when bundle is
                                             // implemented.
            for (String s : items.keySet()) {
                // boolean fav = favorites.getBoolean(s, false);
                if (Boolean.parseBoolean(items.get(s).toString())) {
                    preferences.addFavorites(s);
                }
            }

            viewAvailableSpaces = new Runnable() {
                public void run() {
                    getSpaces(); // retrieves list of study spaces
                }
            };
            Thread thread = new Thread(null, viewAvailableSpaces, "ThreadName"); // change
                                                                                 // name?
            thread.start();
            ss_ProgressDialog = ProgressDialog.show(
                    StudySpaceListActivity.this, null, "Retrieving data ...",
                    true);
        } catch (Exception e) {
            Log.e("StudySpaceListActivity onCreate:", e.toString());
        }

        final TextView search = (EditText) findViewById(R.id.search);
        search.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                String query = search.getText().toString();
                ss_adapter.searchNames(query);
            }

            public void beforeTextChanged(CharSequence s, int start, int count,
                    int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before,
                    int count) {
            }
        });

        registerForContextMenu(getListView());

    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        startDetailsActivity(position);
    }

    private void startDetailsActivity(int position) {
        Intent i = new Intent(this, StudySpaceDetails.class);
        i.putExtra("STUDYSPACE", (StudySpace) getListAdapter()
                .getItem(position));
        i.putExtra("PREFERENCES", preferences);
        startActivityForResult(i,
                StudySpaceListActivity.ACTIVITY_ViewSpaceDetails);
    }

    protected void onActivityResult(int requestCode, int resultCode,
            Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        switch (requestCode) {
        case ACTIVITY_SearchActivity:
            searchOptions = (SearchOptions) intent
                    .getSerializableExtra("SEARCH_OPTIONS");
            ss_adapter.filterSpaces();
            ss_adapter.updateFavorites(preferences);
            break;
        case ACTIVITY_ViewSpaceDetails:
            preferences = (Preferences) intent
                    .getSerializableExtra("PREFERENCES");
            ss_adapter.updateFavorites(preferences);
            break;
        }
    }

    private Runnable returnRes = new Runnable() {
        public void run() {
            ss_ProgressDialog.dismiss();
            ss_adapter.notifyDataSetChanged();
            if (searchOptions != null)
                ss_adapter.filterSpaces();
        }
    };

    private void getSpaces() {
        try {
            ArrayList<StudySpace> studySpaces = ((APIAccessor) getApplication())
                    .getStudySpaces();
            ss_list.addAll(studySpaces);
            ss_adapter.updateFavorites(preferences);
            Log.i("ARRAY", "" + ss_list.size());
        } catch (Exception e) {
            Log.e("BACKGROUND_PROC", "Something went wrong!");
        }
        runOnUiThread(returnRes);
    }

    public void onMapClick(View v) {
        Intent intent = new Intent(this, CustomMap.class);

        int size = ss_adapter.getCount();

        if (size > 0) {
            intent.putExtra(CustomMap.LIST_SIZE, size);
            for (int i = 0; i < size; i++) {
                intent.putExtra(CustomMap.STUDYSPACE + i, ss_adapter.getItem(i));
            }
            startActivity(intent);
        } else {
            Toast.makeText(this, "There are no search results",
                    Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.help:
            startActivity(new Intent(this, Help.class));
            break;
        }
        return true;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
            ContextMenuInfo menuInfo) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.search_results_long_press_menu, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item
                .getMenuInfo();
        int menuItemIndex = item.getItemId();

        switch (menuItemIndex) {
        case R.id.addOrReserve:
            StudySpace space = ss_adapter.getItem(info.position);
            Intent intent;

            if (space.isReservable()) {
                intent = IntentCreators.getReserveIntent(space);
            } else {
                intent = IntentCreators.getCalIntent(space);
            }

            if (intent != null) {
                startActivity(intent);
            } else {
                Toast.makeText(this, "Whoops! Something went wrong..",
                        Toast.LENGTH_LONG).show();
            }
            break;
        case R.id.share:
            startActivity(new ReservationNotifier().getSharingIntent(ss_adapter
                    .getItem(info.position)));
            break;
        }

        return true;
    }
}