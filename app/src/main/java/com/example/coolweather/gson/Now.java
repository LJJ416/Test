package com.example.coolweather.gson;

import com.google.gson.annotations.SerializedName;

public class Now {
    public Basic basic;
    public Update update;
    public String status;
    public now now;

    public class now{
        @SerializedName("tmp")
        public String temperature;

        @SerializedName("cond_txt")
        public String info;

        @SerializedName("wind_dir")
        public String directionOfWind;

        @SerializedName("hum")
        public String humidity;
    }

}
