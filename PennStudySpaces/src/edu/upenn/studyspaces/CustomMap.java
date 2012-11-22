package edu.upenn.studyspaces;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

public class CustomMap extends MapActivity {

    public static final String LIST_SIZE = "LIST_SIZE";
    public static final String STUDYSPACE = "STUDYSPACE";

    LinearLayout linearLayout;
    MapView mapView;
    MapController mc;
    Map<GeoPoint, List<StudySpace>> studySpacePointsToSpaceList;
    GeoPoint currentLocationGeoPoint;
    GeoPoint avg;
    List<Overlay> mapOverlays;
    Drawable buildingPinDrawable;
    Drawable currentLocationPinDrawable;
    PinOverlay buildingPins;
    PinOverlay currentLocationPin;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent i = super.getIntent();

        double avgLong = 0;
        double avgLat = 0;

        int studySpaceCount = i.getIntExtra(LIST_SIZE, 0);
        studySpacePointsToSpaceList = new HashMap<GeoPoint, List<StudySpace>>();
        StudySpace[] studySpaceList = new StudySpace[studySpaceCount];
        for (int j = 0; j < studySpaceList.length; j++) {
            StudySpace studySpace = (StudySpace) i
                    .getSerializableExtra(STUDYSPACE + j);

            double longitude = studySpace.getLongitude();
            double latitude = studySpace.getLatitude();

            avgLat += latitude;
            avgLong += longitude;

            GeoPoint studySpacePoint = new GeoPoint((int) (latitude * 1E6),
                    (int) (longitude * 1E6));
            List<StudySpace> slist = studySpacePointsToSpaceList
                    .get(studySpacePoint);
            if (slist == null) {
                slist = new ArrayList<StudySpace>();
                studySpacePointsToSpaceList.put(studySpacePoint, slist);
            }
            slist.add(studySpace);
        }

        setContentView(R.layout.mapview);
        mapView = (MapView) findViewById(R.id.mapview);
        mapView.setBuiltInZoomControls(true);

        buildingPinDrawable = this.getResources().getDrawable(
                R.drawable.pushpin);
        buildingPins = new PinOverlay(buildingPinDrawable);

        mc = mapView.getController();

        LocationManager locationManager = (LocationManager) this
                .getSystemService(Context.LOCATION_SERVICE);

        Location location = getLocationAndScheduleUpdate(locationManager);

        if (location != null) {
            double gpsLat = location.getLatitude();
            double gpsLong = location.getLongitude();

            avgLat += gpsLat;
            avgLat /= (studySpaceCount + 1);
            avgLong += gpsLong;
            avgLong /= (studySpaceCount + 1);

            currentLocationGeoPoint = new GeoPoint((int) (gpsLat * 1E6),
                    (int) (gpsLong * 1E6));

            currentLocationPinDrawable = this.getResources().getDrawable(
                    R.drawable.bluepin);
            currentLocationPin = new PinOverlay(currentLocationPinDrawable);

            OverlayItem overlayitem = new OverlayItem(currentLocationGeoPoint,
                    "", "");
            currentLocationPin.addOverlay(overlayitem);
        } else {
            avgLat /= (studySpaceCount);
            avgLong /= (studySpaceCount);
        }

        /*
         * MapOverlay mapOverlay = new MapOverlay(); List<Overlay>
         * listOfOverlays = mapView.getOverlays(); listOfOverlays.clear();
         * listOfOverlays.add(mapOverlay);
         */

        for (GeoPoint oneGeoPoint : studySpacePointsToSpaceList.keySet()) {
            OverlayItem overlayitem = new OverlayItem(oneGeoPoint, " ",
                    studySpacePointsToSpaceList.get(oneGeoPoint).toString());
            buildingPins.addOverlay(overlayitem);
        }

        mapOverlays = mapView.getOverlays();
        mapOverlays.add(buildingPins);
        if (currentLocationPin != null) {
            mapOverlays.add(currentLocationPin);
        }

        avg = new GeoPoint((int) (avgLat * 1E6), (int) (avgLong * 1E6));

