package com.tasca.android;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.URLUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Created by carlo on 01/08/2017.
 */

public class Downloader
{
    public static final int DOWNLOADING_WINES = 0;
    public static final int DOWNLOADING_WINES_IMAGE = 1;
    public static final int DOWNLOADING_ESTATES = 2;
    public static final int DOWNLOADING_ESTATES_IMAGE = 3;

    private static final String BASE_URL = "https://www.tascadalmerita.it/";
    private static final String API_VERSION = "api/";
    private static final String ENDPOINT_WINE = "vini/";
    private static final String ENDPOINT_ESTATE = "tenute/";
    private static final String ENDPOINT_WINE_LAST_MODIFIED = "vini/?last_modified=true";
    private static final String ENDPOINT_ESTATE_LAST_MODIFIED = "tenuta/?last_modified=true";

    private static Downloader instance;
    private Context context;

    private Downloader()
    {

    }

    /**
     * Singleton instance getter
     *
     * @return the unique current instance of downloader
     */
    public static final Downloader getInstance(Context context)
    {
        if (instance == null) {
            instance = new Downloader();
        }
        instance.context = context;
        return instance;
    }

    /**
     * start the asyncronous Wine lastUpdate request
     * and callback with true if the database is updated
     *
     * @param language         the optional string languages
     * @param progressListener the optional progress callback
     * @param finishListener   the optional finish callback,
     *                         will return true if the database is updated
     */
    public void checkWineUpdate(final String language, final ProgressListener progressListener,
                                final FinishListener<Boolean> finishListener)
    {

        final long lastDatabaseTimestamp = DatabaseManager.getInstance(context)
                .getLastWineTimestamp(language);
        if(lastDatabaseTimestamp > 0) {
            FinishListener<JSONObject> wrapperFinisListener = new FinishListener<JSONObject>()
            {
                @Override
                public void onDownloadCompleted(JSONObject result)
                {
                    if (finishListener != null) {
                        String serverUpdateString = result.optString("modified");
                        long serverUpdateTime = Utils.dateStringToMillis(serverUpdateString);
                        boolean databaseUpdated = lastDatabaseTimestamp >= serverUpdateTime;
                        finishListener.onDownloadCompleted(databaseUpdated);
                    }
                }

                @Override
                public void onDownloadFailed(int errorCode, String errorMessage)
                {
                    if (finishListener != null) {
                        finishListener.onDownloadFailed(errorCode, errorMessage);
                    }
                }
            };
            executeJsonObjectDownload(getServerUrl(ENDPOINT_WINE, language, true), progressListener,
                    wrapperFinisListener);
        } else if (finishListener != null) {
            finishListener.onDownloadCompleted(false);
        }

    }

    /**
     * start the asyncronous Wine download and store the result json in the database
     * if the database is updated the download will be skipped and the lister will fire immediately
     *
     * @param language         the optional string languages
     * @param downloadImages   if true, after the completition of the download, the image will be
     *                         also downloaded, the finish listener will be fired on the image
     *                         download completition
     * @param progressListener the optional progress callback
     * @param finishListener   the optional finish callback (will return the json)
     */
    public void downloadWines(final String language, final boolean downloadImages,
                              final ProgressListener progressListener,
                              final FinishListener<JSONArray> finishListener)
    {
        final FinishListener<JSONArray> wrapperFinisListener = new FinishListener<JSONArray>()
        {
            @Override
            public void onDownloadCompleted(final JSONArray array)
            {
                new AsyncTask<Void, Void, Void>() {

                    @Override
                    protected Void doInBackground(Void... params)
                    {
                        for (int i = 0; i < array.length(); i++) {
                            DatabaseManager.getInstance(context)
                                    .insertWine(array.opt(i).toString(), language);
                        }

                        JSONArray winesArray = DatabaseManager.getInstance(context)
                                .getWines(language, null, -1, -1, null, true);
                        if (downloadImages) {
                            downloadAllMedia(winesArray, progressListener, finishListener);
                        } else if (finishListener != null) {
                            finishListener.onDownloadCompleted(winesArray);
                        }
                        return null;
                    }
                }.execute();
            }

            @Override
            public void onDownloadFailed(int errorCode, String errorMessage)
            {
                if (finishListener != null) {
                    finishListener.onDownloadFailed(errorCode, errorMessage);
                }
            }
        };

        checkWineUpdate(language, null, new FinishListener<Boolean>()
        {
            @Override
            public void onDownloadCompleted(Boolean updated)
            {
                if(updated) {
                    if(progressListener != null) {
                        progressListener.onProgress(1);
                    }
                    wrapperFinisListener.onDownloadCompleted(new JSONArray());
                } else {
                    executeJsonArrayDownload(getServerUrl(ENDPOINT_WINE, language), progressListener,
                            wrapperFinisListener);
                }
            }

            @Override
            public void onDownloadFailed(int errorCode, String errorMessage)
            {
                wrapperFinisListener.onDownloadFailed(errorCode, errorMessage);
            }
        });
    }

