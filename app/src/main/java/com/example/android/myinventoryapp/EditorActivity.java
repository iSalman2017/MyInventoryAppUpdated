package com.example.android.myinventoryapp;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.myinventoryapp.data.InventoryContract;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import static java.lang.String.valueOf;

public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int EXISTING_PRODUCT_LOADER = 0;

    private boolean mProductHasChanged = false;

    /**
     * Content URI for the existing product (null if it's a new products)
     */
    private Uri mCurrentProductUri;

    private EditText mNameEditText;

    private EditText mPriceEditText;

    private TextView mOrderedQtyText;

    private ImageView mImageView;

    private TextView mSaleQtyText;

    private EditText mIncreaseQtyText;

    private Button mChooseImageBtn;

    private Button mPlusBtn;

    private Button mMinusBtn;

    private Button mSaleBtn;

    private Button mOrderBtn;

    private static final int REQUEST_CODE_GALLERY = 999;

    private static final int CAMERA_REQUEST = 1888;


    public int orderQTY = 0;
    public int saleQTY = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        // Use getIntent() and getData() to get associated URI
        Intent intent = getIntent();
        mCurrentProductUri = intent.getData();

        // If the intent DOES NOT contain a product content uri, then we know that we are
        // creating a new product.
        if (mCurrentProductUri == null) {
            // This is new product, so change the app bar to say "Add a new product"
            setTitle(getString(R.string.editor_activity_title_add_product));

            // Invalidate the options menu, so the "Delete" menu option can be hidden.
            // (It doesn't make sense to delete a product that hasn't been created yet.)
            invalidateOptionsMenu();


        } else {
            // Otherwise this is an existing product, so change the app bar to say "Edit product"
            setTitle(getString(R.string.editor_activity_title_edit_product));

            // Initialize a loader to read the pet data from the database
            // and display the current values in the editor
            getLoaderManager().initLoader(EXISTING_PRODUCT_LOADER, null, this);
        }

        //Find all relevant views
        mNameEditText = (EditText) findViewById(R.id.edit_name);
        mPriceEditText = (EditText) findViewById(R.id.edit_price);
        mOrderedQtyText = (TextView) findViewById(R.id.txt_qty);
        mImageView = (ImageView) findViewById(R.id.image_view);
        mChooseImageBtn = (Button) findViewById(R.id.btn_choose_image);
        mPlusBtn = (Button) findViewById(R.id.btn_plus);
        mMinusBtn = (Button) findViewById(R.id.btn_minus);
        mSaleBtn = (Button) findViewById(R.id.sales_btn);
        mSaleQtyText = (TextView) findViewById(R.id.sales_txt);
        mIncreaseQtyText = (EditText) findViewById(R.id.qty_increment);
        mOrderBtn = (Button) findViewById(R.id.order_btn);


        mNameEditText.setOnTouchListener(mTouchListener);
        mPriceEditText.setOnTouchListener(mTouchListener);
        mOrderedQtyText.setOnTouchListener(mTouchListener);
        mImageView.setOnTouchListener(mTouchListener);
        mChooseImageBtn.setOnTouchListener(mTouchListener);
        mPlusBtn.setOnTouchListener(mTouchListener);
        mMinusBtn.setOnTouchListener(mTouchListener);
        mSaleBtn.setOnTouchListener(mTouchListener);
        mSaleQtyText.setOnTouchListener(mTouchListener);
        mIncreaseQtyText.setOnTouchListener(mTouchListener);
        mOrderBtn.setOnTouchListener(mTouchListener);

        mPlusBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String increaseByText = mIncreaseQtyText.getText().toString();
                int quantity = Integer.parseInt(mOrderedQtyText.getText().toString());

                //If the EditText for increase the quantity is empty, add just 1 to the quantity.
                if (increaseByText.isEmpty()){
                    quantity++;
                }

                // Else, add the amount set in the increase edit text to the quantity.
                else{
                    quantity += Integer.parseInt(increaseByText);
                }
                mOrderedQtyText.setText(String.valueOf(quantity));
            }
        });

        mMinusBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String increaseByText = mIncreaseQtyText.getText().toString();
                int quantity = Integer.parseInt(mOrderedQtyText.getText().toString());

                //If the EditText for decrease the quantity is empty, add just 1 to the quantity.
                if (increaseByText.isEmpty()){
                    quantity--;
                }

                // Else, add the amount set in the decrease edit text to the quantity.
                else{
                    quantity -= Integer.parseInt(increaseByText);
                }
                mOrderedQtyText.setText(String.valueOf(quantity));
            }
        });

        mSaleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateProduct();
            }
        });

        mChooseImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, CAMERA_REQUEST);*/

                ActivityCompat.requestPermissions
                        (EditorActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE_GALLERY);

            }
        });


        mOrderBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent mailIntent = new Intent(Intent.ACTION_SENDTO);
                mailIntent.setData(Uri.parse("mailto:"));
                mailIntent.putExtra(Intent.EXTRA_SUBJECT, "New Order request!!");
                mailIntent.putExtra(Intent.EXTRA_TEXT, submitOrder());
                if (mailIntent.resolveActivity(getPackageManager()) != null) {
                    startActivity(mailIntent);
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE_GALLERY) {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, REQUEST_CODE_GALLERY);
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode != RESULT_CANCELED) {
            if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK) {
                Bitmap photo = (Bitmap) data.getExtras().get("data");
                mImageView.setImageBitmap(photo);
            } else {
                Uri uri = data.getData();
                try {
                    InputStream inputStream = getContentResolver().openInputStream(uri);
                    Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                    mImageView.setImageBitmap(bitmap);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_edit, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.save_item:
                saveProduct();
                // Exit the activity
                finish();
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.delete_item:
                // Pop up confirmation dialog for deletion
                showDeleteConfirmationDialog();
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // If the pet hasn't changed, continue with navigating up to parent activity
                // which is the {@link MainActivity}.
                if (!mProductHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }

                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that
                // changes should be discarded.
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };

                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // Since the editor shows all product attributes, define a projection that contains
        // all columns from the product table
        String[] projection = {
                InventoryContract.ProductEntry._ID,
                InventoryContract.ProductEntry.COLUMN_PRODUCT_NAME,
                InventoryContract.ProductEntry.COLUMN_PRICE,
                InventoryContract.ProductEntry.COLUMN_ORDERED_QTY,
                InventoryContract.ProductEntry.COLUMN_SALES_QTY,
                InventoryContract.ProductEntry.COLUMN_IMAGE};

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,   // Parent activity context
                mCurrentProductUri,         // Query the content URI for the current product
                projection,             // Columns to include in the resulting Cursor
                null,                   // No selection clause
                null,                   // No selection arguments
                null);                  // Default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Bail early if the cursor is null or there is less than 1 row in the cursor
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        // Proceed with moving to the first row of the cursor and reading data from it
        // (This should be the only row in the cursor)
        if (cursor.moveToFirst()) {
            // Find the columns of pet attributes that we're interested in
            int nameColumnIndex = cursor.getColumnIndex(InventoryContract.ProductEntry.COLUMN_PRODUCT_NAME);
            int priceColumnIndex = cursor.getColumnIndex(InventoryContract.ProductEntry.COLUMN_PRICE);
            int imageColumnIndex = cursor.getColumnIndex(InventoryContract.ProductEntry.COLUMN_IMAGE);
            int currentQTYColumnIndex = cursor.getColumnIndex(InventoryContract.ProductEntry.COLUMN_ORDERED_QTY);
            int saleQTYColumnIndex = cursor.getColumnIndex(InventoryContract.ProductEntry.COLUMN_SALES_QTY);

            // Extract out the value from the Cursor for the given column index
            String productName = cursor.getString(nameColumnIndex);
            Integer productPrice = cursor.getInt(priceColumnIndex);
            byte[] productImage = cursor.getBlob(imageColumnIndex);
            Integer currentQTY = cursor.getInt(currentQTYColumnIndex);
            Integer saleQTY = cursor.getInt(saleQTYColumnIndex);

            // Update the views on the screen with the values from the database
            mNameEditText.setText(productName);
            mPriceEditText.setText(String.valueOf(productPrice));
            mOrderedQtyText.setText(String.valueOf(currentQTY));
            mSaleQtyText.setText(String.valueOf(saleQTY));

            if (productImage != null) {
                Bitmap bitmap = BitmapFactory.decodeByteArray(productImage, 0, productImage.length);
                mImageView.setImageBitmap(bitmap);
            } else {
                mImageView.setImageResource(R.mipmap.ic_launcher);
            }

        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

        // If the loader is invalidated, clear out all the data from the input fields.
        mNameEditText.setText("");
        mPriceEditText.setText("");
        mOrderedQtyText.setText("1");
        mImageView.setImageResource(R.mipmap.ic_launcher);
    }

    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mProductHasChanged = true;
            return false;
        }
    };

    private void saveProduct() {
        // Read from input fields
        // Use trim to eliminate leading or trailing white space
        String nameString = mNameEditText.getText().toString().trim();
        String priceString = mPriceEditText.getText().toString().trim();
        String orderedQTYString = mOrderedQtyText.getText().toString().trim();
        byte[] imageView = imageViewToByte(mImageView);


        if (mCurrentProductUri == null &&
                TextUtils.isEmpty(nameString) || TextUtils.isEmpty(priceString) ||
                TextUtils.isEmpty(orderedQTYString) || imageView == null) {
            Toast.makeText(this, getString(R.string.empty_value),
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // Create a ContentValues object where column names are the keys,
        // and product attributes from the editor are the values.
        ContentValues values = new ContentValues();
        values.put(InventoryContract.ProductEntry.COLUMN_PRODUCT_NAME, nameString);
        values.put(InventoryContract.ProductEntry.COLUMN_PRICE, priceString);
        values.put(InventoryContract.ProductEntry.COLUMN_ORDERED_QTY, orderedQTYString);
        values.put(InventoryContract.ProductEntry.COLUMN_IMAGE, imageView);
        // If the price is not provided by the user, don't try to parse the string into an
        // integer value. Use 0 by default.
        int price = 0;
        if (!TextUtils.isEmpty(priceString)) {
            price = Integer.parseInt(priceString);
        }
        values.put(InventoryContract.ProductEntry.COLUMN_PRICE, price);

        // Determine if this is a new or existing product by checking if mCurrentProductUri is null or not
        if (mCurrentProductUri == null) {
            // This is a NEW product, so insert a new product into the provider,
            // returning the content URI for the new product.
            Uri newUri = getContentResolver().insert(InventoryContract.ProductEntry.CONTENT_URI, values);

            // Show a toast message depending on whether or not the insertion was successful.
            if (newUri == null) {
                // If the new content URI is null, then there was an error with insertion.
                Toast.makeText(this, getString(R.string.editor_insert_product_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the insertion was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_insert_product_successful),
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            // Otherwise this is an EXISTING product, so update the product with content URI: mCurrentProductUri
            // and pass in the new ContentValues. Pass in null for the selection and selection args
            // because mCurrentProductUri will already identify the correct row in the database that
            // we want to modify.
            int rowsAffected = getContentResolver().update(mCurrentProductUri, values, null, null);

            // Show a toast message depending on whether or not the update was successful.
            if (rowsAffected == 0) {
                // If no rows were affected, then there was an error with the update.
                Toast.makeText(this, getString(R.string.editor_update_product_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the update was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_update_product_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    private byte[] imageViewToByte(ImageView image) {
        Bitmap bitmap = ((BitmapDrawable) image.getDrawable()).getBitmap();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 0, stream);
        byte[] byteArray = stream.toByteArray();
        return byteArray;
    }

    @Override
    public void onBackPressed() {
        // If the product hasn't changed, continue with handling back button press
        if (!mProductHasChanged) {
            super.onBackPressed();
            return;
        }

        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {


                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };

        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the pet.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new product, hide the "Delete" menu item.
        if (mCurrentProductUri == null) {
            MenuItem menuItem = menu.findItem(R.id.delete_item);
            menuItem.setVisible(false);
        }
        return true;
    }

    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the product.
                deleteProduct();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the pet.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Perform the deletion of the inventory in the database.
     */
    private void deleteProduct() {
        // Only perform the delete if this is an existing product.
        if (mCurrentProductUri != null) {
            // Call the ContentResolver to delete the product at the given content URI.
            // Pass in null for the selection and selection args because the mCurrentProductUri
            // content URI already identifies the product that we want.
            int rowsDeleted = getContentResolver().delete(mCurrentProductUri, null, null);

            // Show a toast message depending on whether or not the delete was successful.
            if (rowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the delete.
                Toast.makeText(this, getString(R.string.editor_delete_product_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_delete_product_successful),
                        Toast.LENGTH_SHORT).show();
            }

        }
        // Close the activity
        finish();
    }

    private void updateProduct() {
        TextView salesTextView = (TextView) findViewById(R.id.sales_txt);
        if (mCurrentProductUri != null) {
            String orderedQTYString = mOrderedQtyText.getText().toString().trim();
            String saleQTYString = mSaleQtyText.getText().toString().trim();
            orderQTY = Integer.parseInt(orderedQTYString);
            if (orderQTY != 0) {
                orderQTY -= 1;
                saleQTY = Integer.parseInt(saleQTYString);
                saleQTY += 1;
                salesTextView.setText(valueOf(saleQTY));
                ContentValues values = new ContentValues();
                values.put(InventoryContract.ProductEntry.COLUMN_ORDERED_QTY, orderQTY);
                values.put(InventoryContract.ProductEntry.COLUMN_SALES_QTY, saleQTY);

                int rowsUpdated = getContentResolver().update(mCurrentProductUri, values, null, null);

                if (rowsUpdated == 0) {
                    // If no rows were updated, then there was an error with the update.
                    Toast.makeText(this, getString(R.string.editor_update_product_failed),
                            Toast.LENGTH_SHORT).show();
                } else {
                    // Otherwise, the update was successful and we can display a toast.
                    Toast.makeText(this, getString(R.string.editor_update_product_successful),
                            Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Order quantity must not be less than 0!",
                        Toast.LENGTH_SHORT).show();
            }

        }

    }

    private String submitOrder() {
        String nameString = mNameEditText.getText().toString().trim();
        String priceString = mPriceEditText.getText().toString().trim();
        String orderedQTYString = mOrderedQtyText.getText().toString().trim();

        String orderSummary = "Hi! \n \nPlease provide this order:\n";
        orderSummary += "\nProduct Name: " + nameString;
        orderSummary += "\nPrice: " + priceString;
        orderSummary += "\nOrdered Quantity: " + orderedQTYString;
        orderSummary += "\n \nThank You.";

        return orderSummary;
    }
}
