package com.lach.common.ui.preference;

import android.content.Context;
import android.preference.CheckBoxPreference;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.View;

import com.lach.common.R;

public class EnhancedCheckboxPreference extends CheckBoxPreference {
    private final EnhancedPreferenceHelper helper;

    public EnhancedCheckboxPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        setLayoutResource(R.layout.v_enhanced_preference_standard);
        helper = new EnhancedPreferenceHelper();
        helper.init(this, attrs, defStyleAttr);
    }

    public EnhancedCheckboxPreference(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.checkBoxPreferenceStyle);
    }

    public EnhancedCheckboxPreference(Context context) {
        this(context, null);
    }

    protected void onBindView(@NonNull View view) {
        super.onBindView(view);
        helper.bindView(this, view);
    }
}
