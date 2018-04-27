package com.rom.rm.hotsale;

import android.content.Context;
import android.content.DialogInterface;
import android.location.Location;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

public class GetNearbyPlacesData extends AsyncTask<Object,String,String> {
    private String googlePlacesData;
    private GoogleMap mMap;
    private String url;
    private int DEFAULT_ZOOM=15;
    @Override
    protected String doInBackground(Object... objects) {
        mMap=(GoogleMap) objects[0];
        url=(String)objects[1];

        DownloadURL downloadURL=new DownloadURL();
        try {
            googlePlacesData=downloadURL.readUrl(url);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return googlePlacesData;
    }

    @Override
    protected void onPostExecute(String s) {
       List<HashMap<String,String>> nearbyPlaces=null;
       DataParser dataParser= new DataParser();
       nearbyPlaces = dataParser.parse(s);
       showNearbyPlaces(nearbyPlaces);
    }

    private void showNearbyPlaces(List<HashMap<String,String>> nearbyPlaces){
        MarkerOptions markerOptions;
        HashMap<String,String> googlePlace;
        String placeName;
        String vicinity;
        LatLng latLng;
        double lat;
        double lng;
        for (int i=0; i<nearbyPlaces.size();i++){
            markerOptions= new MarkerOptions();
            googlePlace = nearbyPlaces.get(i);

            //Lấy value tương ứng với key
            placeName = googlePlace.get("place_name");
            vicinity = googlePlace.get("vicinity");
            lat=Double.parseDouble(googlePlace.get("lat"));
            lng=Double.parseDouble(googlePlace.get("lng"));

            latLng= new LatLng(lat,lng);
            markerOptions.position(latLng);
            markerOptions.title(placeName+" : "+vicinity);
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE));

            mMap.addMarker(markerOptions);
            LatLng mLatLng = new LatLng(MainActivity.getLatitude(),MainActivity.getLongitude());
            MarkerOptions mMarkerOptions= new MarkerOptions()
                    .position(mLatLng)
                    .title("You're here")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
            mMap.addMarker(mMarkerOptions);
            mMap.moveCamera(CameraUpdateFactory.newLatLng(mLatLng));
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(mLatLng)             // Sets the center of the map to location user
                    .zoom(DEFAULT_ZOOM)                   // Sets the zoom
                    .bearing(90)                // Sets the orientation of the camera to east
                    .tilt(40)                   // Sets the tilt of the camera to 30 degrees
                    .build();                   // Creates a CameraPosition from the builder
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        }


    }

}