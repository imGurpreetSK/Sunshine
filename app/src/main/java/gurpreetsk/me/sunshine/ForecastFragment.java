package gurpreetsk.me.sunshine;


import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.text.format.Time;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
public class ForecastFragment extends Fragment {


    public ForecastFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);        //to notify
    }


    ArrayAdapter<String> adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View v = inflater.inflate(R.layout.fragment_main, container, false);

        ArrayList<String> weather = new ArrayList<>();

        ListView weatherListView = (ListView) v.findViewById(R.id.listview_forecast);
        adapter = new ArrayAdapter<>(getContext(), R.layout.list_item_forecast, R.id.list_item_forecast_textview, weather);    //4 parameters
        weatherListView.setAdapter(adapter);

        weatherListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String value = adapter.getItem(i);
                Intent intent = new Intent(getActivity(), DetailActivity.class);
                intent.putExtra(Intent.EXTRA_TEXT, value);
                startActivity(intent);
            }
        });

        return v;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.forecast_fragment_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.refresh_button:

                updateWeather();

                return true;
            case R.id.settings:
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateWeather() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String pin = prefs.getString(getString(R.string.pref_loc_key), getString(R.string.pref_default_value));        //key, default value

        new FetchWeatherTask().execute(pin);
    }

    @Override
    public void onStart() {
        super.onStart();
        updateWeather();
    }

    private class FetchWeatherTask extends AsyncTask<String, Void, String[]> {

        @Override
        protected String[] doInBackground(String... pin) {

            if (pin.length == 0) return null;

            String BASE_URL = "http://api.openweathermap.org/data/2.5/forecast/daily?";
            String QUERY_PARAM = "q";
            String FORMAT_PARAM = "mode";
            String UNITS_PARAM = "units";
            String DAYS_PARAM = "cnt";
            String API_KEY = "appid";

            String format = "json";
            String units = PreferenceManager.getDefaultSharedPreferences(getActivity())
                    .getString(getString(R.string.pref_temp_key), getString(R.string.temp_default_value));
            String days = "7";
            String key = "a1ff265d249cc69c1c87939a917d9aef";

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String forecastJsonStr = null;

            try {

                Uri builtUri = Uri.parse(BASE_URL).buildUpon()
                        .appendQueryParameter(QUERY_PARAM, pin[0])
                        .appendQueryParameter(FORMAT_PARAM, format)
                        .appendQueryParameter(UNITS_PARAM, units)
//                        .appendQueryParameter(DAYS_PARAM, days)
                        .appendQueryParameter(API_KEY, key)
                        .build();

                URL url = new URL(builtUri.toString());
//                URL url = new URL("http://api.openweathermap.org/data/2.5/forecast/daily?q=110041&units=metric&mode=json&cnt=7&appid=a1ff265d249cc69c1c87939a917d9aef");

                // Create the request to OpenWeatherMap, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                forecastJsonStr = buffer.toString();

                try {
                    String[] data = getWeatherDataFromJson(forecastJsonStr, Integer.parseInt(days));
                    return data;
                } catch (JSONException e) {
                    e.printStackTrace();
                }

//                Log.v("ForecastFragment", forecastJsonStr);
            } catch (IOException e) {
//                Log.e("PlaceholderFragment", "Error ", e);
                // If the code didn't successfully get the weather data, there's no point in attemping
                // to parse it.
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
//                        Log.e("PlaceholderFragment", "Error closing stream", e);
                    }
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String[] strings) {
            super.onPostExecute(strings);
            if (strings != null) {
                adapter.clear();
                for (String str : strings)
                    adapter.add(str);
            }
        }
    }

    /* The date/time conversion code is going to be moved outside the asynctask later,
         * so for convenience we're breaking it out into its own method now.
         */
    private String getReadableDateString(long time) {
        // Because the API returns a unix timestamp (measured in seconds),
        // it must be converted to milliseconds in order to be converted to valid date.
        SimpleDateFormat shortenedDateFormat = new SimpleDateFormat("EEE MMM dd");
        return shortenedDateFormat.format(time);
    }

    /**
     * Prepare the weather high/lows for presentation.
     */
    private String formatHighLows(double high, double low) {
        // For presentation, assume the user doesn't care about tenths of a degree.
        long roundedHigh = Math.round(high);
        long roundedLow = Math.round(low);

        String highLowStr = roundedHigh + "/" + roundedLow;
        return highLowStr;
    }

    /**
     * Take the String representing the complete forecast in JSON Format and
     * pull out the data we need to construct the Strings needed for the wireframes.
     * <p/>
     * Fortunately parsing is easy:  constructor takes the JSON string and converts it
     * into an Object hierarchy for us.
     */
    private String[] getWeatherDataFromJson(String forecastJsonStr, int numDays)
            throws JSONException {

        // These are the names of the JSON objects that need to be extracted.
        final String OWM_LIST = "list";
        final String OWM_WEATHER = "weather";
        final String OWM_TEMPERATURE = "temp";
        final String OWM_MAX = "max";
        final String OWM_MIN = "min";
        final String OWM_DESCRIPTION = "main";

        JSONObject forecastJson = new JSONObject(forecastJsonStr);
        JSONArray weatherArray = forecastJson.getJSONArray(OWM_LIST);

        // OWM returns daily forecasts based upon the local time of the city that is being
        // asked for, which means that we need to know the GMT offset to translate this data
        // properly.

        // Since this data is also sent in-order and the first day is always the
        // current day, we're going to take advantage of that to get a nice
        // normalized UTC date for all of our weather.

        Time dayTime = new Time();
        dayTime.setToNow();

        // we start at the day returned by local time. Otherwise this is a mess.
        int julianStartDay = Time.getJulianDay(System.currentTimeMillis(), dayTime.gmtoff);

        // now we work exclusively in UTC
        dayTime = new Time();

        String[] resultStrs = new String[numDays];
        for (int i = 0; i < weatherArray.length(); i++) {
            // For now, using the format "Day, description, hi/low"
            String day;
            String description;
            String highAndLow;

            // Get the JSON object representing the day
            JSONObject dayForecast = weatherArray.getJSONObject(i);

            // The date/time is returned as a long.  We need to convert that
            // into something human-readable, since most people won't read "1400356800" as
            // "this saturday".
            long dateTime;
            // Cheating to convert this to UTC time, which is what we want anyhow
            dateTime = dayTime.setJulianDay(julianStartDay + i);
            day = getReadableDateString(dateTime);

            // description is in a child array called "weather", which is 1 element long.
            JSONObject weatherObject = dayForecast.getJSONArray(OWM_WEATHER).getJSONObject(0);
            description = weatherObject.getString(OWM_DESCRIPTION);

            // Temperatures are in a child object called "temp".  Try not to name variables
            // "temp" when working with temperature.  It confuses everybody.
            JSONObject temperatureObject = dayForecast.getJSONObject(OWM_TEMPERATURE);
            double high = temperatureObject.getDouble(OWM_MAX);
            double low = temperatureObject.getDouble(OWM_MIN);

            highAndLow = formatHighLows(high, low);
            resultStrs[i] = day + " - " + description + " - " + highAndLow;
        }

        for (String s : resultStrs) {
//            Log.v("ForecastFragment", "Forecast entry: " + s);
        }
        return resultStrs;

    }

}
