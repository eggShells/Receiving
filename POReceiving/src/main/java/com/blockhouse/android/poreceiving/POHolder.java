/**
 * 
 */
package com.blockhouse.android.poreceiving;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.blockhouse.android.utils.HttpQuery;
import com.blockhouse.android.utils.HttpQueryListener;

import android.content.Context;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Trace;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.text.format.Time;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

/**
 * @author mbarr
 *         Date Created: Feb 5, 2015
 * 
 */
public class POHolder extends ViewHolder implements HttpQueryListener<ArrayList> {

	private static final Time time = new Time();
	public static final String dateFormat = "%m/%d/%Y";
	private static final NumberFormat numberFormat = NumberFormat.getInstance();
	private final Resources res;
	private final View myView;
	private final Context context;
	private final MainActivity mainActivity;

	private final TextView txtPONum;
	private final TextView txtVend;
	private final TextView txtVendName;
	private final TextView txtOrdDate;
	private final TextView txtOrdBy;
	private final TextView txtReqDate;
	private final TextView txtReqBy;
	private final ListView lstItems;
	private final Button rcvBtn;
	private final TextView txtPendTempRcv;
	
	private PORecord curRec = null;
	AsyncTask myCurUpdate = null;

	/**
	 * @param inItemView
	 */
	public POHolder(final MainActivity inMainActivity, final View inItemView) {
		super(inItemView);
		mainActivity = inMainActivity;

		txtPONum = (TextView) inItemView.findViewById(R.id.RcvPONum);
		txtVend = (TextView) inItemView.findViewById(R.id.POVend);
		txtVendName = (TextView) inItemView.findViewById(R.id.VendName);
		txtOrdBy = (TextView) inItemView.findViewById(R.id.OrdBy);
		txtOrdDate = (TextView) inItemView.findViewById(R.id.OrdDate);
		txtReqDate = (TextView) inItemView.findViewById(R.id.ReqDate);
		txtReqBy = (TextView) inItemView.findViewById(R.id.ReqBy);
		lstItems = (ListView) inItemView.findViewById(R.id.itemList);
		rcvBtn = (Button) inItemView.findViewById(R.id.RcvBtn);
		txtPendTempRcv = (TextView) inItemView.findViewById(R.id.pendTempRcv);

		myView = inItemView;
		context = inItemView.getContext();
		res = context.getResources();
	}

	public void bindPO(PORecord bindMe) {
        Log.d("POHolder/bindPO", bindMe.toString());
        txtPONum.setText(bindMe.getPoNum());
		txtVend.setText(bindMe.getVendId());
		txtVendName.setText(bindMe.getVendName());

		time.setJulianDay(bindMe.getOrdDate());
		txtOrdDate.setText(res.getString(R.string.order_date) + " "
				+ time.format(dateFormat));
		txtOrdBy.setText(res.getString(R.string.order_by) + " "
				+ bindMe.getOrdBy());

		time.setJulianDay(bindMe.getReqDate());
		txtReqDate.setText(res.getString(R.string.req_date) + " "
				+ time.format(dateFormat));

		txtReqBy.setText(res.getString(R.string.req_by) + " "
				+ bindMe.getReqBy());
		
		if (bindMe.isLoaded()) {
			showPOLines(bindMe);
		}
		else {
			rcvBtn.setEnabled(false);
			lstItems.setAdapter(null);
			txtPendTempRcv.setVisibility(View.INVISIBLE);
			
			if (myCurUpdate != null) myCurUpdate.cancel(true);
			myCurUpdate = bindMe.updateRecord(this);
	          
			myView.getLayoutParams().height = 275;
		}
		
		curRec = bindMe;
	}
	
	public PORecord getCurRec() {
		return curRec;
	}
	
	private void showPOLines(PORecord showMe) {
		if (!showMe.isLoaded()) return;
		
		// Prepare Item Data
		final ArrayList<String> itemNums = showMe.getItemNum();
		final ArrayList<String> descs = showMe.getDescs();
		final ArrayList<String> descsTwo = showMe.getDescTwo();
		final ArrayList<Double> ordQtys = showMe.getOrdQty();
		final ArrayList<Double> pstRcvQty = showMe.getPstRcvQty();
		final ArrayList<Double> curRcvQty = showMe.getCurRcvQty();

		double totalTempRcv = 0;
		for (double cur : curRcvQty) {
			totalTempRcv += cur;
		}
		txtPendTempRcv.setVisibility(totalTempRcv > 0 ? View.VISIBLE : View.INVISIBLE);

		if (itemNums.size() == 0) {
			// Loaded, blank PO.
			rcvBtn.setEnabled(false);
		}
		else {
			rcvBtn.setEnabled(true);
			rcvBtn.setOnClickListener(mainActivity);
			rcvBtn.setTag(this);
		}

		final List<Map<String, String>> itemData = new ArrayList<Map<String, String>>(itemNums.size());
		for (int i = 0; i < itemNums.size(); i++) {
			Map<String, String> curLineData = new HashMap<String, String>(2);

			curLineData.put("item", itemNums.get(i).trim() + " \t\t\tBal Due: " + numberFormat.format(ordQtys.get(i) - pstRcvQty.get(i) - curRcvQty.get(i)));
//			curLineData.put("desc", "Ord: " + ordQtys.get(i) + " Posted: " + pstRcvQty.get(i) + " Cur: " + curRcvQty.get(i));
			curLineData.put("desc", descs.get(i) + "\n" + descsTwo.get(i));

			itemData.add(curLineData);
		}

		lstItems.setAdapter(new SimpleAdapter(context, itemData, android.R.layout.simple_list_item_2, new String[] { "item", "desc" }, new int[] {
		          android.R.id.text1, android.R.id.text2 }));
		
		int itemListHeight = lstItems.getCount() * 150;
		myView.getLayoutParams().height = Math.max(itemListHeight, 275);
	}

	@Override
	public void queryComplete(ArrayList inResp) {
		// Query from an update record request.
		synchronized (this) {
			myCurUpdate = null;
			showPOLines(curRec);
			myView.requestLayout();
		}
	}

}
