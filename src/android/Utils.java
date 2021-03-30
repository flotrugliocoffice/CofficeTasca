package com.tasca.android;

import android.text.TextUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Created by carlo on 16/08/2017.
 */

public class Utils
{
    public static final String DEFAULT_LANGUAGE = "en";
    public static final String[] ACCEPTED_LANGUAGES = new String[]{"it", "en", "de"};

    private static final SimpleDateFormat TASCA_DATE_FORMAT  =
            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");



    /**
     * Parse the param dateString and convert the result to a {@link java.util.Date}
     * using TASCA_DATE_FORMAT constant, then return the Date.getTime() timestamp in milliseconds;
     * @param dateString a valid date String, formatted as "yyyy-MM-dd HH:mm:ss"
     * @return the timestamp in millis of the param dateString, or -1 if dateString is not valid.
     */
    public static final long dateStringToMillis(String dateString)
    {
        if(!TextUtils.isEmpty(dateString)) {
            try {
                return TASCA_DATE_FORMAT.parse(dateString).getTime();
            } catch (ParseException e) {
                e.printStackTrace();
                //not a valid date
            }
        }

        return -1;
    }

    /**
     * return language @see getLanguage() using the current locale
     *
     * @return server request language string or DEFAULT_LANGUAGE
     * if the current locale is not supported
     */
    public String getLanguage()
    {
        return getLanguage(null);
    }

    /**
     * return the request language if available, if not it will return the DEFAULT_LANGUAGE
     *
     * @return server request language string or DEFAULT_LANGUAGE
     */
    public static String getLanguage(String requestedLanguage)
    {
        if (TextUtils.isEmpty(requestedLanguage)) {
            requestedLanguage = Locale.getDefault().getLanguage();
        }
        if (!TextUtils.isEmpty(requestedLanguage)) {
            for (String language : ACCEPTED_LANGUAGES) {
                if (requestedLanguage.contains(language)) {
                    return language;
                }
            }
        }
        return DEFAULT_LANGUAGE;
    }
}
