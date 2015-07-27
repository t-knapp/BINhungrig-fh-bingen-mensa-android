package de.fhbingen.mensa;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.widget.ListView;
import android.widget.Toast;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import de.fhbingen.mensa.Fragments.ListFragment;

import java.util.Calendar;
import java.util.Locale;
import java.util.Map;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;

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

        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo == null || !netInfo.isConnectedOrConnecting()) {

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.offline_title);
            builder.setMessage(R.string.you_are_offline);
            builder.setPositiveButton(R.string.reload, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    new LoadWeekTask(false).execute(query);
                }
            });
            builder.setNegativeButton(R.string.quit, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    System.exit(1);
                }
            });

            builder.setCancelable(false);
            builder.create().show();
        } else {
            new LoadWeekTask(false).execute(query);
        }
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
                startActivityForResult(settings, 1337);
                return true;
            case R.id.action_ingredients:
                Intent ingredients = new Intent(this, IngredientsActivity.class);
                startActivity(ingredients);
                return true;
            case R.id.action_about:
                Intent about = new Intent(this, AboutActivity.class);
                startActivity(about);
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    //public CharSequence getPageTitle(int position) {
    //    return "Page " + (position + 1);
    //}

    private class MyFragmentPagerAdapter extends FragmentPagerAdapter {

        public MyFragmentPagerAdapter(FragmentManager fm){
            super(fm);
        }

        @Override
        public Fragment getItem(int index){
        	//Log.d(TAG, "getItem( index : " + index + " )");
        	final Calendar rightNow = Calendar.getInstance();
        	rightNow.add(Calendar.DAY_OF_MONTH, index);

            //Load next week on sunday
            final Calendar sunday = Calendar.getInstance();
            sunday.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);

            long diff = sunday.getTimeInMillis() - rightNow.getTimeInMillis();

            if(!mensa.getNextWeekLoaded()){
                if((diff / (24 * 60 * 60 * 1000)) <= 0){
                    final String query = Mensa.APIURL + "getWeek=" + Mensa.getNextWeek();
                    new LoadWeekTask(true).execute(query);
                }
            }

            // Computating from the actual day
            String date = Mensa.toYYYYMMDD(rightNow); // YYYY-MM-DD
            
            return ListFragment.newInstance(date);
        }

        @Override
        public int getCount(){
            int nextWeekDayCount = mensa.getNextWeekLoaded() ? 5 : 0;
            return this.tillSunday + nextWeekDayCount;
        }

        public CharSequence getPageTitle(int position){
        	final Calendar rightNow = Calendar.getInstance();

        	if(position == 0){
        		return getString(R.string.today);
        	} else if (position == 1) {
        		return getString(R.string.tomorrow);
        	} else {
        		rightNow.add(Calendar.DAY_OF_MONTH, position);

                final String dayName = rightNow.getDisplayName(Calendar.DAY_OF_WEEK,
                        Calendar.LONG, Locale.GERMAN);

        		return dayName + ", " + Mensa.toDDMMYYYY(rightNow); // DD.MM.YYYY
        	}
        }

        /*public void setNextWeekDayCount(int cnt){
            this.nextWeekDayCount = cnt;
        }*/

        public void setNextWeekLoaded(boolean b){
            this.nextWeekLoaded = b;
        }

        //private int nextWeekDayCount = 0;
        private int tillSunday = mensa.daysTillSunday();
        private boolean nextWeekLoaded = false;
    }

	private Mensa mensa;
    private DishItemAdapter adapter;
    private ListView listview;
    private ViewPager viewPager;
    private MyFragmentPagerAdapter myFragmentPagerAdapter;

    private final static String TAG = "MainActivity";

    /**
     * Task loads a week
     * @author tknapp@fh-bingen.de
     *
     */
    private class LoadWeekTask extends ContentTask {

        public LoadWeekTask(boolean append){
            super();
            this.append = append;
        }

        @Override
        protected void onPreExecute() {
            if (!append){
                //Do not show if next week is loading @ Saturdays Fragment
                d = new ProgressDialog(context);
                d.setCancelable(false);
                d.setMessage(getString(R.string.loading_dishes));
                d.show();
            }
        }

        @Override
        protected void onPostExecute(String result) {
            mensa.loadWeek(result, append);
            if(!append){
                //...Not shown @ Saturdays-Loading, no need to close
                d.dismiss();
            }
            // Connection between viewpager und fragmentadapter
            if(!append){
                viewPager.setAdapter(myFragmentPagerAdapter);

                //Swipe to next day if mensa is already closed
                if(mensa.isAlreadyClosed()){
                    viewPager.setCurrentItem(1);
                    Toast.makeText(context,R.string.mensa_already_closed, Toast.LENGTH_LONG).show();
                }

            } else {
                //Updating MUST NOT be done after execute() because
                //assync task. We MUST update after the execution!
                //myFragmentPagerAdapter.setNextWeekDayCount(5);
                mensa.setNextWeekLoaded(true);
                myFragmentPagerAdapter.setNextWeekLoaded(true);
                myFragmentPagerAdapter.notifyDataSetChanged();
            }
        }

        private ProgressDialog d;
        private final boolean append;
    }
    
    private Context context;

}
