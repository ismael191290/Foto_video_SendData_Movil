package com.gruposantoro.sedeso.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.StrictMode;
import android.provider.BaseColumns;
import android.support.annotation.Nullable;

import com.gruposantoro.sedeso.services.Video;

import org.json.JSONArray;

/**
 * Created by ULISES on 27/04/2017.
 */

public class DataSource {

    public static final String STRING_TYPE = "text";
    public static final String INT_TYPE = "integer";
    public static final String DOUBLE_TYPE = "real";
    public static final String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS";
    public static final String PRIMARY_KEY = " PRIMARY KEY AUTOINCREMENT NOT NULL, ";
    public static final String NOT_NULL = " NOT NULL, ";
    public static final String TABLE_EVENTO = "t_evento";
    public static final String TABLE_ACTIVA = "t_activa";
    public static final String TABLE_IMG_DATA = "t_imagen";
    public static final String TABLE_VIDEO_DATA = "t_video";
    private ConexionDB openHelper;
    private SQLiteDatabase database;

    public DataSource(Context context) {
        checkStrickMode();
        openHelper = new ConexionDB(context);
        database = openHelper.getWritableDatabase();
    }

    public void closeDataBase() {
        database.close();
    }

    public static class ColumsVideo {
        public static final String NAME = "c_name";
        public static final String METROS = "c_mts";
        public static final String DOMICILIO = "c_domicilio";
        public static final String OBSERVACION = "c_obs";
        public static final String LAT = "c_lat";
        public static final String LANG = "c_lang";
        public static final String FECHA = "c_fecha";
        public static final String VIDEO = "c_video";

        public static final String CREATE_TABLE_VIDEO =
                CREATE_TABLE + " " + TABLE_VIDEO_DATA + "(" +
                        BaseColumns._ID + " " + INT_TYPE + PRIMARY_KEY +
                        NAME + " " + STRING_TYPE + NOT_NULL + METROS + " " + STRING_TYPE + NOT_NULL +
                        DOMICILIO + " " + STRING_TYPE + NOT_NULL + OBSERVACION + " " + STRING_TYPE +
                        NOT_NULL + LAT + " " + STRING_TYPE + NOT_NULL + LANG + " " + STRING_TYPE +
                        NOT_NULL + FECHA + " " + STRING_TYPE + NOT_NULL + VIDEO + " "+ STRING_TYPE + " NOT NULL " + ")";
    }

    public static class ColumsUser {
        public static final String EVENTO_ID = "c_evento";
        public static final String USUARIO_ID = "c_usuario";

        public static final String CREATE_TABLE_EVENTO =
                CREATE_TABLE + " " + TABLE_EVENTO + "(" +
                        BaseColumns._ID + " " + INT_TYPE + PRIMARY_KEY +
                        EVENTO_ID + " " + STRING_TYPE + NOT_NULL + USUARIO_ID + " " + INT_TYPE + " NOT NULL " + ")";
    }

    public static class ColumsActivacion {
        public static final String ACTIVA_ID = "c_activacion";

        public static final String CREATE_TABLE_ACTIVA =
                CREATE_TABLE + " " + TABLE_ACTIVA + "(" +
                        BaseColumns._ID + " " + INT_TYPE + PRIMARY_KEY +
                        ACTIVA_ID + " " + STRING_TYPE + " NOT NULL " + ")";
    }

    public static class ColumsImg {
        public static final String DATA = "c_data";

        public static final String CREATE_TABLE_IMG =
                CREATE_TABLE + " " + TABLE_IMG_DATA + "(" +
                        BaseColumns._ID + " " + INT_TYPE + PRIMARY_KEY +
                        DATA + " " + STRING_TYPE + " NOT NULL " + ")";
    }

    public void insertEvnto(String id, int userId) {
        ContentValues content = new ContentValues();
        content.put(ColumsUser.EVENTO_ID, id);
        content.put(ColumsUser.USUARIO_ID, userId);
        database.insert(TABLE_EVENTO, null, content);
    }

