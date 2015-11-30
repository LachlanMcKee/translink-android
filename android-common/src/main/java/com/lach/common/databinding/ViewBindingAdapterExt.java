package com.lach.common.databinding;

import android.databinding.BindingAdapter;
import android.view.View;

import com.lach.common.ui.view.Debouncer;

public class ViewBindingAdapterExt {

    @BindingAdapter({"bind:onClickListener", "bind:debouncer"})
    public static void setClickListener(View view, final View.OnClickListener clickListener, final Debouncer debouncer) {
        view.setClickable(true);
        view.setOnClickListener(new View.OnClickListener() {

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
            public void onClick(View v) {
                if (getDebouncer().isValidClick()) {
                    clickListener.onClick(v);
                }
            }
        });
    }

}
