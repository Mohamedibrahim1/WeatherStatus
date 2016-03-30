package com.example.mohamedibrahim.weatherstatus;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ForecastFragment extends Fragment {

    private ArrayAdapter<String> mForecastAdaptor;

    public ForecastFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        FetchWeatherTask weatherTask = new FetchWeatherTask();
        weatherTask.execute();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        mForecastAdaptor = new ArrayAdapter<String>(
                getActivity(),
                R.layout.list_item_forecast,
                R.id.list_item_forecast_textview,
                new ArrayList<String>());

        ListView listView = (ListView) rootView.findViewById(R.id.listview_forecast);
        listView.setAdapter(mForecastAdaptor);

        return rootView;
    }

    public class FetchWeatherTask extends AsyncTask<String, Void, Void> {

        RequestQueue queue = Volley.newRequestQueue(getActivity().getApplicationContext());
        String[] resultStrs = new String[4];
        String url = "http://api.wunderground.com/api/838ed9367e8876bf/forecast/q/EG/Cairo.json";
        SharedPreferences LastData = getActivity().getPreferences(0);
        SharedPreferences.Editor EditLastData = LastData.edit();

        @Override
        protected Void doInBackground(String... params) {
            try {

                // Request a string response from the provided URL.
                StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                try {

                                    JSONObject json = new JSONObject(response);

                                    // These are the names of the JSON objects that need to be extracted.
                                    JSONObject forecast = json.getJSONObject("forecast");
                                    JSONObject simpleforecast = forecast.getJSONObject("simpleforecast");
                                    JSONArray forecastdayS = simpleforecast.getJSONArray("forecastday");

                                    for (int i = 0; i < response.length(); i++) {

                                        JSONObject forecastday = (JSONObject) forecastdayS.get(i);

                                        JSONObject date = forecastday.getJSONObject("date");
                                        String day = date.getString("day");
                                        String month = date.getString("month");
                                        String year = date.getString("year");
                                        String dayName = date.getString("weekday");

                                        String DateDayFormat = day + "/" + month + "/" + year + "   -   " + dayName + "  ";

                                        JSONObject high = forecastday.getJSONObject("high");
                                        String highCelsius = high.getString("celsius");

                                        JSONObject low = forecastday.getJSONObject("low");
                                        String lowCelsius = low.getString("celsius");

                                        String CelsiusFormat = "   ↑ High : " + highCelsius + "  ↓ Low : " + lowCelsius;

                                        resultStrs[i] = DateDayFormat + CelsiusFormat;

                                        EditLastData.putString("LastData " + i, resultStrs[i]);
                                        EditLastData.commit();
                                        //Log.v("day", LastData.getString("LastData " + i,""));

                                        mForecastAdaptor.add(resultStrs[i]);
                                    }

                                } catch (JSONException e) {
                                    Log.v("ERROR", "JSON Error: " + e);
                                }
                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        mForecastAdaptor.clear();
                        Toast.makeText(getActivity().getApplicationContext(),
                                R.string.ErrorMessage, Toast.LENGTH_LONG).show();
                        for (int i = 0; i < 4; i++) {
                            //Log.v("day", LastData.getString("LastData " + i, ""));
                            String DayForcast = LastData.getString("LastData " + i, "");
                            mForecastAdaptor.add(DayForcast);
                        }
                        Log.v("ERROR", "Response Error: " + error);
                    }
                });

                // Add the request to the RequestQueue.
                queue.add(stringRequest);
            } catch (Exception e) {
            }
            return null;
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.forecastfragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        if (id == R.id.action_refresh) {
            mForecastAdaptor.clear();
            FetchWeatherTask weatherTask = new FetchWeatherTask();
            weatherTask.execute();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}