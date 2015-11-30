package com.lach.common.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public abstract class ViewHolderArrayAdapter<T> extends ArrayAdapter<T> {
    private final ArrayList<ViewHolderHelper> viewHolderHelperList;

    @SuppressWarnings("SameParameterValue")
    protected ViewHolderArrayAdapter(Context context, int textViewResourceId, List<T> objects) {
        super(context, textViewResourceId, objects);

        viewHolderHelperList = new ArrayList<>();
    }

    protected void addViewHolderHelper(ViewHolderHelper viewHolderHelper) {
        viewHolderHelperList.add(viewHolderHelper);
    }

    protected interface ViewHolder {

    }

    public static abstract class ViewHolderHelper<D, H extends ViewHolder> {
        final int viewResourceId;

        public ViewHolderHelper(int viewResourceId) {
            this.viewResourceId = viewResourceId;
        }

        public abstract boolean isHolderForData(D data);

        protected TextView findTextViewById(View convertView, int viewResourceId) {
            return (TextView) convertView.findViewById(viewResourceId);
        }

        public abstract H createViewHolder(View convertView);

        public abstract void updateViewHolder(H holder, D item);
    }

    @SuppressWarnings("unchecked")
    @Override
    public int getItemViewType(int position) {
        int l = viewHolderHelperList.size();
        for (int i = 0; i < l; i++) {
            if (viewHolderHelperList.get(i).isHolderForData(getItem(position))) {
                return i;
            }
        }

        return -1;
    }

    @Override
    public int getViewTypeCount() {
        return viewHolderHelperList.size();
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        T item = getItem(position);

        // if the array item is null, nothing to display, just return null
        if (item == null) {
            return null;
        }

        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        int viewType = this.getItemViewType(position);

        ViewHolderHelper helper = viewHolderHelperList.get(viewType);
        ViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(helper.viewResourceId, parent, false);

            onCreateViewCreated(convertView);
            holder = helper.createViewHolder(convertView);

            // store the holder with the view.
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        //noinspection unchecked
        helper.updateViewHolder(holder, item);

        return convertView;
    }

    @SuppressWarnings({"UnusedParameters", "EmptyMethod"})
    private void onCreateViewCreated(View convertView) {

    }

}