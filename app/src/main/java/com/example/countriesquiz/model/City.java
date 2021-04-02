package com.example.countriesquiz.model;

public class City {

    private int cityID;
    private String cityName;
    private int countryID;

    public City(int cityID, String cityName, int countryID) {
        this.cityID = cityID;
        this.cityName = cityName;
        this.countryID = countryID;
    }

    public City(String cityName, int countryID) {
        this.cityName = cityName;
        this.countryID = countryID;
    }

    public int getCityID() {
        return cityID;
    }

    public void setCityID(int cityID) {
        this.cityID = cityID;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public int getCountryID() {
        return countryID;
    }

    public void setCountryID(int countryID) {
        this.countryID = countryID;
    }
}
