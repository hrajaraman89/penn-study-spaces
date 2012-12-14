package edu.upenn.studyspaces.test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.test.ActivityInstrumentationTestCase2;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.jayway.android.robotium.solo.Solo;

import edu.upenn.studyspaces.CustomMap;
import edu.upenn.studyspaces.CustomMap.PinOverlay;
import edu.upenn.studyspaces.MainActivity;
import edu.upenn.studyspaces.R;
import edu.upenn.studyspaces.StudySpace;
import edu.upenn.studyspaces.StudySpaceListActivity;
import edu.upenn.studyspaces.StudySpaceListAdapter;

public class MapPinDetailsDialogTest extends ActivityInstrumentationTestCase2<MainActivity> {

    private Solo solo;

    public MapPinDetailsDialogTest() {
        super(MainActivity.class);
    }

    protected void setUp() throws Exception {
        // make sure we start from scratch for each test
        super.setUp();
        MainActivity activity = getActivity();
        solo = new Solo(getInstrumentation(), activity);
    }
    
    protected void tearDown() {
        solo.finishOpenedActivities();
    }
    
    public void testEngineer15PeopleSearch() {

        // select 15 people, check engineering building and no feature
        setSearchPreference(15, new boolean[] { true, false, false, false },
                new boolean[] { false, false, false, false, false });

        solo.clickOnButton("Search");

        runAndVerify();
    }

    public void testEngineerWharton10PeopleSearch() {

        // select 10 people, check engineering and wharton building and no feature
        setSearchPreference(10, new boolean[] { true, true, false, false },
                new boolean[] { false, false, false, false, false });

        solo.clickOnButton("Search");

        runAndVerify();
    }

    public void testEngineerWhartonLibrary10PeoplePrivateSearch() {

        // select 10 people, check engineering, wharton, library buildings and private places
        setSearchPreference(10, new boolean[] { true, true, true, false },
                new boolean[] { true, false, false, false, false });

        solo.clickOnButton("Search");

        runAndVerify();
    }

    private void setSearchPreference(int numOfPeople, final boolean[] building, final boolean[] features) {
        solo.waitForActivity("MainActivity");
        final MainActivity searchActivity = (MainActivity) solo.getCurrentActivity();

        ProgressBar progressBar = (ProgressBar) searchActivity.findViewById(R.id.numberOfPeopleSlider);
        // 0 based indexing, has to subtract 1
        solo.setProgressBar(progressBar, numOfPeople - 1);
        
        searchActivity.runOnUiThread(new Runnable() {            
            @Override
            public void run() {
                CheckBox engineeringCheckBox = (CheckBox) searchActivity.findViewById(R.id.engibox);
                engineeringCheckBox.setChecked(building[0]);
            
                CheckBox whartonCheckBox = (CheckBox) searchActivity.findViewById(R.id.whartonbox);
                whartonCheckBox.setChecked(building[1]);

                CheckBox libraryCheckBox = (CheckBox) searchActivity.findViewById(R.id.libbox);
                libraryCheckBox.setChecked(building[2]);

                CheckBox otherCheckBox = (CheckBox) searchActivity.findViewById(R.id.otherbox);
                otherCheckBox.setChecked(building[3]);

                CheckBox privateCheckBox = (CheckBox) searchActivity.findViewById(R.id.privateCheckBox);
                privateCheckBox.setChecked(features[0]);

                CheckBox whiteboardCheckBox = (CheckBox) searchActivity.findViewById(R.id.whiteboardCheckBox);
                whiteboardCheckBox.setChecked(features[1]);

                CheckBox computerCheckBox = (CheckBox) searchActivity.findViewById(R.id.computerCheckBox);
                computerCheckBox.setChecked(features[2]);

                CheckBox projectorCheckBox = (CheckBox) searchActivity.findViewById(R.id.projectorCheckBox);
                projectorCheckBox.setChecked(features[3]);
                
                CheckBox reservableCheckBox = (CheckBox) searchActivity.findViewById(R.id.reservableCheckBox);
                reservableCheckBox.setChecked(features[4]);
            }
        });
        
        // select the time to be from 1pm to 2pm for safety, other time might lead to weird room availability.
        solo.clickOnButton(1);
        solo.setTimePicker(0, 13, 0);
        solo.clickOnButton("Done");
        solo.clickOnButton(2);
        solo.setTimePicker(0, 14, 0);
        solo.clickOnButton("Done");
    }

    private void runAndVerify() {
        boolean movedToListActivity = solo.waitForActivity("StudySpaceListActivity");
        assertEquals(true, movedToListActivity);

        StudySpaceListActivity ssListActivity = (StudySpaceListActivity) solo.getCurrentActivity();

        if (solo.searchText("Retrieving data ...")) {
            // we are in the loading dialog
            // have to wait for a while because the app is making API request
            boolean waitForDialogToClose = solo.waitForDialogToClose(90000);
            assertEquals(true, waitForDialogToClose);
        }

        ListView listView = ssListActivity.getListView();
        solo.waitForView(listView);

        StudySpaceListAdapter listAdapter = (StudySpaceListAdapter) ssListActivity.getListAdapter();

        // build the list of geoPoint that result from this list when we move to map view
        Set<GeoPoint> geoPoints = new HashSet<GeoPoint>(); 
        int listAdapterCount = listAdapter.getCount();
        for(int i = 0; i < listAdapterCount; i++) {
            StudySpace studySpace = listAdapter.getItem(i);
            double latitude = studySpace.getLatitude();
            double longitude = studySpace.getLongitude();
            GeoPoint point = new GeoPoint((int) (latitude * 1E6), (int) (longitude * 1E6));
            geoPoints.add(point);
        }

        solo.clickOnText("Map");
        
        boolean movedToMapActivity = solo.waitForActivity("CustomMap");
        assertEquals(true, movedToMapActivity);

        CustomMap mapActivity = (CustomMap) solo.getCurrentActivity();
        final MapView mapView = (MapView) mapActivity.findViewById(R.id.mapview);
        solo.waitForView(mapView);
        List<Overlay> overlays = mapView.getOverlays();
        
        final PinOverlay buildingOverlayItems;
        
        // check current location
        LocationManager locationManager = (LocationManager) mapActivity.getSystemService(Context.LOCATION_SERVICE);
        String _bestProvider = locationManager.getBestProvider(new Criteria(), true);
        Location location = locationManager.getLastKnownLocation(_bestProvider);
        if (location != null) {
            // current location pin must be drawn (bad logic here, need to read CustomMap to understand)
            PinOverlay currentLocationPinOverlay = (PinOverlay) overlays.get(0);
            assertEquals(1, currentLocationPinOverlay.size());
            buildingOverlayItems = (PinOverlay) overlays.get(1);
        } else {
            buildingOverlayItems = (PinOverlay) overlays.get(0);
        }

        // check search result locations
        int buildingOverlayCount = buildingOverlayItems.size();
        assertEquals(geoPoints.size(), buildingOverlayCount);
        for(int i = 0; i < buildingOverlayCount; i++) {
            final GeoPoint point = buildingOverlayItems.getItem(i).getPoint();
            
            mapActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    buildingOverlayItems.onTap(point, mapView);
                }
            });

            solo.waitForText("Cancel");
            solo.clickInList(0);
            boolean activityWait = solo.waitForActivity("StudySpaceDetails");
            
            assertTrue(activityWait);
            
            solo.goBack();
        }
    }
}
