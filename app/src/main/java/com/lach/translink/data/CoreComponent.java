package com.lach.translink.data;

import com.lach.translink.ui.gocard.GoCardDetailsDialog;
import com.lach.translink.ui.resolve.ResolveLocationMapFragment;
import com.lach.translink.ui.search.SearchActivity;

public interface CoreComponent {
    void inject(GoCardDetailsDialog inject);

    void inject(ResolveLocationMapFragment inject);

    void inject(SearchActivity inject);
}
