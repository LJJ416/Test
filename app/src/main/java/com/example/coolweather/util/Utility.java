package com.example.coolweather.util;

import android.text.TextUtils;

import com.example.coolweather.db.City;
import com.example.coolweather.db.County;
import com.example.coolweather.db.Province;
import com.example.coolweather.gson.Forecast;
import com.example.coolweather.gson.LifeStyle;
import com.example.coolweather.gson.Now;
import com.example.coolweather.gson.Weather;
import com.google.gson.Gson;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Utility {
    // 解析省级数据
    public static boolean handleProvinceResponse(String response){
        if(!TextUtils.isEmpty(response)){
            try{
                JSONArray allProvinces = new JSONArray(response);
                for (int i = 0; i < allProvinces.length(); i++){
                    JSONObject provinceObject = allProvinces.getJSONObject(i);
                    Province province = new Province();
                    province.setProvinceName(provinceObject.getString("name"));
                    province.setProvinceCode(provinceObject.getInt("id"));
                    province.save();
                }
                return true;
            }catch (JSONException e){
                e.printStackTrace();
            }
        }
        return false;
    }

    // 解析市级数据
    public static boolean handleCityResponse(String response, int provinceId){
        if(!TextUtils.isEmpty(response)){
            try {
                JSONArray allCities = new JSONArray(response);
                for(int i = 0; i < allCities.length(); i++){
                    JSONObject cityObject = allCities.getJSONObject(i);
                    City city = new City();
                    city.setCityName(cityObject.getString("name"));
                    city.setCityCode(cityObject.getInt("id"));
                    city.setProvinceId(provinceId);
                    city.save();
                }
                return true;
            }catch (JSONException e){
                e.printStackTrace();
            }
        }
        return false;
    }

    // 解析县级数据
    public static boolean handleCountyResponse(String response, int cityId){
        if(!TextUtils.isEmpty(response)){
            try {
                JSONArray allCounties = new JSONArray(response);
                for(int i = 0; i < allCounties.length(); i++){
                    JSONObject countyObject = allCounties.getJSONObject(i);
                    County county = new County();
                    county.setCityId(cityId);
                    county.setCountyName(countyObject.getString("name"));
                    county.setWeatherId(countyObject.getString("weather_id"));
                    county.save();
                }
                return true;
            }catch (JSONException e){
                e.printStackTrace();
            }
        }
        return false;
    }

    // 将返回的数据解析成实体类
    public static Weather handleWeatherResponse(String response, Weather weather, String cate){
        try{
            JSONObject jsonObject = new JSONObject(response);
            JSONArray jsonArray = jsonObject.getJSONArray("HeWeather6");
            String weatherContent = jsonArray.getJSONObject(0).toString();
            if ("now".equals(cate)){
                Now nowWeather = new Gson().fromJson(weatherContent, Now.class);
                weather.basic = nowWeather.basic;
                weather.update = nowWeather.update;
                weather.now = nowWeather.now;
                weather.status = nowWeather.status;
                return weather;
            }else if("forecast".equals(cate)){
                Forecast forecastWeather = new Gson().fromJson(weatherContent, Forecast.class);
                weather.forecastList = forecastWeather.forecastList;
                weather.status = forecastWeather.status;
            }else if("lifestyle".equals(cate)){
                LifeStyle lifeStyleWeather = new Gson().fromJson(weatherContent, LifeStyle.class);
                weather.lifestyleList = lifeStyleWeather.lifestyleList;
                weather.status = lifeStyleWeather.status;
            }
            return weather;
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
}
