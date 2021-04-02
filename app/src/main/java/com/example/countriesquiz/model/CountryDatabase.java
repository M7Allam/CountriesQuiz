package com.example.countriesquiz.model;

import android.content.Context;

import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

public class CountryDatabase extends SQLiteAssetHelper {

    //Database Info
    public static final String DB_NAME = "Country_database.db";
    public static final int DB_VERSION = 1;
    //Country Table
    public static final String COUNTRY_TABLE_NAME = "Country";
    public static final String COUNTRY_COL_COUNTRY_ID = "Country_ID";
    public static final String COUNTRY_COL_COUNTRY_NAME = "Country_Name";
    public static final String COUNTRY_COL_CAPITAL = "Country_Capital";
    public static final String COUNTRY_COL_CONTINENT = "Country_Continent";
    public static final String COUNTRY_COL_LEVEL = "Country_Level";
    //Cities Table
    public static final String CITIES_TABLE_NAME = "City";
    public static final String CITIES_COL_CITIES_ID = "City_ID";
    public static final String CITIES_COL_CITIES_NAME = "City_Name";


    public CountryDatabase(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }
}
