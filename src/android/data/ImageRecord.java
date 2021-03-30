package com.tasca.android.data;

/**
 * Created by carlo on 18/09/2017.
 */

public class ImageRecord
{
    public final static String TABLE_NAME = "images";
    public final static String COLUMN_REMOTE = "remote";
    public final static String COLUMN_LOCAL = "local";

    public static final String TABLE_CREATE = "CREATE TABLE " + TABLE_NAME + "(" +
            "  " + COLUMN_REMOTE                   + " TEXT NOT NULL DEFAULT ''," +
            "  " + COLUMN_LOCAL                    + " TEXT NOT NULL DEFAULT ''," +
            " " +
            "  PRIMARY KEY (" + COLUMN_REMOTE + ")" +
            ");";

    private String remoteUrl;
    private String local;

    public String getRemoteUrl()
    {
        return remoteUrl;
    }

    public void setRemoteUrl(String remoteUrl)
    {
        this.remoteUrl = remoteUrl;
    }

    public String getLocal()
    {
        return local;
    }

    public void setLocal(String local)
    {
        this.local = local;
    }
}
