package de.fhbingen.mensa.service;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

import com.activeandroid.ActiveAndroid;
import com.activeandroid.query.Select;

import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.fhbingen.mensa.Mensa;
import de.fhbingen.mensa.SettingsFragment;
import de.fhbingen.mensa.data.Changes;
import de.fhbingen.mensa.data.event.NetworkStatusEvent;
import de.fhbingen.mensa.data.event.ServiceEvent;
import de.fhbingen.mensa.data.event.SettingsChangeEvent;
import de.fhbingen.mensa.data.orm.Building;
import de.fhbingen.mensa.data.orm.Date;
import de.fhbingen.mensa.data.orm.Delete;
import de.fhbingen.mensa.data.orm.Dish;
import de.fhbingen.mensa.data.orm.Ingredient;
import de.fhbingen.mensa.data.orm.OfferedAt;
import de.fhbingen.mensa.data.orm.Photo;
import de.fhbingen.mensa.data.orm.Rating;
import de.fhbingen.mensa.data.orm.Sequence;
import de.greenrobot.event.EventBus;

/**
 * Background Service for fetching new data from server.
 */
public class UpdateContentService extends Service {

    private static final String TAG = UpdateContentService.class.getSimpleName();

    private boolean isConnected = false;

    /**
     * Central database modifing method (Insert/Update/Delete)
     * @param changes Chanes object holding data fetched from server
     */
    private void updateDatabase(final Changes changes) {
        //Log.d(TAG, "updateDatabase()");

        if (changes.needToUpdate) {

            ActiveAndroid.beginTransaction();
            try {
                //Building
                updateBuildings(changes.getBuildings());

                //Ingredients
                updateIngredients(changes.getIngredients());

                //Dish
                updateDishes(changes.getDishes());

                //Ratings
                updateRatings(changes.getRatings());

                //Photos
                updatePhotos(changes.getPhotos());

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

    /**
     * Updates (Insert/Update) photos in DB
     * @param photos
     */
    private void updatePhotos(List<Photo> photos) {
        Photo selectedPhoto;
        for (final Photo p : photos) {
            selectedPhoto = new Select()
                    .from(p.getClass())
                    .where(Photo.COL_PHOTOID + " = ? ", p.getPhotoId())
                    .executeSingle();
            if (selectedPhoto != null) {
                //Log.d(TAG, "Updating " + p.toString());
                selectedPhoto.update(p).save();
            } else {
                //Log.d(TAG, "Creating new " + p.toString());
                p.save();
            }
        }
    }

    /**
     * Updates (Insert/Update) ingredients in DB
     * @param ingredients
     */
    private void updateIngredients(List<Ingredient> ingredients) {
        Ingredient selectedIngredient;
        for (final Ingredient i : ingredients) {
            selectedIngredient = new Select()
                    .from(i.getClass())
                    .where(Ingredient.COL_KEY + " = ? ", i.getKey())
                    .executeSingle();
            if (selectedIngredient != null) {
                //Log.d(TAG, "Updating " + i.toString());
                selectedIngredient.update(i).save();
            } else {
                //Log.d(TAG, "Creating new " + i.toString());
                i.save();
            }
        }
    }

    /**
     * Updates (Insert/Update) ratings in DB
     * @param ratings
     */
    private void updateRatings(List<Rating> ratings) {
        Rating selectedRating;
        for (final Rating r : ratings) {
            selectedRating = new Select()
                    .from(r.getClass())
                    .where(Rating.COL_RATINGID + " = ? ", r.getRatingId())
                    .executeSingle();
            if (selectedRating != null) {
                //Log.d(TAG, "Updating " + r.toString());
                selectedRating.update(r).save();
            } else {
                //Log.d(TAG, "Creating new " + r.toString());
                r.save();
            }
        }
    }

    /**
     * Updates (Insert/Update) buildings in DB
     * @param buildings
     */
    private void updateBuildings(final List<Building> buildings) {
        Building selectedBuilding;
        for (final Building b : buildings) {
            selectedBuilding = new Select().from(b.getClass()).where("buildingId = ?", b.getBuildingId()).executeSingle();
            if (selectedBuilding != null) {
                //Log.d(TAG, "Updating building " + selectedBuilding.toString());
                selectedBuilding.update(b).save();
            } else {
                //New building
                //Log.d(TAG, "Creating new Building " + b.toString());
                b.save();
            }
        }
        //TODO: Feedback to UI if update/inserts done
    }

    /**
     * Updates (Insert/Update) dishes in DB
     * @param dishes
     */
    private void updateDishes(final List<Dish> dishes) {
        Dish selectedDish;
        for (final Dish d : dishes) {
            selectedDish = new Select()
                    .from(d.getClass())
                    .where(Dish.COL_DISHID + " = ?", d.getDishId())
                    .executeSingle();
            if (selectedDish != null) {
                //Update DishOld in DB
                //Log.d(TAG, "Updating Dish " + selectedDish.getDishId());
                selectedDish.update(d).save();
            } else {
                //New DishOld
                //Log.d(TAG, "Creating new Dish " + d.getDishId());
                d.save();
            }
        }
        //TODO: Feedback to UI if update/inserts done
    }

    /**
     * Updates (Insert/Update) dates in DB
     * @param dates
     */
    private void updateDates(List<Date> dates) {
        Date selectedDate;
        for (final Date d : dates) {
            selectedDate = new Select().from(d.getClass()).where("date = ?", d.getDate()).executeSingle();
            if (selectedDate != null) {
                //Log.d(TAG, "Updating Date " + d.toString());
                selectedDate.update(d).save();
            } else {
                //Log.d(TAG, "Creating new Date " + d.toString());
                d.save();
            }
        }
    }

    /**
     * Updates (Insert/Update) offeredAt in DB
     * @param offeredAt
     */
    private void updateOfferedAt(List<OfferedAt> offeredAt) {
        OfferedAt selectedOfferedAt;
        for (final OfferedAt oA : offeredAt) {
            selectedOfferedAt = new Select()
                    .from(oA.getClass())
                    .where("fk_dishId = ? AND fk_dateId = ?", oA.getDishId(), oA.getDateId())
                    .executeSingle();
            if (selectedOfferedAt != null) {
                //Log.d(TAG, "Updating OfferedAt [dish: " + oA.getDishId() + ", date: " + oA.getDateId() + "]");
                selectedOfferedAt.update(oA).save();
            } else {
                //Log.d(TAG, "Creating new OfferedAt [dish: " + oA.getDishId() + ", date: " + oA.getDateId() + "]");
                oA.save();
            }
        }
    }

    /**
     * Updates (Insert/Update) Sequence in DB
     * @param sequence
     */
    private void updateSequence(final Sequence sequence) {
        Sequence currentSeq = new Select().from(Sequence.class).where("seq_name = ?", Sequence.SEQNAME).executeSingle();
        if (currentSeq != null) {
            //Log.d(TAG, "Updating Sequence to " + sequence.getLastSequence());
            currentSeq.update(sequence).save();
        } else {
            //Log.d(TAG, "Creating Sequence with " + sequence.getLastSequence());
            sequence.save();
        }
    }

    /**
     * Applies deletions in DB
     * @param deletes
     */
    private void applyDeletes(List<Delete> deletes) {
        final String packagePrefix = "de.fhbingen.mensa.data.orm.";
        Class cls;
        for (final Delete d : deletes) {
            try {
                cls = Class.forName(packagePrefix + d.getTableName());

                final Field deleteField = cls.getField("DELETEID");
                deleteField.setAccessible(true);
                final String deleteIdentifier = deleteField.get(null).toString();

                //Log.v(TAG, "Delete " + cls.getCanonicalName() + " WHERE " + deleteIdentifier + " = " + d.getDeleteId());

                new com.activeandroid.query.Delete()
                        .from(cls)
                        .where(deleteIdentifier + " = ?", d.getDeleteId())
                        .execute();

            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * EventBus Callback; Listens on SettingsChangeEvents
     * @param event
     */
    public void onEvent(SettingsChangeEvent event) {
        //Log.d(TAG, "onEvent: SettingsChangeEvent: " + event.getChangePreference());

        // Run update if subscribed Buildings changed
        if(event.getChangePreference().equals(SettingsFragment.REF_KEY_BUILDINGS)) {
            doWork();
        }
    };

    /**
     * EventBus Callback; Listens on NetworkStatusEvents
     * @param event
     */
    public void onEvent(NetworkStatusEvent event){
        this.isConnected = event.isConnected();

        // TODO: Start doWork if online again?
    }

    /**
     * Do blocking network stuff in separate thread.
     */
    private void doWork(){

        if (!this.isConnected){
            Log.v(TAG, "doWork(); Skipping b.c. offline");
            return;
        }

        //Creating new thread for my service
        //Always write your long running tasks in a separate thread, to avoid ANR
        new Thread(new Runnable() {

            @Override
            public void run() {
                //Log.d(TAG, "run()");
                final UrlBuilder urlBuilder = new UrlBuilder();

                // Subscribed Buildings with BuildingSequence
                final SharedPreferences sharedPreferences = PreferenceManager
                        .getDefaultSharedPreferences(getApplicationContext());
                final Set<String> subscribedBuildings = sharedPreferences
                        .getStringSet(SettingsFragment.REF_KEY_BUILDINGS, null);

                boolean firstRun = true;
                if(subscribedBuildings != null){
                    String strBuildingSequence;
                    for(final String strBuildingId : subscribedBuildings){
                        strBuildingSequence = sharedPreferences.getString(
                                SettingsFragment.REF_PREFIX_BUILDINGSEQ + strBuildingId, "0");
                        urlBuilder.addBuildingWithSequence(strBuildingId, strBuildingSequence);
                    }
                    firstRun = false;
                }

                // Current Sequence
                Sequence seq = new Select()
                        .from(Sequence.class)
                        .where("seq_name = ?", Sequence.SEQNAME)
                        .executeSingle();
                if (seq == null) {
                    seq = new Sequence();
                    seq.save();
                }
                long seqNum = seq.getLastSequence();
                urlBuilder.setSequence(seqNum);

                // Get URL from Builder
                String url = urlBuilder.getChangesUrl();
                //Log.d(TAG, url);

                // Do REST call
                final RestTemplate restTemplate = new RestTemplate();
                restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
                Changes result = restTemplate.getForObject(url, Changes.class);

                //Log.d("myThread", "/GET");
                if (result.needToUpdate) {
                    Log.d("myThread", "#buildings: " + result.getBuildings().size());
                    Log.d("myThread", "#ingredien: " + result.getIngredients().size());
                    Log.d("myThread", "#dishes:    " + result.getDishes().size());
                    Log.d("myThread", "#deletes:   " + result.getDeletes().size());
                    Log.d("myThread", "#ratings:   " + result.getRatings().size());
                    Log.d("myThread", "#date:      " + result.getDates().size());
                    Log.d("myThread", "#photos:    " + result.getPhotos().size());
                    Log.d("myThread", "#offeredAt: " + result.getOfferedAt().size());
                }
                //Log.d("myThread", "sequence:   " + result.getSequence().getLastSequence());

                if(firstRun){
                    //Just update Buildings, skipping all other data and sequence
                    updateBuildings(result.getBuildings());

                    updateIngredients(result.getIngredients());
                } else {
                    //Update all data for subscribed buidlings
                    updateDatabase(result);

                    // Set BuildingSequences for further updates
                    final String strLastSequence = Long.toString(result.getSequence().getLastSequence());
                    final SharedPreferences.Editor editor = sharedPreferences.edit();
                    for(final String strBuildingId : urlBuilder.getBuildingsIds()){
                        editor.putString(SettingsFragment.REF_PREFIX_BUILDINGSEQ + strBuildingId, strLastSequence);
                    }
                    editor.apply();
                }
            }
        }).start();
    }

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {
        //Log.d(TAG, "onStartCommand");

        // Register EventBus if not
        if(!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }

        // Initial determination of internet state
        this.isConnected = ((Mensa)getApplication()).isConnected();

        // Do the work (in new thread)
        doWork();

        //Log.d(TAG, "onStartCommand -> return");
        return Service.START_NOT_STICKY;
    }

    private final IBinder mBinder = new MyBinder();

    @Override
    public IBinder onBind(Intent intent) {
        //Log.d(TAG, "onBind");
        // TODO: Return the communication channel to the service.
        //throw new UnsupportedOperationException("Not yet implemented");
        return mBinder;
    }

    public class MyBinder extends Binder {

        public UpdateContentService getService(){
            //Log.d(TAG, "MyBinder.getService");
            return UpdateContentService.this;
        }
    }

    /**
     * Builds URLs for pulling and pushing data.
     * (Pull plan, push ratings and pictures)
     */
    public static class UrlBuilder {
        public static final String BASE = "http://143.93.91.62";

        public static final String RATINGS = BASE + "/ratings";
        public static final String PHOTOS  = BASE + "/photos";
        public static final String COMPLAIN = PHOTOS + "/%d/complain";

        public UrlBuilder() {
            sequence = 0;
        }

        private long sequence;
        private final Map<String, String> mapOfBuildings = new HashMap<String, String>();

        public UrlBuilder setSequence(final long sequence){
            this.sequence = sequence;
            return this;
        }

        public UrlBuilder addBuildingWithSequence(final String buildingId, final String buildingSequence){
            mapOfBuildings.put(buildingId, buildingSequence);
            return this;
        }

        public Set<String> getBuildingsIds(){
            return mapOfBuildings.keySet();
        }

        public String getChangesUrl(){
            final StringBuilder sb = new StringBuilder();
            sb.append(BASE).append("/changes?seq=").append(sequence);

            for(final Map.Entry<String, String> entry : mapOfBuildings.entrySet()){
                sb.append("&" + entry.getKey() + "=" + entry.getValue());
            }

            return sb.toString();
        }

        public static String getPhotoURL(final long photoId){
            return PHOTOS + "/" + photoId;
        }

        public static String getComplainURL(final long photoId) {
            return String.format(COMPLAIN, photoId);
        }

    }

}
