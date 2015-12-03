package com.lach.translink.ui.settings;

import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.lach.translink.TranslinkApplication;
import com.lach.translink.activities.R;
import com.lach.translink.data.BaseDao;
import com.lach.translink.data.journey.JourneyCriteria;
import com.lach.translink.data.journey.favourite.JourneyCriteriaFavourite;
import com.lach.translink.data.journey.favourite.JourneyCriteriaFavouriteDao;
import com.lach.translink.data.place.PlaceParser;
import com.lach.translink.util.JourneyCriteriaHelper;

import javax.inject.Inject;

public class FavouriteJourneysActivity extends CheckableListActivity<JourneyCriteriaFavourite> {

    @Inject
    JourneyCriteriaFavouriteDao journeyCriteriaFavouriteDao;

    @Override
    public String getTypeDescription() {
        return "journeys";
    }

    @Override
    protected BaseDao<JourneyCriteriaFavourite, ?> getListDao() {
        TranslinkApplication application = (TranslinkApplication) getApplication();
        application.getDataComponent().inject(this);
        return journeyCriteriaFavouriteDao;
    }

    @Override
    protected RecyclerView.Adapter<CheckboxViewHolder> createAdapter() {
        return new CheckboxAdapter(null, getRowLayoutId()) {

            @Override
            public CheckboxViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(getRowLayoutId(), parent, false);
                return new JourneyCheckboxViewHolder(view);
            }

            @Override
            public void onBindViewHolder(CheckboxViewHolder holder, Cursor cursor) {
                super.onBindViewHolder(holder, cursor);

                JourneyCheckboxViewHolder h = (JourneyCheckboxViewHolder) holder;
                JourneyCriteriaFavourite item = getListDao().getItemFromCursor(cursor);

                setText(h.title, item.getName());

                JourneyCriteria criteria = item.getJourneyCriteria();

                PlaceParser placeParser = new PlaceParser();
                setText(h.fromLocationLabel, h.fromLocation, placeParser.prettyPrintPlace(criteria.getFromAddress()));
                setText(h.toLocationLabel, h.toLocation, placeParser.prettyPrintPlace(criteria.getToAddress()));

                setText(h.description, JourneyCriteriaHelper.createJourneyDescription(criteria));
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
        };
    }

    @Override
    protected int getRowLayoutId() {
        return R.layout.l_favourite_journey_checkable;
    }

    private class JourneyCheckboxViewHolder extends CheckboxViewHolder {
        final TextView title;

        final TextView fromLocation;
        final TextView fromLocationLabel;

        final TextView toLocation;
        final TextView toLocationLabel;

        final TextView description;

        public JourneyCheckboxViewHolder(View itemView) {
            super(itemView, R.id.journey_check_box);

            title = findTextViewById(itemView, R.id.journey_title_label);

            fromLocation = findTextViewById(itemView, R.id.journey_from_location);
            fromLocationLabel = findTextViewById(itemView, R.id.journey_from_location_label);

            toLocation = findTextViewById(itemView, R.id.journey_to_location);
            toLocationLabel = findTextViewById(itemView, R.id.journey_to_location_label);

            description = findTextViewById(itemView, R.id.journey_description);
        }

        private TextView findTextViewById(View convertView, int viewResourceId) {
            return (TextView) convertView.findViewById(viewResourceId);
        }

    }
}
