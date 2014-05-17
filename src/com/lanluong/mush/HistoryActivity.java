package com.lanluong.mush;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class HistoryActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.history);

		// Setting Action Bar
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setDisplayShowTitleEnabled(false);
		actionBar.setDisplayShowCustomEnabled(true);
		actionBar.setCustomView(R.layout.action_bar);

		TextView actionBarTitle = (TextView) findViewById(R.id.action_bar_title_text);
		actionBarTitle.setText("HISTORY");
	}
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case android.R.id.home:
        	//Back Button
            NavUtils.navigateUpFromSameTask(this);
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

}
