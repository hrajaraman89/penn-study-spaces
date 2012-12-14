package edu.upenn.studyspaces.test;

import android.test.ActivityInstrumentationTestCase2;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.jayway.android.robotium.solo.Solo;

import edu.upenn.studyspaces.MainActivity;
import edu.upenn.studyspaces.R;
import edu.upenn.studyspaces.StudySpaceListActivity;

public class LongClickToReserveTest extends ActivityInstrumentationTestCase2<MainActivity> {

    private Solo solo;

    public LongClickToReserveTest() {
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
    
    public void testEngineerWhartonLibrary10PeoplePrivateSearch() {

        // select 10 people, check engineering, wharton, library buildings and private places
        setSearchPreference(10, new boolean[] { true, true, true, false },
                new boolean[] { true, false, false, false });

        solo.clickOnButton("Search");

        runAndVerify();
    }

    private void setSearchPreference(int numOfPeople, boolean[] building, boolean[] features) {
        solo.waitForActivity("MainActivity");
        MainActivity searchActivity = (MainActivity) solo.getCurrentActivity();

        ProgressBar progressBar = (ProgressBar) searchActivity.findViewById(R.id.numberOfPeopleSlider);
        // 0 based indexing, has to subtract 1
        solo.setProgressBar(progressBar, numOfPeople - 1);
    
        CheckBox engineeringCheckBox = (CheckBox) searchActivity.findViewById(R.id.engibox);
        if (engineeringCheckBox.isChecked() != building[0]) {
            solo.clickOnCheckBox(4);
        }
    
        CheckBox whartonCheckBox = (CheckBox) searchActivity.findViewById(R.id.whartonbox);
        if (whartonCheckBox.isChecked() != building[1]) {
            solo.clickOnCheckBox(5);
        }
        CheckBox libraryCheckBox = (CheckBox) searchActivity.findViewById(R.id.libbox);
        if (libraryCheckBox.isChecked() != building[2]) {
            solo.clickOnCheckBox(6);
        }
        CheckBox otherCheckBox = (CheckBox) searchActivity.findViewById(R.id.otherbox);
        if (otherCheckBox.isChecked()  != building[3]) {
            solo.clickOnCheckBox(7);
        }
    
        CheckBox privateCheckBox = (CheckBox) searchActivity.findViewById(R.id.privateCheckBox);
        if (privateCheckBox.isChecked() != features[0]) {
            solo.clickOnCheckBox(0);
        }
        CheckBox whiteboardCheckBox = (CheckBox) searchActivity.findViewById(R.id.whiteboardCheckBox);
        if (whiteboardCheckBox.isChecked() != features[1]) {
            solo.clickOnCheckBox(1);
        }
        CheckBox computerCheckBox = (CheckBox) searchActivity.findViewById(R.id.computerCheckBox);
        if (computerCheckBox.isChecked() != features[2]) {
            solo.clickOnCheckBox(2);
        }
        CheckBox projectorCheckBox = (CheckBox) searchActivity.findViewById(R.id.projectorCheckBox);
        if (projectorCheckBox.isChecked() != features[3]) {
            solo.clickOnCheckBox(3);
        }
        
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
        
        solo.clickLongInList(1);
        
        boolean firstButtonAppear = solo.searchText("Add or Reserve");
        assertTrue(firstButtonAppear);
        boolean secondButtonAppear = solo.searchText("Share");
        assertTrue(secondButtonAppear);
    }
}
