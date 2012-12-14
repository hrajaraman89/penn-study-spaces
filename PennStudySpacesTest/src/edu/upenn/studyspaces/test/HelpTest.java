package edu.upenn.studyspaces.test;


import com.jayway.android.robotium.solo.Solo;
import android.test.ActivityInstrumentationTestCase2;
import edu.upenn.studyspaces.MainActivity;

public class HelpTest extends ActivityInstrumentationTestCase2<MainActivity> {

	private Solo solo;

	public HelpTest() {
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

		solo.clickOnMenuItem("Help");

		assertTrue(solo.waitForActivity("Help", 5000));
		
		assertTrue(solo.searchText("will be sorted out by distance"));
		solo.sleep(5000);

	}


	
}
