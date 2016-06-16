package com.my.coolweather.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.my.coolweather.R;
import com.my.coolweather.bean.City;
import com.my.coolweather.bean.County;
import com.my.coolweather.bean.Province;
import com.my.coolweather.model.CoolWeatherDB;
import com.my.coolweather.util.HttpCallbackListener;
import com.my.coolweather.util.HttpUtil;
import com.my.coolweather.util.Utility;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/6/16.
 */
public class ChooseAreaActivity extends Activity{
    private static final int LEVEL_PROVINCE = 0;
    private static final int LEVEL_CITY = 1;
    private static final int LEVEL_COUNTY = 2;
    private ProgressDialog progressDialog;
    private TextView titleText;
    private ListView listView;
    private ArrayAdapter<String> adapter;
    private List<String> dataList = new ArrayList<>();
    private CoolWeatherDB coolWeatherDB;
    private List<Province> provinceList;
    private List<City> cityList;
    private List<County> countyList;
    private Province selectedProvince;
    private City selectedCity;
    private int currentLevel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.choose_area);
        titleText = (TextView)findViewById(R.id.title_text);
        listView = (ListView) findViewById(R.id.list_view);
        coolWeatherDB = CoolWeatherDB.getInstance(this);
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,dataList);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (currentLevel == LEVEL_PROVINCE) {
                    selectedProvince = provinceList.get(position);
                    queryCities();
                } else if (currentLevel == LEVEL_CITY) {
                    selectedCity = cityList.get(position);
                    queryCounties();
                }
            }
        });
        queryProvinces();
    }

    /**
     * 查询全国所有的省，优先从数据库查询，如果没有查询到再去服务器上查询
     */
    private void queryProvinces() {
        provinceList = coolWeatherDB.loadProvinces();
        if (provinceList.size()>0){
            dataList.clear();
            for (Province p:provinceList){
                dataList.add(p.getProvince_name());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            titleText.setText("中国");
            currentLevel = LEVEL_PROVINCE;
        }else{
            queryFromServer(null, "province");
        }
    }

    /**
     * 查询全国所有的市，优先从数据库查询，如果没有查询到再去服务器上查询
     */
    private void queryCities() {
        cityList = coolWeatherDB.loadCities(selectedProvince.getId());
        if(cityList.size()>0){
            dataList.clear();
            for(City c : cityList){
                dataList.add(c.getCity_name());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            titleText.setText(selectedProvince.getProvince_name());
            currentLevel = LEVEL_CITY;
        }else{
            queryFromServer(selectedProvince.getProvince_code(),"city");
        }

    }

    /**
     * 查询全国所有的县，优先从数据库查询，如果没有查询到再去服务器上查询
     */
    private void queryCounties() {
        countyList = coolWeatherDB.loadCounties(selectedCity.getId());
        if (countyList.size()>0){
            dataList.clear();
            for (County county : countyList){
                dataList.add(county.getCounty_name());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_COUNTY;

        }else{
            queryFromServer(selectedCity.getCity_code(),"county");
        }
    }

    /**
     * 根据传入的代号和类型从服务器上查询省市县的数据
     * @param code
     * @param type
     */
    private void queryFromServer(final String code, final String type) {

        String address;
        if(!TextUtils.isEmpty(code)){
            address = "http://www.weather.com.cn/data/list3/city"+code+".xml";
        }else{
            address = "http://www.weather.com.cn/data/list3/city.xml";
        }
        showProgressDialog();
        HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
            @Override
            public void onSucceed(String response) {
                boolean result = false;
                if("province".equals(type)){
                    result = Utility.handleProvincesResponse(coolWeatherDB,response);

                }else if ("city".equals(type)){
                    result = Utility.handleCitiesResponse(coolWeatherDB,response,selectedProvince.getId());
                }else if("county".equals(type)){
                    result = Utility.handleCountiesResponse(coolWeatherDB,response,selectedCity.getId());
                }
                if(result){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDialog();
                            if("province".equals(type)){
                                queryProvinces();
                            }else if("city".equals(type)){
                                queryCities();
                            }else if("county".equals(type)){
                                queryCounties();
                            }
                        }
                    });
                }
            }

            @Override
            public void onFailure(Exception e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDialog();
                        Toast.makeText(ChooseAreaActivity.this,"加载失败",Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    /**
     * 显示进度对话框
     */
    private void showProgressDialog() {
        if(progressDialog == null){
            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("正在加载...");
            progressDialog.setCanceledOnTouchOutside(false);
        }
        progressDialog.show();
    }
    /**
     * 关闭进度对话框
     */
    private void closeProgressDialog(){
        if (progressDialog != null){
            progressDialog.dismiss();
        }
    }

    /**
     * 根据当前的级别来判断，此时应该返回市列表。省列表还是直接退出
     */
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (currentLevel == LEVEL_COUNTY){
            queryCities();
        }else if(currentLevel == LEVEL_CITY){
            queryProvinces();
        }else{
            finish();
        }
    }
}
