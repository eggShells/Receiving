/**
 * 
 */
package com.blockhouse.android.poreceiving;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;

/**
 * @author mbarr
 *         Date Created: Jul 29, 2013
 * 
 */
public class RcvAdapter extends RecyclerView.Adapter<RcvHolder> {

     private final Context context;
     final int layoutResourceId;
     private final ArrayList<RcvRecord> data;
     private final HashMap<String, String> listUpdate;

//     private static final Time time = new Time();
     protected static final String LU_PACKLISTQTY = "plQty";
     protected static final String LU_RCVQTY = "rqty";
     protected static final String LU_NOTE = "note";
     protected static final String LU_LINENUM = "linenum";
     protected static final String LU_LBLCOUNT = "lblcount";
	protected static final String LU_ORIGQTY = "origRcv";
	protected static final String LU_PACKLISTORIG = "plorig";
     
     /**
      * @param inContext
      * @param inlayoutResourceId
      * @param inObjects
      */
     public RcvAdapter(Context inContext, int inlayoutResourceId, ArrayList<RcvRecord> inObjects, final OnFocusChangeListener inListener) {
//          super(inContext, inlayoutResourceId, inObjects);
          
          data = inObjects;
          context = inContext;
          layoutResourceId = inlayoutResourceId;
          listUpdate = new HashMap<String, String>();
          
          populateListUpdateValues();
     }
     
     /**
      * Recreation constructor, called to recreate.
      * @param inContext Context
      * @param inlayoutResourceId Resource ID
      * @param inObjects Receiving records
      * @param inUpdateValues List EditText update values
      */
     public RcvAdapter(Context inContext, int inlayoutResourceId, ArrayList<RcvRecord> inObjects, HashMap<String, String> inUpdateValues, final OnFocusChangeListener inListener) {
//          super(inContext, inlayoutResourceId, inObjects);
          
          data = inObjects;
          context = inContext;
          layoutResourceId = inlayoutResourceId;
          
          listUpdate = inUpdateValues;
     }
     
     public ArrayList<RcvRecord> getData() {
          return data;
     }

     /**
      * Returns the values currently held in the edit texts in the list view.
      * @return the listUpdate
      */
     public HashMap<String, String> getCurrentListValues() {
          return listUpdate;
     }
     
     private void populateListUpdateValues() {
          final int dataSize = data.size();
          for (int i = 0; i < dataSize; i++) {
               RcvRecord curRec = data.get(i);
               
               final String packListQty = (curRec.getPackListQty() > 0 ? curRec.getPackListQty() : curRec.getOrdQty()) + "";
               listUpdate.put(i + LU_PACKLISTQTY, packListQty);
               listUpdate.put(i + LU_PACKLISTORIG, packListQty);
               listUpdate.put(i + LU_RCVQTY, curRec.getCurRcvQty() + "");
               listUpdate.put(i + LU_NOTE, "");
               listUpdate.put(i + LU_LINENUM, curRec.getLineNum());
               listUpdate.put(i + LU_ORIGQTY, curRec.getCurRcvQty() + "");
          }
     }

	@Override
     public int getItemCount() {
	     return data.size();
     }

	@Override
     public void onBindViewHolder(RcvHolder holdMe, int pos) {
	     holdMe.bindRcvRec(data.get(pos), pos);
//	     holdMe.setLastEditTextFocusChangeListener(listener);
     }

	@Override
     public RcvHolder onCreateViewHolder(ViewGroup parent, int pos) {
		return new RcvHolder(context, LayoutInflater.from(parent.getContext()).inflate(R.layout.rcv_item, parent, false), listUpdate);
     }
     
}
