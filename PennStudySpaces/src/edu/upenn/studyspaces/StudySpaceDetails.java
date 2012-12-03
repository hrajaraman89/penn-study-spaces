package edu.upenn.studyspaces;

import java.io.Serializable;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

public class StudySpaceDetails extends FragmentActivity {

    private TabDetails tabdetails;
    private StudySpace o;
    private Preferences p;

    private SharedPreferences favorites;

    protected boolean removedFavorite = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ssdetails);

        favorites = getSharedPreferences(
                StudySpaceListActivity.FAV_PREFERENCES, 0);

        Intent i = getIntent();
        o = (StudySpace) i.getSerializableExtra("STUDYSPACE");
        p = (Preferences) i.getSerializableExtra("PREFERENCES");
        tabdetails = new TabDetails();

        // Saves the first state of the code
        ImageView image = (ImageView) findViewById(R.id.button_details);
        image.setImageResource(R.color.lightblue);
        FragmentTransaction transaction = getSupportFragmentManager()
                .beginTransaction();
        transaction.replace(R.id.fragment_container, tabdetails);
        transaction.commit();
    }

    public void onShareClick(View v) {
        tabdetails.onShareClick(v);
    }

    public void onMapClick(View v) {
        Intent i = new Intent(this, CustomMap.class);

        i.putExtra(CustomMap.LIST_SIZE, 1);
        i.putExtra(CustomMap.STUDYSPACE + 0, o);
        startActivity(i);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK) {

            Intent i = new Intent();

            i.putExtra("PREFERENCES", (Serializable) p);
            i.putExtra("REMOVED_FAVORITE", removedFavorite);
            setResult(RESULT_OK, i);
            // ends this activity
            finish();
        }
        return super.onKeyDown(keyCode, event);
    }

    public void onCalClick(View v) {
        tabdetails.onCalClick(v);

    }

    public void onReserveClick(View v) {
        tabdetails.onReserveClick(v);
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
