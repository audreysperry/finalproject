package com.audreysperry.finalproject.googleApi;


import feign.Param;
import feign.RequestLine;

public interface GeoCodingInterface {
    @RequestLine("GET /maps/api/geocode/json?address={address}&key={key}")
    GeoCodingResponse geoCodingResponse(@Param("address") String address, @Param("key") String key);
}
