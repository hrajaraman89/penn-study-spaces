package edu.upenn.cis350.test;


import junit.framework.TestCase;

import org.junit.Test;

import edu.upenn.cis350.Room;
import edu.upenn.cis350.StudySpace;

import android.test.AndroidTestCase;
import android.util.Log;

public class StudySpaceTest extends TestCase {
	
	public void testGSRName() {
		String name = "GSR"; double lat = 0.0; double lon = 0.0;
		int num_rooms = 2; String b_name = "Jon M. Huntsman Hall";
		int max_occ = 10; boolean has_wh = true; String pri = "P";
		boolean has_comp = true; String res_type = ""; boolean has_big_s = true;
		String comm = ""; Room[] r = null;
		
		StudySpace a = new StudySpace(name, lat, lon, num_rooms, b_name, max_occ, has_wh,
										pri, has_comp, res_type, has_big_s, comm, r);
		
		assertEquals("Wharton", a.getBuildingType());
	}
}
