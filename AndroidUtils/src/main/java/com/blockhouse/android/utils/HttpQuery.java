/**
 *
 */
package com.blockhouse.android.utils;

import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import android.os.AsyncTask;
import android.os.Trace;

import com.google.gson.Gson;

/**
 * @author mbarr
 * Date Created: May 15, 2013
 *
 */
public abstract class HttpQuery {

     public static final Gson GSON = new Gson();
     //Live Server
//     public static final String STND_URL = "http://192.168.0.35:8888/servlet/";
     //Test Server
     public static final String STND_URL = "http://192.168.0.32:8888/servlet/";
     //Eric S local
//     public static final String STND_URL = "http://192.168.0.172:8888/servlet/";

     public static final String CMD_QUERY = "query";
     public static final String CMD_POST = "post";
     public static final String CMD_CHECKUPDATE = "checkupdate";
     public static final String CMD_STATUSUPDATE = "statusupdate";
     public static final String QUERY_ITEMLIST = "itemlist";
     public static final String QUERY_ITEMDETAIL = "itemdetail";
     public static final String QUERY_POLIST = "polist";
     public static final String QUERY_POLINES = "polines";
     public static final String QUERY_POLINEDESC = "polinedesc";
     public static final String QUERY_PORCPTDATE = "poreceiptdate";
     public static final String QUERY_NONPOLIST = "nonpolist";
     public static final String POST_RCVPO = "receivepo";
     public static final String POST_RMVNONPO = "removenonpo";
     public static final String PARAM_ITEMNUM = "itemnumber";
     public static final String PARAM_STATUS = "status";
     public static final String PARAM_DATEFROM = "datefrom";
     public static final String PARAM_DATETHRU = "datethru";
     public static final String PARAM_VENDOR = "withvend";
     public static final String PARAM_PONUM = "ponum";
     public static final String PARAM_BALDUE = "baldue";
     public static final String PARAM_LINENUM = "linenum";
     public static final String PARAM_PACKLISTQTY = "packlistqty";
     public static final String PARAM_RCVQTY = "rcvqty";
     public static final String PARAM_NOTE = "nohte";
     public static final String PARAM_SEQNUM = "seqnum";
     public static final String PARAM_VERSION = "version";
     public static final String PARAM_EXTRA = "extra";
     public static final String PARAM_STARTACTIVITY = "startactivity";
     public static final String PARAM_LBLCOUNT = "lblcount";


     public static final String ENCODING = "UTF-8";

     private static <T> T httpRequest(final String url, final ArrayList<String> parameters, final Class<T> tClass) {
          final HttpClient httpclient = new DefaultHttpClient();
          final HttpParams params = httpclient.getParams();
          HttpConnectionParams.setConnectionTimeout(params, 10000);
          HttpConnectionParams.setSoTimeout(params, 20000);

          T ret = null;

          final StringBuilder urlReq = new StringBuilder(url);

          try {
               final int pSize = parameters.size();
               if (pSize >= 2) {
                    if (!url.endsWith("/")) urlReq.append("/");
                    urlReq.append("?");
                    int i = 0;
                    do {
                         urlReq.append(URLEncoder.encode(parameters.get(i++), ENCODING) + "=" + URLEncoder.encode(parameters.get(i++), ENCODING));
                         if (i + 2 <= pSize) urlReq.append("&");
                    } while (i + 2 <= pSize);
               }

               final HttpGet httpget1 = new HttpGet(urlReq.toString());

               final HttpResponse resp = httpclient.execute(httpget1);

               ret = GSON.fromJson(new InputStreamReader(resp.getEntity().getContent()), tClass);
          }
          catch (Exception e) {
               System.err.println("Error getting http response.");
               e.printStackTrace();
               return null;
          }
          finally {
               httpclient.getConnectionManager().shutdown();
          }

          return ret;
     }

     /**
      * Makes a http request. Automates on a worker thread, returns the class type requested to the HttpQueryListener provided.
      * @param inListener Class to call when complete.
      * @param url Url to query
      * @param parameters Parameters of query
      * @param tClass Class of the object returned to the HttpQueryListener
      */
     public static <T> AsyncTask makeRequest(final HttpQueryListener<T> inListener, final String url, final ArrayList<String> parameters, final Class<T> tClass) {
          //Arrange parameters into the array
          String[] p = new String[parameters.size() + 1];
          p[0] = url;
          for (int i = 1; i < p.length; i++) {
               p[i] = parameters.get(i - 1);
          }

          //Handles the http call on a worker thread, and returns the list to the listener provided.
          return new AsyncTask<String, Void, T>() {

               HttpQueryListener<T> asyncListener = null;

               /**
                * Helper method to accept the listener and the parameters. Calls execute to start the async task.
                * @param innerListener - Listener to send list to
                * @param innerParameters Parameters, url as 1st
                */
               protected AsyncTask launch(HttpQueryListener<T> innerListener, String[] innerParameters) {
                    asyncListener = innerListener;
                    execute(innerParameters);
                    return this;
               }

               @Override
               protected T doInBackground(String... params) {
                    ArrayList<String> query = new ArrayList<String>(Arrays.asList(params));
                    query.remove(0);

                    return httpRequest(params[0], query, tClass);
               }

               @Override
               protected void onPostExecute(final T ret) {
                    asyncListener.queryComplete(ret);
               }

          }.launch(inListener, p);
     }

     /**
      * Makes a http request. Automates on a worker thread, returns an ArrayList to the HttpQueryListener provided.
      * @param inListener Class to call when complete.
      * @param url Url to query
      * @param parameters Parameters of query
      */
     @SuppressWarnings("rawtypes")
     public static AsyncTask makeRequest(final HttpQueryListener<ArrayList> inListener, final String url, final ArrayList<String> parameters) {
          return makeRequest(inListener, url, parameters, ArrayList.class);
     }

     /**
      * Makes an http request on the current thread.
      * @param url Url to query
      * @param parameters Parameters of query
      * @return Response from request, already translated through gson.
      */
     @SuppressWarnings("unchecked")
     public static ArrayList<String> makeRequest(String url, ArrayList<String> parameters) {
          return httpRequest(url, parameters, parameters.getClass());
     }

}
