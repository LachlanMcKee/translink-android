package com.lach.translink.ui.presenter;

import android.os.Bundle;

public interface BasePresenter {
    void onCreate(Bundle savedInstanceState);

    void onSaveInstanceState(Bundle outState);
}
