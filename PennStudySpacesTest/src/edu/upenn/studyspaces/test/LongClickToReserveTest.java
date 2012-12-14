package edu.upenn.studyspaces.test;

import android.test.ActivityInstrumentationTestCase2;
import android.test.UiThreadTest;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.jayway.android.robotium.solo.Solo;

import edu.upenn.studyspaces.MainActivity;
import edu.upenn.studyspaces.R;
import edu.upenn.studyspaces.StudySpaceListActivity;

public class LongClickToReserveTest extends ActivityInstrumentationTestCase2<MainActivity> {

    private Solo solo;
    private MainActivity searchActivity;
    public LongClickToReserveTest() {
        super(MainActivity.class);
    }

    protected void setUp() throws Exception {
        // make sure we start from scratch for each test
        super.setUp();
         searchActivity = getActivity();
        solo = new Solo(getInstrumentation(), searchActivity);
    }
    
    protected void tearDown() {
        solo.finishOpenedActivities();
    }
    
    public void testEngineerWhartonLibrary10PeoplePrivateSearch() {
        // select 10 people, check engineering, wharton, library buildings and private places
        solo.waitForActivity("MainActivity");
        searchActivity.runOnUiThread(new Runnable() {

            @Override
            public void run() {
                setSearchPreference(10, new boolean[] { true, true, true, false },
                        new boolean[] { true, false, false, false, false });
            }
            
        });
       
        // select the time to be from 1pm to 2pm for safety, other time might lead to weird room availability.
        solo.clickOnButton(1);
        solo.setTimePicker(0, 13, 0);
        solo.clickOnButton("Done");
        solo.clickOnButton(2);
        solo.setTimePicker(0, 14, 0);
        solo.clickOnButton("Done");

        solo.clickOnButton("Search");

        runAndVerify();
    }

    private void setSearchPreference(int numOfPeople, boolean[] building, boolean[] features) {
        ProgressBar progressBar = (ProgressBar) searchActivity.findViewById(R.id.numberOfPeopleSlider);
        // 0 based indexing, has to subtract 1
        solo.setProgressBar(progressBar, numOfPeople - 1);
    
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

        //solo.searchText scrolls down so need to scroll back up to top.
        while(solo.scrollUp());

        ListView listView = ssListActivity.getListView();
        solo.waitForView(listView);
        
        solo.clickLongInList(1);
        
        boolean firstButtonAppear = solo.searchText("Add or Reserve");
        assertTrue(firstButtonAppear);
        boolean secondButtonAppear = solo.searchText("Share");
        assertTrue(secondButtonAppear);
    }
}
