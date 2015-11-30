package com.lach.common.ui.view;

import android.content.Context;
import android.os.Build;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.TranslateAnimation;
import android.widget.LinearLayout;

import java.util.List;

@CoordinatorLayout.DefaultBehavior(FloatingActionButtonContainer.Behaviour.class)
public class FloatingActionButtonContainer extends LinearLayout {

    public FloatingActionButtonContainer(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public static class Behaviour extends android.support.design.widget.CoordinatorLayout.Behavior<FloatingActionButtonContainer> {
        private static final boolean SNACKBAR_BEHAVIOR_ENABLED;
        private float mTranslationY;

        public Behaviour() {
        }

        public boolean layoutDependsOn(CoordinatorLayout parent, FloatingActionButtonContainer child, View dependency) {
            return dependency instanceof Snackbar.SnackbarLayout;
        }

        public boolean onDependentViewChanged(CoordinatorLayout parent, FloatingActionButtonContainer child, View dependency) {
            if (dependency instanceof Snackbar.SnackbarLayout) {
                this.updateFabTranslationForSnackbar(parent, child, dependency);
            }

            return true;
        }

        @Override
        public boolean onLayoutChild(CoordinatorLayout parent, FloatingActionButtonContainer child, int layoutDirection) {
            parent.onLayoutChild(child, layoutDirection);

            if (!SNACKBAR_BEHAVIOR_ENABLED) {
                TranslateAnimation anim = new TranslateAnimation(0, 0, 0, 0);
                anim.setFillAfter(true);
                anim.setDuration(0);
                child.startAnimation(anim);
            }
            mTranslationY = 0;

            return true;
        }

        private void updateFabTranslationForSnackbar(CoordinatorLayout parent, FloatingActionButtonContainer layout, View snackbar) {
            float translationY = this.getFabTranslationYForSnackbar(parent, layout);
            if (translationY != this.mTranslationY) {
                if (SNACKBAR_BEHAVIOR_ENABLED) {
                    ViewCompat.animate(layout).cancel();
                }

                if (Math.abs(translationY - this.mTranslationY) == (float) snackbar.getHeight()) {

                    if (SNACKBAR_BEHAVIOR_ENABLED) {
                        ViewCompat.animate(layout)
                                .translationY(translationY)
                                .setInterpolator(new FastOutSlowInInterpolator())
                                .setListener(null);
                    } else {
                        TranslateAnimation anim = new TranslateAnimation(0, 0, translationY, translationY);
                        anim.setFillAfter(true);
                        anim.setDuration(0);
                        layout.startAnimation(anim);
                    }

                } else {
                    ViewCompat.setTranslationY(layout, translationY);
                }

                this.mTranslationY = translationY;
            }

        }

        private float getFabTranslationYForSnackbar(CoordinatorLayout parent, FloatingActionButtonContainer layout) {
            float minOffset = 0.0F;
            List dependencies = parent.getDependencies(layout);
            int i = 0;

            for (int z = dependencies.size(); i < z; ++i) {
                View view = (View) dependencies.get(i);
                if (view instanceof Snackbar.SnackbarLayout && parent.doViewsOverlap(layout, view)) {
                    minOffset = Math.min(minOffset, ViewCompat.getTranslationY(view) - (float) view.getHeight());
                }
            }

            return minOffset;
        }

        static {
            SNACKBAR_BEHAVIOR_ENABLED = Build.VERSION.SDK_INT >= 11;
        }
    }

}
