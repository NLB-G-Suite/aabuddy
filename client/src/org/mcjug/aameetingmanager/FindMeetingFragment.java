package org.mcjug.aameetingmanager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.mcjug.aameetingmanager.LocationFinder.LocationResult;
import org.mcjug.aameetingmanager.MultiSpinner.MultiSpinnerListener;
import org.mcjug.aameetingmanager.util.DateTimeUtil;
import org.mcjug.aameetingmanager.util.LocationUtil;

import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;

public class FindMeetingFragment extends Fragment {
	private static final String TAG = FindMeetingFragment.class.getSimpleName();
	private static final String EMPTY_TIME = "--:--";

	private EditText nameEditText;
	private EditText addressEditText;
	private Button currentLocationButton;
	private Button startTimeButton;
	private Button startTimeClearButton;
	private Button endTimeButton;
	private Button endTimeClearButton;
	private Button findMeetingButton;
	private Calendar startTimeCalendar;
	private Calendar endTimeCalendar;
	private DaysOfWeekMultiSpinner daysOfWeekSpinner;
	private Spinner distanceSpinner;
	
	private ProgressDialog progress;
	private LocationResult locationResult;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View view = inflater.inflate(R.layout.find_meeting_fragment, container, false);

		startTimeCalendar = Calendar.getInstance();

