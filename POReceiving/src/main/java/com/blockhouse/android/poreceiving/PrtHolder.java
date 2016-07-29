package com.blockhouse.android.poreceiving;

import android.content.Context;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;

public class PrtHolder extends ViewHolder {

	private final EditText prtQty;
	private final EditText itmQty;
	
	private View myView;
	
	private PrtRecord curRec = null;
//	private final OnFocusChangeListener itemWatcher;
	
	public PrtHolder(final Context context, final View inItemView) {
		super(inItemView);
		
		prtQty = (EditText) inItemView.findViewById(R.id.prtQty);
		prtQty.addTextChangedListener(new TextWatcher() {
			@Override
               public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
			@Override
               public void onTextChanged(CharSequence s, int start, int before, int count) {
				if (curRec == null) return;
				if (s != null && s.length() > 0) {
					setItemQty(Integer.parseInt(s.toString()));
				}
				else {
					curRec.setLabelQty(0);
				}
               }
			@Override
               public void afterTextChanged(Editable s) {}
			
		});
		
		itmQty = (EditText) inItemView.findViewById(R.id.itmQty);
		itmQty.addTextChangedListener(new TextWatcher() {

			@Override
               public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

			@Override
               public void onTextChanged(CharSequence s, int start, int before, int count) {
				if (curRec == null) return;
	               if (s != null && s.length() > 0) {
	               	curRec.setItemQty(Double.parseDouble(s.toString()));
	               }
	               else curRec.setItemQty(0);
               }

			@Override
               public void afterTextChanged(Editable s) {}
			
		});
//		itmQty.setOnFocusChangeListener(inItemWatcher);
		
		((Button)inItemView.findViewById(R.id.prtMinus)).setOnClickListener(new OnClickListener() {

			@Override
               public void onClick(View v) {
	               prtQty.setText(Math.max(Integer.parseInt(prtQty.getText().toString()) - 1, 0) + "");
               }
		});
		
		((Button)inItemView.findViewById(R.id.prtPlus)).setOnClickListener(new OnClickListener() {

			@Override
               public void onClick(View v) {
				prtQty.setText((Integer.parseInt(prtQty.getText().toString()) + 1) + "");
               }
			
		});
		
		myView = inItemView;
//		itemWatcher = inItemWatcher;
     }

	public void bindPrt(final PrtRecord inRec) {
//		System.out.println("bindPrt:: " + inRec);
		prtQty.setText(inRec.getLabelQty() + "");
		itmQty.setText(inRec.getItemQty() + "");
		
		curRec = inRec;
	}
	
	private void setItemQty(int inLabelQty) {
		double totItemQty = curRec.getItemQty() * curRec.getLabelQty();
		
		curRec.setLabelQty(inLabelQty);
		
		if (curRec.getLabelQty() > 0) curRec.setItemQty((int)(totItemQty / curRec.getLabelQty()));
		
		itmQty.setText(curRec.getItemQty() + "");
//		itemWatcher.onFocusChange(itmQty, false);
	}
	
}
