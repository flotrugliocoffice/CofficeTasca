package com.tasca.android;

/**
 * Created by carlo on 10/08/2017.
 */


import android.content.Context;
import com.tasca.android.data.Taste;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * this class include all the methods used by the cordova plugin
 */
public class CofficeTascaInternal
{
    private Context context;
    private static CofficeTascaInternal instance;

    private CofficeTascaInternal() {

    }

    public static CofficeTascaInternal getInstance(Context context)
    {
        if(instance == null) {
            instance = new CofficeTascaInternal();
        }
        instance.context = context;
        return instance;
    }

    // UTILITY METHODS

    /**
     * Return the single Wine json by id, return null if not found or not a valid json
     * @param language optional language param, if null or empty the device default will be used
     * @param id the wine id
     * @return a {@link JSONObject} representing the wine or null if not found/not valid
     */
    public JSONObject getWineById(String language, String id)
    {
        return DatabaseManager.getInstance(context).getWineById(language, id);
    }

    /**
     * Return the single Estate json by id, return null if not found or not a valid json
     * @param language optional language param, if null or empty the device default will be used
     * @param id the estate id
     * @return a {@link JSONObject} representing the estate or null if not found/not valid
     */
    public JSONObject getEstateById(String language, String id)
    {
        return DatabaseManager.getInstance(context).getEstateById(language, id);
    }

    /**
     * Clear the wine database table, return the deleted rows count
     * @return int - deleted rows count
     */
    public int clearWines()
    {
        return DatabaseManager.getInstance(context).clearWines();
    }

    /**
     * Clear the estate database table, return the deleted rows count
     * @return int - deleted rows count
     */
    public int clearEstates()
    {
        return DatabaseManager.getInstance(context).clearEstates();
    }

    /**
     * Clear the tastes database table, return the deleted rows count
     * @return int - deleted rows count
     */
    public int clearTastes()
    {
        return DatabaseManager.getInstance(context).clearTastes();
    }

    /**
     * Delete all the saved images
     */
    public void clearCache()
    {
        DatabaseManager.getInstance(context).clearCache();
    }

    /**
     * Delete all the saved data by calling in sequence all the clear methods, if the supplied param
     * clearImages is true, also the images will be deleted
     * @param clearImages if true the image will be also deleted by this method
     */
    public void clearAll(boolean clearImages)
    {
        DatabaseManager.getInstance(context).clearAll(clearImages);
    }

    /**
     * asynctronous start all the downloads for the selected languages
     * save all the obtanied json to the db, and also download the images if downloadImages == true
     * the stepProgress will respond with this logic:
     *
     *  0 - downloading Wines
     *  1 - downloading Wines images
     *  2 - downloading Estates
     *  3 - downloading Estates images
     *
     * @param lang optional language params, if not passed the device default will be used
     * @param downloadImages if true also the images will be downloaded
     * @param stepProgress callback, notify with a int (0 - 4) when starting each step:
     *                     0 - downloading Wines
     *                     1 - downloading Wines images
     *                     2 - downloading Estates
     *                     3 - downloading Estates images
     * @param currentProgress callback, notify with a float (0-1) about the current progress
     * @param finishListener callback, notify with a JsonArray (the last downloaded one)
     *                       when complete
     */
    public void downloadAll(String lang, boolean downloadImages,
                            Downloader.ProgressListener stepProgress,
                            Downloader.ProgressListener currentProgress,
                            Downloader.FinishListener<JSONArray> finishListener)
    {
        Downloader.getInstance(context).downloadAll(lang, downloadImages, stepProgress,
                currentProgress, finishListener);
    }

    // IMAGES METHODS

    /**
     * parse the json array params and check all the urls inside the {@link JSONArray}
     * if the url contains an image (checked by mimetype) the image
     * will be downloaded on the directory provided by the {@link FileUtils} class
     *
     * @param array       a valid {@link JSONArray}
     * @param progressListener the optional progress callback
     * @param finishListener   the optional finish callback (will return the param array)
     */
    public void downloadAllMedia(JSONArray array, Downloader.ProgressListener progressListener,
                                 Downloader.FinishListener<JSONArray> finishListener)
    {
        Downloader.getInstance(context).downloadAllMedia(array, progressListener, finishListener);
    }

