package com.harshal.mush.gcm;

import static com.harshal.mush.selection.SelectionActivity.CONTACT_SELECTED;
import android.R;
import android.app.ActionBar.LayoutParams;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.harshal.mush.selection.SelectionActivity;

public class GcmMessageHandler extends IntentService {

	String mes;
	String title;
	private WindowManager mWindowManager;
	private TextView mMushHead;
	private Button mReplyButton;
	private Button mDeleteButton;
	private LinearLayout.LayoutParams mDeleteButtonParams;
	private LinearLayout.LayoutParams mReplyButtonParams;
	private LinearLayout mLinearLayout;
	private WindowManager.LayoutParams mLinearLayoutParams;
	private WindowManager.LayoutParams mMushHeadParams;
	private boolean mIsLinearShown = false;
	private Handler mMainHandler;
	private GestureDetector mDetector;

	public GcmMessageHandler() {
		super("GcmMessageHandler");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		Bundle extras = intent.getExtras();
		GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
		String messageType = gcm.getMessageType(intent);
		title = extras.getString("title");
		mes = extras.getString("message");
		Log.i("GCM",
				"Received : (" + messageType + ")  "
						+ extras.getString("title"));

		mWindowManager = (WindowManager) getApplicationContext()
				.getSystemService(Context.WINDOW_SERVICE);
		mMainHandler = new Handler(Looper.getMainLooper());
		mDetector = new GestureDetector(this, new SingleTapConfirm());
		addMushHead();
		createLinearLayout();
		GcmBroadcastReceiver.completeWakefulIntent(intent);

	}

	private void createLinearLayout() {
		mLinearLayout = new LinearLayout(this);
		DisplayMetrics displaymetrics = new DisplayMetrics();
		mWindowManager.getDefaultDisplay().getRealMetrics(displaymetrics);
		int linearHeight = displaymetrics.heightPixels;
		mLinearLayout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, linearHeight/9));
		mLinearLayout.setOrientation(LinearLayout.HORIZONTAL);
		
		mLinearLayoutParams = new WindowManager.LayoutParams();
		mLinearLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
		mLinearLayoutParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
		mLinearLayoutParams.width = mLinearLayout.getLayoutParams().width;
		mLinearLayoutParams.height = mLinearLayout.getLayoutParams().height;
		mLinearLayoutParams.gravity = Gravity.BOTTOM;
		mLinearLayoutParams.windowAnimations = R.style.Animation_Toast;
		
		mReplyButton = new Button(this);
		mReplyButtonParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		mReplyButtonParams.gravity = Gravity.CENTER_VERTICAL;
		mReplyButtonParams.weight = 1;
		mReplyButton.setLayoutParams(mReplyButtonParams);
		mReplyButton.setText("Reply");
		mReplyButton.setPadding(20, 20, 20, 20);
		mReplyButton.setTextColor(Color.RED);
		mReplyButton.setTextSize(20);
		mReplyButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				mWindowManager.removeView(mMushHead);
				mWindowManager.removeView(mLinearLayout);
				Intent selectionIntent = new Intent(
						getApplicationContext(), SelectionActivity.class);
				selectionIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				selectionIntent.putExtra(CONTACT_SELECTED, "Test");
				startActivity(selectionIntent);
			}
		});
		
		mDeleteButton = new Button(this);
		mDeleteButtonParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		mDeleteButtonParams.gravity = Gravity.CENTER_VERTICAL;
		mDeleteButtonParams.weight = 1;
		mDeleteButton.setLayoutParams(mDeleteButtonParams);
		mDeleteButton.setText("Delete");
		mDeleteButton.setPadding(20, 20, 20, 20);
		mDeleteButton.setTextColor(Color.RED);
		mDeleteButton.setTextSize(20);
		mDeleteButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				mWindowManager.removeView(mMushHead);
				mWindowManager.removeView(mLinearLayout);
			}
		});
		
		mLinearLayout.addView(mReplyButton);
		mLinearLayout.addView(mDeleteButton);
		
	}

	private void addMushHead() {
		mMushHead = new TextView(this);
		mMushHead.setLayoutParams(new LayoutParams(200, 200));
		mMushHead.setBackgroundColor(Color.RED);
		mMushHead.setText(title);
		mMushHead.setGravity(Gravity.CENTER);
		mMushHead.setTextColor(Color.WHITE);
		mMushHead.setTextSize(20);

		mMushHeadParams = new WindowManager.LayoutParams();
		mMushHeadParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
		mMushHeadParams.format = PixelFormat.TRANSLUCENT;
		mMushHeadParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
		mMushHeadParams.width = mMushHead.getLayoutParams().width;
		mMushHeadParams.height = mMushHead.getLayoutParams().height;
		mMushHeadParams.gravity = Gravity.CENTER;
		mMushHeadParams.windowAnimations = R.style.Animation_Toast;
		
		mMushHead.setOnTouchListener(new View.OnTouchListener() {

			private int initialX;
			private int initialY;
			private float initialTouchX;
			private float initialTouchY;

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				
				
				if (mDetector.onTouchEvent(event)) { // Detects Single tap
					if (mIsLinearShown){
						mWindowManager.removeView(mLinearLayout);
						mIsLinearShown = false;
					} else {
						mMainHandler.post(new Runnable() {
							
							@Override
							public void run() {
								mWindowManager.addView(mLinearLayout, mLinearLayoutParams);
							}
						});
						mIsLinearShown = true;
					}
					return true;
				} else { //Only if its not a single tap
					switch (event.getAction()) {
					case MotionEvent.ACTION_DOWN:
						initialX = mMushHeadParams.x;
						initialY = mMushHeadParams.y;
						initialTouchX = event.getRawX();
						initialTouchY = event.getRawY();
						return true;
					case MotionEvent.ACTION_UP:
						if (mMushHeadParams.x == initialX
								&& mMushHeadParams.y == initialY) {
							return false;
						}
						return true;
					case MotionEvent.ACTION_MOVE:
						mMushHeadParams.x = initialX
								+ (int) (event.getRawX() - initialTouchX);
						mMushHeadParams.y = initialY
								+ (int) (event.getRawY() - initialTouchY);
						mWindowManager.updateViewLayout(mMushHead,
								mMushHeadParams);
						return true;
					}
					return false;
				}

			}
		});
		mMainHandler.post(new Runnable() {

			@Override
			public void run() {
				mWindowManager.addView(mMushHead, mMushHeadParams);

			}
		});
	}

	private class SingleTapConfirm extends SimpleOnGestureListener {

		@Override
		public boolean onSingleTapUp(MotionEvent event) {
			return true;
		}
		
		@Override
		public boolean onSingleTapConfirmed(MotionEvent e) {
			return true;
		}
	}
}
