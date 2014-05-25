package com.harshal.mush;

import com.lanluong.mush.R;

import android.content.Context;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

public class MushAnimationUtility {
	public static void setAndStartImageFadeInAnimation(Context context, final View view) {
		final Animation fadeInAnimation = AnimationUtils.loadAnimation(context,
				R.anim.image_fade_in_animation);
		fadeInAnimation.setFillAfter(true);
		fadeInAnimation.setDuration(700);
		fadeInAnimation.setInterpolator(new AccelerateInterpolator());
		view.startAnimation(fadeInAnimation);
	}

	public static void setAndStartViewFadeOutCompleteAnimation(Context context, final View view,
			Animation.AnimationListener animationListener) {
		final Animation fadeOutCompleteAnimation = AnimationUtils
				.loadAnimation(context, R.anim.view_complete_fade_out);
		fadeOutCompleteAnimation.setFillAfter(true);
		fadeOutCompleteAnimation.setDuration(700);
		fadeOutCompleteAnimation.setInterpolator(new AccelerateInterpolator());
		if (animationListener != null) {
			fadeOutCompleteAnimation.setAnimationListener(animationListener);
		}
		view.startAnimation(fadeOutCompleteAnimation);
	}

	public static void setAndStartImageFadeOutAnimation(Context context, View view) {
		final Animation fadeOutAnimation = AnimationUtils.loadAnimation(context,
				R.anim.image_fade_out_animation);
		fadeOutAnimation.setFillAfter(true);
		fadeOutAnimation.setDuration(700);
		fadeOutAnimation.setInterpolator(new AccelerateInterpolator());
		view.startAnimation(fadeOutAnimation);
	}

	public static void setAndStartSlideUpAnimation(Context context, final View view) {
		final Animation slideUpAnimation = AnimationUtils.loadAnimation(context,
				R.anim.slide_up_animation);
		slideUpAnimation.setFillAfter(true);
		slideUpAnimation.setDuration(700);
		slideUpAnimation.setInterpolator(new AccelerateInterpolator());
		view.startAnimation(slideUpAnimation);
	}
}
