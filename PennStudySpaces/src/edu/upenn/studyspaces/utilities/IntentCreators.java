package edu.upenn.studyspaces.utilities;

import java.util.Calendar;

import android.content.Intent;
import android.net.Uri;
import edu.upenn.studyspaces.StudySpace;

public class IntentCreators {

    public static Intent getCalIntent(StudySpace space) {
        Calendar cal = Calendar.getInstance();
        Intent intent = new Intent(Intent.ACTION_EDIT)
                .setType("vnd.android.cursor.item/event")
                .putExtra("beginTime", cal.getTimeInMillis())
                .putExtra("endTime", cal.getTimeInMillis() + 60 * 60 * 1000)
                .putExtra("title", "PennStudySpaces Reservation")
                .putExtra(
                        "eventLocation",
                        space.getBuildingName() + " - "
                                + space.getRooms()[0].getRoomName());
        return intent;
    }

    public static Intent getReserveIntent(StudySpace space) {
        Intent intent = null;

        if (space.getBuildingType().equals(StudySpace.WHARTON)) {
            intent = new Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://spike.wharton.upenn.edu/Calendar/gsr.cfm?"));
        } else if (space.getBuildingType().equals(StudySpace.ENGINEERING)) {
            intent = new Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://weblogin.pennkey.upenn.edu/login/?factors=UPENN.EDU&cosign-seas-www_userpages-1&https://www.seas.upenn.edu/about-seas/room-reservation/form.php"));
        } else if (space.getBuildingType().equals(StudySpace.LIBRARIES)) {
            intent = new Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://weblogin.library.upenn.edu/cgi-bin/login?authz=grabit&app=http://bookit.library.upenn.edu/cgi-bin/rooms/rooms"));
        }
        return intent;
    }
}
