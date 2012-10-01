package edu.upenn.studyspaces.test;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import edu.upenn.studyspaces.Preferences;

public class PreferencesTest extends junit.framework.TestCase {
	Preferences _pref;
	
	@Before
	public void setUp() {
		_pref = new Preferences();
		_pref.addFavorites("Apple");
		_pref.addFavorites("Banana");
	}
	
	public void testAdd() {
		assertTrue(_pref.isFavorite("Apple"));
		assertFalse(_pref.isFavorite("Car"));
	}
	
	public void testRemove() {
		_pref.removeFavorites("Apple");
		assertFalse(_pref.isFavorite("Apple"));
		assertTrue(_pref.isFavorite("Banana"));
	}
}
