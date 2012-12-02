package edu.upenn.studyspaces.test;

import java.util.Map;

import org.json.JSONObject;

import android.content.Intent;
import android.content.SharedPreferences;
import android.test.ActivityInstrumentationTestCase2;

import com.jayway.android.robotium.solo.Solo;

import edu.upenn.studyspaces.Preferences;
import edu.upenn.studyspaces.Room;
import edu.upenn.studyspaces.StudySpace;
import edu.upenn.studyspaces.StudySpaceDetails;

public class StudySpaceDetailsTest extends
        ActivityInstrumentationTestCase2<StudySpaceDetails> {

    Solo solo;
    StudySpaceDetails detailsActivity;
    SharedPreferences favorites;
    Preferences preferences;
    StudySpace studySpace;

    // Space details
    String name = "Test Space";
    double lat = 0.0;
    double lon = 0.0;
    int numRooms = 2;
    String buildingName = "Test Building";
    int maxOccupancy = 10;
    boolean hasWhiteBoard = true;
    String priv = "P";
    boolean hasComputer = true;
    String reserveType = "";
    boolean hasBigScreen = true;
    String comments = "";
    JSONObject json = new JSONObject();
    Room room1 = new Room(3, "TestRoom", json);
    Room rooms[];

    public StudySpaceDetailsTest() {
        super(StudySpaceDetails.class);

    }

    protected void setUp1() throws Exception {
        super.setUp();

        rooms = new Room[1];
        rooms[0] = room1;

        studySpace = new StudySpace().setSpaceName(name)
                .setBuildingName(buildingName).setLatitude(lat)
                .setLongitude(lon).setNumRooms(numRooms)
                .setMaxOccupancy(maxOccupancy).setHasWhiteboard(hasWhiteBoard)
                .setPrivacy(priv).setHasComputer(hasComputer)
                .setReserveType(reserveType).setHasBigScreen(hasBigScreen)
                .setComments(comments).setRooms(rooms);

        Intent i = new Intent();

        i.putExtra("STUDYSPACE", studySpace);
        i.putExtra("PREFERENCES", new Preferences());

        setActivityIntent(i);
        detailsActivity = getActivity();

        solo = new Solo(getInstrumentation(), detailsActivity);
    }
    
    protected void setUp2() throws Exception {
        super.setUp();
        
        rooms = new Room[1];
        rooms[0] = room1;

        studySpace = new StudySpace().setSpaceName(name)
                .setBuildingName(buildingName).setLatitude(lat)
                .setLongitude(lon).setNumRooms(numRooms)
                .setMaxOccupancy(maxOccupancy).setHasWhiteboard(hasWhiteBoard)
                .setPrivacy(priv).setHasComputer(hasComputer)
                .setReserveType(reserveType).setHasBigScreen(hasBigScreen)
                .setComments(comments).setRooms(rooms);

        Intent i = new Intent();
        preferences = new Preferences();
        
        preferences.addFavorites(studySpace.getBuildingName() + studySpace.getSpaceName()
                + studySpace.getRoomNames());

       
        i.putExtra("STUDYSPACE", studySpace);
        i.putExtra("PREFERENCES", preferences);

        setActivityIntent(i);
        detailsActivity = getActivity();

        solo = new Solo(getInstrumentation(), detailsActivity);
    }
   

    protected void tearDown() throws Exception {
        solo.finishOpenedActivities();
        super.tearDown();
    }

    public Preferences getPreferences() {
        favorites = detailsActivity.getSharedPreferences("favoritePreferences",
                0);

        Map<String, ?> items = favorites.getAll();

        preferences = new Preferences();
        for (String s : items.keySet()) {
            if (Boolean.parseBoolean(items.get(s).toString())) {
                preferences.addFavorites(s);
            }
        }

        return preferences;
    }

    public void testAddFavorites() throws Exception {
        setUp1();
        solo.clickOnCheckBox(0);

        preferences = getPreferences();

        assertTrue(preferences.isFavorite(studySpace.getBuildingName()
                + studySpace.getSpaceName() + studySpace.getRoomNames()));

        solo.clickOnCheckBox(0);
    }

    public void testRemoveFavoritesAfterAdding() throws Exception {
        setUp1();
        solo.clickOnCheckBox(0);
        solo.clickOnCheckBox(0);

        preferences = getPreferences();

        assertFalse(preferences.isFavorite(studySpace.getBuildingName()
                + studySpace.getSpaceName() + studySpace.getRoomNames()));

    }
    
    public void testRemoveFavorites() throws Exception {
        setUp2();
        
        solo.clickOnCheckBox(0);
        preferences = getPreferences();
        assertFalse(preferences.isFavorite(studySpace.getBuildingName()
                + studySpace.getSpaceName() + studySpace.getRoomNames()));
    }
    
}
