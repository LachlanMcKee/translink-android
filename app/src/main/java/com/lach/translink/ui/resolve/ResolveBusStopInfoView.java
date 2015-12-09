package com.lach.translink.ui.resolve;

import android.content.Context;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorCompat;
import android.support.v4.view.ViewPropertyAnimatorListener;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.lach.translink.activities.R;
import com.lach.translink.data.place.bus.BusStop;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class ResolveBusStopInfoView extends FrameLayout {

    @InjectView(R.id.resolve_bus_dismiss)
    View dismissArea;

    @InjectView(R.id.resolve_bus_stop_container)
    View container;

    @InjectView(R.id.resolve_bus_stop_description)
    TextView description;

    int containerHeight;

    private DismissListener dismissListener;

    public ResolveBusStopInfoView(Context context) {
        super(context);
        init();
    }

    public ResolveBusStopInfoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ResolveBusStopInfoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        containerHeight = getResources().getDimensionPixelSize(R.dimen.bus_stop_prompt_height);

        LayoutInflater inflater = LayoutInflater.from(getContext());
        inflater.inflate(R.layout.v_resolve_bus_stop, this, true);

        ButterKnife.inject(this);
        dismissArea.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }

    public void show(BusStop busStop, boolean animate) {
        description.setText(busStop.getDescription());

        if (animate) {
            container.setTranslationY(containerHeight);
            ViewCompat.animate(container)
                    .translationY(0.0f)
                    .setDuration(150L)
                    .setInterpolator(new LinearInterpolator())
                    .withLayer()
                    .setListener(null).start();
        } else {
            container.setTranslationY(0.0f);
        }
    }

    public void dismiss() {
        ViewPropertyAnimatorCompat animation = ViewCompat.animate(container)
                .translationY(containerHeight)
                .setDuration(150L)
                .setInterpolator(new LinearInterpolator())
                .withLayer();

        if (dismissListener != null) {
            animation.setListener(new ViewPropertyAnimatorListener() {
                @Override
                public void onAnimationStart(View view) {

                }

                @Override
                public void onAnimationEnd(View view) {
                    dismissListener.onDismissCompleted();
                }

                @Override
                public void onAnimationCancel(View view) {
                    dismissListener.onDismissCompleted();
                }
            });
        }
        animation.start();
    }

    public void setDismissListener(DismissListener dismissListener) {
        this.dismissListener = dismissListener;
    }

    public interface DismissListener {
        void onDismissCompleted();
    }

}
