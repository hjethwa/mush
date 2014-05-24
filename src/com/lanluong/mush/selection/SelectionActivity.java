package com.lanluong.mush.selection;

import static com.lanluong.mush.MushMainActivity.CONTACTS_CLICKED;
import static com.lanluong.mush.MushMainActivity.EMOTICONS_CLICKED;
import static com.lanluong.mush.MushMainActivity.SELECTION_CLICKED;
import static com.lanluong.mush.SendingActivity.IS_SENT;
import static com.lanluong.mush.SendingActivity.SENT_NEW;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TabHost.TabContentFactory;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;

import com.lanluong.mush.MushAnimationUtility;
import com.lanluong.mush.R;
import com.lanluong.mush.SendingActivity;

public class SelectionActivity extends FragmentActivity implements
		TabHost.OnTabChangeListener, ViewPager.OnPageChangeListener {

	public static final String CONTACTS_X = "CONTACTS-X";
	public static final String CONTACTS_Y = "CONTACTS-Y";
	public static final String EMOTICONS_X = "EMOTICONS-X";
	public static final String EMOTICONS_Y = "EMOTICONS-Y";
	public static final String LINEAR_TOP_X = "LINEAR-TOP-X";
	
	//Values used for intents and shared preferences
	public static final String CONTACT_SELECTED = "CONTACT-SELECTED";
	public static final String MUSH_SELECTED = "MUSH-SELECTED";
	public static final String CURRENT_SELECTION = "CURRENT_SELECTION";

	private TabHost mTabHost;
	private HashMap<String, TabInfo> mapTabInfo = new HashMap<String, SelectionActivity.TabInfo>();
	private ViewPager mViewPager;
	private PagerAdapter mPagerAdapter;
	private LinearLayout mLinearContactsBottom;
	private LinearLayout mLinearEmoticonsBottom;
	private LinearLayout mLinearTop;
	private ImageView mContactsImage;
	private ImageView mEmoticonsImage;
	private ListView mContactList;
	private TextView mActionBarTitle;

	private int mClicked;
	private String mContactNameSelected;

	private boolean contactSelected = false;
	private boolean emoticonSelected = false;

	private Selection currentSelection;
	private Selection previousSelection;

	private enum Selection {
		CONTACTS, EMOTICONS, CAUSE_ACTIVITY
	}

	private class TabInfo {
		private String tag;
		private Class<?> clazz;
		private Bundle args;
		private Fragment fragment;

		TabInfo(String tag, Class<?> clazz, Bundle args) {
			this.tag = tag;
			this.clazz = clazz;
			this.args = args;
		}
	}

	class TabFactory implements TabContentFactory {

		private final Context mContext;

		public TabFactory(Context context) {
			mContext = context;
		}

		public View createTabContent(String tag) {
			View v = new View(mContext);
			v.setMinimumWidth(0);
			v.setMinimumHeight(0);
			return v;
		}

	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.selection);

		// Setting action bar
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setDisplayShowTitleEnabled(false);
		actionBar.setDisplayShowCustomEnabled(true);
		actionBar.setCustomView(R.layout.action_bar);

		mActionBarTitle = (TextView) findViewById(R.id.action_bar_title_text);

		mLinearContactsBottom = (LinearLayout) findViewById(R.id.selection_contacts_linear_bottom);
		mLinearEmoticonsBottom = (LinearLayout) findViewById(R.id.selection_emoticons_linear_bottom);
		mLinearTop = (LinearLayout) findViewById(R.id.selection_top_linear);
		mTabHost = (TabHost) findViewById(android.R.id.tabhost);
		mContactsImage = (ImageView) findViewById(R.id.selection_contacts_image);
		mEmoticonsImage = (ImageView) findViewById(R.id.selection_emoticons_image);

		mContactList = (ListView) findViewById(R.id.selection_contacts_list);
		mContactList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				contactSelected = true;
				reloadScreen();
			}
		});

		// Setting the tabs
		this.initialiseTabHost(savedInstanceState);
		this.intialiseViewPager();

		mLinearContactsBottom.setVisibility(View.GONE);
		mLinearEmoticonsBottom.setVisibility(View.GONE);

		Intent causeIntent = getIntent();
		mClicked = causeIntent.getIntExtra(SELECTION_CLICKED, 0); // This is from previous activity
		mContactNameSelected = causeIntent.getStringExtra(CONTACT_SELECTED); // This is for reply from Mush head

	}

	@Override
	protected void onResume() {
		super.onResume();
		// Restoring state
		SharedPreferences preferences = PreferenceManager
				.getDefaultSharedPreferences(this);
		contactSelected = preferences.getBoolean(CONTACT_SELECTED, false);
		emoticonSelected = preferences.getBoolean(MUSH_SELECTED, false);
		String currentSelectionPreference = preferences.getString(CURRENT_SELECTION, "");
		currentSelection = currentSelectionPreference.equals(Selection.CONTACTS.name()) ? Selection.CONTACTS : Selection.EMOTICONS;
		// Case when back pressed from sending activity. Noticed that only
		// onPause is called when going to sending.
		if (contactSelected && emoticonSelected) {
			getActionBar().show(); // This is because when going to sending, it is hidden.
			switch (currentSelection) {
			case CONTACTS:
				// Showing as if I just clicked an emoticon. So i was on
				// selection.emoticons now going to contacts.
				contactSelected = false;
				currentSelection = Selection.EMOTICONS;
				previousSelection = Selection.CAUSE_ACTIVITY;
				break;
			case EMOTICONS:
				// reverse of above
				emoticonSelected = false;
				currentSelection = Selection.CONTACTS;
				previousSelection = Selection.CAUSE_ACTIVITY;
			default:
				break;
			}
		} else if (contactSelected && !emoticonSelected) {
			// When pressed home or call, message etc. For this and next
			// condition. Making it appear that I just came
			// from the selection. So setting current and previous selection
			// accordingly.
			currentSelection = Selection.CONTACTS;
			previousSelection = Selection.CAUSE_ACTIVITY;
		} else if (!contactSelected && emoticonSelected) {
			currentSelection = Selection.EMOTICONS;
			previousSelection = Selection.CAUSE_ACTIVITY;
		}
		//Else case means that this came from the main activity where I set
		//both booleans to be false. Or nothing was chosen when this activity was taken out of view.
		reloadScreen();
	}

	@Override
	protected void onPause() {
		super.onPause();
		// Saving State
		SharedPreferences preferences = PreferenceManager
				.getDefaultSharedPreferences(this);
		SharedPreferences.Editor preferencesEditor = preferences.edit();
		preferencesEditor.putBoolean(CONTACT_SELECTED, contactSelected);
		preferencesEditor.putBoolean(MUSH_SELECTED, emoticonSelected);
		preferencesEditor.putString(CURRENT_SELECTION, currentSelection.name());
		//reset for the next activity
		preferencesEditor.putInt(IS_SENT, SENT_NEW);
		preferencesEditor.commit();
	}

	private void intialiseViewPager() {
		List<Fragment> fragments = new Vector<Fragment>();
		fragments.add(Fragment.instantiate(this,
				FaceTabFragment.class.getName()));
		fragments.add(Fragment.instantiate(this,
				CatsTabFragment.class.getName()));
		fragments.add(Fragment.instantiate(this,
				FoodTabFragment.class.getName()));
		fragments.add(Fragment.instantiate(this,
				RandomTabFragment.class.getName()));
		this.mPagerAdapter = new PagerAdapter(
				super.getSupportFragmentManager(), fragments);
		this.mViewPager = (ViewPager) super.findViewById(R.id.tabviewpager);
		this.mViewPager.setAdapter(this.mPagerAdapter);
		this.mViewPager.setOnPageChangeListener(this);
	}

	private void initialiseTabHost(Bundle args) {
		mTabHost.setup();
		TabInfo tabInfo = null;
		SelectionActivity.AddTab(this, this.mTabHost, "Face",
				(tabInfo = new TabInfo("Face", FaceTabFragment.class, args)));
		this.mapTabInfo.put(tabInfo.tag, tabInfo);
		SelectionActivity.AddTab(this, this.mTabHost, "Cats",
				(tabInfo = new TabInfo("Cats", CatsTabFragment.class, args)));
		this.mapTabInfo.put(tabInfo.tag, tabInfo);
		SelectionActivity.AddTab(this, this.mTabHost, "Food",
				(tabInfo = new TabInfo("Food", FoodTabFragment.class, args)));
		this.mapTabInfo.put(tabInfo.tag, tabInfo);
		SelectionActivity
				.AddTab(this, this.mTabHost, "Random", (tabInfo = new TabInfo(
						"Random", RandomTabFragment.class, args)));
		this.mapTabInfo.put(tabInfo.tag, tabInfo);
		mTabHost.setOnTabChangedListener(this);
		mTabHost.getTabWidget().setDividerDrawable(null);
	}

	private static void AddTab(SelectionActivity activity, TabHost tabHost,
			String tabTitle, TabInfo tabInfo) {
		View tabview = createTabView(tabHost.getContext(), tabTitle);
		TabSpec setContent = tabHost.newTabSpec(tabTitle).setIndicator(tabview)
				.setContent(activity.new TabFactory(activity));
		tabHost.addTab(setContent);

	}

	private static View createTabView(final Context context, final String text) {
		View view = LayoutInflater.from(context).inflate(
				R.layout.tabs_emoticons_layout, null);
		TextView tv = (TextView) view
				.findViewById(R.id.emoticons_tabs_layout_text);
		tv.setText(text);
		return view;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			backOrActionBackPressed();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if ((keyCode == KeyEvent.KEYCODE_BACK)) {// Back Button
			backOrActionBackPressed();
		}
		return true;
	}

	private void backOrActionBackPressed() {
		//Both will be false. Coz think about all the scenarios. Back or ActionBack will go back to the previous selection.
		//coz if both are selected then there is no time to click back and next activity will be called.
		contactSelected = false;
		emoticonSelected = false;
		switch (previousSelection) {
		case CAUSE_ACTIVITY:
			finish(); // will call pause(),onStop(),onDestroy().
			break;
		case CONTACTS: // Doin this here and not in reloadScreen() because of extra animations.
			currentSelection = previousSelection;
			previousSelection = Selection.CAUSE_ACTIVITY;
			mLinearEmoticonsBottom.setVisibility(View.GONE);
			mLinearContactsBottom.setVisibility(View.VISIBLE);
			MushAnimationUtility.setAndStartViewFadeOutCompleteAnimation(this,
					mLinearEmoticonsBottom, null);
			MushAnimationUtility.setAndStartSlideUpAnimation(this,
					mLinearContactsBottom);
			MushAnimationUtility.setAndStartImageFadeOutAnimation(this,
					mEmoticonsImage);
			MushAnimationUtility.setAndStartImageFadeInAnimation(this,
					mContactsImage);
			mLinearContactsBottom.bringToFront();
			mActionBarTitle.setText("CHOOSE CONTACT");
			new ContactsTask().execute();
			// TODO Nullify both the images up top.
			break;

		case EMOTICONS: // Doin this here and not in reloadScreen() because of extra animations.
			currentSelection = previousSelection;
			previousSelection = Selection.CAUSE_ACTIVITY;
			mLinearEmoticonsBottom.setVisibility(View.VISIBLE);
			mLinearContactsBottom.setVisibility(View.GONE);
			MushAnimationUtility.setAndStartViewFadeOutCompleteAnimation(this,
					mLinearContactsBottom, null);
			MushAnimationUtility.setAndStartSlideUpAnimation(this,
					mLinearEmoticonsBottom);
			MushAnimationUtility.setAndStartImageFadeOutAnimation(this,
					mContactsImage);
			MushAnimationUtility.setAndStartImageFadeInAnimation(this,
					mEmoticonsImage);
			mLinearEmoticonsBottom.bringToFront();
			mActionBarTitle.setText("CHOOSE MUSH!");
			// TODO Nullify both images up top
			break;
		default:
			finish();
			break;
		}
	}

	@Override
	public void onPageScrollStateChanged(int arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onPageSelected(int position) {
		this.mTabHost.setCurrentTab(position);
	}

	@Override
	public void onTabChanged(String tabId) {
		int pos = this.mTabHost.getCurrentTab();
		this.mViewPager.setCurrentItem(pos);
	}

	public void onEmoticonClicked() {
		emoticonSelected = true;
		reloadScreen();
	}

	private void reloadScreen() {
		if (contactSelected && emoticonSelected) { // When both selected.
			final Intent sendingIntent = new Intent(this, SendingActivity.class);
			Animation.AnimationListener animationListener = new Animation.AnimationListener() {

				@Override
				public void onAnimationStart(Animation animation) {
				}

				@Override
				public void onAnimationRepeat(Animation animation) {

				}

				@Override
				public void onAnimationEnd(Animation animation) {
					getActionBar().hide();
					sendingIntent.putExtra(CONTACTS_X, mContactsImage.getX());
					sendingIntent.putExtra(CONTACTS_Y, mContactsImage.getY());
					sendingIntent.putExtra(EMOTICONS_X, mEmoticonsImage.getX());
					sendingIntent.putExtra(EMOTICONS_Y, mEmoticonsImage.getY());
					sendingIntent.putExtra(LINEAR_TOP_X, mLinearTop.getX());
//					// If home pressed or any interruption, then this activity will not be saved on the stack.
//					sendingIntent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY); 
					startActivity(sendingIntent);
					overridePendingTransition(0, 0);
				}
			};
			// Fade Out Animation depending on current selection.
			switch (currentSelection) {
			case CONTACTS:
				mLinearContactsBottom.setVisibility(View.GONE);
				MushAnimationUtility.setAndStartViewFadeOutCompleteAnimation(
						this, mLinearContactsBottom, animationListener);
				break;
			case EMOTICONS:
				mLinearEmoticonsBottom.setVisibility(View.GONE);
				MushAnimationUtility.setAndStartViewFadeOutCompleteAnimation(
						this, mLinearEmoticonsBottom, animationListener);
				break;
			default:
				break;
			}
		} else if (contactSelected && !emoticonSelected) {
			mLinearEmoticonsBottom.setVisibility(View.VISIBLE);
			mLinearContactsBottom.setVisibility(View.GONE);
			MushAnimationUtility.setAndStartViewFadeOutCompleteAnimation(this,
					mLinearContactsBottom, null);
			MushAnimationUtility.setAndStartSlideUpAnimation(this,
					mLinearEmoticonsBottom);
			MushAnimationUtility.setAndStartImageFadeOutAnimation(this,
					mContactsImage);
			MushAnimationUtility.setAndStartImageFadeInAnimation(this,
					mEmoticonsImage);
			mLinearEmoticonsBottom.bringToFront();
			previousSelection = currentSelection;
			currentSelection = Selection.EMOTICONS;
			mActionBarTitle.setText("CHOOSE MUSH!");
		} else if (!contactSelected && emoticonSelected) {
			mLinearEmoticonsBottom.setVisibility(View.GONE);
			mLinearContactsBottom.setVisibility(View.VISIBLE);
			MushAnimationUtility.setAndStartViewFadeOutCompleteAnimation(this,
					mLinearEmoticonsBottom, null);
			MushAnimationUtility.setAndStartSlideUpAnimation(this,
					mLinearContactsBottom);
			MushAnimationUtility.setAndStartImageFadeOutAnimation(this,
					mEmoticonsImage);
			MushAnimationUtility.setAndStartImageFadeInAnimation(this,
					mContactsImage);
			mLinearContactsBottom.bringToFront();
			previousSelection = currentSelection;
			currentSelection = Selection.CONTACTS;
			mActionBarTitle.setText("CHOOSE CONTACT");
			new ContactsTask().execute();
		} else {
			switch (mClicked) {
			case CONTACTS_CLICKED:
				currentSelection = Selection.CONTACTS;
				previousSelection = Selection.CAUSE_ACTIVITY;
				mLinearEmoticonsBottom.setVisibility(View.GONE);
				mLinearContactsBottom.setVisibility(View.VISIBLE);
				mLinearContactsBottom.bringToFront();
				MushAnimationUtility.setAndStartSlideUpAnimation(this,
						mLinearContactsBottom);
				MushAnimationUtility.setAndStartImageFadeOutAnimation(this,
						mEmoticonsImage);
				readAndDisplayAllContacts();
				mActionBarTitle.setText("CHOOSE CONTACT");
				break;
			case EMOTICONS_CLICKED:
				currentSelection = Selection.EMOTICONS;
				previousSelection = Selection.CAUSE_ACTIVITY;
				mLinearContactsBottom.setVisibility(View.GONE);
				mLinearEmoticonsBottom.setVisibility(View.VISIBLE);
				mLinearEmoticonsBottom.bringToFront();
				mTabHost.setOnTabChangedListener(this);
				MushAnimationUtility.setAndStartSlideUpAnimation(this,
						mLinearEmoticonsBottom);
				MushAnimationUtility.setAndStartImageFadeOutAnimation(this,
						mContactsImage);
				mActionBarTitle.setText("CHOOSE MUSH!");
				break;
			default: // Case when reply clicked from mush head
				currentSelection = Selection.CONTACTS; // Act as if the user came  from selection.contacts.
				contactSelected = true;
				emoticonSelected = false; //just an extra measure
				reloadScreen();
				break;
			}
		}
	}

		
	private class ContactsTask extends AsyncTask<Void, Void, Void>{

		@Override
		protected Void doInBackground(Void... params) {
			readAndDisplayAllContacts();
			return null;
		}
		
	}
	private void readAndDisplayAllContacts() {
		ArrayList<String> arrayList = new ArrayList<String>();
		ArrayList<String> idList = new ArrayList<String>();
		// Note Querying a different table here than for photo later in the adapter.
		Cursor cursor = getContentResolver().query(
				ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null,
				null,
				ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC");
		while (cursor.moveToNext()) {
			String name = cursor
					.getString(cursor
							.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
			String phoneNumber = cursor
					.getString(cursor
							.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
			arrayList.add(name + " - " + phoneNumber);
			String id = cursor
			.getString(cursor
					.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_ID));
			idList.add(id);
		}
		cursor.close();
		ContactsListAdapter adapter = new ContactsListAdapter(this,
				R.layout.contacts_list_row, arrayList, idList);
		mContactList.setAdapter(adapter);

	}
}
