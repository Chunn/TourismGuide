package com.rom.rm.hotsale;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PlaceJsonParser {
    //phân tích 1 thằng place json object
    private HashMap<String,String> getPlace(JSONObject jsonObject) {
        HashMap<String,String> place=new HashMap<>();

        String id="";
        String reference="";
        String description="";

        try{
            description=jsonObject.getString("description");
            reference=jsonObject.getString("reference");
            id=jsonObject.getString("id");

            //Thêm vào hashmap
            place.put("description",description);
            place.put("reference",reference);
            place.put("_id",id);


        } catch (JSONException e) {
            e.printStackTrace();
        }
        return place;
    }
    private List<HashMap<String,String>> getPlaces(JSONArray jsonArray){
        HashMap<String,String> place=null;
        List<HashMap<String,String>> places= new ArrayList<>();
        for (int i=0; i<jsonArray.length();i++){
            try {
                place=getPlace((JSONObject) jsonArray.get(i));
                places.add(place);
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
            jsonObject=new JSONObject(jsonData);
            jsonArray=jsonObject.getJSONArray("predictions"); //Lấy tất cả giá trị dự kiến trong mảng json
        }catch (JSONException e){
            e.printStackTrace();
        }
        return getPlaces(jsonArray);
    }


}
