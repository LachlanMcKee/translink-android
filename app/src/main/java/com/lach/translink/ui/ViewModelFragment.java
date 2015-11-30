package com.lach.translink.ui;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.lach.translink.activities.BR;

public abstract class ViewModelFragment<T extends PrimaryViewModel> extends Fragment {

    private T viewModel;

    protected abstract T createViewModel();

    @SuppressWarnings("SameReturnValue")
    protected abstract
    @LayoutRes
    int getLayoutId();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //
        // Assign the view model and bind it immediately.
        // This allows the views such as the Spinners to handle selected state internally.
        //
        viewModel = createViewModel();
        viewModel.init(savedInstanceState);

        ViewDataBinding inflate = DataBindingUtil.inflate(inflater, getLayoutId(), container, false);
        inflate.setVariable(BR.viewModel, viewModel);
        inflate.executePendingBindings();

        return inflate.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        viewModel.onResume();
    }

    @Override
    public void onPause() {
        viewModel.onPause();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        viewModel.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (viewModel != null) {
            viewModel.onSaveInstanceState(outState);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (viewModel != null) {
            viewModel.onActivityResult(requestCode, resultCode, data);
        }
    }

    protected T getViewModel() {
        return viewModel;
    }
}
