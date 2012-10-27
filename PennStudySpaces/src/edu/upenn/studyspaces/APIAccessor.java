package edu.upenn.studyspaces;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import android.location.Location;
import android.util.Log;

public class APIAccessor {

    private static final int MAX_CACHE_SIZE = 5000;
    // public static JSONObject availabilities;
    private static Map<String, Integer> cachedDistanceRequest = new HashMap<String, Integer>();
    private static final String directionAPIPrefix = "http://maps.googleapis.com/maps/api/directions/json?";

    public static ArrayList<StudySpace> getStudySpaces() throws Exception {

        String _url = "http://www.pennstudyspaces.com/api?showall=1&format=json";

        BufferedReader reader = new BufferedReader(new InputStreamReader(
                new URL(_url).openStream()));

        String line = reader.readLine();

        JSONObject json_obj = new JSONObject(line);

        JSONArray buildings_arr = json_obj.getJSONArray("buildings");

        ArrayList<StudySpace> study_spaces = new ArrayList<StudySpace>();

        for (int i = 0; i < buildings_arr.length(); i++) {

            JSONArray roomkinds_arr = buildings_arr.getJSONObject(i)
                    .getJSONArray("roomkinds");

            for (int j = 0; j < roomkinds_arr.length(); j++) {

                JSONArray t = roomkinds_arr.getJSONObject(j).getJSONArray(
                        "rooms");
                Room[] rooms = new Room[t.length()];

                for (int k = 0; k < t.length(); k++) {
                    /*
                     * System.out.println(t.getJSONObject(k).getInt("id"));
                     * System.out.println(t.getJSONObject(k).getString("name"));
                     * System
                     * .out.println(roomkinds_arr.getJSONObject(j).getString
                     * ("name"));
                     * System.out.println(t.getJSONObject(k).getJSONObject
                     * ("availabilities").getJSONArray("2012-03-27"));
                     * System.out.println(); availabilities =
                     * t.getJSONObject(k).getJSONObject("availabilities");
                     * System.out.println(availableNow());
                     * //System.out.println(availabilities); //JSONArray ja =
                     * availabilities.getJSONArray("2012-04-08"); if(ja.get(1)
                     * == null) {
                     * System.out.println("No JSONArray at index 1!"); } else {
                     * System.out.println(ja.get(1)); }
                     */
                    Room temp = new Room(t.getJSONObject(k).getInt("id"), t
                            .getJSONObject(k).getString("name"), t
                            .getJSONObject(k).getJSONObject("availabilities"));
                    rooms[k] = temp;
                    /*
                     * System.out.println(rooms[k].getRoomName());
                     * System.out.println(rooms[k].getID());
                     * System.out.println();
                     */
                }

                /*
                 * System.out.println(roomkinds_arr.getJSONObject(j).get(
                 * "max_occupancy")); System.out.println(t.length());
                 * System.out.println(roomkinds_arr.getJSONObject(j));
                 * System.out
                 * .println(roomkinds_arr.getJSONObject(j).getString("name"));
                 * System
                 * .out.println(buildings_arr.getJSONObject(i).getString("name"
                 * ));
                 */

                StudySpace temp = new StudySpace(roomkinds_arr.getJSONObject(j)
                        .getString("name"), buildings_arr.getJSONObject(i)
                        .getDouble("latitude"), buildings_arr.getJSONObject(i)
                        .getDouble("longitude"), t.length(), buildings_arr
                        .getJSONObject(i).getString("name"), roomkinds_arr
                        .getJSONObject(j).getInt("max_occupancy"),
                        roomkinds_arr.getJSONObject(j).getBoolean(
                                "has_whiteboard"), roomkinds_arr.getJSONObject(
                                j).getString("privacy"), roomkinds_arr
                                .getJSONObject(j).getBoolean("has_computer"),
                        roomkinds_arr.getJSONObject(j)
                                .getString("reserve_type"), roomkinds_arr
                                .getJSONObject(j).getBoolean("has_big_screen"),
                        roomkinds_arr.getJSONObject(j).getString("comments"),
                        rooms);

                study_spaces.add(temp);
            }

        }
        return study_spaces;
    }

    public static void sortStudySpaceListByWalkingDistance(
            ArrayList<StudySpace> studySpacesList, Location location) {
        
        // flush the cache if needed
        if (cachedDistanceRequest.size() > MAX_CACHE_SIZE) {
            cachedDistanceRequest = new HashMap<String, Integer>();
        }

        //double currentLat = location.getLatitude();
        double currentLat = 39.95233d;
        //double currentLong = location.getLongitude();
        double currentLong = -75.1906d;

        try {
            for (StudySpace oneStudySpace : studySpacesList) {
                String requestUrl = directionAPIPrefix + "origin=" + currentLat
                        + "," + currentLong + "&destination="
                        + oneStudySpace.getSpaceLatitude() + ","
                        + oneStudySpace.getSpaceLongitude() + "&mode=walking&sensor=false";
                Integer cachedDistance = cachedDistanceRequest.get(requestUrl);

                if (cachedDistance == null) {
                    BufferedReader reader = new BufferedReader(
                            new InputStreamReader(
                                    new URL(requestUrl).openStream()));

                    StringBuilder recentResponse = new StringBuilder();
                    String oneLine = reader.readLine();
                    while (oneLine != null) {
                        recentResponse.append(oneLine);
                        oneLine = reader.readLine();
                    }
                    boolean allGood = false;

                    // start parsing response to get the distance
                    JSONObject jsonObj = new JSONObject(
                            recentResponse.toString());
                    JSONObject route = jsonObj.getJSONArray("routes")
                            .getJSONObject(0);
                    JSONObject leg = route.getJSONArray("legs")
                            .getJSONObject(0);
                    int distance = leg.getJSONObject("distance")
                            .getInt("value");
                    allGood = true;

                    if (allGood) {
                        cachedDistanceRequest.put(requestUrl, distance);
                        oneStudySpace
                                .setWalkingDistanceToCurrentGPSLocation(distance);
                    }
                } else {
                    oneStudySpace
                            .setWalkingDistanceToCurrentGPSLocation(cachedDistance);
                }
            }
            
            Comparator<StudySpace> c = new Comparator<StudySpace>() {
                @Override
                public int compare(StudySpace lhs, StudySpace rhs) {
                    int lhsDistance = lhs.getWalkingDistanceFromGPSLocation();
                    int rhsDistance = rhs.getWalkingDistanceFromGPSLocation();
                    if (lhsDistance != -1 && rhsDistance != -1) {
                        return lhsDistance - rhsDistance;
                    }
                    return 0;
                }
            };

            Collections.sort(studySpacesList, c);

        } catch (Exception e) {
            Log.e("BACKGROUND_PROC", "Problem getting walking distance" + e.getMessage());
        }
    }

}
