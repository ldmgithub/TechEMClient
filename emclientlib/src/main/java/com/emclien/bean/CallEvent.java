package com.emclien.bean;

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
public class CallEvent {
    public String userName;//昵称
    public String headPortrait;//头像
    public boolean isComingCall;//是否正在呼叫

    public CallEvent() {
    }

    public CallEvent(String userName, String headPortrait, boolean isComingCall) {
        this.userName = userName;
        this.headPortrait = headPortrait;
        this.isComingCall = isComingCall;
    }


}
