package com.rom.rm.hotsale;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

/**
 * Created by huynh on 2018-05-04.
 */

public class GetDetailsOfPlaces extends AsyncTask<String, Void, String> {

    private String googlePlacesData;
    private Context context;
    private int imgWidth = 600;

    public GetDetailsOfPlaces(Context context) {
        this.context = context;
    }

    @Override
    protected String doInBackground(String... strings) {

        DownloadURL downloadURL = new DownloadURL();
        try {
            googlePlacesData = downloadURL.readUrl(strings[0]);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return googlePlacesData;
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        JSONObject jsonObject = null;
        String address = "";
        String name = "";
        String contact = "";
        String time_open = "";
        String photoReference = "";
        int openNow = -1;

        try {
            jsonObject = new JSONObject(s);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (jsonObject != null && jsonObject.has("result")) {
            try {
                if (jsonObject.getJSONObject("result").has("formatted_address")) {
                    address = jsonObject.getJSONObject("result").optString("formatted_address");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                if (jsonObject.getJSONObject("result").has("name")) {
                    name = jsonObject.getJSONObject("result").optString("name");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                if (jsonObject.getJSONObject("result").has("website")) {
                    contact = jsonObject.getJSONObject("result").optString("website");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                if (jsonObject.getJSONObject("result").has("opening_hours") && jsonObject.getJSONObject("result").getJSONObject("opening_hours").has("weekday_text")) {
                    JSONArray jsonArray = jsonObject.getJSONObject("result").getJSONObject("opening_hours").optJSONArray("weekday_text");

                    for (int i = 0; i < jsonArray.length(); i++) {
                        String elm = jsonArray.optString(i);
                        time_open += elm;
                        if (i != jsonArray.length() - 1) {
                            time_open += "\n";
                        }
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                if (jsonObject.getJSONObject("result").has("photos")) {
                    JSONArray jsonArray = jsonObject.getJSONObject("result").getJSONArray("photos");
                    for (int i = 0; i < jsonArray.length(); i++) {
                        photoReference = jsonArray.optJSONObject(i).optString("photo_reference");
                        break;
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            Dialog dialog = new Dialog(context);
            dialog.setContentView(R.layout.dialog_details);

            TextView txt1, txt2, txt3, txt4;
            txt1 = dialog.findViewById(R.id.txtTenDiaDiem);
            txt2 = dialog.findViewById(R.id.txtDiaChi);
            txt3 = dialog.findViewById(R.id.txtLienHe);
            txt4 = dialog.findViewById(R.id.txtGioMoCua);

            LinearLayout linear1 = dialog.findViewById(R.id.linear1);

            ImageView imgPhotos = dialog.findViewById(R.id.imgPhotos);

            Picasso.get().load(photoReference.isEmpty() ? "http://leeford.in/wp-content/uploads/2017/09/image-not-found.jpg" : getPhotosURL(imgWidth, photoReference)).transform(cropPosterTransformation).into(imgPhotos);

            Log.d("AAA", photoReference.isEmpty() ? "http://leeford.in/wp-content/uploads/2017/09/image-not-found.jpg" : getPhotosURL(imgWidth, photoReference));
//            if (openNow == -1) {
//                linear1.setVisibility(View.GONE);
//            }.

            txt1.setText((name != null) ? name : "");
            txt2.setText((address != null) ? address : "");
            txt3.setText((contact != null) ? contact : "");
            txt4.setText(time_open);
            dialog.show();
        }
    }

    private Transformation cropPosterTransformation = new Transformation() {

        @Override
        public Bitmap transform(Bitmap source) {
            int targetWidth = imgWidth;
            double aspectRatio = (double) source.getHeight() / (double) source.getWidth();
            int targetHeight = (int) (targetWidth * aspectRatio);
            Bitmap result = Bitmap.createScaledBitmap(source, targetWidth, targetHeight, false);
            if (result != source) {
                // Same bitmap is returned if sizes are the same
                source.recycle();
            }
            return result;
        }

        @Override
        public String key() {
            return "cropPosterTransformation" + imgWidth;
        }
    };

    private String getPhotosURL(int maxWidth, String photoReference) {
        String url = "https://maps.googleapis.com/maps/api/place/photo?";
        String width = "maxwidth=" + maxWidth;
        String reference = "&photoreference=" + photoReference;
        String key = "&key=" + "AIzaSyBpTLBynSv6JC0kBIRRNOmdsVNdsOsD_Do";
        return url + width + reference + key;
    }
}
