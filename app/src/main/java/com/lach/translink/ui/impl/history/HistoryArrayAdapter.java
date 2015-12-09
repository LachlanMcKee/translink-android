package com.lach.translink.ui.impl.history;

import android.content.Context;
import android.os.Build;
import android.view.View;
import android.widget.TextView;

import com.lach.common.ui.adapter.ViewHolderArrayAdapter;
import com.lach.translink.activities.R;
import com.lach.translink.data.journey.JourneyCriteria;
import com.lach.translink.data.journey.history.JourneyCriteriaHistory;
import com.lach.translink.data.place.PlaceParser;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

public class HistoryArrayAdapter extends ViewHolderArrayAdapter<JourneyCriteriaHistory> {
    private final PlaceParser placeParser;
    private final boolean ignoreHistoryTime;

    public HistoryArrayAdapter(Context context, PlaceParser placeParser, List<? extends JourneyCriteriaHistory> list, boolean ignoreHistoryTime) {
        super(context, R.layout.l_journey_history, (List<JourneyCriteriaHistory>) list);

        this.placeParser = placeParser;
        this.ignoreHistoryTime = ignoreHistoryTime;

        addViewHolderHelper(new HistoryViewHolderHelper());
    }

    static class HistoryViewHolder implements ViewHolder {
        TextView fromLocation;
        TextView toLocation;
        TextView time;
    }

    private class HistoryViewHolderHelper extends ViewHolderHelper<JourneyCriteriaHistory, HistoryViewHolder> {

        public HistoryViewHolderHelper() {
            super(R.layout.l_journey_history);
        }

        @Override
        public boolean isHolderForData(JourneyCriteriaHistory data) {
            return true;
        }

        @Override
        public HistoryViewHolder createViewHolder(View convertView) {
            HistoryViewHolder holder = new HistoryViewHolder();
            holder.fromLocation = findTextViewById(convertView, R.id.history_from_loc);
            holder.toLocation = findTextViewById(convertView, R.id.history_to_loc);

            holder.time = findTextViewById(convertView, R.id.history_time);

            return holder;
        }

        @Override
        public void updateViewHolder(HistoryViewHolder holder, JourneyCriteriaHistory item) {
            JourneyCriteria journeyCriteria = item.getJourneyCriteria();

            holder.fromLocation.setText(placeParser.prettyPrintPlace(journeyCriteria.getFromAddress()));
            holder.toLocation.setText(placeParser.prettyPrintPlace(journeyCriteria.getToAddress()));

            if (!ignoreHistoryTime) {
                DateFormat timeFormat = SimpleDateFormat.getTimeInstance(DateFormat.SHORT);
                holder.time.setText(timeFormat.format(journeyCriteria.getTime()));
            } else {
                holder.time.setVisibility(View.GONE);
            }

            if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
                int textColour = getContext().getResources().getColor(android.R.color.black);

                holder.fromLocation.setTextColor(textColour);
                holder.toLocation.setTextColor(textColour);

                holder.time.setTextColor(textColour);
            }
        }
    }

}