    /**
     * return the local file image path if present
     * if not present return the param url and start the asyncronous download
     * @return a image url (local or remote)
     */
    public String getImageByUrl(String imageUrl)
    {
        return Downloader.getInstance(context).getImageByUrl(imageUrl);
    }

    /**
     * return a JSONObject containing all the images as a Json Object, where the key is the
     * remote url, and the value is the local url, if not present, the local url will be an empty
     * String
     * @return the JSONObject mapping all the project images
     */
    public JSONObject getImagesMap()
    {
        return DatabaseManager.getInstance(context).getImagesMap();
    }

    // WINE MANAGMENT

    /**
     * start the asyncronous Wine download and store the result json in the database
     *
     * @param language         the optional language, if null the device default will be used
     * @param downloadImages   if true, after the completition of the download, the image will be
     *                         also downloaded, the finish listener will be fired on the image
     *                         download completition
     * @param progressListener the optional progress callback
     * @param finishListener   the optional finish callback (will return the json)
     */
    public void downloadWines(final String language,
                              final boolean downloadImages,
                              final Downloader.ProgressListener progressListener,
                              final Downloader.FinishListener<JSONArray> finishListener)
    {
        Downloader.getInstance(context).downloadWines(language, downloadImages,
                progressListener, finishListener);
    }

    /**
     * return an array containing the term inside all the exposed
     * {@link com.tasca.android.data.Wine} database columns max 10 results
     * for each row only the title and the id will be returned
     * (eg. [{"id" = 1, "title" = "winename"}])
     * @param language the optional language, if null the device default will be used
     * @param term search term, will be searched on all the columns exposed
     * @return a {@link JSONArray} containing id and title for each wine, max 10 results
     */
    public JSONArray getWinesTypeAhead(String language, String term)
    {
        return DatabaseManager.getInstance(context).getWineTypeAhead(language, term);
    }

    /**
     * return an array containing all the {@link com.tasca.android.data.Wine}
     * that meets the param specifications.
     * @param language the optional language, if null the device default will be used
     * @param term the optional search term, will be searched on all the columns exposed
     * @param limit the optional limit (pass -1 for no limit)
     * @param offset the optional offset (pass -1 for no offset)
     * @param sort the optional sort column, the sortable column are contained on
     *              {@link com.tasca.android.data.Wine}
     * @param sortDirection sorting order boolean, true for asc, false for desc
     * @return a {@link JSONArray} containing all the wine that meets the params specification
     */
    public JSONArray getWines(String language, String term, int limit, int offset, String sort,
                              boolean sortDirection)
    {
        return DatabaseManager.getInstance(context).getWines(language, term, limit, offset, sort,
                sortDirection);
    }

    // ESTATES MANAGEMENT

    /**
     * start the asyncronous Estate download and store the result json in the database
     *
     * @param language         the optional language, if null the device default will be used
     * @param downloadImages   if true, after the completition of the download, the image will be
     *                         also downloaded, the finish listener will be fired on the image
     *                         download completition
     * @param progressListener the optional progress callback
     * @param finishListener   the optional finish callback (will return the json)
     */
    public void downloadEstates(final String language,
                                final boolean downloadImages,
                                final Downloader.ProgressListener progressListener,
                                final Downloader.FinishListener<JSONArray> finishListener)
    {
        Downloader.getInstance(context).downloadEstates(language, downloadImages,
                progressListener, finishListener);
    }

