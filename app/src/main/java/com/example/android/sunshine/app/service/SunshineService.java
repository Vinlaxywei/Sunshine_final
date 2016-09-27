/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.sunshine.app.service;

import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.text.format.Time;
import android.util.Log;

import com.example.android.sunshine.app.BuildConfig;
import com.example.android.sunshine.app.R;
import com.example.android.sunshine.app.Utility;
import com.example.android.sunshine.app.data.WeatherContract;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Vector;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class SunshineService extends IntentService {
    private final String LOG_TAG = SunshineService.class.getSimpleName();

    // 提取地址信息的 key
    public static final String LOCATION_QUERY_EXTRA = "lqe";

    // 重载函数，上传这个服务的名称
    public SunshineService() {
        super("Sunshine");
    }

//    @Override
//    protected void onHandleIntent(Intent intent) {
//        Log.d(LOG_TAG, "onHandleIntent: ");
//        String locationQuery = intent.getStringExtra(LOCATION_QUERY_EXTRA);
//
//        // 使用 HttpURLConnection 作为此次发起连接的主体
//        HttpURLConnection urlConnection = null;
//        BufferedReader reader = null;
//
//        // 储存返回的天气信息的字符串
//        String forecastJsonStr = null;
//
//        String format = "json";
//        String units = "metric";
//        int numDays = 14;
//
//        try {
//            final String FORECAST_BASE_URL =
//                    "http://api.openweathermap.org/data/2.5/forecast/daily?";
//            final String QUERY_PARAM = "q";
//            final String FORMAT_PARAM = "mode";
//            final String UNITS_PARAM = "units";
//            final String DAYS_PARAM = "cnt";
//            final String APPID_PARAM = "APPID";
//
//            Uri builtUri = Uri.parse(FORECAST_BASE_URL).buildUpon()
//                    .appendQueryParameter(QUERY_PARAM, locationQuery)
//                    .appendQueryParameter(FORMAT_PARAM, format)
//                    .appendQueryParameter(UNITS_PARAM, units)
//                    .appendQueryParameter(DAYS_PARAM, Integer.toString(numDays))
//                    .appendQueryParameter(APPID_PARAM, BuildConfig.OPEN_WEATHER_MAP_API_KEY)
//                    .build();
//
//            URL url = new URL(builtUri.toString());
//
//            // Create the request to OpenWeatherMap, and open the connection
//            urlConnection = (HttpURLConnection) url.openConnection();
//            urlConnection.setRequestMethod("GET");
//            urlConnection.connect();
//
//            // Read the input stream into a String
//            InputStream inputStream = urlConnection.getInputStream();
//            StringBuffer buffer = new StringBuffer();
//            if (inputStream == null) {
//                // Nothing to do.
//                return;
//            }
//            reader = new BufferedReader(new InputStreamReader(inputStream));
//
//            String line;
//            while ((line = reader.readLine()) != null) {
//                buffer.append(line + "\n");
//            }
//
//            if (buffer.length() == 0) {
//                return;
//            }
//            forecastJsonStr = buffer.toString();
//            getWeatherDataFromJson(forecastJsonStr, locationQuery);
//        } catch (IOException e) {
//            Log.e(LOG_TAG, "Error ", e);
//        } catch (JSONException e) {
//            Log.e(LOG_TAG, e.getMessage(), e);
//            e.printStackTrace();
//        } finally {
//            if (urlConnection != null) {
//                urlConnection.disconnect();
//            }
//            if (reader != null) {
//                try {
//                    reader.close();
//                } catch (final IOException e) {
//                    Log.e(LOG_TAG, "Error closing stream", e);
//                }
//            }
//        }
//        return;
//    }

    /*
    * 使用 OkHttp库 作为发起连接的主体
    * */
    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(LOG_TAG, "onHandleIntent: ");

        // 这个字符串用于存储获取到的 json 信息
        String jsonStr = null;
        // 从 intent 中获取地址信息
        String locationQuery = intent.getStringExtra(LOCATION_QUERY_EXTRA);

        /*
        * 定义此次发起请求的一些参数
        * */
        String format = "json";
        String units = "metric";
        int numDays = 14;

        final String FORECAST_BASE_URL = "http://api.openweathermap.org/data/2.5/forecast/daily?";
        final String QUERY_PARAM = "q";
        final String FORMAT_PARAM = "mode";
        final String UNITS_PARAM = "units";
        final String DAYS_PARAM = "cnt";
        final String APPID_PARAM = "APPID";

        try {

            // 构建Uri
            Uri builtUri = Uri.parse(FORECAST_BASE_URL).buildUpon()
                    .appendQueryParameter(QUERY_PARAM, locationQuery)
                    .appendQueryParameter(FORMAT_PARAM, format)
                    .appendQueryParameter(UNITS_PARAM, units)
                    .appendQueryParameter(DAYS_PARAM, Integer.toString(numDays))
                    .appendQueryParameter(APPID_PARAM, BuildConfig.OPEN_WEATHER_MAP_API_KEY)
                    .build();

            // 定义 OkHttp 对象
            OkHttpClient client = new OkHttpClient();

            // 定义请求
            Request request = new Request.Builder()
                    .url(builtUri.toString())
                    .build();

            // 使用 OkHttp 对象发起请求，获取到的数据赋值到 Response 对象
            Response response = client.newCall(request).execute();
            jsonStr = response.body().string();

            // 解析 Json 数据
            getWeatherDataFromJson(jsonStr, locationQuery);

        } catch (IOException e) {
            Log.e(LOG_TAG, "IO Error ", e);
            e.printStackTrace();
        } catch (JSONException e) {
            Log.e(LOG_TAG, "JSON Error ", e);
            e.printStackTrace();
        }
    }

    private void getWeatherDataFromJson(String forecastJsonStr, String locationSetting) throws JSONException {

        final String OWM_CITY = "city";
        final String OWM_CITY_NAME = "name";
        final String OWM_COORD = "coord";
        final String OWM_LATITUDE = "lat";
        final String OWM_LONGITUDE = "lon";
        final String OWM_LIST = "list";
        final String OWM_PRESSURE = "pressure";
        final String OWM_HUMIDITY = "humidity";
        final String OWM_WINDSPEED = "speed";
        final String OWM_WIND_DIRECTION = "deg";

        final String OWM_TEMPERATURE = "temp";
        final String OWM_MAX = "max";
        final String OWM_MIN = "min";

        final String OWM_WEATHER = "weather";
        final String OWM_DESCRIPTION = "main";
        final String OWM_WEATHER_ID = "id";
        final String OWM_MESSAGE_COD = "cod";

        try {
            JSONObject forecastJson = new JSONObject(forecastJsonStr);

            // 使用 sharepreference 储存地址状态信息
            if (forecastJson.has(OWM_MESSAGE_COD)) {
                int cod = forecastJson.getInt(OWM_MESSAGE_COD);
                switch (cod) {
                    // 有效位置
                    case 200:
                        Utility.setExceptionStatus(getBaseContext(), getString(R.string.pref_exception_status_valid));
                        break;
                    // 无效位置
                    case 404:
                        Utility.setExceptionStatus(getBaseContext(), getString(R.string.pref_exception_status_invalid));
                        return;
                    // 由于数据来源于国外网址，可能有翻墙失败等其他状态
                    default:
                        return;
                }
            }

            JSONArray weatherArray = forecastJson.getJSONArray(OWM_LIST);

            JSONObject cityJson = forecastJson.getJSONObject(OWM_CITY);
            String cityName = cityJson.getString(OWM_CITY_NAME);

            JSONObject cityCoord = cityJson.getJSONObject(OWM_COORD);
            double cityLatitude = cityCoord.getDouble(OWM_LATITUDE);
            double cityLongitude = cityCoord.getDouble(OWM_LONGITUDE);

            long locationId = addLocation(locationSetting, cityName, cityLatitude, cityLongitude);

            Vector<ContentValues> cVVector = new Vector<ContentValues>(weatherArray.length());

            Time dayTime = new Time();
            dayTime.setToNow();

            int julianStartDay = Time.getJulianDay(System.currentTimeMillis(), dayTime.gmtoff);

            dayTime = new Time();

            for (int i = 0; i < weatherArray.length(); i++) {
                long dateTime;
                double pressure;
                int humidity;
                double windSpeed;
                double windDirection;

                double high;
                double low;

                String description;
                int weatherId;

                dateTime = dayTime.setJulianDay(julianStartDay + i);

                /*
                * 提取压力指数、湿度、风速、风向的天气数据
                * */
                JSONObject dayForecast = weatherArray.getJSONObject(i);
                pressure = dayForecast.getDouble(OWM_PRESSURE);
                humidity = dayForecast.getInt(OWM_HUMIDITY);
                windSpeed = dayForecast.getDouble(OWM_WINDSPEED);
                windDirection = dayForecast.getDouble(OWM_WIND_DIRECTION);

                /*
                * 提取天气概况
                * */
                JSONObject weatherObject = dayForecast.getJSONArray(OWM_WEATHER).getJSONObject(0);
                description = weatherObject.getString(OWM_DESCRIPTION);
                weatherId = weatherObject.getInt(OWM_WEATHER_ID);

                /*
                * 提取温度信息
                * */
                JSONObject temperatureObject = dayForecast.getJSONObject(OWM_TEMPERATURE);
                high = temperatureObject.getDouble(OWM_MAX);
                low = temperatureObject.getDouble(OWM_MIN);

                ContentValues weatherValues = new ContentValues();

                weatherValues.put(WeatherContract.WeatherEntry.COLUMN_LOC_KEY, locationId);
                weatherValues.put(WeatherContract.WeatherEntry.COLUMN_DATE, dateTime);
                weatherValues.put(WeatherContract.WeatherEntry.COLUMN_HUMIDITY, humidity);
                weatherValues.put(WeatherContract.WeatherEntry.COLUMN_PRESSURE, pressure);
                weatherValues.put(WeatherContract.WeatherEntry.COLUMN_WIND_SPEED, windSpeed);
                weatherValues.put(WeatherContract.WeatherEntry.COLUMN_DEGREES, windDirection);
                weatherValues.put(WeatherContract.WeatherEntry.COLUMN_MAX_TEMP, high);
                weatherValues.put(WeatherContract.WeatherEntry.COLUMN_MIN_TEMP, low);
                weatherValues.put(WeatherContract.WeatherEntry.COLUMN_SHORT_DESC, description);
                weatherValues.put(WeatherContract.WeatherEntry.COLUMN_WEATHER_ID, weatherId);

                cVVector.add(weatherValues);
            }

            int inserted = 0;

            /*
            * 通过 Vector 灵活的调整数据长度，再将其转化为固定数组，最后通过 bulkInsert 能够提高插入效率
            * */
            if (cVVector.size() > 0) {
                ContentValues[] cvArray = new ContentValues[cVVector.size()];
                cVVector.toArray(cvArray);
                this.getContentResolver().bulkInsert(WeatherContract.WeatherEntry.CONTENT_URI, cvArray);
            }

            Log.d(LOG_TAG, "Sunshine Service Complete. " + cVVector.size() + " Inserted");

        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
    }

    /*
    * 自定义方法，通过查询数据库中的地址表格是否已经拥有这个地址的信息，
    * 再根据查询结果更新表格，并返回相应的地址信息 Row id
    * */
    long addLocation(String locationSetting, String cityName, double lat, double lon) {
        long locationId;

        Cursor locationCursor = this.getContentResolver().query(
                WeatherContract.LocationEntry.CONTENT_URI,
                new String[]{WeatherContract.LocationEntry._ID},
                WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING + " = ?",
                new String[]{locationSetting},
                null);

        if (locationCursor.moveToFirst()) {
            int locationIdIndex = locationCursor.getColumnIndex(WeatherContract.LocationEntry._ID);
            locationId = locationCursor.getLong(locationIdIndex);
        } else {
            ContentValues locationValues = new ContentValues();

            locationValues.put(WeatherContract.LocationEntry.COLUMN_CITY_NAME, cityName);
            locationValues.put(WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING, locationSetting);
            locationValues.put(WeatherContract.LocationEntry.COLUMN_COORD_LAT, lat);
            locationValues.put(WeatherContract.LocationEntry.COLUMN_COORD_LONG, lon);

            Uri insertedUri = this.getContentResolver().insert(
                    WeatherContract.LocationEntry.CONTENT_URI,
                    locationValues
            );

            locationId = ContentUris.parseId(insertedUri);
        }

        locationCursor.close();
        return locationId;
    }

    /*
    * 内部类，有需要时即可通过此广播组件定时更新天气信息
    * */
    public static class AlarmReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Intent sendIntent = new Intent(context, SunshineService.class);
            sendIntent.putExtra(SunshineService.LOCATION_QUERY_EXTRA, intent.getStringExtra(SunshineService.LOCATION_QUERY_EXTRA));
            context.startService(sendIntent);

        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(LOG_TAG, "onDestroy: ");
    }
}