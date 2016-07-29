/**
 * 
 */
package com.blockhouse.android.poreceiving;

import java.util.List;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

/**
 * @author mbarr
 * Date Created: Sep 5, 2013
 *
 */
public class NonPOAdapter extends RecyclerView.Adapter<NonPOHolder> implements OnClickListener {

     final int layoutResourceId;
     private final Context context;
     
     private final NonPODialog dialog;
     
     private final List<NonPORecord> objects;
          
     /**
      * @param inContext
      * @param inResourceId
      * @param inObjects
      */
     public NonPOAdapter(Context inContext, int inResourceId, List<NonPORecord> inObjects, NonPODialog inDialog) {
          super();
          layoutResourceId = inResourceId;
          context = inContext;
          dialog = inDialog;
          objects = inObjects;
     }
     
     @Override
     public void onClick(View inView) {
          final Button curButton = (Button) inView;

          dialog.removeNonPORcpt(((NonPORecord)curButton.getTag()).getSeqNum());
     }

	@Override
     public int getItemCount() {
	     return objects.size();
     }

	@Override
     public void onBindViewHolder(NonPOHolder holdMe, int pos) {
		holdMe.bindNonPO(objects.get(pos));	     
     }

	@Override
     public NonPOHolder onCreateViewHolder(ViewGroup parent, int pos) {
		return new NonPOHolder(context, LayoutInflater.from(parent.getContext()).inflate(R.layout.nonpo_item, parent, false), this);
     }
	
	public void removeNonPO(final String seq) {
		for (int i = 0; i < objects.size(); i++) {
			if (objects.get(i).getSeqNum().equals(seq)) {
				objects.remove(i);
				super.notifyItemRemoved(i);
			}
		}
	}
	
	public void setRecords(final List<NonPORecord> inObjects) {
		objects.clear();
		objects.addAll(inObjects);
		super.notifyDataSetChanged();
	}
}
