package com.lach.translink.ui.resolve;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.TextView;

import com.lach.common.ui.ButterFragment;
import com.lach.translink.activities.R;
import com.lach.translink.data.place.bus.BusStop;

import butterknife.InjectView;
import butterknife.OnClick;

public class ResolveBusStopFragment extends ButterFragment {
    private static final String BUNDLE_KEY_BUS_STOP = "bus_stop";

    @InjectView(R.id.resolve_title)
    TextView title;

    @InjectView(R.id.resolve_subtitle)
    TextView subtitle;

    @InjectView(R.id.resolve_bus_stop_container)
    View container;

    @InjectView(R.id.resolve_bus_stop_description)
    TextView description;

    int containerHeight;

    public static ResolveBusStopFragment newInstance(BusStop busStop) {
        ResolveBusStopFragment fragment = new ResolveBusStopFragment();
        Bundle args = new Bundle(1);
        args.putParcelable(BUNDLE_KEY_BUS_STOP, busStop);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.f_resolve_bus_stop, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        BusStop busStop = getArguments().getParcelable(BUNDLE_KEY_BUS_STOP);
        description.setText(busStop.getDescription());

        title.setText("Confirm bus stop");
        subtitle.setText(R.string.resolve_map_subtitle_continue);

        containerHeight = getResources().getDimensionPixelSize(R.dimen.bus_stop_prompt_height);

        if (savedInstanceState == null) {
            container.setTranslationY(containerHeight);

            ViewCompat.animate(container)
                    .translationY(0.0f)
                    .translationY(0.0f)
                    .setDuration(150L)
                    .setInterpolator(new LinearInterpolator())
                    .withLayer()
                    .setListener(null).start();
        }
    }

    @OnClick(R.id.resolve_bus_continue)
    void confirmBusStop() {
        FragmentActivity activity = getActivity();
        if (activity != null) {
            activity.setResult(Activity.RESULT_OK);
            close();
        }
    }

    @OnClick(R.id.resolve_bus_dismiss)
    public void dismiss() {
        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(container, "translationY", containerHeight);
        AnimatorSet animSetXY = new AnimatorSet();
        animSetXY.setDuration(150L);
        animSetXY.setInterpolator(new LinearInterpolator());
        animSetXY.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                close();
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                close();
            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        animSetXY.playTogether(objectAnimator);
        animSetXY.start();
    }

    private void close() {
        FragmentActivity activity = getActivity();
        if (activity != null) {
            activity.finish();
        }
    }
}
