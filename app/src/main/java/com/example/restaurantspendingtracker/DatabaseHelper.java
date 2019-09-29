/*
 * Class used for managing the database storing the bill data of bills present
 * in the bill history.
 */

package com.example.restaurantspendingtracker;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

// TODO delete the "Bill" column from the database and add columns for the
//      date, amount paid, and money allowed instead so it is less constraining
//      for later

public class DatabaseHelper extends SQLiteOpenHelper {

   public static final String DATABASE_NAME = "BillHistory.db";
   public static final String TABLE_NAME = "BillData";
   public static final String COL1 = "ID";
   public static final String COL2 = "DisplayedBill";
   // leftoverMoney = moneyAllowed - moneyPaid
   public static final String COL3 = "LeftoverMoney";

   // post: constructs a database with the current DATABASE_NAME in the given
   //       context
   public DatabaseHelper(Context context) {
      super(context, DATABASE_NAME, null, 1);
   }

   // post: creates a new table with the current TABLE_NAME and columns; uses
   //       auto-incrementing IDs as the primary identifier for each row of
   //       data
   @Override
   public void onCreate(SQLiteDatabase db) {
      String createTable = "CREATE TABLE " + TABLE_NAME + " (" + COL1 +
                           " INTEGER PRIMARY KEY " + "AUTOINCREMENT, " + COL2 +
                           " String, " + COL3 + " DOUBLE)";
      db.execSQL(createTable);
   }

   // post: deletes the existing table from the given database and creates a
   //       new one
   public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
      db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
      onCreate(db);
   }

   // post: adds the given billDisplay and leftoverMoney to the database
   public void addBillData(String billDisplay, double leftoverMoney) {
      ContentValues contentValues = new ContentValues();
      contentValues.put(COL2, billDisplay);
      contentValues.put(COL3, leftoverMoney);
      this.getWritableDatabase().insert(TABLE_NAME, null, contentValues);
   }

   // post: returns a cursor containing the IDs of all the bills in the
   //       database
   public Cursor getAllIDs() {
      return this.getWritableDatabase().rawQuery
              ("SELECT " + COL1 + " FROM " + TABLE_NAME, null);
   }

   // post: returns a cursor containing all the display bills in the database
   public Cursor getAllDisplayBills() {
      return this.getWritableDatabase().rawQuery
              ("SELECT " + COL2 + " FROM "+ TABLE_NAME, null);
   }

   // post: returns a cursor containing the leftover money of all bills in the
   //       database
   public Cursor getAllLeftoverMoney() {
      return this.getWritableDatabase().rawQuery
              ("SELECT " + COL3 + " FROM " + TABLE_NAME, null);
   }

   // pre:  the database contains the given ID
   // post: deletes the bill data of the bill belonging to the given ID from
   //       the database
   public void removeBill(int passedID) {
      this.getWritableDatabase().execSQL
              ("DELETE FROM " + TABLE_NAME + " WHERE " + COL1 + "= " + passedID);
   }

}
