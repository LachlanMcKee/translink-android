package com.lach.common.ui.view;

public class Debouncer {
    private static final int TIME_OUT_DURATION = 500;

    private long lastClickedTime = 0;

    public boolean isValidClick() {
        // Prevent accidental double taps.
        long time = System.currentTimeMillis();
        if ((time - lastClickedTime) <= TIME_OUT_DURATION) {
            return false;
        }
        lastClickedTime = time;

        return true;
    }

}
