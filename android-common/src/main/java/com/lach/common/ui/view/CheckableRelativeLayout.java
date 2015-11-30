package com.lach.common.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Checkable;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.List;

public class CheckableRelativeLayout extends RelativeLayout implements Checkable {

    private boolean isChecked;
    private final List<Checkable> checkableViews;

    public CheckableRelativeLayout(Context context) {
        super(context);
        checkableViews = new ArrayList<>();
    }

    public CheckableRelativeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        checkableViews = new ArrayList<>();
    }

    public CheckableRelativeLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        checkableViews = new ArrayList<>();
    }

    // @see android.widget.Checkable#isChecked()
    public boolean isChecked() {
        return isChecked;
    }

    // @see android.widget.Checkable#setChecked(boolean)
    public void setChecked(boolean isChecked) {
        this.isChecked = isChecked;
        for (Checkable c : checkableViews) {
            // Pass the information to all the child Checkable widgets
            c.setChecked(isChecked);
        }
    }

    // @see android.widget.Checkable#toggle()
    public void toggle() {
        this.isChecked = !this.isChecked;
        for (Checkable c : checkableViews) {
            // Pass the information to all the child Checkable widgets
            c.toggle();
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        final int childCount = this.getChildCount();
        for (int i = 0; i < childCount; ++i) {
            findCheckableChildren(this.getChildAt(i));
        }
    }

    /**
     * Add to our checkable list all the children of the view that implement the
     * interface Checkable
     */
    private void findCheckableChildren(View v) {
        if (v instanceof Checkable) {
            this.checkableViews.add((Checkable) v);
        }
        if (v instanceof ViewGroup) {
            final ViewGroup vg = (ViewGroup) v;
            final int childCount = vg.getChildCount();
            for (int i = 0; i < childCount; ++i) {
                findCheckableChildren(vg.getChildAt(i));
            }
        }
    }
}
