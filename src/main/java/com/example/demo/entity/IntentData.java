package com.example.demo.entity;

public class IntentData {
    private String intent;
    private String place;
    private String fromPlace;
    private String toPlace;
    private String response;
    private String poiType;      // NEW for dynamic POI search
    // Getters and setters
    public String getIntent() { return intent; }
    public void setIntent(String intent) { this.intent = intent; }
    public String getPlace() { return place; }
    public void setPlace(String place) { this.place = place; }
    public String getFromPlace() { return fromPlace; }
    public void setFromPlace(String fromPlace) { this.fromPlace = fromPlace; }
    public String getToPlace() { return toPlace; }
    public void setToPlace(String toPlace) { this.toPlace = toPlace; }
    public String getResponse() { return response; }
    public void setResponse(String response) { this.response = response; }


    // getters/setters for all fields as usual
    public String getPoiType() { return poiType; }
    public void setPoiType(String poiType) { this.poiType = poiType; }
}