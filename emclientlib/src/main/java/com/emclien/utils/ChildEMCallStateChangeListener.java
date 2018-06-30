package com.emclien.utils;

import android.app.Activity;
import android.content.Context;

import com.hyphenate.chat.EMCallStateChangeListener;

/**
 * +----------------------------------------------------------------------
 * |  说明     ：封装电话监听回调
 * +----------------------------------------------------------------------
 * | 创建者   :  zzh
 * +----------------------------------------------------------------------
 * 时　　间 ：2018/6/30 11:51
 * +----------------------------------------------------------------------
 * | 版权所有: 北京市车位管家科技有限公司
 * +----------------------------------------------------------------------
 */

public class ChildEMCallStateChangeListener implements EMCallStateChangeListener {

    private Context mContext;
    private  boolean mIsRunOnUIThread;
    private CallStateChangeListener mCallStateChangeListener;

    public ChildEMCallStateChangeListener(Context context , boolean isRunOnUIThread, CallStateChangeListener callStateChangeListener) {
        if (callStateChangeListener==null){
            throw new NullPointerException("CallStateChangeListener can not null");
        }
        mIsRunOnUIThread = isRunOnUIThread;
        mContext = context;
        this.mCallStateChangeListener = callStateChangeListener;
    }
    public ChildEMCallStateChangeListener( CallStateChangeListener callStateChangeListener) {
        this(null,false,callStateChangeListener);
    }

    public void onCallStateChanged(final CallState callState, final CallError callError) {
        if (mIsRunOnUIThread && mContext instanceof Activity){
            ((Activity)mContext).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    changeCallState(callState, callError);
                }
            });
        }else {
            changeCallState(callState, callError);
        }

    }

    private void changeCallState(CallState callState, CallError callError) {
        switch (callState) {
            case RINGING: // 响铃
                mCallStateChangeListener.ringing();
                break;
            case CONNECTING: // 正在连接对方
                mCallStateChangeListener.connecting();
                break;
            case CONNECTED: // 双方已经建立连接
                mCallStateChangeListener.connected();
                break;

            case ACCEPTED: // 电话接通成功
                mCallStateChangeListener.accepted();
                break;
            case DISCONNECTED: // 电话断了
                mCallStateChangeListener.disconnected(callError);
                break;
            case NETWORK_UNSTABLE: //网络不稳定
                if (callError == CallError.ERROR_NO_DATA) {
                    //无通话数据
                    mCallStateChangeListener.noData();
                } else {
                mCallStateChangeListener.networkUnstable();
                }
                break;
            case NETWORK_NORMAL: //网络恢复正常
                mCallStateChangeListener.networkNormal();
                break;
            case NETWORK_DISCONNECTED: //网络断开
                mCallStateChangeListener.networkDisconnected();
                break;
            default:
                break;
        }
    }

    public interface CallStateChangeListener {
        void ringing();

        void connecting();

        void connected();

        void disconnected(CallError callState);

        void accepted();

        void networkDisconnected();

        void networkUnstable();

        void noData();

        void networkNormal();
    }
}
