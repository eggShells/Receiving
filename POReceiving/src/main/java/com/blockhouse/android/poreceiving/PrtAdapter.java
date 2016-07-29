package com.blockhouse.android.poreceiving;

import java.util.ArrayList;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;


public class PrtAdapter extends RecyclerView.Adapter<PrtHolder> implements TextWatcher, OnClickListener {

	private ArrayList<PrtRecord> prtRecords;
     final int layoutResourceId;
     private final Context context;
     private double targetItmQty = 1;
     
     private final String lineNum;
     
     public PrtAdapter(Context inContext, int inLayoutResourceId, final RcvRecord inRec) {
     	super();
     	
     	context = inContext;
     	layoutResourceId = inLayoutResourceId;
     	lineNum = inRec.getLineNum();
     	
//     	System.out.println("Adapter constructor:: inserting:: " + inRec.getLabels());
     	final ArrayList<PrtRecord> newLabels = inRec.getLabels();
     	prtRecords = newLabels;
     	if (newLabels.size() > 0) notifyItemRangeInserted(0, newLabels.size());
//     	System.out.println("Adapter constructor:: done:: " + inRec.getLabels() + " / " + getItemCount());
     	
     }
     
     /**
      * Adds a record to this adapter and notifies the RecyclerView.
      * @param quantity Item quantity for label.
      */
     private void addPrtRecord(final double quantity) {
     	prtRecords.add(new PrtRecord(1, quantity));
     	notifyItemInserted(prtRecords.size() - 1);
     }
     
     private void calcQuantities() {
     	double curItmQty = getTotalItems();
     	final int prtSize = prtRecords.size();
     	
     	if (curItmQty == targetItmQty) return;
     	if (curItmQty < targetItmQty) {
     		if (prtSize >= 1) {
     			PrtRecord lastRec = prtRecords.get(prtSize - 1);
     			if (lastRec.getLabelQty() == 1) {
     				lastRec.setItemQty(lastRec.getItemQty() + (targetItmQty - curItmQty));
     				super.notifyItemChanged(prtSize - 1);
     				curItmQty = targetItmQty;
     			}
     		}
     		
     		if (curItmQty != targetItmQty) {
     			addPrtRecord(targetItmQty - curItmQty);
     		}
     	}
     }
     
     /**
      * Returns the record associated with the given position.
      * @param pos Position in the RecyclerView
      * @return Corresponding record
      */
     public PrtRecord getItem(final int pos) {
     	return prtRecords.get(pos);
     }
	
	@Override
     public int getItemCount() {
	     return prtRecords.size();
     }
	
	private double getTotalItems() {
		int ret = 0;
		for (int i = 0; i < prtRecords.size(); i++) {
     		PrtRecord curRec = prtRecords.get(i);
     		ret += curRec.getItemQty() * curRec.getLabelQty();
     	}
		return ret;
	}
	
	/**
	 * Removes the record/item at the given index from the underlying data structure and updates
	 * the RecyclerView display.
	 * @param pos Position to remove.
	 * @return Record removed.
	 */
	public PrtRecord remove(final int pos) {
		PrtRecord ret = prtRecords.remove(pos);
		notifyItemRemoved(pos);
//		super.notifyItemRangeChanged(pos, getItemCount());
		System.out.println("Removed! " + prtRecords);
		return ret;
	}

	@Override
     public void onBindViewHolder(PrtHolder holdMe, int pos) {
	     holdMe.bindPrt(prtRecords.get(pos));
     }

	@Override
     public PrtHolder onCreateViewHolder(final ViewGroup parent, final int pos) {
		return new PrtHolder(context, LayoutInflater.from(parent.getContext()).inflate(layoutResourceId, parent, false));
     }

	@Override
     public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

	@Override
     public void onTextChanged(CharSequence s, int start, int before, int count) {
		if (s == null || s.length() <= 0) return;
		
		targetItmQty = Double.parseDouble(s.toString());
		calcQuantities();
		System.out.println("PrtAdapter:: " + lineNum + " - " + targetItmQty);
     }

	@Override
     public void afterTextChanged(Editable s) {}

	@Override
     public void onClick(View v) {
	     addPrtRecord(1);
     }

}
