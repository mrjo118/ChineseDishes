package cct.mad.lab.app;

import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;

public class MainActivity extends ListActivity
{
	private static final int REQUEST_CODE = 10;
	private DataSource datasource;
	boolean result;
	
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		Thread.setDefaultUncaughtExceptionHandler(new CustomExceptionHandler());
		setContentView(R.layout.activity_main);
	    datasource = new DataSource(this);
	    registerForContextMenu(getListView());//To create a context menu to manipulate comments
	    datasource.open();
        initialiseDB();
	    fillData();
	}

    private void initialiseDB() {
        boolean mboolean = false;
        SharedPreferences settings = getSharedPreferences("PREFS_NAME", 0);
        mboolean = settings.getBoolean("FIRST_RUN", false);
        if (!mboolean) {
            // do the thing for the first time
            AssetManager assetManager = getAssets();
            datasource.importDefaultDatabase(assetManager);

            settings = getSharedPreferences("PREFS_NAME", 0);
            SharedPreferences.Editor editor = settings.edit();
            editor.putBoolean("FIRST_RUN", true);
            editor.commit();
        }
    }

	private void fillData() { 	
	      Cursor c = datasource.getAllDishes();
	      CustomAdapter dishes = new CustomAdapter(this, c);
	      setListAdapter(dishes);
	}

	//Respond to item tapped
	protected void onListItemClick(ListView l, View v, int position, long id) {
		long itemid = getListAdapter().getItemId(position);
		// Create intent object
		Intent nextIntent = new Intent(this, AddEditActivity.class);
		nextIntent.putExtra("id", itemid);
		// Set the request code to any code you like, you can identify the callback via this code
	    startActivityForResult(nextIntent, REQUEST_CODE);
	}
	
	//Create OPTIONS menu
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	//Respond to item selected on OPTIONS MENU 
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
        case R.id.add:
        	//Toast.makeText(this, "Add selected", Toast.LENGTH_SHORT).show();
        	// Create intent object
			Intent nextIntent = new Intent(this, AddEditActivity.class);
		    // Set the request code to any code you like, you can identify the callback via this code
		    startActivityForResult(nextIntent, REQUEST_CODE);
            return true;
        case R.id.Import:
        	ImportPrompt(R.string.prompt_import);
            return true;
        case R.id.Export:
        	if (datasource.backupDatabase()) {
	            Toast.makeText(getBaseContext(), "Database was backed up successfully!", Toast.LENGTH_LONG).show();
	        }
	        else
	        {
	         	Toast.makeText(getBaseContext(), "An error occured when backing up the database", Toast.LENGTH_LONG).show();
	        }
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }
	
	//Create CONTEXT menu
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_context, menu); //Expand the menu options from the XML file
      }
    
    //Respond to item selected on the CONTEXT menu
    public boolean onContextItemSelected(MenuItem item) {
    	  AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
    	  switch (item.getItemId()) {  	  
    	  case R.id.edit:
    		//Toast.makeText(this, itemTitle+" "+values[info.position], Toast.LENGTH_SHORT).show();
    		// Create intent object
  			Intent nextIntent = new Intent(this, AddEditActivity.class);
  			nextIntent.putExtra("id", info.id);
  			// Set the request code to any code you like, you can identify the callback via this code
  		    startActivityForResult(nextIntent, REQUEST_CODE);
             return true;
    	  case R.id.delete:
    		datasource.deleteDish(info.id);
    		fillData(); // reload the list
    		Toast.makeText(this, "Dish deleted", Toast.LENGTH_SHORT).show();
      	    return true;
    	  default:
    	    return super.onContextItemSelected(item);
    	  }
    	}
	
    /*
	protected void onListItemClick(ListView l, View v, int position, long id) {
		String item = (String) getListAdapter().getItem(position);
		Toast.makeText(this, item + " was selected", Toast.LENGTH_LONG).show();
	}
	*/
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
	    if (resultCode == RESULT_OK && requestCode == REQUEST_CODE)
	    {
	    	String result = data.getExtras().getString("action");
	    	 if (result.equals("Add"))
	    	 {
	    		 datasource.open();
	    		 fillData();
	    		 Toast.makeText(this, "Dish added", Toast.LENGTH_LONG).show();
	    	 }
	    	 else if (result.equals("Update"))
	    	 {
	    		 datasource.open();
	    		 fillData();
	    		 Toast.makeText(this, "Dish updated", Toast.LENGTH_LONG).show();
	    	 }
	    }
	}
	
	protected void onResume() {
	    datasource.open();
	    super.onResume();
	  }

	  protected void onPause() {
	    datasource.close();
	    super.onPause();
	  }
	
	  public void ImportPrompt(int prompt)
	  {
	    	// Prompt for user feedback
		    AlertDialog.Builder builder = new AlertDialog.Builder(this);
		    builder.setTitle(R.string.prompt_import_title);
		    builder.setMessage(prompt)
				.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
				    public void onClick(DialogInterface dialog, int which) {
				    	// Yes is clicked
						if(datasource.importDatabase()) {
						    fillData();
						    Toast.makeText(getBaseContext(), "Database was imported successfully!", Toast.LENGTH_LONG).show();
						}
						else
						{
					  		Toast.makeText(getBaseContext(), "Database backup does not exist", Toast.LENGTH_LONG).show();
					  	}
				    }
			})
				.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
				    public void onClick(DialogInterface dialog, int which) {
				    	// No is clicked
				    }
			}).show();
	  }
	  
}
