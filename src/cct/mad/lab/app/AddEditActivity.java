package cct.mad.lab.app;
import java.io.ByteArrayOutputStream;

import android.os.Bundle;
import android.provider.MediaStore;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

@SuppressLint("DefaultLocale")
public class AddEditActivity extends Activity implements OnClickListener
{
	private static final int PICK_FROM_CAMERA = 1;
	private static final int PICK_FROM_GALLERY = 2;
	private DataSource datasource;
	EditText name;
	EditText desc;
	EditText price;
	TextView image;
	Button button;
	String action;
	Long id;
	Bitmap photo;
	boolean clear = false;
	
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		Thread.setDefaultUncaughtExceptionHandler(new CustomExceptionHandler());
		setContentView(R.layout.activity_add_edit);
		// Create a reference to the button
        Button button = (Button) findViewById(R.id.save);
        // Set the button to listen to onClick events
		button.setOnClickListener(this);
		// Get input fields
		name = (EditText)findViewById(R.id.input_name);
		desc = (EditText)findViewById(R.id.input_desc);
		price = (EditText)findViewById(R.id.input_price);
		image = (TextView)findViewById(R.id.image_name);
		
		datasource = new DataSource(this);
	    datasource.open();
	    
	    // Fill in data if edit dish was selected
		Bundle extras = getIntent().getExtras();
		if (extras == null)
		{
			setTitle(R.string.title_activity_add);
			action = "Add";
		}
		else
		{
			setTitle(R.string.title_activity_edit);
			action = "Update";
			id = extras.getLong("id");
			Cursor c = datasource.getDish(id);
			if (null!=c) {
                c.moveToNext();
                //Here you can directly set the value in textview
                name.setText(c.getString(c.getColumnIndex(DBHelper.COLUMN_NAME)));
                desc.setText(c.getString(c.getColumnIndex(DBHelper.COLUMN_DESC)));
                price.setText(String.format("%.2f", Float.parseFloat(c.getString(c.getColumnIndex(DBHelper.COLUMN_PRICE)))));
                if (c.getBlob(c.getColumnIndex(DBHelper.COLUMN_IMAGE)) != null)
                {
                	image.setText(R.string.image_selected);
                }
			}
		}
		
		// Bind intents to image selection buttons
		Button buttonCamera = (Button) findViewById(R.id.take_photo);
		Button buttonGallery = (Button) findViewById(R.id.select_photo);
		buttonCamera.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				// call android default camera
				Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

				intent.putExtra(MediaStore.EXTRA_OUTPUT,
						MediaStore.Images.Media.EXTERNAL_CONTENT_URI.toString());
				// ******** code for crop image
				intent.putExtra("crop", "true");
				intent.putExtra("aspectX", 1);
				intent.putExtra("aspectY", 1);
				intent.putExtra("outputX", 70);
				intent.putExtra("outputY", 70);

				try {

					intent.putExtra("return-data", true);
					startActivityForResult(intent, PICK_FROM_CAMERA);

				} catch (ActivityNotFoundException e) {
					// Do nothing for now
					Log.d("Tag",e.getMessage());
				}
			}
		});
		buttonGallery.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				Intent intent = new Intent();
				// call android default gallery
				intent.setType("image/*");
				intent.setAction(Intent.ACTION_GET_CONTENT);
				// ******** code for crop image
				intent.putExtra("crop", "true");
				intent.putExtra("aspectX", 1);
				intent.putExtra("aspectY", 1);
				intent.putExtra("outputX", 70);
				intent.putExtra("outputY", 70);

				try {
					intent.putExtra("return-data", true);
					startActivityForResult(Intent.createChooser(intent,
							"Complete action using"), PICK_FROM_GALLERY);

				} catch (ActivityNotFoundException e) {
					// Do nothing for now
					Log.d("Tag",e.getMessage());
				}
			}
		});
	}
	
	//Respond to image selected
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if (resultCode == RESULT_OK)
		{
			if (data != null) {
				Bundle extras = data.getExtras();
				photo = extras.getParcelable("data");
				TextView imagename = (TextView)findViewById(R.id.image_name);
		        imagename.setText(R.string.image_selected);
			}
		}
	}
	
	//Create OPTIONS menu
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.add_edit, menu);
		return true;
	}
	//Respond to item selected on OPTIONS MENU 
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle item selection
        switch (item.getItemId())
        {
	        case R.id.cancel:
	        	//Toast.makeText(this, "Cancel selected", Toast.LENGTH_SHORT).show();
	        	super.finish();
	            return true;
	        case R.id.clear:
	        	photo = null;
	        	clear = true;
	        	TextView imagename = (TextView)findViewById(R.id.image_name);
		        imagename.setText(R.string.empty);
	        	Toast.makeText(this, "Image cleared", Toast.LENGTH_SHORT).show();
	        	return true;
	        default:
	            return super.onOptionsItemSelected(item);
        }
    }
	
	public void onClick(View v)
	{
		// Set user input to variables
		String sName = name.getText().toString();
		String sDesc = desc.getText().toString();
		String sPrice = price.getText().toString();
		byte[] sImage;
		if (photo != null)
		{
			sImage = getBitmapAsByteArray(photo);
		}
		else
		{
			sImage = null;
		}
		
		// Validation
		if (sName.length() == 0) // Check if name is empty
		{
			PopupError(R.string.error_name);
		}
		else if (sDesc.length() == 0) // Check if description is empty
		{
			PopupError(R.string.error_desc);
		}
		else if (sPrice.length() == 0) // Check if price is empty
		{
			PopupError(R.string.error_price_1);
		}
		else if (sPrice.replace(".","").length() > 10) // Check the length of price
		{
			PopupError(R.string.error_price_2);
		}
		else if (sPrice.substring(sPrice.indexOf(".") + 1).length() > 2 && sPrice.replace(".","").length() != sPrice.length()) // Check if price has more than 2 decimal places
		{
			PopupError(R.string.error_price_3);
		}
		else
		{
			sPrice = String.format("%.2f", Float.parseFloat(sPrice));
			
			if (action.equals("Add"))
			{
				datasource.createDish(sName, sDesc, sPrice, sImage);
			}
			else if (action.equals("Update"))
			{
				datasource.updateDish(sName, sDesc, sPrice, sImage, id, clear);
			}
			
			Intent retData = new Intent();
			retData.putExtra("action", action);
		    setResult(RESULT_OK, retData);
		    super.finish();
		}
	}
	
	private byte[] getBitmapAsByteArray(Bitmap bitmap)
	{
		   ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

		   // Middle parameter is quality, but since PNG is lossless, it doesn't matter
		   bitmap.compress(CompressFormat.PNG, 0, outputStream);
		   return outputStream.toByteArray();
	} 
	
    public void PopupError(int error)
    {
    	// Alert error message
	    AlertDialog.Builder builder = new AlertDialog.Builder(this);
	    builder.setTitle(R.string.error_title);
	    builder.setMessage(error)
			.setCancelable(false)
			.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener()
			{
		    	public void onClick(DialogInterface dialog, int id)
		    	{
		    		dialog.cancel();
		    	}
			});
		AlertDialog alert = builder.create();
		alert.show();
    }
}
