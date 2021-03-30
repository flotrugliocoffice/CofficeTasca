package com.tasca.android;

import android.Manifest;
import android.content.pm.PackageManager;

import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.file.LocalFilesystem;

import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;
import java.util.ArrayList;
import java.util.Locale;
import java.util.HashMap;
import java.util.List;

import android.net.Uri;
import android.location.Address;
import android.location.Geocoder;
import android.support.v4.content.ContextCompat;
import android.telecom.Call;
import android.text.TextUtils;



/**
 * This class echoes a string called from JavaScript.
 */
public class CofficeTasca extends CordovaPlugin
{
    Geocoder geocoder;

    public static final int PLUGIN_RES_CODE = 1009;
    String[] permissions = {Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};

    private String[] requestesPermissions = new String[0];
    private CallbackContext _callbackContext;

    private CofficeTascaInternal cofficeInstance;

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView)
    {
        super.initialize(cordova, webView);
        // Richiedi permessi
        cofficeInstance = CofficeTascaInternal.getInstance(cordova.getActivity());
        inner_requestPermissions(null);
    }

    public void onRequestPermissionResult(int requestCode, String[] permissions,
                                          int[] grantResults) throws JSONException
    {
        HashMap<String, Integer> grantRes = new HashMap<String, Integer>();
        HashMap<String, Integer> ungrantRes = new HashMap<String, Integer>();
        for (int i = 0; i < permissions.length; i++) {
            if (grantResults.length > i) {
                if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                    ungrantRes.put(permissions[i], grantResults[i]);
                } else {
                    grantRes.put(permissions[i], grantResults[i]);
                }
            }
        }

        boolean permissionGranted = (ungrantRes.size() == 0);
        if (permissionGranted == true) {
            if(_callbackContext != null) {
                _callbackContext.success("ALL GRANTED");
            }
            initDatabase();
        } else {
            JSONObject jp = new JSONObject();
            jp.put("message", "Some not Granted");
            jp.put("granted", grantRes.keySet().toString());
            jp.put("not_granted", ungrantRes.keySet().toString());
            if(_callbackContext != null) {
                _callbackContext.error(jp);
            }
        }
    }

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException
    {
        if (action.equals("reverseGeocode")) {
            double latitude = args.getDouble(0);
            double longitude = args.getDouble(1);
            this.reverseGeocode(latitude, longitude, callbackContext);
            return true;
        }


        if (action.equals("getInfo")) {
            String message = args.getString(0);
            this.getInfo(message, callbackContext);
            return true;
        }
        if (action.equals("requestPermissions")) {
            this.inner_requestPermissions(callbackContext);
            return true;
        }

        if (action.equals("getWineById")) {
            String language = args.optString(0, null);
            String id = args.getString(1);
            this.getWineById(language, id, callbackContext);
            return true;
        }

        if (action.equals("getEstateById")) {
            String language = args.optString(0, null);
            String id = args.getString(1);
            this.getEstateById(language, id, callbackContext);
            return true;
        }

        if (action.equals("clearWines")) {
            cofficeInstance.clearWines();
            return true;
        }

        if (action.equals("clearEstates")) {
            cofficeInstance.clearEstates();
            return true;
        }

        if (action.equals("clearTastes")) {
            cofficeInstance.clearTastes();
            return true;
        }

        if (action.equals("clearCache")) {
            cofficeInstance.clearCache();
            return true;
        }

        if (action.equals("clearAll")) {
            clearAll(args.optBoolean(0, false));
            return true;
        }

        if (action.equals("downloadAll")) {
            String lang = args.optString(0, null);
            boolean downloadImages = args.optBoolean(1, false);
            downloadAll(lang, downloadImages, callbackContext);
            return true;
        }

        if (action.equals("downloadAllMedia")) {
            downloadAllMedia(args.optJSONArray(0), callbackContext);
            return true;
        }

        if (action.equals("getImageByUrl")) {
            getImagesByUrl(args.optString(0), callbackContext);
            return true;
        }

        if (action.equals("getImageMap")) {
            getImageMap(args.optBoolean(0, false), callbackContext);
            return true;
        }

        if (action.equals("downloadWines")) {
            String lang = args.optString(0, null);
            boolean downloadImages = args.optBoolean(1, false);
            downloadWines(lang, downloadImages, callbackContext);
            return true;
        }

        if (action.equals("getWinesTypeAhead")) {
            String lang = args.optString(0, null);
            String term = args.optString(1, null);
            getWinesTypeAhead(lang, term, callbackContext);
            return true;
        }

        if (action.equals("getWines")) {
            String lang = args.optString(0, null);
            String term = args.optString(1, null);
            int limit = args.optInt(2, -1);
            int offset = args.optInt(3, -1);
            String sort = args.optString(4, null);
            boolean sortDirection = args.optBoolean(5, false);
            getWines(lang, term, limit, offset, sort, sortDirection, callbackContext);
            return true;
        }

        if (action.equals("downloadEstates")) {
            String lang = args.optString(0, null);
            boolean downloadImages = args.optBoolean(1, false);
            downloadEstates(lang, downloadImages, callbackContext);
            return true;
        }

        if (action.equals("getEstatesTypeAhead")) {
            String lang = args.optString(0, null);
            String term = args.optString(1, null);
            getEstatesTypeAhead(lang, term, callbackContext);
            return true;
        }

        if (action.equals("getEstates")) {
            String lang = args.optString(0, null);
            String term = args.optString(1, null);
            int limit = args.optInt(2, -1);
            int offset = args.optInt(3, -1);
            String sort = args.optString(4, null);
            boolean sortDirection = args.optBoolean(5, false);
            getEstates(lang, term, limit, offset, sort, sortDirection, callbackContext);
            return true;
        }

        if (action.equals("getTasteById")) {
            String id = args.getString(1);
            getTasteById(id, callbackContext);
            return true;
        }

        if (action.equals("insertTaste")) {
            JSONObject taste = args.optJSONObject(0);
            insertTaste(taste, callbackContext);
            return true;
        }

        if (action.equals("editTaste")) {
            long id = args.optLong(0, -1);
            JSONObject taste = args.optJSONObject(1);
            editTaste(id, taste, callbackContext);
            return true;
        }

        if (action.equals("deleteTaste")) {
            long id = args.optLong(0, -1);
            deleteTaste(id, callbackContext);
            return true;
        }

        if (action.equals("getTastesTypeAhead")) {
            String term = args.optString(0, null);
            getTastesTypeAhead(term, callbackContext);
            return true;
        }

        if (action.equals("getTastes")) {
            String term = args.optString(0, null);
            int limit = args.optInt(1, -1);
            int offset = args.optInt(2, -1);
            String sort = args.optString(3, null);
            boolean sortDirection = args.optBoolean(4, false);
            getTastes(term, limit, offset, sort, sortDirection, callbackContext);
            return true;
        }
        return false;
    }

    private void initDatabase()
    {
        //TODO Add a method to prepare database is not prepared.
    }

    private void inner_requestPermissions(CallbackContext callbackContext)
    {
        List<String> permissionToRequest = new ArrayList<String>();
        for (String permission : permissions) {
            if (cordova.hasPermission(permission) == false) {
                permissionToRequest.add(permission);
            }
        }
        if (permissionToRequest.size() > 0) {
            this._callbackContext = callbackContext;
            requestesPermissions = permissionToRequest.toArray(new String[permissionToRequest.size()]);
            cordova.requestPermissions(this, PLUGIN_RES_CODE, requestesPermissions);
        } else {
            initDatabase();
            if(callbackContext != null) {
                callbackContext.success("ALL GRANTED");
            }
        }
    }

    private void getInfo(String message, CallbackContext callbackContext)
    {
        if (message != null && message.length() > 0) {
            callbackContext.success(message);
        } else {
            callbackContext.error("Expected one non-empty string argument.");
        }
    }

    private void reverseGeocode(double latitude, double longitude, CallbackContext callbackContext)
    {

        if (latitude == 0 || longitude == 0) {
            PluginResult r = new PluginResult(PluginResult.Status.ERROR, "Expected two non-empty double arguments.");
            callbackContext.sendPluginResult(r);
            return;
        }

        if (!Geocoder.isPresent()) {
            PluginResult r = new PluginResult(PluginResult.Status.ERROR, "Geocoder is not present on this device/emulator.");
            callbackContext.sendPluginResult(r);
            return;
        }

        geocoder = new Geocoder(cordova.getActivity().getApplicationContext(), Locale.getDefault());

        try {
            List<Address> geoResults = geocoder.getFromLocation(latitude, longitude, 1);
            if (geoResults.size() > 0) {
                Address address = geoResults.get(0);

                // https://developer.android.com/reference/android/location/Address.html
                JSONObject resultObj = new JSONObject();
                resultObj.put("countryCode", address.getCountryCode());
                resultObj.put("countryName", address.getCountryName());
                resultObj.put("postalCode", address.getPostalCode());
                resultObj.put("administrativeArea", address.getAdminArea());
                resultObj.put("subAdministrativeArea", address.getSubAdminArea());
                resultObj.put("locality", address.getLocality());
                resultObj.put("subLocality", address.getSubLocality());
                resultObj.put("thoroughfare", address.getThoroughfare());
                resultObj.put("subThoroughfare", address.getSubThoroughfare());

                callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, resultObj));
            } else {
                PluginResult r = new PluginResult(PluginResult.Status.ERROR, "Cannot get an address.");
                callbackContext.sendPluginResult(r);
            }
        } catch (Exception e) {
            PluginResult r = new PluginResult(PluginResult.Status.ERROR, "Geocoder:getFromLocation Error: " + e.getMessage());
            callbackContext.sendPluginResult(r);
        }
    }

    private void getWineById(String language, String id, CallbackContext callbackContext)
    {
        JSONObject wineJson = cofficeInstance.getWineById(language, id);
        PluginResult r = null;

        if (wineJson != null) {
            r = new PluginResult(PluginResult.Status.OK, wineJson);
        } else {
            r = new PluginResult(PluginResult.Status.ERROR, "Wine not found");
        }
        callbackContext.sendPluginResult(r);
    }

    private void getEstateById(String language, String id, CallbackContext callbackContext)
    {
        JSONObject estateJson = cofficeInstance.getEstateById(language, id);
        PluginResult r = null;

        if (estateJson != null) {
            r = new PluginResult(PluginResult.Status.OK, estateJson);
        } else {
            r = new PluginResult(PluginResult.Status.ERROR, "Estate not found");
        }
        callbackContext.sendPluginResult(r);
    }

    private void clearAll(boolean clearImages)
    {
        cofficeInstance.clearAll(clearImages);
    }

    private void downloadAll(String lang, boolean downloadImages,
                             final CallbackContext callbackContext)
    {
        Downloader.ProgressListener stepProgress = new Downloader.ProgressListener()
        {
            @Override
            public void onProgress(float progress)
            {
                callbackContext.sendPluginResult(getProgressResult(progress, 0));
            }
        };

        Downloader.ProgressListener currentProgress = new Downloader.ProgressListener()
        {
            @Override
            public void onProgress(float progress)
            {
                callbackContext.sendPluginResult(getProgressResult(progress, 1));
            }
        };

        Downloader.FinishListener<JSONArray> finishListener =
                new Downloader.FinishListener<JSONArray>()
                {
                    @Override
                    public void onDownloadCompleted(JSONArray result)
                    {
                        callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, result));
                    }

                    @Override
                    public void onDownloadFailed(int errorCode, String errorMessage)
                    {
                        callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.ERROR,
                                errorMessage));
                    }
                };

        cofficeInstance.downloadAll(lang, downloadImages, stepProgress, currentProgress,
                finishListener);
    }

    private void downloadAllMedia(JSONArray array, final CallbackContext callbackContext)
    {
        Downloader.ProgressListener progressListener = new Downloader.ProgressListener()
        {
            @Override
            public void onProgress(float progress)
            {
                callbackContext.sendPluginResult(getProgressResult(progress));
            }
        };

        Downloader.FinishListener<JSONArray> finishListener =
                new Downloader.FinishListener<JSONArray>()
                {
                    @Override
                    public void onDownloadCompleted(JSONArray result)
                    {
                        callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, result));
                    }

                    @Override
                    public void onDownloadFailed(int errorCode, String errorMessage)
                    {
                        callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.ERROR,
                                errorMessage));
                    }
                };

        cofficeInstance.downloadAllMedia(array, progressListener, finishListener);
    }

    private void getImagesByUrl(String imageUrl, CallbackContext callbackContext)
    {
        String imagePath = cofficeInstance.getImageByUrl(imageUrl);
        if(TextUtils.isEmpty(imagePath)) {
            callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.ERROR,
                    "Image not found"));
        } else {
            callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, imagePath));
        }
    }

    private void getImageMap(boolean useCordovaPath, CallbackContext callbackContext) {
        JSONObject imageMap = cofficeInstance.getImagesMap();
        if(useCordovaPath) {
            Iterator<String> keys = imageMap.keys();
            LocalFilesystem fileSystem = new LocalFilesystem("persistent", webView.getContext(),
                    webView.getResourceApi(), FileUtils.getImageCacheDir());

            while(keys.hasNext()) {
                String key = keys.next();
                String value = imageMap.optString(key);
                if(TextUtils.isEmpty(value)) continue;
                try {
//                    if(!value.startsWith("file")) {
//                        value = "file://" + value;
//                    }
                    Uri localUri = Uri.parse(value);
                    String newValue = fileSystem.toLocalUri(localUri).toString();
                    imageMap.put(key, newValue);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, imageMap));
    }

    private void downloadWines(final String language, final boolean downloadImages,
                               final CallbackContext callbackContext)
    {
        Downloader.ProgressListener progressListener = new Downloader.ProgressListener()
        {
            @Override
            public void onProgress(float progress)
            {
                callbackContext.sendPluginResult(getProgressResult(progress));
            }
        };

        Downloader.FinishListener<JSONArray> finishListener =
                new Downloader.FinishListener<JSONArray>()
                {
                    @Override
                    public void onDownloadCompleted(JSONArray result)
                    {
                        callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, result));
                    }

                    @Override
                    public void onDownloadFailed(int errorCode, String errorMessage)
                    {
                        callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.ERROR,
                                errorMessage));
                    }
                };

        cofficeInstance.downloadWines(language, downloadImages, progressListener, finishListener);
    }

    private void getWinesTypeAhead(String language, String term, CallbackContext callbackContext)
    {
        JSONArray winesTypeAhead = cofficeInstance.getWinesTypeAhead(language, term);
        callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, winesTypeAhead));
    }

    private void getWines(String language, String term, int limit, int offset, String sort,
                          boolean sortDirection, CallbackContext callbackContext)
    {
        JSONArray wineArray = cofficeInstance.getWines(language, term, limit, offset, sort,
                sortDirection);
        callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, wineArray));

    }

    private void downloadEstates(final String language,
                                 final boolean downloadImages,
                                 final CallbackContext callbackContext)
    {
        Downloader.ProgressListener progressListener = new Downloader.ProgressListener()
        {
            @Override
            public void onProgress(float progress)
            {
                callbackContext.sendPluginResult(getProgressResult(progress));
            }
        };

        Downloader.FinishListener<JSONArray> finishListener =
                new Downloader.FinishListener<JSONArray>()
                {
                    @Override
                    public void onDownloadCompleted(JSONArray result)
                    {
                        callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK,
                                result));
                    }

                    @Override
                    public void onDownloadFailed(int errorCode, String errorMessage)
                    {
                        callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.ERROR,
                                errorMessage));
                    }
                };

        cofficeInstance.downloadEstates(language, downloadImages, progressListener, finishListener);
    }

    private void getEstatesTypeAhead(String language, String term, CallbackContext callbackContext)
    {
        JSONArray estatesTypeAhead = cofficeInstance.getEstatesTypeAhead(language, term);
        callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, estatesTypeAhead));
    }

    private void getEstates(String language, String term, int limit, int offset, String sort,
                            boolean sortDirection, CallbackContext callbackContext)
    {
        JSONArray estateArray = cofficeInstance.getEstates(language, term, limit, offset, sort,
                sortDirection);
        callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, estateArray));
    }

    private void getTasteById(String id, CallbackContext callbackContext)
    {
        JSONObject taste = cofficeInstance.getTasteById(id);
        PluginResult r = null;

        if (taste != null) {
            r = new PluginResult(PluginResult.Status.OK, taste);
        } else {
            r = new PluginResult(PluginResult.Status.ERROR, "Taste not found");
        }
        callbackContext.sendPluginResult(r);

    }

    private void insertTaste(JSONObject taste, CallbackContext callbackContext)
    {
        long tasteId = cofficeInstance.insertTaste(taste);
        PluginResult r = null;

        if (tasteId >= 0) {
            r = new PluginResult(PluginResult.Status.OK, taste);
        } else {
            r = new PluginResult(PluginResult.Status.ERROR, "Taste not inserted");
        }
        callbackContext.sendPluginResult(r);
    }

    private void editTaste(long id, JSONObject taste, CallbackContext callbackContext)
    {
        boolean edited = cofficeInstance.editTaste(id, taste);
        PluginResult r = null;

        if (edited) {
            r = new PluginResult(PluginResult.Status.OK, taste);
        } else {
            r = new PluginResult(PluginResult.Status.ERROR, "Taste not inserted");
        }
        callbackContext.sendPluginResult(r);
    }

    private void deleteTaste(long id, CallbackContext callbackContext)
    {
        boolean deleted = cofficeInstance.deleteTaste(id);
        PluginResult r = null;

        if (deleted) {
            r = new PluginResult(PluginResult.Status.OK, true);
        } else {
            r = new PluginResult(PluginResult.Status.ERROR, "Taste not inserted");
        }
        callbackContext.sendPluginResult(r);
    }

    private void getTastesTypeAhead(String term, CallbackContext callbackContext)
    {
        JSONArray tastesTypeAhead = cofficeInstance.getTastesTypeAhead(term);
        callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, tastesTypeAhead));
    }

    private void getTastes(String term, int limit, int offset, String sort,
                           boolean sortDirection, CallbackContext callbackContext)
    {
        JSONArray tastes = cofficeInstance.getTastes(term, limit, offset, sort, sortDirection);
        callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, tastes));

    }

    private PluginResult getProgressResult(float progressValue)
    {
        return getProgressResult(progressValue, -1);
    }

    private PluginResult getProgressResult(float progressValue, int progressIndex)
    {
        JSONObject progessJson = new JSONObject();

        try {
            progessJson.put("type", "progress");
            if(progressIndex == -1) {
                progessJson.put("progress", progressValue);
            } else {
                progessJson.put("progress" + (progressIndex + 1), progressValue);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        PluginResult r = new PluginResult(PluginResult.Status.OK, progessJson);
        r.setKeepCallback(true);
        return r;
    }

}



