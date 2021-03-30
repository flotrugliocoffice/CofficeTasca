package com.tasca.android.data;

import android.content.ContentValues;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by carlo on 02/08/2017.
 */

    public class Wine implements DatabaseItem
{
    public final static String TABLE_NAME = "wines";
    public final static String COLUMN_TITLE = "title";
    public final static String COLUMN_ESTATE_ID = "estate_id";
    public final static String COLUMN_ACF_DENOMINATION = "acf_denomination";
    public final static String COLUMN_SLUG = "slug";

    public final static String[] EXPOSED_COLUMNS = new String[] {COLUMN_TITLE, COLUMN_ESTATE_ID,
            COLUMN_ACF_DENOMINATION, COLUMN_SLUG};

    public static final String TABLE_CREATE = "CREATE TABLE " + TABLE_NAME + "(" +
            "  " + COLUMN_ID                + " INTEGER NOT NULL DEFAULT 0," +
            "  " + COLUMN_LANGUAGE          + " TEXT NOT NULL DEFAULT ''," +
            "  " + COLUMN_RAW_JSON          + " TEXT NOT NULL DEFAULT ''," +
            "  " + COLUMN_DATE_GMT          + " TEXT NOT NULL DEFAULT ''," +
            "  " + COLUMN_MODIFIED_GMT      + " TEXT NOT NULL DEFAULT ''," +
            "  " + COLUMN_TITLE             + " TEXT NOT NULL DEFAULT ''," +
            "  " + COLUMN_ESTATE_ID         + " INTEGER," +
            "  " + COLUMN_ACF_DENOMINATION  + " TEXT," +
            "  " + COLUMN_SLUG              + " TEXT NOT NULL DEFAULT ''," +
            " " +
            "  PRIMARY KEY (" + COLUMN_ID + ")" +
            ");";

    /**
     * return the {@link ContentValues} created from the param string, this method will return null if the jsonString param is not a valid json
     *
     * @param jsonString a valid json formatted String
     * @param language   the language used while requesting the wine
     * @return ContentValues containing the json info or null if not a valid json
     */
    public static ContentValues toContentValue(String jsonString, String language)
    {
        ContentValues contentValues = new ContentValues();
        try {
            JSONObject jsonObject = new JSONObject(jsonString);
            contentValues.put(COLUMN_ID, jsonObject.getLong("id"));
            contentValues.put(COLUMN_LANGUAGE, language);
            contentValues.put(COLUMN_RAW_JSON, jsonString);
            contentValues.put(COLUMN_DATE_GMT, jsonObject.getString("date"));
            contentValues.put(COLUMN_MODIFIED_GMT, jsonObject.getString("modified"));
            contentValues.put(COLUMN_TITLE, jsonObject.getString("title"));
            JSONObject acf = jsonObject.getJSONObject("acf");
            JSONObject estate = acf.optJSONObject("tenuta");
            if(estate != null) {
                contentValues.put(COLUMN_ESTATE_ID, estate.getLong("ID"));
            }
            if(acf.has("denominazione")) {
                contentValues.put(COLUMN_ACF_DENOMINATION, acf.getString("denominazione"));
            }
            contentValues.put(COLUMN_SLUG, jsonObject.getString("slug"));
        } catch (JSONException e) {
            e.printStackTrace();
            //NOT A VALID JSON
            return null;
        }
        return contentValues;
    }

}
