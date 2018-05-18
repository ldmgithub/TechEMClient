//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.emclien.utils;

import android.util.Log;

import com.emclien.emclientlib.BuildConfig;

public class TagLibUtil {
    private static final String TAG = "TagLibUtil";

    public TagLibUtil() {
    }

  

    public static void showLogDebug(String str) {
        if(BuildConfig.DEBUG) {
            Log.d("TagLibUtil", str);
        }

    }

    public static void showLogDebug(Class context, String str) {
        if(BuildConfig.DEBUG) {
            Log.d("TagLibUtil", "<" + context.getName().toString() + ">--" + str);
        }

    }

    public static void showLogError(String str) {
        if(BuildConfig.DEBUG) {
            Log.e("TagLibUtil", str);
        }

    }

    public static void showLogError(Class context, String str) {
        if(BuildConfig.DEBUG) {
            Log.e("TagLibUtil", "<" + context.getName().toString() + ">--" + str);
        }

    }

    public static void showLogDebug(String tag, String content) {
        if(BuildConfig.DEBUG) {
            Log.d(tag, content);
        }

    }
}