		startTimeButton = (Button) view.findViewById(R.id.findMeetingStartTimeButton);
		startTimeButton.setText(DateTimeUtil.getTimeStr(startTimeCalendar));
		startTimeButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				TimePickerDialog d = new TimePickerDialog(getActivity(), startTimeDialogListener, 
						startTimeCalendar.get(Calendar.HOUR_OF_DAY), startTimeCalendar.get(Calendar.MINUTE), true);
				d.show();			
			}
		});
		
		startTimeClearButton = (Button) view.findViewById(R.id.findMeetingStartTimeClearButton);
		startTimeClearButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				int width = startTimeButton.getWidth();
				startTimeButton.setText(EMPTY_TIME);
				startTimeButton.setWidth(width);
			}
		});

		endTimeCalendar = Calendar.getInstance();
		endTimeCalendar.add(Calendar.HOUR_OF_DAY, 1);
		
		endTimeButton = (Button) view.findViewById(R.id.findMeetingEndTimeButton);
		endTimeButton.setText(DateTimeUtil.getTimeStr(endTimeCalendar));
		endTimeButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				TimePickerDialog d = new TimePickerDialog(getActivity(), endTimeDialogListener, 
						endTimeCalendar.get(Calendar.HOUR_OF_DAY), endTimeCalendar.get(Calendar.MINUTE), true);
				d.show();			
			}
		});
		
		endTimeClearButton = (Button) view.findViewById(R.id.findMeetingEndTimeClearButton);
		endTimeClearButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				int width = endTimeButton.getWidth();
				endTimeButton.setText(EMPTY_TIME);
				endTimeButton.setWidth(width);
			}
		});

		daysOfWeekSpinner = (DaysOfWeekMultiSpinner) view.findViewById(R.id.findMeetingDaysOfWeekSpinner);
		List<String> daysOfWeekListItems = Arrays.asList(getResources().getStringArray(R.array.daysOfWeek));
		daysOfWeekSpinner.setItems(daysOfWeekListItems, getString(R.string.all_days_of_week), daysOfWeekSpinnerListener);

		nameEditText = (EditText) view.findViewById(R.id.findMeetingNameEditText);
	    addressEditText = (EditText) view.findViewById(R.id.findMeetingAddressEditText);

	    currentLocationButton = (Button) view.findViewById(R.id.findMeetingCurrentLocationButton); 
	    currentLocationButton.setOnClickListener(new OnClickListener() { 
			public void onClick(View v) {
				try {
					Context context = getActivity();
					progress = ProgressDialog.show(context, context.getString(R.string.getLocationMsg), context.getString(R.string.waitMsg));
					LocationFinder locationTask = new LocationFinder(getActivity(), locationResult);
					locationTask.requestLocation();
				} catch (Exception ex) {
				    Log.d(TAG, "Error current location " + ex);
				}
			} 
		}); 

	    findMeetingButton = (Button) view.findViewById(R.id.findMeetingFindButton); 
		findMeetingButton.setOnClickListener(new OnClickListener() { 
			public void onClick(View v) {
				try {
					FindMeetingTask findMeetingTask = new FindMeetingTask(getActivity(), getFindMeetingParams());
					findMeetingTask.execute();
				} catch (Exception ex) {
				    Log.d(TAG, "Error getting meetings: " + ex);
				}
			} 
		}); 
		
	    distanceSpinner = (Spinner) view.findViewById(R.id.findMeetingDistanceSpinner); 
		List<String> distanceValues = Arrays.asList(getResources().getStringArray(R.array.searchDistanceValues));
	    distanceSpinner.setSelection(distanceValues.indexOf("20"));
	    
		return view;
	}
	
	public void onActivityCreated(Bundle savedInstanceState) {
		locationResult =  new LocationResult() {
			@Override
			public void setLocation(Location location) {
				progress.cancel();
				
				if (location == null) {
					location = LocationUtil.getLastKnownLocation(getActivity());
				}

				if (location == null) {
					Toast.makeText(getActivity(), "Not able to get current location. Please check if GPS is turned or you have a network data connection.", Toast.LENGTH_LONG).show();
				} else {
					String address = LocationUtil.getAddress(location, getActivity());
					if (address.trim().equals("")) {
						Toast.makeText(getActivity(), "Not able to get address from location. Please check for a network data connection", Toast.LENGTH_LONG).show();
					} else {
						addressEditText.setText(address);
					}
				}
			}
		};
		
		super.onActivityCreated(savedInstanceState);
	}
	
	public void setFocus() {
		if (nameEditText != null) {
			nameEditText.requestFocus();
		}
	}
	
	public void setKeyDown(int keyCode, KeyEvent event) {
	    // this will happen on first key pressed on hard-keyboard only. Once nameEditText 
	    // gets the focus again, it will automatically receive further key presses.
	    if (nameEditText != null && !nameEditText.hasFocus()){ 
	    	nameEditText.requestFocus();
	        nameEditText.onKeyDown(keyCode, event);
	    }
	}
	
	private final TimePickerDialog.OnTimeSetListener startTimeDialogListener = new TimePickerDialog.OnTimeSetListener() {
		public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
			startTimeCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
			startTimeCalendar.set(Calendar.MINUTE, minute);
			startTimeButton.setText(DateTimeUtil.getTimeStr(startTimeCalendar));
		}
	};

	private final TimePickerDialog.OnTimeSetListener endTimeDialogListener = new TimePickerDialog.OnTimeSetListener() {
		public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
			endTimeCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
			endTimeCalendar.set(Calendar.MINUTE, minute);
			endTimeButton.setText(DateTimeUtil.getTimeStr(endTimeCalendar));
		}
	};
		
	private final MultiSpinnerListener daysOfWeekSpinnerListener = new MultiSpinnerListener() {
		public void onItemsSelected(boolean[] selected) {
		}
	};
	
	private String getFindMeetingParams() throws Exception {
		List<NameValuePair> params = new ArrayList<NameValuePair>();
	        
		String name = nameEditText.getText().toString().trim();
		if (!name.equals("")) {
			params.add(new BasicNameValuePair("name", name));
		}
		
		if (!startTimeButton.getText().equals(EMPTY_TIME)) {
			params.add(new BasicNameValuePair("start_time__gte", DateTimeUtil.getFindMeetingTimeStr(startTimeCalendar)));
		}
		
		if (!endTimeButton.getText().equals(EMPTY_TIME )) {
			params.add(new BasicNameValuePair("end_time__lte", DateTimeUtil.getFindMeetingTimeStr(endTimeCalendar)));
		}
		
		String[] daysOfWeekSelections = ((String)daysOfWeekSpinner.getSelectedItem()).split(",");
		if (daysOfWeekSelections[0].equalsIgnoreCase(getString(R.string.all_days_of_week))) {
			daysOfWeekSelections = getString(R.string.all_days_of_week_value).split(",");
	    } 
		
		List<String> daysOfWeekAbbr = Arrays.asList(getResources().getStringArray(R.array.daysOfWeekAbbr));
		for (String str: daysOfWeekSelections) {				
			int idx = daysOfWeekAbbr.indexOf(str.trim());
			params.add(new BasicNameValuePair("day_of_week_in", String.valueOf(idx)));
		}

		String addressName = addressEditText.getText().toString();
		if (addressName.trim().equals("")) {
			Location location = LocationUtil.getLastKnownLocation(getActivity());
			if (location == null) {
				Toast.makeText(getActivity(), "Please enter an address", Toast.LENGTH_LONG).show();
			} else {
				params.add(new BasicNameValuePair("lat", String.valueOf(location.getLatitude())));
				params.add(new BasicNameValuePair("long", String.valueOf(location.getLongitude())));
			}
			
		} else {	
			Address address = LocationUtil.getAddressFromLocationName(addressName, getActivity());
			if (address != null) {
				params.add(new BasicNameValuePair("lat", String.valueOf(address.getLatitude())));
				params.add(new BasicNameValuePair("long", String.valueOf(address.getLongitude())));
				
				String[] mileValues = getResources().getStringArray(R.array.searchDistanceValues);
				params.add(new BasicNameValuePair("distance_miles", mileValues[distanceSpinner.getSelectedItemPosition()]));

			} else {
			    Log.d(TAG, "Address is invalid: " + address);
				throw new Exception("Address is invalid: " + address);
			}
		}
		
		params.add(new BasicNameValuePair("order_by", getString(R.string.sortingDefault)));

		String paramStr = URLEncodedUtils.format(params, "utf-8");
	    Log.d(TAG, "Find meeting request params: " + paramStr);

		return paramStr;
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.activity_main, menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);
		
        switch (item.getItemId()) {

	        // Admin prefs
	        case R.id.adminPrefs:
	            startActivity(new Intent(getActivity().getApplicationContext(), AdminPrefsActivity.class)
	                    .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
	            return true;
        }

		
		return false;
	}
}
