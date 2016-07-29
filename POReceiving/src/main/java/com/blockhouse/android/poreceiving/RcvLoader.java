/** This class loads item description from the server into the adapter used for recycler view.
 *
 */
package com.blockhouse.android.poreceiving;

import java.util.ArrayList;

import com.blockhouse.android.utils.HttpQuery;

import android.os.AsyncTask;

/**
 * @author mbarr
 * Date Created: Jul 31, 2013
 *
 */
public class RcvLoader extends AsyncTask<Void, Integer, Void> {

     private final ArrayList<RcvRecord> data;
     private RcvAdapter rcvAdapter;
     private final String poNum;
     
     private boolean stopDownload = false;
     
     /**
      * Default constructor
      * @param inData Data
      * @param inRcvList ListView data goes in.
      */
     public RcvLoader(final ArrayList<RcvRecord> inData, final RcvAdapter inRcvAdapter, final String inPONum) {
          super();
          
          data = inData;
          rcvAdapter = inRcvAdapter;
          poNum = inPONum;
     }
     
     
     @Override
     protected Void doInBackground(Void... inArg0) {
          final ArrayList<String> parameters = new ArrayList<String>(6);
          parameters.add(HttpQuery.CMD_QUERY);
          parameters.add(HttpQuery.QUERY_POLINEDESC);
          parameters.add(HttpQuery.PARAM_PONUM);
          parameters.add("");
          parameters.add(HttpQuery.PARAM_LINENUM);
          parameters.add("");
          
          ArrayList<String> resp = null;
          final int poSize = data.size();
          for (int i = 0; i < poSize; i++) {
               synchronized (this) {
                    if (stopDownload) break;
               }
               parameters.set(1, HttpQuery.QUERY_POLINEDESC);
               parameters.set(3, poNum);
               parameters.set(5, data.get(i).getLineNum());
               resp = HttpQuery.makeRequest(HttpQuery.STND_URL + "AndroidServlet", parameters);
               
               parameters.set(1, HttpQuery.QUERY_PORCPTDATE);
               resp.addAll(HttpQuery.makeRequest(HttpQuery.STND_URL + "AndroidServlet", parameters));
               
               setRcvData(i, resp);
               
               publishProgress(i);
          }
          
          return null;
     }
     
     @Override
     protected synchronized void onProgressUpdate(Integer... index) {
          super.onProgressUpdate(index);
          
          for (int i = 0; i < index.length; i++) {
          	rcvAdapter.notifyItemChanged(index[i]);
          }
     }


	/**
      * Sets the given data into the appropriate record. Changed to thread safe, using post.
      * @param index Index of the record
      * @param rcvData Data to set
      */
     private void setRcvData(final int index, final ArrayList<String> rcvData) {
     	data.get(index).setData(rcvData);
     }
     
     /**
      * Stop the current process.
      */
     public synchronized void stopDownload() {
          stopDownload = true;
     }
     
}
