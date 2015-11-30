package com.lach.translink.ui.search.dialog;

import java.util.ArrayList;

class WebSiteInfoList extends ArrayList<WebSiteInfo> {

    public WebSiteInfoList() {

    }

    public CharSequence[] getDialogList() {
        ArrayList<String> items = new ArrayList<>();
        for (WebSiteInfo wsi : this) {
            items.add(wsi.getName());
        }

        return items.toArray(new String[items.size()]);
    }

}
