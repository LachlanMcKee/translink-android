package com.lach.common.ui.view;

import android.os.Build;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorListener;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Interpolator;

import com.lach.common.R;

public class ScaleAnimator {
    private boolean mIsAnimatingOut;
    private static final Interpolator FAST_OUT_SLOW_IN_INTERPOLATOR = new FastOutSlowInInterpolator();

    private final View view;

    public ScaleAnimator(View view) {
        this.view = view;
    }

    public void show() {
        if (view.getVisibility() == View.VISIBLE) {
            return;
        }

        view.setVisibility(View.VISIBLE);
        if (Build.VERSION.SDK_INT >= 14) {
            // Ensure the initial values are set correctly.
            view.setScaleX(0.0F);
            view.setScaleY(0.0F);
            view.setAlpha(0.0F);

            ViewCompat.animate(view)
                    .scaleX(1.0F)
                    .scaleY(1.0F)
                    .alpha(1.0F)
                    .setDuration(300L)
                    .setInterpolator(FAST_OUT_SLOW_IN_INTERPOLATOR).withLayer()
                    .setListener(null).start();
        } else {
            Animation anim = android.view.animation.AnimationUtils.loadAnimation(view.getContext(),  R.anim.fab_in);
            anim.setDuration(300L);
            anim.setFillAfter(true);
            anim.setInterpolator(FAST_OUT_SLOW_IN_INTERPOLATOR);
            view.startAnimation(anim);
        }
    }

    public void hide() {
        hide(true);
    }

    public void hide(boolean forceAnimation) {
        // Earlier devices have troubles animating under heavy UI interactions.
        if (!forceAnimation && Build.VERSION.SDK_INT < 14) {
            view.setVisibility(View.INVISIBLE);
            return;
        }

        if (mIsAnimatingOut || view.getVisibility() == View.INVISIBLE) {
            return;
        }

        if (Build.VERSION.SDK_INT >= 14) {
            ViewCompat.animate(view)
                    .scaleX(0.0F)
                    .scaleY(0.0F)
                    .alpha(0.0F)
                    .setDuration(300L)
                    .setInterpolator(FAST_OUT_SLOW_IN_INTERPOLATOR).withLayer()
                    .setListener(new ViewPropertyAnimatorListener() {

                        public void onAnimationStart(View view) {
                            mIsAnimatingOut = true;
                        }

                        public void onAnimationCancel(View view) {
                            mIsAnimatingOut = false;
                        }

                        public void onAnimationEnd(View view) {
                            mIsAnimatingOut = false;
                            view.setVisibility(View.INVISIBLE);
                        }

                    }).start();

        } else {
            Animation anim = android.view.animation.AnimationUtils.loadAnimation(view.getContext(), R.anim.fab_out);
            anim.setInterpolator(FAST_OUT_SLOW_IN_INTERPOLATOR);
            anim.setDuration(300L);
            anim.setFillAfter(true);
            anim.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    mIsAnimatingOut = true;
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    mIsAnimatingOut = false;
                    view.setVisibility(View.INVISIBLE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            view.startAnimation(anim);
        }
    }

}
