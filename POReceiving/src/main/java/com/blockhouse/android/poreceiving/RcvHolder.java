package com.blockhouse.android.poreceiving;

import java.util.ArrayList;
import java.util.HashMap;

import com.blockhouse.android.utils.gui.recyclerview.RecyclerSwipeDismissTouchListener;
import com.blockhouse.android.utils.gui.recyclerview.SlideInRightAnimator;
import com.blockhouse.android.utils.gui.recyclerview.RecyclerSwipeDismissTouchListener.DismissCallbacks;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.Time;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class RcvHolder extends ViewHolder {

	private Context context;
	private RcvRecord curRec;

	private TextView itemNum;
	private TextView ordQty;
	private EditText packListQty;
	private EditText rcvQty;
	private TextView descs;
	private TextView rcptDueDate;
	private EditText note;
	private TextView pendRcpt;
	private TextView lblRcvQty;
	private RecyclerView prtList;
	private Button addLbl;

	private static final Time time = new Time();
	private final HashMap<String, String> listUpdate;
	private final ArrayList<RcvTextWatcher> watchers = new ArrayList<RcvTextWatcher>(4);
	
	private PrtAdapter prtAdapter = null;

	public RcvHolder(final Context inContext, final View inRcvView, final HashMap<String, String> inListUpdate) {
		super(inRcvView);
		context = inContext;
		listUpdate = inListUpdate;

		itemNum = (TextView) inRcvView.findViewById(R.id.RcvItemNum);
		ordQty = (TextView) inRcvView.findViewById(R.id.rcvQtyOrd);
		packListQty = (EditText) inRcvView.findViewById(R.id.RcvItmPackListQty);
		rcvQty = (EditText) inRcvView.findViewById(R.id.RcvItmQty);
		rcptDueDate = (TextView) inRcvView.findViewById(R.id.rcvItmReqDate);
		note = (EditText) inRcvView.findViewById(R.id.rcvItmNote);
		descs = (TextView) inRcvView.findViewById(R.id.rcvItemDescsList);
		pendRcpt = (TextView) inRcvView.findViewById(R.id.linePendingRcpt);
		lblRcvQty = (TextView) inRcvView.findViewById(R.id.lblRcvQty);
		prtList = (RecyclerView) inRcvView.findViewById(R.id.lblQtyList);
		prtList.setLayoutManager(new LinearLayoutManager(prtList.getContext()));
		prtList.setItemAnimator(new SlideInRightAnimator());
		addLbl = (Button) inRcvView.findViewById(R.id.addLblBtn);

		for (int i = 0; i < 4; i++)
			watchers.add(null);
	}

	public void bindRcvRec(final RcvRecord bindMe, final int inPosition) {
		System.out.println("Binding:: " + inPosition);
		clearTextWatchers();

		itemNum.setText(bindMe.getItemNum());
		ordQty.setText(bindMe.getOrdQty() + "");
		synchronized (this) {
			System.out.println("Update:: " + inPosition + " = "  + listUpdate.get(inPosition + RcvAdapter.LU_RCVQTY));
			packListQty.setText(listUpdate.get(inPosition + RcvAdapter.LU_PACKLISTQTY));
			
			final String curRcv = listUpdate.get(inPosition + RcvAdapter.LU_RCVQTY).trim();
			if (!curRcv.isEmpty() && Double.parseDouble(curRcv) > 0) rcvQty.setText(curRcv);
			else rcvQty.setText("");
			
			note.setText(listUpdate.get(inPosition + RcvAdapter.LU_NOTE));
		}

		String desc = "";
		for (String curDesc : bindMe.getDescs()) {
			desc += curDesc + "\n";
		}
		descs.setText(desc);

		if (bindMe.getReqDate() > 0) {
			time.setJulianDay(bindMe.getReqDate());
			rcptDueDate.setText(context.getResources().getString(R.string.rcptDueBy) + ":\n\t" + time.format(POHolder.dateFormat));
		}
		if (bindMe.getCurRcvQty() > 0) {
			pendRcpt.setText(context.getString(R.string.linePendTempRcv) + " " + bindMe.getCurRcvQty());
			pendRcpt.setVisibility(View.VISIBLE);
			lblRcvQty.setTextColor(context.getResources().getColor(R.color.bh_red));
		}
		else {
			pendRcpt.setVisibility(View.INVISIBLE);
			lblRcvQty.setTextAppearance(context, android.R.attr.textAppearanceMedium);
			lblRcvQty.setTypeface(null, Typeface.BOLD);
		}
		
		
		initRecyclerView(bindMe);
		addLbl.setOnClickListener(prtAdapter);

		int itemListHeight = 25 + bindMe.getDescs().size() * 25;
		super.itemView.getLayoutParams().height = Math.max(itemListHeight, 500);

		curRec = bindMe;

		setNewTextWatcher(0, inPosition, packListQty);
		setNewTextWatcher(1, inPosition, rcvQty);
		setNewTextWatcher(2, inPosition, note);

		note.setTag(inPosition);

	}
	
	private void initRecyclerView(final RcvRecord bindMe) {	
		prtAdapter = new PrtAdapter(context, R.layout.prt_item, bindMe);
		prtList.setAdapter(prtAdapter);
		rcvQty.addTextChangedListener(prtAdapter);
		
		RecyclerSwipeDismissTouchListener touchListener = new RecyclerSwipeDismissTouchListener(prtList,
		          new DismissCallbacks() {
			          public void onDismiss(RecyclerView listView, int[] reverseSortedPositions) {
				          for (int position : reverseSortedPositions) {
				          	prtAdapter.remove(position);
				          }
//				          prtAdapter.notifyDataSetChanged();
			          }

					@Override
                         public boolean canDismiss(int position) {
	                         return true;
                         }
		          });
		prtList.setOnTouchListener(touchListener);
		prtList.setOnScrollListener(touchListener.makeScrollListener());
	}
	
	/**
	 * Clear all text watchers from widgets.
	 */
	private void clearTextWatchers() {
		packListQty.removeTextChangedListener(watchers.get(0));
		rcvQty.removeTextChangedListener(watchers.get(1));
		if (prtAdapter!= null) rcvQty.removeTextChangedListener(prtAdapter);
		note.removeTextChangedListener(watchers.get(2));
	}

	/**
	 * Set a new text watcher on the given widget.
	 * @param index Index of the watcher, 0 = packListQty, 1 = rcvQty, 2 = note
	 * @param inPosition Position of the holder in the adapter
	 * @param watchMe Widget to watch
	 */
	private void setNewTextWatcher(final int index, final int inPosition, final EditText watchMe) {
		String newKeyStr = inPosition + "";
		switch (index) {
		case 0:
			newKeyStr += RcvAdapter.LU_PACKLISTQTY;
			break;
		case 1:
			newKeyStr += RcvAdapter.LU_RCVQTY;
			break;
		case 2:
			newKeyStr += RcvAdapter.LU_NOTE;
			break;
		case 3:
			newKeyStr += RcvAdapter.LU_LBLCOUNT;
		}

		if (watchers.get(index) != null) {
			watchers.get(index).setKeyString(newKeyStr);
		}
		else {
			watchers.set(index, new RcvTextWatcher(newKeyStr));
		}
		watchMe.addTextChangedListener(watchers.get(index));
	}

	public RcvRecord getCurRec() {
		return curRec;
	}

	/**
	 * Sets all EditText's focus change listeners to the given listener.
	 * 
	 * @param listener
	 *             Listener for all edit texts.
	 */
	public void setLastEditTextFocusChangeListener(OnFocusChangeListener listener) {
		// packListQty.setOnFocusChangeListener(listener);
		// rcvQty.setOnFocusChangeListener(listener);
		note.setOnFocusChangeListener(listener);
		// lblCount.setOnFocusChangeListener(listener);
	}

	private class RcvTextWatcher implements TextWatcher {

		private String keyStr;

		public RcvTextWatcher(String inKeyStr) {
			keyStr = inKeyStr;
		}

		@Override
		public void afterTextChanged(Editable inS) {
			synchronized (RcvHolder.class) {
				System.out.println("puting " + inS.toString() + " in " + keyStr);
				listUpdate.put(keyStr, inS.toString());
			}
		}

		@Override
		public void beforeTextChanged(CharSequence inS, int inStart, int inCount, int inAfter) {
		}

		@Override
		public void onTextChanged(CharSequence inS, int inStart, int inBefore, int inCount) {
		}

		public void setKeyString(final String inKeyStr) {
			keyStr = inKeyStr;
		}
	}
}
