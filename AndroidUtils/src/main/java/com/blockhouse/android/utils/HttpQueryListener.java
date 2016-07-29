/**
 * 
 */
package com.blockhouse.android.utils;

/**
 * @author mbarr
 * Date Created: May 15, 2013
 *
 */
public interface HttpQueryListener<T> {

     public void queryComplete(T resp);
     
}
