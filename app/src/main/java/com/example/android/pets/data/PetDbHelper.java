package com.example.android.pets.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.example.android.pets.data.PetsContract.PetsEntry;


/**
 * Created by Shichao Nie on 2016/11/28.
 */

public class PetDbHelper extends SQLiteOpenHelper {

    public static int DATABASE_VERSION = 2;
    public static final String DATABASE_NAME = "shelter.db";
    public static final String SQL_CREATE_PETS_TABLE =  "CREATE TABLE " + PetsEntry.TABLE_NAME + " ("
            + PetsEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + PetsEntry.COLUMN_PET_NAME + " TEXT NOT NULL, "
            + PetsEntry.COLUMN_PET_BREED + " TEXT, "
            + PetsEntry.COLUMN_PET_GENDER + " INTEGER NOT NULL, "
            + PetsEntry.COLUMN_PET_WEIGHT + " INTEGER NOT NULL DEFAULT 0);";
    public static final String SQL_DELETE_PETS_TABLE = "DROP TABLE " + PetsEntry.TABLE_NAME;

    public PetDbHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL(SQL_CREATE_PETS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        db.execSQL(SQL_DELETE_PETS_TABLE);
        onCreate(db);
    }
}
