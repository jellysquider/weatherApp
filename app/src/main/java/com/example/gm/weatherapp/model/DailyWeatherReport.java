package com.example.gm.weatherapp.model;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DailyWeatherReport {

    public static final String WEATHER_CLOUDS = "Cloudy";
    public static final String WEATHER_SUNNY = "Sunny";
    public static final String WEATHER_SNOW = "Snow";
    public static final String WEATHER_SHOWERS = "Showers";
    public static final String WEATHER_RAIN = "Rain";
    public static final String WEATHER_FOG = "Fog";


    private String cityName;
    private String countryName;
    private int minTemp;
    private int maxTemp;
    private String weather;
    private String formatDate;

    public String getCityName() {
        return cityName;
    }

    public String getCountryName() {
        return countryName;
    }

    public int getMinTemp() {
        return minTemp;
    }

    public int getMaxTemp() {
        return maxTemp;
    }

    public String getWeather() {
        return weather;
    }

    public String getFormattedDate() {
        return formatDate;
    }

    public DailyWeatherReport(String cityName, String countryName, int minTemp, int maxTemp, String weather, String formatDate) throws ParseException {
        this.cityName = cityName;
        this.countryName = countryName;
        this.minTemp = minTemp;
        this.maxTemp = maxTemp;
        this.weather = weather;
        this.formatDate = formattedDate(formatDate);
    }

    public String formattedDate(String rawDate) throws ParseException {
        // convert raw date into formatted date

        DateFormat outputFormat = new SimpleDateFormat("EEE, MMM d", Locale.US);
        DateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss-hh:mm", Locale.US);

//        String inputText = "2012-11-17T00:00:00.000-05:00";
        Date date;
        date = inputFormat.parse(rawDate);
        String output = outputFormat.format(date);

        return output;
    }
}