    /**
     * start the asyncronous Estate lastUpdate request
     * and callback with true if the database is updated
     *
     * @param language         the optional string languages
     * @param progressListener the optional progress callback
     * @param finishListener   the optional finish callback,
     *                         will return true if the database is updated
     */
    public void checkEstateUpdate(final String language, final ProgressListener progressListener,
                                  final FinishListener<Boolean> finishListener)
    {

        final long lastDatabaseTimestamp = DatabaseManager.getInstance(context)
                .getLastEstateTimestamp(language);
        if(lastDatabaseTimestamp > 0) {
            FinishListener<JSONObject> wrapperFinisListener = new FinishListener<JSONObject>()
            {
                @Override
                public void onDownloadCompleted(JSONObject result)
                {
                    if (finishListener != null) {
                        String serverUpdateString = result.optString("modified");
                        long serverUpdateTime = Utils.dateStringToMillis(serverUpdateString);
                        boolean databaseUpdated = lastDatabaseTimestamp >= serverUpdateTime;
                        finishListener.onDownloadCompleted(databaseUpdated);
                    }
                }

                @Override
                public void onDownloadFailed(int errorCode, String errorMessage)
                {
                    if (finishListener != null) {
                        finishListener.onDownloadFailed(errorCode, errorMessage);
                    }
                }
            };
            executeJsonObjectDownload(getServerUrl(ENDPOINT_ESTATE, language, true), progressListener,
                    wrapperFinisListener);
        } else if (finishListener != null) {
            finishListener.onDownloadCompleted(false);
        }
    }

    /**
     * start the asyncronous Estate download and store the result json in the database
     * if the database is updated the download will be skipped and the lister will fire immediately
     *
     * @param language         the optional string languages
     * @param progressListener the optional progress callback
     * @param finishListener   the optional finish callback (will return the json)
     * @param downloadImages   if true, after the completition of the download, the image will be
     *                         also downloaded, the finish listener will be fired on the image
     *                         download completition
     */
    public void downloadEstates(final String language, final boolean downloadImages,
                                final ProgressListener progressListener,
                                final FinishListener<JSONArray> finishListener)
    {
        final FinishListener<JSONArray> wrapperFinisListener = new FinishListener<JSONArray>()
        {
            @Override
            public void onDownloadCompleted(final JSONArray array)
            {
                new AsyncTask<Void, Void, Void>() {

                    @Override
                    protected Void doInBackground(Void... params)
                    {
                        for (int i = 0; i < array.length(); i++) {
                            DatabaseManager.getInstance(context)
                                    .insertEstate(array.opt(i).toString(), language);
                        }

                        JSONArray estateArray = DatabaseManager.getInstance(context)
                                .getEstates(language, null, -1, -1, null, true);
                        if (downloadImages) {
                            downloadAllMedia(estateArray, progressListener, finishListener);
                        } else if (finishListener != null) {
                            finishListener.onDownloadCompleted(estateArray);
                        }
                        return null;
                    }
                }.execute();
            }

            @Override
            public void onDownloadFailed(int errorCode, String errorMessage)
            {
                if (finishListener != null) {
                    finishListener.onDownloadFailed(errorCode, errorMessage);
                }
            }
        };

        checkEstateUpdate(language, null, new FinishListener<Boolean>()
        {
            @Override
            public void onDownloadCompleted(Boolean updated)
            {
                if(updated) {
                    if(progressListener != null) {
                        progressListener.onProgress(1);
                    }
                    wrapperFinisListener.onDownloadCompleted(new JSONArray());
                } else {
                    executeJsonArrayDownload(getServerUrl(ENDPOINT_ESTATE, language), progressListener,
                            wrapperFinisListener);
                }
            }

            @Override
            public void onDownloadFailed(int errorCode, String errorMessage)
            {
                wrapperFinisListener.onDownloadFailed(errorCode, errorMessage);
            }
        });

    }

