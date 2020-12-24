package com.example.coolweather;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.example.coolweather.gson.Forecast;
import com.example.coolweather.gson.LifeStyle;
import com.example.coolweather.gson.Weather;
import com.example.coolweather.service.AutoUpdateService;
import com.example.coolweather.util.HttpUtil;
import com.example.coolweather.util.Utility;
import org.jetbrains.annotations.NotNull;
import java.io.IOException;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class WeatherActivity extends AppCompatActivity {
    private ScrollView weatherLayout;
    private TextView titleCity;
    private TextView titleUpdateTime;
    private TextView degreeText;
    private TextView weatherInfoText;
    private LinearLayout forecastLayout;
    private TextView humText;
    private TextView windText;
    private TextView comfortText;
    private TextView drsgText;
    private TextView sportText;
    private ImageView bingPicImg;
    public SwipeRefreshLayout swipeRefreshLayout;
    private String mWeatherId;
    public DrawerLayout drawerLayout;
    private Button navButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 背景和状态栏融合
        if (Build.VERSION.SDK_INT >= 21){
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        setContentView(R.layout.activity_weather);
        swipeRefreshLayout = findViewById(R.id.swipe_refresh);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary);

        // 初始化控件
        weatherLayout = findViewById(R.id.weather_layout);
        titleCity = findViewById(R.id.title_city);
        titleUpdateTime = findViewById(R.id.title_update_time);
        weatherInfoText = findViewById(R.id.weather_info_text);
        forecastLayout = findViewById(R.id.forecast_layout);
        humText = findViewById(R.id.hum_text);
        windText = findViewById(R.id.wind_text);
        comfortText = findViewById(R.id.comfort_text);
        drsgText = findViewById(R.id.drsg_text);
        sportText = findViewById(R.id.sport_text);
        degreeText = findViewById(R.id.degree_text);
        bingPicImg = findViewById(R.id.bing_pic_img);
        drawerLayout = findViewById(R.id.drawer_layout);
        navButton = findViewById(R.id.nav_button);


        navButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });


        Weather weather = new Weather();
        SharedPreferences prefs = getApplicationContext().getSharedPreferences("weather_data", MODE_PRIVATE);
        String bingPic = prefs.getString("bing_pic", null);
        if(bingPic != null){
            Glide.with(this).load(bingPic).into(bingPicImg);
        }else {
            loadBingPic();
        }
        String nowString = prefs.getString("now", null);
        String forecastString = prefs.getString("forecast", null);
        String lifestyleString = prefs.getString("lifestyle", null);
        if(TextUtils.isEmpty(nowString) || TextUtils.isEmpty(forecastString) || TextUtils.isEmpty(lifestyleString)){
            // 无缓存时去服务器查询天气
            mWeatherId = getIntent().getStringExtra("weather_id");
            weatherLayout.setVisibility(View.INVISIBLE);
            requestWeather(mWeatherId, weather, "now");
        }else{
            // 有缓存时直接读取数据
            weather = Utility.handleWeatherResponse(nowString, weather, "now");
            weather = Utility.handleWeatherResponse(forecastString, weather, "forecast");
            weather = Utility.handleWeatherResponse(lifestyleString, weather, "lifestyle");
            mWeatherId = weather.basic.weatherId;
            showWeatherInfo(weather);
        }
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestWeather(mWeatherId, new Weather(), "now");
            }
        });
    }

    // 加载每日一图
    private void loadBingPic() {
        String requestBingPic = "http://guolin.tech/api/bing_pic";
        HttpUtil.sendOkHttpRequest(requestBingPic, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Toast.makeText(WeatherActivity.this, "壁纸加载出错", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                final String bingPic = response.body().string();
                SharedPreferences.Editor editor = getApplicationContext().getSharedPreferences("weather_data", MODE_PRIVATE).edit();
                editor.putString("bing_pic", bingPic);
                editor.apply();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Glide.with(WeatherActivity.this).load(bingPic).into(bingPicImg);
                    }
                });
            }
        });
    }

    // 构造Url
    private String reUrl(String cate, String weatherId){
        String key = "cd9034ce70ad477b8454b6638261f0a5";
        return "https://free-api.heweather.net/s6/weather/" + cate + "?location=" + weatherId + "&key=" + key;
    }


    // 根据天气 id 查询城市天气
    public void requestWeather(final String weatherId, final Weather weather,final String cate){
        String requestUrl = reUrl(cate, weatherId);
        HttpUtil.sendOkHttpRequest(requestUrl, new Callback() {
            @Override
            public void onFailure(@NotNull final Call call, @NotNull IOException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        switch (cate) {
                            case "now":
                                Toast.makeText(WeatherActivity.this, "获取今天天气失败", Toast.LENGTH_SHORT).show();
                                break;
                            case "lifestyle":
                                Toast.makeText(WeatherActivity.this, "获取生活指数失败", Toast.LENGTH_SHORT).show();
                                break;
                            case "forecast":
                                Toast.makeText(WeatherActivity.this, "获取预报天气失败", Toast.LENGTH_SHORT).show();
                                break;
                        }
                        swipeRefreshLayout.setRefreshing(false);
                    }
                });
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                final String responseText = response.body().string();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if ((weather != null && "ok".equals(weather.status)) || ("now".equals(cate))){
                            SharedPreferences.Editor editor = getApplicationContext().getSharedPreferences("weather_data", MODE_PRIVATE).edit();
                            Weather weather_new = Utility.handleWeatherResponse(responseText, weather, cate);
                            editor.putString(cate, responseText);
                            editor.apply();
                            switch (cate) {
                                case "now":
                                    requestWeather(weatherId, weather_new, "forecast");
                                    break;
                                case "forecast":
                                    requestWeather(weatherId, weather_new, "lifestyle");
                                    break;
                                case "lifestyle":
                                    assert weather_new != null;
                                    showWeatherInfo(weather_new);
                                    mWeatherId = weather.basic.weatherId;
                                    swipeRefreshLayout.setRefreshing(false);
                                    break;
                            }
                        }else {
                            swipeRefreshLayout.setRefreshing(false);
                            Toast.makeText(WeatherActivity.this, "请求到的数据出错", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
    }

    // 处理并展示实体类 Weather 的数据
    private void showWeatherInfo(Weather weather){
        loadBingPic();
        String cityName = weather.basic.cityName;
        String updateTime = weather.update.updateTime;
        String degree = weather.now.temperature + "℃";
        String weatherInfo = weather.now.info;

        titleCity.setText(cityName);
        titleUpdateTime.setText(updateTime);
        degreeText.setText(degree);
        weatherInfoText.setText(weatherInfo);
        forecastLayout.removeAllViews();
        for (Forecast.forecast forecast: weather.forecastList){
            View view= LayoutInflater.from(this).inflate(R.layout.forecast_item, forecastLayout, false);
            TextView dateText = view.findViewById(R.id.date_text);
            TextView infoText = view.findViewById(R.id.info_text);
            TextView maxText = view.findViewById(R.id.max_text);
            TextView minText = view.findViewById(R.id.min_text);
            dateText.setText(forecast.date);
            if (forecast.day.equals(forecast.night)){
                infoText.setText(forecast.day);
            }else {
                infoText.setText(String.format("%s转%s", forecast.day, forecast.night));
            }
            maxText.setText(forecast.tmp_max);
            minText.setText(forecast.tmp_min);
            forecastLayout.addView(view);
        }
        if(weather.now != null){
            humText.setText(weather.now.humidity);
            windText.setText(weather.now.directionOfWind);
        }
        for(LifeStyle.lifestyle lifestyle: weather.lifestyleList){
            if ("comf".equals(lifestyle.type)){
                comfortText.setText(String.format("舒适度：%s", lifestyle.txt));
            }
            if ("drsg".equals(lifestyle.type)){
                drsgText.setText(String.format("穿衣指数：%s", lifestyle.txt));
            }
            if ("sport".equals(lifestyle.type)){
                sportText.setText(String.format("运动建议：%s", lifestyle.txt));
            }
        }
        weatherLayout.setVisibility(View.VISIBLE);
        Intent intent = new Intent(this, AutoUpdateService.class);
        startService(intent);
    }
}
