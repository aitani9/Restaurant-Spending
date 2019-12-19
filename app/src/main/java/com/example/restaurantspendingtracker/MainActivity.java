// TODO change name of app to "Restaurant Spending"

/*
 * The home screen.
 *
 * It displays the current leftover amount of money
 * (total money allowed - total money spent), and it offers access to the
 * activity used to add a bill and the activity used to view the bill history.
 */

package com.example.restaurantspendingtracker;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

// TODO do something about app name not showing entirely in ViewHistory
//      activity after adding a bill (?, not sure about this)

// TODO make the add button clickable area a circle, not a square (if possible)

public class MainActivity extends AppCompatActivity {

   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_main);

      Button btAddBill = findViewById(R.id.btAddBill);
      Button btViewHistory = findViewById(R.id.btViewHistory);

      btAddBill.setOnClickListener(new View.OnClickListener() {
         // post: starts the AddBill activity
         @Override
         public void onClick(View view) {
            startActivity(new Intent(MainActivity.this,
                                     AddBillActivity.class));
         }
      });

      btViewHistory.setOnClickListener(new View.OnClickListener() {
         // post: starts the ViewHistory activity
         @Override
         public void onClick(View v) {
            startActivity(new Intent(MainActivity.this,
                                     ViewHistoryActivity.class));
         }
      });

   }

   // refreshes to make sure the leftover amount of money is up-to-date when
   // clicking back to the MainActivity
   public void onResume() {
      super.onResume();

      TextView tvLeftoverMoney = findViewById(R.id.tvLeftoverMoney);
      DatabaseHelper billDB = new DatabaseHelper(this);
      Cursor leftoverMoneyCursor = billDB.getAllLeftoverMoney();

      double leftoverMoney = 0; // leftoverMoney = moneyAllowed - moneyPaid
      if(leftoverMoneyCursor.moveToFirst()) {
         do {
            leftoverMoney += leftoverMoneyCursor.getDouble(0);
         } while (leftoverMoneyCursor.moveToNext());
      }

      String leftoverMoneyDisplay = String.format(Locale.US, "%.2f",
                                                  leftoverMoney);

      tvLeftoverMoney.setText(getString(R.string.leftover_money,
                                        leftoverMoneyDisplay));
   }

}
