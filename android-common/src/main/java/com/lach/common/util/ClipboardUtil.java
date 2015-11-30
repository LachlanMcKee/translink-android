package com.lach.common.util;

import android.content.Context;
import android.text.ClipboardManager;

public class ClipboardUtil {
    @SuppressWarnings("deprecation")
    public static void setClipboardText(Context context, CharSequence text) {
        ClipboardManager ClipMan = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipMan.setText(text.toString());
    }
}
