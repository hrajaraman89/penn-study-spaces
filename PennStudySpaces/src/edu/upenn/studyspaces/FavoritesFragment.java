package edu.upenn.studyspaces;

import java.util.ArrayList;
import java.util.Map;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

public class FavoritesFragment extends ListFragment {
    private ProgressDialog ss_ProgressDialog = null;
    private ArrayList<StudySpace> ss_list = null; // List containing available
                                                  // rooms
    private StudySpaceListAdapter ss_adapter; // Adapter to format list items
    private Preferences preferences;

    private SharedPreferences favorites;
    private Runnable viewAvailableSpaces;
    private Button mMapButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private Runnable returnRes = new Runnable() {
        public void run() {
            ss_ProgressDialog.dismiss();
            ss_adapter.notifyDataSetChanged();
            ss_adapter.allToFav();
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        LinearLayout layout = (LinearLayout) inflater.inflate(
                R.layout.activity_favorites, container, false);
        mMapButton = (Button) layout.findViewById(R.id.mapFavoritesButton);

        return layout;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        favorites = getActivity().getSharedPreferences(
                StudySpaceListActivity.FAV_PREFERENCES, 0);

        ss_list = new ArrayList<StudySpace>(); // List to store StudySpaces
        this.ss_adapter = new StudySpaceListAdapter(getActivity()
                .getApplicationContext(), R.layout.sslistitem, ss_list, null);

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

        mMapButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                onMapClick(v);

            }
        });

        ss_adapter.allToFav();
        viewAvailableSpaces = new Runnable() {
            public void run() {
                getSpaces(); // retrieves list of study spaces
            }
        };

        Thread thread = new Thread(null, viewAvailableSpaces, "FavoritesThread");
        thread.start();
        ss_ProgressDialog = ProgressDialog.show(getActivity(), null,
                "Retrieving data ...", true);
        ss_ProgressDialog.setCancelable(true);
    }

    private void getSpaces() {
        try {
            ArrayList<StudySpace> studySpaces = ((APIAccessor) getActivity()
                    .getApplication()).getStudySpaces();
            ss_list.addAll(studySpaces);
            ss_adapter.updateFavorites(preferences);
            Log.i("ARRAY", "" + ss_list.size());
        } catch (Exception e) {
            Log.e("BACKGROUND_PROC", "Something went wrong!");
        }
        if (getActivity() != null)
            getActivity().runOnUiThread(returnRes);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        Intent i = new Intent(getActivity(), StudySpaceDetails.class);
        i.putExtra("STUDYSPACE", (StudySpace) getListAdapter()
                .getItem(position));
        i.putExtra("PREFERENCES", preferences);
        startActivityForResult(i,
                StudySpaceListActivity.ACTIVITY_ViewSpaceDetails);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        if (intent.getBooleanExtra("REMOVED_FAVORITE", false)) {
            Intent refresh = new Intent(getActivity(), MainActivity.class);
            refresh.putExtra("FAVORITES", true);
            startActivity(refresh);

            getActivity().finish();
        }
    }

    public void onMapClick(View v) {
        Intent intent = new Intent(getActivity(), CustomMap.class);

        int size = ss_adapter.getCount();
        if (size > 0) {
            intent.putExtra(CustomMap.LIST_SIZE, size);
            for (int i = 0; i < size; i++) {
                intent.putExtra(CustomMap.STUDYSPACE + i, ss_adapter.getItem(i));
            }
            startActivity(intent);
        } else {
            Toast.makeText(getActivity(), "There are no favorites",
                    Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.meme:
            startActivity(new Intent(getActivity(), Meme.class));
            break;
        case R.id.about:
            startActivity(new Intent(getActivity(), About.class));
            break;
        case R.id.help:
            startActivity(new Intent(getActivity(), Help.class));
            break;
        }
        return true;
    }

}
