package com.lach.common.ui.preference;

import android.content.Context;
import android.preference.Preference;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.View;

import com.lach.common.R;

public class EnhancedPreference extends Preference {
    private final EnhancedPreferenceHelper helper;

    public EnhancedPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        setLayoutResource(R.layout.v_enhanced_preference_standard);
        helper = new EnhancedPreferenceHelper();
        helper.init(this, attrs, defStyleAttr);
    }

    public EnhancedPreference(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.preferenceStyle);
    }

    public EnhancedPreference(Context context) {
        this(context, null);
    }

    protected void onBindView(@NonNull View view) {
        super.onBindView(view);
        helper.bindView(this, view);
    }
}
