package com.lach.common.databinding;

import android.databinding.BindingAdapter;
import android.view.MotionEvent;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.widget.AbsSpinner;
import android.widget.ArrayAdapter;
import android.widget.SpinnerAdapter;

import com.lach.common.ui.view.Debouncer;

public class AbsSpinnerBindingAdapterExt {

    @BindingAdapter({"bind:text", "bind:onTouchListener"})
    public static void setText(AbsSpinner view, String text, final View.OnTouchListener listener) {
        setText(view, text, listener, null);
    }

    @BindingAdapter({"bind:text", "bind:onTouchListener", "bind:debouncer"})
    public static void setText(AbsSpinner view, String text, final View.OnTouchListener listener, final Debouncer debouncer) {
        setEntries(view, new String[]{text});

        view.setClickable(false);
        view.setOnTouchListener(new View.OnTouchListener() {

            Debouncer localDebouncer;

            private Debouncer getDebouncer() {
                if (localDebouncer == null) {
                    if (debouncer != null) {
                        localDebouncer = debouncer;
                    } else {
                        localDebouncer = new Debouncer();
                    }
                }
                return localDebouncer;
            }

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (getDebouncer().isValidClick()) {
                        listener.onTouch(v, event);

                        // On click listener is null, so this will simply fire the accessibility logic.
                        v.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_CLICKED);
                    }
                }

                // Handle the touch event regardless.
                return true;
            }
        });
    }

    @BindingAdapter({"bind:entries"})
    public static void setEntries(AbsSpinner view, Object[] entries) {
        if (entries != null) {
            SpinnerAdapter oldAdapter = view.getAdapter();
            boolean changed = true;
            if (oldAdapter != null && oldAdapter.getCount() == entries.length) {
                changed = false;
                for (int i = 0; i < entries.length; i++) {
                    if (!entries[i].equals(oldAdapter.getItem(i))) {
                        changed = true;
                        break;
                    }
                }
            }
            if (changed) {
                ArrayAdapter<Object> adapter =
                        new ArrayAdapter<>(view.getContext(),
                                android.R.layout.simple_spinner_item, entries);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                view.setAdapter(adapter);
            }
        } else {
            view.setAdapter(null);
        }
    }

}
