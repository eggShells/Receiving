package com.blockhouse.android.poreceiving;

import java.util.ArrayList;
import java.util.HashMap;

import com.blockhouse.android.utils.AwareActivity;
import com.blockhouse.android.utils.HttpQuery;
import com.blockhouse.android.utils.HttpQueryListener;
import com.google.gson.Gson;

import android.os.Bundle;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.Time;

public class RcvActivity extends AwareActivity implements OnClickListener, HttpQueryListener<ArrayList>, android.content.DialogInterface.OnClickListener, OnFocusChangeListener {

	private static final Time time = new Time();

	private static RcvLoader rl = null;

	private PORecord poRecord = null;
	private RcvAdapter rcvAdapter = null;

	private ArrayList<RcvRecord> data = null;

	private HashMap<String, String> listUpdateValues = null;
	private RecyclerView rcvList = null;
	private ProgressDialog progressDisplay = null;
	private String lastRcvLine = null;

	private int rcvLines = 0;
	private int successLines = 0;

	/**
	 * Sets up the action bar.
	 */
	private void configureActionBar() {
		ActionBar ab = getActionBar();
		ab.hide();

		ab.setTitle(R.string.appTitle);
		ab.setSubtitle(R.string.rcvActivityTitle);

		ab.show();
	}

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rcv);

        poRecord = (PORecord) getIntent().getExtras().get(MainActivity.PO_REC);
        Log.d("RcvActivity/onCreate:",getIntent().getExtras().toString());
//		System.out.println("RcvActivity onCreate rec: " + poRecord.hashCode());
        populateHeader(poRecord);

        rcvAdapter = new RcvAdapter(this, R.layout.rcv_item, poRecord.getRcvRecords(), this);
        rcvList = ((RecyclerView) findViewById(R.id.rcvItemsList));
        rcvList.setLayoutManager(new LinearLayoutManager(this));
