package com.emclien.bean;

import java.io.Serializable;

/**
 * +----------------------------------------------------------------------
 * |  说明     ：语音、视频通话
 * +----------------------------------------------------------------------
 * | 创建者   :  zzh
 * +----------------------------------------------------------------------
 * 时　　间 ：2018/5/29 14:32
 * +----------------------------------------------------------------------
 * | 版权所有: 北京市车位管家科技有限公司
 * +----------------------------------------------------------------------
 */
public class CallEvent implements Serializable{
    public String userName;//昵称
    public boolean isComingCall;//是否正在呼叫
    public String ext;//自定义数据

    public CallEvent() {
    }

    public CallEvent(String userName, boolean isComingCall, String ext) {
        this.ext = ext;
        this.userName = userName;
        this.isComingCall = isComingCall;
    }


}
