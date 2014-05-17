package com.lanluong.mush;

import java.io.IOException;

import static com.lanluong.mush.selection.SelectionActivity.CONTACT_SELECTED;
import static com.lanluong.mush.selection.SelectionActivity.MUSH_SELECTED;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.lanluong.mush.selection.SelectionActivity;

public class MushMainActivity extends Activity {
	
	private String mRegId;
	private static String mProjectNumber = "149282363445";
	
	public static final int REQUEST_CODE = 1;
	public static final int CONTACTS_CLICKED = 100;
	public static final int EMOTICONS_CLICKED = 101;
	public static final String SELECTION_CLICKED = "Selection";
	
	private ImageButton mContactsImageButton;
	private ImageButton mEmoticonsImageButton;
	private ImageButton mSettingsImageButton;
	private ImageButton mHistoryImageButton;
	
	private GoogleCloudMessaging mGcm;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.mush_main_activity);
		
		//Hiding the action bar
		getActionBar().hide();
		
		mContactsImageButton = (ImageButton) findViewById(R.id.mush_main_contacts_image_button);
		mContactsImageButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(MushMainActivity.this, SelectionActivity.class);
				intent.putExtra(SELECTION_CLICKED, CONTACTS_CLICKED);
				startActivity(intent);
			}
		});
		mEmoticonsImageButton = (ImageButton) findViewById(R.id.mush_main_emoticons_image_button);
		mEmoticonsImageButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(MushMainActivity.this, SelectionActivity.class);
				intent.putExtra(SELECTION_CLICKED, EMOTICONS_CLICKED);
				startActivity(intent);
			}
		});
		mSettingsImageButton = (ImageButton) findViewById(R.id.mush_main_settings_image_button);
		mSettingsImageButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(MushMainActivity.this, SettingsActivity.class);
				startActivity(intent);
			}
		});
		mHistoryImageButton = (ImageButton) findViewById(R.id.mush_main_history_image_button);
		mHistoryImageButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(MushMainActivity.this, HistoryActivity.class);
				startActivity(intent);
			}
		});
		
		//Clearing any saved states. No need for current selection. Coz look at the logic in selection activity.
		// will switch on current selection only if both bools are set.
		SharedPreferences preferences = PreferenceManager
				.getDefaultSharedPreferences(this);
		SharedPreferences.Editor preferencesEditor = preferences.edit();
		preferencesEditor.putBoolean(CONTACT_SELECTED, false);
		preferencesEditor.putBoolean(MUSH_SELECTED, false);
		preferencesEditor.commit();
}

	public void getRegId() {
		new AsyncTask<Void, Void, String>() {
			@Override
			protected String doInBackground(Void... params) {
				String msg = "";
				try {
					if (mGcm == null) {
						mGcm = GoogleCloudMessaging
								.getInstance(getApplicationContext());
					}
					mRegId = mGcm.register(mProjectNumber);
					msg = "Device registered, registration ID=" + mRegId;
					Log.i("GCM", msg);

				} catch (IOException ex) {
					msg = "Error :" + ex.getMessage();

				}
				return msg;
			}
		}.execute(null, null, null);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
	}

}
