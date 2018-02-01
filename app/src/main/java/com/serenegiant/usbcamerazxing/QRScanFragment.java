/*
******************* Copyright (c) ***********************\
**
**         (c) Copyright 2015, 蒋朋, china. sd
**                  All Rights Reserved
**
**                       _oo0oo_
**                      o8888888o
**                      88" . "88
**                      (| -_- |)
**                      0\  =  /0
**                    ___/`---'\___
**                  .' \\|     |// '.
**                 / \\|||  :  |||// \
**                / _||||| -:- |||||- \
**               |   | \\\  -  /// |   |
**               | \_|  ''\---/''  |_/ |
**               \  .-\__  '-'  ___/-. /
**             ___'. .'  /--.--\  `. .'___
**          ."" '<  `.___\_<|>_/___.' >' "".
**         | | :  `- \`.;`\ _ /`;.`/ - ` : | |
**         \  \ `_.   \_ __\ /__ _/   .-` /  /
**     =====`-.____`.___ \_____/___.-`___.-'=====
**                       `=---='
**
**
**     ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
**
**               佛祖保佑         永无BUG
**
**
**                   南无本师释迦牟尼佛
**

**----------------------版本信息------------------------
** 版    本: V0.1
**
******************* End of Head **********************\
*/

package com.serenegiant.usbcamerazxing;
/*
 * UVCCamera
 * library and sample to access to UVC web camera on non-rooted Android device
 *
 * Copyright (c) 2014-2015 saki t_saki@serenegiant.com
 *
 * File name: CameraFragment.java
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

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.graphics.Bitmap;
import android.hardware.usb.UsbDevice;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;

import com.serenegiant.serviceclient.CameraClient;
import com.serenegiant.serviceclient.ICameraClient;
import com.serenegiant.serviceclient.ICameraClientCallback;
import com.serenegiant.usb.CameraDialog;
import com.serenegiant.usb.DeviceFilter;
import com.serenegiant.usb.USBMonitor;
import com.serenegiant.usb.USBMonitor.OnDeviceConnectListener;
import com.serenegiant.usb.USBMonitor.UsbControlBlock;
import com.serenegiant.widget.CameraViewInterface;
import com.uuzuche.lib_zxing.CodeUtils;

import java.util.List;

public class QRScanFragment extends Fragment{
	private static final boolean DEBUG = true;
	private static final String TAG = "CameraFragment";

	private static final int DEFAULT_WIDTH = 640;
	private static final int DEFAULT_HEIGHT = 480;

	private USBMonitor mUSBMonitor;
	private ICameraClient mCameraClient;

	private CameraViewInterface mCameraView;
	private ImageView mImageView;               //扫描动画
	private TranslateAnimation mAnimation;       //动画

	private static final int MESSAGE_QR_SUCCESS = 1;

	private static final int SCAN_TIME = 16;            //到达扫描时间
	private int time2Scan;   //设置要扫描二维码的时间
	private String mQRString;   //二维码

	private AlertDialog mDialog;

	private Handler mHandler;

	public QRScanFragment() {
		if (DEBUG) Log.v(TAG, "Constructor:");
//		setRetainInstance(true);
	}

	@Override
	public void onAttach(final Activity activity) {
		super.onAttach(activity);
		if (DEBUG) Log.v(TAG, "onAttach:");
	}

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (DEBUG) Log.v(TAG, "onCreate:");
		if (mUSBMonitor == null) {
			mUSBMonitor = new USBMonitor(getActivity().getApplicationContext(), mOnDeviceConnectListener);
			final List<DeviceFilter> filters = DeviceFilter.getDeviceFilters(getActivity(), R.xml.device_filter);
			mUSBMonitor.setDeviceFilter(filters);
		}

		initHandler();

	}

	private void initHandler() {
		mHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
					case MESSAGE_QR_SUCCESS:
						Log.w(TAG, "扫描二维码: " + mQRString);

						if (!TextUtils.isEmpty(mQRString)) {
							if (mDialog != null) {
								if (mDialog.isShowing()) {
									return;
								}
							}

							mDialog = new AlertDialog.Builder(getActivity())
									.setTitle("Found QR")
									.setMessage(mQRString)
									.setPositiveButton("OK", null)
									.create();
									mDialog.show();
						}
						break;
				}

			}
		};
	}

	@Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
		if (DEBUG) Log.v(TAG, "onCreateView:");
		final View rootView = inflater.inflate(R.layout.fragment_qr_scan, container, false);

		mCameraView = (CameraViewInterface)rootView.findViewById(R.id.camera_view_qr);
		mCameraView.setAspectRatio(DEFAULT_WIDTH / (float)DEFAULT_HEIGHT);
		mCameraView.setCallback(mCallback);

		mImageView = (ImageView) rootView.findViewById(R.id.iv_qr_scan_animate);
		mAnimation = new TranslateAnimation(Animation.RELATIVE_TO_SELF,
				Animation.RELATIVE_TO_SELF, 0, 240);
		mAnimation.setDuration(4000);
//        mAnimation.setFillAfter(true);

		initHandler();

		mHandler.post(new Runnable() {
			@Override
			public void run() {
				//设置动画
				mImageView.startAnimation(mAnimation);

				mHandler.postDelayed(this, 4000);
			}
		});

		return rootView;
	}

	@Override
	public void onResume() {
		super.onResume();
		if (DEBUG) Log.v(TAG, "onResume:");
		mUSBMonitor.register();
	}

	@Override
	public void onPause() {
		if (DEBUG) Log.v(TAG, "onPause:");
		if (mCameraClient != null) {
			mCameraClient.removeSurface(mCameraView.getSurface());
		}
		mUSBMonitor.unregister();
		super.onPause();
	}

	@Override
	public void onDestroyView() {
		if (DEBUG) Log.v(TAG, "onDestroyView:");
		super.onDestroyView();
	}

	@Override
	public void onDestroy() {
		if (DEBUG) Log.v(TAG, "onDestroy:");
		if (mCameraClient != null) {
			mCameraClient.release();
			mCameraClient = null;
		}

		super.onDestroy();
	}

	@Override
	public void onDetach() {
		if (DEBUG) Log.v(TAG, "onDetach:");
		super.onDetach();
	}

	public USBMonitor getUSBMonitor() {
		return mUSBMonitor;
	}

	private final OnDeviceConnectListener mOnDeviceConnectListener = new OnDeviceConnectListener() {
		@Override
		public void onAttach(final UsbDevice device) {
			if (DEBUG) Log.v(TAG, "OnDeviceConnectListener#onAttach:");
			if (!updateCameraDialog() && (mCameraView.getSurface() != null)) {
				tryOpenUVCCamera(true);
			}
		}

		@Override
		public void onConnect(final UsbDevice device, final UsbControlBlock ctrlBlock, final boolean createNew) {
			if (DEBUG) Log.v(TAG, "OnDeviceConnectListener#onConnect:");
		}

		@Override
		public void onDisconnect(final UsbDevice device, final UsbControlBlock ctrlBlock) {
			if (DEBUG) Log.v(TAG, "OnDeviceConnectListener#onDisconnect:");
		}

		@Override
		public void onDettach(final UsbDevice device) {
			if (DEBUG) Log.v(TAG, "OnDeviceConnectListener#onDettach:");
			if (mCameraClient != null) {
				mCameraClient.disconnect();
				mCameraClient.release();
				mCameraClient = null;
			}
			updateCameraDialog();
		}

		@Override
		public void onCancel() {
			if (DEBUG) Log.v(TAG, "OnDeviceConnectListener#onCancel:");
		}
	};

	private boolean updateCameraDialog() {
		final Fragment fragment = getFragmentManager().findFragmentByTag("CameraDialog");
		if (fragment instanceof CameraDialog) {
			((CameraDialog)fragment).updateDevices();
			return true;
		}
		return false;
	}

	private void tryOpenUVCCamera(final boolean requestPermission) {
		if (DEBUG) Log.v(TAG, "tryOpenUVCCamera:");
		openUVCCamera(0);
	}

	public void openUVCCamera(final int index) {
		if (DEBUG) Log.v(TAG, "openUVCCamera:index=" + index);
		if (!mUSBMonitor.isRegistered()) return;
		final List<UsbDevice> list = mUSBMonitor.getDeviceList();
		if (list.size() > index) {
			if (mCameraClient == null)
				mCameraClient = new CameraClient(getActivity(), mCameraListener);
			mCameraClient.select(list.get(index));
			mCameraClient.resize(DEFAULT_WIDTH, DEFAULT_HEIGHT);
			mCameraClient.connect();
		}
	}

	private final CameraViewInterface.Callback mCallback = new CameraViewInterface.Callback() {
		@Override
		public void onSurfaceCreated(final Surface surface) {
//			tryOpenUVCCamera(true);
		}
		@Override
		public void onSurfaceChanged(final Surface surface, final int width, final int height) {
		}
		@Override
		public void onSurfaceDestroy(final Surface surface) {

		}

		@Override
		public void onSurfaceUpdate(Surface surface) {
			time2Scan++;

			//从TextureView获得　Bitmap
			final Bitmap bitmap = ((TextureView) mCameraView).getBitmap();

			if (time2Scan > SCAN_TIME) {
				time2Scan = 0;

				mHandler.post(new Runnable() {
					@Override
					public void run() {

						//识别二维码／条形码
						CodeUtils.analyzeBitmap(bitmap, new CodeUtils.AnalyzeCallback() {
							@Override
							public void onAnalyzeSuccess(Bitmap mBitmap, String result) {
								Log.w(TAG, "发现二维码： " + result);

								mQRString = result;
								mHandler.sendEmptyMessage(MESSAGE_QR_SUCCESS);

							}

							@Override
							public void onAnalyzeFailed() {
								Log.w(TAG, "二维码有误");
							}
						});
					}
				});
			}
		}
	};

	private final ICameraClientCallback mCameraListener = new ICameraClientCallback() {
		@Override
		public void onConnect() {
			if (DEBUG) Log.v(TAG, "onConnect:");
			mCameraClient.addSurface(mCameraView.getSurface(), false);
		}

		@Override
		public void onDisconnect() {
			if (DEBUG) Log.v(TAG, "onDisconnect:");
		}

	};

	private final OnCheckedChangeListener mOnCheckedChangeListener = new OnCheckedChangeListener() {
		@Override
		public void onCheckedChanged(final CompoundButton buttonView, final boolean isChecked) {
			if (DEBUG) Log.v(TAG, "onCheckedChanged:" + isChecked);
			if (isChecked) {
				mCameraClient.addSurface(mCameraView.getSurface(), false);
//				mCameraClient.addSurface(mCameraViewSub.getHolder().getSurface(), false);
			} else {
				mCameraClient.removeSurface(mCameraView.getSurface());
//				mCameraClient.removeSurface(mCameraViewSub.getHolder().getSurface());
			}
		}
	};


}
