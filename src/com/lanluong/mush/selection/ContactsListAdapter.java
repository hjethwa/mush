package com.lanluong.mush.selection;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.ContactsContract;
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

	private class PhotoTask extends AsyncTask<String, Void, Uri> {

		private WeakReference<ImageView> weakReferenceImage;

		public PhotoTask(ImageView imageView) {
			weakReferenceImage = new WeakReference<ImageView>(imageView);
		}

		@Override
		protected Uri doInBackground(String... params) {
			try {
				Cursor cur = getContext()
						.getContentResolver()
						.query(ContactsContract.Data.CONTENT_URI,
								null,
								ContactsContract.Data.CONTACT_ID
										+ "="
										+ params[0]
										+ " AND "
										+ ContactsContract.Data.MIMETYPE
										+ "='"
										+ ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE
										+ "'", null, null);
				if (cur != null) {
					if (!cur.moveToFirst()) {
						return null; // no photo
					}
				} else {
					return null; // error in cursor process
				}
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
			Uri person = ContentUris.withAppendedId(
					ContactsContract.Contacts.CONTENT_URI,
					Long.parseLong(params[0]));
			return Uri.withAppendedPath(person,
					ContactsContract.Contacts.Photo.CONTENT_DIRECTORY);
		}

		@Override
		protected void onPostExecute(Uri result) {
			super.onPostExecute(result);
			if (isCancelled()) {
				result = null;
			}

			if (weakReferenceImage != null) {
				ImageView imageView = weakReferenceImage.get();
				if (imageView != null) {

					if (result != null) {
						imageView.setImageURI(result);
					} else {
						imageView.setImageResource(R.drawable.ic_launcher);
					}
				}

			}
		}

	}
}
