package com.my.coolweather.util;

/**
 * Created by Administrator on 2016/6/16.
 */
public interface HttpCallbackListener {
    void onSucceed(String response);
    void onFailure(Exception e);
}
