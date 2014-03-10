package de.fhbingen.mensa;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.widget.ListView;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import de.fhbingen.mensa.Fragments.ListFragment;

import java.util.Calendar;
import java.util.List;


public class MainActivity extends SherlockFragmentActivity {

	//public static boolean roleChanged;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.view_pager);
        Log.i(TAG, "ContentView is set");

        //Connect to Mensa
        mensa = (Mensa) getApplication();
                
        //Setting the ViewPager
        viewPager = (ViewPager) findViewById(R.id.viewPager);
        myFragmentPagerAdapter = new MyFragmentPagerAdapter(getSupportFragmentManager());

        
        // The PagerTabStripe is set automatically!

        //Start LoadWeekTask, Save to Map. Provide Access to this Map
        context = viewPager.getContext();
        final String query = Mensa.APIURL + "getWeek=" + Mensa.getCurrentWeek();
        new LoadWeekTask().execute(query);
	}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getSupportMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                Intent settings = new Intent(this, SettingsActivity.class);
                //startActivity(settings);

                startActivityForResult(settings, 1337);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public CharSequence getPageTitle(int position) {
        return "Page " + (position + 1);
    }

    private static class MyFragmentPagerAdapter extends FragmentPagerAdapter {

        public MyFragmentPagerAdapter(FragmentManager fm){
            super(fm);
        }

        @Override
        public Fragment getItem(int index){
        	Log.d(TAG, "getItem( index : " + index + " )");
        	final Calendar rightNow = Calendar.getInstance();
        	rightNow.add(Calendar.DAY_OF_MONTH, index);
            // Computating from the actual day
            String date = Mensa.toYYYYMMDD(rightNow); // YYYY-MM-DD
            
            //TODO: Hier können NullPointer kommen wenn date nicht in der Map enthalten ist.
            //Tobi macht das noch =)
            return ListFragment.newInstance(date);
        }

        @Override
        public int getCount(){
            return NUMBER_OF_PAGES;
        }

        public CharSequence getPageTitle(int position){
        	final Calendar rightNow = Calendar.getInstance();
        	if(position == 0){
        		return "Heute";
        	} else if (position == 1) {
        		return "Morgen";
        	} else {
        		rightNow.add(Calendar.DAY_OF_MONTH, position);   		
        		return Mensa.toDDMMYYYY(rightNow); // DD.MM.YYYY
        	}
        }
    }

	private Mensa mensa;
    private List<Dish> dlist;
    private DishItemAdapter adapter;
    private ListView listview;
    private ViewPager viewPager;
    private MyFragmentPagerAdapter myFragmentPagerAdapter;
    private static String today = "2014-03-10";
    private final static String TAG = "MainActivity";
    // TODO perhaps computating the max value of available pages
    private final static int NUMBER_OF_PAGES = 5;
    
    /**
     * Task loads a week
     * @author tknapp@fh-bingen.de
     *
     */
    private class LoadWeekTask extends ContentTask {
        @Override
        protected void onPreExecute() {
            d = new ProgressDialog(context);
            d.setCancelable(false);
            d.setMessage("Lade Speiseplan");
            d.show();
        }

        @Override
        protected void onPostExecute(String result) {
            Log.d("TOBI", result);
            mensa.loadWeek(result);
            d.dismiss();
            // Connection between viewpager und fragmentadapter
            viewPager.setAdapter(myFragmentPagerAdapter);
        }

        private ProgressDialog d;
    }
    
    private Context context;

}
