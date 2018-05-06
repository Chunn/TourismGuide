package com.rom.rm.hotsale;

import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;

public class PlaceProvider extends ContentProvider {
    public static final String AUTHORITY = "com.rom.rm.hotsale.PlaceProvider"; //tên của content provider
    //(<prefix>://<authority>/<dataType>/<id>)
    //Uri định danh dữ liệu trong 1 provider
    public static final Uri SEARCH_URI = Uri.parse("content://"+AUTHORITY+"/search");

    public static final Uri DETAILS_URI = Uri.parse("content://"+AUTHORITY+"/details");

    private static final int SEARCH = 1;
    private static final int SUGGESTIONS = 2;
    private static final int DETAILS = 3;

    private static double longitude,latitude;

    String mKey = "&key=AIzaSyBpTLBynSv6JC0kBIRRNOmdsVNdsOsD_Do";

    //định nghĩa 1 set uri được truyền vào content provider
    private static final UriMatcher mUriMatcher = buildUriMatcher();
    //xác định chính xác mẫu URI nào được Content provider hỗ trợ
    private static UriMatcher buildUriMatcher() {

        UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

        // URI cho "Go" button
        uriMatcher.addURI(AUTHORITY, "search", SEARCH);

        // URI cho suggestions in Search Dialog
        uriMatcher.addURI(AUTHORITY, SearchManager.SUGGEST_URI_PATH_QUERY, SUGGESTIONS);

        // URI cho Details
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
        Cursor c=null;

        PlaceJsonParser placeJsonParser = null;
        PlaceDetailJsonParser placeDetailJsonParser=null;

        String jsonPlace="";
        String jsonPlaceDetail="";

        List<HashMap<String,String>> places=null;
        List<HashMap<String,String>> placeDetails=null;

        HashMap<String,String> place=null;
        HashMap<String,String> placeDetail=null;

        MatrixCursor mCursor=null;//tạo 1 cusor truy vấn tới dl k nằm trong dtb

        switch (mUriMatcher.match(uri)){
            case SEARCH:
                //ĐỊnh nghĩa con trỏ mCursor vs các cột "description,lat,lng"
                mCursor=new MatrixCursor(new String[]{"description","lat","lng"});

                placeJsonParser=new PlaceJsonParser();
                placeDetailJsonParser=new PlaceDetailJsonParser();

                jsonPlace=getPlace(selectionArgs); //Lấy place từ ggPlace API
                //Parser place (JSON->HashList)
                places=placeJsonParser.parse(jsonPlace);

                //Tìm lat &lng cho từng địa điểm bằng Google Place Detail API
                for (int i=0; i<places.size();i++){
                    place=places.get(i);
                    //get place detail từ ggPlace API
                    jsonPlaceDetail= getDetailPlace(place.get("reference"));
                    //parser detail (JSON=>list)
                    placeDetails=placeDetailJsonParser.parse(jsonPlaceDetail);

                    //tạo cursor vs các place
                    for (int j=0;j<placeDetails.size();j++){
                        placeDetail=placeDetails.get(j);
                        //Add vào matrix cursor
                        mCursor.addRow(new String[]{placeDetail.get("description"),placeDetail.get("lat"),placeDetail.get("lng")});
                    }
                }
                c=mCursor;
                break;
            case SUGGESTIONS:
                //Sử dụng giá trị từ cột SUGGEST_COLUMN_TEXT_1 để viết lại văn bản truy vấn.
                //
                mCursor = new MatrixCursor(new String[] { "_id", SearchManager.SUGGEST_COLUMN_TEXT_1, SearchManager.SUGGEST_COLUMN_INTENT_EXTRA_DATA } );
                placeJsonParser = new PlaceJsonParser();

                jsonPlace=getPlace(selectionArgs);

                places=placeJsonParser.parse(jsonPlace);

                for (int i=0; i<places.size();i++){
                    place=places.get(i);
                    mCursor.addRow(new String[]{Integer.toString(i),place.get("description"),place.get("reference")});
                }
                c=mCursor;
                break;
            case DETAILS:
                mCursor = new MatrixCursor(new String[] { "description","lat","lng" });
                placeDetailJsonParser = new PlaceDetailJsonParser();

                jsonPlaceDetail=getDetailPlace(selectionArgs[0]);

                placeDetails=placeDetailJsonParser.parse(jsonPlaceDetail);

                for (int i=0;i<placeDetails.size();i++){
                    placeDetail=placeDetails.get(i);
                    mCursor.addRow(new String[] {placeDetail.get("formatted_address"),placeDetail.get("lat"),placeDetail.get("lng")});
                }
                c=mCursor;
                break;
     }
        return c;
    }
    private String getURLPlace(String query){
        try {
            query="input="+ URLEncoder.encode(query,"utf8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String sensor="&sensor=false";
        String types = "&types=geocode";//loại tìm kiếm địa điểm
        // Building the parameters to the web service
        String parameters = query+types+sensor+mKey;
        String url = "https://maps.googleapis.com/maps/api/place/autocomplete/json?"+parameters;
        Log.d("Url",url);
       return url;
    }

    private String getURLDetailPlace(String ref){
        String reference="reference="+ref;
        String sensor="&sensor=false";
        String parameters =reference+sensor+mKey;
        String url = "https://maps.googleapis.com/maps/api/place/details/json?"+parameters;
        Log.d("Url",url);
        return url;
    }

    private String getPlace(String[]dataTransfer){

        String googlePlaceData="";//Lưu trữ dữ liệu từ web service
        String url= getURLPlace(dataTransfer[0]);
        DownloadURL downloadURL=new DownloadURL();
        try {
            googlePlaceData=downloadURL.readUrl(url); //chạy nền
        } catch (IOException e) {
            e.printStackTrace();
        }
        return googlePlaceData;
    }
    private String getDetailPlace(String ref){
        String googlePlaceDetailData="";
        String url=getURLDetailPlace(ref);
        DownloadURL downloadURL=new DownloadURL();
        try {
            googlePlaceDetailData=downloadURL.readUrl(url);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return googlePlaceDetailData;
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
