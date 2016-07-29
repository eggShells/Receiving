/**
 * 
 */
package com.blockhouse.android.poreceiving;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;

import android.os.AsyncTask;

import com.blockhouse.android.utils.HttpQuery;
import com.blockhouse.android.utils.HttpQueryListener;

/**
 * @author mbarr
 *         Date Created: Jun 3, 2013
 * 
 */
public class PORecord implements Serializable,  HttpQueryListener<ArrayList> {

     private final String poNum;
     private final String vendId;
     private final String vendName;
     private final int ordDate;
     private final String ordBy;
     private final int reqDate;
     private final String reqBy;
     private ArrayList<String> itemNum = null;
     private final ArrayList<String> lineNum = new ArrayList<String>();
//     private final ArrayList<Double> balDue = new ArrayList<Double>();
     private final ArrayList<String> descs = new ArrayList<String>();
     private final ArrayList<String> descTwo = new ArrayList<String>();
//     private final ArrayList<Double> pendTempRcv = new ArrayList<Double>();
     private final ArrayList<Double> ordQty = new ArrayList<Double>();
     private final ArrayList<Double> pstRcvQty = new ArrayList<Double>();
     private final ArrayList<Double> curRcvQty = new ArrayList<Double>();
     private final ArrayList<Double> packListQty = new ArrayList<Double>();
     
     private HttpQueryListener<ArrayList> updateListener = null;

     public PORecord(final String inPONum, final String inVendId, final String inVendName, final int inOrdDate, final int inReqDate, final String inOrdBy, final String inReqBy) {
          poNum = inPONum;
          vendId = inVendId;
          vendName = inVendName;
          ordDate = inOrdDate;
          ordBy = inOrdBy;
          reqDate = inReqDate;
          reqBy = inReqBy;
     }
     
     /**
      * Clears stored item list info.
      */
     public void clearItemList() {
     	itemNum = null;
     	lineNum.clear();
          descs.clear();
          descTwo.clear();
          ordQty.clear();
          pstRcvQty.clear();
          curRcvQty.clear();
          packListQty.clear();
     }
     
     @Override
     public boolean equals(Object o) {
          if (o instanceof PORecord) {
               return ((PORecord)o).getPoNum().equals(getPoNum());
          }
          return false;
     }
     
     /**
      * Returns the total amount of 'current' receipts for this order.
      * @return Total current receipts.
      */
     public double getCurRcpts() {
     	double ret = 0;
     	for (int i = 0; i < curRcvQty.size(); i++) {
     		ret += Math.abs(curRcvQty.get(i));
     	}
     	return ret;
     }
     
     /**
	 * @return the curRcvQty
	 */
	public ArrayList<Double> getCurRcvQty() {
		return curRcvQty;
	}

     /**
	 * @return the descs
	 */
	public ArrayList<String> getDescs() {
		return descs;
	}

     /**
	 * @return the descTwo
	 */
	public ArrayList<String> getDescTwo() {
		return descTwo;
	}

	/**
	 * @return the itemNum
	 */
	public ArrayList<String> getItemNum() {
		return itemNum;
	}

	/**
	 * @return the lineNum
	 */
	public ArrayList<String> getLineNum() {
		return lineNum;
	}

	/**
      * Returns ordBy
      * 
      * @return the ordBy
      */
     public String getOrdBy() {
          return ordBy;
     }

	/**
      * Returns ordDate
      * 
      * @return the ordDate
      */
     public int getOrdDate() {
          return ordDate;
     }

	/**
	 * @return the ordQty
	 */
	public ArrayList<Double> getOrdQty() {
		return ordQty;
	}

	/**
      * Returns poNum
      * 
      * @return the poNum
      */
     public String getPoNum() {
          return poNum;
     }

	/**
	 * @return the pstRcvQty
	 */
	public ArrayList<Double> getPstRcvQty() {
		return pstRcvQty;
	}

