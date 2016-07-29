/**
 * 
 */
package com.blockhouse.android.poreceiving;

import java.io.Serializable;

/**
 * @author mbarr
 * Date Created: Sep 5, 2013
 *
 */
public class NonPORecord implements Serializable {

     private final String seqNum;
     private final String source;
     private final double qty;
     private final int expDate;
     private final String desc;
     
     public NonPORecord(final String inSeqNum, final String inDesc, final String inSource, final double inQty, final int inExpDate) {
          seqNum = inSeqNum;
          source = inSource;
          qty = inQty;
          expDate = inExpDate;
          desc = inDesc;
     }
     
     /**
      * Returns seqNum
      * @return the seqNum
      */
     public String getSeqNum() {
          return seqNum;
     }
     /**
      * Returns source
      * @return the source
      */
     public String getSource() {
          return source;
     }
     /**
      * Returns qty
      * @return the qty
      */
     public double getQty() {
          return qty;
     }
     /**
      * Returns expDate
      * @return the expDate
      */
     public int getExpDate() {
          return expDate;
     }
     /**
      * Returns desc
      * @return the desc
      */
     public String getDesc() {
          return desc;
     }
     
}
