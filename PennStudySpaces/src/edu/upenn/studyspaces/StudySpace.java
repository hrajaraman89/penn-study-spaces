package edu.upenn.studyspaces;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONObject;

public class StudySpace implements Serializable {
    /**
	 * 
	 */
    private static final long serialVersionUID = 1L;

    // Constants, need to be public
    public final static String ENGINEERING = "Engineering";
    public final static String WHARTON = "Wharton";
    public final static String LIBRARIES = "Libraries";
    public final static String OTHER = "Other";

    // Attributes
    private String buildingName;
    private String spaceName;
    private double latitude;
    private double longitude;
    private int walkingDistanceFromCurrentGPSLocation = -1; // in meters
    private int number_of_rooms;
    private int max_occupancy;
    private boolean has_whiteboard;
    private String privacy;
    private boolean has_computer;
    private String reserve_type;
    private boolean has_big_screen;
    private String comments;
    private Room[] rooms;
    private String[] foursquare;

    public StudySpace(String name, double lat, double lon, int num_rooms,
            String b_name, int max_occ, boolean has_wh, String pri,
            boolean has_comp, String res_type, boolean has_big_s, String comm,
            Room[] r) {
        spaceName = name;
        latitude = lat;
        longitude = lon;
        number_of_rooms = num_rooms;
        buildingName = b_name;
        max_occupancy = max_occ;
        has_whiteboard = has_wh;
        privacy = pri;
        has_computer = has_comp;
        reserve_type = res_type;
        has_big_screen = has_big_s;
        comments = comm;
        rooms = r;
    }

    public String[] getFoursquare() {
        if (foursquare == null) {
            Date d = new Date();
            int year = (d.getYear() + 1900);
            int month = (d.getMonth() + 1);
            String m = "";
            String da = "";
            if (month < 10) {
                m = "0" + month;
            } else {
                m = "" + month;
            }
            int day = d.getDate();
            if (day < 10) {
                da = "0" + day;
            } else {
                da = "" + day;
            }
            String date = year + "" + m + "" + da;

            try {
                String _url = "https://api.foursquare.com/v2/tips/search?ll="
                        + latitude
                        + ","
                        + longitude
                        + "&client_id=0XRPWALWWL4LCZ5DSWKU3UZ0GVGDCMKGADYSRPZYRLIVCPUM&client_secret=JWUKQD0HOKZGO4SBCR0N3NDCI40FTDFN03SS1W45AMRN5BBU&v="
                        + date;

                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(new URL(_url).openStream()));

                String line = reader.readLine();

                JSONObject json_obj = new JSONObject(line);

                JSONObject responses = (JSONObject) json_obj.get("response");
                JSONArray tips = responses.getJSONArray("tips");

                String[] tips_text = new String[tips.length()];

                for (int i = 0; i < tips.length(); i++) {
                    JSONObject text = tips.getJSONObject(i);
                    tips_text[i] = text.getString("text");
                    System.out.println(tips_text[i]);
                }

                foursquare = tips_text;
                return tips_text;
            } catch (Exception e) {
                return new String[0];
            }
        } else {
            return foursquare;
        }
    }

    public String getSpaceName() {
        return spaceName;
    }

    public double getSpaceLatitude() {
        return latitude;
    }

    public double getSpaceLongitude() {
        return longitude;
    }

    public int getWalkingDistanceFromGPSLocation() {
        return walkingDistanceFromCurrentGPSLocation;
    }
    
    public int getNumberOfRooms() {
        return number_of_rooms;
    }

    public String getBuildingName() {
        return buildingName;
    }

    public int getMaximumOccupancy() {
        return max_occupancy;
    }

    public boolean hasWhiteboard() {
        return has_whiteboard;
    }

    public String getPrivacy() {
        return privacy;
    }

    public boolean hasComputer() {
        return has_computer;
    }

    public String getReserveType() {
        return reserve_type;
    }

    public boolean has_big_screen() {
        return has_big_screen;
    }

    public String getComments() {
        return comments;
    }

    public Room[] getRooms() {
        return rooms;
    }

    // get the list of roomNames as a string
    public String getRoomNames() {

        // GSR has a lot of rooms so it's being formatted differently
        if (getSpaceName().equals("GSR"))
            return getGSRNames();
        else {
            String out = "";
            for (Room r : getRooms())
                out = out + r.getRoomName() + " ";
            return out;
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

        if (buildingName.equals("Towne Building")
                || buildingName.equals("Levine Hall")
                || buildingName.equals("Skirkanich Hall")) {
            return ENGINEERING;
        } else if (buildingName.equals("Jon M. Huntsman Hall")) {
            return WHARTON;
        } else if (buildingName.equals("Van Pelt Library")
                || buildingName.equals("Biomedical Library")
                || buildingName.equals("Lippincott Library")
                || buildingName.equals("Museum Library")) {
            return LIBRARIES;
        } else {
            return OTHER;
        }
    }

    public void setWalkingDistanceToCurrentGPSLocation(int distance) {
        this.walkingDistanceFromCurrentGPSLocation = distance;
    }
}
