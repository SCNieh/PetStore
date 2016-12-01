package com.example.android.pets;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.content.CursorLoader;
import android.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.android.pets.data.PetDbHelper;

import com.example.android.pets.data.PetsContract.PetsEntry;

/**
 * Displays list of pets that were entered and stored in the app.
 */
public class CatalogActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private Cursor cursor;
    private ListView list;
    private PetCursorAdapter adapter;
    private static final int PET_LOADER = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);

        // Setup FAB to open EditorActivity
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });


        list = (ListView) findViewById(R.id.list);
        View emptyView = findViewById(R.id.empty_view);
        list.setEmptyView(emptyView);

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);

                Uri currenPetUri = ContentUris.withAppendedId(PetsEntry.CONTENT_URI, id);

                intent.setData(currenPetUri);
                startActivity(intent);

            }
        });
        adapter = new PetCursorAdapter(this, null);
        list.setAdapter(adapter);

        getLoaderManager().initLoader(PET_LOADER, null, this);
    }

    /**
     * Temporary helper method to display information in the onscreen TextView about the state of
     * the pets database.
     */
//    private String newPetInfo() {
//        int _id = cursor.getInt(cursor.getColumnIndex(PetsEntry._ID));
//        String petName = cursor.getString(cursor.getColumnIndex(PetsEntry.COLUMN_PET_NAME));
//        String petBreed = cursor.getString(cursor.getColumnIndex(PetsEntry.COLUMN_PET_BREED));
//        String petWeigth = cursor.getString(cursor.getColumnIndex(PetsEntry.COLUMN_PET_WEIGHT));
//        String info = _id + " - " + petName + " - " + petBreed + " - " + petWeigth + "Kg" + "\n";
//        return info;
//    }
//    private void displayDatabaseInfo() {
//
//
//        /**TextView textVersion = (TextView)findViewById(R.id.text_version);
//         textVersion.setText("Current version is " + Integer.toString(db.getVersion()));
//         */
//        // Perform this raw SQL query "SELECT * FROM pets"
//        // to get a Cursor that contains all rows from the pets table.
//        list = (ListView) findViewById(R.id.list);
//        View emmptyView = findViewById(R.id.empty_view);
//        list.setEmptyView(emmptyView);
//
//        cursor = getContentResolver().query(PetsEntry.CONTENT_URI, null, null, null, null);
//        adapter = new PetCursorAdapter(this, cursor);
//
//        list.setAdapter(adapter);
//
//
//       // cursor.close();
//
//    }
    private void insertPet() {

        ContentValues values = new ContentValues();
        values.put(PetsEntry.COLUMN_PET_NAME, "Toto");
        values.put(PetsEntry.COLUMN_PET_BREED, "Terrier");
        values.put(PetsEntry.COLUMN_PET_GENDER, PetsEntry.GENDER_MALE);
        values.put(PetsEntry.COLUMN_PET_WEIGHT, "7");

        getContentResolver().insert(PetsEntry.CONTENT_URI, values);

        // displayDatabaseInfo();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_catalog.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_catalog, menu);
        return true;
    }

    private void deletePet() {
        getContentResolver().delete(PetsEntry.CONTENT_URI, null, null);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Insert dummy data" menu option
            case R.id.action_insert_dummy_data:
                insertPet();
                return true;
            // Respond to a click on the "Delete all entries" menu option
            case R.id.action_delete_all_entries:
                deletePet();
                //    displayDatabaseInfo();
                // Do nothing for now
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this, PetsEntry.CONTENT_URI, null, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        adapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.swapCursor(null);
    }
}
