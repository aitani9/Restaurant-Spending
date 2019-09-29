/*
 * Class used for managing the ViewHistory activity's delete dialog (launched
 * by pressing on the delete action item in the ViewHistory activity toolbar).
 *
 * The dialog is especially useful for deleting multiple bills simultaneously,
 * rather than swiping through each individual bill. To delete bills, the user
 * simply needs to enter the date those bills were paid on.
 *
 * Additionally, bills on different dates can be deleted (simultaneously as
 * well). This can be achieved by using "X"s when entering a date. For example,
 * "XX/XX/2019" deletes all bills of 2019; "9/XX/2019" deletes all bills of
 * September 2019; and "XX/XX/XXXX" deletes all bills.
 */

package com.example.restaurantspendingtracker;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class HistoryDeleteDialog extends AppCompatDialogFragment  {

   private EditText etDeleteDates; // EditText for inputting the desired
                                   // date(s) to be deleted

   private HistoryDeleteDialogListener mHistoryDeleteDialogListener;

   // post: creates a dialog that with 'activity_history_dialog' as its layout;
   //       it has a positive button of "delete", a negative button of
   //       "cancel", and a title of "Delete".
   @NonNull
   @Override
   public Dialog onCreateDialog(Bundle savedInstanceState) {
      Activity mActivity = requireActivity();
      AlertDialog.Builder mAlertDialogBuilder =
              new AlertDialog.Builder(mActivity);

      LayoutInflater mLayoutInflater = mActivity.getLayoutInflater();
      View mView = mLayoutInflater.inflate(R.layout.activity_history_dialog,
                                           null); // TODO fix warning

      mAlertDialogBuilder.setView(mView).setTitle("Delete")
                         .setNegativeButton("cancel", null)
                         .setPositiveButton("Delete", null);

      etDeleteDates = mView.findViewById(R.id.etDeleteDates);

      return mAlertDialogBuilder.create();
   }

   // post: checks to make sure that the given Context implements the
   //       HistoryDeleteDialogListener interface, throwing a
   //       ClassCastException if it doesn't
   @Override
   public void onAttach(Context context) {
      super.onAttach(context);

      try {
         mHistoryDeleteDialogListener = (HistoryDeleteDialogListener) context;
      } catch (ClassCastException e) {
         throw new ClassCastException
                 (context.toString()
                         + " must implement HistoryDeleteDialogListener");
      }
   }

   @Override
   public void onResume() {
      super.onResume();
      AlertDialog mAlertDialog = (AlertDialog) getDialog();

      Button positiveButton = mAlertDialog.getButton(Dialog.BUTTON_POSITIVE);
      positiveButton.setOnClickListener(new View.OnClickListener() {
         // pre:  the bill history is not empty, a valid date is entered in
         //       etDeleteDates's text field, and there is at least one
         //       matching date in the bill history (displays a message telling
         //       the user what is wrong if conditions not met); in the entered
         //       date, 'MM' and 'dd' may be "XX", 'yyyy' may be "XXXX"
         // post: deletes the date(s) in the bill history that match the date
         //       entered in etDateDeleted's text field and closes the dialog
         @Override
         public void onClick(View view) {
            if (mHistoryDeleteDialogListener.historyIsEmpty()) {
               Toast.makeText(getContext(),"Your bill history is empty",
                              Toast.LENGTH_SHORT).show();
               return;
            }
            String[] dateValidityAndComponents =
                    getDateValidityAndComponents
                            (etDeleteDates.getText().toString());
            if (dateValidityAndComponents[0].equals("false")) {
               Toast.makeText(getContext(), "Enter a valid date",
                              Toast.LENGTH_SHORT).show();
               return;
            }
            String date = dateValidityAndComponents[1]
                          +"/" + dateValidityAndComponents[2]
                          + "/" + dateValidityAndComponents[3];
            boolean noBillsWereDeleted =
                    mHistoryDeleteDialogListener.deleteMatchingDates(date);
            if (noBillsWereDeleted) {
               Toast.makeText(getContext(),
                             "There aren't any bills for the date(s)\n" + date,
                             Toast.LENGTH_SHORT).show();
               return;
            }
            dismiss();
         }
      });
   }

   public interface HistoryDeleteDialogListener {
      boolean deleteMatchingDates(String date);
      boolean historyIsEmpty();
   }

   // post: if the given string is not a valid date (i.e. does not follow the
   //       format "MM/dd/yyyy"), returns an array with its first element as
   //       the string "false"; otherwise, returns an array with its first
   //       element as the string "true" and the rest of its elements as the
   //       formatted (simply adds a "0" in front of single digit months and
   //       days) month, day, and year of the given date; in the given date,
   //       'MM' and 'dd' may be "XX", 'yyyy' may be "XXXX"
   private String[] getDateValidityAndComponents(String givenDate) {
      String[] validityAndDateInfo = new String[4];
      String[] passedBillDateComponents = givenDate.split("/");
      if (passedBillDateComponents.length != 3) {
         validityAndDateInfo[0] = "false";
         return  validityAndDateInfo;
      }
      String passedYear = passedBillDateComponents[2];
      String passedMonth = passedBillDateComponents[0];
      String passedDay = passedBillDateComponents[1];

      /*
       * In the cases below where at least one component doesn't contain "X"s,
       * "01" is used as a month, "15" as a day, and/or "0000" as a year. These
       * numbers simply act as placeholders so it can be passed to the method
       * for doing the same processing as this method but for numerical dates.
       *
       * Note that these numbers can't be randomly chosen. The placeholder
       * month must be a month that exists in every year; the placeholder day
       * must be a day of the month that exists every year; and the placeholder
       * year must be a year that exists. Otherwise, the incorrect date
       * component may be returned
       */
      if (passedMonth.equals("XX")
              && passedDay.equals("XX")
              && passedYear.equals("XXXX")) { // "XX/XX/XXXX"

         validityAndDateInfo[0] = "true";
         validityAndDateInfo[1] = passedMonth;
         validityAndDateInfo[2] = passedDay;
         validityAndDateInfo[3] = passedYear;

      } else if (passedMonth.equals("XX")
              && passedDay.equals("XX")) { // "XX/XX/[NNNN]"

         validityAndDateInfo[1] = passedMonth;
         validityAndDateInfo[2] = passedDay;
         validityAndDateInfo[3] =
                 getValidityAndComponentsOfNumDate("01/15/"+passedYear)[3];
         validityAndDateInfo[0] =
                 String.valueOf(validityAndDateInfo[3] == null);

      } else if (passedMonth.equals("XX")
              && passedYear.equals("XXXX")) { // "XX/[NN]/XXXX"

         validityAndDateInfo[1] = passedMonth;
         validityAndDateInfo[3] = passedYear;
         validityAndDateInfo[2] =
                 getValidityAndComponentsOfNumDate("01/"+passedDay+"/0000")[2];
         validityAndDateInfo[0] =
                 String.valueOf(validityAndDateInfo[2] == null);

      } else if (passedMonth.equals("XX")) { // "XX/[NN]/[NNNN]"

         validityAndDateInfo[1] = passedMonth;
         String[] testComponents =
                 getValidityAndComponentsOfNumDate
                         ("01/"+passedDay+"/"+passedYear);
         validityAndDateInfo[2] = testComponents[2];
         validityAndDateInfo[3] = testComponents[3];
         validityAndDateInfo[0] = testComponents[0];

      } else if (passedDay.equals("XX")
              && passedYear.equals("XXXX")) { // "[NN]/XX/XXXX"

         validityAndDateInfo[2] = passedDay;
         validityAndDateInfo[3] = passedYear;
         validityAndDateInfo[1] =
                 getValidityAndComponentsOfNumDate(passedMonth+"/15/0000")[1];
         validityAndDateInfo[0] =
                 String.valueOf(validityAndDateInfo[1] == null);

      } else if (passedDay.equals("XX")) { // "[NN]/XX/[NNNN]"

         validityAndDateInfo[2] = passedDay;
         String[] testComponents =
                 getValidityAndComponentsOfNumDate
                         (passedMonth+"/15/"+passedYear);
         validityAndDateInfo[1] = testComponents[1];
         validityAndDateInfo[3] = testComponents[3];
         validityAndDateInfo[0] = testComponents[0];

      } else if (passedYear.equals("XXXX")) { // "[NN]/[NN]/XXXX"

         validityAndDateInfo[3] = passedYear;
         String[] testComponents =
                 getValidityAndComponentsOfNumDate
                         (passedMonth+"/"+passedDay+"/0000");
         validityAndDateInfo[1] = testComponents[1];
         validityAndDateInfo[2] = testComponents[2];
         validityAndDateInfo[0] = testComponents[0];

      } else { // "[NN]/[NN]/[NNNN]"
         validityAndDateInfo =  getValidityAndComponentsOfNumDate(givenDate);
      }
      return validityAndDateInfo;
   }

   // post: if the given string is not a valid numerical date (i.e. does not
   //       follow the format "[NN]/[NN]/[NNNN]"), returns an array with its
   //       first element as the string "false"; otherwise, returns an array
   //       with its first element as the string "true" and the rest of its
   //       elements as the formatted (simply adds a "0" in front of single
   //       digit months and days) month, day, and year of the given numerical
   //       date
   private String[] getValidityAndComponentsOfNumDate(String numericalDate) {
      String[] validityAndDateInfo = new String[4];
      try {
         SimpleDateFormat  mSimpleDateFormat =
                 new SimpleDateFormat("MM/dd/yyyy", Locale.US);
         Date mDateObject = mSimpleDateFormat.parse(numericalDate);
         if (mDateObject == null) {
            validityAndDateInfo[0] = "false";
            return validityAndDateInfo;
         }
         Calendar cal = Calendar.getInstance();
         cal.setTime(mDateObject);
         validityAndDateInfo[1] =
                 Integer.toString((cal.get(Calendar.MONTH) + 1));
         if (validityAndDateInfo[1].length() == 1) {
            validityAndDateInfo[1] = "0"+validityAndDateInfo[1];
         }
         validityAndDateInfo[2]=
                 Integer.toString(cal.get(Calendar.DAY_OF_MONTH));
         if (validityAndDateInfo[2].length() == 1) {
            validityAndDateInfo[2] = "0"+validityAndDateInfo[2];
         }
         validityAndDateInfo[3] = Integer.toString(cal.get(Calendar.YEAR));
         validityAndDateInfo[0] = "true";
      } catch(NullPointerException | ParseException e) {
         validityAndDateInfo[0] = "false";
      }
      return validityAndDateInfo;
   }

}
