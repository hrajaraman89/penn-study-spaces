package edu.upenn.studyspaces;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import edu.upenn.studyspaces.utilities.IntentCreators;
import edu.upenn.studyspaces.utilities.ReservationNotifier;

public class TabDetails extends Fragment {

    private StudySpace studySpace;
    private Preferences preferences;
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
        studySpace = (StudySpace) i.getSerializableExtra("STUDYSPACE");
        preferences = (Preferences) i.getSerializableExtra("PREFERENCES");

        favorites = getActivity().getSharedPreferences(
                StudySpaceListActivity.FAV_PREFERENCES, 0);

        TextView tt = (TextView) getView().findViewById(R.id.spacename);
        tt.setText(studySpace.getBuildingName());

        TextView rt = (TextView) getView().findViewById(R.id.roomtype);
        rt.setText(studySpace.getSpaceName());

        TextView rn = (TextView) getView().findViewById(R.id.roomnumbers);
        rn.setText(studySpace.getRoomNames());

        TextView mo = (TextView) getView().findViewById(R.id.maxoccupancy);
        mo.setText("Maximum occupancy: " + studySpace.getMaxOccupancy());

        TextView pi = (TextView) getView().findViewById(R.id.privacy);
        ImageView private_icon = (ImageView) getView().findViewById(
                R.id.private_icon);

        if (studySpace.getPrivacy().equals("S")) {
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
        if (studySpace.getReserveType().equals("N")) {
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
        if (studySpace.hasWhiteboard()) {
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
        if (studySpace.getHasComputer()) {
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
        if (studySpace.getHasBigScreen()) {
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
                    preferences.addFavorites(studySpace.getBuildingName()
                            + studySpace.getSpaceName()
                            + studySpace.getRoomNames());

                    editor.putBoolean(
                            studySpace.getBuildingName()
                                    + studySpace.getSpaceName()
                                    + studySpace.getRoomNames(), true);
                    ((StudySpaceDetails) getActivity()).removedFavorite = false;
                } else {
                    preferences.removeFavorites(studySpace.getBuildingName()
                            + studySpace.getSpaceName()
                            + studySpace.getRoomNames());

                    editor.putBoolean(
                            studySpace.getBuildingName()
                                    + studySpace.getSpaceName()
                                    + studySpace.getRoomNames(), false);
                    ((StudySpaceDetails) getActivity()).removedFavorite = true;
                }
                editor.commit();

            }
        });

        // favorites
        if (preferences.isFavorite(studySpace.getBuildingName()
                + studySpace.getSpaceName() + studySpace.getRoomNames())) {
            favorite.setChecked(true);
        } else {
            favorite.setChecked(false);
        }

        View an = (View) getView().findViewById(R.id.availablenow);
        if (an != null) {
            boolean availableNow = false;
            for (Room r : studySpace.getRooms()) {
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

    public void onReserveClick(View v) {
        Intent k = IntentCreators.getReserveIntent(studySpace);
        if (k != null)
            startActivity(k);
        else {
            Toast.makeText(this.getActivity().getBaseContext(),
                    "Whoops! Something went wrong..", Toast.LENGTH_LONG).show();
        }

    }

    public void onCalClick(View v) {
        Intent calIntent = IntentCreators.getCalIntent(studySpace);
        startActivity(calIntent);
    }

    public void onShareClick(View v) {
        startActivity(notifier.getSharingIntent(studySpace));
    }
}
