package edu.upenn.studyspaces.test;

import java.util.ArrayList;
import java.util.Collections;

import junit.framework.TestCase;

import org.junit.Test;

import edu.upenn.studyspaces.APIAccessor;
import edu.upenn.studyspaces.StudySpace;
import edu.upenn.studyspaces.StudySpaceListAdapter;
import edu.upenn.studyspaces.StudySpaceListAdapter.StudySpaceDistanceComparator;

public class StudySpaceDistanceTest extends TestCase  {
    @Test
    public void testStudySpacesDistanceSort() throws Exception {
        double currentLocationLat =  39.95442;
        double currentLocationLong = -75.19245;

        ArrayList<StudySpace> ssList = APIAccessor.getStudySpaces();
        StudySpaceDistanceComparator comparator = new StudySpaceListAdapter.StudySpaceDistanceComparator(
                currentLocationLat, currentLocationLong);
        Collections.sort(ssList, comparator);
        
        StudySpace prev = null;
        for(StudySpace one : ssList) {
            if (prev == null) {
                prev = one;
                continue;
            }
            double prevDis =
                    distance(currentLocationLat, currentLocationLong, prev.getSpaceLatitude(), prev.getSpaceLongitude());
            double currentDis =
                    distance(currentLocationLat, currentLocationLong, one.getSpaceLatitude(), one.getSpaceLongitude());
            assertEquals(true, prevDis <= currentDis);
        }
    }

    static double distance(double lat1, double long1, double lat2, double long2) {
        double latDiff = lat1 - lat2;
        double longDiff = long1 - long2;
        double retval = Math.sqrt(latDiff * latDiff + longDiff * longDiff);
        return retval;
    }
}
