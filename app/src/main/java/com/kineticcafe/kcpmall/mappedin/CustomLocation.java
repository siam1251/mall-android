package com.kineticcafe.kcpmall.mappedin;

import android.util.Log;

import com.mappedin.jpct.Logger;
import com.mappedin.sdk.ImageCollection;
import com.mappedin.sdk.Location;
import com.mappedin.sdk.RawData;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by paul on 2016-06-14.
 */
public class CustomLocation extends Location {
    public String description;
    public ImageCollection logo;
    private String externalID;

    private final String TYPE_AMENITIES = "amenities";
    private final String TYPE_AMENITY = "amenity";
    private final String TYPE = "type";
    private final String TYPE_EXTERNAL_ID = "externalId";

    private static HashMap<String, ArrayList<CustomLocation>> amenityHashmap = new HashMap<>();
    private static HashMap<String, CustomLocation> locationHashmap = new HashMap<>();

    public CustomLocation(RawData rawData) throws Exception {
        super(rawData);
        try {
            description = rawData.string("description");
            logo = rawData.imageCollection("logo");
            if(rawData.string(TYPE_EXTERNAL_ID) != null) {
                externalID = rawData.stringForce(TYPE_EXTERNAL_ID);
                locationHashmap.put(externalID, this);
            }
            if(rawData.string(TYPE) != null && rawData.string(TYPE).equals(TYPE_AMENITIES)) {
                String amenity = rawData.string(TYPE_AMENITY);
                if(amenity.equals("atm")){
                    String a = "ef";
                    Log.d("ATM", "FOUND!");
                }

                ArrayList<CustomLocation> amenityList;
                if(amenityHashmap.containsKey(amenity)) amenityList = amenityHashmap.get(amenity);
                else amenityList = new ArrayList<>();
                amenityList.add(this);
                amenityHashmap.put(amenity, amenityList);
            }



        } catch (Exception var3) {
            Logger.log("create location failed");
        }
    }

    public static HashMap<String, ArrayList<CustomLocation>> getAmenityHashMap() {
        return amenityHashmap;
    }

    public static HashMap<String, CustomLocation> getLocationHashMap() {
        return locationHashmap;
    }

    public String getExternalID() {
        if(externalID == null) return "-1";
        return externalID;
    }
}
