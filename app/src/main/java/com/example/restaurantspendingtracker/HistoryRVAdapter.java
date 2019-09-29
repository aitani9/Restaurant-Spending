/*
 * Class used for managing the display of the bills in the bill history.
 */

package com.example.restaurantspendingtracker;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class HistoryRVAdapter
        extends RecyclerView.Adapter<HistoryRVAdapter.ViewHolder>
        implements Filterable {

   private List<String> viewableBills; // the bills in-view

   private List<Integer> viewableIDs; // the IDs of the bills in-view (ID
                                      // matches the index of its corresponding
                                      // bill in 'viewableBills')

   private List<Integer> allIDs;
   private List<String> allBills;

   // post: constructs a HistoryRVAdapter object with 'viewableIDs' and this
   //       'allIDs' each set to copies of the given 'allIDs', and
   //       'viewableBills' and this 'allBills' each set to copies of the given
   //       'allBills'
   public HistoryRVAdapter(List<Integer> allIDs, List<String> allBills) {
      this.viewableIDs = new ArrayList<>(allIDs);
      this.viewableBills = new ArrayList<>(allBills);
      this.allIDs = new ArrayList<>(allIDs);
      this.allBills = new ArrayList<>(allBills);
   }

   // post: returns a new custom ViewHolder object
   @NonNull
   @Override
   public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
                                        int viewType) {
      View mView = LayoutInflater.from(parent.getContext()).inflate
              (R.layout.history_rv_list_item, parent, false);
      return new ViewHolder(mView);
   }

   // post: displays the bill present in 'viewableBills' at index 'position' in
   //       the given ViewHolder's tvBillDisplay
   @Override
   public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
      holder.tvBillDisplay.setText(viewableBills.get(position));
   }

   // post: returns the number of bills in-view
   @Override
   public int getItemCount() {
      return viewableBills.size();
   }

   // post: returns a new Filter object
   @Override
   public Filter getFilter() {
      return billFilter;
   }

   private Filter billFilter = new Filter() {
      // post: returns a new FilterResults object whose values are bills that
      //       contain the given CharSequence (ignores case and spaces around
      //       the CharSequence)
      @Override
      protected FilterResults performFiltering(CharSequence charSequence) {
         List<String> resultingBills = new ArrayList<>();

         viewableIDs.clear();
         viewableBills.clear();

         if (charSequence == null || charSequence.length() == 0) {
            resultingBills.addAll(allBills);
            viewableIDs.addAll(allIDs);
            viewableBills.addAll(allBills);
         } else {
            String processedCharSeq =
                    charSequence.toString().toLowerCase().trim();

            for (int i = allBills.size() - 1; i >= 0; i--) {
               String bill = allBills.get(i);
               if (bill.toLowerCase().contains(processedCharSeq)) {
                  resultingBills.add(0, bill);
                  viewableIDs.add(0, allIDs.get(i));
                  viewableBills.add(0, bill);
               }
            }
         }

         FilterResults mFilterResults = new FilterResults();
         mFilterResults.values = resultingBills;

         return mFilterResults;
      }

      @Override
      protected void publishResults(CharSequence charSequence,
                                    FilterResults filterResults) {
         notifyDataSetChanged();
      }
   };

   // pre:  'allBills' contains a bill at the given index
   // post: removes the bill at the given index from the RV adapter
   public void removeIDAndBill(int indexInAll){
      int ID = allIDs.remove(indexInAll);
      allBills.remove(indexInAll);
      int positionInRV = viewableIDs.indexOf(ID);
      if (positionInRV != -1) {
         viewableIDs.remove(positionInRV);
         viewableBills.remove(positionInRV);
         notifyItemRemoved(positionInRV);
      }
   }

   // post: returns the IDs of the viewable bills from top to bottom
   public List<Integer> getViewableIDs() {
      return viewableIDs;
   }

   /*
    * Custom RV ViewHolder class.
    *
    * Uses 'history_rv_list_item' as its layout
    * Currently only contains a TextView.
    */
   public class ViewHolder extends RecyclerView.ViewHolder {

      private TextView tvBillDisplay;

      public ViewHolder(@NonNull View itemView) {
         super(itemView);
         tvBillDisplay = itemView.findViewById(R.id.tvBillDisplay);
      }

   }

}
