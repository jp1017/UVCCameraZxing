/*
******************* Copyright (c) ***********************\
**
**         (c) Copyright 2016, 蒋朋, china,. sd
**                  All Rights Reserved
**
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

package com.serenegiant.app;

import android.app.Application;

/**
 * 文 件 名: UVCApplication
 * 创 建 人: 蒋朋
 * 创建日期: 16-9-14 15:04
 * 邮    箱: jp19891017@gmail.com
 * 博    客: https://jp1017.github.io/
 * 描    述:
 * 修 改 人:
 * 修改时间：
 * 修改备注：
 */

public class UVCApplication extends Application {
    public static UVCApplication sInstance;

    @Override
    public void onCreate() {
        super.onCreate();
        sInstance = this;
    }

    public static UVCApplication getInstance() {
        return sInstance;
    }
}
