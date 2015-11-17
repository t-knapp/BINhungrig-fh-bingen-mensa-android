package de.fhbingen.mensa.service;

import android.app.Service;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Binder;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

import com.activeandroid.ActiveAndroid;
import com.activeandroid.query.Select;

import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import de.fhbingen.mensa.SettingsFragment;
import de.fhbingen.mensa.data.Changes;
import de.fhbingen.mensa.data.event.ServiceEvent;
import de.fhbingen.mensa.data.event.SettingsChangeEvent;
import de.fhbingen.mensa.data.orm.Building;
import de.fhbingen.mensa.data.orm.Date;
import de.fhbingen.mensa.data.orm.Delete;
import de.fhbingen.mensa.data.orm.Dish;
import de.fhbingen.mensa.data.orm.OfferedAt;
import de.fhbingen.mensa.data.orm.Sequence;
import de.greenrobot.event.EventBus;

public class UpdateContentService extends Service {

    private static final String TAG = UpdateContentService.class.getSimpleName();

    private boolean isRunning  = false;

    public static void clearDB(){
        SQLiteDatabase db = ActiveAndroid.getDatabase();
        List<String> tables = new ArrayList<String>();
        Cursor cursor = db.rawQuery("SELECT * FROM sqlite_master WHERE type='table';", null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            String tableName = cursor.getString(1);
            if (!tableName.equals("android_metadata") &&
                    !tableName.equals("sqlite_sequence")) {
                tables.add(tableName);
            }
            cursor.moveToNext();
        }
        cursor.close();
        for (String tableName : tables) {
            Log.d(TAG, "Delete content from '" + tableName + "'");
            db.execSQL("DELETE FROM " + tableName);
        }
    }

    private void updateBuildings(final List<Building> buildings) {
        Building selectedBuilding;
        for (final Building b : buildings) {
            selectedBuilding = new Select().from(b.getClass()).where("buildingId = ?", b.getBuildingId()).executeSingle();
            if (selectedBuilding != null) {
                Log.d(TAG, "Updating building " + selectedBuilding.getBuildingId());
                selectedBuilding.update(b).save();
            } else {
                //New building
                Log.d(TAG, "Creating new Building " + b.getBuildingId());
                b.save();
            }
        }
        //TODO: Feedback to UI if update/inserts done
    }

    private void updateDishes(final List<Dish> dishes) {
        Dish selectedDish;
        for (final Dish d : dishes) {
            selectedDish = new Select().from(d.getClass()).where("dishId = ?", d.getDishId()).executeSingle();
            if (selectedDish != null) {
                //Update DishOld in DB
                Log.d(TAG, "Updating Dish " + selectedDish.getDishId());
                selectedDish.update(d).save();
            } else {
                //New DishOld
                Log.d(TAG, "Creating new Dish " + d.getDishId());
                d.save();
            }
        }
        //TODO: Feedback to UI if update/inserts done
    }

    private void updateDates(List<Date> dates) {
        Date selectedDate;
        for (final Date d : dates) {
            selectedDate = new Select().from(d.getClass()).where("date = ?", d.getDate()).executeSingle();
            if (selectedDate != null) {
                Log.d(TAG, "Updating Date " + d.getDateId());
                selectedDate.update(d).save();
            } else {
                Log.d(TAG, "Creating new Date " + d.getDateId());
                d.save();
            }
        }
    }

    private void updateOfferedAt(List<OfferedAt> offeredAt) {
        OfferedAt selectedOfferedAt;
        for (final OfferedAt oA : offeredAt) {
            selectedOfferedAt = new Select()
                    .from(oA.getClass())
                    .where("fk_dishId = ? AND fk_dateId = ?", oA.getDishId(), oA.getDateId())
                    .executeSingle();
            if (selectedOfferedAt != null) {
                Log.d(TAG, "Updating OfferedAt [dish: " + oA.getDishId() + ", date: " + oA.getDateId() + "]");
                selectedOfferedAt.update(oA).save();
            } else {
                Log.d(TAG, "Creating new OfferedAt [dish: " + oA.getDishId() + ", date: " + oA.getDateId() + "]");
                oA.save();
            }
        }
    }

    private void updateSequence(final Sequence sequence) {
        Sequence currentSeq = new Select().from(Sequence.class).where("seq_name = ?", Sequence.SEQNAME).executeSingle();
        if (currentSeq != null) {
            Log.d(TAG, "Updating Sequence to " + sequence.getLastSequence());
            currentSeq.update(sequence).save();
        } else {
            Log.d(TAG, "Creating Sequence with " + sequence.getLastSequence());
            sequence.save();
        }
    }

