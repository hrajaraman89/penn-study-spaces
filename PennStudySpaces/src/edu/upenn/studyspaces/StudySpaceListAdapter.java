package edu.upenn.studyspaces;

import java.util.ArrayList;
import java.util.Date;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class StudySpaceListAdapter extends ArrayAdapter<StudySpace> {

    private ArrayList<StudySpace> list_items;
    private ArrayList<StudySpace> orig_items;
    private ArrayList<StudySpace> before_search;
    private ArrayList<StudySpace> fav_orig_items;
    private ArrayList<StudySpace> temp; // Store list items for when
                                        // favorites is displayed
    private Context context;
    private SearchOptions searchOptions;

    public StudySpaceListAdapter(Context context, int textViewResourceId,
            ArrayList<StudySpace> items, SearchOptions searchOptions) {
        super(context, textViewResourceId, items);
        this.list_items = items;
        this.orig_items = items;
        this.fav_orig_items = new ArrayList<StudySpace>();
        this.context = context;
        this.searchOptions = searchOptions;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        if (v == null) {
            LayoutInflater vi = (LayoutInflater) getContext()
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(R.layout.sslistitem, null);
        }
        // int index = getRealPosition(position);

        StudySpace o = list_items.get(position);
        if (o != null) {

            TextView tt = (TextView) v.findViewById(R.id.nametext);
            TextView bt = (TextView) v.findViewById(R.id.roomtext);
            if (tt != null) {
                tt.setText(o.getBuildingName() + " - " + o.getSpaceName());
            }
            if (bt != null) {
                if (o.getNumberOfRooms() == 1) {
                    bt.setText(o.getRooms()[0].getRoomName());
                } else {
                    bt.setText(o.getRooms()[0].getRoomName() + " (and "
                            + String.valueOf(o.getNumberOfRooms() - 1)
                            + " others)");
                }
            }
            ImageView image = (ImageView) v.findViewById(R.id.icon);
            int resID;
            if (image != null) {
                Resources resource = context.getResources();
                if (o.getBuildingType().equals(StudySpace.ENGINEERING))
                    resID = resource.getIdentifier("engiicon", "drawable",
                            context.getPackageName());
                else if (o.getBuildingType().equals(StudySpace.WHARTON))
                    resID = resource.getIdentifier("whartonicon",
                            "drawable", context.getPackageName());
                else if (o.getBuildingType().equals(StudySpace.LIBRARIES))
                    resID = resource.getIdentifier("libicon", "drawable",
                            context.getPackageName());
                else
                    resID = resource.getIdentifier("othericon", "drawable",
                            context.getPackageName());
                image.setImageResource(resID);
            }
            ImageView priv = (ImageView) v.findViewById(R.id.priv);
            ImageView wb = (ImageView) v.findViewById(R.id.wb);
            ImageView comp = (ImageView) v.findViewById(R.id.comp);
            ImageView proj = (ImageView) v.findViewById(R.id.proj);
            if (priv != null && o.getPrivacy().equals("S"))
                priv.setVisibility(View.INVISIBLE);
            else
                priv.setVisibility(View.VISIBLE);
            if (wb != null && !o.hasWhiteboard())
                wb.setVisibility(View.INVISIBLE);
            else
                wb.setVisibility(View.VISIBLE);
            if (comp != null && !o.hasComputer())
                comp.setVisibility(View.INVISIBLE);
            else
                comp.setVisibility(View.VISIBLE);
            if (proj != null && !o.has_big_screen())
                proj.setVisibility(View.INVISIBLE);
            else
                proj.setVisibility(View.VISIBLE);
        }
        return v;
    }

    @Override
    public int getCount() {
        return list_items.size();
    }

    @Override
    public StudySpace getItem(int position) {
        return list_items.get(position);
    }

    public void filterSpaces() {

        ArrayList<StudySpace> filtered = (ArrayList<StudySpace>) orig_items
                .clone();

        int i = 0;
        while (i < filtered.size()) {
            if (!searchOptions.getEngi()
                    && filtered.get(i).getBuildingType()
                            .equals(StudySpace.ENGINEERING)) {
                filtered.remove(i);
                continue;
            }
            if (!searchOptions.getWhar()
                    && filtered.get(i).getBuildingType()
                            .equals(StudySpace.WHARTON)) {
                filtered.remove(i);
                continue;
            }
            if (!searchOptions.getLib()
                    && filtered.get(i).getBuildingType()
                            .equals(StudySpace.LIBRARIES)) {
                filtered.remove(i);
                continue;
            }
            if (!searchOptions.getOth()
                    && filtered.get(i).getBuildingType()
                            .equals(StudySpace.OTHER)) {
                filtered.remove(i);
                continue;
            }
            if (searchOptions.getPrivate()
                    && filtered.get(i).getPrivacy().equals("S")) {
                filtered.remove(i);
                continue;
            }
            if (searchOptions.getWhiteboard()
                    && !filtered.get(i).hasWhiteboard()) {
                filtered.remove(i);
                continue;
            }
            if (searchOptions.getComputer()
                    && !filtered.get(i).hasComputer()) {
                filtered.remove(i);
                continue;
            }
            if (searchOptions.getProjector()
                    && !filtered.get(i).has_big_screen()) {
                filtered.remove(i);
                continue;
            }
            i++;
        }

        // this.list_items = filtered;

        this.list_items = SpaceInfo.sortByRank(filtered);
        this.list_items = filterByPeople(list_items);
        this.list_items = filterByDate(list_items);
        this.before_search = (ArrayList<StudySpace>) this.list_items
                .clone();

        notifyDataSetChanged();
    }

    public void searchNames(String query) {
        query = query.toLowerCase();
        this.list_items = (ArrayList<StudySpace>) this.before_search
                .clone();
        if (!query.equals("")) {
            for (int i = list_items.size() - 1; i >= 0; i--) {
                StudySpace s = list_items.get(i);
                if (s.getBuildingName().toLowerCase().indexOf(query) >= 0
                        || s.getSpaceName().toLowerCase().indexOf(query) >= 0
                        || s.getRoomNames().toLowerCase().indexOf(query) >= 0) {

                } else {
                    list_items.remove(i);
                }
            }
        }
        notifyDataSetChanged();
    }

    // switch to favorites
    public void allToFav() {
        this.temp = this.list_items; // remember list items
        this.list_items = fav_orig_items;
        notifyDataSetChanged();
    }

    public void favToAll() {
        this.list_items = this.temp; // restore list items
        notifyDataSetChanged();
    }

    public void updateFavorites(Preferences p) {
        this.fav_orig_items = SpaceInfo.sortByRank(this.orig_items);
        for (int i = fav_orig_items.size() - 1; i >= 0; i--) {
            if (!p.isFavorite(fav_orig_items.get(i).getBuildingName()
                    + fav_orig_items.get(i).getSpaceName()))
                fav_orig_items.remove(i);
        }
    }
    
    public ArrayList<StudySpace> filterByDate(ArrayList<StudySpace> arr) {
        Date d1 = searchOptions.getStartDate();
        Date d2 = searchOptions.getEndDate();
        // d1 = new Date(112, 3, 7, 15, 0);
        // d2 = new Date(112, 3, 9, 23, 0);
        // d2.setHours(d2.getHours()+1);
        // Log.e("date1", d1.toString());
        // Log.e("date2", d2.toString());
        for (int i = arr.size() - 1; i >= 0; i--) {
            boolean flag = false;
            for (Room r : arr.get(i).getRooms()) {
                try {
                    if (r.searchAvailability(d1, d2)) {
                        flag = true;
                    }
                } catch (Exception e) {
                    // Log.e("exception","here");
                    // arr.remove(i); shouldn't be here
                }
            }

            if (!flag)
                arr.remove(i);
        }
        return arr;
    }

    public ArrayList<StudySpace> filterByPeople(ArrayList<StudySpace> arr) {
        for (int i = arr.size() - 1; i >= 0; i--) {
            if (arr.get(i).getMaximumOccupancy() < searchOptions
                    .getNumberOfPeople())
                arr.remove(i);
        }
        return arr;
    }
}
