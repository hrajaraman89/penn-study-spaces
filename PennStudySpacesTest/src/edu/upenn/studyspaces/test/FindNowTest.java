package edu.upenn.studyspaces.test;


import com.jayway.android.robotium.solo.Solo;
import android.test.ActivityInstrumentationTestCase2;
import edu.upenn.studyspaces.MainActivity;

public class FindNowTest extends ActivityInstrumentationTestCase2<MainActivity> {

	private Solo solo;

	public FindNowTest() {
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
	
	public void testFindNowButtonDirectly() {

		solo.clickOnButton("Find now!");

		assertTrue(solo.waitForActivity("StudySpaceDetails", 5000));

		solo.sleep(5000);

	}

	public void testFindNowBySearch() {

		solo.clickOnButton("Search");

		assertTrue(solo.waitForActivity("StudySpaceListActivity", 2000));
		
		if (solo.searchText("Retrieving data ...")) {
            // we are in the loading dialog
            // have to wait for a while because the app is making API request
            boolean waitForDialogToClose = solo.waitForDialogToClose(90000);
            assertEquals(true, waitForDialogToClose);
        }

		solo.sleep(5000);

	}

	
}
