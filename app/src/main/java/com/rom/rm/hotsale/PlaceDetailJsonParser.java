package com.rom.rm.hotsale;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PlaceDetailJsonParser {
    /** Receives a JSONObject and returns a list */
    public List<HashMap<String,String>> parse(String jsonData){

        String lat="";
        String lng="";
        String formattedAddress = "";
        JSONObject jsonObject= null;
        try {
            jsonObject = new JSONObject(jsonData);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        HashMap<String, String> place = new HashMap<String, String>();
        List<HashMap<String, String>> places = new ArrayList<HashMap<String,String>>();

        try {
            lat = jsonObject.getJSONObject("result").getJSONObject("geometry").getJSONObject("location").getString("lat");
            lng = jsonObject.getJSONObject("result").getJSONObject("geometry").getJSONObject("location").getString("lng");
            formattedAddress = (String) jsonObject.getJSONObject("result").get("formatted_address");

        } catch (JSONException e) {
            e.printStackTrace();
        }catch(Exception e){
            e.printStackTrace();
        }

        place.put("lat", lat);
        place.put("lng", lng);
        place.put("formatted_address",formattedAddress);

        places.add(place);
        return places;
    }
}
