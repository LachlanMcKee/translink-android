package com.lach.translink.ui;

import android.content.Intent;
import android.databinding.BaseObservable;
import android.os.Bundle;
import android.support.annotation.CallSuper;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseViewModel extends BaseObservable {

    final BaseViewModel parent; // A parent may not exist.
    private List<BaseViewModel> childViewModels;

    public BaseViewModel() {
        parent = null;
    }

    public BaseViewModel(BaseViewModel parent) {
        this.parent = parent;
    }

    public <T extends BaseViewModel> T addChildViewModel(T viewModel) {
        if (childViewModels == null) {
            childViewModels = new ArrayList<>();
        }
        childViewModels.add(viewModel);
        return viewModel;
    }

    @CallSuper
    public void init(Bundle savedInstanceState) {
        if (childViewModels == null) {
            return;
        }
        for (BaseViewModel childViewModel : childViewModels) {
            childViewModel.init(savedInstanceState);
        }
    }

    @CallSuper
    public void onResume() {
        if (childViewModels == null) {
            return;
        }
        for (BaseViewModel childViewModel : childViewModels) {
            childViewModel.onResume();
        }
    }

    @CallSuper
    public void onPause() {
        if (childViewModels == null) {
            return;
        }

        for (BaseViewModel childViewModel : childViewModels) {
            childViewModel.onPause();
        }
    }

    @CallSuper
    public void onDestroy() {
        if (childViewModels == null) {
            return;
        }

        for (BaseViewModel childViewModel : childViewModels) {
            childViewModel.onDestroy();
        }
    }

    @CallSuper
    public void onSaveInstanceState(Bundle outState) {
        if (childViewModels == null) {
            return;
        }

        for (BaseViewModel childViewModel : childViewModels) {
            childViewModel.onSaveInstanceState(outState);
        }
    }

    @CallSuper
    public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
        if (childViewModels == null) {
            return false;
        }

        for (BaseViewModel childViewModel : childViewModels) {
            if (childViewModel.onActivityResult(requestCode, resultCode, data)) {
                return true;
            }
        }

        return false;
    }

    public BaseViewModel getParent() {
        return parent;
    }
}