    public void insertActiva(String id) {
        ContentValues content = new ContentValues();
        content.put(ColumsActivacion.ACTIVA_ID, id);
        database.insert(TABLE_ACTIVA, null, content);
    }

    public void insertImg(String data) {
        ContentValues content = new ContentValues();
        content.put(ColumsImg.DATA, data);
        database.insert(TABLE_IMG_DATA, null, content);
    }

    public void insertVideo(@Nullable String nombre, @Nullable String mts,
                            @Nullable String lat, @Nullable String lang, @Nullable String domicilio,
                            @Nullable String observa, @Nullable String video,String fecha) {
        ContentValues content = new ContentValues();
        content.put(ColumsVideo.NAME, nombre);
        content.put(ColumsVideo.METROS, mts);
        content.put(ColumsVideo.LAT, lat);
        content.put(ColumsVideo.LANG, lang);
        content.put(ColumsVideo.DOMICILIO, domicilio);
        content.put(ColumsVideo.OBSERVACION, observa);
        content.put(ColumsVideo.VIDEO, video);
        content.put(ColumsVideo.FECHA, fecha);
        database.insert(TABLE_VIDEO_DATA, null, content);
    }

    public void deleteVideo() {
        database.delete(TABLE_VIDEO_DATA, null
                , null);
    }

    public Cursor video() {
        return database.query(TABLE_VIDEO_DATA, new String[]{BaseColumns._ID, ColumsVideo.NAME,ColumsVideo.METROS,
                ColumsVideo.DOMICILIO,ColumsVideo.LAT,ColumsVideo.LANG,ColumsVideo.OBSERVACION,
                ColumsVideo.VIDEO}, null, null, null, null, null);
    }

    public Cursor data() {
        return database.query(TABLE_IMG_DATA, new String[]{BaseColumns._ID, ColumsImg.DATA}, null, null, null, null, null);
    }

    public String idEvent() {
        String s = "";
        Cursor c = database.query(TABLE_EVENTO, new String[]{ColumsUser.EVENTO_ID}, null, null, null, null, null);
        if (c.moveToNext()) {
            s = c.getString(0);
        }
        c.close();
        return s;
    }

    public String idActiva() {
        String s = "";
        Cursor c = database.query(TABLE_ACTIVA, new String[]{ColumsActivacion.ACTIVA_ID}, null, null, null, null, null);
        if (c.moveToNext()) {
            s = c.getString(0);
        }
        c.close();
        return s;
    }

    public int idUser() {
        int s = 0;
        Cursor c = database.query(TABLE_EVENTO, new String[]{ColumsUser.USUARIO_ID}, null, null, null, null, null);
        if (c.moveToNext()) {
            s = c.getInt(0);
        }
        c.close();
        return s;
    }

    public void deleteEvent() {
        database.delete(TABLE_EVENTO, null, null);
    }

    public void deleteImg(int id) {
        database.delete(TABLE_IMG_DATA, BaseColumns._ID + "=?", new String[]{"" + id});
    }

    public void deleteImg() {
        database.delete(TABLE_IMG_DATA, null, null);
    }

    public void deleteActiva() {
        database.delete(TABLE_ACTIVA, null, null);
    }

    // OBTIENE LA VERSION DEL CELULAR
    public boolean version() {
        int currentapiVersion = Build.VERSION.SDK_INT;
        if (currentapiVersion > Build.VERSION_CODES.HONEYCOMB) {
            // Toast.makeText(this, "Es mayor", Toast.LENGTH_LONG).show();
            return true;
        } else if (currentapiVersion <= Build.VERSION_CODES.HONEYCOMB) {
            // Toast.makeText(this, "es menor", Toast.LENGTH_LONG).show();
            return false;
        }
        return false;
    }

    public void checkStrickMode() {
        if (version()) {
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                    .detectDiskReads().detectDiskWrites().detectNetwork()
                    .penaltyLog().build());
            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                    .detectLeakedSqlLiteObjects().detectLeakedClosableObjects()
                    .penaltyLog().penaltyDeath().build());
        }
    }
}
