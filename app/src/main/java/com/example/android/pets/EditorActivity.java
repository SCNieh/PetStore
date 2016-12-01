/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.pets;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;

import android.support.v4.app.NavUtils;
import android.content.Loader;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextPaint;
import android.text.TextUtils;

import android.view.Menu;
import android.view.MenuItem;

import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;


import com.example.android.pets.data.PetsContract.PetsEntry;


/**
 * Allows user to create a new pet or edit an existing one.
 */
public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    /** EditText field to enter the pet's name */
    private EditText mNameEditText;

    /** EditText field to enter the pet's breed */
    private EditText mBreedEditText;

    /** EditText field to enter the pet's weight */
    private EditText mWeightEditText;

    /** EditText field to enter the pet's gender */
    private Spinner mGenderSpinner;

    private Uri currentUri;

    private boolean mPetHasChanged = false;

    private static final int PET_LOADER = 0;

    private String iniPetName = "";
    private String iniPetBreed = "";
    private String iniPetWeight = "";
    private int iniPetGender = 0;

    /**
     * Gender of the pet. The possible values are:
     * 0 for unknown gender, 1 for male, 2 for female.
     */
    private int mGender = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        Intent intent = getIntent();
        currentUri = intent.getData();
        if(currentUri != null){
            setTitle("Edit a pet");

            getLoaderManager().initLoader(PET_LOADER, null, this);
        }else {
            setTitle("Add a pet");

            invalidateOptionsMenu();
        }

        // Find all relevant views that we will need to read user input from
        mNameEditText = (EditText) findViewById(R.id.edit_pet_name);
        mBreedEditText = (EditText) findViewById(R.id.edit_pet_breed);
        mWeightEditText = (EditText) findViewById(R.id.edit_pet_weight);
        mGenderSpinner = (Spinner) findViewById(R.id.spinner_gender);

        setupSpinner();
    }

    private void checkIfChanged(){
        String currentName = mNameEditText.getText().toString();
        String currentBreed = mBreedEditText.getText().toString();
        String currentWeight = mWeightEditText.getText().toString();
        mPetHasChanged = !(currentName.equals(iniPetName) && currentBreed.equals(iniPetBreed) && currentWeight.equals(iniPetWeight) && mGender == iniPetGender);
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
    public void onBackPressed() {

        checkIfChanged();

        if(!mPetHasChanged){
            super.onBackPressed();
            return;
        }

        DialogInterface.OnClickListener discardButtonClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        };
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    /**
     * Setup the dropdown spinner that allows the user to select the gender of the pet.
     */
    private void setupSpinner() {
        // Create adapter for spinner. The list options are from the String array it will use
        // the spinner will use the default layout
        ArrayAdapter genderSpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.array_gender_options, android.R.layout.simple_spinner_item);

        // Specify dropdown layout style - simple list view with 1 item per line
        genderSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

        // Apply the adapter to the spinner
        mGenderSpinner.setAdapter(genderSpinnerAdapter);

        // Set the integer mSelected to the constant values
        mGenderSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);

                if (!TextUtils.isEmpty(selection)) {
                    if (selection.equals(getString(R.string.gender_male))) {
                        mGender =PetsEntry.GENDER_MALE; // Male
                    } else if (selection.equals(getString(R.string.gender_female))) {
                        mGender = PetsEntry.GENDER_FEMALE; // Female
                    } else {
                        mGender = PetsEntry.GENDER_UNKNOWN; // Unknown
                    }
                }
            }

            // Because AdapterView is an abstract class, onNothingSelected must be defined
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mGender = 0; // Unknown
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new pet, hide the "Delete" menu item.
        if (currentUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    private boolean insertPet(){

        String nameString = mNameEditText.getText().toString().trim();
        String breedString = mBreedEditText.getText().toString().trim();
        String weightString = mWeightEditText.getText().toString().trim();
        ContentValues values = new ContentValues();
        values.put(PetsEntry.COLUMN_PET_NAME, nameString);
        values.put(PetsEntry.COLUMN_PET_BREED, breedString);
        values.put(PetsEntry.COLUMN_PET_WEIGHT, weightString);
        values.put(PetsEntry.COLUMN_PET_GENDER, mGender);

        Uri uri;
        if(currentUri == null){
            uri = getContentResolver().insert(PetsEntry.CONTENT_URI, values);
            if(uri != null){
                long pet_id  = ContentUris.parseId(uri);
                Toast.makeText(this, "Pet saved with id: " + pet_id, Toast.LENGTH_SHORT).show();
                return true;
            }
            else{
                return false;
            }
        }else {
            int rowUpdate = getContentResolver().update(currentUri, values, null, null);
            if(rowUpdate == 1){
                Toast.makeText(this, "Modificaiton applied", Toast.LENGTH_SHORT).show();
                return true;
            }
            else {
                return false;
            }
        }

    }

    private boolean deletePet(){
        int rowDelete = getContentResolver().delete(currentUri, null, null);
        if(rowDelete == 1){
            Toast.makeText(this, "Pet delete successfully", Toast.LENGTH_SHORT).show();
            return true;
        }else{
            return false;
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                if(insertPet()){
                    finish();
                }
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                // Do nothing for now
                if(deletePet()){
                    finish();
                }
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // Navigate back to parent activity (CatalogActivity)
                checkIfChanged();

                if(!mPetHasChanged){
                    NavUtils.navigateUpFromSameTask(this);
                    return true;
                }

                DialogInterface.OnClickListener discardButtonClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    }
                };
                showUnsavedChangesDialog(discardButtonClickListener);

                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this, currentUri, null, null, null, null);
    }

    private void updateUI(Cursor cursor){
        if(cursor == null || cursor.getCount() < 1){
            return;
        }
        cursor.moveToFirst();

        iniPetName = cursor.getString(cursor.getColumnIndex(PetsEntry.COLUMN_PET_NAME));
        iniPetBreed = cursor.getString(cursor.getColumnIndex(PetsEntry.COLUMN_PET_BREED));
        iniPetWeight = cursor.getString(cursor.getColumnIndex(PetsEntry.COLUMN_PET_WEIGHT));
        mNameEditText.setText(iniPetName);
        mBreedEditText.setText(iniPetBreed);
        mWeightEditText.setText(iniPetWeight);

        iniPetGender = cursor.getInt(cursor.getColumnIndex(PetsEntry.COLUMN_PET_GENDER));
        switch(iniPetGender){
            case PetsEntry.GENDER_FEMALE:
                mGender = PetsEntry.GENDER_FEMALE;
                mGenderSpinner.setSelection(2);
                break;
            case PetsEntry.GENDER_MALE:
                mGender = PetsEntry.GENDER_MALE;
                mGenderSpinner.setSelection(1);
                break;
            default:
                mGender = PetsEntry.GENDER_UNKNOWN;
                mGenderSpinner.setSelection(0);
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        updateUI(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mNameEditText.setText("");
        mBreedEditText.setText("");
        mWeightEditText.setText("");
        mGenderSpinner.setSelection(0);
    }
}