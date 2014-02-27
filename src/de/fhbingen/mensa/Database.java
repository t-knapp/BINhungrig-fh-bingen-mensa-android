package de.fhbingen.mensa;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;

public class Database extends SQLiteOpenHelper {

	private static final int DATABASE_VERSION = 7;
	private static final String DATABASE_NAME = "MensaDB";

	public Database(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		//Create table for users dish ratings
		String createQuery = 
				"CREATE TABLE \"ratings\" ("
				+ "\"id_dishes\" INTEGER PRIMARY KEY NOT NULL,"
				+ "\"rating\" INTEGER NOT NULL" +
		        ");";
		db.execSQL(createQuery);

		//Create table complains about dish pictures
		createQuery = 
				"CREATE TABLE \"complains\" ("
			    + "\"id_pictures\" INTEGER PRIMARY KEY NOT NULL );";

		db.execSQL(createQuery);
	}

	public void insertRating(int id_dishes, int rating){
		final SQLiteDatabase db = this.getWritableDatabase();
		final String query = "INSERT INTO \"ratings\" (id_dishes, rating)" 
						   + "VALUES (?,?);";
		final SQLiteStatement statement = db.compileStatement(query);
		statement.bindLong(1, id_dishes);
		statement.bindLong(2, rating);
		statement.execute();
		
		db.close();
	}
	
	public int selectRating(int id_dishes){
		final SQLiteDatabase db = this.getReadableDatabase();
		
		final Cursor cursor = db.query("ratings", new String[] { "rating" }, "id_dishes = " + id_dishes, null, null, null, null, "1");
		
		int retval = -1;
		
		if(cursor.moveToFirst()){
			retval = cursor.getInt(0);
		}
		
		cursor.close();
		db.close();
		
		return retval;
	}
	
	public void insertComplain(int id_pictures){
		final SQLiteDatabase db = this.getWritableDatabase();
		
		final String query = "INSERT INTO \"complains\" (id_pictures)" 
						   + "VALUES (?);";
		
		final SQLiteStatement statement = db.compileStatement(query);
		statement.bindLong(1, id_pictures);
		statement.execute();
		
		db.close();
	}
	
	public boolean complainedAboutPicture(int id_pictures){
		final SQLiteDatabase db = this.getWritableDatabase();
		
		final String query = "SELECT \"id_pictures\""
				           + "FROM \"complains\""
				           + "WHERE  \"id_pictures\" = " + id_pictures;
		
		final Cursor cursor = db.rawQuery(query, null);
	    final int cnt = cursor.getCount();
	    
	    cursor.close();
	    db.close();
	    
	    return cnt > 0;
	}
		
	@Override
	public void onUpgrade(SQLiteDatabase db, 
			              int oldVersion,
			              int newVersion) {
		// Called on DATABASE_VERSION change.
		db.execSQL("DROP TABLE IF EXISTS \"ratings\";");
		db.execSQL("DROP TABLE IF EXISTS \"complains\";");
		onCreate(db);
	}

}
