package com.example.android.myinventoryapp;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.content.ContentValues;

import com.example.android.myinventoryapp.data.InventoryContract;

import static java.lang.String.valueOf;

/**
 * Created by Salman on 10/14/2017.
 */

public class InventoryCursorAdapter extends CursorAdapter {

    private TextView nameTextView;

    private TextView priceTextView;

    private TextView currentQTYTextView;

    private TextView saleQTYTextView;

    private ImageView imgView;

    private Button saleButton;

    public InventoryCursorAdapter(Context context, Cursor c) {
        super(context, c, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    @Override
    public void bindView(View view, final Context context, final Cursor cursor) {
        // Find the views
        nameTextView = view.findViewById(R.id.product_name_txt);
        priceTextView = view.findViewById(R.id.price_txt);
        imgView = view.findViewById(R.id.img_view);
        currentQTYTextView = view.findViewById(R.id.current_qty_txt);
        saleQTYTextView = view.findViewById(R.id.sale_qty_txt);
        saleButton = view.findViewById(R.id.sales_btn1);

        // Find the columns
        int nameColumnIndex = cursor.getColumnIndex(InventoryContract.ProductEntry.COLUMN_PRODUCT_NAME);
        int priceColumnIndex = cursor.getColumnIndex(InventoryContract.ProductEntry.COLUMN_PRICE);
        int imageColumnIndex = cursor.getColumnIndex(InventoryContract.ProductEntry.COLUMN_IMAGE);
        int currentQTYColumnIndex = cursor.getColumnIndex(InventoryContract.ProductEntry.COLUMN_ORDERED_QTY);
        int saleQTYColumnIndex = cursor.getColumnIndex(InventoryContract.ProductEntry.COLUMN_SALES_QTY);

        // Read the products attribute
        String productName = cursor.getString(nameColumnIndex);
        Integer productPrice = cursor.getInt(priceColumnIndex);
        byte[] productImage = cursor.getBlob(imageColumnIndex);
        Integer currentQTY = cursor.getInt(currentQTYColumnIndex);
        Integer saleQTY = cursor.getInt(saleQTYColumnIndex);

        // Update the views
        nameTextView.setText(productName);
        priceTextView.setText(valueOf(productPrice));

        if (productImage != null) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(productImage, 0, productImage.length);
            imgView.setImageBitmap(bitmap);
        } else {
            imgView.setImageResource(R.mipmap.ic_launcher);
        }

        currentQTYTextView.setText(valueOf(currentQTY));
        saleQTYTextView.setText(valueOf(saleQTY));


        if (cursor.moveToPosition(cursor.getPosition())) {
            saleButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    {

updateProduct(context);
                    }
                }
            });
        }
    }



    void updateProduct(Context context) {
        String orderedQTYString = currentQTYTextView.getText().toString().trim();
        String saleQTYString = saleQTYTextView.getText().toString().trim();
        int orderQTY = Integer.parseInt(orderedQTYString);
        if (orderQTY != 0) {
            orderQTY -= 1;
            int saleQTY = Integer.parseInt(saleQTYString);
            saleQTY += 1;
            saleQTYTextView.setText(valueOf(saleQTY));
            ContentValues values = new ContentValues();
            values.put(InventoryContract.ProductEntry.COLUMN_ORDERED_QTY, orderQTY);
            values.put(InventoryContract.ProductEntry.COLUMN_SALES_QTY, saleQTY);

            int rowsUpdated = context.getContentResolver().update(InventoryContract.ProductEntry.CONTENT_URI, values, null, null);

            if (rowsUpdated == 0) {
                // If no rows were updated, then there was an error with the update.
                Toast.makeText(context, context.getString(R.string.editor_update_product_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the update was successful and we can display a toast.
                Toast.makeText(context, context.getString(R.string.editor_update_product_successful),
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(context, "Order quantity must not be less than 0!",
                    Toast.LENGTH_SHORT).show();
        }
    }

}
