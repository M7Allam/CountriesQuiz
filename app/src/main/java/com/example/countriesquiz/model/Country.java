package com.example.countriesquiz.model;

public class Country {

    private int countryID;
    private String countryName;
    private String countryCapital;
    private String countryContinent;
    private String countryLevel;

    public Country(int countryID, String countryName, String countryCapital, String countryContinent, String countryLevel) {
        this.countryID = countryID;
        this.countryName = countryName;
        this.countryCapital = countryCapital;
        this.countryContinent = countryContinent;
        this.countryLevel = countryLevel;
    }

    public Country(String countryName, String countryCapital, String countryContinent, String countryLevel) {
        this.countryName = countryName;
        this.countryCapital = countryCapital;
        this.countryContinent = countryContinent;
        this.countryLevel = countryLevel;
    }

    public int getCountryID() {
        return countryID;
    }

    public void setCountryID(int countryID) {
        this.countryID = countryID;
    }

    public String getCountryName() {
        return countryName;
    }

    public void setCountryName(String countryName) {
        this.countryName = countryName;
    }

    public String getCountryCapital() {
        return countryCapital;
    }

    public void setCountryCapital(String countryCapital) {
        this.countryCapital = countryCapital;
    }

    public String getCountryContinent() {
        return countryContinent;
    }

    public void setCountryContinent(String countryContinent) {
        this.countryContinent = countryContinent;
    }

    public String getCountryLevel() {
        return countryLevel;
    }

    public void setCountryLevel(String countryLevel) {
        this.countryLevel = countryLevel;
    }
}
