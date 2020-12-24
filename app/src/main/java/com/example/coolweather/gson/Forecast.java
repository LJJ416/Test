package com.example.coolweather.gson;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Forecast {
    public Basic basic;
    public Update update;
    public String status;

    @SerializedName("daily_forecast")
    public List<forecast> forecastList;

    public class forecast{
        public String date;

        @SerializedName("cond_txt_d")
        public String day;

        @SerializedName("cond_txt_n")
        public String night;

        public String tmp_max;
        public String tmp_min;
    }
}
