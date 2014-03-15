package cct.mad.lab.app;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.util.Log;

//Class to reference the database and provide methods to interact with the data
public class DataSource {

  // Database fields
  private SQLiteDatabase database;
  private DBHelper dbHelper;
  private String[] allColumns = { DBHelper.COLUMN_ID, DBHelper.COLUMN_NAME, DBHelper.COLUMN_DESC, DBHelper.COLUMN_PRICE, DBHelper.COLUMN_IMAGE };
  private FileInputStream fileInputStream;
  private FileOutputStream fileOutputStream;
  String dbPath = "/data/cct.mad.lab.app/databases/";
  String backupDir = "/dishes_backup";

  public DataSource(Context context) {
    dbHelper = new DBHelper(context);
  }
  public void open() throws SQLException {
    database = dbHelper.getWritableDatabase();
  }
  
  //Method to Query the data 
  public Cursor getAllDishes() {
	return  database.query(DBHelper.TABLE_DISH,
		        allColumns, null, null, null, null, DBHelper.COLUMN_NAME + " COLLATE NOCASE");
  }
  
  public Cursor getDish(long id) {
		return  database.query(DBHelper.TABLE_DISH,
			        allColumns, DBHelper.COLUMN_ID + " = " + id, null, null, null, null);
	  }

  public long createDish(String Name, String Desc, String Price, byte[] Image) {
      ContentValues values = new ContentValues();
      values.put(DBHelper.COLUMN_NAME, Name);
      values.put(DBHelper.COLUMN_DESC, Desc);
      values.put(DBHelper.COLUMN_PRICE, Price);
      if (Image != null)
      {
    	  values.put(DBHelper.COLUMN_IMAGE, Image);
      }
      else
      {
    	  values.putNull(DBHelper.COLUMN_IMAGE);
      }
      return database.insert(DBHelper.TABLE_DISH, null,values);
  }
  
  public long updateDish(String Name, String Desc, String Price, byte[] Image, long id, boolean Clear) {
      ContentValues values = new ContentValues();
      values.put(DBHelper.COLUMN_NAME, Name);
      values.put(DBHelper.COLUMN_DESC, Desc);
      values.put(DBHelper.COLUMN_PRICE, Price);
      if (Image != null)
      {
		  values.put(DBHelper.COLUMN_IMAGE, Image);
      }
      else if (Image == null && Clear == true)
      {
		  values.putNull(DBHelper.COLUMN_IMAGE);
	  }
      return database.update(DBHelper.TABLE_DISH, values, DBHelper.COLUMN_ID
      + " = " + id, null);
  }

  public void deleteDish(long id) {
      database.delete(DBHelper.TABLE_DISH, DBHelper.COLUMN_ID
      + " = " + id, null);
  }

  public void close() {
    dbHelper.close();
  }
  
  boolean backupDatabase() {
	 try{
		 File sd = Environment.getExternalStorageDirectory();
	     File dir = new File (sd.getAbsolutePath() + backupDir);
	     dir.mkdirs();
	     File data = Environment.getDataDirectory();
	     if (sd.canWrite()) {
	         String currentDBPath = dbPath + DBHelper.DATABASE_NAME;
	         String backupDBPath = DBHelper.DATABASE_NAME;
	         File currentDB = new File(data, currentDBPath);
	         File backupDB = new File(dir, backupDBPath);
	
	         fileInputStream = new FileInputStream(currentDB);
			 FileChannel src = fileInputStream.getChannel();
	         fileOutputStream = new FileOutputStream(backupDB);
			 FileChannel dst = fileOutputStream.getChannel();
	         dst.transferFrom(src, 0, src.size());
	         src.close();
	         dst.close();
	         return true;
	     }
	     else
	     {
	    	 return false;
	     }
	 }
	 catch (IOException e) {
		 Log.d("Tag",e.getMessage());
	     return false;
	 }
  }
  
  boolean importDatabase() {
	  try{
		  File sd = Environment.getExternalStorageDirectory();
	      File backupDB = new File (sd.getAbsolutePath() + backupDir, DBHelper.DATABASE_NAME);
	      File data = Environment.getDataDirectory();
	      File currentDB = new File (data.getAbsolutePath() + dbPath + DBHelper.DATABASE_NAME);
	      InputStream from = new FileInputStream(backupDB);
	      OutputStream to = new FileOutputStream(currentDB);
	      if (backupDB.exists()) {
		      byte[] buffer = new byte[1024];
		      int bytesRead;
		      while ((bytesRead = from.read(buffer))>0)
		      {
		          to.write(buffer, 0, bytesRead);
		      }
		      to.flush();
		      to.close();
		      from.close();
		      return true;
	      }
	      else
	      {
	    	  to.flush();
		      to.close();
		      from.close();
	    	  return false;
	      }
	  }
	  catch (IOException e) {
		  Log.d("Tag",e.getMessage());
		  return false;
	  }
  }

    boolean importDefaultDatabase(AssetManager assetManager) {
        try{
            InputStream inputStream = assetManager.open("database/" + DBHelper.DATABASE_NAME);
            File data = Environment.getDataDirectory();
            File f = new File(data.getAbsolutePath() + dbPath + DBHelper.DATABASE_NAME);
            OutputStream outputStream = new FileOutputStream(f);
            byte buffer[] = new byte[1024];
            int length = 0;

            while((length=inputStream.read(buffer)) > 0) {
                outputStream.write(buffer,0,length);
            }

            outputStream.close();
            inputStream.close();
            return true;
        }
        catch (IOException e) {
            Log.d("Tag",e.getMessage());
            return false;
        }
    }

} 