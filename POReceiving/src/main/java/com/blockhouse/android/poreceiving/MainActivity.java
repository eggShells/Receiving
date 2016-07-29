package com.blockhouse.android.poreceiving;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

import com.blockhouse.android.prefs.SettingsActivity;
import com.blockhouse.android.utils.AwareActivity;
import com.blockhouse.android.utils.HttpQuery;
import com.blockhouse.android.utils.HttpQueryListener;
import com.blockhouse.android.utils.Utils;
import com.blockhouse.android.utils.WifiUtils;
import com.blockhouse.android.utils.WifiUtils.OnConnectionListener;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.app.ActionBar;
import android.app.ActivityOptions;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.Time;
import android.transition.Explode;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

public class MainActivity extends AwareActivity implements OnItemSelectedListener, HttpQueryListener<ArrayList>, OnClickListener {

	class PORecComparator implements Comparator<PORecord> {

		private final boolean sortByPO;

		public PORecComparator(final boolean inSortByPO) {
			sortByPO = inSortByPO;
		}

		@Override
		public int compare(PORecord poOne, PORecord poTwo) {
			if (sortByPO) {
				return poOne.getPoNum().compareTo(poTwo.getPoNum());
			}
			return poOne.getReqDate() - poTwo.getReqDate();
		}

	}
	private RecyclerView poList = null;
	private Spinner spnSortBy = null;
	private POAdapter listAdapter = null;
	private ArrayList<PORecord> data = null;
	private ProgressDialog progressCircle = null;
	private SharedPreferences sharedPref = null;

	private SwipeRefreshLayout curSwipeRefresh = null;

	private final Set<BroadcastReceiver> rcvrs = new HashSet<BroadcastReceiver>();
	private int curDay = Time.getJulianDay(System.currentTimeMillis(), 0);

	private long lastUpdate = 0;
	public static final int REQ_PREFERENCE = 1;

	public static final int REQ_RECEIVE = 2;

	public static final String PO_REC = "porec";

	private boolean processingClick = false;
	protected static int pref_PastDays = 30;
	protected static int pref_FutureDays = 15;

	protected static boolean pref_OnlyBalDue = true;

