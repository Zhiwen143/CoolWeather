package com.zhiwen143.coolweather;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.zhiwen143.coolweather.db.City;
import com.zhiwen143.coolweather.db.County;
import com.zhiwen143.coolweather.db.Province;
import com.zhiwen143.coolweather.util.HttpUtil;
import com.zhiwen143.coolweather.util.Utility;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by msi on 2017/3/7.
 */

public class ChooseAreaFragment extends Fragment {
	
	
	public static final int LEVEL_PROVINCE = 0;
	public static final int LEVEL_CITY = 1;
	public static final int LEVEL_COUNTY = 2;
	/**
	 * 当前选中的级别
	 */
	private int currentLevel;
	/**
	 * 选中的省份
	 */
	private Province selectedProvince;
	/**
	 * 选中的城市
	 */
	private City selectedCity;
	
	private TextView mTitleText;
	private Button mBackButton;
	private ListView mListView;
	private List<String> dataList = new ArrayList<>();
	private ArrayAdapter<String> mAdapter;
	
	/**
	 * 省列表
	 */
	private List<Province> mProvinceList;
	/**
	 * 市列表
	 */
	private List<City> mCityList;
	/**
	 * 县列表
	 */
	private List<County> mCountyList;
	private ProgressDialog mProgressDialog;
	
	
	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.choose_area, container, false);
		mTitleText = (TextView) view.findViewById(R.id.title_text);
		mBackButton = (Button) view.findViewById(R.id.back_button);
		mListView = (ListView) view.findViewById(R.id.list_view);
		mAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, dataList);
		mListView.setAdapter(mAdapter);
		return view;
	}
	
	@Override
	public void onActivityCreated(@Nullable Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				if (currentLevel == LEVEL_PROVINCE) {
					selectedProvince = mProvinceList.get(position);
					queryCities();
				} else if (currentLevel == LEVEL_CITY) {
					selectedCity = mCityList.get(position);
					queryCounties();
				} else if (currentLevel == LEVEL_COUNTY) {
//					String weatherId = mCountyList.get(position).getWeatherId();
//					if (getActivity() instanceof MainActivity) {
//						Intent intent = new Intent(getActivity(), WeatherActivity.class);
//						intent.putExtra("weather_id", weatherId);
//						startActivity(intent);
//						getActivity().finish();
//					} else if (getActivity() instanceof WeatherActivity) {
						Toast.makeText(getContext(), "天气正在刷新", Toast.LENGTH_SHORT).show();
					Context context = getContext();
					Activity activity = (Activity) context;
					activity.runOnUiThread(new Runnable() {
						@Override
						public void run() {
							
						}
					});
					//					}
				}
			}
		});
		mBackButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (currentLevel == LEVEL_COUNTY) {
					queryCities();
				} else if (currentLevel == LEVEL_CITY) {
					queryProvinces();
				}
			}
		});
		queryProvinces();
	}
	
	/**
	 * 查询所有的省，优先从数据库查询，如果没有再去服务器上查询
	 */
	private void queryProvinces() {
		mTitleText.setText("中国");
		mBackButton.setVisibility(View.GONE);
		mProvinceList = DataSupport.findAll(Province.class);
		if (mProvinceList.size() > 0) {
			dataList.clear();
			for (Province province : mProvinceList) {
				dataList.add(province.getProvinceName());
			}
			mAdapter.notifyDataSetChanged();
			mListView.setSelection(0);
			currentLevel = LEVEL_PROVINCE;
		} else {
			String address = "http://guolin.tech/api/china";
			queryFromServer(address, "province");
		}
	}
	
	/**
	 * 查询所有的市，优先从数据库查询，如果没有再去服务器上查询
	 */
	private void queryCities() {
		mTitleText.setText(selectedProvince.getProvinceName());
		mBackButton.setVisibility(View.VISIBLE);
		mCityList = DataSupport.where("provinceid = ?", String.valueOf(selectedProvince.getId())).find(City.class);
		if (mCityList.size() > 0) {
			dataList.clear();
			for (City city : mCityList) {
				dataList.add(city.getCityName());
			}
			mAdapter.notifyDataSetChanged();
			mListView.setSelection(0);
			currentLevel = LEVEL_CITY;
		} else {
			int provinceCode = selectedProvince.getProvinceCode();
			String address = "http://guolin.tech/api/china/" + provinceCode;
			queryFromServer(address, "city");
		}
	}
	
	/**
	 * 查询所有的县，优先从数据库查询，如果没有再去服务器上查询
	 */
	private void queryCounties() {
		mTitleText.setText(selectedCity.getCityName());
		mBackButton.setVisibility(View.VISIBLE);
		mCountyList = DataSupport.where("cityid = ?", String.valueOf(selectedCity.getId())).find(County.class);
		if (mCountyList.size() > 0) {
			dataList.clear();
			for (County county : mCountyList) {
				dataList.add(county.getCountyName());
			}
			mAdapter.notifyDataSetChanged();
			mListView.setSelection(0);
			currentLevel = LEVEL_COUNTY;
		} else {
			int provinceCode = selectedProvince.getProvinceCode();
			int cityCode = selectedCity.getCityCode();
			String address = "http://guolin.tech/api/china/" + provinceCode + "/" + cityCode;
			queryFromServer(address, "county");
		}
	}
	
	/**
	 * 根据传入的地址和类型从服务器上查询所有的省市县数据
	 *
	 * @param address
	 * @param type
	 */
	private void queryFromServer(String address, final String type) {
		showProgressDialog();
		HttpUtil.sendOkHttpRequest(address, new Callback() {
			@Override
			public void onFailure(Call call, IOException e) {
				getActivity().runOnUiThread(new Runnable() {
					@Override
					public void run() {
						closeProgressDialog();
						Toast.makeText(getContext(), "加载失败！", Toast.LENGTH_SHORT).show();
					}
				});
			}
			
			@Override
			public void onResponse(Call call, Response response) throws IOException {
				String responseText = response.body().string();
				boolean result = false;
				if ("province".equals(type)) {
					result = Utility.handleProvincesResponse(responseText);
				} else if ("city".equals(type)) {
					result = Utility.handleCitiesResponse(responseText, selectedProvince.getId());
				} else if ("county".equals(type)) {
					result = Utility.handleCountiesResponse(responseText, selectedCity.getId());
				}
				if (result) {
					getActivity().runOnUiThread(new Runnable() {
						@Override
						public void run() {
							closeProgressDialog();
							if ("province".equals(type)) {
								queryProvinces();
							} else if ("city".equals(type)) {
								queryCities();
							} else if ("county".equals(type)) {
								queryCounties();
							}
						}
					});
				}
			}
		});
	}
	
	
	/**
	 * 显示进度对话框
	 */
	private void showProgressDialog() {
		if (mProgressDialog == null) {
			mProgressDialog = new ProgressDialog(getActivity());
			mProgressDialog.setMessage("正在加载...");
			mProgressDialog.setCanceledOnTouchOutside(false);
		}
		mProgressDialog.show();
	}
	
	/**
	 * 关闭进度对话框
	 */
	private void closeProgressDialog() {
		if (mProgressDialog != null) {
			mProgressDialog.dismiss();
		}
	}
}