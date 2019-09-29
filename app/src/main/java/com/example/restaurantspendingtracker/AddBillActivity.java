/*
 * The activity used to "add" (see below) a bill to the bill history.
 *
 * It doesn't actually add the bill to the bill history, but rather receives
 * the bill information from the user, processes that information, and then
 * sends it to the ViewHistory activity where it is actually added to the bill
 * history.
 */

package com.example.restaurantspendingtracker;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;

// TODO make it so that when an entry is added it is grouped with other bills
//      of the same date (if there are multiple bills for the same date)
//      instead of being placed at the top of the history (?, not too sure
//      about this feature)

public class AddBillActivity extends AppCompatActivity {

   private EditText etDatePayed;
   private EditText etAmountPaid;
   private EditText etMoneyAllowed;
   private CheckBox cbDefaultToToday;
   private CheckBox cbRememberAllowedMoney;
   private SharedPreferences mPreferences;
   private DatePickerDialog.OnDateSetListener mDateSetListener;
   private String dateBeforeDefaulting;
   private String lastSelectedDialogDate;

   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_add_bill);

      etDatePayed = findViewById(R.id.etDatePayed);
      etAmountPaid = findViewById(R.id.etAmountPaid);
      etMoneyAllowed = findViewById(R.id.etMoneyAllowed);
      cbDefaultToToday = findViewById(R.id.cbDefaultToToday);
      cbRememberAllowedMoney = findViewById(R.id.cbRememberAllowedMoney);
      mPreferences = PreferenceManager.getDefaultSharedPreferences(this);

      Button btAddToBillHistory = findViewById(R.id.btAddToBillHistory);

      checkSharedPrefs(); // whether or not to default to today's date and/or
                          // use the last saved allowed amount of money

      btAddToBillHistory.setOnClickListener(new View.OnClickListener() {
         // pre:  a date is selected and a valid amount of money spent and
         //       allowed are entered (displays a message telling the user what
         //       to fix if conditions not met)
         // post: saves the current user preferences; sends the date, amount
         //       spent, and amount allowed to the ViewHistory activity; starts
         //       the ViewHistory activity
         @Override
         public void onClick(View v) {

            String givenDate = etDatePayed.getText().toString();

            if (givenDate.isEmpty()) {
               Toast.makeText(AddBillActivity.this,"Select a date",
                              Toast.LENGTH_SHORT).show();
               return;
            }

            String givenAmountPaid = etAmountPaid.getText().toString();
            String givenMoneyAllowed = etMoneyAllowed.getText().toString();

            if (toastedInvalidMoney(givenAmountPaid.isEmpty(),
                                    givenMoneyAllowed.isEmpty())) {
               return;
            }

            double dblAmountPaid = Double.parseDouble(givenAmountPaid);
            double dblMoneyAllowed = Double.parseDouble(givenMoneyAllowed);

            if(toastedInvalidMoney(isNotValidMoneyInput(dblAmountPaid),
                                   isNotValidMoneyInput(dblMoneyAllowed))) {
               return;
            }

            updateSharedPrefs(givenMoneyAllowed);

            Intent intent = new Intent(AddBillActivity.this,
                                       ViewHistoryActivity.class);
            intent.putExtra("displayDate", "("+givenDate+")");
            intent.putExtra("dblAmountPaid", dblAmountPaid);
            intent.putExtra("dblMoneyAllowed", dblMoneyAllowed);
            startActivity(intent);
         }
      });

      etDatePayed.setOnClickListener(new View.OnClickListener() {
         // post: opens a date picker dialog with the highlighted date set to
         //       the last selected date or, if there wasn't a previous date
         //       selected, to the current date (today)
         @Override
         public void onClick(View view) {
            hideSoftKeyboard(AddBillActivity.this);
            int month;
            int day;
            int year;
            if (lastSelectedDialogDate == null
                    || lastSelectedDialogDate.equals("")) {
               Calendar cal = Calendar.getInstance();
               month = cal.get(Calendar.MONTH);
               day = cal.get(Calendar.DAY_OF_MONTH);
               year = cal.get(Calendar.YEAR);
            } else {
               String[] dateComponents = lastSelectedDialogDate.split("/");
               month = Integer.parseInt(dateComponents[0]) - 1;
               day = Integer.parseInt(dateComponents[1]);
               year = Integer.parseInt(dateComponents[2]);
            }

            DatePickerDialog mDatePickerDialog = new DatePickerDialog
                    (AddBillActivity.this, mDateSetListener, year, month, day);
            mDatePickerDialog.show();

         }
      });

      mDateSetListener = new DatePickerDialog.OnDateSetListener() {
         // post: displays the selected date in etDatePayed's text field in the
         //       format "MM/dd/yyyy" and (as a minor QoL feature) saves the
         //       selected date so it defaults back to it when reselecting a
         //       date before adding the bill
         @Override
         public void onDateSet(DatePicker datePicker, int year, int month,
                               int day) {
            String strMonth = processMonthOrDay(month + 1);
            String strDay = processMonthOrDay(day);
            String date = strMonth+"/"+strDay+"/"+year;
            etDatePayed.setText(date);
            lastSelectedDialogDate = date;
         }
      };

      cbDefaultToToday.setOnCheckedChangeListener(
              new CompoundButton.OnCheckedChangeListener() {
         // checked:   displays the current date (today) in etDatePayed's text
         //            field in the format "MM/dd/yyyy"; the displayed date is
         //            unchangeable; and (as a minor QoL feature) saves the
         //            existing date before checking
         // unchecked: if was previously checked, the last saved date is
         //            displayed, otherwise doesn't display a date; allows the
         //            user to change the date
         @Override
         public void onCheckedChanged(CompoundButton compoundButton,
                                      boolean isChecked) {
            etDatePayed.setEnabled(!isChecked);
            if (isChecked) {
               dateBeforeDefaulting = etDatePayed.getText().toString();
               displayTodaysDate();
            } else {
              etDatePayed.setText(dateBeforeDefaulting);
            }
         }
      });
   }

   // pre:  there is a view in-focus in the given Activity and there is an
   //       on-screen keyboard already open (does nothing if conditions not
   //       met)
   // post: hides the on-screen keyboard from the view in-focus in the given
   //       Activity
   public void hideSoftKeyboard(Activity activity) {
      InputMethodManager inputMethodManager = (InputMethodManager)
              activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
      View focusedView = activity.getCurrentFocus();
      if (inputMethodManager != null && focusedView != null) {
         inputMethodManager.
                 hideSoftInputFromWindow(focusedView.getWindowToken(), 0);
      }
   }

   // post: returns a formatted string representation of the given month or
   //       day; the format is that all months and days have exactly two
   //       numbers (e.g. "4" is converted to "04")
   public String processMonthOrDay (int monthOrDay) {
      String strComponent = Integer.toString(monthOrDay);
      if (strComponent.length() == 1) {
         strComponent = "0"+strComponent;
      }
      return strComponent;
   }

   // post: returns false if 'amount' has <= 2 decimal places, returns true
   //       otherwise
   private boolean isNotValidMoneyInput(double amount) {
      String strAmount = Double.toString(amount);
      if (strAmount.contains(".")) {
         String[] amountComponents = strAmount.split("\\.");
         return (amountComponents[1].length() > 2);
      }
      return false;
   }

   // post: defaults to the current date (today) and/or uses the last saved
   //       allowed amount of money (checking their corresponding CheckBoxes in
   //       the process), or neither, depending on the last saved user
   //       preferences
   private void checkSharedPrefs() {
      boolean dateCheckboxIsChecked =
              mPreferences.getBoolean("cbDefaultToTodayIsChecked", false);
      boolean allowedCheckboxIsChecked =
              mPreferences.getBoolean("cbRememberAllowedMoneyIsChecked",
                                      false);
      String savedMoneyAllowed = mPreferences.getString("savedAllowed", "");

      etMoneyAllowed.setText(savedMoneyAllowed);

      if (dateCheckboxIsChecked) {
         cbDefaultToToday.setChecked(true);
         displayTodaysDate();
         etDatePayed.setEnabled(false);
      }
      if (allowedCheckboxIsChecked) {
         cbRememberAllowedMoney.setChecked(true);
         // line below: minor QoL feature where less actions are required from
         //             the user to add a bill when cbRememberAllowedMoney is
         //             already checked
         etAmountPaid.setImeOptions(EditorInfo.IME_ACTION_DONE);
      }
   }

   // post: saves the current user preferences for the next time they want to
   //       add a bill (default to the current date (today) and/or remember the
   //       current allowed amount of money, or neither)
   private void updateSharedPrefs(String givenMoneyAllowed) {
      SharedPreferences.Editor mPrefsEditor = mPreferences.edit();
      boolean cbRememberAllowedMoneyIsChecked =
              cbRememberAllowedMoney.isChecked();

      mPrefsEditor.putBoolean("cbDefaultToTodayIsChecked",
                              cbDefaultToToday.isChecked());
      mPrefsEditor.putBoolean("cbRememberAllowedMoneyIsChecked",
                              cbRememberAllowedMoneyIsChecked);

      if (cbRememberAllowedMoneyIsChecked) {
         // if/else block below: minor visual enhancement that formats the
         //                      allowed amount of money to have exactly two
         //                      decimal places (e.g. "20" is reformatted to
         //                      "20.00")
         String formattedMoneyAllowed = givenMoneyAllowed;
         if (!givenMoneyAllowed.contains(".")) {
            formattedMoneyAllowed += ".00";
         } else {
            String[] allowedSplit = givenMoneyAllowed.split("\\.");
            if (allowedSplit.length == 1) {
               formattedMoneyAllowed += "00";
            } else if (allowedSplit[1].length() == 1) {
               formattedMoneyAllowed += "0";
            }
         }

         mPrefsEditor.putString("savedAllowed", formattedMoneyAllowed);
      } else {
         mPrefsEditor.putString("savedAllowed", "");
      }
      mPrefsEditor.apply();
   }

   // post: displays the current date (today) in etDatePayed's text field
   private void displayTodaysDate() {
      Calendar cal = Calendar.getInstance();
      int month = cal.get(Calendar.MONTH) + 1;
      int day = cal.get(Calendar.DAY_OF_MONTH);
      int year = cal.get(Calendar.YEAR);

      etDatePayed.setText(getString(R.string.display_date,
              processMonthOrDay(month), processMonthOrDay(day),
              Integer.toString(year)));
   }

   // post: returns true and displays a message telling the user what amounts
   //       of money are invalid if at least one of the given booleans is true,
   //       returns false otherwise
   private boolean toastedInvalidMoney(boolean amountPaidIsNotValid,
                                       boolean moneyAllowedIsNotValid) {
      if (amountPaidIsNotValid && moneyAllowedIsNotValid) {
         Toast.makeText(AddBillActivity.this,
                        "Enter a valid amount of money paid and allowed",
                        Toast.LENGTH_SHORT).show();
         return true;
      } else if (amountPaidIsNotValid) {
         Toast.makeText(AddBillActivity.this,
                        "Enter a valid amount of money paid",
                        Toast.LENGTH_SHORT).show();
         return true;
      } else if (moneyAllowedIsNotValid) {
         Toast.makeText(AddBillActivity.this,
                        "Enter a valid amount of money allowed",
                        Toast.LENGTH_SHORT).show();
         return true;
      }
      return false;
   }

}
