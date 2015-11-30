package com.lach.translink.ui.search;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.databinding.Bindable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.DatePicker;
import android.widget.TimePicker;
import android.widget.Toast;

import com.lach.common.BaseApplication;
import com.lach.common.data.preference.Preferences;
import com.lach.common.data.preference.PreferencesProvider;
import com.lach.common.ui.dialog.MinMaxDatePickerDialog;
import com.lach.common.ui.view.Debouncer;
import com.lach.common.util.DateUtil;
import com.lach.common.util.DialogUtil;
import com.lach.common.util.NetworkUtil;
import com.lach.translink.TranslinkApplication;
import com.lach.translink.activities.BR;
import com.lach.translink.data.journey.JourneyCriteria;
import com.lach.translink.data.journey.favourite.JourneyCriteriaFavouriteDao;
import com.lach.translink.data.journey.history.JourneyCriteriaHistoryDao;
import com.lach.translink.data.journey.JourneyTimeCriteria;
import com.lach.translink.data.journey.JourneyTransport;
import com.lach.translink.data.location.PlaceType;
import com.lach.translink.ui.PrimaryViewModel;
import com.lach.translink.ui.history.HistoryDialog;
import com.lach.translink.ui.result.JourneyResultActivity;
import com.lach.translink.ui.result.JourneyResultActivityDark;
import com.lach.translink.ui.search.dialog.FavouriteJourneysDialog;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.inject.Inject;

public class SearchViewModel extends PrimaryViewModel {
    private static final int REQUEST_HISTORY = 103;
    private static final int REQUEST_SAVED_JOURNEYS = 104;

    private static final int MAX_DAYS_IN_FUTURE = 30;

    // Save object key constants
    private static final String ARRIVE_TYPE_SAVE = "arriveType";
    private static final String TRANSPORT_TYPE_SAVE = "transportType";
    private static final String DATE_SAVE = "date";

    @Inject
    PreferencesProvider preferencesProvider;

    @Inject
    JourneyCriteriaFavouriteDao journeyCriteriaFavouriteDao;

    @Inject
    JourneyCriteriaHistoryDao journeyCriteriaHistoryDao;

    private SearchPlaceViewModel fromPlace;
    private SearchPlaceViewModel toPlace;

    private Date date;

    private JourneyTransport transportType;
    private JourneyTimeCriteria timeCriteria;

    private final Debouncer debouncer;

    public SearchViewModel(Fragment fragment, Debouncer debouncer) {
        super(fragment);
        this.debouncer = debouncer;
    }

    @Override
    public void init(Bundle savedInstanceState) {
        TranslinkApplication application = (TranslinkApplication) getActivity().getApplication();
        application.getDataComponent().inject(this);

        fromPlace = addChildViewModel(new SearchPlaceViewModel(this, PlaceType.FROM));
        toPlace = addChildViewModel(new SearchPlaceViewModel(this, PlaceType.TO));

        if (savedInstanceState != null) {
            transportType = (JourneyTransport) savedInstanceState.getSerializable(TRANSPORT_TYPE_SAVE);
            timeCriteria = (JourneyTimeCriteria) savedInstanceState.getSerializable(ARRIVE_TYPE_SAVE);

            Date date = (Date) savedInstanceState.getSerializable(DATE_SAVE);

            // display the current date
            updateDateTime(date);

        } else {
            transportType = JourneyTransport.All;
            timeCriteria = JourneyTimeCriteria.LeaveAfter;

            setCurrentTime();
        }

        super.init(savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putSerializable(ARRIVE_TYPE_SAVE, timeCriteria);
        outState.putSerializable(TRANSPORT_TYPE_SAVE, transportType);

        outState.putSerializable(DATE_SAVE, date);

        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {

                case REQUEST_SAVED_JOURNEYS:
                    JourneyCriteria journeySearch = data.getParcelableExtra(FavouriteJourneysDialog.INTENT_JOURNEY_CRITERIA_KEY);
                    updateJourneyCriteria(journeySearch, false);

                    return true;

                case REQUEST_HISTORY:
                    JourneyCriteria selectedSearch = data.getParcelableExtra(HistoryDialog.INTENT_JOURNEY_CRITERIA_KEY);

                    Preferences preferences = preferencesProvider.getPreferences();
                    updateJourneyCriteria(selectedSearch, preferences.getBoolean("IgnoreHistoryTime", true));

                    return true;
            }
        }
        return super.onActivityResult(requestCode, resultCode, data);
    }

    @Bindable
    public SearchPlaceViewModel getFromPlace() {
        return fromPlace;
    }

