package com.harshal.mush;

import static com.harshal.mush.selection.SelectionActivity.CONTACTS_X;
import static com.harshal.mush.selection.SelectionActivity.CONTACTS_Y;
import static com.harshal.mush.selection.SelectionActivity.EMOTICONS_X;
import static com.harshal.mush.selection.SelectionActivity.EMOTICONS_Y;
import static com.harshal.mush.selection.SelectionActivity.LINEAR_TOP_X;

import com.lanluong.mush.R;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.TypedValue;
import android.view.Display;
import android.view.Surface;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class SendingActivity extends Activity {
	
	public static final String IS_SENT = "IS-SENT";
	public static final int SENT_DONE = 100;
	public static final int SENT_IN_PROGRESS = 101;
	public static final int SENT_NEW = 102;
	
	ImageView mContactsImage;
	ImageView mEmoticonsImage;
	ImageView mArrowImage;
	TextView mSentView;
	RelativeLayout mRelativeLayout;
	AnimationSet mArrowAnimations;
	float mScreenWidth;
	float mScreenHeight;
	float mActionBarHeight;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.sending);
		
		mContactsImage = (ImageView) findViewById(R.id.sending_contacts_image);
		mEmoticonsImage = (ImageView) findViewById(R.id.sending_emoticons_image);
		mArrowImage = (ImageView) findViewById(R.id.arrow_image);
		mArrowImage.setVisibility(View.INVISIBLE);
		mSentView = (TextView) findViewById(R.id.sending_sent_image);
		mSentView.setVisibility(View.INVISIBLE);
		
		mSentView.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				startMainActivity();
			}
		});
		
		Display display = getWindowManager().getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		mScreenHeight = size.y;
		mScreenWidth = size.x;
		
		final Intent causeIntent = getIntent();
		
		//Hide Action Bar by calculating its height to add in the values provided by intent.
		TypedValue tv = new TypedValue();
		if (getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true))
		{
			mActionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data,getResources().getDisplayMetrics());
		}
		getActionBar().hide();
		
		mRelativeLayout = (RelativeLayout) findViewById(R.id.sending_frame);
		
		//Getting values from previous activity and setting both the images accordingly.
		Bundle intentExtras = causeIntent.getExtras();
		float linearTopX = intentExtras.getFloat(LINEAR_TOP_X);
		float contactsY = (intentExtras.getFloat(CONTACTS_Y) + mActionBarHeight);
		final float contactsX = (intentExtras.getFloat(CONTACTS_X) + linearTopX);
		RelativeLayout.LayoutParams contactsParams = (RelativeLayout.LayoutParams) mContactsImage.getLayoutParams();
		contactsParams.leftMargin = (int)contactsX;
		contactsParams.topMargin = (int)contactsY;
		mContactsImage.setLayoutParams(contactsParams);
		
		final float emoticonsX = (intentExtras.getFloat(EMOTICONS_X) + linearTopX);
		final float emoticonsY = (intentExtras.getFloat(EMOTICONS_Y) + mActionBarHeight);
		RelativeLayout.LayoutParams emoticonsParams = (RelativeLayout.LayoutParams) mEmoticonsImage.getLayoutParams();
		emoticonsParams.leftMargin = (int)emoticonsX;
		emoticonsParams.topMargin = (int)emoticonsY;
		mEmoticonsImage.setLayoutParams(emoticonsParams);
		
		
		//0.57f value seems right. Trial and error. same below for -0.57f
		TranslateAnimation contactsAnimation = 
				new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0,
						Animation.RELATIVE_TO_SELF, 0.57f,
						Animation.RELATIVE_TO_SELF, 0,
						Animation.RELATIVE_TO_PARENT, 0.5f);
		contactsAnimation.setDuration(700);
		contactsAnimation.setInterpolator(new AccelerateInterpolator());
		contactsAnimation.setStartOffset(500);
		contactsAnimation.setFillAfter(true);
		contactsAnimation.setAnimationListener(new AnimationListener() {
			
			@Override
			public void onAnimationStart(Animation animation) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onAnimationRepeat(Animation animation) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onAnimationEnd(Animation animation) {
				mArrowAnimations = new AnimationSet(false);
				RelativeLayout.LayoutParams arrowParams = (RelativeLayout.LayoutParams) mArrowImage.getLayoutParams();
				//mEmoticonsImage.getMeasuredWidth() for emoticons. 0.93 for the padding.
				arrowParams.leftMargin = (int) ((int)contactsX + mEmoticonsImage.getMeasuredWidth() * 0.93);
				arrowParams.topMargin = (int)emoticonsY + 50;
				mArrowImage.setLayoutParams(arrowParams);
				TranslateAnimation arrowTranslateAnimation = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_PARENT, 0.5f);
				arrowTranslateAnimation.setDuration(1000);
				arrowTranslateAnimation.setInterpolator(new AccelerateInterpolator());
				arrowTranslateAnimation.setFillAfter(false);
				arrowTranslateAnimation.setRepeatCount(Animation.INFINITE);
				ScaleAnimation arrowScaleAnimation = new ScaleAnimation(1f, 0.5f, 1f, 0.5f,Animation.RELATIVE_TO_SELF, 0.5f,Animation.RELATIVE_TO_SELF, 1f);
				arrowScaleAnimation.setDuration(1000);
				arrowScaleAnimation.setInterpolator(new AccelerateInterpolator());
				arrowScaleAnimation.setFillAfter(false);
				arrowScaleAnimation.setFillBefore(false);
				arrowScaleAnimation.setRepeatCount(Animation.INFINITE);
				mArrowAnimations.addAnimation(arrowScaleAnimation);
				mArrowAnimations.addAnimation(arrowTranslateAnimation);
				mArrowImage.setVisibility(View.VISIBLE);
				mArrowImage.startAnimation(mArrowAnimations);
				new sendTask().execute();
			}
		});
		mContactsImage.startAnimation(contactsAnimation);
		
		TranslateAnimation emoticonsAnimation = 
				new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0,
						Animation.RELATIVE_TO_SELF, -0.57f,
						Animation.RELATIVE_TO_SELF, 0,
						Animation.RELATIVE_TO_PARENT, 0);
		emoticonsAnimation.setDuration(700);
		emoticonsAnimation.setInterpolator(new AccelerateInterpolator());
		emoticonsAnimation.setFillAfter(true);
		emoticonsAnimation.setStartOffset(500);
		mEmoticonsImage.startAnimation(emoticonsAnimation); 
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		SharedPreferences preferences = PreferenceManager
				.getDefaultSharedPreferences(this);
		SharedPreferences.Editor preferencesEditor = preferences.edit();
		int isSent = preferences.getInt(IS_SENT, SENT_NEW);
		if (isSent == SENT_DONE){ // Case when sent shown on screen but not clicked
			startMainActivity();
		} else if (isSent == SENT_IN_PROGRESS){ // Case when interruption occurs before sending finished.
			finish(); // goes back to revious activity.
		} else if (isSent == SENT_NEW){ // Case when this intent comes from previous activity
			preferencesEditor.putInt(IS_SENT, SENT_IN_PROGRESS);
			preferencesEditor.commit();
			lockOrientation(this);
		}
	}
	
	private void startMainActivity(){
		Intent firstScreenIntent = new Intent(SendingActivity.this, MushMainActivity.class);
		firstScreenIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
		startActivity(firstScreenIntent);
	}
	
	public static void lockOrientation(Activity activity) {
	    Display display = ((WindowManager) activity.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
	    int rotation = display.getRotation();
	    int tempOrientation = activity.getResources().getConfiguration().orientation;
	    int orientation = 0;
	    switch(tempOrientation)
	    {
	    case Configuration.ORIENTATION_LANDSCAPE:
	        if(rotation == Surface.ROTATION_0 || rotation == Surface.ROTATION_90)
	            orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
	        else
	            orientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
	        break;
	    case Configuration.ORIENTATION_PORTRAIT:
	        if(rotation == Surface.ROTATION_0 || rotation == Surface.ROTATION_270)
	            orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
	        else
	            orientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;
	    }
	    activity.setRequestedOrientation(orientation);
	}
	
	private class sendTask extends AsyncTask<Void, Void, Void>{
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		@Override
		protected Void doInBackground(Void... params) {
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			return null;
		}
		
		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			AlphaAnimation sentAnimation = new AlphaAnimation(0f, 1.0f);
			sentAnimation.setDuration(1000);
			sentAnimation.setInterpolator(new AccelerateInterpolator());
			sentAnimation.setFillAfter(true);
			mArrowImage.clearAnimation();
			mSentView.setVisibility(View.VISIBLE);
			mRelativeLayout.removeView(mArrowImage);
			mRelativeLayout.removeView(mContactsImage);
			mRelativeLayout.removeView(mEmoticonsImage);
			mSentView.startAnimation(sentAnimation);
			SharedPreferences preferences = PreferenceManager
					.getDefaultSharedPreferences(SendingActivity.this);
			SharedPreferences.Editor preferencesEditor = preferences.edit();
			preferencesEditor.putInt(IS_SENT, SENT_DONE);
			preferencesEditor.commit();
		}
		
	}
}