	public ArrayList<RcvRecord> getRcvRecords() {
          final ArrayList<RcvRecord> ret = new ArrayList<RcvRecord>();
          
          for (int i = 0; i < itemNum.size(); i++) {
               ret.add(new RcvRecord(itemNum.get(i), lineNum.get(i), ordQty.get(i), descs.get(i), descTwo.get(i), pstRcvQty.get(i), curRcvQty.get(i), packListQty.get(i)));
          }
          
          return ret;
     }

     /**
      * Returns reqBy
      * 
      * @return the reqBy
      */
     public String getReqBy() {
          return reqBy;
     }

     /**
      * Returns reqDate
      * 
      * @return the reqDate
      */
     public int getReqDate() {
          return reqDate;
     }

     /**
      * Returns vendId
      * 
      * @return the vendId
      */
     public String getVendId() {
          return vendId;
     }

     /**
      * Returns vendName
      * 
      * @return the vendName
      */
     public String getVendName() {
          return vendName;
     }
     
     /**
      * Returns if the record has been loaded yet. Determined by if the item numbers have been populated.
      * @return True if loaded, false otherwise.
      */
     protected boolean isLoaded() {
          return itemNum != null;
     }
     
     
     
     public void queryComplete(ArrayList inResp) {
		// Query from an update record request.
		synchronized (this) {
			setLineData(inResp);
		}
		if (updateListener != null) {
			updateListener.queryComplete(inResp);
			updateListener = null;
		}
	}
     
     /**
	 * @param itemNum the itemNum to set
	 */
	public void setItemNum(ArrayList<String> itemNum) {
		this.itemNum = itemNum;
	}
     
     /**
      * Sets the line data in the po record.
      * @param rec IRecord to set data on
      * @param lineData Data to set
      * @return Number of lines added
      */
     protected int setLineData(ArrayList<String> lineData) {
//     	System.out.println(lineData);
          if (lineData == null) return 0;
          
          clearItemList();
          final int listSize = lineData.size() / 8;
          itemNum = new ArrayList<String>(listSize);
          lineNum.ensureCapacity(listSize);
          descs.ensureCapacity(listSize);
          descTwo.ensureCapacity(listSize);
          ordQty.ensureCapacity(listSize);
          pstRcvQty.ensureCapacity(listSize);
          curRcvQty.ensureCapacity(listSize);
          packListQty.ensureCapacity(listSize);
          
          int i = 0;
          for (int q = 0; q < listSize; q++) {
              itemNum.add(lineData.get(i++));
               lineNum.add(lineData.get(i++));
               descs.add(lineData.get(i++));
               descTwo.add(lineData.get(i++));
               ordQty.add(Double.parseDouble(lineData.get(i++)));
               pstRcvQty.add(Double.parseDouble(lineData.get(i++)));
               curRcvQty.add(Double.parseDouble(lineData.get(i++)));
               packListQty.add(Double.parseDouble(lineData.get(i++)));
          }
          
          return listSize;
     }
     
     @Override
     public String toString() {
          return "PORecord:: " + getPoNum() + ", " + getVendId() + " - " + getVendName() + " < " + itemNum.toString() + " > < " + curRcvQty.toString() + " >";
     }
     
     /**
      * Sends a QUERY to the server to get current live data on record's line information.
      */
     @SuppressWarnings("rawtypes")
     public AsyncTask updateRecord() {
     	ArrayList<String> parameters = new ArrayList<String>(4);
          parameters.add(HttpQuery.CMD_QUERY);
          parameters.add(HttpQuery.QUERY_POLINES);
          parameters.add(HttpQuery.PARAM_PONUM);
          parameters.add(getPoNum());
          parameters.add(HttpQuery.PARAM_BALDUE);
		parameters.add(MainActivity.pref_OnlyBalDue ? "1" : "0");
          return HttpQuery.makeRequest(this, HttpQuery.STND_URL + "AndroidServlet", parameters);
     }
     
     /**
      * Sends a request to the server to update this record's line information and sends query completion event to the
      * given listener.
      * @param listener Listener to receive queryComplete call.
      */
     @SuppressWarnings("rawtypes")
     public AsyncTask updateRecord(HttpQueryListener<ArrayList> listener) {
     	updateListener = listener;
     	return updateRecord();
     }
}
