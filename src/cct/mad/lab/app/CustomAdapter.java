package cct.mad.lab.app;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class CustomAdapter extends CursorAdapter {
	
	public CustomAdapter(Context context, Cursor c) {
		super(context, c);
	}
 
	public void bindView(View view, Context context, Cursor cursor) {
		ImageView image = (ImageView)view.findViewById(R.id.icon);
		if (cursor.getBlob(cursor.getColumnIndex(DBHelper.COLUMN_IMAGE)) != null)
		{
			byte[] bitmapData = cursor.getBlob(cursor.getColumnIndex(DBHelper.COLUMN_IMAGE));
			Bitmap photo = BitmapFactory.decodeByteArray(bitmapData, 0, bitmapData.length);
			image.setImageBitmap(photo);
		}
		else
		{
			image.setImageResource(R.drawable.defaultimg);
		}
		TextView name = (TextView)view.findViewById(R.id.item_text);
		name.setText(cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_NAME)));
		
	}
 
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		LayoutInflater inflater = LayoutInflater.from(context);
		View v = inflater.inflate(R.layout.dishrow, parent, false);
		bindView(v, context, cursor);
		return v;
	}
	
}
