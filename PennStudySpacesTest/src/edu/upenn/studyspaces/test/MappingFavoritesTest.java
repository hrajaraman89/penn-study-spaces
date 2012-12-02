package edu.upenn.studyspaces.test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.test.ActivityInstrumentationTestCase2;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.jayway.android.robotium.solo.Solo;

import edu.upenn.studyspaces.CustomMap;
import edu.upenn.studyspaces.CustomMap.PinOverlay;
import edu.upenn.studyspaces.FavoritesFragment;
import edu.upenn.studyspaces.MainActivity;
import edu.upenn.studyspaces.R;
import edu.upenn.studyspaces.StudySpace;

public class MappingFavoritesTest extends ActivityInstrumentationTestCase2<MainActivity> {

    private Solo solo;
    private Set<String> toBeRestoredKeys = new HashSet<String>();
    private Editor editor;

    public MappingFavoritesTest() {
        super(MainActivity.class);
    }

    protected void setUp() throws Exception {
        // make sure we start from scratch for each test
        super.setUp();
        MainActivity activity = getActivity();
        solo = new Solo(getInstrumentation(), activity);        
    }
    
    protected void tearDown() {
        for(String oneKey : toBeRestoredKeys) {
            editor.remove(oneKey);
        }
        solo.finishOpenedActivities();
    }

    public void testMappingAllFavorites() throws InterruptedException {
        assertEquals(true, solo.waitForActivity("MainActivity"));
        MainActivity mainActivity = (MainActivity) solo.getCurrentActivity();

        // make sure we have at least 3 entries in the favourite set
        SharedPreferences favorites = mainActivity.getSharedPreferences("favoritePreferences", 0);        
        editor = favorites.edit();
        String[] vals = new String[] {
                "Levine HallConference RoomLevine Hall Conference Room 512",
                "Towne BuildingMultipurpose RoomTowne Room 225 – Raisler Lounge",
                "Jon M. Huntsman HallForumPatty and Jay Baker Forum" };
        for(String oneVal : vals) {
            if (!favorites.getBoolean(oneVal, false)) {
                editor.putBoolean(oneVal, true);
                toBeRestoredKeys.add(oneVal);
            }
        }
        editor.commit();

        getInstrumentation().waitForIdleSync();
        solo.clickOnText("Search");
        solo.clickOnText("Favorites");

        // may take a while to load because we need to make API call
        String favTag = mainActivity.getString(R.string.favorites);
        boolean waitedForFavoriteFragment = solo.waitForFragmentByTag(favTag, 60000);
        assertEquals(true, waitedForFavoriteFragment);

        FavoritesFragment favFragment = (FavoritesFragment) mainActivity.getSupportFragmentManager().findFragmentByTag(favTag);

        if (solo.searchText("Retrieving data ...")) {
            // we are in the loading dialog
            // have to wait for a while because the app is making API request
            boolean waitForDialogToClose = solo.waitForDialogToClose(90000);
            assertEquals(true, waitForDialogToClose);
        }

        assertEquals(true, favFragment.isVisible());

        ListView listView = favFragment.getListView();
        solo.waitForView(listView);

        ListAdapter listAdapter = favFragment.getListAdapter();
        int count = listAdapter.getCount();
        assertEquals(true, count >= 3);

        // build the list of geoPoints that will be shown in the map
        Set<GeoPoint> geoPoints = new HashSet<GeoPoint>(); 
        int listAdapterCount = count;
        for(int i = 0; i < listAdapterCount; i++) {
            StudySpace studySpace = (StudySpace) listAdapter.getItem(i);
            double latitude = studySpace.getLatitude();
            double longitude = studySpace.getLongitude();
            GeoPoint point = new GeoPoint((int) (latitude * 1E6), (int) (longitude * 1E6));
            geoPoints.add(point);
        }

        solo.clickOnText("Map");

        boolean movedToMapActivity = solo.waitForActivity("CustomMap", 30000);
        assertEquals(true, movedToMapActivity);

        CustomMap mapActivity = (CustomMap) solo.getCurrentActivity();
        MapView mapView = (MapView) mapActivity.findViewById(R.id.mapview);
        solo.waitForView(mapView);
        
        List<Overlay> overlays = mapView.getOverlays();
        
        PinOverlay buildingOverlayItems;
        
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
            GeoPoint point = buildingOverlayItems.getItem(i).getPoint();
            assertEquals(true, geoPoints.contains(point));
        }

        solo.scrollViewToSide(mapView, Solo.LEFT);
    }
}
