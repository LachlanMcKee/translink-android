package com.lach.common.ui.dialog;

import android.content.Context;
import android.support.v7.app.AlertDialog;
import android.view.accessibility.AccessibilityEvent;

public class AccessibleAlertDialog extends AlertDialog {
    protected AccessibleAlertDialog(Context context) {
        super(context);
    }

    protected AccessibleAlertDialog(Context context, int theme) {
        super(context, theme);
    }

    protected AccessibleAlertDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    @Override
    public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent event) {
        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            event.getText().add("Custom text from onPopulateAccessibilityEvent");
            return true;
        }
        return super.dispatchPopulateAccessibilityEvent(event);
    }

}
