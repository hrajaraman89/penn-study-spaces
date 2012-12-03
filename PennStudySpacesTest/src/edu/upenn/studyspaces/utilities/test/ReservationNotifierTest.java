package edu.upenn.studyspaces.utilities.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import android.content.Intent;
import edu.upenn.studyspaces.StudySpace;
import edu.upenn.studyspaces.utilities.ReservationNotifier;

public class ReservationNotifierTest {

    private ReservationNotifier notifier = new ReservationNotifier();

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void test_SimpleCase() {
        testNotifier();
    }

    private void testNotifier() {

        String spaceName = "name";
        String buildingName = "bname";
        double latitude = 9.45;
        double longtitude = 10.45;
        StudySpace space = new StudySpace().setSpaceName(spaceName)
                .setBuildingName(buildingName).setLatitude(latitude)
                .setLongitude(longtitude);

        Intent intent = this.notifier.getSharingIntent(space);

        assertTrue(intent != null);
        assertEquals("text/plain", intent.getType());
        assertEquals("StudySpace Reservation",
                intent.getExtras().get(Intent.EXTRA_SUBJECT));

        String body = intent.getExtras().get(Intent.EXTRA_TEXT).toString();

        assertTrue(body.contains("Building"));
        assertTrue(body.contains("Maps:"));
        assertTrue(body.contains("Start"));
        assertTrue(body.contains("End"));
    }

    @Test
    public void test_AnotherCase_Same_Instance() {
        testNotifier();
    }

}
