package com.example.android.pets.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.text.TextPaint;
import android.util.Log;
import android.widget.Toast;

import com.example.android.pets.EditorActivity;
import com.example.android.pets.R;
import com.example.android.pets.data.PetsContract.PetsEntry;

import static android.R.attr.cacheColorHint;
import static android.R.attr.id;
import static android.R.attr.permission;
import static android.R.attr.readPermission;
import static android.R.attr.requireDeviceUnlock;
import static android.R.attr.switchMinWidth;
import static android.R.attr.thickness;
import static android.R.attr.titleTextAppearance;
import static android.R.attr.value;
import static android.widget.Toast.makeText;

/**
 * Created by Shichao Nie on 2016/11/29.
 */

public class PetProvider extends ContentProvider {

    public static final String LOG_TAG = PetProvider.class.getSimpleName();
    private static Toast toast;

    /**
     * Initialize the provider and the database helper object.
     */
    private PetDbHelper mDbHelper;

    private static final int PETS = 100;

    /** URI matcher code for the content URI for a single pet in the pets table */
    private static final int PET_ID = 101;

    /**
     * UriMatcher object to match a content URI to a corresponding code.
     * The input passed into the constructor represents the code to return for the root URI.
     * It's common to use NO_MATCH as the input for this case.
     */
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    // Static initializer. This is run the first time anything is called from this class.
    static {
        // The calls to addURI() go here, for all of the content URI patterns that the provider
        // should recognize. All paths added to the UriMatcher have a corresponding code to return
        // when a match is found.

        // TODO: Add 2 content URIs to URI matcher
        sUriMatcher.addURI(PetsEntry.CONTENT_AUTHORITY, PetsEntry.PATH_PET, PETS);
        sUriMatcher.addURI(PetsEntry.CONTENT_AUTHORITY, PetsEntry.PATH_PET + "/#", PET_ID);
    }


    @Override
    public boolean onCreate() {
        // TODO: Create and initialize a PetDbHelper object to gain access to the pets database.
        // Make sure the variable is a global variable, so it can be referenced from other
        // ContentProvider methods.
        mDbHelper = new PetDbHelper(getContext());
        return true;
    }

    /**
     * Perform the query for the given URI. Use the given projection, selection, selection arguments, and sort order.
     */
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        // Get readable database
        SQLiteDatabase database = mDbHelper.getReadableDatabase();

        // This cursor will hold the result of the query
        Cursor cursor;

        // Figure out if the URI matcher can match the URI to a specific code
        int match = sUriMatcher.match(uri);
        switch (match) {
            case PETS:

                // TODO: Perform database query on pets table
                cursor = database.query(PetsEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);

                break;
            case PET_ID:

                selection = PetsEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };

                // This will perform a query on the pets table where the _id equals 3 to return a
                // Cursor containing that row of the table.
                cursor = database.query(PetsEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }

        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }


    /**
     * Insert new data into the provider with the given ContentValues.
     */
    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PETS:
                return insertPet(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    private boolean isLegal(ContentValues values){
        String name = values.getAsString(PetsEntry.COLUMN_PET_NAME);
        String breed = values.getAsString(PetsEntry.COLUMN_PET_BREED);
        int gender = values.getAsInteger(PetsEntry.COLUMN_PET_GENDER);
        String weight = values.getAsString(PetsEntry.COLUMN_PET_WEIGHT);
        if (name.isEmpty()) {
            //throw new IllegalArgumentException("Pet requires a name");
            if(toast != null){
                toast.setText(getContext().getResources().getString(R.string.require_name));
            }else {
                toast = Toast.makeText(getContext(), getContext().getResources().getString(R.string.require_name), Toast.LENGTH_SHORT);
            }
            toast.show();
            return false;
        }else if(breed.isEmpty()){
            //throw new IllegalArgumentException("Pet requires breed");
            if(toast != null){
                toast.setText(getContext().getResources().getString(R.string.require_breed));
            }else {
                toast = Toast.makeText(getContext(), getContext().getResources().getString(R.string.require_breed), Toast.LENGTH_SHORT);
            }
            toast.show();
            return false;
        }else if(weight.isEmpty()){
            //throw new IllegalArgumentException("Pet requires weight");
            if(toast != null){
                toast.setText(getContext().getResources().getString(R.string.require_weight));
            }else {
                toast = Toast.makeText(getContext(), getContext().getResources().getString(R.string.require_weight), Toast.LENGTH_SHORT);
            }
            toast.show();
            return false;
        }else if(gender != PetsEntry.GENDER_MALE && gender != PetsEntry.GENDER_FEMALE && gender != PetsEntry.GENDER_UNKNOWN){
            //throw new IllegalArgumentException("Pet gender is illegal");
            if(toast != null){
                toast.setText(getContext().getResources().getString(R.string.gender_illegal));
            }else {
                toast = Toast.makeText(getContext(), getContext().getResources().getString(R.string.gender_illegal), Toast.LENGTH_SHORT);
            }
            toast.show();
            return false;
        }
        else {
            return true;
        }
    }

    private Uri insertPet(Uri uri, ContentValues values) {

        // TODO: Insert a new pet into the pets database table with the given ContentValues

        // Once we know the ID of the new row in the table,
        // return the new URI with the ID appended to the end of it
        if(!isLegal(values)){
            return null;
        }

        // TODO: Finish sanity checking the rest of the attributes in ContentValues



        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        Long id = database.insert(PetsEntry.TABLE_NAME, null, values);

        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }
        getContext().getContentResolver().notifyChange(uri, null);

        return ContentUris.withAppendedId(uri, id);
    }

    /**
     * Updates the data at the given selection and selection arguments, with the new ContentValues.
     */
    @Override
    public int update(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {
        if(contentValues.size() == 0){
            return 0;
        }
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PETS:
                return updatePet(uri, contentValues, selection, selectionArgs);
            case PET_ID:
                // For the PET_ID code, extract out the ID from the URI,
                // so we know which row to update. Selection will be "_id=?" and selection
                // arguments will be a String array containing the actual ID.
                selection = PetsEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                return updatePet(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    /**
     * Update pets in the database with the given content values. Apply the changes to the rows
     * specified in the selection and selection arguments (which could be 0 or 1 or more pets).
     * Return the number of rows that were successfully updated.
     */
    private int updatePet(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

       if(!isLegal(values)){
           return -1;
       }
        // TODO: Update the selected pets in the pets database table with the given ContentValues

            SQLiteDatabase database = mDbHelper.getWritableDatabase();
        int rows = database.update(PetsEntry.TABLE_NAME, values, selection, selectionArgs);

        getContext().getContentResolver().notifyChange(uri, null);

        // TODO: Return the number of rows that were affected
        return rows;
    }

    /**
     * Delete the data at the given selection and selection arguments.
     */
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PETS:
                // Delete all rows that match the selection and selection args
                int rows = database.delete(PetsEntry.TABLE_NAME, selection, selectionArgs);
                getContext().getContentResolver().notifyChange(uri, null);
                return rows;
            case PET_ID:
                // Delete a single row given by the ID in the URI
                selection = PetsEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                rows = database.delete(PetsEntry.TABLE_NAME, selection, selectionArgs);
                //database.execSQL("UPDATE sqlite_sequence SET seq = 0 WHERE NAME = " + "'" + PetsEntry.TABLE_NAME + "'");
                getContext().getContentResolver().notifyChange(uri, null);
                return rows;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }
    }

    /**
     * Returns the MIME type of data for the content URI.
     */
    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch(match){
            case PETS:
                return PetsEntry.CONTENT_LIST_TYPE;
            case PET_ID:
                return PetsEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }
}