	/**
	 * Sets up the action bar.
	 */
	private void configureActionBar() {
		final ActionBar ab = getActionBar();
		ab.hide();

		String title = getString(R.string.appTitle);
		if (HttpQuery.STND_URL.contains("test")) title += " *TEST*";

		ab.setTitle(title);
		ab.setSubtitle(R.string.rcptDueBy);

		ab.show();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Explode transition
		getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
		getWindow().setExitTransition(new Explode());

		setContentView(R.layout.activity_main);
		sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
		final BroadcastReceiver bcRcvr = WifiUtils.ensureConnection(this, new OnConnectionListener() {
			@Override
			public void onConnection() {
				// Check for Update
				Utils.checkForUpdates(MainActivity.this, getString(R.string.app_name));
				updateHeartbeat("onResume");
				setRunning(true);
				getPOList();
			}

		});
		if (bcRcvr != null) {
			rcvrs.add(bcRcvr);
		}

		// Get RecyclerView
		poList = (RecyclerView) findViewById(R.id.POList);
		poList.setLayoutManager(new LinearLayoutManager(poList.getContext()));
		final SwipeRefreshLayout swipeView = (SwipeRefreshLayout) findViewById(R.id.POListSwipe);
		swipeView.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
			@Override
			public void onRefresh() {
				curSwipeRefresh = swipeView;
				swipeView.setRefreshing(true);
				refreshPOList();
			}
		});

		spnSortBy = (Spinner) findViewById(R.id.SortBy);
		spnSortBy.setAdapter(ArrayAdapter.createFromResource(this, R.array.sortbyoptions, android.R.layout.simple_spinner_item));
		spnSortBy.setOnItemSelectedListener(this);

		// Last selection
		spnSortBy.setSelection(sharedPref.getInt(getString(R.string.sort_by), 0));

		// Set up polist filter
		TextWatcher filterWatcher = new TextWatcher() {

			@Override
			public void afterTextChanged(Editable inS) {
			}

			@Override
			public void beforeTextChanged(CharSequence inS, int inStart, int inCount, int inAfter) {
			}

			@Override
			public void onTextChanged(CharSequence inS, int inStart, int inBefore, int inCount) {
				if (inS.length() > 0) {
					EditText curText;
					if (Character.isDigit(inS.charAt(0))) {
						// PO Number
						curText = ((EditText) findViewById(R.id.vendFilter));
					}
					else {
						// Vendor
						curText = ((EditText) findViewById(R.id.poFilter));
					}
					curText.removeTextChangedListener(this);
					curText.setText("");
					curText.addTextChangedListener(this);
				}

				if (listAdapter != null) {
					listAdapter.getFilter().filter(inS);
				}
			}

		};

		((EditText) findViewById(R.id.vendFilter)).addTextChangedListener(filterWatcher);
		((EditText) findViewById(R.id.poFilter)).addTextChangedListener(filterWatcher);

		configureActionBar();
		// Load default preferences, if not already
		PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
	}

	@Override
	protected void onResume() {
		super.onResume();
		// If it's been more than 10 hours since last update, refresh.
		if (System.currentTimeMillis() - 36000000 > lastUpdate) {
			final BroadcastReceiver bcRcvr = WifiUtils.ensureConnection(this, new OnConnectionListener() {
				@Override
				public void onConnection() {
					if (!isRunning()) {
						updateHeartbeat("onResume");
					}
					// Check for Update
					Utils.checkForUpdates(MainActivity.this, getString(R.string.app_name));

					getPOList();
				}

			});
			if (bcRcvr != null) {
				rcvrs.add(bcRcvr);
			}
		}
		else {
			final BroadcastReceiver bcRcvr = WifiUtils.ensureConnection(this, new OnConnectionListener() {
				@Override
				public void onConnection() {
					updateHeartbeat("onResume");
				}

			});
			if (bcRcvr != null) {
				rcvrs.add(bcRcvr);
			}
		}
		Utils.hideKeyboard(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		final BroadcastReceiver bcRcvr = WifiUtils.ensureConnection(this, new OnConnectionListener() {
			@Override
			public void onConnection() {
				updateHeartbeat("onPause");
			}

		});
		if (bcRcvr != null) {
			rcvrs.add(bcRcvr);
		}
	}

	@Override
	protected void onStop() {
		super.onStop();

		// Save sort by preference
		final Editor prefEditor = sharedPref.edit();
		prefEditor.putInt(getString(R.string.sort_by), spnSortBy.getSelectedItemPosition());
		prefEditor.apply();

		// Remove any receivers
		if (rcvrs.size() > 0) {
			for (BroadcastReceiver curRcvr : rcvrs) {
				unregisterReceiver(curRcvr);
			}
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		if (progressCircle != null && progressCircle.isShowing()) {
			progressCircle.hide();
			progressCircle.dismiss();
			progressCircle = null;
		}
	}

	/**
	 * Returns progressCircle
	 *
	 * @return the progressCircle
	 */
	public synchronized ProgressDialog getProgressCircle() {
		return progressCircle;
	}

	@Override
	protected void onActivityResult(int inRequestCode, int inResultCode, Intent inData) {
		super.onActivityResult(inRequestCode, inResultCode, inData);
		switch (inRequestCode) {
			case REQ_PREFERENCE:
				// Preferences page
				if (inData != null && inData.getBooleanExtra(SettingsActivity.EXTRA_WASCHANGED, false)) {
					getPOList();
				}
				break;
			case REQ_RECEIVE:
				// Receiving
				if (inResultCode == RESULT_OK && inData != null) {
					// putExtras() serializes, so it is a different object. Need
					// to update the original.
					final PORecord rcvd = data.get(data.indexOf((PORecord) inData.getExtras().get(PO_REC)));
					System.out.println("Received:: " + rcvd.getPoNum());

					rcvd.updateRecord(new HttpQueryListener<ArrayList>() {

						@Override
						public void queryComplete(ArrayList resp) {
							final ArrayList<String> rcvdItems = rcvd.getItemNum();
							final int rcvdPos = listAdapter.getPosition(rcvd);

							MainActivity.this.runOnUiThread(new Runnable() {

								@Override
								public void run() {
									if (rcvdItems.size() <= 0) {
										listAdapter.notifyItemRemoved(rcvdPos);
										data.remove(rcvdPos);
									}
									else {
										listAdapter.notifyItemChanged(rcvdPos);
									}

								}

							});
						}

					});
					rcvd.clearItemList();
				}
				break;
		}
	}

	@Override
	public void onClick(final View inV) {
		if (!processingClick) {
			processingClick = true;
			System.out.println("onClick! " + ((POHolder) inV.getTag()).getCurRec().hashCode());
			final PORecord rec = ((POHolder) inV.getTag()).getCurRec();

			Intent intent = new Intent();

			intent.setClass(this, RcvActivity.class);
			// Avoid multiple clicks/calls
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			intent.putExtra(PO_REC, rec);

			ActivityOptions opts = ActivityOptions.makeSceneTransitionAnimation(this, (View) inV.getParent().getParent(), "ponum");

			// if (!rec.isLoaded() && ll != null) {
			// ll.scheduleRcvActivity(this, intent, rec);
			// }
			// else {
			startActivityForResult(intent, REQ_RECEIVE, opts.toBundle());
			// }
			processingClick = false;
		}
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);

		//Set the list adapter for the recyclerView.
		poList.setAdapter(listAdapter);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is
		// present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
		sortPOList();
	}

	@Override
	public void onNothingSelected(AdapterView<?> inArg0) {
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem inItem) {
		switch (inItem.getItemId()) {
			case R.id.action_refresh:
				refreshPOList();
				break;
			case R.id.action_settings:
				// Display preferences activity
				Intent intent = new Intent(this, SettingsActivity.class);
				intent.putExtra("prefId", R.xml.preferences);
				startActivityForResult(intent, REQ_PREFERENCE);
				break;
			case R.id.action_nonpo:
				// Display non po receipts
				new NonPODialog(this);
		}

		return true;
	}

	@Override
	public void queryComplete(ArrayList inResp) {
		if (inResp == null) {
			Toast.makeText(this.getApplicationContext(), R.string.timeoutToast, Toast.LENGTH_LONG).show();
			inResp = new ArrayList();
		}
		String curResponse = (String) inResp.get(0);
		if (curResponse.equals("heartbeat_response")){
			String resp_code = (String) inResp.get(1);
			if (resp_code.equals("0") && !isPOST()){
				setPOST(true);
				Toast.makeText(this.getApplicationContext(), R.string.postToast, Toast.LENGTH_LONG).show();
			}else if (isPOST() && resp_code.equals("1")){
				setPOST(false);
			}
			inResp.clear();
		}
		if (!isPOST() && !curResponse.equals("heartbeat_response")) {

			data = new ArrayList<PORecord>(inResp.size() / 7);
			// list to populate the lines later
			final ArrayList<String> poPopList = new ArrayList<String>(data.size());
			final int respSize = inResp.size();
			for (int i = 0; i + 6 < respSize; i++) {
				final String poNum = inResp.get(i++) + "";
				data.add(new PORecord(poNum, inResp.get(i++) + "", inResp.get(i++) + "", Integer.parseInt(inResp.get(i++) + ""), Integer
						.parseInt(inResp.get(i++) + ""), inResp.get(i++) + "", inResp.get(i) + ""));
				poPopList.add(poNum);
			}
			listAdapter = new POAdapter(this, R.layout.porec_item, data);
			sortPOList();
			poList.setAdapter(listAdapter);
			inResp.clear();
		}
		if (progressCircle != null && progressCircle.isShowing()) {
			progressCircle.hide();
			progressCircle.dismiss();
			progressCircle = null;
		}
		if (curSwipeRefresh != null && curSwipeRefresh.isRefreshing()) {
			curSwipeRefresh.setRefreshing(false);
			curSwipeRefresh = null;
		}
	}

    /**
     * Gets the polist using preferences stored.
     */
    public void getPOList() {
        updatePreferences();

        // Update current day, should be unnecessary, unless the program is
        // left open overnight.
        curDay = Time.getJulianDay(System.currentTimeMillis(), 0);

        getPOList(curDay - pref_PastDays, curDay + pref_FutureDays, pref_OnlyBalDue);
    }

    /**
     * Gets the po list using the given parameters
     *
     * @param inDateFrom
     *             Julian day range start for po lines requested date
     * @param inDateThru
     *             Julian day range end for po lines requested date
     * @param inOnlyBalDue
     *             If true, only returns orders with a line that has a balance
     *             due.
     */
    private void getPOList(final int inDateFrom, final int inDateThru, final boolean inOnlyBalDue) {
        // Show Progress Circle
        if (curSwipeRefresh == null) progressCircle = ProgressDialog.show(this, getString(R.string.appTitle), getString(R.string.loadingPOs), true,
                false);
        if (!isPOST()) {
            ArrayList<String> parameters = new ArrayList<String>();
            parameters.add(HttpQuery.CMD_QUERY);
            parameters.add(HttpQuery.QUERY_POLIST);
            parameters.add(HttpQuery.PARAM_STATUS);
            parameters.add("4"); // Only get status 4 and less
            parameters.add(HttpQuery.PARAM_DATEFROM);
            parameters.add(inDateFrom + "");
            parameters.add(HttpQuery.PARAM_DATETHRU);
            parameters.add(inDateThru + "");
            // parameters.add(HttpQuery.PARAM_VENDOR);
            // parameters.add(inVendor);
            parameters.add(HttpQuery.PARAM_BALDUE);
            parameters.add(inOnlyBalDue ? "1" : "0");
            // if (inOnlyBalDue) parameters.add("1");
            // else parameters.add("0");

            HttpQuery.makeRequest(this, HttpQuery.STND_URL + "AndroidServlet", parameters);
            lastUpdate = System.currentTimeMillis();
        }
        else{
            Toast.makeText(this.getApplicationContext(), R.string.postToast, Toast.LENGTH_LONG).show();
            progressCircle.dismiss();
        }
    }

	/**
	 * Refreshes the PO List, pulling new data from the server and displaying
	 * it appropriately.
	 */
	private void refreshPOList() {
		// clear filter controls
		((EditText) findViewById(R.id.vendFilter)).setText("");
		((EditText) findViewById(R.id.poFilter)).setText("");

		// Download POs again.
		final BroadcastReceiver bcRcvr = WifiUtils.ensureConnection(this, new OnConnectionListener() {
			@Override
			public void onConnection() {
				updateHeartbeat("onResume");
				getPOList();
			}

		});
		if (bcRcvr != null) {
			rcvrs.add(bcRcvr);
		}
	}

	/**
	 * Sets progressCircle
	 *
	 * @param inProgressCircle
	 *             the progressCircle to set
	 */
	public synchronized void setProgressCircle(ProgressDialog inProgressCircle) {
		progressCircle = inProgressCircle;
	}

	private void sortPOList() {
		if (listAdapter == null) return;

		if (spnSortBy.getSelectedItemPosition() == 0) {
			listAdapter.sort(new PORecComparator(true));
		}
		else listAdapter.sort(new PORecComparator(false));

		poList.setAdapter(listAdapter);
	}

	/**
	 * Updates the preferences from the preference manager.
	 */
	protected void updatePreferences() {
		pref_PastDays = Integer.parseInt(sharedPref.getString(getString(R.string.pref_PastDays), "30"));
		pref_FutureDays = Integer.parseInt(sharedPref.getString(getString(R.string.pref_FutureDays), "15"));
		pref_OnlyBalDue = sharedPref.getBoolean(getString(R.string.pref_OnlyBalDue), true);
	}
}
