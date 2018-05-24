# EMClient
环信lib
使用
1.在 projuct.gradle下配置

allprojects {
    repositories {
        google()
        jcenter()
        maven { url "https://raw.githubusercontent.com/HyphenateInc/Hyphenate-SDK-Android/master/repository" }
    }
}

2.在module.gradle下依赖项目
 compile 'com.github.TopTech666.EMClient:emclientlib:1.0.0'
 
3.项目初始化emclient
 1.在项目application的oncreate初始化
 
  public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        EMClientUtils.init(this,null);//第二个参数EMOptions，不配置默认为easyui中配置的initChatOptions（）；
    }
}

easyui中initChatOptions方法为：
protected EMOptions initChatOptions(){
        Log.d(TAG, "init HuanXin Options");

        EMOptions options = new EMOptions();
        // change to need confirm contact invitation
        options.setAcceptInvitationAlways(false);
        // set if need read ack
        options.setRequireAck(true);
        // set if need delivery ack
        options.setRequireDeliveryAck(false);
        //不自动登录
        options.setAutoLogin(false);
        
        return options;
    }
    
  2.在module的清单文件中配置环信消息接受者
     <!-- 设置环信应用的AppKey -->
        <meta-data
            android:name="EASEMOB_APPKEY"
            android:value="easemob-demo#chatdemoui"/><!--需要去环信上申请appkey-->
    
        <!-- 声明SDK所需的service SDK核心功能-->
        <service
            android:name="com.hyphenate.chat.EMChatService"
            android:exported="true"/>
        <service
            android:name="com.hyphenate.chat.EMJobService"
            android:exported="true"
            android:permission="android.permission.BIND_JOB_SERVICE"
            />
        <!-- 声明SDK所需的receiver -->
        <receiver android:name="com.hyphenate.chat.EMMonitorReceiver">
            <intent-filter>
                <action android:name="android.intent.action.PACKAGE_REMOVED"/>
                <data android:scheme="package"/>
            </intent-filter>
            <!-- 可选filter -->
            <intent-filter>
                <action android:name="android.intent.action.BOOT_COMPLETED"/>
                <action android:name="android.intent.action.USER_PRESENT"/>
            </intent-filter>
        </receiver>
        
     4.使用EMClientUtils中方法，目前接入了登录注册模块，消息模块，好友管理模块，1v1实时通话
