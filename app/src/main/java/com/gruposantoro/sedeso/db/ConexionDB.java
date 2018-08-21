package com.gruposantoro.sedeso.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by ULISES on 03/01/2018.
 */

public class ConexionDB extends SQLiteOpenHelper {

    private static final String DATABASE_NAME="sedeso";
    private static final int VERSION_NAME=1;

    public ConexionDB(Context context) {
        super(context, DATABASE_NAME, null, VERSION_NAME);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DataSource.ColumsUser.CREATE_TABLE_EVENTO);
        db.execSQL(DataSource.ColumsImg.CREATE_TABLE_IMG);
        db.execSQL(DataSource.ColumsActivacion.CREATE_TABLE_ACTIVA);
        db.execSQL(DataSource.ColumsVideo.CREATE_TABLE_VIDEO);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
