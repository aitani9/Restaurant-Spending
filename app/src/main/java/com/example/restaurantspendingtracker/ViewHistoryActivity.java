/*
 * The activity used to view (and search within) the bill history, add bills to
 * the database, and remove bills from the bill history.
 */

package com.example.restaurantspendingtracker;

import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

// TODO make it so that we can edit entries (?)
// TODO make it so that we can move entries (?)

// TODO hide keyboard when clicking "check" after search and pressing the
//      menu's delete button and exiting from the dialog

public class ViewHistoryActivity extends AppCompatActivity
        implements HistoryDeleteDialog.HistoryDeleteDialogListener {

   private DatabaseHelper billDB; // database containing the bills and the
                                  // leftover amount of money from each bill

   private HistoryRVAdapter mHistoryRVAdapter;

   private TextView tvEmptyHistory; // tells the user that the bill history is
                                    // empty; remains hidden when history is
                                    // not empty

   private List<Integer> allIDs;
   private List<String> allBills;

   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_view_history);
      setSupportActionBar((Toolbar) findViewById(R.id.tbHistory));

      tvEmptyHistory = findViewById(R.id.tvEmptyHistory);

      billDB = new DatabaseHelper(this);
      allIDs = new ArrayList<>();
      allBills = new ArrayList<>();

      receiveAddedBillDataAndAddToDB();

      getAllIDsAndBillsFromDB();

      showHistory();
   }

   // current menu action items: search and delete
   @Override
   public boolean onCreateOptionsMenu(Menu menu) {

      MenuInflater mMenuInflater = getMenuInflater();
      mMenuInflater.inflate(R.menu.history_menu, menu);

      MenuItem searchItem = menu.findItem(R.id.action_search);
      final SearchView mSearchView = (SearchView) searchItem.getActionView();

      mSearchView.setImeOptions(EditorInfo.IME_ACTION_DONE);

      mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
         @Override
         public boolean onQueryTextSubmit(String query) {
            return false;
         }

         // pre:  the bill history is not empty (if empty, the user can still
         //       enter a string but only tvEmptyHistory will be displayed)
         // post: shows bills containing the given string (ignores case and
         //       spaces around the string) and hides bills that don't
         @Override
         public boolean onQueryTextChange(String newText) {
            if (mHistoryRVAdapter != null) {
               mHistoryRVAdapter.getFilter().filter(newText);
            }
            return false;
         }
      });
      return true;
   }

   // delete item selected (post): opens the delete dialog
   @Override
   public boolean onOptionsItemSelected(@NonNull MenuItem item) {
      int itemID = item.getItemId();
      if (itemID == R.id.action_delete) {
         HistoryDeleteDialog mHistoryDialog = new HistoryDeleteDialog();
         mHistoryDialog.show(getSupportFragmentManager(),
                             "history delete dialog");
      }
      return super.onOptionsItemSelected(item);
   }

   ItemTouchHelper.SimpleCallback historyItemTouchHelper =
           new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
      @Override
      public boolean onMove(@NonNull RecyclerView recyclerView,
                            @NonNull RecyclerView.ViewHolder viewHolder,
                            @NonNull RecyclerView.ViewHolder target) {
         return false;
      }

      // post: permanently deletes the bill that is swiped left from the
      //       history, removing the deleted bill from view and moving up the
      //       bills below it if there are any; if the resulting history is
      //       empty, tvEmptyHistory is displayed
      @Override
      public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder,
                           int direction) {
         int positionInRV = viewHolder.getAdapterPosition();
         int ID = mHistoryRVAdapter.getViewableIDs().get(positionInRV);
         int indexInAll = allIDs.indexOf(ID);
         deleteBillFromSQLandRV(indexInAll);

         if (allBills.isEmpty()) {
            tvEmptyHistory.setText(R.string.your_bill_history_is_empty);
         }
      }
   };

   // pre:  the given string corresponds to a valid date of the format
   //       "MM/dd/yyyy"; 'MM' and 'dd' may be "XX", 'yyyy' may be "XXXX"
   // post: deletes all bills in the bill history with dates that match the
   //       given date, returning true if at least one bill was deleted and
   //       false otherwise; when 'MM' is "XX", it corresponds to all months;
   //       when 'dd' is "XX", it corresponds ot all days of the month; when
   //       'yyyy' is "XXXX", it corresponds to all years; (e.g. "09/XX/2019"
   //       deletes all bills of September 2019, "XX/XX/2019" deletes all bills
   //       of 2019, "XX/XX/XXXX" deletes all bills, etc.)
   @Override
   public boolean deleteMatchingDates(String date) {
      int initialSize = allBills.size();

      String[] componentsOfPassedDate = date.split("/");
      String passedYear = componentsOfPassedDate[2];
      String passedMonth = componentsOfPassedDate[0];
      String passedDay = componentsOfPassedDate[1];

      if (passedMonth.equals("XX") && passedDay.equals("XX")
              && passedYear.equals("XXXX")) { // "XX/XX/XXXX"

         for (int i = allBills.size() - 1; i >= 0; i--) {
            deleteBillFromSQLandRV(i);
         }

      } else if (passedMonth.equals("XX")
              && passedDay.equals("XX")) { // "XX/XX/[NNNN]"

         for (int i = allBills.size() - 1; i >= 0; i--) {
            if (passedYear.equals
                    (getComponentsOfDisplayDate(allBills.get(i))[2])) {
               deleteBillFromSQLandRV(i);
            }
         }

      } else if (passedMonth.equals("XX")
              && passedYear.equals("XXXX")) { // "XX/[NN]/XXXX"

         for (int i = allBills.size() - 1; i >= 0; i--) {
            if (passedDay.equals
                    (getComponentsOfDisplayDate(allBills.get(i))[1])) {

               deleteBillFromSQLandRV(i);
            }
         }

      } else if (passedMonth.equals("XX")) { // "XX/[NN]/[NNNN]"

         for (int i = allBills.size() - 1; i >= 0; i--) {
            String[] billDateComponents =
                    getComponentsOfDisplayDate(allBills.get(i));

            if (passedDay.equals(billDateComponents[1])
                    && passedYear.equals(billDateComponents[2])) {

               deleteBillFromSQLandRV(i);
            }
         }

      } else if (passedDay.equals("XX")
              && passedYear.equals("XXXX")) { // "[NN]/XX/XXXX"

         for (int i = allBills.size() - 1; i >= 0; i--) {
            if (passedMonth.equals
                    (getComponentsOfDisplayDate(allBills.get(i))[0])) {

               deleteBillFromSQLandRV(i);
            }
         }

      } else if (passedDay.equals("XX")) { // "[NN]/XX/[NNNN]"

         for (int i = allBills.size() - 1; i >= 0; i--) {
            String[] billDateComponents =
                    getComponentsOfDisplayDate(allBills.get(i));

            if (passedMonth.equals(billDateComponents[0])
                    && passedYear.equals(billDateComponents[2])) {

               deleteBillFromSQLandRV(i);
            }
         }

      } else if (passedYear.equals("XXXX")) { // "[NN]/[NN]/XXXX"

         for (int i = allBills.size() - 1; i >= 0; i--) {
            String[] billDateComponents =
                    getComponentsOfDisplayDate(allBills.get(i));

            if (passedMonth.equals(billDateComponents[0])
                    && passedDay.equals(billDateComponents[1])) {

               deleteBillFromSQLandRV(i);
            }
         }

      } else { // "[NN]/[NN]/[NNNN]"

         for (int i = allBills.size() - 1; i >= 0; i--) {
            String[] billDateComponents =
                    getComponentsOfDisplayDate(allBills.get(i));

            if (passedMonth.equals(billDateComponents[0])
                    && passedDay.equals(billDateComponents[1])
                    && passedYear.equals(billDateComponents[2])) {

               deleteBillFromSQLandRV(i);
            }
         }

      }

      if (allBills.isEmpty()) {
         tvEmptyHistory.setText(R.string.your_bill_history_is_empty);
      }

      return (initialSize == allBills.size());
   }

   // post: returns true if the bill history is empty, returns false otherwise
   @Override
   public boolean historyIsEmpty() {
      return (allBills.isEmpty());
   }

   // pre:  a bill was just "added" (see AddBillActivity class header) via the
   //       AddBill activity
   // post: adds the bill in the format:
   //       "([MM]/[dd]/[yyyy]) Spent: [amount paid]; Allowed: [money allowed]"
   //       to the database; also adds the difference between the money allowed
   //       and amount paid of that bill to the database
   private void receiveAddedBillDataAndAddToDB() {
      Bundle receivedBillData = getIntent().getExtras();
      if (receivedBillData != null) {
         if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
         }
         String displayDate = receivedBillData.getString("displayDate");
         double dblAmountPaid = receivedBillData.getDouble("dblAmountPaid");
         double dblMoneyAllowed =
                 receivedBillData.getDouble("dblMoneyAllowed");

         String billDisplay = getString(R.string.new_display_date, displayDate,
                                        dblAmountPaid, dblMoneyAllowed);

         billDB.addBillData(billDisplay, dblMoneyAllowed - dblAmountPaid);
      }
   }

   // post: retrieves all bills and their IDs from the database and adds them
   //       to 'allBills' and 'allIDs' respectively
   private void getAllIDsAndBillsFromDB() {
      Cursor IDsCursor = billDB.getAllIDs();
      Cursor billsCursor = billDB.getAllDisplayBills();

      if (billsCursor.moveToFirst() && IDsCursor.moveToFirst()) {
         do {
            allIDs.add(0, IDsCursor.getInt(0));
            allBills.add(0, billsCursor.getString(0));
         } while (IDsCursor.moveToNext() && billsCursor.moveToNext());
      }
   }

   // post: if the history is not empty, displays the bills retrieved from the
   //       database in the following format:
   //       "([MM]/[dd]/[yyyy]) Spent: [amount paid]; Allowed: [money allowed]"
   //       ; if the history is empty, tvEmptyHistory is displayed
   private void showHistory() {
      if (allBills.isEmpty()) {
         tvEmptyHistory.setText(R.string.your_bill_history_is_empty);
      } else {
         tvEmptyHistory.setText("");
         RecyclerView rvHistory = findViewById(R.id.rvHistory);
         mHistoryRVAdapter = new HistoryRVAdapter(allIDs, allBills);

         LinearLayoutManager mLinearLayoutManager =
                 new LinearLayoutManager(this);
         rvHistory.setLayoutManager(mLinearLayoutManager);

         DividerItemDecoration mDividerItemDecoration =
                 new DividerItemDecoration
                         (rvHistory.getContext(),
                          mLinearLayoutManager.getOrientation());
         rvHistory.addItemDecoration(mDividerItemDecoration);

         new ItemTouchHelper(historyItemTouchHelper).
                 attachToRecyclerView(rvHistory);
         rvHistory.setAdapter(mHistoryRVAdapter);
      }
   }

   // pre:  the given bill starts with the display date "([MM]/[dd]/[yyyy])"
   // post: returns an array of strings containing the month, followed by the
   //       day, followed by the year
   private String[] getComponentsOfDisplayDate(String displayedBill) {
      String[] rightSideTrimmedDisplayDate = displayedBill.split("\\)");
      String date = rightSideTrimmedDisplayDate[0].substring(1);
      return date.split("/");
   }

   // pre:  'allBills' contains a bill at the given index
   // post: removes the bill at the given index from the bill history
   private void deleteBillFromSQLandRV(int indexInAll) {
      int ID = allIDs.remove(indexInAll);
      allBills.remove(indexInAll);
      billDB.removeBill(ID);
      mHistoryRVAdapter.removeIDAndBill(indexInAll);
   }

}
