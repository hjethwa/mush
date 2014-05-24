package com.lanluong.mush.selection;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.CommonDataKinds.Photo;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.lanluong.mush.R;

public class ContactsListAdapter extends ArrayAdapter<String> {

	private ArrayList<String> mNamesList = new ArrayList<String>();
	private ArrayList<String> mIdList = new ArrayList<String>();
	private int mLayoutResource;
	private Context mContext;

	public ContactsListAdapter(Context context, int resource,
			ArrayList<String> namesList, ArrayList<String> idList) {
		super(context, resource, namesList);
		mContext = context;
		mNamesList = namesList;
		mLayoutResource = resource;
		mIdList = idList;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View row = convertView;
		Holder holder = null;
		LayoutInflater inflater = (LayoutInflater) mContext
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		if (row == null) {
			row = inflater.inflate(mLayoutResource, parent, false);
			holder = new Holder();
			holder.contactImage = (ImageView) row
					.findViewById(R.id.contacts_list_row_image);
			holder.contactName = (TextView) row
					.findViewById(R.id.contacts_list_row_text);
			row.setTag(holder);
		} else {
			holder = (Holder) row.getTag();
		}
		holder.contactName.setText(mNamesList.get(position));
		holder.contactImage.setImageResource(R.drawable.ic_launcher);
		new PhotoTask(holder.contactImage).execute(mIdList.get(position));
		return row;
	}

	static class Holder {
		ImageView contactImage;
		TextView contactName;
	}

	private class PhotoTask extends AsyncTask<String, Void, Bitmap> {

		private WeakReference<ImageView> weakReferenceImage;

		public PhotoTask(ImageView imageView) {
			weakReferenceImage = new WeakReference<ImageView>(imageView);
		}

		@Override
		protected Bitmap doInBackground(String... params) {
			// Note: This works. _ID for some reason would equal to PHOTO_ID as this a different table.
	        ContentResolver cr = getContext().getContentResolver();
	        Cursor cur = cr.query(ContactsContract.Data.CONTENT_URI, new String[] {Photo.PHOTO},
	        		ContactsContract.Data._ID
					+ "="
					+ params[0], null, null);
	        final Bitmap photoBitmap;
			if(cur.moveToFirst()) {
				byte[] photoBlob = cur.getBlob(
		        		cur.getColumnIndex(Photo.PHOTO));
				photoBitmap = BitmapFactory.decodeByteArray(
						photoBlob, 0, photoBlob.length);
			} else {
				photoBitmap = null;
			}
			cur.close();
			return photoBitmap;
		}

		@Override
		protected void onPostExecute(Bitmap result) {
			super.onPostExecute(result);
			if (isCancelled()) {
				result = null;
			}

			if (weakReferenceImage != null) {
				ImageView imageView = weakReferenceImage.get();
				if (imageView != null) {

					if (result != null) {
						imageView.setImageBitmap(result);
					} else {
						imageView.setImageResource(R.drawable.ic_launcher);
					}
				}

			}
		}

	}
}
