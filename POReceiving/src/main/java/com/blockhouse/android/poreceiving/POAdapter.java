/**
 * 
 */
package com.blockhouse.android.poreceiving;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Locale;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Filter;

/**
 * @author mbarr
 *         Date Created: Jun 3, 2013
 * 
 */
public class POAdapter extends RecyclerView.Adapter<POHolder> {

     private final ArrayList<PORecord> fullData;
     private ArrayList<PORecord> curData;
     final int layoutResourceId;
     private final MainActivity context;
     private final Filter poFilter = new POFilter();
     private Comparator<? super PORecord> lastComp = null;


     /**
      * @param inContext
      * @param inLayoutResourceId
      */
     @SuppressWarnings("unchecked")
     public POAdapter(MainActivity inContext, int inLayoutResourceId, ArrayList<PORecord> inData) {
          super();

          context = inContext;
          layoutResourceId = inLayoutResourceId;
          fullData = inData;
          
          curData = (ArrayList<PORecord>) inData.clone();
     }

     @Override
     public int getItemCount() {
          return curData.size();
     }


     @Override
     public void onBindViewHolder(POHolder holdMe, int pos) {
          holdMe.bindPO(curData.get(pos));
     }


     @Override
     public POHolder onCreateViewHolder(ViewGroup parent, int pos) {
          return new POHolder(context, LayoutInflater.from(parent.getContext()).inflate(layoutResourceId, parent, false));
     }
     
     protected ArrayList<PORecord> getData() {
          return (ArrayList<PORecord>) curData.clone();
     }

     public Filter getFilter() {
          return poFilter;
     }
//
//     @Override
     public synchronized PORecord getItem(int inPosition) {
          return curData.get(inPosition);
     }

     public int getPosition(PORecord inItem) {
          return curData.indexOf(inItem);
     }

     public synchronized void insert(PORecord inObject, int inIndex) {
          curData.add(inIndex, inObject);
     }

     public synchronized void remove(PORecord inObject) {
          curData.remove(inObject);
     }
     
     public synchronized void set(int inIndex, PORecord inRecord) {
          curData.set(inIndex, inRecord);
          notifyItemChanged(inIndex);
     }
     
//     @Override
     public void sort(Comparator<? super PORecord> inComparator) {
          lastComp = inComparator;
          Collections.sort(curData, inComparator);
     }

     class POFilter extends Filter {

          private String lastFilter = "";
          
          @Override
          protected FilterResults performFiltering(CharSequence filter) {
               FilterResults ret = new FilterResults();

               if (filter != null && filter.length() > 0) {
                    String strFilter = filter.toString().toLowerCase(Locale.US);

                    ArrayList<PORecord> filterItems = new ArrayList<PORecord>(curData.size());
                    ArrayList<PORecord> goodItems = new ArrayList<PORecord>();

                    synchronized (this) {
                         if (strFilter.length() > lastFilter.length()) {
                              filterItems.addAll(curData);
                         }
                         else {
                              filterItems.addAll(fullData);
                         }
                    }

                    final int fltItms = filterItems.size();
                    if (Character.isLetter(strFilter.charAt(0))) {
                         // Letter first, must be vendor
                         if (strFilter.length() > 1 && Character.isLetter(strFilter.charAt(1))) {
                              // Vendor Name
                              for (int i = 0; i < fltItms; i++) {
                                   if (filterItems.get(i).getVendName().toLowerCase(Locale.US).startsWith(strFilter)) {
                                        goodItems.add(filterItems.get(i));
                                   }
                              }
                         } else {
                              // Vendor ID
                              for (int i = 0; i < fltItms; i++) {
                                   if (filterItems.get(i).getVendId().toLowerCase(Locale.US).startsWith(strFilter)) {
                                        goodItems.add(filterItems.get(i));
                                   }
                              }
                         }
                    } else {
                         // PO
                         for (int i = 0; i < fltItms; i++) {
                              String poNum = filterItems.get(i).getPoNum();
                              if (poNum.startsWith(strFilter) || poNum.replaceFirst("^0*", "").startsWith(strFilter)) {
                                   goodItems.add(filterItems.get(i));
                              }
                         }
                    }
                    
                    ret.values = goodItems;
                    ret.count = goodItems.size();
                    lastFilter = strFilter;
               } else {
                    synchronized (this) {
                         ret.count = fullData.size();
                         ret.values = fullData.clone();
                         if (lastComp != null) {
                              Collections.sort((ArrayList<PORecord>)ret.values, lastComp);
                         }
                    }
                    lastFilter = "";
               }

               return ret;
          }

          @Override
          protected void publishResults(CharSequence filter, FilterResults filtered) {
               curData = (ArrayList<PORecord>) filtered.values;
               
               notifyDataSetChanged();
          }

     }
}
