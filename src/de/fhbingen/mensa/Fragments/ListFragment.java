package de.fhbingen.mensa.Fragments;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import com.actionbarsherlock.app.SherlockFragment;
import de.fhbingen.mensa.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class ListFragment extends SherlockFragment {

    public static boolean roleChanged;

    public static ListFragment newInstance(String date){
        ListFragment fragment = new ListFragment();
        Bundle bundle = new Bundle();
        bundle.putString(DATE_IDENTIFY, date);
        fragment.setArguments(bundle);
        return fragment;
    }

    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
    }

    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState
    ){
        // activity_main as layout contains the layout for the fragment!
        // The fragment itself is an second layout file !!
        view = inflater.inflate(R.layout.activity_main, container, false);

        // Filling the view of the fragment
        listview = (ListView) view.findViewById(android.R.id.list);

        // mensa = (Mensa) this.getApplication();
        mensa = (Mensa) view.getContext().getApplicationContext();

        // Load userrole from preferences
        SharedPreferences settings = view.getContext().getSharedPreferences(Mensa.PREF_USER, 0);
        Mensa.userRole = Mensa.UserRole.values()[settings.getInt("userRole", Mensa.UserRole.STUDENT.ordinal())];

        //new LoadWeekTask().execute(Mensa.APIURL + "getWeek=201403");
        //new LoadWeekTask().execute(Mensa.APIURL + "getWeek=" + Mensa.getCurrentWeek());
        createList();

        listview.setOnItemClickListener(new ListView.OnItemClickListener( ) {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id){
                Intent detail = new Intent(view.getContext().getApplicationContext(), DishDetailActivity.class);
                detail.putExtra("data", (Dish)listview.getItemAtPosition(position));
                startActivity(detail);
            }
        });

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == 1337 && resultCode == 0 && roleChanged){
            //TODO: Update view
            roleChanged = false;
        }
    }

    private void createList(){
        // Line by Tobi
        //dlist = mensa.getDay("2014-01-13");

        dList = mensa.getDay(getArguments().getString(DATE_IDENTIFY));
        //TODO: dList possible null if no plan for this day.

        try {
            // Filling the Adapter with the generated values
            adapter = new DishItemAdapter(
                    view.getContext(),
                    dList
            );

            // Connection between ListView and Adapter
            listview.setAdapter(adapter);

        } catch (Exception e) {
            Log.e(TAG, "Exception cause: " + e.getCause() + "\nException message" + e.getMessage() + "\nException toStr" + e.toString());
        }

        /*
        // Set date
        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat dayFormat   = new SimpleDateFormat("EEEE", Locale.GERMAN);
        SimpleDateFormat monthFormat = new SimpleDateFormat("MMMMMMMMM", Locale.GERMAN);
        try {
            Date date = inputFormat.parse("2014-01-13");

            Calendar cal = new GregorianCalendar();
            cal.setTime(date);

            
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        */
    }

    private Mensa mensa;
    private ListView listview;
    private View view;
    private List<Dish> dList;
    private DishItemAdapter adapter;
    // String for finding an value in the bundle
    private static final String DATE_IDENTIFY = "DATE";
    private final static String TAG = "ListFragment";
}
