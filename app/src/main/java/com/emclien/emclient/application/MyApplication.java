package com.emclien.emclient.application;

import android.app.Application;

import com.emclien.manager.EMClientUtils;

/**
 * +----------------------------------------------------------------------
 * |  说明     ：
 * +----------------------------------------------------------------------
 * | 创建者   :  zzh
 * +----------------------------------------------------------------------
 *   时　　间 ：2018/5/16 16:05
 * +----------------------------------------------------------------------
 * | 版权所有: 北京市车位管家科技有限公司
 * +----------------------------------------------------------------------
*/
public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        EMClientUtils.init(this,null,false);
    }
}
