package com.example.android.myinventoryapp.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Salman on 10/11/2017.
 */

public class InventoryDbHelper extends SQLiteOpenHelper {
    // Name of the database file
    private static final String DATABASE_NAME = "inventory.db";

    // Database version, If you change the database schema, you must increment the database version
    private static final int DATABASE_VERSION = 1;

    public InventoryDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // CREATE TABLE products (id INTEGER PRIMARY KEY, productName TEXT, price INTEGER);
        // Create a String that contains the SQL statement to create the products table
        String SQL_CREATE_PRODUCTS_TABLE = "CREATE TABLE " + InventoryContract.ProductEntry.TABLE_NAME + "("
                + InventoryContract.ProductEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + InventoryContract.ProductEntry.COLUMN_PRODUCT_NAME + " TEXT NOT NULL, "
                + InventoryContract.ProductEntry.COLUMN_PRICE + " INTEGER NOT NULL DEFAULT 0, "
                + InventoryContract.ProductEntry.COLUMN_ORDERED_QTY + " INTEGER NOT NULL DEFAULT 0, "
                + InventoryContract.ProductEntry.COLUMN_SALES_QTY + " INTEGER NOT NULL DEFAULT 0, "
                + InventoryContract.ProductEntry.COLUMN_IMAGE + " BLOB);";

        db.execSQL(SQL_CREATE_PRODUCTS_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
