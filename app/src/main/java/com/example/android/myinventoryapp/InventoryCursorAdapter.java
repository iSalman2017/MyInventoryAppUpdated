package com.example.android.myinventoryapp;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.myinventoryapp.data.InventoryContract;

/**
 * Created by Salman on 10/14/2017.
 */

public class InventoryCursorAdapter extends CursorAdapter {

    public InventoryCursorAdapter(Context context, Cursor c) {
        super(context, c, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        // Find the views
        TextView nameTextView = view.findViewById(R.id.product_name_txt);
        TextView priceTextView = view.findViewById(R.id.price_txt);
        ImageView imgView = view.findViewById(R.id.img_view);
        TextView currentQTYTextView = view.findViewById(R.id.current_qty_txt);
        TextView saleQTYTextView = view.findViewById(R.id.sale_qty_txt);

        // Find the columns
        int nameColumnIndex = cursor.getColumnIndex(InventoryContract.ProductEntry.COLUMN_PRODUCT_NAME);
        int priceColumnIndex = cursor.getColumnIndex(InventoryContract.ProductEntry.COLUMN_PRICE);
        int imageColumnIndex = cursor.getColumnIndex(InventoryContract.ProductEntry.COLUMN_IMAGE);
        int currentQTYColumnIndex = cursor.getColumnIndex(InventoryContract.ProductEntry.COLUMN_ORDERED_QTY);
        int saleQTYColumnIndex = cursor.getColumnIndex(InventoryContract.ProductEntry.COLUMN_SALES_QTY);

        // Read the products attribute
        String productName = cursor.getString(nameColumnIndex);
        Integer productPrice = cursor.getInt(priceColumnIndex);
        //String productImage = cursor.getString(imageColumnIndex);
        byte[] productImage = cursor.getBlob(imageColumnIndex);
        Integer currentQTY = cursor.getInt(currentQTYColumnIndex);
        Integer saleQTY = cursor.getInt(saleQTYColumnIndex);

        // Update the views
        nameTextView.setText(productName);
        priceTextView.setText(String.valueOf(productPrice));

        if (productImage.length < 0) {
            imgView.setImageResource(R.mipmap.ic_launcher);
        } else {
            Bitmap bitmap = BitmapFactory.decodeByteArray(productImage, 0, productImage.length);
            imgView.setImageBitmap(bitmap);
        }

        currentQTYTextView.setText(String.valueOf(currentQTY));
        saleQTYTextView.setText(String.valueOf(saleQTY));

    }

}
