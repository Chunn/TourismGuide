package com.rom.rm.hotsale;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class NearbyPlaceParser {
    //HashMap là 1 kiểu đối tượng lưu giá trị theo cặp key, value (key là duy nhất)
    private HashMap <String,String> getPlace(JSONObject googlePlacesJson){
        HashMap <String,String> googlePlacesMap=new HashMap<>();
        String placeName="-NA-";
        String vicinity="-NA-";
        String latitude="";    //Vĩ độ
        String longitude="";   //Kinh độ
        String reference="";
        String mainType = "";
        String placeId = "";
        try {
            if (!googlePlacesJson.isNull("name"))
            {
                placeName=googlePlacesJson.getString("name");
            }
            if (!googlePlacesJson.isNull("vicinity"))
            {
                vicinity=googlePlacesJson.getString("vicinity");
            }
            latitude=googlePlacesJson.getJSONObject("geometry").getJSONObject("location").getString("lat");
            longitude=googlePlacesJson.getJSONObject("geometry").getJSONObject("location").getString("lng");
            reference=googlePlacesJson.getString("reference");
            mainType = googlePlacesJson.getJSONArray("types").getString(0);
            placeId = googlePlacesJson.getString("place_id");

            //Đưa key và value vào hashMap
            googlePlacesMap.put("place_name", placeName);
            googlePlacesMap.put("vicinity", vicinity);
            googlePlacesMap.put("lat", latitude);
            googlePlacesMap.put("lng", longitude);
            googlePlacesMap.put("reference", reference);
            googlePlacesMap.put("main_type", mainType);
            googlePlacesMap.put("place_id", placeId);


        } catch (JSONException e) {
            e.printStackTrace();
        }
        return googlePlacesMap;
    }
    private List<HashMap<String,String>> getPlaces(JSONArray jsonArray){
        List<HashMap<String,String>> places= new ArrayList<>();
        HashMap<String,String> placeMap=null;

        for (int i=0; i<jsonArray.length();i++){

            try {
                placeMap=getPlace((JSONObject) jsonArray.get(i));
                places.add(placeMap);

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
        return places;
    }

    public List<HashMap<String,String>> parse (String jsonData){
        JSONArray jsonArray=null;
        JSONObject jsonObject;
        try {
            jsonObject = new JSONObject(jsonData);
            jsonArray=jsonObject.getJSONArray("results");

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return getPlaces(jsonArray);
    }
 }
