package cct.mad.lab.app;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
/*
 * This class is responsible for creating the database. 
 * It also defines several constants for the table name and the table columns
 * This could be a private class within CommentsDataSource
 */
public class DBHelper extends SQLiteOpenHelper {//Note subclass

	  // After making changes to the table, always uninstall the app to reset the table schema
	  public static final String TABLE_DISH = "dish";
	  public static final String COLUMN_ID = "_id"; // MUST BE CALLED _id
	  public static final String COLUMN_NAME = "name";
	  public static final String COLUMN_DESC = "description";
	  public static final String COLUMN_PRICE = "price";
	  public static final String COLUMN_IMAGE = "image";

	  public static final String DATABASE_NAME = "dishes.db";
	  private static final int DATABASE_VERSION = 1;

	  // Database creation sql statement
	  private static final String DATABASE_CREATE = "create table "
	      + TABLE_DISH + "(" + COLUMN_ID
	      + " integer primary key autoincrement, " + COLUMN_NAME
	      + " text not null, " + COLUMN_DESC
	      + " text not null, " + COLUMN_PRICE
	      + " real not null, " + COLUMN_IMAGE
	      + " blob);";

	  public DBHelper (Context context) {
	    super(context, DATABASE_NAME, null, DATABASE_VERSION);
	  }
      //Must override this method
	  public void onCreate(SQLiteDatabase database) {
	    database.execSQL(DATABASE_CREATE);
	  }

	  //The onUpgrade() method will simply delete all existing data and re-create the table.
	//Must override this method
	  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	    Log.w(DBHelper.class.getName(),
	        "Upgrading database from version " + oldVersion + " to "
	            + newVersion + ", which will destroy all old data");
	    db.execSQL("DROP TABLE IF EXISTS " + TABLE_DISH);
	    onCreate(db);
	  }
	
	} 