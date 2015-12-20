package com.lach.translink.ui.impl.search.dialog;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import com.lach.common.util.WebUtil;

public class WebsiteLinksDialog extends DialogFragment {

    public static WebsiteLinksDialog newInstance() {
        return new WebsiteLinksDialog();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder b = new AlertDialog.Builder(getActivity());

        final WebSiteInfoList webSitesList = new WebSiteInfoList();
        webSitesList.add(new WebSiteInfo("http://mobile.translink.com.au/travel-information/service-notices/current-status", "Service Status"));
        webSitesList.add(new WebSiteInfo("http://mobile.jp.translink.com.au/travel-information/network-information/timetables", "Timetables"));
        webSitesList.add(new WebSiteInfo("http://mobile.translink.com.au/tickets-and-fares/go-card/locations", "Go card Locations"));
        webSitesList.add(new WebSiteInfo("http://mobile.translink.com.au/travel-information/network-information/event-transport", "Events"));
        webSitesList.add(new WebSiteInfo("http://mobile.translink.com.au/tickets-and-fares", "Tickets and Fares"));
        webSitesList.add(new WebSiteInfo("http://mobile.translink.com.au/site-information/contact-us-and-help", "Contact Translink"));

        final CharSequence[] webSites = webSitesList.getDialogList();

        b.setTitle("Select a link");

        b.setItems(webSites, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                WebSiteInfo selectedItem = webSitesList.get(item);
                WebUtil.openUrl(getActivity(), selectedItem.getUri());
            }
        });

        return b.create();
    }

}