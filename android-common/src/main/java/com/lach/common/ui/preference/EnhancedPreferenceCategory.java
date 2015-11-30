package com.lach.common.ui.preference;

import android.content.Context;
import android.preference.PreferenceCategory;
import android.util.AttributeSet;

import com.lach.common.R;

public class EnhancedPreferenceCategory extends PreferenceCategory {
    public EnhancedPreferenceCategory(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        setLayoutResource(R.layout.v_enhanced_preference_category);
    }

    public EnhancedPreferenceCategory(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.preferenceCategoryStyle);
    }

    public EnhancedPreferenceCategory(Context context) {
        this(context, null);
    }
}