        if (studySpacePointsToSpaceList.size() != 0) {
            // no location to display, let Google handle it
            mc.animateTo(avg);
        }
        mc.setZoom(17);
    }

    private Location getLocationAndScheduleUpdate(
            LocationManager locationManager) {
        Criteria _criteria = new Criteria();
        // _criteria.setAccuracy(Criteria.ACCURACY_LOW);
        PendingIntent _pIntent = PendingIntent.getBroadcast(
                getApplicationContext(), 0, getIntent(), 0);
        try {
            locationManager.requestSingleUpdate(_criteria, _pIntent);
        } catch (IllegalArgumentException e) {
            Log.e("CustomMap", "GPS probably turned off", e);
        }

        String _bestProvider = locationManager.getBestProvider(_criteria, true);
        if (_bestProvider != null) {
            Location location = locationManager
                    .getLastKnownLocation(_bestProvider);

            LocationListener loc_listener = new LocationListener() {
                public void onLocationChanged(Location l) {
                }

                public void onProviderEnabled(String p) {
                }

                public void onProviderDisabled(String p) {
                }

                public void onStatusChanged(String p, int status, Bundle extras) {
                }
            };
            locationManager.requestLocationUpdates(_bestProvider, 0, 0,
                    loc_listener);
            location = locationManager.getLastKnownLocation(_bestProvider);
            return location;
        }
        return null;
    }

    /*
     * class MapOverlay extends Overlay {
     * 
     * @Override public boolean draw(Canvas canvas, MapView mapView, boolean
     * shadow, long when) { super.draw(canvas, mapView, shadow);
     * 
     * //---translate the GeoPoint to screen pixels--- Point screenPts = new
     * Point(); mapView.getProjection().toPixels(p, screenPts);
     * 
     * //---add the marker--- Bitmap bmp = BitmapFactory.decodeResource(
     * getResources(), R.drawable.pushpin); //Positions the image
     * canvas.drawBitmap(bmp, screenPts.x-10, screenPts.y-34, null); return
     * true; } }
     */

    @Override
    protected boolean isRouteDisplayed() {
        return false;
    }

    public class PinOverlay extends ItemizedOverlay<OverlayItem> {

        private ArrayList<OverlayItem> mOverlays = new ArrayList<OverlayItem>();

        public PinOverlay(Drawable defaultMarker) {
            super(boundCenterBottom(defaultMarker));
        }

        public void addOverlay(OverlayItem overlay) {
            mOverlays.add(overlay);
            populate();
        }

        @Override
        protected OverlayItem createItem(int i) {
            return mOverlays.get(i);
        }

        @Override
        public int size() {
            return mOverlays.size();
        }

        @Override
        protected boolean onTap(int index) {
            OverlayItem overlay = mOverlays.get(index);
            GeoPoint p = overlay.getPoint();

            Geocoder geoCoder = new Geocoder(getBaseContext(),
                    Locale.getDefault());
            String add = "";
            try {
                List<Address> addresses = geoCoder.getFromLocation(
                        p.getLatitudeE6() / 1E6, p.getLongitudeE6() / 1E6, 1);

                if (addresses.size() > 0) {
                    for (int i = 0; i < addresses.get(0)
                            .getMaxAddressLineIndex(); i++)
                        add += addresses.get(0).getAddressLine(i) + "\n";
                }
            } catch (IOException e) {
                Log.e("CustomMap", "Getting address problem", e);
            }

            List<StudySpace> slist = studySpacePointsToSpaceList.get(p);
            if (slist != null && slist.size() > 0) {
                String buildingName = slist.get(0).getBuildingName();
                int nRoom = slist.size();
                String roomCount;
                if (nRoom == 1) {
                    roomCount = " (1 matching room)";
                } else {
                    roomCount = " (" + nRoom + " matching rooms)";
                }
                Toast.makeText(getBaseContext(),
                        buildingName + roomCount + "\n\n" + add,
                        Toast.LENGTH_LONG * 3).show();
            } else {
                Toast.makeText(getBaseContext(), add, Toast.LENGTH_LONG * 3)
                        .show();
            }
            return true;
        }

        @Override
        public boolean onTap(GeoPoint p, MapView v) {
            return super.onTap(p, v);
        }

    }

}
