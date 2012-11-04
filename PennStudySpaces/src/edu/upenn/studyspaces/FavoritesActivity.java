package edu.upenn.studyspaces;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Map;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

public class FavoritesActivity extends ListActivity {
    private ProgressDialog ss_ProgressDialog = null;
    private ArrayList<StudySpace> ss_list = null; // List containing available
                                                  // rooms
    private StudySpaceListAdapter ss_adapter; // Adapter to format list items
    private SearchOptions searchOptions;
    private Preferences preferences;

    private SharedPreferences favorites;
    private Runnable viewAvailableSpaces;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);

        this.searchOptions = (SearchOptions) getIntent().getSerializableExtra(
                "SEARCH_OPTIONS");
        favorites = getSharedPreferences(
                StudySpaceListActivity.FAV_PREFERENCES, 0);

        ss_list = new ArrayList<StudySpace>(); // List to store StudySpaces
        this.ss_adapter = new StudySpaceListAdapter(this, R.layout.sslistitem,
                ss_list, searchOptions);
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

        Thread thread = new Thread(null, viewAvailableSpaces, "FavoritesThread");
        thread.start();
        ss_ProgressDialog = ProgressDialog.show(FavoritesActivity.this,
                "Please wait...", "Retrieving data ...", true);
        ss_ProgressDialog.setCancelable(true);
    }

    private Runnable returnRes = new Runnable() {
        public void run() {
            ss_ProgressDialog.dismiss();
            ss_adapter.notifyDataSetChanged();
            if (searchOptions != null)
                ss_adapter.filterSpaces();
            ss_adapter.allToFav();
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

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        Intent i = new Intent(this, StudySpaceDetails.class);
        i.putExtra("STUDYSPACE", (StudySpace) getListAdapter()
                .getItem(position));
        i.putExtra("PREFERENCES", preferences);
        startActivityForResult(i,
                StudySpaceListActivity.ACTIVITY_ViewSpaceDetails);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
            Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        if (intent.getBooleanExtra("REMOVED_FAVORITE", false)) {
            Intent refresh = new Intent(this, FavoritesActivity.class);
            refresh.putExtra("SEARCH_OPTIONS", (Serializable) searchOptions);
            startActivity(refresh);

            this.finish();
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