    private void applyDeletes(List<Delete> deletes) {
        final String packagePrefix = "de.fhbingen.mensa.data.orm.";
        Class cls;
        for (final Delete d : deletes) {
            try {
                cls = Class.forName(packagePrefix + d.getTableName());
                Log.v(TAG, "Delete " + cls.getCanonicalName() + " seq = " + d.getDeleteSeqNumber());
                new com.activeandroid.query.Delete().from(cls).where("seq = ?", d.getDeleteSeqNumber()).execute();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    private void updateDatabase(final Changes changes) {
        Log.d(TAG, "updateDatabase()");

        if (changes.needToUpdate) {

            ActiveAndroid.beginTransaction();
            try {
                //Building
                updateBuildings(changes.getBuildings());

                //DishOld
                updateDishes(changes.getDishes());

                //Date
                updateDates(changes.getDates());

                //offeredAt
                updateOfferedAt(changes.getOfferedAt());

                //Apply delets
                applyDeletes(changes.getDeletes());

                //Sequence
                updateSequence(changes.getSequence());

                ActiveAndroid.setTransactionSuccessful();
            } finally {
                ActiveAndroid.endTransaction();

                EventBus.getDefault().post(new ServiceEvent(ServiceEvent.EventType.UPDATEDONE, "Data pulled"));
            }
        } else {
            Log.v(TAG, "Nothing to update.");
            EventBus.getDefault().post(new ServiceEvent(ServiceEvent.EventType.ALLREADYUPTODATE, "Nothing to do here."));
        }
    }

    // EventBus
    public void onEvent(SettingsChangeEvent event) {
        Log.d(TAG, "onEvent: SettingsChangeEvent: " + event.getChangePreference());

        //TODO: Workaround: If buildings changed, set local sequence to 0 to fetch all data
        //TODO: Better: Use a sequence per building

        // Run update if subscribed buildings changed
        if(event.getChangePreference().equals(SettingsFragment.REF_KEY_BUILDINGS)) {
            doWork();
        }
    };

    /**
     * Do blocking network stuff in separate thread.
     */
    private void doWork(){
        //Creating new thread for my service
        //Always write your long running tasks in a separate thread, to avoid ANR
        new Thread(new Runnable() {

            private final String TAG = UpdateContentService.TAG + ".Thread";

            @Override
            public void run() {
                Log.d(TAG, "run()");

                // Subscribed Buildings
                final Set<String> subscribedBuildings = PreferenceManager
                        .getDefaultSharedPreferences(getApplicationContext())
                        .getStringSet(SettingsFragment.REF_KEY_BUILDINGS, null);
                String[] buildingIds;
                boolean firstRun = true;
                if(subscribedBuildings != null){
                    buildingIds = subscribedBuildings.toArray(new String[subscribedBuildings.size()]);
                    firstRun = false;
                } else {
                    //TODO: Fix ugly ...
                    buildingIds = new String[] {"1703"};
                }

                // Current Sequence
                Sequence seq = new Select().from(Sequence.class).where("seq_name = ?", Sequence.SEQNAME).executeSingle();
                if (seq == null) {
                    seq = new Sequence();
                    seq.save();
                }
                long seqNum = seq.getLastSequence();

                final RestTemplate restTemplate = new RestTemplate();

                restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());

                String url = new UrlBuilder().setSequence(seqNum).setBuildings(buildingIds).getChangesUrl();

                Log.d(TAG, url);

                Changes result = restTemplate.getForObject(url, Changes.class);

                Log.d("myThread", "/GET");
                if (result.needToUpdate) {
                    Log.d("myThread", "#buildings: " + result.getBuildings().size());
                    Log.d("myThread", "#dishes:    " + result.getDishes().size());
                    Log.d("myThread", "#deletes:   " + result.getDeletes().size());
                    Log.d("myThread", "#ratings:   " + result.getRatings().size());
                    Log.d("myThread", "#date:      " + result.getDates().size());
                    Log.d("myThread", "#offeredAt: " + result.getOfferedAt().size());
                }
                Log.d("myThread", "sequence:   " + result.getSequence().getLastSequence());

                if(firstRun){
                    //Just update buildings, skipping all other data and sequence
                    updateBuildings(result.getBuildings());
                } else {
                    //Update all data for subscribed buidlings
                    updateDatabase(result);
                }

                //Stop service once it finishes its task
                //stopSelf();

            }
        }).start();

    }

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");

        // Register EventBus if not
        if(!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }

        // Do the work (in new thread)
        doWork();

        Log.d(TAG, "onStartCommand -> return");
        return Service.START_NOT_STICKY;
    }

    //TODO: Was macht ein Binder?
    private final IBinder mBinder = new MyBinder();

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind");
        // TODO: Return the communication channel to the service.
        //throw new UnsupportedOperationException("Not yet implemented");
        return mBinder;
    }

    public class MyBinder extends Binder {

        private static final String TAG = "MyBinder";

        public UpdateContentService getService(){
            Log.d(TAG, "MyBinder.getService");
            return UpdateContentService.this;
        }

        public void sendRating(int dishId, int value){
            Log.d(TAG, "sendRating [dishId: " + dishId + ", value: " + value + "]");
            //TODO: Send rating to server
            //TODO: Store rating local with dishId and dateId in DB
        }
    }

    public class UrlBuilder {
        //public static final String BASE = "http://192.168.178.28:8080";
        public static final String BASE = "http://192.168.2.165:8080";

        public UrlBuilder() { }

        private long sequence;
        private String[] buildings;

        public UrlBuilder setSequence(final long sequence){
            this.sequence = sequence;
            return this;
        }

        public UrlBuilder setBuildings(final String[] buildingsIds){
            buildings = buildingsIds;
            return this;
        }

        public String getChangesUrl(){
            final StringBuilder sb = new StringBuilder();
            sb.append(BASE).append("/changes?").append("seq=").append(sequence);
            if(buildings != null) {
                sb.append("&buildings=");
                sb.append(TextUtils.join(",", buildings));
            }
            return sb.toString();
        }

    }

}
