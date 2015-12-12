package com.lach.translink.ui.impl.gocard;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import com.lach.common.ui.adapter.ViewHolderArrayAdapter;
import com.lach.translink.activities.R;
import com.lach.translink.data.gocard.GoCardTransaction;
import com.lach.translink.data.gocard.GoCardTransactionGroup;
import com.lach.translink.data.gocard.GoCardTransactionJourney;
import com.lach.translink.data.gocard.GoCardTransactionTopUp;

import java.util.ArrayList;

public class GoCardTransactionArrayAdapter extends ViewHolderArrayAdapter<GoCardTransaction> {

    public GoCardTransactionArrayAdapter(Context context, ArrayList<GoCardTransaction> list) {
        super(context, R.layout.l_journey_history, list);

        addViewHolderHelper(new TitleViewHolderHelper());
        addViewHolderHelper(new JourneyViewHolderHelper());
        addViewHolderHelper(new TopUpViewHolderHelper());
    }

    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    @Override
    public boolean isEnabled(int position) {
        return false;
    }

    static class TitleViewHolder implements ViewHolder {
        TextView titleText;
    }

    static class TitleViewHolderHelper extends ViewHolderHelper<GoCardTransaction, TitleViewHolder> {

        public TitleViewHolderHelper() {
            super(R.layout.l_gocard_history_title);
        }

        @Override
        public boolean isHolderForData(GoCardTransaction data) {
            return data instanceof GoCardTransactionGroup;
        }

        @Override
        public TitleViewHolder createViewHolder(View convertView) {
            TitleViewHolder holder = new TitleViewHolder();
            holder.titleText = findTextViewById(convertView, R.id.gocard_history_title);

            return holder;
        }

        @Override
        public void updateViewHolder(TitleViewHolder holder, GoCardTransaction item) {
            GoCardTransactionGroup group = (GoCardTransactionGroup) item;
            holder.titleText.setText(group.title);
        }
    }

    static class JourneyViewHolder implements ViewHolder {
        TextView dateText, fromText, toText, costText;
    }

    private class JourneyViewHolderHelper extends ViewHolderHelper<GoCardTransaction, JourneyViewHolder> {

        public JourneyViewHolderHelper() {
            super(R.layout.l_gocard_history_journey);
        }

        @Override
        public boolean isHolderForData(GoCardTransaction data) {
            return data instanceof GoCardTransactionJourney;
        }

        @Override
        public JourneyViewHolder createViewHolder(View convertView) {
            JourneyViewHolder holder = new JourneyViewHolder();
            holder.dateText = findTextViewById(convertView, R.id.gocard_history_date);
            holder.fromText = findTextViewById(convertView, R.id.gocard_history_from);
            holder.toText = findTextViewById(convertView, R.id.gocard_history_to);
            holder.costText = findTextViewById(convertView, R.id.gocard_history_cost);

            return holder;
        }

        @Override
        public void updateViewHolder(JourneyViewHolder holder, GoCardTransaction item) {
            GoCardTransactionJourney journeyTransaction = (GoCardTransactionJourney) item;
            holder.dateText.setText(journeyTransaction.getTimeString());
            holder.fromText.setText(journeyTransaction.fromLocation);
            holder.toText.setText(journeyTransaction.toLocation);
            holder.costText.setText(journeyTransaction.amount);
        }
    }

    static class TopUpViewHolder implements ViewHolder {
        TextView dateText, eventText, resultText;
    }

    private class TopUpViewHolderHelper extends ViewHolderHelper<GoCardTransaction, TopUpViewHolder> {

        public TopUpViewHolderHelper() {
            super(R.layout.l_gocard_history_top_up);
        }

        @Override
        public boolean isHolderForData(GoCardTransaction data) {
            return data instanceof GoCardTransactionTopUp;
        }

        @Override
        public TopUpViewHolder createViewHolder(View convertView) {
            TopUpViewHolder holder = new TopUpViewHolder();
            holder.dateText = findTextViewById(convertView, R.id.gocard_history_date);
            holder.eventText = findTextViewById(convertView, R.id.gocard_history_event);
            holder.resultText = findTextViewById(convertView, R.id.gocard_history_result);

            return holder;
        }

        @Override
        public void updateViewHolder(TopUpViewHolder holder, GoCardTransaction item) {
            GoCardTransactionTopUp topUpTransaction = (GoCardTransactionTopUp) item;
            holder.dateText.setText(topUpTransaction.time);
            holder.eventText.setText(topUpTransaction.description);

            holder.resultText.setText(getContext().getString(R.string.gocard_info_amout_range,
                    topUpTransaction.oldAmount, topUpTransaction.newAmount));
        }
    }

}