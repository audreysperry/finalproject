package com.audreysperry.finalproject.models;

public class ApiKey {

    private final String API_Key = System.getenv("GOOGLE_MAPS_GEOCODING_API_KEY");

    public String getAPI_Key() {
        return API_Key;
    }

    public ApiKey() {}
}
