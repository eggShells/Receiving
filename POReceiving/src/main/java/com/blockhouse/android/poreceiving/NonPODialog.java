/**
 * 
 */
package com.blockhouse.android.poreceiving;

import java.util.ArrayList;

import com.blockhouse.android.utils.HttpQuery;
import com.blockhouse.android.utils.HttpQueryListener;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Toast;

/**
 * @author mbarr
 * Date Created: Sep 5, 2013
 *
 */
public class NonPODialog extends DialogFragment implements HttpQueryListener<ArrayList> {
     
     private RecyclerView nonPOList;
     private NonPOAdapter adapter = null;
     private ArrayList<NonPORecord> records = null;
     private Context context;
     ProgressDialog myProgress = null;
     private boolean curRemoval = false;

    public NonPODialog(){

    }

     /**
      * @param inContext
      */
     @SuppressLint("ValidFragment")
     public NonPODialog(Context inContext) {
//          super(inContext);
          context = inContext;

          getNonPOList();
          super.setStyle(STYLE_NORMAL, R.style.AlertTheme);
     }
	
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
     	View view = inflater.inflate(R.layout.nonpo_popup, container);
     	getDialog().setTitle(R.string.NonPO_Title);
     	setCancelable(true);
     	getDialog().setCanceledOnTouchOutside(true);
     	
     	nonPOList = (RecyclerView) view.findViewById(R.id.NonPOList);
     	adapter = new NonPOAdapter(context, R.layout.nonpo_item, records, this);
          nonPOList.setLayoutManager(new LinearLayoutManager(nonPOList.getContext()));
          nonPOList.setAdapter(adapter);
     	
	     return view;
     }

	@Override
     public void onConfigurationChanged(Configuration newConfig) {
	     super.onConfigurationChanged(newConfig);
	     
	     resizeDialog();
     }

	@Override
     public void onResume() {
		resizeDialog();
		
	     super.onResume();
     }

	private void getNonPOList() {
          // Show Progress Circle
          myProgress = ProgressDialog.show(context, context.getString(R.string.appTitle), context.getString(R.string.NonPO_progress), true, false);

          final ArrayList<String> parameters = new ArrayList<String>();
          parameters.add(HttpQuery.CMD_QUERY);
          parameters.add(HttpQuery.QUERY_NONPOLIST);
          
          HttpQuery.makeRequest(this, HttpQuery.STND_URL + "AndroidServlet", parameters);
     }

     @Override
     public void queryComplete(final ArrayList inResp) {
          if (myProgress != null && myProgress.isShowing()) {
               myProgress.hide();
               myProgress.dismiss();
               myProgress = null;
          }
          
//          System.out.println(inResp);
          if (curRemoval) {
               //Removal response
               if (inResp != null && inResp.size() >= 1) {
//                    GuiUtils.animateRemoval(nonPOList, adapter, curRemoval, MOVE_DURATION);
               	adapter.removeNonPO(inResp.get(0) + "");
               }
               else {
                    Toast.makeText(context.getApplicationContext(), R.string.NonPO_rcvfailed, Toast.LENGTH_LONG).show();
               }
               curRemoval = false;
          }
          else {
               //Initial list response
               if (inResp.size() > 1) {
                    //parse response
                    records = new ArrayList<NonPORecord>(inResp.size() / 5);
                    for (int i = 0; i <= inResp.size() - 5; i++) {
                         records.add(new NonPORecord(inResp.get(i++) + "", inResp.get(i++) + "", inResp.get(i++) + "", Double.parseDouble(inResp.get(i++) + ""), Integer.parseInt(inResp.get(i) + "")));
                    }
                    
                    super.show(((Activity)context).getFragmentManager().beginTransaction(), "NonPODialog");
               }
               else {
                    Toast.makeText(context.getApplicationContext(), R.string.NonPO_none, Toast.LENGTH_LONG).show();
               }
          }
     }
     
     protected void removeNonPORcpt(final String seqRemove) {
     	curRemoval = true;
          myProgress = ProgressDialog.show(context, context.getString(R.string.appTitle), context.getString(R.string.NonPO_rcving), true, false);

          final ArrayList<String> parameters = new ArrayList<String>();
          parameters.add(HttpQuery.CMD_POST);
          parameters.add(HttpQuery.POST_RMVNONPO);
          parameters.add(HttpQuery.PARAM_SEQNUM);
          parameters.add(seqRemove);
          
          HttpQuery.makeRequest(this, HttpQuery.STND_URL + "AndroidServlet", parameters);
     }
     
     private void resizeDialog() {
     	final DisplayMetrics metrics = context.getResources().getDisplayMetrics();
          final WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
          lp.copyFrom(getDialog().getWindow().getAttributes());
          
          lp.height = (int) (metrics.heightPixels * .75);
          lp.width = (int) (metrics.widthPixels * .75);
          
          getDialog().getWindow().setAttributes(lp);
     }
}
