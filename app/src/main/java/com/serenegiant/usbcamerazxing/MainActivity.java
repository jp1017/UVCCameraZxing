package com.serenegiant.usbcamerazxing;
/*
 * UVCCamera
 * library and sample to access to UVC web camera on non-rooted Android device
 *
 * Copyright (c) 2014-2015 saki t_saki@serenegiant.com
 *
 * File name: MainActivity.java
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 * All files in the folder are under this Apache License, Version 2.0.
 * Files in the jni/libjpeg, jni/libusb, jin/libuvc, jni/rapidjson folder may have a different license, see the respective files.
*/

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.Toast;

import com.serenegiant.utils.ImageUtil;
import com.uuzuche.lib_zxing.CodeUtils;

public class MainActivity extends AppCompatActivity {
	private static final boolean DEBUG = false;
	private static final String TAG = "MainActivity";

	private Toolbar mToolbar;

	//选择图片后的识别码
	private static final int REQUEST_QR_IMAGE = 480;

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);

		mToolbar = (Toolbar) findViewById(R.id.toolbar);

		mToolbar.setTitle("二维码识别");
		mToolbar.setTitleTextColor(Color.WHITE);
		setSupportActionBar(mToolbar);
		mToolbar.setNavigationIcon(R.drawable.ic_actionbar_back);

		if (savedInstanceState == null) {
			if (DEBUG) Log.i(TAG, "onCreate:new");
			final Fragment fragment = new QRScanFragment();
			getFragmentManager().beginTransaction()
					.add(R.id.container, fragment).commit();
		}
	}

	/**
	 * 相册选择后处理
	 * @param requestCode
	 * @param resultCode
	 * @param data
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == REQUEST_QR_IMAGE) {
			if (data != null) {
				Uri uri = data.getData();
				try {
					CodeUtils.analyzeBitmap(ImageUtil.getImageAbsolutePath(this, uri), new CodeUtils.AnalyzeCallback() {
						@Override
						public void onAnalyzeSuccess(Bitmap Bitmap, String result) {
							Log.w(TAG, "qrcode: " + result);

							new AlertDialog.Builder(MainActivity.this)
									.setTitle("Found QR")
									.setMessage(result)
									.setPositiveButton("OK", null)
									.create()
									.show();

						}

						@Override
						public void onAnalyzeFailed() {
							Toast.makeText(MainActivity.this, ("解析二维码失败"), Toast.LENGTH_LONG).show();
						}
					});
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * actionbar右侧添加菜单
	 * @param menu 要添加的菜单
	 * @return
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_pic_select, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				finish();
				break;

			//相册选择
			case R.id.menu_pic_select:
				Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
				intent.addCategory(Intent.CATEGORY_OPENABLE);
				intent.setType("image/*");
				startActivityForResult(intent, REQUEST_QR_IMAGE);
				break;

			default:
				break;
		}
		return super.onOptionsItemSelected(item);
	}

}
