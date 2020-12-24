package com.example.coolweather.gson;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class LifeStyle {
    public Basic basic;
    public Update update;
    public String status;

    @SerializedName("lifestyle")
    public List<lifestyle> lifestyleList;

    public class lifestyle{
        public String type;
        public String txt;
    }
}