    /**
     * return an array containing the term inside all the exposed
     * {@link com.tasca.android.data.Estate} database columns max 10 results
     * for each row only the title and the id will be returned
     * (eg. [{"id" = 1, "title" = "estatename"}])
     * @param language the optional language, if null the device default will be used
     * @param term search term, will be searched on all the columns exposed
     * @return a {@link JSONArray} containing id and title for each estate, max 10 results
     */
    public JSONArray getEstatesTypeAhead(String language, String term)
    {
        return DatabaseManager.getInstance(context).getEstateTypeAhead(language, term);
    }

    /**
     * return an array containing all the {@link com.tasca.android.data.Estate}
     * that meets the param specifications.
     * @param language the optional language, if null the device default will be used
     * @param term the optional search term, will be searched on all the columns exposed
     * @param limit the optional limit (pass -1 for no limit)
     * @param offset the optional offset (pass -1 for no offset)
     * @param sort the optional sort column, the sortable column are contained on
     *              {@link com.tasca.android.data.Estate}
     * @param sortDirection sorting order boolean, true for asc, false for desc
     * @return a {@link JSONArray} containing all the estate that meets the params specification
     */
    public JSONArray getEstates(String language, String term, int limit, int offset, String sort,
                                boolean sortDirection)
    {
        return DatabaseManager.getInstance(context).getEstates(language, term, limit, offset, sort,
                sortDirection);
    }

    // TASTES MANAGMENT

    /**
     * Return the single Taste json by id, return null if not found or not a valid json
     * @param id the wine id
     * @return a {@link JSONObject} representing the Taste or null if not found/not valid
     */
    public JSONObject getTasteById(String id)
    {
        return DatabaseManager.getInstance(context).getTasteById(id);
    }

    /**
     * insert the single Taste json in the table, return the id if successful or -1,
     * if the id is present on the json and in the table, the record will be overwritten
     * @param taste the {@link JSONObject} representing the taste
     * @return long taste id if successful, -1 if the insert fail
     */
    public long insertTaste(JSONObject taste)
    {
        return DatabaseManager.getInstance(context).insertTaste(taste);
    }

    /**
     * edit the single Taste json in the table, return true if successful, all the exposed columns
     * will be overwritten with the data from the new Taste json
     * @param id the id to be edited in the database
     * @param taste the {@link JSONObject} representing the taste
     * @return boolean true if edit was successfull
     */
    public boolean editTaste(long id, JSONObject taste)
    {
        return DatabaseManager.getInstance(context).updateTaste(id, taste);
    }

    /**
     * delete the single Taste json in the table, return true if successful
     * @param id the id to be deleted in the database
     * @return boolean true id if delete is successful
     */
    public boolean deleteTaste(long id)
    {
        return DatabaseManager.getInstance(context).deleteTaste(id);
    }

    /**
     * return an array containing the term inside all the exposed
     * {@link Taste} database columns max 10 results
     * for each row only the name and the id will be returned
     * (eg. [{"id" = 1, "name" = "tastename"}])
     * @param term search term, will be searched on all the columns exposed
     * @return a {@link JSONArray} containing id and name for each taste, max 10 results
     */
    public JSONArray getTastesTypeAhead(String term)
    {
        return DatabaseManager.getInstance(context).getTasteTypeAhead(term);
    }

    /**
     * return an array containing all the {@link Taste}
     * that meets the param specifications.
     * @param term the optional search term, will be searched on all the columns exposed
     * @param limit the optional limit (pass -1 for no limit)
     * @param offset the optional offset (pass -1 for no offset)
     * @param sort the optional sort column, the sortable column are contained on
     *              {@link Taste}
     * @param sortDirection sorting order boolean, true for asc, false for desc
     * @return a {@link JSONArray} containing all the tastes that meets the params specification
     */
    public JSONArray getTastes(String term, int limit, int offset, String sort,
                               boolean sortDirection)
    {
        return DatabaseManager.getInstance(context).getTastes(term, limit, offset, sort,
                sortDirection);
    }

    private interface SuccessListener
    {
        public void onSuccess(JSONArray response);
    }

    private interface FailureListener
    {
        public void onFailure(int errorCode, String errorMessage);
    }

}
