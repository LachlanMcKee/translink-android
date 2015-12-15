package com.lach.common.util;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.TypedValue;

import com.lach.common.R;

public class ThemeUtil {
    public static int getIconTintColour(Context context) {
        return getColorAttribute(context, R.attr.icon_tint_colour);
    }

    public static int getColorAttribute(Context context, int attribute) {
        TypedValue typedValue = new TypedValue();

        TypedArray a = null;
        try {
            a = context.obtainStyledAttributes(typedValue.data, new int[]{attribute});
            return a.getColor(0, Color.BLACK);
        } finally {
            if (a != null) {
                a.recycle();
            }
        }
    }
}
