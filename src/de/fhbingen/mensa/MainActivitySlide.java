package de.fhbingen.mensa;


import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteCursor;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.activeandroid.ActiveAndroid;
import com.activeandroid.Cache;
import com.activeandroid.query.Select;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import de.fhbingen.mensa.data.event.BuildingChangedEvent;
import de.fhbingen.mensa.data.orm.Building;
import de.fhbingen.mensa.data.orm.Dish;
import de.fhbingen.mensa.data.orm.Ingredient;
import de.fhbingen.mensa.data.orm.LocalComplains;
import de.fhbingen.mensa.data.orm.LocalRating;
import de.fhbingen.mensa.data.orm.OfferedAt;
import de.fhbingen.mensa.data.orm.Photo;
import de.fhbingen.mensa.data.orm.Rating;
import de.fhbingen.mensa.data.orm.Sequence;
import de.fhbingen.mensa.service.UpdateContentService;
import de.greenrobot.event.EventBus;

// TODO: Refresh list onResume (for Ratings and Pictures etc.)

/**
 * MainActivity.
 *
 * Binding to BackgroundService
 * Creating Tabbed View
 * Select Building
 * Menu-Logic
 */
public class MainActivitySlide extends Activity implements ActionBar.TabListener {

    private final String TAG = MainActivitySlide.class.getSimpleName();

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v13.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, "onCreate()");

        setContentView(R.layout.activity_main_activity_slide);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        //Initialize ActiveAndroid SQLite ORM
        ActiveAndroid.initialize(this);

        // Start data Service
        Intent intent = new Intent(this, UpdateContentService.class);
        startService(intent);


        // Set up the action bar.
        final ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        // When swiping between different sections, select the corresponding
        // tab. We can also use ActionBar.Tab#select() to do this if we have
        // a reference to the Tab.
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                actionBar.setSelectedNavigationItem(position);
            }
        });

        // For each of the sections in the app, add a tab to the action bar.
        for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
            // Create a tab with text corresponding to the page title defined by
            // the adapter. Also specify this Activity object, which implements
            // the TabListener interface, as the callback (listener) for when
            // this tab is selected.
            actionBar.addTab(
                    actionBar.newTab()
                            .setText(mSectionsPagerAdapter.getPageTitle(i))
                            .setTabListener(this));
        }

        // Set current day as selected default tab
        mViewPager.setCurrentItem(1);

        // Check if mensa is selected
        final Set<String> subscribedBuildingIds = PreferenceManager
                .getDefaultSharedPreferences(this)
                .getStringSet(SettingsFragment.REF_KEY_BUILDINGS, null);

        if(subscribedBuildingIds == null){
            Toast.makeText(this, R.string.toast_no_building_subscribed, Toast.LENGTH_LONG).show();
        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main_activity_slide, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, de.fhbingen.mensa.SettingsActivity.class);
            startActivity(intent);

            return true;
        } else if (id == R.id.action_about) {
            final Intent intent = new Intent(this, AboutActivity.class);
            startActivity(intent);

            return true;
        } else if (id == R.id.action_ingredients) {
            Intent intent = new Intent(this, de.fhbingen.mensa.IngredientsActivity.class);
            startActivity(intent);

            return true;
        } else if (id == R.id.action_change_building) {

            final Set<String> subscribedBuildingIds = PreferenceManager
                    .getDefaultSharedPreferences(this)
                    .getStringSet(SettingsFragment.REF_KEY_BUILDINGS, null);

            if(subscribedBuildingIds == null){
                Toast.makeText(this, R.string.toast_no_building_subscribed, Toast.LENGTH_LONG).show();
                return true;
            }

            final String[] arValues = subscribedBuildingIds.toArray(new String[subscribedBuildingIds.size()]);
            final String[] arItems  = new String[subscribedBuildingIds.size()];

            for(int i = 0; i < subscribedBuildingIds.size(); i++){
                arItems[i] = Building.findByBuildingId(arValues[i]).getName();
            }

            // Get buildingId of last used building
            final int currentBuildingId = PreferenceManager
                    .getDefaultSharedPreferences(this)
                    .getInt(SettingsFragment.REF_KEY_CURRENT_BUILDINGID, SettingsFragment.DUMMY_INT_NOT_USED);
            int selectedItem = 0;
            if(currentBuildingId != SettingsFragment.DUMMY_INT_NOT_USED) {
                boolean found = false;
                for (int i = 0; i < arValues.length && !found; i++) {
                    if (currentBuildingId == Integer.parseInt(arValues[i])) {
                        selectedItem = i;
                        found = true;
                    }
                }
            }

            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.dialog_title_select_building);
            //builder.setItems(arItems, new DialogInterface.OnClickListener() {
            builder.setSingleChoiceItems(arItems, selectedItem,  new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int position) {

                    dialog.dismiss();

                    Log.v(TAG, arValues[position] + " -> " + arItems[position]);

                    //TODO: Used common reference to sharedPrefs???
                    final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(builder.getContext());
                    final SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putInt(SettingsFragment.REF_KEY_CURRENT_BUILDINGID, Integer.parseInt(arValues[position]));
                    editor.commit();

                    //Post slected BuildingId
                    EventBus.getDefault().post(new BuildingChangedEvent(Integer.parseInt(arValues[position])));
                }
            });

            builder.show();

            /*
            List<Building> buildings = new Select().from(Building.class).execute();
            for (final Building b : buildings) {
                Log.v(TAG, b.toString());
            }

            List<Ingredient> ingredients = new Select().from(Ingredient.class).execute();
            for (final Ingredient i : ingredients) {
                Log.v(TAG, i.toString());
            }

            List<Dish> dishes = new Select().from(Dish.class).execute();
            for (final Dish d : dishes) {
                Log.v(TAG, d.toString());
            }

            List<Rating> ratings = new Select().from(Rating.class).execute();
            for (final Rating r : ratings){
                Log.v(TAG, r.toString());
            }

            List<Photo> photos = new Select().from(Photo.class).execute();
            for(final Photo p : photos){
                Log.v(TAG, p.toString());
            }

            List<LocalRating> localRatings = new Select().from(LocalRating.class).execute();
            for (final LocalRating lR : localRatings){
                Log.v(TAG, lR.toString());
            }

            List<LocalComplains> localComplains = new Select().from(LocalComplains.class).execute();
            for (final LocalComplains lC : localComplains){
                Log.v(TAG, lC.toString());
            }


            List<de.fhbingen.mensa.data.orm.Date> dates = new Select().from(de.fhbingen.mensa.data.orm.Date.class).execute();
            for (final de.fhbingen.mensa.data.orm.Date d : dates) {
                Log.v(TAG, d.toString());
            }

            List<OfferedAt> offeredAt = new Select().from(OfferedAt.class).execute();
            for (final OfferedAt oA : offeredAt) {
                Log.v(TAG, oA.toString());
            }

            Sequence seq = new Select().from(Sequence.class).where("seq_name = ?", Sequence.SEQNAME).executeSingle();
            if(seq == null){
                seq = new Sequence();
            }
            Log.v(TAG, "Sequence: " + seq.toString());
            */

            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    private int currentTabSelected;

    public int getCurrentTabSelected(){
        return currentTabSelected;
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        // When the given tab is selected, switch to the corresponding page in
        // the ViewPager.
        mViewPager.setCurrentItem(tab.getPosition());

        this.currentTabSelected = tab.getPosition();
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }


    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return PlaceholderFragment.newInstance(position + 1);
        }

        @Override
        public int getCount() {
            // Show 9 pages
            return 9;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Gestern";
                case 1:
                    return "Heute";
                case 2:
                    return "Morgen";
                default:
                    final Calendar cal = Calendar.getInstance();
                    cal.add(Calendar.DAY_OF_YEAR, position - 1);
                    java.util.Date date = cal.getTime();
                    // @see: https://docs.oracle.com/javase/tutorial/i18n/format/simpleDateFormat.html
                    SimpleDateFormat formatter = new SimpleDateFormat("EEE, d. MMM", Locale.GERMAN);
                    return formatter.format(date);
            }
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        // Date argument in yyyy-MM-yy format
        public static final String ARG_DATE = "arg_date";

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            //args.putInt(ARG_SECTION_NUMBER, sectionNumber);

            // Put date in yyyy-MM-dd format as argument in fragment
            final Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_YEAR, sectionNumber - 2);
            java.util.Date date = cal.getTime();
            // @see: https://docs.oracle.com/javase/tutorial/i18n/format/simpleDateFormat.html
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", Locale.GERMAN);
            args.putString(ARG_DATE, formatter.format(date));

            if(!EventBus.getDefault().isRegistered(fragment)){
                EventBus.getDefault().register(fragment);
            }

            fragment.setArguments(args);
            return fragment;
        }

        public PlaceholderFragment() {
        }

        private DishCursorAdapter dishCursorAdapter;

        private ListView listView;

        public void updateBuildingCursor(int buildingId){
            final Date date = Date.valueOf(getArguments().getString(ARG_DATE));
            final int currentBuildingId = PreferenceManager
                    .getDefaultSharedPreferences(getActivity())
                    .getInt(SettingsFragment.REF_KEY_CURRENT_BUILDINGID, SettingsFragment.DUMMY_INT_NOT_USED);

            String statement = Dish.getSqlStatementForDateAndBuilding(date, 1703);
            String[] args = new String[] {date.toString(), Integer.toString(currentBuildingId)};
            Cursor dishCursorNew = Cache.openDatabase().rawQuery(statement, args);

            dishCursorAdapter.changeCursor(dishCursorNew);
        }

        /**
         * EventBus Callback
         * @param buildingChangedEvent
         */
        public void onEvent(BuildingChangedEvent buildingChangedEvent){
            Log.v("Fragment", "buildingChangedEvent.buildingId: " + buildingChangedEvent.selectedBuildingId);

            updateBuildingCursor(buildingChangedEvent.selectedBuildingId);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main_activity_slide, container, false);

            final Date date = Date.valueOf(getArguments().getString(ARG_DATE));
            final int currentBuildingId = PreferenceManager
                    .getDefaultSharedPreferences(getActivity())
                    .getInt(SettingsFragment.REF_KEY_CURRENT_BUILDINGID, SettingsFragment.DUMMY_INT_NOT_USED);

            // Build View

            String stmtnew = Dish.getSqlStatementForDateAndBuilding(date, 1703);
            String[] argsNew = new String[] {date.toString(), Integer.toString(currentBuildingId)};
            Cursor dishCursorNew = Cache.openDatabase().rawQuery(stmtnew, argsNew);

            listView = (ListView) rootView.findViewById(R.id.listView_dishes);
            dishCursorAdapter = new DishCursorAdapter(container.getContext(), dishCursorNew);
            listView.setAdapter(dishCursorAdapter);



            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    final Intent detail = new Intent(view.getContext(), DishDetailActivity.class);

                    final SQLiteCursor cursor = (SQLiteCursor) parent.getAdapter().getItem(position);
                    detail.putExtra(Dish.COL_DISHID, cursor.getInt(cursor.getColumnIndex(Dish.COL_DISHID)));
                    //Workaround: Ugly... Use view.id to store photoId of random thumb and pass it to detail
                    detail.putExtra(Photo.COL_PHOTOID, view.getId());

                    //Put date of tab to further comparison if user is allowed to vote
                    detail.putExtra(Dish.ARG_DATE, date.toString());
                    startActivity(detail);
                }
            });

            return rootView;
        }
    }



    @Override
    protected void onResume() {
        super.onResume();
        //TODO: Check if data of current tab changed (User has uploaded photo)

        Intent intent= new Intent(this, UpdateContentService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();


    }

    @Override
    protected void onPause() {
        super.onPause();
        unbindService(mConnection);
    }

    private UpdateContentService s;

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            UpdateContentService.MyBinder b = (UpdateContentService.MyBinder) binder;
            s = b.getService();

            //Toast.makeText(MainActivitySlide.this, "Connected", Toast.LENGTH_SHORT)
            //        .show();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            s =  null;
        }
    };
}
