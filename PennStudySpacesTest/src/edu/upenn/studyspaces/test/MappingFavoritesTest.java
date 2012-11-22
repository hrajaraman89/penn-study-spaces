package edu.upenn.studyspaces.test;

import java.util.ArrayList;
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
import android.view.View;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.jayway.android.robotium.solo.Solo;

import edu.upenn.studyspaces.CustomMap;
import edu.upenn.studyspaces.CustomMap.PinOverlay;
import edu.upenn.studyspaces.FavoritesActivity;
import edu.upenn.studyspaces.R;
import edu.upenn.studyspaces.SearchActivity;
import edu.upenn.studyspaces.StudySpace;

public class MappingFavoritesTest extends ActivityInstrumentationTestCase2<SearchActivity> {

    private Solo solo;
    private Set<String> toBeRestoredKeys = new HashSet<String>();
    private Editor editor;

    public MappingFavoritesTest() {
        super(SearchActivity.class);
    }

    protected void setUp() throws Exception {
        // make sure we start from scratch for each test
        super.setUp();
        SearchActivity activity = getActivity();
        solo = new Solo(getInstrumentation(), activity);        
    }
    
    protected void tearDown() {
        for(String oneKey : toBeRestoredKeys) {
            editor.remove(oneKey);
        }
        solo.finishOpenedActivities();
    }

    public void testMappingAllFavorites() throws InterruptedException {
        assertEquals(true, solo.waitForActivity("SearchActivity"));

        solo.clickOnButton("Favorites");
        
        boolean waitedForFavoriteActivity = solo.waitForActivity("FavoritesActivity");
        assertEquals(true, waitedForFavoriteActivity);
        FavoritesActivity favoritesActivity = (FavoritesActivity) solo.getCurrentActivity();
        
        ArrayList<View> currentViews = solo.getCurrentViews();
        if (currentViews.size() == 0) {
            // we are in the loading dialog
            // have to wait for a while because the app will make API request
            boolean waitForDialogToClose = solo.waitForDialogToClose(90000);
            assertEquals(true, waitForDialogToClose);
        }
        ListView listView = favoritesActivity.getListView();
        solo.waitForView(listView);

        SharedPreferences favorites = favoritesActivity.getSharedPreferences("favoritePreferences", 0);        
        editor = favorites.edit();
        
        // make sure we have at least 3 entries in the favourite set
        String[] vals = new String[] {"Levine HallAuditorium", "Towne BuildingConference Room", "Jon M. Huntsman HallForum"};
        for(String oneVal : vals) {
            if (!favorites.getBoolean(oneVal, false)) {
                editor.putBoolean(oneVal, true);
                toBeRestoredKeys.add(oneVal);
            }
        }
        editor.commit();

        
        // make sure there are at least a few favourite entries
        FavoritesActivity favoriteActivity = (FavoritesActivity) solo.getCurrentActivity();
        
        // wait because of API call
        solo.waitForDialogToClose(30000);
        
        ListAdapter listAdapter = favoriteActivity.getListAdapter();
        assertEquals(true, listAdapter.getCount() >= 3);

        // build the list of geoPoints that will be shown in the map
        Set<GeoPoint> geoPoints = new HashSet<GeoPoint>(); 
        int listAdapterCount = listAdapter.getCount();
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
