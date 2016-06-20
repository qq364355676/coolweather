package com.my.coolweather.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.my.coolweather.bean.City;
import com.my.coolweather.bean.County;
import com.my.coolweather.bean.Province;
import com.my.coolweather.db.CoolWeatherOpenHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/6/16.
 */
public class CoolWeatherDB {
    /**
     * 数据库名
     */
    public static final String DB_NAME = "cool_weather";
    /**
     * 数据库版本
     */
    public static final int VERSION = 1;

    private static CoolWeatherDB coolWeatherDB;
    private SQLiteDatabase db;
    private CoolWeatherDB(Context context){
        CoolWeatherOpenHelper dbHelper = new CoolWeatherOpenHelper(context,DB_NAME,null,VERSION);
        db = dbHelper.getWritableDatabase();
    }
    public  synchronized static CoolWeatherDB getInstance (Context context){
        if(coolWeatherDB == null){
            coolWeatherDB = new CoolWeatherDB(context);
        }
        return coolWeatherDB;
    }

    /**
     * 将province储存到数据库
     * @param province
     */
    public void saveProvince(Province province){
        if(province!=null){
            ContentValues cv = new ContentValues();
            cv.put("province_name",province.getProvince_name());
            cv.put("province_code",province.getProvince_code());
            db.insert("province",null,cv);
        }
    }

    /**
     * 读取全国所有的省份信息
     * @return
     */
    public List<Province> loadProvinces(){
        List<Province> list = new ArrayList<>();
        Cursor cursor = db.query("province",null,null,null,null,null,null);
        if(cursor.moveToFirst()){
            do {
                Province province = new Province();
                province.setId(cursor.getInt(cursor.getColumnIndex("id")));
                province.setProvince_name(cursor.getString(cursor.getColumnIndex("province_name")));
                province.setProvince_code(cursor.getString(cursor.getColumnIndex("province_code")));
                list.add(province);
            }while (cursor.moveToNext());
        }
        if (cursor != null)cursor.close();
        return list;
    }

    /**
     * 将 city实例储存到数据库
     * @param city
     */
    public void saveCity(City city){
        if(city!=null){
            ContentValues cv = new ContentValues();
            cv.put("city_name",city.getCity_name());
            cv.put("city_code",city.getCity_code());
            cv.put("province_id",city.getProvince_id());
            db.insert("city",null,cv);
        }
    }
    /**
     * 从数据库读取某省下所有的城市信息
     */
    public List<City> loadCities(int provinceId){
        List<City> list = new ArrayList<>();
        Cursor cursor = db.query("city",null,"province_id=?",new String[]{String.valueOf(provinceId)},null,null,null);
        while (cursor.moveToNext()){
            City city = new City();
            city.setId(cursor.getInt(cursor.getColumnIndex("id")));
            city.setCity_name(cursor.getString(cursor.getColumnIndex("city_name")));
            city.setCity_code(cursor.getString(cursor.getColumnIndex("city_code")));
            city.setProvince_id(provinceId);
            list.add(city);
        }
        if (cursor != null)cursor.close();
        return list;
    }
    /**
     * 将 couty 实例储存到数据库
     * @param county
     */
    public void saveCounty(County county){
        if(county!=null){
            ContentValues cv = new ContentValues();
            cv.put("county_name",county.getCounty_name());
            cv.put("county_code",county.getCounty_code());
            cv.put("city_id",county.getCity_id());
            db.insert("county",null,cv);
        }
    }
    /**
     * 从数据库读取某城市下所有的县信息
     */
    public List<County> loadCounties(int cityId){
        List<County> list = new ArrayList<>();
        Cursor cursor = db.query("county",null,"city_id=?",new String[]{(cityId+"")},null,null,null);
        if(cursor.moveToFirst()){
            do {
                County county = new County();
                county.setCity_id(cursor.getInt(cursor.getColumnIndex("id")));
                county.setCounty_name(cursor.getString(cursor.getColumnIndex("county_name")));
                county.setCounty_code(cursor.getString(cursor.getColumnIndex("county_code")));
                county.setCity_id(cityId);
                list.add(county);
            }while (cursor.moveToNext());
        }
        if (cursor != null)cursor.close();
        return list;
    }
}
