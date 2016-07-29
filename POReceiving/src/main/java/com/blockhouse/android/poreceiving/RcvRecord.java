/**
 * 
 */
package com.blockhouse.android.poreceiving;

import java.util.ArrayList;
import java.util.List;

/**
 * @author mbarr
 * Date Created: Jul 29, 2013
 *
 */
public class RcvRecord {

     private final String itemNum;
     private final String lineNum;
     private int reqDate = -1;
     private final double ordQty;
     private final double pstRcvQty;
     private final double curRcvQty;
     private final double packListQty;

	private final ArrayList<String> descs;
	private final ArrayList<PrtRecord> labels;

	/**
      * Initial constructor, all fields should be previously known.
      * @param inItemNum
      * @param inLineNum
      * @param inOrdQty
      * @param desc
      * @param descTwo
      */
     public RcvRecord(final String inItemNum, final String inLineNum, final double inOrdQty, final String desc, final String descTwo, final double inPstRcvQty, final double inCurRcvQty, final double inPackListQty) {
          itemNum = inItemNum;
          lineNum = inLineNum;
          ordQty = inOrdQty;
          pstRcvQty = inPstRcvQty;
          curRcvQty = inCurRcvQty;
          packListQty = inPackListQty;
          descs = new ArrayList<String>();
          if (!desc.isEmpty()) descs.add(desc);
          if (!descTwo.isEmpty()) descs.add(descTwo);
          labels = new ArrayList<PrtRecord>();
     }

     public boolean equals(Object o) {
          if (o instanceof RcvRecord) {
               RcvRecord rr = (RcvRecord)o;
                return rr.getItemNum().equals(getItemNum()) && rr.getLineNum().equals(getLineNum());
          }
          return false;
     }

     public double getCurRcvQty() {
		return curRcvQty;
	}
     
     /**
      * Returns descs
      * @return the descs
      */
     public ArrayList<String> getDescs() {
          return descs;
     }

     /**
      * Returns itemNum
      * @return the itemNum
      */
     public String getItemNum() {
          return itemNum;
     }

     /**
	 * @return the labels
	 */
	public ArrayList<PrtRecord> getLabels() {
		return labels;
	}

	/**
      * Returns lineNum
      * @return the lineNum
      */
     public String getLineNum() {
          return lineNum;
     }
     /**
      * Returns ordQty
      * @return the ordQty
      */
     public double getOrdQty() {
          return ordQty;
     }

     public double getPackListQty() {
		return packListQty;
	}

	public double getPstRcvQty() {
		return pstRcvQty;
	}
     
     /**
      * Returns reqDate
      * @return the reqDate
      */
     public int getReqDate() {
          return reqDate;
     }
     
     /**
      * Sets data that is retreived after initialization of the record.
      * @param inData Descriptions for the record, last record is the receipt date
      */
     public void setData(final List<String> inData) {
          if (inData != null && inData.size() > 0) {
               reqDate = Integer.parseInt(inData.remove(inData.size() - 1));
               descs.addAll(inData);
          }
     }

}