    /**
     * parse the jsonString params and check all the urls inside the {@link JSONObject}
     * or {@link JSONArray}, if the url contains an image (checked by mimetype) the image
     * will be downloaded on the directory provided by the {@link FileUtils} class
     *
     * @param jsonArray        a valid {@link JSONArray}
     * @param progressListener the optional progress callback
     * @param finishListener   the optional finish callback (will return the param array)
     */
    public void downloadAllMedia(final JSONArray jsonArray, final ProgressListener progressListener,
                                 final FinishListener<JSONArray> finishListener)
    {
        //if the user hasn't granted the write permission don't attempt download
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
            if (progressListener != null) {
                progressListener.onProgress(1);
            }

            if (finishListener != null) {
                finishListener.onDownloadFailed(-1, " permission not granted");
            }
            return;
        }

        //using a set to avoid duplicates
        final Set<String> urls = new HashSet<String>();

        if (progressListener != null) {
            progressListener.onProgress(0);
        }

        urls.addAll(extractUrls(jsonArray));

        if (urls.size() == 0) {
            if (progressListener != null) {
                progressListener.onProgress(1);
            }

            if (finishListener != null) {
                finishListener.onDownloadCompleted(jsonArray);
                return;
            }
        } else {
            final List<String> downloadUrls = new ArrayList<String>(urls);
            executeImageDownload(downloadUrls.remove(0), null, new FinishListener<String>()
            {
                @Override
                public void onDownloadCompleted(String path)
                {
                    if (progressListener != null) {
                        float progress = (float) (urls.size() - downloadUrls.size()) / urls.size();
                        progressListener.onProgress(progress);
                    }
                    if (downloadUrls.size() > 0) {
                        executeImageDownload(downloadUrls.remove(0), null, this);
                    } else if (finishListener != null) {
                        finishListener.onDownloadCompleted(jsonArray);
                    }
                }

                @Override
                public void onDownloadFailed(int errorCode, String errorMessage)
                {
                    if (progressListener != null) {
                        float progress = (float) (urls.size() - downloadUrls.size()) / urls.size();
                        progressListener.onProgress(progress);
                    }
                    if (downloadUrls.size() > 0) {
                        executeImageDownload(downloadUrls.remove(0), null, this);
                    } else if (finishListener != null) {
                        finishListener.onDownloadCompleted(jsonArray);
                    }
                }
            });
        }
    }

    /**
     * Asyncronous download all the media that have a remote url but not a local url in the image table
     * all the images will be downloaded on the directory provided by the {@link FileUtils} class
     *
     * @param progressListener the optional progress callback
     * @param finishListener   the optional finish callback (will return the param array)
     */
    public void downloadAllUncompleteMedia(final ProgressListener progressListener,
                                           final FinishListener<Boolean> finishListener)
    {
        //if the user hasn't granted the write permission don't attempt download
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
            if (progressListener != null) {
                progressListener.onProgress(1);
            }

            if (finishListener != null) {
                finishListener.onDownloadFailed(-1, " permission not granted");
            }
            return;
        }

        //using a set to avoid duplicates
        final Set<String> urls = new HashSet<String>();

        if (progressListener != null) {
            progressListener.onProgress(0);
        }

        JSONObject uncompleteUrls = DatabaseManager.getInstance(context).getImagesMap(true);
        urls.addAll(extractUrls(uncompleteUrls));

        if (urls.size() == 0) {
            if (progressListener != null) {
                progressListener.onProgress(1);
            }

            if (finishListener != null) {
                finishListener.onDownloadCompleted(true);
                return;
            }
        } else {
            final List<String> downloadUrls = new ArrayList<String>(urls);
            executeImageDownload(downloadUrls.remove(0), null, new FinishListener<String>()
            {
                @Override
                public void onDownloadCompleted(String path)
                {
                    if (progressListener != null) {
                        float progress = (float) (urls.size() - downloadUrls.size()) / urls.size();
                        progressListener.onProgress(progress);
                    }
                    if (downloadUrls.size() > 0) {
                        executeImageDownload(downloadUrls.remove(0), null, this);
                    } else if (finishListener != null) {
                        finishListener.onDownloadCompleted(true);
                    }
                }

                @Override
                public void onDownloadFailed(int errorCode, String errorMessage)
                {
                    if (progressListener != null) {
                        float progress = (float) (urls.size() - downloadUrls.size()) / urls.size();
                        progressListener.onProgress(progress);
                    }
                    if (downloadUrls.size() > 0) {
                        executeImageDownload(downloadUrls.remove(0), null, this);
                    } else if (finishListener != null) {
                        finishListener.onDownloadCompleted(true);
                    }
                }
            });
        }
    }

    /**
     * asynctronous start all the downloads for the selected languages
     * save all the obtanied json to the db, and also download the images if downloadImages == true
     * the stepProgress will respond with this logic:
     * <p>
     * 0 - downloading Wines
     * 1 - downloading Wines images
     * 2 - downloading Estates
     * 3 - downloading Estates images
     *
     * @param language
     * @param downloadImages
     * @param stepProgress
     * @param progressListener
     * @param finishListener
     */
    public void downloadAll(final String language,
                            final boolean downloadImages,
                            final ProgressListener stepProgress,
                            final ProgressListener progressListener,
                            final FinishListener finishListener)
    {
        final int totalSteps = downloadImages ? 4 : 2;
        if (stepProgress != null) {
            stepProgress.onProgress(DOWNLOADING_WINES);
        }

        final ProgressListener wineProgress = new ProgressListener()
        {
            @Override
            public void onProgress(float progress)
            {
                if (progressListener != null) {
                    progressListener.onProgress(progress / totalSteps);
                }
            }
        };

        final ProgressListener wineImageProgress = new ProgressListener()
        {
            @Override
            public void onProgress(float progress)
            {
                if (progressListener != null) {
                    float completedProgress = 0.25f;
                    progressListener.onProgress(completedProgress + (progress / totalSteps));
                }
            }
        };

        final ProgressListener estateProgress = new ProgressListener()
        {
            @Override
            public void onProgress(float progress)
            {
                if (progressListener != null) {
                    float completedProgress = 0.5f;
                    progressListener.onProgress(completedProgress + (progress / totalSteps));
                }
            }
        };

        final ProgressListener estateImageProgress = new ProgressListener()
        {
            @Override
            public void onProgress(float progress)
            {
                if (progressListener != null) {
                    float completedProgress = 0.75f;
                    progressListener.onProgress(completedProgress + (progress / totalSteps));
                }
            }
        };

        final FinishListener<JSONArray> estateImageFinish = new FinishListener<JSONArray>()
        {
            @Override
            public void onDownloadCompleted(JSONArray jsonResult)
            {
                if (finishListener != null) {
                    finishListener.onDownloadCompleted(jsonResult);
                }
            }

            @Override
            public void onDownloadFailed(int errorCode, String errorMessage)
            {
                if (finishListener != null) {
                    finishListener.onDownloadFailed(errorCode, errorMessage);
                }
            }
        };

        final FinishListener estateFinish = new FinishListener<JSONArray>()
        {
            @Override
            public void onDownloadCompleted(JSONArray jsonResult)
            {
                if (downloadImages) {
                    downloadAllMedia(jsonResult, estateImageProgress, estateImageFinish);
                    if (stepProgress != null) {
                        stepProgress.onProgress(DOWNLOADING_ESTATES_IMAGE);
                    }
                } else {
                    if (finishListener != null) {
                        finishListener.onDownloadCompleted(jsonResult);
                    }
                }
            }

            @Override
            public void onDownloadFailed(int errorCode, String errorMessage)
            {
                if (finishListener != null) {
                    finishListener.onDownloadFailed(errorCode, errorMessage);
                }
            }
        };

        final FinishListener wineImageFinish = new FinishListener<JSONArray>()
        {
            @Override
            public void onDownloadCompleted(JSONArray jsonResult)
            {
                downloadEstates(language, false, estateProgress, estateFinish);
                if (stepProgress != null) {
                    stepProgress.onProgress(DOWNLOADING_ESTATES);
                }
            }

            @Override
            public void onDownloadFailed(int errorCode, String errorMessage)
            {
                downloadEstates(language, false, estateProgress, estateFinish);
                if (finishListener != null) {
                    finishListener.onDownloadFailed(errorCode, errorMessage);
                }
            }
        };

        final FinishListener wineFinish = new FinishListener<JSONArray>()
        {
            @Override
            public void onDownloadCompleted(JSONArray jsonResult)
            {
                if (downloadImages) {
                    downloadAllMedia(jsonResult, wineImageProgress, wineImageFinish);
                    if (stepProgress != null) {
                        stepProgress.onProgress(DOWNLOADING_WINES_IMAGE);
                    }
                } else {
                    downloadEstates(language, false, estateProgress, estateFinish);
                    if (stepProgress != null) {
                        stepProgress.onProgress(DOWNLOADING_ESTATES);
                    }
                }
            }

            @Override
            public void onDownloadFailed(int errorCode, String errorMessage)
            {
                if (finishListener != null) {
                    finishListener.onDownloadFailed(errorCode, errorMessage);
                }
            }
        };

        //start first download
        downloadWines(language, false, wineProgress, wineFinish);

    }

    public static List<String> extractUrls(String jsonString)
    {
        List<String> urls = new ArrayList<String>();
        if(!TextUtils.isEmpty(jsonString)) {
            String check = String.valueOf(jsonString.charAt(0));
            try {
                if (check.equalsIgnoreCase("[")) {
                    urls = extractUrls(new JSONArray(jsonString));
                } else if (check.equalsIgnoreCase("{")){
                    urls = extractUrls(new JSONObject(jsonString));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return urls;
    }

    public static List<String> extractUrls(JSONObject json)
    {
        List<String> urls = new ArrayList<String>();
        Iterator<String> keys = json.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            if (json.optJSONArray(key) != null) {
                urls.addAll(extractUrls(json.optJSONArray(key)));
            } else if (json.optJSONObject(key) != null) {
                urls.addAll(extractUrls(json.optJSONObject(key)));
            } else {
                String testUrl = json.optString(key);
                if (URLUtil.isValidUrl(testUrl)) {
                    urls.add(testUrl);
                }
            }
        }
        return urls;
    }

    /**
     * recursively extract urls from {@link JSONArray}
     *
     * @param jsonArray the array to extract the urls
     * @return a list of url found on the {@link JSONArray}
     */
    public static List<String> extractUrls(JSONArray jsonArray)
    {
        List<String> urls = new ArrayList<String>();
        for (int i = 0; i < jsonArray.length(); i++) {
            if (jsonArray.optJSONArray(i) != null) {
                urls.addAll(extractUrls(jsonArray.optJSONArray(i)));
            } else if (jsonArray.optJSONObject(i) != null) {
                urls.addAll(extractUrls(jsonArray.optJSONObject(i)));
            } else if (jsonArray.optString(i) != null) {
                String testUrl = jsonArray.optString(i);
                if (URLUtil.isValidUrl(testUrl)) {
                    urls.add(testUrl);
                }
            }
        }
        return urls;
    }

    private String getServerUrl(String path, String language)
    {
        return getServerUrl(path, language, false);
    }

    private String getServerUrl(String path, String language, boolean lastUpdate)
    {
        language = Utils.getLanguage(language);
        if (language.equals("it")) {
            language = "";
        }
        String urlLanguage = TextUtils.isEmpty(language) ? language : language + "/";
        return BASE_URL + urlLanguage + API_VERSION + path + (lastUpdate ? "/?last_modified=true" : "");
    }

    /**
     * Asyncronous download a {@link JSONObject} from an url
     *
     * @param urlRequest       the url containing the json
     * @param progressListener progress callback, will respond in the UI thread
     *                         with a value comprised from 0 to 1
     * @param finishListener   complete callback, will respond in the UI thread
     *                         with onDownloadCompleted or onDownloadFailed
     * @return the current {@link AsyncTask} used for the request (for cancelling or other handling)
     */
    private AsyncTask executeJsonObjectDownload(final String urlRequest,
                                                final ProgressListener progressListener,
                                                final FinishListener<JSONObject> finishListener)
    {
        return executeJsonDownload(urlRequest, progressListener, finishListener, false);
    }

    /**
     * Asyncronous download a {@link JSONArray} from an url
     *
     * @param urlRequest       the url containing the json
     * @param progressListener progress callback, will respond in the UI thread
     *                         with a value comprised from 0 to 1
     * @param finishListener   complete callback, will respond in the UI thread
     *                         with onDownloadCompleted or onDownloadFailed
     * @return the current {@link AsyncTask} used for the request (for cancelling or other handling)
     */
    private AsyncTask executeJsonArrayDownload(final String urlRequest,
                                               final ProgressListener progressListener,
                                               final FinishListener<JSONArray> finishListener)
    {
        return executeJsonDownload(urlRequest, progressListener, finishListener, true);
    }

    /**
     * Asyncronous download a json string from an url
     *
     * @param urlRequest       the url containing the json
     * @param progressListener progress callback, will respond in the UI thread
     *                         with a value comprised from 0 to 1
     * @param finishListener   complete callback, will respond in the UI thread
     *                         with onDownloadCompleted or onDownloadFailed
     * @return the current {@link AsyncTask} used for the request (for cancelling or other handling)
     */
    private AsyncTask executeJsonDownload(final String urlRequest,
                                          final ProgressListener progressListener,
                                          final FinishListener finishListener, final boolean jsonArray)
    {

        AsyncTask<Void, Float, String> downloadTask = new AsyncTask<Void, Float, String>()
        {
            @Override
            protected String doInBackground(Void... voids)
            {
                try {
                    //start the progress at 0;
                    publishProgress(0f);

                    //opening the connection
                    URL url = new URL(urlRequest);
                    URLConnection urlConnection = url.openConnection();
                    int contentLength = urlConnection.getContentLength();

                    //start reading data
                    InputStreamReader inr = new InputStreamReader(urlConnection.getInputStream());
                    BufferedReader in = new BufferedReader(inr);
                    StringBuilder sb = new StringBuilder();
                    String line;
                    long bytesRead = 0;
                    while ((line = in.readLine()) != null) {
                        //check if the download should continue
                        if (isCancelled()) {
                            return null;
                        }

                        //write data to the StringBuilder
                        sb.append(line);
                        bytesRead = bytesRead + line.getBytes().length + 2;

                        //update progress if possible
                        if (contentLength != -1) {
                            publishProgress(Float.valueOf(contentLength / bytesRead));
                        } else {
                            //todo publish undefined?
                        }
                    }
                    //close the stream
                    in.close();
                    return sb.toString();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onProgressUpdate(Float... values)
            {
                super.onProgressUpdate(values);
                if (progressListener != null) {
                    progressListener.onProgress(values[0]);
                }
            }

            @Override
            protected void onPostExecute(String result)
            {
                super.onPostExecute(result);
                onProgressUpdate(1f);
                if (result == null) {
                    if (finishListener != null) {
                        finishListener.onDownloadFailed(-1, "unexpected error");
                    }
                } else {
                    if (finishListener != null) {
                        try {
                            if (jsonArray) {
                                finishListener.onDownloadCompleted(new JSONArray(result));
                            } else {
                                finishListener.onDownloadCompleted(new JSONObject(result));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            finishListener.onDownloadFailed(-1, "not valid json (" + (jsonArray ? "array" : "object") +")");
                        }
                    }
                }
            }
        };


        downloadTask.execute();
        return downloadTask;
    }

    /**
     * return the local file image path if present
     * if not present return the param url and start the asyncronous download
     *
     * @return a image url (local or remote)
     */
    public String getImageByUrl(String imageUrl)
    {
        //if the user hasn't granted the write permission only return the remote url
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
            return imageUrl;
        }

        File file = FileUtils.getLocalImageFile(imageUrl);
        if (file.exists() && file.length() > 0) {
            DatabaseManager.getInstance(context).updateImageRecord(imageUrl, file.getAbsolutePath());
            return file.getAbsolutePath();
        } else {
            executeImageDownload(imageUrl, null, null);
            return imageUrl;
        }
    }

    /**
     * Asyncronous download a json string from an url
     *
     * @param urlRequest       the url containing the image, the image will be saved
     *                         in the {@link FileUtils} with url hashcode followed by .TMP
     *                         as filename
     * @param progressListener progress callback, will respond in the UI thread
     *                         with a value comprised from 0 to 1
     * @param finishListener   complete callback, will respond in the UI thread
     *                         with onDownloadCompleted or onDownloadFailed
     * @return the current {@link AsyncTask} used for the request (for cancelling or other handling)
     */
    public void executeImageDownload(final String urlRequest,
                                     final ProgressListener progressListener,
                                     final FinishListener<String> finishListener)
    {

        requestMimeType(urlRequest, new FinishListener<String>()
        {
            @Override
            public void onDownloadCompleted(String mimetype)
            {
                if (mimetype.contains("image")) {
                    AsyncTask<Void, Float, File> downloadTask = new AsyncTask<Void, Float, File>()
                    {
                        @Override
                        protected File doInBackground(Void... voids)
                        {
                            try {
                                //start the progress at 0;
                                publishProgress(0f);

                                //opening the connection
                                URL url = new URL(urlRequest);
                                URLConnection urlConnection = url.openConnection();
                                int contentLength = urlConnection.getContentLength();

                                //start reading data
                                File destinationFile = FileUtils.getLocalImageFile(urlRequest);

                                InputStream in = urlConnection.getInputStream();
                                if (destinationFile.exists()
                                        & contentLength == destinationFile.length()) {
                                    Log.d("TEST DOWNLOAD", "File valid, skipping");
                                    in.close();
                                    return destinationFile;
                                }

                                OutputStream out = new FileOutputStream(destinationFile);
                                byte data[] = new byte[4096];
                                long total = 0;
                                int count;

                                while ((count = in.read(data)) != -1) {
                                    // allow canceling with back button
                                    if (isCancelled()) {
                                        in.close();
                                        destinationFile.delete();
                                        return null;
                                    }
                                    total += count;
                                    // publishing the progress....
                                    if (contentLength > 0) // only if total length is known
                                        publishProgress(Float.valueOf(total / contentLength));
                                    out.write(data, 0, count);
                                }

                                //close the stream
                                in.close();

                                return destinationFile;
                            } catch (IOException e) {
                                e.printStackTrace();
                                if (finishListener != null) {
                                    finishListener.onDownloadFailed(-1, "unexpected error");
                                }
                            }
                            return null;
                        }

                        @Override
                        protected void onProgressUpdate(Float... values)
                        {
                            super.onProgressUpdate(values);
                            if (progressListener != null) {
                                progressListener.onProgress(values[0]);
                            }
                        }

                        @Override
                        protected void onPostExecute(File result)
                        {
                            super.onPostExecute(result);
                            onProgressUpdate(1f);
                            if (finishListener != null) {
                                if (result != null && result.length() > 0) {
                                    finishListener.onDownloadCompleted(result.getAbsolutePath());
                                    DatabaseManager.getInstance(context).updateImageRecord(urlRequest, result.getAbsolutePath());
                                } else {
                                    finishListener.onDownloadFailed(-1, "download error");
                                }
                            }
                        }
                    };


                    downloadTask.execute();
                } else {
                    DatabaseManager.getInstance(context).deleteImageRecord(urlRequest);
                    finishListener.onDownloadFailed(-1, "not a valid image");
                }
            }

            @Override
            public void onDownloadFailed(int errorCode, String errorMessage)
            {
                finishListener.onDownloadFailed(errorCode, errorMessage);
            }
        });
    }

    /**
     * return the mime/type on the FinishListener callback
     *
     * @param finishListener callback interface return the mimetype as String
     * @return the asynctask running the request
     */
    public AsyncTask requestMimeType(final String requestUrl,
                                     final FinishListener<String> finishListener)
    {

        AsyncTask<Void, Void, String> downloadTask = new AsyncTask<Void, Void, String>()
        {
            @Override
            protected String doInBackground(Void... voids)
            {
                HttpURLConnection urlConnection = null;
                System.setProperty("http.keepAlive", "false");
                try {
                    URL url = new URL(requestUrl);
                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("HEAD");
                    return urlConnection.getContentType();
                } catch (MalformedURLException e) {
                    finishListener.onDownloadFailed(-1, "not valid url");
                    e.printStackTrace();
                } catch (IOException e) {
                    finishListener.onDownloadFailed(-1, "unexpected error");
                    e.printStackTrace();
                } finally {
                    if (urlConnection != null) {
                        urlConnection.disconnect();
                    }
                }
                return null;
            }

            @Override
            protected void onPostExecute(String result)
            {
                super.onPostExecute(result);
                finishListener.onDownloadCompleted(result);
            }
        };

        downloadTask.execute();
        return downloadTask;
    }

    public interface ProgressListener
    {
        public void onProgress(float progress);
    }

    public interface FinishListener<T>
    {
        public void onDownloadCompleted(T result);

        public void onDownloadFailed(int errorCode, String errorMessage);
    }
}
