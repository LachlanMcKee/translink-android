package com.lach.translink.ui.impl.search.dialog;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.lach.common.ui.adapter.ViewHolderArrayAdapter;
import com.lach.translink.activities.R;
import com.lach.translink.data.journey.JourneyCriteria;
import com.lach.translink.data.journey.favourite.JourneyCriteriaFavourite;
import com.lach.translink.data.place.PlaceParser;
import com.lach.translink.util.JourneyCriteriaHelper;

import java.util.List;

public class JourneyAdapter extends ViewHolderArrayAdapter<JourneyCriteriaFavourite> {
    private final PlaceParser placeParser;

    @SuppressWarnings("unchecked")
    public JourneyAdapter(Context context, PlaceParser placeParser, List<? extends JourneyCriteriaFavourite> favouriteList) {
        super(context, R.layout.l_journey_history, (List<JourneyCriteriaFavourite>) favouriteList);

        this.placeParser = placeParser;

        addViewHolderHelper(new JourneyViewHolderHelper());
    }

    static class JourneyViewHolder implements ViewHolder {
        TextView title;

        TextView fromLocation;
        TextView fromLocationLabel;

        TextView toLocation;
        TextView toLocationLabel;

        TextView description;
    }

    private class JourneyViewHolderHelper extends ViewHolderHelper<JourneyCriteriaFavourite, JourneyViewHolder> {

        public JourneyViewHolderHelper() {
            super(R.layout.l_favourite_journey);
        }

        @Override
        public boolean isHolderForData(JourneyCriteriaFavourite data) {
            return true;
        }

        @Override
        public JourneyViewHolder createViewHolder(View convertView) {
            JourneyViewHolder holder = new JourneyViewHolder();
            holder.title = findTextViewById(convertView, R.id.journey_title_label);

            holder.fromLocation = findTextViewById(convertView, R.id.journey_from_location);
            holder.fromLocationLabel = findTextViewById(convertView, R.id.journey_from_location_label);

            holder.toLocation = findTextViewById(convertView, R.id.journey_to_location);
            holder.toLocationLabel = findTextViewById(convertView, R.id.journey_to_location_label);

            holder.description = findTextViewById(convertView, R.id.journey_description);

            return holder;
        }

        @Override
        public void updateViewHolder(JourneyViewHolder holder, JourneyCriteriaFavourite item) {
            setText(holder.title, item.getName());

            JourneyCriteria criteria = item.getJourneyCriteria();
            setText(holder.fromLocationLabel, holder.fromLocation, placeParser.prettyPrintPlace(criteria.getFromAddress()));
            setText(holder.toLocationLabel, holder.toLocation, placeParser.prettyPrintPlace(criteria.getToAddress()));
            setText(holder.description, JourneyCriteriaHelper.createJourneyDescription(criteria));
        }
    }

    private void setText(TextView labelTextView, TextView textView, String text) {
        boolean valueExists = !TextUtils.isEmpty(text);
        if (valueExists) {
            textView.setText(text);
        }
        toggleViewVisibility(labelTextView, valueExists);
        toggleViewVisibility(textView, valueExists);
    }

    private void setText(TextView textView, String text) {
        if (!TextUtils.isEmpty(text)) {
            textView.setText(text);
        } else {
            toggleViewVisibility(textView, false);
        }
    }

    private void toggleViewVisibility(TextView textView, boolean visible) {
        textView.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

}