    @Bindable
    public SearchPlaceViewModel getToPlace() {
        return toPlace;
    }

    @Bindable
    public String getDate() {
        DateFormat dateFormat = SimpleDateFormat.getDateInstance(DateFormat.SHORT);
        return dateFormat.format(date);
    }

    @Bindable
    public String getTime() {
        DateFormat timeFormat = SimpleDateFormat.getTimeInstance(DateFormat.SHORT);
        return timeFormat.format(date);
    }

    @SuppressWarnings("SameReturnValue")
    @Bindable
    public int getVisibility() {
        // TODO: This is a work around. Currently data binding crashes on certain views if they don't have binding.
        return View.VISIBLE;
    }

    public View.OnClickListener getSwapPlacesClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (fromPlace.address == null && toPlace.address == null) {
                    // Don't update the UI if they both the same.
                    return;
                }
                if (fromPlace.address != null && toPlace.address != null) {
                    if (fromPlace.address.equals(toPlace.address)) {
                        // Don't update the UI if they both the same.
                        return;
                    }
                }
                String tmp = fromPlace.address;
                fromPlace.address = toPlace.address;
                toPlace.address = tmp;

                notifyPropertyChanged(BR.fromPlace);
                notifyPropertyChanged(BR.toPlace);
            }
        };
    }

    public Object[] getLeaveTypeEntries() {
        return JourneyTimeCriteria.values();
    }

    public Object[] getTransportTypeEntries() {
        return JourneyTransport.values();
    }

    @Bindable
    public int getLeaveTypeIndex() {
        return timeCriteria.ordinal();
    }

    @Bindable
    public int getTransportTypeIndex() {
        return transportType.ordinal();
    }

    public AdapterView.OnItemSelectedListener getLeaveTypeSelectedListener() {
        return new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                timeCriteria = JourneyTimeCriteria.values()[position];
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        };
    }

    public AdapterView.OnItemSelectedListener getTransportTypeSelectedListener() {
        return new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                transportType = JourneyTransport.values()[position];
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        };
    }

    public View.OnClickListener getSearchClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!validateSearchParameters()) {
                    return;
                }

                FragmentActivity activity = getActivity();
                if (!NetworkUtil.isOnline(activity)) {
                    return;
                }

                //loadSettings();

                // Pick the correct Results Activity based on the current theme.
                Class resultActivityClass;
                if ((((BaseApplication)activity.getApplication()).isLightTheme(activity))) {
                    resultActivityClass = JourneyResultActivity.class;
                } else {
                    resultActivityClass = JourneyResultActivityDark.class;
                }

                Intent resultsIntent = new Intent(activity, resultActivityClass);
                resultsIntent.putExtra(JourneyResultActivity.JOURNEY_CRITERIA, createJourneyCriteria());
                resultsIntent.putExtra(JourneyResultActivity.JOURNEY_DATE, date);
                activity.startActivity(resultsIntent);
            }
        };
    }

    private boolean validateSearchParameters() {
        String fromAddress = fromPlace.address;
        String toAddress = toPlace.address;

        if (fromAddress == null || toAddress == null) {
            DialogUtil.showAlertDialog(getContext(), "Please provide a " + PlaceType.FROM.getDescription() + " and " + PlaceType.TO.getDescription(), "Invalid criteria");
            return false;
        }

        if (fromAddress.equals(toAddress)) {
            DialogUtil.showAlertDialog(getContext(), PlaceType.FROM.getDescription() + " and " + PlaceType.TO.getDescription() + " cannot be the same.", "Invalid criteria");
            return false;
        }

        return true;
    }

    public View.OnClickListener getResetClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fromPlace.clearLocation();
                toPlace.clearLocation();

                timeCriteria = JourneyTimeCriteria.LeaveAfter;
                transportType = JourneyTransport.All;

                notifyPropertyChanged(BR.leaveTypeIndex);
                notifyPropertyChanged(BR.transportTypeIndex);

                setCurrentTime();
            }
        };
    }

    public View.OnTouchListener getDateTouchListener() {
        return new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Date currentDate = new Date();

                Calendar calendar = Calendar.getInstance();
                calendar.setTime(date);

                MinMaxDatePickerDialog.ConstraintParams constraintParams = new MinMaxDatePickerDialog.ConstraintParams(currentDate, MAX_DAYS_IN_FUTURE);
                new MinMaxDatePickerDialog(
                        getContext(),
                        mDateSetListener,
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH),
                        constraintParams
                ).show();
                return true;
            }
        };
    }

    private final DatePickerDialog.OnDateSetListener mDateSetListener = new DatePickerDialog.OnDateSetListener() {
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);

            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, monthOfYear);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

            updateDateTime(calendar.getTime());
        }
    };

    public View.OnTouchListener getTimeTouchListener() {
        return new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(date);

                new TimePickerDialog(
                        getContext(),
                        mTimeSetListener,
                        calendar.get(Calendar.HOUR_OF_DAY),
                        calendar.get(Calendar.MINUTE),
                        android.text.format.DateFormat.is24HourFormat(getContext())
                ).show();
                return true;
            }
        };

    }

    private final TimePickerDialog.OnTimeSetListener mTimeSetListener = new TimePickerDialog.OnTimeSetListener() {
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);

            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
            calendar.set(Calendar.MINUTE, minute);

            updateDateTime(calendar.getTime());
        }
    };

    public Debouncer getDebouncer() {
        return debouncer;
    }

    public void setCurrentTime() {
        updateDateTime(new Date());
    }

    private void updateDateTime(Date date) {
        this.date = date;

        notifyPropertyChanged(BR.date);
        notifyPropertyChanged(BR.time);
    }

    /**
     * Double check and ensure that the date value is set not into the past or too far in the future.
     */
    private void validateDateTimeConstraints() {
        Calendar inputCalendar = Calendar.getInstance();
        inputCalendar.setTime(date);

        // Check if the date has been altered due to constraints.
        if (DateUtil.constrainDate(inputCalendar, new Date(), MAX_DAYS_IN_FUTURE)) {
            updateDateTime(inputCalendar.getTime());
        }
    }

    public JourneyCriteria createJourneyCriteria() {
        JourneyCriteria journeyCriteria = new JourneyCriteria();

        journeyCriteria.setFromAddress(fromPlace.address);
        journeyCriteria.setToAddress(toPlace.address);

        journeyCriteria.setJourneyTransport(transportType);
        journeyCriteria.setJourneyTimeCriteria(timeCriteria);

        validateDateTimeConstraints();
        journeyCriteria.setTime(date);

        return journeyCriteria;
    }

    private void updatePlace(PlaceType placeType, String address) {
        if (placeType == PlaceType.FROM) {
            fromPlace.changeLocation(address);
        } else {
            toPlace.changeLocation(address);
        }
    }

    private void updateJourneyCriteria(JourneyCriteria journey, boolean ignoreTime) {
        if (journey != null) {
            updatePlace(PlaceType.FROM, journey.getFromAddress());
            updatePlace(PlaceType.TO, journey.getToAddress());

            if (!ignoreTime) {
                Date time = journey.getTime();
                if (time != null) {
                    // We only want to extract the time from the date object.
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(time);

                    int hourOfDay = calendar.get(Calendar.HOUR_OF_DAY);
                    int minute = calendar.get(Calendar.MINUTE);

                    calendar.setTime(date);
                    calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    calendar.set(Calendar.MINUTE, minute);

                    updateDateTime(calendar.getTime());
                }
            }

            if (journey.getJourneyTimeCriteria() != null) {
                timeCriteria = journey.getJourneyTimeCriteria();
                notifyPropertyChanged(BR.leaveTypeIndex);
            }

            if (journey.getJourneyTransport() != null) {
                transportType = journey.getJourneyTransport();
                notifyPropertyChanged(BR.transportTypeIndex);
            }
        }
    }

    public void loadJourney() {
        if (journeyCriteriaFavouriteDao.getRowCount() == 0) {
            Toast.makeText(getActivity(), "No journeys saved.", Toast.LENGTH_SHORT)
                    .show();
            return;
        }

        FavouriteJourneysDialog dialog = FavouriteJourneysDialog.newInstance();
        dialog.setTargetFragment(getFragment(), REQUEST_SAVED_JOURNEYS);
        dialog.show(getActivity().getSupportFragmentManager(), "dialog");
    }

    public void showHistory() {
        if (journeyCriteriaHistoryDao.getRowCount() == 0) {
            Toast.makeText(getActivity(), "No history saved.", Toast.LENGTH_SHORT).show();
            return;
        }

        //loadSettings();

        Preferences preferences = preferencesProvider.getPreferences();
        HistoryDialog dialog = HistoryDialog.newInstance(preferences.getBoolean("IgnoreHistoryTime", true));
        dialog.setTargetFragment(getFragment(), REQUEST_HISTORY);
        dialog.show(getActivity().getSupportFragmentManager(), "dialog");
    }
}
