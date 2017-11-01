package kurzo.yann.lab3;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DateFormat;

import kurzo.yann.lab3.commons.Profile;

/**
 * Created by Yann on 25/10/2017.
 */

public class ProfileListFragment extends ListFragment {

    // Tag for Logcat
    private static final String TAG = "ProfileListFragment";

    private View fragmentView;
    private ArrayAdapter adapter;

    // Database
    private MyFirebase mMyFirebase;

    public AlarmConfiguration mAlarmConfig;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        fragmentView = inflater.inflate(R.layout.fragment_list_layout, container, false);

        return fragmentView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Create list adapter, binding the array of data to the listView
        adapter = new ProfileAdapter(getActivity(), R.layout.row_layout);
        setListAdapter(adapter);

        // Set alarm configuration
        mAlarmConfig = new AlarmConfiguration(getContext());
    }

    @Override
    public void onResume() {
        super.onResume();

        // New Firebase listener
        mMyFirebase = new MyFirebase(this);
        mMyFirebase.addValueEventListener();
    }

    @Override
    public void onPause() {
        super.onPause();

        // Remove Firebase listener
        mMyFirebase.removeValueEventListener();
    }

    public void addProfile(Profile profile) {
        adapter.add(profile);
    }

    public void clearProfileList() {
        adapter.clear();
        mAlarmConfig.cancelAlarms();
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        Toast.makeText(getActivity(),"You chose " + ((Profile) adapter.getItem(position)).name,
                Toast.LENGTH_SHORT).show();

        // todo open profile, should implement modification of entry...
    }

    private class ProfileAdapter extends ArrayAdapter<Profile> {

        private final int row_layout;

        public ProfileAdapter(FragmentActivity activity, int row_layout) {
            super(activity, row_layout);
            this.row_layout = row_layout;
        }

        public View getView(int position, View convertView, @NonNull ViewGroup parent) {

            // Reference to the "row" View
            View row = convertView;
            if(row == null) {
                // If it's not recycled, create (inflate) it from layout
                row = getLayoutInflater(null).inflate(row_layout, null);
            }
            // Fill-in with the data from the Profile
            Profile profile = getItem(position);
            if(profile != null) {
                ((ImageView) row.findViewById(R.id.profile_image)).setImageDrawable(profile.photo);
                ((TextView) row.findViewById(R.id.profile_name)).setText("Name: " + profile.name);
                ((TextView) row.findViewById(R.id.profile_nickname)).setText("Nickname: " + profile.nickname);
                ((TextView) row.findViewById(R.id.profile_description)).setText(profile.description);

                // Format the date as a string according to the user's locale settings
                ((TextView) row.findViewById(R.id.profile_birthday)).
                        setText("Birthday: " + DateFormat.getDateInstance(DateFormat.SHORT).
                                format(profile.birthday.getTime()));
            }

            // We do not display the full description in the list
            return row;
        }
    }
}
