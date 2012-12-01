package edu.upenn.studyspaces;

import java.util.Calendar;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import edu.upenn.studyspaces.utilities.ReservationNotifier;

public class TabDetails extends Fragment {

    private StudySpace o;
    private Preferences p;
    private ReservationNotifier notifier;
    private CheckBox favorite;
    private SharedPreferences favorites;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.tabdetails, container, false);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        notifier = new ReservationNotifier();

        Intent i = getActivity().getIntent();
        o = (StudySpace) i.getSerializableExtra("STUDYSPACE");
        p = (Preferences) i.getSerializableExtra("PREFERENCES");

        favorites = getActivity().getSharedPreferences(
                StudySpaceListActivity.FAV_PREFERENCES, 0);

        TextView tt = (TextView) getView().findViewById(R.id.spacename);
        tt.setText(o.getBuildingName());

        TextView rt = (TextView) getView().findViewById(R.id.roomtype);
        rt.setText(o.getSpaceName());

        TextView rn = (TextView) getView().findViewById(R.id.roomnumbers);
        rn.setText(o.getRoomNames());

        TextView mo = (TextView) getView().findViewById(R.id.maxoccupancy);
        mo.setText("Maximum occupancy: " + o.getMaxOccupancy());

        TextView pi = (TextView) getView().findViewById(R.id.privacy);
        ImageView private_icon = (ImageView) getView().findViewById(
                R.id.private_icon);

        if (o.getPrivacy().equals("S")) {
            pi.setText("This study space is a common Space");
            if (private_icon != null) {
                Resources resource = getResources();
                int resID = resource.getIdentifier("icon_no_private",
                        "drawable", getActivity().getPackageName());
                private_icon.setImageResource(resID);
            }
        } else {
            pi.setText("Private space");
            if (private_icon != null) {
                Resources resource = getResources();
                int resID = resource.getIdentifier("icon_private", "drawable",
                        getActivity().getPackageName());
                private_icon.setImageResource(resID);
            }
        }

        TextView res = (TextView) getView().findViewById(R.id.reservetype);
        View calLayout = getView().findViewById(R.id.addCal);
        View resLayout = getView().findViewById(R.id.reserve);
        if (o.getReserveType().equals("N")) {
            res.setText("This study space is non-reservable.");
            calLayout.setVisibility(View.VISIBLE);
            resLayout.setVisibility(View.GONE);
        } else {
            res.setText("This study space can be reserved.");
            calLayout.setVisibility(View.GONE);
            resLayout.setVisibility(View.VISIBLE);
        }
        TextView wb = (TextView) getView().findViewById(R.id.whiteboard);
        ImageView wb_icon = (ImageView) getView().findViewById(
                R.id.whiteboard_icon);
        if (o.hasWhiteboard()) {
            if (wb_icon != null) {
                Resources resource = getResources();
                int resID = resource.getIdentifier("icon_whiteboard",
                        "drawable", getActivity().getPackageName());
                wb_icon.setImageResource(resID);
            }
            wb.setText("This study space has a whiteboard.");
        } else {
            wb.setText("This study space does not have a whiteboard.");
            if (wb_icon != null) {
                Resources resource = getResources();
                int resID = resource.getIdentifier("icon_no_whiteboard",
                        "drawable", getActivity().getPackageName());
                wb_icon.setImageResource(resID);
            }
        }

        TextView com = (TextView) getView().findViewById(R.id.computer);
        ImageView com_icon = (ImageView) getView().findViewById(
                R.id.computer_icon);
        if (o.getHasComputer()) {
            com.setText("This study space has a computer.");
            if (com_icon != null) {
                Resources resource = getResources();
                int resID = resource.getIdentifier("icon_computer", "drawable",
                        getActivity().getPackageName());
                com_icon.setImageResource(resID);
            }
        } else {
            com.setText("This study space does not have computers.");
            if (com_icon != null) {
                Resources resource = getResources();
                int resID = resource.getIdentifier("icon_no_computer",
                        "drawable", getActivity().getPackageName());
                com_icon.setImageResource(resID);
            }
        }

        TextView proj = (TextView) getView().findViewById(R.id.projector);
        ImageView proj_icon = (ImageView) getView().findViewById(
                R.id.projector_icon);
        if (o.getHasBigScreen()) {
            proj.setText("This study space has a big screen.");
            if (proj_icon != null) {
                Resources resource = getResources();
                int resID = resource.getIdentifier("icon_projector",
                        "drawable", getActivity().getPackageName());
                proj_icon.setImageResource(resID);
            }
        } else {
            proj.setText("This study space does not have a big screen.");
            if (proj_icon != null) {
                Resources resource = getResources();
                int resID = resource.getIdentifier("icon_no_projector",
                        "drawable", getActivity().getPackageName());
                proj_icon.setImageResource(resID);
            }
        }

        favorite = (CheckBox) getView().findViewById(R.id.favoriteCheckBox);
        favorite.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                    boolean isChecked) {
                SharedPreferences.Editor editor = favorites.edit();

                if (isChecked) {
                    p.addFavorites(o.getBuildingName() + o.getSpaceName()
                            + o.getRoomNames());

                    editor.putBoolean(o.getBuildingName() + o.getSpaceName()
                            + o.getRoomNames(), true);
                    ((StudySpaceDetails) getActivity()).removedFavorite = false;
                } else {
                    p.removeFavorites(o.getBuildingName() + o.getSpaceName()
                            + o.getRoomNames());

                    editor.putBoolean(o.getBuildingName() + o.getSpaceName()
                            + o.getRoomNames(), false);
                    ((StudySpaceDetails) getActivity()).removedFavorite = true;
                }
                editor.commit();

            }
        });

        // favorites
        if (p.isFavorite(o.getBuildingName() + o.getSpaceName()
                + o.getRoomNames())) {
            favorite.setChecked(true);
        } else {
            favorite.setChecked(false);
        }

        View an = (View) getView().findViewById(R.id.availablenow);
        if (an != null) {
            boolean availableNow = false;
            for (Room r : o.getRooms()) {
                try {
                    if (r.availableNow())
                        availableNow = true;
                } catch (Exception e) {
                    availableNow = false; // Calendar crashes
                }
            }
            if (availableNow)
                an.setVisibility(View.VISIBLE);
            else
                an.setVisibility(View.GONE);
        }
    }

    public Intent getReserveIntent(View v) {
        Intent k = null;
        if (o.getBuildingType().equals(StudySpace.WHARTON)) {
            k = new Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://spike.wharton.upenn.edu/Calendar/gsr.cfm?"));
        } else if (o.getBuildingType().equals(StudySpace.ENGINEERING)) {
            k = new Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://weblogin.pennkey.upenn.edu/login/?factors=UPENN.EDU&cosign-seas-www_userpages-1&https://www.seas.upenn.edu/about-seas/room-reservation/form.php"));
        } else if (o.getBuildingType().equals(StudySpace.LIBRARIES)) {
            k = new Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://weblogin.library.upenn.edu/cgi-bin/login?authz=grabit&app=http://bookit.library.upenn.edu/cgi-bin/rooms/rooms"));
        }
        return k;
    }

    public Intent getCalIntent(View v) {
        Calendar cal = Calendar.getInstance();
        Intent intent = new Intent(Intent.ACTION_EDIT)
                .setType("vnd.android.cursor.item/event")
                .putExtra("beginTime", cal.getTimeInMillis())
                .putExtra("endTime", cal.getTimeInMillis() + 60 * 60 * 1000)
                .putExtra("title", "PennStudySpaces Reservation")
                .putExtra(
                        "eventLocation",
                        o.getBuildingName() + " - "
                                + o.getRooms()[0].getRoomName());
        return intent;
    }

    public void onReserveClick(View v) {
        Intent k = getReserveIntent(v);
        if (k != null)
            startActivity(k);

    }

    public void onCalClick(View v) {
        Intent calIntent = getCalIntent(v);
        startActivity(calIntent);
    }

    public void onShareClick(View v) {
        Calendar start = Calendar.getInstance();
        Calendar end = Calendar.getInstance();
        end.add(Calendar.HOUR, 1);

        startActivity(notifier.getSharingIntent(start, end, o));
    }
}
