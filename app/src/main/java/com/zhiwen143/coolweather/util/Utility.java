package com.zhiwen143.coolweather.util;

import android.text.TextUtils;

import com.zhiwen143.coolweather.db.City;
import com.zhiwen143.coolweather.db.County;
import com.zhiwen143.coolweather.db.Province;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by msi on 2017/3/7.
 */

public class Utility {
	/**
	 * 解析并处理服务器返回的省级数据
	 *
	 * @param response
	 * @return
	 */
	public static boolean handleProvincesResponse(String response) {
		if (!TextUtils.isEmpty(response)) {
			try {
				JSONArray allProvinces = new JSONArray(response);
				for (int i = 0; i < allProvinces.length(); i++) {
					JSONObject provincesObject = allProvinces.getJSONObject(i);
					Province province = new Province();
					province.setProvinceName(provincesObject.getString("name"));
					province.setProvinceCode(provincesObject.getInt("id"));
					province.save();
				}
				return true;
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return false;
	}
	
	/**
	 * 解析并处理服务器返回的市级数据
	 *
	 * @param response
	 * @param provinceId
	 * @return
	 */
	public static boolean handleCitiesResponse(String response, int provinceId) {
		if (!TextUtils.isEmpty(response)) {
			try {
				JSONArray allCities = new JSONArray(response);
				for (int i = 0; i < allCities.length(); i++) {
					JSONObject cityObject = allCities.getJSONObject(i);
					City city = new City();
					city.setCityName(cityObject.getString("name"));
					city.setCityCode(cityObject.getInt("id"));
					city.setProvinceId(provinceId);
					city.save();
				}
				return true;
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return false;
	}
	
	/**
	 * 解析并处理服务器返回的县级数据
	 * @param response
	 * @param cityId
	 * @return
	 */
	public static boolean handleCountiesResponse(String response, int cityId) {
		if (!TextUtils.isEmpty(response)) {
			try {
				JSONArray allCounties = new JSONArray(response);
				for (int i = 0; i < allCounties.length(); i++) {
					JSONObject countyObject = allCounties.getJSONObject(i);
					County county = new County();
					county.setCountyName(countyObject.getString("name"));
					county.setWeatherId(countyObject.getString("weather_id"));
					county.setCityId(cityId);
					county.save();
				}
				return true;
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return false;
	}
	
}
