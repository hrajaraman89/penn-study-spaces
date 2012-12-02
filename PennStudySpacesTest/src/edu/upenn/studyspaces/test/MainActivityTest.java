package edu.upenn.studyspaces.test;

import com.jayway.android.robotium.solo.Solo;

import edu.upenn.studyspaces.Preferences;
import edu.upenn.studyspaces.Room;
import edu.upenn.studyspaces.StudySpace;
import edu.upenn.studyspaces.MainActivity;
import android.content.Intent;
import android.test.ActivityInstrumentationTestCase2;

public class MainActivityTest extends
        ActivityInstrumentationTestCase2<MainActivity> {

    private Solo solo;

    public MainActivityTest() {
        super(MainActivity.class);

    }

    protected void setUp() throws Exception {
        super.setUp();

        solo = new Solo(getInstrumentation(), getActivity());
    }

    protected void tearDown() throws Exception {

        solo.finishOpenedActivities();
        super.tearDown();
    }

    public void testClickNavigationFavorites() {
        solo.clickOnText("Search");
        solo.clickOnText("Favorites");

        assertTrue(solo.waitForFragmentByTag("Favorites"));
    }

    public void testClickNothing() {
        assertTrue(solo.waitForFragmentByTag("Search"));
    }

    public void testClickNavigationSearchOnce() {
        solo.clickOnText("Search");

        assertTrue(solo.waitForFragmentByTag("Search"));
    }

    public void testClickNavigationSearchTwice() {
        solo.clickOnText("Search");
        solo.clickOnText("Search");

        assertTrue(solo.waitForFragmentByTag("Search", 1000));
    }

    public void testClickSearchButton() {
        solo.clickOnButton("Search");

        assertTrue(solo.waitForActivity("StudySpaceListActivity", 2000));
    }

}
