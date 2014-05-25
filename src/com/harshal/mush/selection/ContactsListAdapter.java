package com.harshal.mush.selection;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Photo;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.lanluong.mush.R;

public class ContactsListAdapter extends ArrayAdapter<String> {

	private List<String> mNamesList = new ArrayList<String>();
	private List<String> mIdList = new ArrayList<String>();
	private Map<Integer, Bitmap> mBitmapMap = new HashMap<Integer, Bitmap>();
	private int mLayoutResource;
	private Context mContext;

	public ContactsListAdapter(Context context, int resource,
			List<String> namesList, List<String> idList) {
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
		new PhotoTask(holder.contactImage,position).execute(mIdList.get(position));
		return row;
	}

	static class Holder {
		ImageView contactImage;
		TextView contactName;
	}

	private class PhotoTask extends AsyncTask<String, Void, Bitmap> {

		private WeakReference<ImageView> mWeakReferenceImage;
		private WeakReference<Integer> mWeakReferencePosition;
		//private int position;

		public PhotoTask(ImageView imageView, Integer position) {
			mWeakReferenceImage = new WeakReference<ImageView>(imageView);
			mWeakReferencePosition = new WeakReference<Integer>(position);
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

			if (mWeakReferenceImage != null && mWeakReferencePosition != null) {
				ImageView imageView = mWeakReferenceImage.get();
				Integer pos = mWeakReferencePosition.get();
				if (imageView != null && pos != null) {
					if (result != null) {
						imageView.setImageBitmap(result);
						mBitmapMap.put(pos, result);
					} else {
						mBitmapMap.put(pos, BitmapFactory.decodeResource(getContext().getResources(), R.drawable.ic_launcher));
						imageView.setImageResource(R.drawable.ic_launcher);
					}
				}
			}
		}

	}
	
	public Map<Integer,Bitmap> getMap(){
		return mBitmapMap;
	}
}
