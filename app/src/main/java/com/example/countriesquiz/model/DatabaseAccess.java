package com.example.countriesquiz.model;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.countriesquiz.view.MainActivity;

import java.util.ArrayList;

public class DatabaseAccess {

    private SQLiteDatabase database;
    private SQLiteOpenHelper openHelper;
    private static DatabaseAccess instance;

    private DatabaseAccess(Context context) {

        this.openHelper = new CountryDatabase(context);
    }

    public static DatabaseAccess getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseAccess(context);
        }
        return instance;
    }

    public void openDB() {
        this.database = this.openHelper.getReadableDatabase();
    }

    public void closeDB() {
        if (this.database != null) {
            this.database.close();
        }
    }

    public ArrayList<Country> getCountryDataFromDB(String level) {
        ArrayList<Country> list = new ArrayList<>();
        Cursor cursor;
        try {
            if (MainActivity.language.equalsIgnoreCase("ar")) {
                cursor = database.rawQuery("SELECT * FROM " + CountryDatabase.COUNTRY_TABLE_NAME_AR + " WHERE " +
                        CountryDatabase.COUNTRY_COL_LEVEL + "=?", new String[]{level});
            } else {
                cursor = database.rawQuery("SELECT * FROM " + CountryDatabase.COUNTRY_TABLE_NAME + " WHERE " +
                        CountryDatabase.COUNTRY_COL_LEVEL + "=?", new String[]{level});
            }

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    String c_name = cursor.getString(cursor.getColumnIndex(CountryDatabase.COUNTRY_COL_COUNTRY_NAME));
                    String c_capital = cursor.getString(cursor.getColumnIndex(CountryDatabase.COUNTRY_COL_CAPITAL));
                    String c_continent = cursor.getString(cursor.getColumnIndex(CountryDatabase.COUNTRY_COL_CONTINENT));
                    String c_level = cursor.getString(cursor.getColumnIndex(CountryDatabase.COUNTRY_COL_LEVEL));
                    list.add(new Country(c_name, c_capital, c_continent, c_level));
                } while (cursor.moveToNext());
                cursor.close();
            }

        } catch (SQLException e) {
            System.out.println("Error SQL : " + e.getMessage());
        }



        return list;
    }

    public ArrayList<City> getCityDataFromDB() {
        ArrayList<City> list = new ArrayList<>();
        Cursor cursor = database.rawQuery("SELECT * FROM " + CountryDatabase.CITIES_TABLE_NAME, null);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                int cityID = cursor.getInt(cursor.getColumnIndex(CountryDatabase.CITIES_COL_CITIES_ID));
                String cityName = cursor.getString(cursor.getColumnIndex(CountryDatabase.CITIES_COL_CITIES_NAME));
                int countryID = cursor.getInt(cursor.getColumnIndex(CountryDatabase.COUNTRY_COL_COUNTRY_ID));
                list.add(new City(cityID, cityName, countryID));
            } while (cursor.moveToNext());
            cursor.close();
        }


        return list;
    }
}
