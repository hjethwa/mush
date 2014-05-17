package com.lanluong.mush.selection;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.lanluong.mush.R;

public class FaceTabFragment extends Fragment {
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if (container == null) {
            return null;
        }
		
		LinearLayout faceTabLinear = (LinearLayout)inflater.inflate(R.layout.tab_face_fragment, container, false);
		faceTabLinear.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				SelectionActivity selectionAcitivty = (SelectionActivity) getActivity();
				selectionAcitivty.onEmoticonClicked();
			}
		});
		return faceTabLinear;
	}

}
