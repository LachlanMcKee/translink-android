package com.lach.translink.ui.impl.search;

import android.app.Activity;
import android.content.Intent;
import android.databinding.Bindable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

import com.fourmob.datetimepicker.date.CalendarDay;
import com.fourmob.datetimepicker.date.DatePickerDialog;
import com.lach.common.BaseApplication;
import com.lach.common.data.preference.Preferences;
import com.lach.common.data.preference.PreferencesProvider;
import com.lach.common.ui.view.Debouncer;
import com.lach.common.util.DateUtil;
import com.lach.common.util.DialogUtil;
import com.lach.common.util.NetworkUtil;
import com.lach.translink.TranslinkApplication;
import com.lach.translink.activities.BR;
import com.lach.translink.data.journey.JourneyCriteria;
import com.lach.translink.data.journey.JourneyTimeCriteria;
import com.lach.translink.data.journey.JourneyTransport;
import com.lach.translink.data.journey.favourite.JourneyCriteriaFavouriteDao;
import com.lach.translink.data.journey.history.JourneyCriteriaHistoryDao;
import com.lach.translink.data.location.PlaceType;
import com.lach.translink.ui.impl.PrimaryViewModel;
import com.lach.translink.ui.impl.UiPreference;
import com.lach.translink.ui.impl.history.HistoryDialog;
import com.lach.translink.ui.impl.result.JourneyResultActivity;
import com.lach.translink.ui.impl.result.JourneyResultActivityDark;
import com.lach.translink.ui.impl.search.dialog.FavouriteJourneysDialog;
import com.sleepbot.datetimepicker.time.RadialPickerLayout;
import com.sleepbot.datetimepicker.time.TimePickerDialog;

import java.text.DateFormat;
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

    private static final String FRAGMENT_TAG_DATE_PICKER = "date_picker";
    private static final String FRAGMENT_TAG_TIME_PICKER = "timer_picker";

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

            // Check if the date picker, or time picker dialogs were recreated. Attach their listeners if required.
            DatePickerDialog datePicker = (DatePickerDialog) getActivity().getSupportFragmentManager().findFragmentByTag(FRAGMENT_TAG_DATE_PICKER);
            if (datePicker != null) {
                datePicker.setOnDateSetListener(mDateSetListener);
            }

            TimePickerDialog timePicker = (TimePickerDialog) getActivity().getSupportFragmentManager().findFragmentByTag(FRAGMENT_TAG_TIME_PICKER);
            if (timePicker != null) {
                timePicker.setOnTimeSetListener(mTimeSetListener);
            }

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
                    updateJourneyCriteria(selectedSearch, UiPreference.IGNORE_HISTORY_TIME.get(preferences));

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
        DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(getContext());
        return dateFormat.format(date);
    }

    @Bindable
    public String getTime() {
        DateFormat timeFormat = android.text.format.DateFormat.getTimeFormat(getContext());
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
                if ((((BaseApplication)activity.getApplication()).isLightTheme())) {
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

    public View.OnClickListener getDateClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(date);

                DatePickerDialog datePickerDialog = DatePickerDialog.newInstance(
                        mDateSetListener,
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH)
                );

                calendar.setTime(new Date());

                Calendar maxDate = Calendar.getInstance();
                maxDate.setTime(new Date());
                maxDate.add(Calendar.DAY_OF_YEAR, MAX_DAYS_IN_FUTURE);

                datePickerDialog.setDateConstraints(new CalendarDay(calendar), new CalendarDay(maxDate));

                datePickerDialog.setVibrate(false);
                datePickerDialog.setPulseAnimationsEnabled(false);
                datePickerDialog.show(getActivity().getSupportFragmentManager(), FRAGMENT_TAG_DATE_PICKER);
            }
        };
    }

    private final DatePickerDialog.OnDateSetListener mDateSetListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePickerDialog datePickerDialog, int year, int monthOfYear, int dayOfMonth) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);

            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, monthOfYear);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

            updateDateTime(calendar.getTime());
        }
    };

    public View.OnClickListener getTimeClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(date);

                TimePickerDialog timePickerDialog = TimePickerDialog.newInstance(
                        mTimeSetListener,
                        calendar.get(Calendar.HOUR_OF_DAY),
                        calendar.get(Calendar.MINUTE),
                        android.text.format.DateFormat.is24HourFormat(getContext())
                );
                timePickerDialog.setVibrate(false);
                timePickerDialog.setPulseAnimationsEnabled(false);
                timePickerDialog.show(getActivity().getSupportFragmentManager(), FRAGMENT_TAG_TIME_PICKER);
            }
        };
    }

    private final TimePickerDialog.OnTimeSetListener mTimeSetListener = new TimePickerDialog.OnTimeSetListener() {
        @Override
        public void onTimeSet(RadialPickerLayout radialPickerLayout, int hourOfDay, int minute) {
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
        HistoryDialog dialog = HistoryDialog.newInstance(UiPreference.IGNORE_HISTORY_TIME.get(preferences));
        dialog.setTargetFragment(getFragment(), REQUEST_HISTORY);
        dialog.show(getActivity().getSupportFragmentManager(), "dialog");
    }
}