//		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);
//		rcvList.setItemViewCacheSize(rcvAdapter.getItemCount());
        rcvList.setAdapter(rcvAdapter);

        data = rcvAdapter.getData();
        listUpdateValues = rcvAdapter.getCurrentListValues();
        Log.d("TAG",listUpdateValues.toString());

        ((Button) findViewById(R.id.rcvComplete)).setOnClickListener(this);

        if (rl != null) {
            rl.stopDownload();
        }
        rl = new RcvLoader(rcvAdapter.getData(), rcvAdapter, poRecord.getPoNum());
        rl.execute();

        // set result as failed, until successful rcpt.
        setResult(RESULT_CANCELED);

        // Show the Up button in the action bar.
        setupActionBar();
        //Define the rest of the action bar
        configureActionBar();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is
        // present.
        getMenuInflater().inflate(R.menu.rcv, menu);
        return true;
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (!hasFocus) {
//	     	System.out.println("Scroll to : " + (((Integer)v.getTag()) + 1));
//	     	rcvList.scrollToPosition(((Integer)v.getTag()) + 1);
            rcvList.smoothScrollToPosition(((Integer)v.getTag()) + 1);
        }
    }

	@Override
	public void onClick(DialogInterface inArg0, int buttonPressed) {
		if (buttonPressed == AlertDialog.BUTTON_NEGATIVE) return;
		// Approved receiving
		progressDisplay = ProgressDialog.show(this, getString(R.string.appTitle), getString(R.string.rcv_po), true, false);
		final String poNum = poRecord.getPoNum();
		String lineNum = null;

		final int listCount = rcvAdapter.getItemCount();
        rl = new RcvLoader(rcvAdapter.getData(), rcvAdapter, poRecord.getPoNum());
        rl.execute();
        Log.d("onClick",listUpdateValues.toString());
		listUpdateValues = rcvAdapter.getCurrentListValues();
        Log.d("onClick",listUpdateValues.toString());

		rcvLines = 0;
		successLines = 0;
		for (int i = 0; i < listCount; i++) {

			final String rcvQty = listUpdateValues.get(i + RcvAdapter.LU_RCVQTY);
			final double origRcvQty = Double.parseDouble(listUpdateValues.get(i + RcvAdapter.LU_ORIGQTY));
			final double packListQty = Double.parseDouble(listUpdateValues.get(i + RcvAdapter.LU_PACKLISTQTY));
			final double packListOrig = Double.parseDouble(listUpdateValues.get(i + RcvAdapter.LU_PACKLISTORIG));

			// If something is received...
			if ((rcvQty != null && !rcvQty.isEmpty() && Double.parseDouble(rcvQty) != origRcvQty) || (packListQty != packListOrig)) {
				lineNum = listUpdateValues.get(i + RcvAdapter.LU_LINENUM);
				final String note = listUpdateValues.get(i + RcvAdapter.LU_NOTE);
//				String lblCount = listUpdateValues.get(i + RcvAdapter.LU_LBLCOUNT);
//				if (lblCount.isEmpty()) lblCount = "0";

				ArrayList<String> parameters = new ArrayList<String>(10);
				parameters.add(HttpQuery.CMD_POST);
				parameters.add(HttpQuery.POST_RCVPO);
				parameters.add(HttpQuery.PARAM_PONUM);
				parameters.add(poNum);
				parameters.add(HttpQuery.PARAM_LINENUM);
				parameters.add(lineNum);
				parameters.add(HttpQuery.PARAM_PACKLISTQTY);
				parameters.add(packListQty + "");
				parameters.add(HttpQuery.PARAM_RCVQTY);
				parameters.add(rcvQty);
				if (!note.isEmpty()) {
					parameters.add(HttpQuery.PARAM_NOTE);
					parameters.add(note);
				}
				parameters.add(HttpQuery.PARAM_LBLCOUNT);
				parameters.add(new Gson().toJson(getLabelQtys(i), ArrayList.class));

				HttpQuery.makeRequest(this, HttpQuery.STND_URL + "AndroidServlet", parameters);
				rcvLines++;
			}
		}
		if (rcvLines == 0) {
			progressDisplay.hide();
			progressDisplay.dismiss();
			progressDisplay = null;

			Toast.makeText(getApplicationContext(), R.string.nothingRcvd, Toast.LENGTH_SHORT).show();

			finish();
		}
		else lastRcvLine = lineNum;
	}

	@Override
	public void onClick(View inV) {
		// Receive Complete
		if (rcvList != null) {

			AlertDialog rcvConfirm = new AlertDialog.Builder(this).create();
			rcvConfirm.setTitle(getString(R.string.Rcv_Confirm_Title));
			rcvConfirm.setMessage(getString(R.string.Rcv_Confirm));
			rcvConfirm.setCancelable(true);
			rcvConfirm.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.YES), this);
			rcvConfirm.setButton(AlertDialog.BUTTON_NEGATIVE, getString(R.string.NO), this);
			rcvConfirm.show();
		}
		else finish();
	}
	
	/**
	 * Flattens all label records from the RcvRecord with the given index into a single dimensional
	 * ArrayList to send to the servlet. Format is Label Qty, Item Qty, ...
	 * @param index Index of the RcvRecord in data.
	 * @return ArrayList of label and item qtys.
	 */
	private ArrayList<Double> getLabelQtys(final int index) {
		final RcvRecord curRec = data.get(index);
		final ArrayList<Double> ret = new ArrayList<Double>();
		
		final ArrayList<PrtRecord> labels = curRec.getLabels();
		for (int i = 0; i < labels.size(); i++) {
			PrtRecord curPrt = labels.get(i);
			ret.add((double) curPrt.getLabelQty());
			ret.add(curPrt.getItemQty());
		}
		
		return ret;
	}

	@Override
	public void onConfigurationChanged(Configuration inNewConfig) {
		super.onConfigurationChanged(inNewConfig);
		setContentView(R.layout.activity_rcv);

		populateHeader(poRecord);

//		if (data != null) {
//			rcvAdapter = new RcvAdapter(this, R.layout.rcv_item, data, listUpdateValues, this);
			rcvList = ((RecyclerView) findViewById(R.id.rcvItemsList));
			rcvList.setAdapter(rcvAdapter);
			rcvList.setLayoutManager(new LinearLayoutManager(rcvList.getContext()));
//
//			data = rcvAdapter.getData();
//			listUpdateValues = rcvAdapter.getCurrentListValues();
//		}
		
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// This ID represents the Home or Up button. In the case of this
			// activity, the Up button is shown. Use NavUtils to allow users
			// to navigate up one level in the application structure. For
			// more details, see the Navigation pattern on Android Design:
			//
			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
			//
//			NavUtils.navigateUpFromSameTask(this);
			finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onPause() {
		super.onPause();

	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	/**
	 * Populates screen from the given PORecord
	 * 
	 * @param inRecord
	 *             Record containing data to populate the screen.
	 */
	private void populateHeader(PORecord inRecord) {
		((TextView) findViewById(R.id.RcvPONum)).setText(inRecord.getPoNum());
		((TextView) findViewById(R.id.RcvVend)).setText(inRecord.getVendId() + " - " + inRecord.getVendName());
		time.setJulianDay(inRecord.getReqDate());
		((TextView) findViewById(R.id.RcvReqDate)).setText(getString(R.string.req_date) + " " + time.format(POHolder.dateFormat));
		((TextView) findViewById(R.id.RcvReqBy)).setText(getString(R.string.req_by) + " " + inRecord.getReqBy());
		time.setJulianDay(inRecord.getReqDate());
		((TextView) findViewById(R.id.RcvOrdDate)).setText(getString(R.string.order_date) + " " + time.format(POHolder.dateFormat));
		((TextView) findViewById(R.id.RcvOrdBy)).setText(getString(R.string.order_by) + " " + inRecord.getOrdBy());
	}

	@Override
	public void queryComplete(ArrayList inResp) {
//		System.out.println(inResp.size() + " - - " + lastRcvLine);
		if (progressDisplay != null && lastRcvLine != null) {

			if (inResp != null && inResp.size() == 2) {
				// successful receiving
				successLines++;
			}
			if (inResp == null || inResp.size() != 2 || inResp.get(1).toString().equals(lastRcvLine) || lastRcvLine.equals("-1")) {
				// Check for last receiving, or error.
				progressDisplay.hide();
				progressDisplay.dismiss();
				progressDisplay = null;

				// Check for total success
				if (inResp != null && inResp.size() > 1 && inResp.get(1).toString().equals(lastRcvLine) && successLines == rcvLines) {
					Toast.makeText(this.getApplicationContext(), R.string.success_rcv, Toast.LENGTH_LONG).show();

					Intent i = new Intent();
					i.putExtra(MainActivity.PO_REC, poRecord);

					setResult(RESULT_OK, i);
					finish();
				}
				else {
					// unsuccessful
					final String errorStr = (inResp != null && inResp.size() == 1) ? "\nError:: " + inResp.get(0) : "";
//					Toast.makeText(this.getApplicationContext(), R.string.failed_rcv, Toast.LENGTH_LONG).show();
					Toast.makeText(getApplicationContext(), getResources().getString(R.string.failed_rcv) + errorStr, Toast.LENGTH_LONG).show();
				}
			}
		}
	}
	/**
	 * Set up the {@link android.app.ActionBar}.
	 */
	private void setupActionBar() {

		getActionBar().setDisplayHomeAsUpEnabled(true);

	}

}
