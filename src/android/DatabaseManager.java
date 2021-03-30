package com.tasca.android;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;
import android.util.Log;

import com.tasca.android.data.DatabaseItem;
import com.tasca.android.data.Estate;
import com.tasca.android.data.ImageRecord;
import com.tasca.android.data.Taste;
import com.tasca.android.data.Wine;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

/**
 * Created by carlo on 02/08/2017.
 */

public class DatabaseManager extends SQLiteOpenHelper
{
    private static DatabaseManager instance;
    private SQLiteDatabase db;
    private Context context;

    private static final String DB_NAME = "tasca.db";
    private static final int DB_VERSION = 2;

    private DatabaseManager(Context context)
    {
        super(context, DB_NAME, null, DB_VERSION);
        db = getWritableDatabase();
    }

    public static final DatabaseManager getInstance(Context context)
    {
        if (instance == null) {
            instance = new DatabaseManager(context);
        }
        instance.context = context;
        return instance;
    }


    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase)
    {
        sqLiteDatabase.execSQL(Wine.TABLE_CREATE);
        sqLiteDatabase.execSQL(Estate.TABLE_CREATE);
        sqLiteDatabase.execSQL(Taste.TABLE_CREATE);
        sqLiteDatabase.execSQL(ImageRecord.TABLE_CREATE);

        Log.d("TABLE_CREATE", "WINE TABLE: " + Wine.TABLE_CREATE + "\n" +
                "ESTATE TABLE: " + Estate.TABLE_CREATE + "\n" +
                "TASTES TABLE: " + Taste.TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion)
    {
        if (oldVersion < newVersion) {
            switch (oldVersion) {
                case 1:
                    sqLiteDatabase.execSQL(ImageRecord.TABLE_CREATE);
            }
        }

    }

    // LAST UPDATE METHODS

    /**
     * Get the last modified timestamp record in millis for the param table
     * return -1 if no record was found
     *
     * @param table             the table to check for the last record
     * @param requestedLanguage optional language, if null the device default will be used
     * @return last modified timestamp for the table or -1 if not found;
     */
    private long getLastRecordTimestamp(String table, String requestedLanguage)
    {
        long lastTimestamp = -1;
        String language = Utils.getLanguage(requestedLanguage);

        Cursor dateCursor = db.query(table,
                new String[]{DatabaseItem.COLUMN_MODIFIED_GMT},
                DatabaseItem.COLUMN_LANGUAGE + " = ?",
                new String[]{language},
                null,
                null,
                DatabaseItem.COLUMN_MODIFIED_GMT + " DESC",
                "1");

        if (dateCursor.moveToFirst()) {
            String timeString = dateCursor.getString(0);
            lastTimestamp = Utils.dateStringToMillis(timeString);
        }
        dateCursor.close();

        return lastTimestamp;
    }

    /**
     * Get the last modified timestamp record in millis for the Wines table
     * return -1 if no record was found
     *
     * @param requestedLanguage optional language, if null the device default will be used
     * @return last modified timestamp for the Wines table or -1 if not found;
     */
    public long getLastWineTimestamp(String requestedLanguage)
    {
        return getLastRecordTimestamp(Wine.TABLE_NAME, requestedLanguage);
    }

    /**
     * Get the last modified timestamp record in millis for the Estates table
     * return -1 if no record was found
     *
     * @param requestedLanguage optional language, if null the device default will be used
     * @return last modified timestamp for the Wines table or -1 if not found;
     */
    public long getLastEstateTimestamp(String requestedLanguage)
    {
        return getLastRecordTimestamp(Estate.TABLE_NAME, requestedLanguage);
    }

    // QUERY METHODS

    /**
     * Return the single Wine json by id, return null if not found or not a valid json
     *
     * @param language optional language param, if null or empty the device default will be used
     * @param id       the wine id
     * @return a {@link JSONObject} representing the wine or null if not found/not valid
     */
    public JSONObject getWineById(String language, String id)
    {
        language = Utils.getLanguage(language);

        JSONObject wineJson = null;
        Cursor cursor = db.query(Wine.TABLE_NAME,
                new String[]{Wine.COLUMN_RAW_JSON},
                Wine.COLUMN_LANGUAGE + " = ? AND " + Wine.COLUMN_ID + " = ?",
                new String[]{language, id},
                null,
                null,
                null);
        if (cursor.moveToFirst()) {
            try {
                wineJson = new JSONObject(cursor.getString(cursor.getColumnIndex(Wine.COLUMN_RAW_JSON)));
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
        cursor.close();
        return wineJson;
    }

    /**
     * return an array containing the term inside all the exposed
     * {@link com.tasca.android.data.Wine} database columns max 10 results
     * for each row only the title and the id will be returned
     * (eg. [{"id" = 1, "title" = "winename"}])
     *
     * @param language the optional language, if null the device default will be used
     * @param term     search term, will be searched on all the columns exposed
     * @return a {@link JSONArray} containing id and title for each wine, max 10 results
     */
    public JSONArray getWineTypeAhead(String language, String term)
    {
        JSONArray wineArray = new JSONArray();
        language = Utils.getLanguage(language);

        String where = null;
        String[] whereClause = null;
        if (!TextUtils.isEmpty(term)) {
            where = getWineWhereFromTerm(language);
            whereClause = new String[Wine.EXPOSED_COLUMNS.length];
            for (int i = 0; i < whereClause.length; i++) {
                whereClause[i] = "%" + term + "%";
            }
        } else {
            where = Wine.COLUMN_LANGUAGE + " = '" + language + "'";
        }

        Cursor cursor = db.query(Wine.TABLE_NAME,
                new String[]{Wine.COLUMN_ID, Wine.COLUMN_TITLE},
                where,
                whereClause,
                null,
                null,
                Wine.COLUMN_TITLE + " ASC",
                "10");

        while (cursor.moveToNext()) {
            JSONObject typeAheadJson = new JSONObject();
            try {
                typeAheadJson.put("id", cursor.getLong(cursor.getColumnIndex(Wine.COLUMN_ID)));
                typeAheadJson.put("title", cursor.getString(cursor.getColumnIndex(Wine.COLUMN_TITLE)));
                wineArray.put(typeAheadJson);
            } catch (JSONException e) {
                e.printStackTrace();
                continue;
            }
        }

        //// TODO: 13/08/2017 add Levensthein sorting
        return wineArray;
    }

    /**
     * return an array containing all the {@link com.tasca.android.data.Wine}
     * that meets the param specifications.
     *
     * @param language      the optional language, if null the device default will be used
     * @param term          the optional search term, will be searched on all the columns exposed
     * @param limit         the optional limit (pass -1 for no limit)
     * @param offset        the optional offset (pass -1 for no offset)
     * @param sort          the optional sort column, the sortable column are contained on
     *                      {@link com.tasca.android.data.Wine}
     * @param sortDirection sorting order boolean, true for asc, false for desc
     * @return a {@link JSONArray} containing all the wine that meets the params specification
     */
    public JSONArray getWines(String language, String term, int limit, int offset, String sort,
                              boolean sortDirection)
    {
        JSONArray wineArray = new JSONArray();
        language = Utils.getLanguage(language);

        String where = null;
        String[] whereClause = null;
        if (!TextUtils.isEmpty(term)) {
            where = getWineWhereFromTerm(language);
            whereClause = new String[Wine.EXPOSED_COLUMNS.length];
            for (int i = 0; i < whereClause.length; i++) {
                whereClause[i] = "%" + term + "%";
            }
        } else {
            where = Wine.COLUMN_LANGUAGE + " = '" + language + "'";
        }

        String limitString = null;
        if (limit >= 0 && offset >= 0) {
            limitString = offset + "," + limit;
        } else if (limit >= 0) {
            limitString = String.valueOf(limit);
        }

        String sortString = null;
        if (!TextUtils.isEmpty(sort)) {
            for (String validSort : Wine.EXPOSED_COLUMNS) {
                if (sort.contains(validSort)) {
                    sortString = validSort + " " + (sortDirection ? "ASC" : "DESC");
                    break;
                }
            }
        }

        Cursor cursor = db.query(Wine.TABLE_NAME,
                new String[]{Wine.COLUMN_RAW_JSON},
                where,
                whereClause,
                null,
                null,
                sortString,
                limitString);

        while (cursor.moveToNext()) {
            try {
                wineArray.put(new JSONObject(cursor.getString(cursor.getColumnIndex(Wine.COLUMN_RAW_JSON))));
            } catch (JSONException e) {
                e.printStackTrace();
                //not a valid json, ignoring
            }
        }
        cursor.close();
        return wineArray;
    }

    /**
     * private method to retreive the where clause by Exposed columns
     *
     * @param language mandatory, a valid language String
     * @return a where clause constructed using the Exposed Wine columns
     */
    private String getWineWhereFromTerm(String language)
    {
        String where = Wine.COLUMN_LANGUAGE + " = '" + language + "' AND ( ";
        for (int i = 0; i < Wine.EXPOSED_COLUMNS.length; i++) {
            where += Wine.EXPOSED_COLUMNS[i] + " LIKE ?";
            if (i < Wine.EXPOSED_COLUMNS.length - 1) {
                where += " OR ";
            }
        }
        where += " )";
        return where;
    }

    public JSONObject getEstateById(String language, String id)
    {
        language = Utils.getLanguage(language);

        JSONObject estateJson = null;
        Cursor cursor = db.query(Estate.TABLE_NAME,
                new String[]{Wine.COLUMN_RAW_JSON},
                Estate.COLUMN_LANGUAGE + " = ? AND " + Estate.COLUMN_ID + " = ?",
                new String[]{language, id},
                null,
                null,
                null);
        if (cursor.moveToFirst()) {
            try {
                estateJson = new JSONObject(cursor.getString(
                        cursor.getColumnIndex(Estate.COLUMN_RAW_JSON)));
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
        cursor.close();
        return estateJson;
    }

    /**
     * return an array containing the term inside all the exposed
     * {@link com.tasca.android.data.Estate} database columns max 10 results
     * for each row only the title and the id will be returned
     * (eg. [{"id" = 1, "title" = "estatename"}])
     *
     * @param language the optional language, if null the device default will be used
     * @param term     search term, will be searched on all the columns exposed
     * @return a {@link JSONArray} containing id and title for each estate, max 10 results
     */
    public JSONArray getEstateTypeAhead(String language, String term)
    {
        JSONArray estateArray = new JSONArray();
        language = Utils.getLanguage(language);

        String where = null;
        String[] whereClause = null;
        if (!TextUtils.isEmpty(term)) {
            where = getEstateWhereFromTerm(language);
            whereClause = new String[Estate.EXPOSED_COLUMNS.length];
            for (int i = 0; i < whereClause.length; i++) {
                whereClause[i] = "%" + term + "%";
            }
        } else {
            where = Estate.COLUMN_LANGUAGE + " = '" + language + "'";
        }

        Cursor cursor = db.query(Estate.TABLE_NAME,
                new String[]{Estate.COLUMN_ID, Estate.COLUMN_TITLE},
                where,
                whereClause,
                null,
                null,
                Estate.COLUMN_TITLE + " ASC",
                "10");

        while (cursor.moveToNext()) {
            JSONObject typeAheadJson = new JSONObject();
            try {
                typeAheadJson.put("id", cursor.getLong(cursor.getColumnIndex(Estate.COLUMN_ID)));
                typeAheadJson.put("title", cursor.getString(cursor.getColumnIndex(Estate.COLUMN_TITLE)));
                estateArray.put(typeAheadJson);
            } catch (JSONException e) {
                e.printStackTrace();
                //not valid son, ignore
            }
        }
        cursor.close();

        //// TODO: 13/08/2017 add Levensthein sorting
        return estateArray;
    }

    /**
     * return an array containing all the {@link com.tasca.android.data.Estate}
     * that meets the param specifications.
     *
     * @param language      the optional language, if null the device default will be used
     * @param term          the optional search term, will be searched on all the columns exposed
     * @param limit         the optional limit (pass -1 for no limit)
     * @param offset        the optional offset (pass -1 for no offset)
     * @param sort          the optional sort column, the sortable column are contained on
     *                      {@link com.tasca.android.data.Estate}
     * @param sortDirection sorting order boolean, true for asc, false for desc
     * @return a {@link JSONArray} containing all the estate that meets the params specification
     */
    public JSONArray getEstates(String language, String term, int limit, int offset, String sort,
                                boolean sortDirection)
    {
        JSONArray estatesArray = new JSONArray();
        language = Utils.getLanguage(language);

        String where = null;
        String[] whereClause = null;
        if (!TextUtils.isEmpty(term)) {
            where = getEstateWhereFromTerm(language);
            whereClause = new String[Estate.EXPOSED_COLUMNS.length];
            for (int i = 0; i < whereClause.length; i++) {
                whereClause[i] = "%" + term + "%";
            }
        } else {
            where = Estate.COLUMN_LANGUAGE + " = '" + language + "'";
        }

        String limitString = null;
        if (limit >= 0 && offset >= 0) {
            limitString = offset + "," + limit;
        } else if (limit >= 0) {
            limitString = String.valueOf(limit);
        }

        String sortString = null;
        if (!TextUtils.isEmpty(sort)) {
            for (String validSort : Estate.EXPOSED_COLUMNS) {
                if (sort.contains(validSort)) {
                    sortString = validSort + " " + (sortDirection ? "ASC" : "DESC");
                    break;
                }
            }
        }

        Cursor cursor = db.query(Estate.TABLE_NAME,
                new String[]{Estate.COLUMN_RAW_JSON},
                where,
                whereClause,
                null,
                null,
                sortString,
                limitString);

        while (cursor.moveToNext()) {
            try {
                estatesArray.put(new JSONObject(cursor.getString(cursor.getColumnIndex(Estate.COLUMN_RAW_JSON))));
            } catch (JSONException e) {
                e.printStackTrace();
                continue;
            }
        }
        return estatesArray;
    }

    /**
     * private method for generating the Estate where string for the exposed columns
     *
     * @param language
     * @return
     */
    private String getEstateWhereFromTerm(String language)
    {
        String where = Estate.COLUMN_LANGUAGE + " = '" + language + "' AND ( ";
        for (int i = 0; i < Estate.EXPOSED_COLUMNS.length; i++) {
            where += Estate.EXPOSED_COLUMNS[i] + " LIKE ?";
            if (i < Estate.EXPOSED_COLUMNS.length - 1) {
                where += " OR ";
            }
        }
        where += " )";
        return where;
    }

    /**
     * Return the single Taste json by id, return null if not found or not a valid json
     *
     * @param id the wine id
     * @return a {@link JSONObject} representing the wine or null if not found/not valid
     */
    public JSONObject getTasteById(String id)
    {
        JSONObject tasteJson = null;
        Cursor cursor = db.query(Wine.TABLE_NAME,
                new String[]{Wine.COLUMN_RAW_JSON},
                Taste.COLUMN_ID + " = ?",
                new String[]{id},
                null,
                null,
                null);
        if (cursor.moveToFirst()) {
            try {
                tasteJson = new JSONObject(cursor.getString(cursor.getColumnIndex(Wine.COLUMN_RAW_JSON)));
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
        cursor.close();
        return tasteJson;
    }

    /**
     * return an array containing the term inside all the exposed
     * {@link Taste} database columns max 10 results
     * for each row only the name and the id will be returned
     * (eg. [{"id" = 1, "name" = "tastename"}])
     *
     * @param term search term, will be searched on all the columns exposed
     * @return a {@link JSONArray} containing id and name for each taste, max 10 results
     */
    public JSONArray getTasteTypeAhead(String term)
    {
        JSONArray tasteArray = new JSONArray();

        String where = null;
        String[] whereClause = null;
        if (!TextUtils.isEmpty(term)) {
            where = getTasteWhereFromTerm();
            whereClause = new String[Taste.EXPOSED_COLUMNS.length];
            for (int i = 0; i < whereClause.length; i++) {
                whereClause[i] = "%" + term + "%";
            }
        }

        Cursor cursor = db.query(Taste.TABLE_NAME,
                new String[]{Taste.COLUMN_ID, Taste.COLUMN_NAME},
                where,
                whereClause,
                null,
                null,
                Taste.COLUMN_NAME + " ASC",
                "10");

        while (cursor.moveToNext()) {
            JSONObject typeAheadJson = new JSONObject();
            try {
                typeAheadJson.put("id", cursor.getLong(cursor.getColumnIndex(Taste.COLUMN_ID)));
                typeAheadJson.put("name", cursor.getString(cursor.getColumnIndex(Taste.COLUMN_NAME)));
                tasteArray.put(typeAheadJson);
            } catch (JSONException e) {
                e.printStackTrace();
                continue;
            }
        }

        //// TODO: 13/08/2017 add Levensthein sorting
        return tasteArray;
    }

    /**
     * return an array containing all the {@link Taste}
     * that meets the param specifications.
     *
     * @param term          the optional search term, will be searched on all the columns exposed
     * @param limit         the optional limit (pass -1 for no limit)
     * @param offset        the optional offset (pass -1 for no offset)
     * @param sort          the optional sort column, the sortable column are contained on
     *                      {@link Taste}
     * @param sortDirection sorting order boolean, true for asc, false for desc
     * @return a {@link JSONArray} containing all the tastes that meets the params specification
     */
    public JSONArray getTastes(String term, int limit, int offset, String sort,
                               boolean sortDirection)
    {
        JSONArray tasteArray = new JSONArray();

        String where = null;
        String[] whereClause = null;
        if (!TextUtils.isEmpty(term)) {
            where = getTasteWhereFromTerm();
            whereClause = new String[Taste.EXPOSED_COLUMNS.length];
            for (int i = 0; i < whereClause.length; i++) {
                whereClause[i] = "%" + term + "%";
            }
        }

        String limitString = null;
        if (limit >= 0 && offset >= 0) {
            limitString = offset + "," + limit;
        } else if (limit >= 0) {
            limitString = String.valueOf(limit);
        }

        String sortString = null;
        if (!TextUtils.isEmpty(sort)) {
            for (String validSort : Taste.EXPOSED_COLUMNS) {
                if (sort.contains(validSort)) {
                    sortString = validSort + " " + (sortDirection ? "ASC" : "DESC");
                    break;
                }
            }
        }

        Cursor cursor = db.query(Taste.TABLE_NAME,
                new String[]{Taste.COLUMN_RAW_JSON},
                where,
                whereClause,
                null,
                null,
                sortString,
                limitString);

        while (cursor.moveToNext()) {
            try {
                tasteArray.put(new JSONObject(cursor.getString(cursor.getColumnIndex(Taste.COLUMN_RAW_JSON))));
            } catch (JSONException e) {
                e.printStackTrace();
                continue;
            }
        }
        return tasteArray;
    }

    /**
     * private method to retreive the where clause by Exposed columns
     *
     * @return a well formatted where String
     */
    private String getTasteWhereFromTerm()
    {
        String where = "";
        for (int i = 0; i < Taste.EXPOSED_COLUMNS.length; i++) {
            where += Taste.EXPOSED_COLUMNS[i] + " LIKE ?";
            if (i < Taste.EXPOSED_COLUMNS.length - 1) {
                where += " OR ";
            }
        }
        return where;
    }

    /**
     * return a JSONObject containing all the progect images as a Json Object, where the key is the
     * remote url, and the value is the local url, if not present, the local url will be an empty
     * String
     * @return the JSONObject mapping all the project images
     */
    public JSONObject getImagesMap()
    {
        return getImagesMap(false);
    }

    /**
     * return a JSONObject containing all the progect images as a Json Object, where the key is the
     * remote url, and the value is the local url, if not present, the local url will be an empty
     * String
     * @param incompleteOnly  if this flag is true only the record without local urls will be included
     * @return the JSONObject mapping all the project images
     */
    public JSONObject getImagesMap(boolean incompleteOnly)
    {
        JSONObject imageMap = new JSONObject();
        String where = incompleteOnly ? ImageRecord.COLUMN_LOCAL + " == ?" : null;
        String[] whereArgs = incompleteOnly ? new String[]{""} : null;
        Cursor cursor = db.query(ImageRecord.TABLE_NAME, null, where, whereArgs, null, null, null);
        if(cursor != null) {
            while(cursor.moveToNext()) {
                try {
                    String remote = cursor.getString(cursor.getColumnIndex(ImageRecord.COLUMN_REMOTE));
                    String local = cursor.getString(cursor.getColumnIndex(ImageRecord.COLUMN_LOCAL));
                    imageMap.put(remote, local);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            cursor.close();
        }

        return imageMap;
    }

    // INSERT METHODS

    /**
     * insert the tasting into the db, if a record with the same ID is present it will be overwritten
     * @param tastingJson the jsonString for the single taste
     * @return the numeric id of the insert or -1 if the insert failed
     */
    public long insertTaste(JSONObject tastingJson)
    {
        ContentValues values = Taste.toContentValue(tastingJson.toString());
        if(values != null) {
            return db.insertWithOnConflict(Taste.TABLE_NAME, null, values,
                    SQLiteDatabase.CONFLICT_REPLACE);
        } else {
            return -1;
        }
    }

    /**
     * insert the wine into the db, if a record with the same ID is present it will be overwritten
     * @param wineJson the jsonString for the single wine
     * @param language the language used
     * @return the numeric id of the insert or -1 if the insert failed
     */
    public long insertWine(String wineJson, String language)
    {
        language = Utils.getLanguage(language);
        for(String remoteUrl: Downloader.extractUrls(wineJson)) {
            insertImageRecord(remoteUrl);
        }
        ContentValues values = Wine.toContentValue(wineJson, language);
        if(values != null) {
            return db.insertWithOnConflict(Wine.TABLE_NAME, null, values,
                    SQLiteDatabase.CONFLICT_REPLACE);
        } else {
            return -1;
        }
    }

    /**
     * insert the estate into the db, if a record with the same ID is present it will be overwritten
     * @param estateJson the jsonString for the single estate
     * @param language the language used
     * @return the numeric id of the insert or -1 if the insert failed
     */
    public long insertEstate(String estateJson, String language)
    {
        language = Utils.getLanguage(language);
        for(String remoteUrl: Downloader.extractUrls(estateJson)) {
            insertImageRecord(remoteUrl);
        }
        ContentValues values = Estate.toContentValue(estateJson, language);
        if(values != null) {
            return db.insertWithOnConflict(Estate.TABLE_NAME, null, values,
                    SQLiteDatabase.CONFLICT_REPLACE);
        } else {
            return -1;
        }
    }

    /**
     * insert the remote url in the ImageRecord table, if the record is already present,
     * the record will be overwritten an any current local url lost.
     * This method will return true if the record was correctly saved
     * @param remoteUrl the remote url to be saved
     * @return true if the record was correctly saved
     */
    public boolean insertImageRecord(String remoteUrl)
    {
        ContentValues values = new ContentValues();
        values.put(ImageRecord.COLUMN_REMOTE, remoteUrl);
        return db.insertWithOnConflict(ImageRecord.TABLE_NAME, null, values,
                SQLiteDatabase.CONFLICT_REPLACE) > 0;

    }

    // UPDATE METHODS

    /**
     * update the tasting into the db, if a record with the param ID is not present it will be IGNORED
     * @param tastingJson the jsonString for the single taste
     * @return true if the update was successful
     */
    public boolean updateTaste(long id, JSONObject tastingJson)
    {
        ContentValues values = Taste.toContentValue(tastingJson.toString());
        if(values != null && db.updateWithOnConflict(Taste.TABLE_NAME, values,
                Taste.COLUMN_ID + " = ?", new String[]{String.valueOf(id)},
                SQLiteDatabase.CONFLICT_IGNORE) > 0) {
            return id == 1;
        }
        return false;
    }

    /**
     * update the wine into the db, if a record with the param ID is not present it will be IGNORED
     * @param wineJson the jsonString for the single wine
     * @return the numeric id of the updated row or -1 if the insert failed
     */
    public long updateTasting(long id, String wineJson, String language)
    {
        ContentValues values = Wine.toContentValue(wineJson, language);
        if(values != null && db.updateWithOnConflict(Wine.TABLE_NAME, values,
                Wine.COLUMN_ID + " = ?", new String[]{String.valueOf(id)},
                SQLiteDatabase.CONFLICT_IGNORE) > 0) {
            return id;
        }
        return -1;
    }

    /**
     * update the estate into the db, if a record with the param ID is not present it will be IGNORED
     * @param estateJson the jsonString for the single estate
     * @return the numeric id of the updated row or -1 if the insert failed
     */
    public long updateEstate(long id, String estateJson, String language)
    {
        ContentValues values = Estate.toContentValue(estateJson, language);
        if(values != null && db.updateWithOnConflict(Estate.TABLE_NAME, values,
                Estate.COLUMN_ID + " = ?", new String[]{String.valueOf(id)},
                SQLiteDatabase.CONFLICT_IGNORE) > 0) {
            return id;
        }
        return -1;
    }

    /**
     * update the local url in the ImageRecord table, using the remote url as key
     * if the record is already present the record will be overwritten an any current local url lost.
     * This method will return true if the record was correctly saved
     * @param remoteUrl the remote url used as record key
     * @param localUrl the local url to be stored
     * @return true if the record was correctly saved
     */
    public boolean updateImageRecord(String remoteUrl, String localUrl)
    {
        ContentValues values = new ContentValues();
        values.put(ImageRecord.COLUMN_REMOTE, remoteUrl);
        values.put(ImageRecord.COLUMN_LOCAL, localUrl);
        return db.updateWithOnConflict(ImageRecord.TABLE_NAME,
                values,
                ImageRecord.COLUMN_REMOTE + " = ?",
                new String[]{remoteUrl},
                SQLiteDatabase.CONFLICT_REPLACE) > 0;

    }

    // DELETE METHODS

    /**
     * delete the tasting with the selected id, return true if the delete was successful
     * @param id the id to be deleted
     * @return true if the delete was successful
     */
    public boolean deleteTaste(long id)
    {
        return db.delete(Taste.TABLE_NAME, Taste.COLUMN_ID + " = ?",
                new String[]{String.valueOf(id)}) == 1;
    }

    /**
     * delete the wine with the selected id, return true if the delete was successful
     * @param id the id to be deleted
     * @return true if the delete was successful
     */
    public boolean deleteWine(long id)
    {
        return db.delete(Wine.TABLE_NAME,
                Wine.COLUMN_ID + " = ?", new String[]{String.valueOf(id)}) == 1;
    }

    /**
     * delete the estate with the selected id, return true if the delete was successful
     * @param id the id to be deleted
     * @return true if the delete was successful
     */
    public boolean deleteEstate(long id)
    {
        return db.delete(Estate.TABLE_NAME,
                Estate.COLUMN_ID + " = ?", new String[]{String.valueOf(id)}) == 1;
    }


    /**
     * delete the ImageRecord with the selected remoteUrl, return true if the delete was successful
     * @param remoteUrl the url to be deleted
     * @return true if the delete was successful
     */
    public boolean deleteImageRecord(String remoteUrl)
    {
        return db.delete(ImageRecord.TABLE_NAME,
                ImageRecord.COLUMN_REMOTE + " = ?", new String[]{String.valueOf(remoteUrl)}) == 1;
    }

    /**
     * clear the tasting table returns the deleted rows count
     */
    public int clearTastes() {
        return db.delete(Taste.TABLE_NAME, "1", null);
    }

    /**
     * clear the wines table returns the deleted rows count
     */
    public int clearWines() {
        return db.delete(Wine.TABLE_NAME, "1", null);
    }

    /**
     * clear the estate table returns the deleted rows count
     */
    public int clearEstates() {
        return db.delete(Estate.TABLE_NAME, "1", null);
    }

    /**
     * clear the downloaded images dir
     */
    public void clearCache()
    {
        File cacheDir = FileUtils.getImageCacheDir();
        db.delete(ImageRecord.TABLE_NAME, "1", null);
        deleteDirFiles(cacheDir);
    }

    /**
     * recursive delete files in a directory
     * @param directory the directory to be deleted
     */
    private void deleteDirFiles(File directory) {
        if(directory.exists() && directory.isDirectory()) {
            if(directory.listFiles() == null) return;
            for(File file: directory.listFiles())
            {
                if(file.isDirectory()) {
                    deleteDirFiles(directory);
                } else {
                    file.delete();
                }
            }
        }
    }

    /**
     * clear all the tables, if clearImages is true, delete also the downloaded images dir
     * @param clearImages flag, if true delete also the image cache
     */
    public void clearAll(boolean clearImages)
    {
        clearTastes();
        clearWines();
        clearEstates();
        if(clearImages) {
            clearCache();
        }
    }
}
