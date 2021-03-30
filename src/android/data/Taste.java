package com.tasca.android.data;

import android.content.ContentValues;
import android.text.TextUtils;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by carlo on 02/08/2017.
 */

public class Taste implements DatabaseItem
{
    public final static String TABLE_NAME = "tastes";
    public final static String COLUMN_YEAR = "year";
    public final static String COLUMN_NAME = "name";
    public final static String COLUMN_WINE = "wine";
    public final static String COLUMN_VOTE = "vote";
    public final static String COLUMN_ESTATE= "estate";
    public final static String COLUMN_WINE_ID = "wine_id";
    public final static String COLUMN_ESTATE_ID = "estate_id";

    public final static String[] EXPOSED_COLUMNS = new String[] {COLUMN_YEAR, COLUMN_NAME,
            COLUMN_WINE, COLUMN_VOTE, COLUMN_ESTATE, COLUMN_WINE_ID, COLUMN_ESTATE_ID};

    public static final String TABLE_CREATE = "CREATE TABLE " + TABLE_NAME + "(" +
            "  " + COLUMN_ID                       + " INTEGER NOT NULL DEFAULT 0," +
            "  " + COLUMN_RAW_JSON                 + " TEXT NOT NULL DEFAULT ''," +
            "  " + COLUMN_DATE_GMT                 + " TEXT NOT NULL DEFAULT ''," +
            "  " + COLUMN_MODIFIED_GMT             + " TEXT NOT NULL DEFAULT ''," +
            "  " + COLUMN_YEAR                     + " INTEGER," +
            "  " + COLUMN_NAME                     + " TEXT," +
            "  " + COLUMN_WINE                     + " TEXT," +
            "  " + COLUMN_VOTE                     + " TEXT," +
            "  " + COLUMN_ESTATE                   + " TEXT," +
            "  " + COLUMN_WINE_ID                  + " INTEGER," +
            "  " + COLUMN_ESTATE_ID                + " INTEGER," +
            " " +
            "  PRIMARY KEY (" + COLUMN_ID + ")" +
            ");";

    /**
     * return the {@link ContentValues} created from the param string, this method will return null if the jsonString param is not a valid json
     *
     * @param jsonString a valid json formatted String
     * @return ContentValues containing the json info or null if not a valid json
     */
    public static ContentValues toContentValue(String jsonString)
    {
        ContentValues contentValues = new ContentValues();
        try {
            JSONObject jsonObject = new JSONObject(jsonString);

            // mandatory values
            contentValues.put(COLUMN_ID, jsonObject.getLong("id"));
            contentValues.put(COLUMN_RAW_JSON, jsonString);
            contentValues.put(COLUMN_DATE_GMT, jsonObject.getString("date"));
            contentValues.put(COLUMN_MODIFIED_GMT, jsonObject.getString("modified"));

            //optional values
            int year = jsonObject.optInt("year", -1);
            if(year != -1) {
                contentValues.put(COLUMN_YEAR, year);
            }

            String name = jsonObject.optString("name");
            if(TextUtils.isEmpty(name)) {
                contentValues.put(COLUMN_NAME, name);
            }

            String wine = jsonObject.optString("wine");
            if(TextUtils.isEmpty(wine)) {
                contentValues.put(COLUMN_WINE, wine);
            }

            String vote = jsonObject.optString("vote");
            if(TextUtils.isEmpty(vote)) {
                contentValues.put(COLUMN_VOTE, vote);
            }

            String estate = jsonObject.optString("estate");
            if(TextUtils.isEmpty(estate)) {
                contentValues.put(COLUMN_ESTATE, estate);
            }

            long wineId = jsonObject.optLong("wineId", -1);
            if(wineId != -1) {
                contentValues.put(COLUMN_WINE_ID, wineId);
            }
            long estateId = jsonObject.optLong("estateId", -1);
            if(estateId != -1) {
                contentValues.put(COLUMN_ESTATE_ID, estateId);
            }

        } catch (JSONException e) {
            e.printStackTrace();
            //NOT A VALID JSON
            return null;
        }
        return contentValues;
    }

}