package edu.upenn.studyspaces;

import java.io.Serializable;
import java.util.ArrayList;

public class StudySpace implements Serializable {

    private static final long serialVersionUID = 1L;

    // Constants, need to be public
    public final static String ENGINEERING = "Engineering";
    public final static String WHARTON = "Wharton";
    public final static String LIBRARIES = "Libraries";
    public final static String OTHER = "Other";

    // Attributes
    private String buildingName;
    private String spaceName;
    private String privacy;
    private String reserveType;
    private String comments;

    private double latitude;
    private double longitude;

    private int walkingDistanceFromCurrentGPSLocation = -1; // in meters

    private int numRooms;
    private int maxOccupancy;

    private boolean hasWhiteboard;
    private boolean hasComputer;
    private boolean hasBigScreen;

    private Room[] rooms;

    private String roomNames;

    public StudySpace() {

    }

    public double getLatitude() {
        return latitude;
    }

    public StudySpace setLatitude(double latitude) {
        this.latitude = latitude;
        return this;
    }

    public double getLongitude() {
        return longitude;
    }

    public StudySpace setLongitude(double longitude) {
        this.longitude = longitude;
        return this;
    }

    public int getNumRooms() {
        return numRooms;
    }

    public StudySpace setNumRooms(int numRooms) {
        this.numRooms = numRooms;
        return this;
    }

    public int getMaxOccupancy() {
        return maxOccupancy;
    }

    public StudySpace setMaxOccupancy(int maxOccupancy) {
        this.maxOccupancy = maxOccupancy;
        return this;
    }

    public boolean getHasWhiteboard() {
        return hasWhiteboard;
    }

    public StudySpace setHasWhiteboard(boolean hasWhiteboard) {
        this.hasWhiteboard = hasWhiteboard;
        return this;
    }

    public boolean getHasComputer() {
        return hasComputer;
    }

    public StudySpace setHasComputer(boolean hasComputer) {
        this.hasComputer = hasComputer;
        return this;
    }

    public boolean getHasBigScreen() {
        return hasBigScreen;
    }

    public StudySpace setHasBigScreen(boolean hasBigScreen) {
        this.hasBigScreen = hasBigScreen;
        return this;
    }

    public String getSpaceName() {
        return spaceName;
    }

    public StudySpace setSpaceName(String spaceName) {
        this.spaceName = spaceName;
        return this;
    }

    public String getPrivacy() {
        return privacy;
    }

    public StudySpace setPrivacy(String privacy) {
        this.privacy = privacy;
        return this;
    }

    public String getComments() {
        return comments;
    }

    public StudySpace setComments(String comments) {
        this.comments = comments;
        return this;
    }

    public Room[] getRooms() {
        return rooms;
    }

    public StudySpace setRooms(Room[] rooms) {
        this.rooms = rooms;
        return this;
    }

    public int getWalkingDistanceFromGPSLocation() {
        return walkingDistanceFromCurrentGPSLocation;
    }

    public StudySpace setWalkingDistanceToCurrentGPSLocation(int distance) {
        this.walkingDistanceFromCurrentGPSLocation = distance;
        return this;
    }

    public String getBuildingName() {
        return buildingName;
    }

    public StudySpace setBuildingName(String buildingName) {
        this.buildingName = buildingName;
        return this;
    }

    public boolean hasWhiteboard() {
        return hasWhiteboard;
    }

    public boolean isReservable() {
        return !"N".equals(reserveType);
    }

    public String getReserveType() {
        return reserveType;
    }

    public StudySpace setReserveType(String reserveType) {
        this.reserveType = reserveType;
        return this;
    }

    // get the list of roomNames as a string
    public String getRoomNames() {

        // GSR has a lot of rooms so it's being formatted differently
        if (getSpaceName().equals("GSR"))
            return getGSRNames();
        else {
            if (this.roomNames == null) {
                StringBuilder builder = new StringBuilder();
                for (Room room : getRooms()) {
                    builder.append(room.getRoomName() + " ");
                }

                this.roomNames = builder.toString();
            }

            return this.roomNames;
        }
    }

    public String getGSRNames() {
        ArrayList<Integer> F = new ArrayList<Integer>();
        ArrayList<Integer> G = new ArrayList<Integer>();
        ArrayList<Integer> sec = new ArrayList<Integer>();
        ArrayList<Integer> third = new ArrayList<Integer>();
        ArrayList<String> oth = new ArrayList<String>();

        String out = "";

        for (Room r : getRooms()) {
            char floor = r.getRoomName().charAt(0);

            int num = Integer.parseInt(r.getRoomName().substring(1));

            if (floor == 'F')
                F.add(num);
            else if (floor == 'G')
                G.add(num);
            else if (floor == '2')
                sec.add(num);
            else if (floor == '3')
                third.add(num);
            else
                oth.add(r.getRoomName());
        }

        out = out + sortToString(F, "F") + "\n\n"; // string builders?
        out = out + sortToString(G, "G") + "\n\n";
        out = out + sortToString(sec, "2") + "\n\n";
        out = out + sortToString(third, "3") + "\n";
        for (String s : oth)
            out = out + s + " ";
        return out;
    }

    private String sortToString(ArrayList<Integer> arr, String floor) {
        // Counting Sort
        int[] C = new int[100];
        ArrayList<Integer> S = new ArrayList<Integer>();

        for (int i = 0; i < arr.size(); i++) {
            C[arr.get(i)]++;
            S.add(null);
        }

        for (int k = 1; k <= 99; k++) {
            C[k] += C[k - 1];
        }

        for (int j = arr.size() - 1; j >= 0; j--) {
            S.set(C[arr.get(j)] - 1, arr.get(j));
            C[arr.get(j)]--; // Should never have duplicates
        }

        String out = "";

        for (int i : S) {
            out = out + floor + Integer.toString(i) + " ";
        }
        return out;
    }

    public String getBuildingType() {

        if ("Towne Building".equals(buildingName)
                || "Levine Hall".equals(buildingName)
                || "Skirkanich Hall".equals(buildingName)) {
            return ENGINEERING;
        } else if ("Jon M. Huntsman Hall".equals(buildingName)) {
            return WHARTON;
        } else if ("Van Pelt Library".equals(buildingName)
                || "Biomedical Library".equals(buildingName)
                || "Lippincott Library".equals(buildingName)
                || "Museum Library".equals(buildingName)) {
            return LIBRARIES;
        } else {
            return OTHER;
        }
    }
}
