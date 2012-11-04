package edu.upenn.studyspaces.test;

import junit.framework.TestCase;
import edu.upenn.studyspaces.Room;
import edu.upenn.studyspaces.StudySpace;

public class StudySpaceTest extends TestCase {

    public void testGSRName() {
        String name = "GSR";
        double lat = 0.0;
        double lon = 0.0;
        int num_rooms = 2;
        String b_name = "Jon M. Huntsman Hall";
        int max_occ = 10;
        boolean has_wh = true;
        String pri = "P";
        boolean has_comp = true;
        String res_type = "";
        boolean has_big_s = true;
        String comm = "";
        Room[] r = null;

        StudySpace a = new StudySpace().setSpaceName(name)
                .setBuildingName(b_name).setLatitude(lat).setLongitude(lon)
                .setNumRooms(num_rooms).setMaxOccupancy(max_occ)
                .setHasWhiteboard(has_wh).setPrivacy(pri)
                .setHasComputer(has_comp).setReserveType(res_type)
                .setHasBigScreen(has_big_s).setComments(comm).setRooms(r);

        assertEquals("Wharton", a.getBuildingType());
    }
}
