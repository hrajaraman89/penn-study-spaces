package edu.upenn.studyspaces;

import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

import android.app.ListActivity;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class StudySpaceListActivity extends ListActivity {

    private ProgressDialog ss_ProgressDialog = null; // Dialog when loading
    private ArrayList<StudySpace> ss_list = null; // List containing available
                                                  // rooms
    private StudySpaceListAdapter ss_adapter; // Adapter to format list items
    private Runnable viewAvailableSpaces; // runnable to get available spaces
    public static final int ACTIVITY_ViewSpaceDetails = 1;
    public static final int ACTIVITY_SearchActivity = 2;
    private SearchOptions searchOptions; // create a default searchoption later
    private boolean favSelected = false;
    private Preferences preferences;

    private SharedPreferences favorites;
    static final String FAV_PREFERENCES = "favoritePreferences";

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sslist);

        this.searchOptions = (SearchOptions) getIntent().getSerializableExtra(
                "SEARCH_OPTIONS");
        favorites = getSharedPreferences(FAV_PREFERENCES, 0);
        
        // get current GPS location
        LocationManager locationManager = (LocationManager) this
                .getSystemService(Context.LOCATION_SERVICE);
        Criteria _criteria = new Criteria();
        String provider = locationManager.getBestProvider(_criteria, true);
        Location location = null;
        if (provider != null) {
            location = locationManager.getLastKnownLocation(provider);
        }

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
        ss_ProgressDialog = ProgressDialog.show(StudySpaceListActivity.this,
                "Please wait...", "Retrieving data ...", true);

        /*
         * engiBox.setChecked(true); engiBox.setOnCheckedChangeListener(new
         * CompoundButton.OnCheckedChangeListener() { public void
         * onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
         * ss_adapter.filterSpaces(); } }); whartonBox.setChecked(true);
         * whartonBox .setOnCheckedChangeListener(new
         * CompoundButton.OnCheckedChangeListener() { public void
         * onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
         * ss_adapter.filterSpaces(); } }); libBox.setChecked(true);
         * libBox.setOnCheckedChangeListener(new
         * CompoundButton.OnCheckedChangeListener() { public void
         * onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
         * ss_adapter.filterSpaces(); } }); otherBox.setChecked(true);
         * otherBox.setOnCheckedChangeListener(new
         * CompoundButton.OnCheckedChangeListener() { public void
         * onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
         * ss_adapter.filterSpaces(); } });
         */
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

    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) { // click
        // String item = ((StudySpace)
        // getListAdapter().getItem(position)).getSpaceName();
        // Toast.makeText(this, item + " selected", Toast.LENGTH_SHORT).show();
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
            favSelected = false;
            ImageView image = (ImageView) this
                    .findViewById(R.id.favorite_button);
            image.setImageResource(R.color.yellow);
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
            ss_list.addAll(APIAccessor.getStudySpaces());
            ss_adapter.updateFavorites(preferences);
            //Thread.sleep(2000); // appears to load for 2 seconds FIXME: Why is this needed?
            Log.i("ARRAY", "" + ss_list.size());
        } catch (Exception e) {
            Log.e("BACKGROUND_PROC", "Something went wrong!");
        }
        runOnUiThread(returnRes);
    }

    public void onFavClick(View v) {
        ImageView image = (ImageView) this.findViewById(R.id.favorite_button);
        if (favSelected) {
            favSelected = false;
            image.setImageResource(R.color.yellow);
            ss_adapter.favToAll();
        } else {
            favSelected = true;
            image.setImageResource(R.color.lightblue);
            ss_adapter.allToFav();
        }

    }

    public void onFilterClick(View view) {
        // Start up the search options screen
        finish();
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
        case R.id.meme:
            startActivity(new Intent(this, Meme.class));
            break;
        case R.id.about:
            startActivity(new Intent(this, About.class));
            break;
        case R.id.help:
            startActivity(new Intent(this, Help.class));
            break;
        }
        return true;
    }
}