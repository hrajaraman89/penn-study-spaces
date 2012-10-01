package edu.upenn.studyspaces.test;

import static org.junit.Assert.*;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import edu.upenn.studyspaces.Room;

public class RoomTest extends junit.framework.TestCase {
	Room _room1;
	Room _room2;
	
	@Before
	public void setUp() {
		JSONObject _json = new JSONObject();
		_room1 = new Room(3, "Huntsman", _json);
		_room2 = new Room(5, "Harrison", _json);
	}
	
	public void testId() {
		assertEquals(_room1.getID(), 3);
		assertEquals(_room2.getID(), 5);
	}
	
	public void testNames() {
		assertEquals(_room1.getRoomName(), "Huntsman");
		assertEquals(_room2.getRoomName(), "Harrison");
	}
}
