package edu.upenn.studyspaces;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Application;
import android.util.Log;

public class APIAccessor extends Application {

    private static final String API_URL = "http://www.pennstudyspaces.com/api?showall=1&format=json";
    private static final String API_ACCESSOR = "API_ACCESSOR";
    private static final long REFRESH_INTERVAL = 5 * 1000 * 60; // 5 minutes
    private long lastAccess;
    private ArrayList<StudySpace> studySpaces;

    public APIAccessor() {
        this.studySpaces = new ArrayList<StudySpace>();
        lastAccess = 0;
    }

    @Override
    public void onCreate() {
        Runnable runnable = new Runnable() {
            public void run() {
                buildStudySpaces();
            }
        };

        Thread buildSpaces = new Thread(runnable);
        buildSpaces.start();
    }

    private void buildStudySpaces() {

        ArrayList<StudySpace> previousSpaces = this.studySpaces;
        this.studySpaces = new ArrayList<StudySpace>();

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    new URL(API_URL).openStream()));

            String line = reader.readLine();

            JSONObject json_obj = new JSONObject(line);

            JSONArray buildings = json_obj.getJSONArray("buildings");

            createStudySpacesFromBuildings(buildings);

            lastAccess = System.currentTimeMillis();

        } catch (Exception e) {
            this.studySpaces = previousSpaces;
            Log.e(API_ACCESSOR, e.getMessage());
        }
    }

    private void createStudySpacesFromBuildings(JSONArray buildings)
            throws JSONException {

        JSONObject currentBuilding;

        for (int i = 0; i < buildings.length(); i++) {
            int lastIndex = this.studySpaces.size();

            currentBuilding = buildings.getJSONObject(i);

            double latitude = currentBuilding.getDouble("latitude");
            double longitude = currentBuilding.getDouble("longitude");
            String buildingName = currentBuilding.getString("name");

            createStudySpacesFromRoomKinds(currentBuilding
                    .getJSONArray("roomkinds"));

            StudySpace currentStudySpace;
            while (lastIndex < this.studySpaces.size()) {
                currentStudySpace = this.studySpaces.get(lastIndex);
                currentStudySpace.setLatitude(latitude).setLongitude(longitude)
                        .setBuildingName(buildingName);

                ++lastIndex;
            }
        }
    }

    private void createStudySpacesFromRoomKinds(JSONArray roomKinds)
            throws JSONException {

        JSONObject currentRoomKind;
        JSONArray currentRooms;

        for (int j = 0; j < roomKinds.length(); j++) {

            currentRoomKind = roomKinds.getJSONObject(j);
            currentRooms = currentRoomKind.getJSONArray("rooms");
            Room[] rooms = new Room[currentRooms.length()];

            for (int roomIndex = 0; roomIndex < rooms.length; roomIndex++) {
                rooms[roomIndex] = createRoomFromJSONObject(currentRooms
                        .getJSONObject(roomIndex));
            }

            StudySpace temp = new StudySpace()
                    .setSpaceName(currentRoomKind.getString("name"))
                    .setMaxOccupancy(currentRoomKind.getInt("max_occupancy"))
                    .setHasWhiteboard(
                            currentRoomKind.getBoolean("has_whiteboard"))
                    .setPrivacy(currentRoomKind.getString("privacy"))
                    .setHasComputer(currentRoomKind.getBoolean("has_computer"))
                    .setReserveType(currentRoomKind.getString("reserve_type"))
                    .setHasBigScreen(
                            currentRoomKind.getBoolean("has_big_screen"))
                    .setComments(currentRoomKind.getString("comments"))
                    .setNumRooms(currentRooms.length()).setRooms(rooms);

            studySpaces.add(temp);
        }
    }

    private Room createRoomFromJSONObject(JSONObject roomJSON)
            throws JSONException {
        return new Room(roomJSON.getInt("id"), roomJSON.getString("name"),
                roomJSON.getJSONObject("availabilities"));
    }

    public ArrayList<StudySpace> getStudySpaces() {
        if (System.currentTimeMillis() - lastAccess > REFRESH_INTERVAL) {
            this.buildStudySpaces();
        }
        return this.studySpaces;
    }

}
