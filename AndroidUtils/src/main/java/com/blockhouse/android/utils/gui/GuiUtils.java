/**
 * 
 */
package com.blockhouse.android.utils.gui;

import java.util.HashMap;

import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ListView;

/**
 * @author mbarr
 * Date Created: Sep 6, 2013
 *
 */
public abstract class GuiUtils {

     /**
      * This method animates all other views in the ListView container (not including ignoreView)
      * into their final positions. It is called after ignoreView has been removed from the
      * adapter, but before layout has been run. The approach here is to figure out where
      * everything is now, then allow layout to run, then figure out where everything is after
      * layout, and then to run animations between all of those start/end positions.
      * 
      * Code from Chet Haase : https://plus.google.com/u/0/+ChetHaase/posts/RRTm14JsPQY
      * @param listView View to remove from
      * @param adapter Adapter for view
      * @param indexToRemove Index to remove
      * @param MOVE_DURATION Time, in milliseconds to take to remove the item.
      */
     public static void animateRemoval(final ListView listView, final StableArrayAdapter adapter, final int indexToRemove, final int MOVE_DURATION) {
          final HashMap<Long, Integer> itemIdTopMap = new HashMap<Long, Integer>();
          final View viewToRemove = listView.getChildAt(indexToRemove);
          listView.setEnabled(false);
          
         int firstVisiblePosition = listView.getFirstVisiblePosition();
         for (int i = 0; i < listView.getChildCount(); ++i) {
             View child = listView.getChildAt(i);
             if (child != viewToRemove) {
                 int position = firstVisiblePosition + i;
                 long itemId = adapter.getItemId(position);
                 itemIdTopMap.put(itemId, child.getTop());
             }
         }
         // Delete the item from the adapter
         adapter.remove(adapter.getItem(indexToRemove));

         final ViewTreeObserver observer = listView.getViewTreeObserver();
         observer.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
             public boolean onPreDraw() {
                 observer.removeOnPreDrawListener(this);
                 boolean firstAnimation = true;
                 int firstVisible = listView.getFirstVisiblePosition();
                 for (int i = 0; i < listView.getChildCount(); ++i) {
                     final View child = listView.getChildAt(i);
                     int curPos = firstVisible + i;
                     long itemId = adapter.getItemId(curPos);
                     Integer startTop = itemIdTopMap.get(itemId);
                     int top = child.getTop();
                     if (startTop != null) {
                         if (startTop != top) {
                             int delta = startTop - top;
                             child.setTranslationY(delta);
                             child.animate().setDuration(MOVE_DURATION).translationY(0);
                             if (firstAnimation) {
                                 child.animate().withEndAction(new Runnable() {
                                     public void run() {
//                                         mBackgroundContainer.hideBackground();
                                          listView.setEnabled(true);
                                     }
                                 });
                                 firstAnimation = false;
                             }
                         }
                     } else {
                         // Animate new views along with the others. The catch is that they did not
                         // exist in the start state, so we must calculate their starting position
                         // based on neighboring views.
                         int childHeight = child.getHeight() + listView.getDividerHeight();
                         startTop = top + (i > 0 ? childHeight : -childHeight);
                         int delta = startTop - top;
                         child.setTranslationY(delta);
                         child.animate().setDuration(MOVE_DURATION).translationY(0);
                         if (firstAnimation) {
                             child.animate().withEndAction(new Runnable() {
                                 public void run() {
//                                     mBackgroundContainer.hideBackground();
//                                     mSwiping = false;
                                      listView.setEnabled(true);
                                 }
                             });
                             firstAnimation = false;
                         }
                     }
                 }
                 itemIdTopMap.clear();
                 return true;
             }
         });
     }
}
