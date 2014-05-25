package com.harshal.mush.selection;

import com.lanluong.mush.R;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

public class CatsTabFragment extends Fragment {
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if (container == null) {
            return null;
        }
		LinearLayout catsTabLinear = (LinearLayout)inflater.inflate(R.layout.tab_cats_fragment, container, false);
		catsTabLinear.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				SelectionActivity selectionAcitivty = (SelectionActivity) getActivity();
				selectionAcitivty.onEmoticonClicked();
			}
		});
		return catsTabLinear;
	}

}
