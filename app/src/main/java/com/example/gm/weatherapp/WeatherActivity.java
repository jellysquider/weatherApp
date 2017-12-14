package com.example.gm.weatherapp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.media.Image;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.gm.weatherapp.model.DailyWeatherReport;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class WeatherActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks, LocationListener {

    final String URL_BASE = "http://dataservice.accuweather.com/forecasts/v1/daily/5day/";
    final String GET_LOCATION_KEY = "http://dataservice.accuweather.com/locations/v1/cities/geoposition/search";
 //   final String URL_COORD = "&q=50.4501,30.5234";
//    final String LOCATION_KEY = "324505";
    final String API_KEY = "?apikey=7ehwRLZGZ37cskBdty1eLKMox8qXUBMU";
    final String ADD_PREF = "&metric=true";

    private String LOCATION_KEY;

    private GoogleApiClient mGoogleApiClient;
    private final int PERMISSION_LOCATION = 111;

    private String wUrl;
    private ArrayList<DailyWeatherReport> weatherReportList = new ArrayList<>();


    private ImageView weatherIcon;
    private TextView weatherDate;
    private TextView maxTemp;
    private TextView minTemp;
    private TextView cityCountry;
    private TextView weatherCond;


    WeatherAdapter mAdapter;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);

        weatherIcon = findViewById(R.id.weatherIcon);
        weatherDate = findViewById(R.id.weatherDate);
        maxTemp = findViewById(R.id.maxTemp);
        minTemp = findViewById(R.id.minTemp);
        cityCountry = findViewById(R.id.cityCountry);
        weatherCond = findViewById(R.id.weatherCond);

        RecyclerView recyclerView = findViewById(R.id.content_weather_reports);
        mAdapter = new WeatherAdapter(weatherReportList);
        recyclerView.setAdapter(mAdapter);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);

        recyclerView.setLayoutManager(layoutManager);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .enableAutoManage(this, this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

    }

    public void downloadWeatherData(Location location) {

        final String URL_COORD = "&q=" + location.getLatitude() + "," + location.getLongitude();

        final String loc = GET_LOCATION_KEY + API_KEY + URL_COORD;

        final JsonObjectRequest locationRequest = new JsonObjectRequest(Request.Method.GET, loc, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.v("Weather", "Response: " + response.toString());
                try {

                    LOCATION_KEY = response.getString("Key");
                    final String cityName = response.getString("LocalizedName");
                    JSONObject country = response.getJSONObject("Country");
                    final String countryName = country.getString("LocalizedName");

                    wUrl = URL_BASE + LOCATION_KEY + API_KEY + ADD_PREF;

                    Log.v("JSON", "Name " + LOCATION_KEY + "Country " + countryName);

                    final JsonObjectRequest weatherRequest = new JsonObjectRequest(Request.Method.GET, wUrl, null, new Response.Listener<JSONObject>() {
                        @RequiresApi(api = Build.VERSION_CODES.O)
                        @Override
                        public void onResponse(JSONObject response) throws ParseException {
                            Log.v("Weather", "Response: " + response.toString());
                            try {

                                JSONArray dailyList = response.getJSONArray("DailyForecasts");

                                for (int i = 0; i < 5; i++) {
                                    JSONObject obj = dailyList.getJSONObject(i);
                                    JSONObject temp = obj.getJSONObject("Temperature");

                                    JSONObject minTemp = temp.getJSONObject("Minimum");
                                    Double min = minTemp.getDouble("Value");

                                    JSONObject maxTemp = temp.getJSONObject("Maximum");
                                    Double max = maxTemp.getDouble("Value");

                                    JSONObject weatherCond = obj.getJSONObject("Day");
                                    String weather = weatherCond.getString("IconPhrase");

                                    String rawDate = obj.getString("Date");
//                                    Log.v("DATE", "ISO: " + rawDate);

                                    DailyWeatherReport report = new DailyWeatherReport(cityName, countryName, min.intValue(), max.intValue(), weather, rawDate);

                                    weatherReportList.add(report);

//                                    Log.v("JSON", "Printing from Class: " + report.getWeather());
                                }
                            }
                            catch (JSONException e) {
                                Log.v("JSON", "EXC: " + e.getLocalizedMessage());
                            }

                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.v("Weather", "Err: " + error.getLocalizedMessage());
                        }
                    });

//                    Log.v("URLRLRLR", "URL  " + wUrl);
                    Volley.newRequestQueue(WeatherActivity.this).add(weatherRequest);

                }
                catch (JSONException e) {
                    Log.v("JSON", "EXC: " + e.getLocalizedMessage());
                }

                updateUI();
                mAdapter.notifyDataSetChanged();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.v("Weather", "Err: " + error.getLocalizedMessage());
            }
        });

        Volley.newRequestQueue(this).add(locationRequest);
    }

    public void updateUI() {

        if (weatherReportList.size() > 0) {
            DailyWeatherReport report = weatherReportList.get(0);

            switch (report.getWeather()) {
                case DailyWeatherReport.WEATHER_CLOUDS:
                    weatherIcon.setImageDrawable(getResources().getDrawable(R.drawable.cloudy));
                case DailyWeatherReport.WEATHER_RAIN:
                    weatherIcon.setImageDrawable(getResources().getDrawable(R.drawable.rain));
                case DailyWeatherReport.WEATHER_FOG:
                    weatherIcon.setImageDrawable(getResources().getDrawable(R.drawable.fog));
                case DailyWeatherReport.WEATHER_SHOWERS:
                    weatherIcon.setImageDrawable(getResources().getDrawable(R.drawable.showers));
                case DailyWeatherReport.WEATHER_SNOW:
                    weatherIcon.setImageDrawable(getResources().getDrawable(R.drawable.snow));
                default:
                    weatherIcon.setImageDrawable(getResources().getDrawable(R.drawable.sunny));
            }

            weatherDate.setText(report.getFormattedDate());
            maxTemp.setText(Integer.toString(report.getMaxTemp()) + "\u00B0");
            minTemp.setText(Integer.toString(report.getMinTemp()) + "\u00B0");
            cityCountry.setText(report.getCityName() + ", " + report.getCountryName());
            weatherCond.setText(report.getWeather());

        }

    }

    @Override
    public void onLocationChanged(Location location) {

        downloadWeatherData(location);

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_LOCATION);
        }
        else {
            startLocationServices();
        }
    }



    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    private void startLocationServices() {
        try {
            LocationRequest req = LocationRequest.create().setPriority(LocationRequest.PRIORITY_LOW_POWER);
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, req, this);
        }
        catch (SecurityException exception) {

        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case PERMISSION_LOCATION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startLocationServices();
                }
                else {
                    // show a dialog that permission wasn't granted
                    Toast.makeText(this, "Can't run location due to permission limitations", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    public class WeatherAdapter extends RecyclerView.Adapter<WeatherReportViewHolder> {
        private ArrayList<DailyWeatherReport> mDailyWeatherReports;

        public WeatherAdapter(ArrayList<DailyWeatherReport> mDailyWeatherReports) {
            this.mDailyWeatherReports = mDailyWeatherReports;
        }

        @Override
        public void onBindViewHolder(WeatherReportViewHolder holder, int position) {
            DailyWeatherReport report = mDailyWeatherReports.get(position);
            holder.updateUI(report);

        }

        @Override
        public int getItemCount() {
            return mDailyWeatherReports.size();
        }

        @Override
        public WeatherReportViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View card = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_weather, parent, false);
            return new WeatherReportViewHolder(card);
        }
    }


    public class WeatherReportViewHolder extends RecyclerView.ViewHolder {

        private ImageView rweatherIcon;
        private TextView rweatherDate;
        private TextView rweatherCond;
        private TextView rmaxTemp;
        private TextView rminTemp;

        public WeatherReportViewHolder(View itemView) {
            super(itemView);

            rweatherIcon = itemView.findViewById(R.id.list_weather_icon);
            rweatherDate = itemView.findViewById(R.id.weather_day);
            rweatherCond = itemView.findViewById(R.id.weather_cond);
            rmaxTemp = itemView.findViewById(R.id.weather_maxTemp);
            rminTemp = itemView.findViewById(R.id.weather_minTemp);

        }

        public void updateUI(DailyWeatherReport report) {

            rweatherDate.setText(report.getFormattedDate());
            rweatherCond.setText(report.getWeather());
            rminTemp.setText(Integer.toString(report.getMinTemp()) + "\u00B0");
            rmaxTemp.setText(Integer.toString(report.getMaxTemp()) + "\u00B0");

            switch (report.getWeather()) {
                case DailyWeatherReport.WEATHER_CLOUDS:
                    rweatherIcon.setImageDrawable(getResources().getDrawable(R.drawable.cloudy));
                case DailyWeatherReport.WEATHER_RAIN:
                    rweatherIcon.setImageDrawable(getResources().getDrawable(R.drawable.rain));
                case DailyWeatherReport.WEATHER_FOG:
                    rweatherIcon.setImageDrawable(getResources().getDrawable(R.drawable.fog));
                case DailyWeatherReport.WEATHER_SHOWERS:
                    rweatherIcon.setImageDrawable(getResources().getDrawable(R.drawable.showers));
                case DailyWeatherReport.WEATHER_SNOW:
                    rweatherIcon.setImageDrawable(getResources().getDrawable(R.drawable.snow));
                default:
                    rweatherIcon.setImageDrawable(getResources().getDrawable(R.drawable.sunny));
            }

        }
    }


}


