package de.fhbingen.mensa.service;

import android.app.Service;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Binder;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

import com.activeandroid.ActiveAndroid;
import com.activeandroid.query.Select;

import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import de.fhbingen.mensa.SettingsFragment;
import de.fhbingen.mensa.data.Changes;
import de.fhbingen.mensa.data.event.ServiceEvent;
import de.fhbingen.mensa.data.orm.Building;
import de.fhbingen.mensa.data.orm.Date;
import de.fhbingen.mensa.data.orm.Delete;
import de.fhbingen.mensa.data.orm.Dish;
import de.fhbingen.mensa.data.orm.OfferedAt;
import de.fhbingen.mensa.data.orm.Sequence;
import de.greenrobot.event.EventBus;

public class LocalWordService extends Service {

    private static final String TAG = LocalWordService.class.getSimpleName();

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

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");

        if(!isRunning) {

            isRunning = true;

            //Creating new thread for my service
            //Always write your long running tasks in a separate thread, to avoid ANR
            new Thread(new Runnable() {

                private final String TAG = LocalWordService.TAG + ".Thread";

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
                            Log.d(TAG, "Updating DishOld " + selectedDish.getDishId());
                            selectedDish.update(d).save();
                        } else {
                            //New DishOld
                            Log.d(TAG, "Creating new DishOld " + d.getDishId());
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

                @Override
                public void run() {
                    Log.d("myThread", "run()");

                    //Clear
                    //clearDB();


                        Log.d("myThread", "running ...");
                        isRunning = true;

                        try {
                            //Simulate Activity
                            Thread.sleep(3000);

                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        // Current Sequence
                        Sequence seq = new Select().from(Sequence.class).where("seq_name = ?", Sequence.SEQNAME).executeSingle();
                        if (seq == null) {
                            seq = new Sequence();
                            seq.save();
                        }

                        long seqNum = seq.getLastSequence();

                        // Subscribed Buildings
                        Set<String> subscribedBuildings = PreferenceManager
                                .getDefaultSharedPreferences(getApplicationContext())
                                .getStringSet(SettingsFragment.REF_KEY_BUILDINGS, null);
                        String[] buildingIds;
                        if(subscribedBuildings != null){
                            buildingIds = subscribedBuildings.toArray(new String[subscribedBuildings.size()]);
                        } else {
                            //TODO: Fix ugly ...
                            buildingIds = new String[] {"-1"};
                        }

                        //String url = "http://192.168.178.28:8080/changes?buildings={buildings}&seq={seq}";
                        //String url = "http://192.168.2.165:8080/changes?buildings={buildings}&seq={seq}";

                        RestTemplate restTemplate = new RestTemplate();

                        restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());

                        //Changes result = restTemplate.getForObject(url, Changes.class, "4,8", seqNum);

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

                        updateDatabase(result);

                        //Stop service once it finishes its task
                        stopSelf();

                }
            }).start();

        }

        //This freezes Application, so use new thread.
        /*
        try {
            Thread.sleep(100000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }*/

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

        public LocalWordService getService(){
            Log.d(TAG, "MyBinder.getService");
            return LocalWordService.this;
        }

        public void sendRating(int dishId, int value){
            Log.d(TAG, "sendRating [dishId: " + dishId + ", value: " + value + "]");
            //TODO: Send rating to server
            //TODO: Store rating local with dishId and dateId in DB
        }
    }

    public class UrlBuilder {
        public static final String BASE = "http://192.168.178.28:8080";
        ///changes?buildings={buildings}&seq={seq}";
        ///changes?buildings=&seq=0
        public UrlBuilder() { }

        private long sequence;
        private long[] buildings;

        public UrlBuilder setSequence(final long sequence){
            this.sequence = sequence;
            return this;
        }

        public UrlBuilder setBuildings(final long[] buildingsIds){
            this.buildings = buildingsIds;
            return this;
        }
        public UrlBuilder setBuildings(final String[] buildingsIds){
            buildings = new long[buildingsIds.length];
            for (int i = 0; i < buildings.length; i++) {
                buildings[i] = Long.valueOf(buildingsIds[i]);
            }
            return this;
        }


        public String getChangesUrl(){
            final StringBuilder sb = new StringBuilder();
            sb.append(BASE).append("/changes?").append("seq=").append(sequence);
            sb.append("&buildings=");
            for(int i = 0; i < buildings.length - 1; i++){
                sb.append(buildings[i]).append(",");
            }
            sb.append(buildings[buildings.length-1]);
            return sb.toString();
        }

    }

}
