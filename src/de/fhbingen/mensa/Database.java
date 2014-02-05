package de.fhbingen.mensa;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

public class Database extends SQLiteOpenHelper {

	// Database Version
	private static final int DATABASE_VERSION = 6;
	// Database Name
	private static final String DATABASE_NAME = "MensaDB";

	// Logcat TAG
	private static final String TAG = "Database";

	public Database(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		//Create table for dishes
		String createQuery = "CREATE TABLE \"dishes\" ("
				+ "\"id\" INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,"
				+ "\"date\" TEXT," + "\"name\" TEXT," + "\"rating\" REAL"
				+ ");";
		db.execSQL(createQuery);

		//Create table for pictures, pictures will be stored as BLOB
		createQuery = "CREATE TABLE \"pictures\" ("
				+ "\"id\" INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,"
				+ "\"dish_id\" INTEGER NOT NULL," 
				+ "\"data\" BLOB  NOT NULL,"
				+ "\"user\" TEXT NOT NULL,"
				+ "UNIQUE (id, dish_id)" + ");";

		db.execSQL(createQuery);
	}

	public void addDish(String name, float rating) {
		Log.i(TAG, "addDish");

		SQLiteDatabase db = this.getWritableDatabase();

		//Insert values, prepared statement would be more secure
		final String query = "INSERT INTO dishes (name, rating)" +
				             "VALUES (\""+ name + "\", " + rating + ");";

		db.execSQL(query);
	}

	public byte[] getDishPhoto(int dish_id) {
		Log.i(TAG, "getDishPhoto");

		SQLiteDatabase db = this.getReadableDatabase();

		Cursor cursor = db.query("pictures", new String[] { "data" }, null,
				null, null, null, "id DESC", "1");
		cursor.moveToFirst();

		return cursor.getBlob(0);
	}

	public void addDishPhoto(int dish_id, byte[] data) {
		Log.i(TAG, "addDishPhoto");

		SQLiteDatabase db = this.getWritableDatabase();

		String query = "INSERT INTO pictures (dish_id, data, user)" +
				       "VALUES (?,?,?);";
		
		//Prepared statements
		SQLiteStatement statement = db.compileStatement(query);

		statement.bindLong(1, dish_id);
		statement.bindBlob(2, data);
		statement.bindString(3, "t.knapp");

		statement.execute();
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, 
			              int oldVersion,
			              int newVersion) {
		// Called on DATABASE_VERSION change.
		db.execSQL("DROP TABLE IF EXISTS \"dishes\";");
		db.execSQL("DROP TABLE IF EXISTS \"pictures\";");
		onCreate(db);
	}

}
