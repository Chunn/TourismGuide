package com.rom.rm.hotsale;

import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public class PlaceProvider extends ContentProvider {
    public static final String AUTHORITY = "com.rom.rm.hotsale.PlaceProvider";

    public static final Uri SEARCH_URI = Uri.parse("content://"+AUTHORITY+"/search");

    public static final Uri DETAILS_URI = Uri.parse("content://"+AUTHORITY+"/details");

    private static final int SEARCH = 1;
    private static final int SUGGESTIONS = 2;
    private static final int DETAILS = 3;

    // Obtain browser key from https://code.google.com/apis/console
    String mKey = "key=YOUR_BROWSER_KEY";

    // Defines a set of uris allowed with this content provider
    private static final UriMatcher mUriMatcher = buildUriMatcher();

    private static UriMatcher buildUriMatcher() {

        UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

        // URI for "Go" button
        uriMatcher.addURI(AUTHORITY, "search", SEARCH);

        // URI for suggestions in Search Dialog
        uriMatcher.addURI(AUTHORITY, SearchManager.SUGGEST_URI_PATH_QUERY, SUGGESTIONS);

        // URI for Details
        uriMatcher.addURI(AUTHORITY, "details", DETAILS);

        return uriMatcher;
    }
    @Override
    public boolean onCreate() {
        return false;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        return null;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }
}
