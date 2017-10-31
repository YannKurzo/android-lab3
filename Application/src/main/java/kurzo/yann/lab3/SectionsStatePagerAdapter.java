package kurzo.yann.lab3;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Yann on 25/10/2017.
 */

public class SectionsStatePagerAdapter extends FragmentStatePagerAdapter {

    //List of fragments
    private final List<Fragment> mFragmentList = new ArrayList<>();

    public SectionsStatePagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        return mFragmentList.get(position);
    }

    @Override
    public int getCount() {
        return mFragmentList.size();
    }

    //Method to add fragment to the list of fragments
    public void addFragment(Fragment fragment, String title){
        mFragmentList.add(fragment);
    }
}
