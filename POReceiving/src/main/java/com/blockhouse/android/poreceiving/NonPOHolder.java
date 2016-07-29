package com.blockhouse.android.poreceiving;

import android.content.Context;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.text.format.Time;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class NonPOHolder extends ViewHolder {

	private TextView source;
	private TextView expectedDate;
	private TextView qty;
     private TextView desc;
     private Button receive;
     
     private NonPORecord curRec = null;
     
     private final Context context;
     private static final Time time = new Time();
     public static final String dateFormat = "%m/%d/%Y";
	
	public NonPOHolder(final Context inContext, final View itemView, final OnClickListener listener) {
	     super(itemView);
	     context = inContext;

          source = (TextView) itemView.findViewById(R.id.NonPOSource);
          expectedDate = (TextView) itemView.findViewById(R.id.NonPOExp);
          qty = (TextView) itemView.findViewById(R.id.NonPOQty);
          desc = (TextView) itemView.findViewById(R.id.NonPODesc);
          receive = (Button) itemView.findViewById(R.id.NonPORemove);
          
          receive.setOnClickListener(listener);
//          curButton.setTag(position);
     }

	public void bindNonPO(final NonPORecord bindMe) {
		source.setText(context.getString(R.string.NonPO_Source) + " " + bindMe.getSource());
		time.setJulianDay(bindMe.getExpDate());
		expectedDate.setText(context.getString(R.string.NonPO_ExpDate) + " " + time.format(dateFormat));
		qty.setText(context.getString(R.string.NonPO_Qty) + " " + bindMe.getQty());
		final String descStr = bindMe.getDesc();
		desc.setText(descStr);

		int lineGuess = 0;

		for (int i = 0; i < descStr.length(); i++) {
			if (descStr.charAt(i) == '\n') lineGuess++;
		}
		lineGuess = Math.max(lineGuess, descStr.length() / 35);
		
		receive.setTag(bindMe);

		super.itemView.getLayoutParams().height = Math.max(lineGuess * 50, 200);
		curRec = bindMe;
	}
	
	public NonPORecord getCurRec() {
		return curRec;
	}
}
