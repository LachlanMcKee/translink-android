package com.lach.common.ui.preference;

import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.preference.Preference;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

import com.lach.common.R;

public class EnhancedPreferenceHelper {
    private int mIconResId;
    private Drawable mIcon;

    public void init(Preference preference, AttributeSet attrs, int defStyleAttr) {
        if (attrs == null) {
            return;
        }

        TypedArray typedArray = preference.getContext().obtainStyledAttributes(attrs, R.styleable.EnhancedPreference, defStyleAttr, 0);
        try {
            mIconResId = typedArray.getResourceId(R.styleable.EnhancedPreference_pref_icon, 0);
        } finally {
            typedArray.recycle();
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            preference.setIcon(mIconResId);
        }
    }

    public void bindView(Preference preference, @NonNull View view) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            final ImageView iconImageView = (ImageView) view.findViewById(android.R.id.icon);

            // Gingerbread and below does not have icon behaviour.
            if (iconImageView != null) {
                if (mIconResId != 0 || mIcon != null) {
                    if (mIcon == null) {
                        mIcon = ContextCompat.getDrawable(preference.getContext(), mIconResId);
                    }
                    if (mIcon != null) {
                        iconImageView.setImageDrawable(mIcon);
                    }
                }
                if (mIcon != null) {
                    iconImageView.setVisibility(View.VISIBLE);
                } else {
                    iconImageView.setVisibility(View.GONE);
                }
            }
        }
    }
}
