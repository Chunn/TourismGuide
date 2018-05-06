package com.rom.rm.hotsale;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.model.Marker;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by huynh on 2018-05-04.
 */

public class CustomInfoWindowAdapter implements InfoWindowAdapter {

    private View contentsView;
    private Context context;

    public CustomInfoWindowAdapter(Context context) {
        this.context = context;
        LayoutInflater inflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.contentsView = inflater.inflate(R.layout.layout_info_window, null);
    }

    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }

    @Override
    public View getInfoContents(Marker marker) {
        TextView txtTitle, txtAddress;
        txtTitle = contentsView.findViewById(R.id.txtTitleInfoWindow);
        txtAddress = contentsView.findViewById(R.id.txtAddressInfoWindow);

        txtTitle.setText(marker.getTitle());

        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(marker.getSnippet());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (jsonObject != null && jsonObject.has("vicinity")) {
            txtAddress.setText(jsonObject.optString("vicinity"));
        }

        return contentsView;
    }


}
