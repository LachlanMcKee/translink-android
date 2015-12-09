package com.lach.translink.network;

import com.lach.translink.ui.impl.gocard.GoCardInfoFragment;
import com.lach.translink.tasks.gocard.TaskGoCardDetails;
import com.lach.translink.tasks.gocard.TaskGoCardHistory;

public interface GoCardNetworkComponent {
    TaskGoCardDetails inject(TaskGoCardDetails inject);

    TaskGoCardHistory inject(TaskGoCardHistory inject);

    GoCardInfoFragment injectFragment(GoCardInfoFragment inject);

    GoCardInfoFragment.GoCardGraphFragment injectFragment(GoCardInfoFragment.GoCardGraphFragment inject);
}
