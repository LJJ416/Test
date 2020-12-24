package com.example.coolweather.gson;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Weather {
    public Basic basic;
    public Update update;
    public Now.now now;
    public String status;

    @SerializedName("daily_forecast")
    public List<Forecast.forecast> forecastList;

    @SerializedName("lifestyle")
    public List<LifeStyle.lifestyle> lifestyleList;
}
