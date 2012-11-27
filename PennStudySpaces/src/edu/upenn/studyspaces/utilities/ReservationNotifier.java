package edu.upenn.studyspaces.utilities;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import android.content.Intent;
import edu.upenn.studyspaces.StudySpace;

public class ReservationNotifier {

    private static final String SUBJECT = "StudySpace Reservation";
    private static final String TEXT_FORMAT = "Building:\t%s\n" + "Room:\t%s\n"
            + "Start:\t%s\n" + "End:\t%s\n" + "Maps:\t%s";
    private static final String MAP_URI_FORMAT = "https://maps.google.com/maps?q=%s,%s";
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat(
            "EEE, d MMM yyyy HH:mm:ss");

    private static final String TYPE = "text/plain";

    public Intent getSharingIntent(Calendar timeBegin, Calendar timeEnd,
            StudySpace space) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType(TYPE);
        intent.putExtra(Intent.EXTRA_SUBJECT, SUBJECT);
        intent.putExtra(Intent.EXTRA_TEXT, getText(timeBegin, timeEnd, space));

        return intent;
    }

    private String getText(Calendar timeBegin, Calendar timeEnd,
            StudySpace space) {
        return String.format(TEXT_FORMAT, space.getBuildingName(),
                space.getSpaceName(), getTimeAsString(timeBegin),
                getTimeAsString(timeEnd),
                getMapUrl(space.getLatitude(), space.getLongitude()));
    }

    private String getTimeAsString(Calendar time) {
        return DATE_FORMAT.format(time.getTime());
    }

    private String getMapUrl(double latitude, double longitude) {
        return String.format(MAP_URI_FORMAT, latitude, longitude);
    }
}