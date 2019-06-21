package com.emclien.manager;

import android.app.ActivityManager;
import android.content.Context;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.util.Log;

import com.emclien.emclientlib.BuildConfig;
import com.emclien.receiver.CallReceiver;
import com.hyphenate.EMMessageListener;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.chat.EMOptions;

import java.util.Iterator;
import java.util.List;


/**
 * +----------------------------------------------------------------------
 * |  说明     ：环信管理端
 * +----------------------------------------------------------------------
 * | 创建者   :  zzh
 * +----------------------------------------------------------------------
 * 时　　间 ：2018/5/15 17:14
 * +----------------------------------------------------------------------
 * | 版权所有: 北京市车位管家科技有限公司
 * +----------------------------------------------------------------------
 */
public class EMClientManager {
    public static String CALL_STATUES_DISCONNECT = "CALL_STATUES_DISCONNECT";
    public static String CALL_STATUES_CALLING = "CALL_STATUES_CALLING";
    public static String CALL_STATUES_IN_CALL = "CALL_STATUES_IN_CALL";
    private static String TAG = "EMClientManager";

    private static EMClientManager instance;
    private Context mAppContext;
    public EMMessageListener messageListener = null;
    public String mCallStatues = CALL_STATUES_DISCONNECT;
    public long mCallStartTime ;
    private CallReceiver callReceiver;
    private boolean mIsAutoLogin;//是否自动登录

    public static EMClientManager getInstance() {
        synchronized (EMClientManager.class) {
            if (instance == null) {
                instance = new EMClientManager();
            }
        }
        return instance;
    }

    /**
     * init flag: test if the sdk has been inited before, we don't need to init again
     */
    private boolean sdkInited = false;

    //emchat初始化
    public void init(Context context, EMOptions options) {
        if (sdkInited) {
            return;
        }
        mAppContext = context;
        int pid = android.os.Process.myPid();
        String processAppName = getAppName(pid);

        Log.d(TAG, "process app name : " + processAppName);

        // if there is application has remote service, application:onCreate() maybe called twice
        // this check is to make sure SDK will initialized only once
        // return if process name is not application's name since the package name is the default process name
        if (processAppName == null || !processAppName.equalsIgnoreCase(mAppContext.getPackageName())) {
            Log.e(TAG, "enter the service process!");
            return;
        }
        if (options == null) {
            EMClient.getInstance().init(context, initChatOptions());
        } else {
            EMClient.getInstance().init(context, options);
        }

        //debug mode, you'd better set it to false, if you want release your App officially.
        EMClient.getInstance().setDebugMode(BuildConfig.DEBUG);
        //get easeui instance
        //to set user's profile and avatar
//            setEaseUIProviders();
        //initialize preference manager
        PreferenceManager.init(context);
        //initialize profile manager
        //  getUserProfileManager().init(context);
        //set Call options
        setCallOptions();

        setGlobalListeners();
        sdkInited = true;
//            broadcastManager = LocalBroadcastManager.getInstance(appContext);
//            initDbDao();
    }

    public void init(Context context, EMOptions options, boolean isAutoLogin) {
        this.mIsAutoLogin = isAutoLogin;
        init(context, options);
    }

    /**
     * check the application process name if process name is not qualified, then we think it is a service process and we will not init SDK
     *
     * @param pID
     * @return
     */
    private String getAppName(int pID) {
        String processName = null;
        ActivityManager am = (ActivityManager) mAppContext.getSystemService(Context.ACTIVITY_SERVICE);
        List l = am.getRunningAppProcesses();
        Iterator i = l.iterator();
        PackageManager pm = mAppContext.getPackageManager();
        while (i.hasNext()) {
            ActivityManager.RunningAppProcessInfo info = (ActivityManager.RunningAppProcessInfo) (i.next());
            try {
                if (info.pid == pID) {
                    CharSequence c = pm.getApplicationLabel(pm.getApplicationInfo(info.processName, PackageManager.GET_META_DATA));
                    // Log.d("Process", "Id: "+ info.pid +" ProcessName: "+
                    // info.processName +"  Label: "+c.toString());
                    // processName = c.toString();
                    processName = info.processName;
                    return processName;
                }
            } catch (Exception e) {
                // Log.d("Process", "Error>> :"+ e.toString());
            }
        }
        return processName;
    }

    private void setCallOptions() {
//        HeadsetReceiver headsetReceiver = new HeadsetReceiver();
//        IntentFilter headsetFilter = new IntentFilter(Intent.ACTION_HEADSET_PLUG);
//        mAppContext.registerReceiver(headsetReceiver, headsetFilter);

        // min video kbps
        int minBitRate = PreferenceManager.getInstance().getCallMinVideoKbps();
        if (minBitRate != -1) {
            EMClient.getInstance().callManager().getCallOptions().setMinVideoKbps(minBitRate);
        }

        // max video kbps
        int maxBitRate = PreferenceManager.getInstance().getCallMaxVideoKbps();
        if (maxBitRate != -1) {
            EMClient.getInstance().callManager().getCallOptions().setMaxVideoKbps(maxBitRate);
        }

        // max frame rate
        int maxFrameRate = PreferenceManager.getInstance().getCallMaxFrameRate();
        if (maxFrameRate != -1) {
            EMClient.getInstance().callManager().getCallOptions().setMaxVideoFrameRate(maxFrameRate);
        }

        // audio sample rate
        int audioSampleRate = PreferenceManager.getInstance().getCallAudioSampleRate();
        if (audioSampleRate != -1) {
            EMClient.getInstance().callManager().getCallOptions().setAudioSampleRate(audioSampleRate);
        }

        /**
         * This function is only meaningful when your app need recording
         * If not, remove it.
         * This function need be called before the video stream started, so we set it in onCreate function.
         * This method will set the preferred video record encoding codec.
         * Using default encoding format, recorded file may not be played by mobile player.
         */
        //EMClient.getInstance().callManager().getVideoCallHelper().setPreferMovFormatEnable(true);

        // resolution
        String resolution = PreferenceManager.getInstance().getCallBackCameraResolution();
        if (resolution.equals("")) {
            resolution = PreferenceManager.getInstance().getCallFrontCameraResolution();
        }
        String[] wh = resolution.split("x");
        if (wh.length == 2) {
            try {
                EMClient.getInstance().callManager().getCallOptions().setVideoResolution(new Integer(wh[0]).intValue(), new Integer(wh[1]).intValue());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // enabled fixed sample rate
        boolean enableFixSampleRate = PreferenceManager.getInstance().isCallFixedVideoResolution();
        EMClient.getInstance().callManager().getCallOptions().enableFixedVideoResolution(enableFixSampleRate);

        // Offline call push
//        EMClient.getInstance().callManager().getCallOptions().setIsSendPushIfOffline(getModel().isPushCall());

        // 设置会议模式
        if (PreferenceManager.getInstance().isLargeConferenceMode()) {
//            EMClient.getInstance().conferenceManager().setConferenceMode(EMConferenceListener.ConferenceMode.LARGE);
        } else {
//            EMClient.getInstance().conferenceManager().setConferenceMode(EMConferenceListener.ConferenceMode.NORMAL);
        }
    }


    /**
     * set global listener
     */
    public void setGlobalListeners() {
////        syncGroupsListeners = new ArrayList<>();
////        syncContactsListeners = new ArrayList<>();
////        syncBlackListListeners = new ArrayList<>();
//
////        isGroupsSyncedWithServer = demoModel.isGroupsSynced();
////        isContactsSyncedWithServer = demoModel.isContactSynced();
////        isBlackListSyncedWithServer = demoModel.isBacklistSynced();
//
//        // create the global connection listener
////        connectionListener = new EMConnectionListener() {
////            @Override
////            public void onDisconnected(int error) {
////                EMLog.d("global listener", "onDisconnect" + error);
////                if (error == EMError.USER_REMOVED) {
////                    onUserException(Constant.ACCOUNT_REMOVED);
////                } else if (error == EMError.USER_LOGIN_ANOTHER_DEVICE) {
////                    onUserException(Constant.ACCOUNT_CONFLICT);
////                } else if (error == EMError.SERVER_SERVICE_RESTRICTED) {
////                    onUserException(Constant.ACCOUNT_FORBIDDEN);
////                } else if (error == EMError.USER_KICKED_BY_CHANGE_PASSWORD) {
////                    onUserException(Constant.ACCOUNT_KICKED_BY_CHANGE_PASSWORD);
////                } else if (error == EMError.USER_KICKED_BY_OTHER_DEVICE) {
////                    onUserException(Constant.ACCOUNT_KICKED_BY_OTHER_DEVICE);
////                }
////            }
////
////            @Override
////            public void onConnected() {
////                // in case group and contact were already synced, we supposed to notify sdk we are ready to receive the events
////                if (isGroupsSyncedWithServer && isContactsSyncedWithServer) {
////                    EMLog.d(TAG, "group and contact already synced with servre");
////                } else {
////                    if (!isGroupsSyncedWithServer) {
////                        asyncFetchGroupsFromServer(null);
////                    }
////
////                    if (!isContactsSyncedWithServer) {
////                        asyncFetchContactsFromServer(null);
////                    }
////
////                    if (!isBlackListSyncedWithServer) {
////                        asyncFetchBlackListFromServer(null);
////                    }
////                }
////            }
////        };
//
        IntentFilter callFilter = new IntentFilter(EMClient.getInstance().callManager().getIncomingCallBroadcastAction());
        if (callReceiver == null) {
            callReceiver = new CallReceiver();
        }
//        EMClient.getInstance().conferenceManager().addConferenceListener(new EMConferenceListener() {
//            @Override public void onMemberJoined(String username) {
//                EMLog.i(TAG, String.format("member joined username: %s, member: %d", username,
//                        EMClient.getInstance().conferenceManager().getConferenceMemberList().size()));
//            }
//
//            @Override public void onMemberExited(String username) {
//                EMLog.i(TAG, String.format("member exited username: %s, member size: %d", username,
//                        EMClient.getInstance().conferenceManager().getConferenceMemberList().size()));
//            }
//
//            @Override public void onStreamAdded(EMConferenceStream stream) {
//                EMLog.i(TAG, String.format("Stream added streamId: %s, streamName: %s, memberName: %s, username: %s, extension: %s, videoOff: %b, mute: %b",
//                        stream.getStreamId(), stream.getStreamName(), stream.getMemberName(), stream.getUsername(),
//                        stream.getExtension(), stream.isVideoOff(), stream.isAudioOff()));
//                EMLog.i(TAG, String.format("Conference stream subscribable: %d, subscribed: %d",
//                        EMClient.getInstance().conferenceManager().getAvailableStreamMap().size(),
//                        EMClient.getInstance().conferenceManager().getSubscribedStreamMap().size()));
//            }
//
//            @Override public void onStreamRemoved(EMConferenceStream stream) {
//                EMLog.i(TAG, String.format("Stream removed streamId: %s, streamName: %s, memberName: %s, username: %s, extension: %s, videoOff: %b, mute: %b",
//                        stream.getStreamId(), stream.getStreamName(), stream.getMemberName(), stream.getUsername(),
//                        stream.getExtension(), stream.isVideoOff(), stream.isAudioOff()));
//                EMLog.i(TAG, String.format("Conference stream subscribable: %d, subscribed: %d",
//                        EMClient.getInstance().conferenceManager().getAvailableStreamMap().size(),
//                        EMClient.getInstance().conferenceManager().getSubscribedStreamMap().size()));
//            }
//
//            @Override public void onStreamUpdate(EMConferenceStream stream) {
//                EMLog.i(TAG, String.format("Stream added streamId: %s, streamName: %s, memberName: %s, username: %s, extension: %s, videoOff: %b, mute: %b",
//                        stream.getStreamId(), stream.getStreamName(), stream.getMemberName(), stream.getUsername(),
//                        stream.getExtension(), stream.isVideoOff(), stream.isAudioOff()));
//                EMLog.i(TAG, String.format("Conference stream subscribable: %d, subscribed: %d",
//                        EMClient.getInstance().conferenceManager().getAvailableStreamMap().size(),
//                        EMClient.getInstance().conferenceManager().getSubscribedStreamMap().size()));
//            }
//
//            @Override public void onPassiveLeave(int error, String message) {
//                EMLog.i(TAG, String.format("passive leave code: %d, message: %s", error, message));
//            }
//
//            @Override public void onConferenceState(ConferenceState state) {
//                EMLog.i(TAG, String.format("State code=%d", state.ordinal()));
//            }
//
//            @Override public void onStreamSetup(String streamId) {
//                EMLog.i(TAG, String.format("Stream id - %s", streamId));
//            }
//
//            @Override
//            public void onSpeakers(List<String> speakers) {}
//
//            @Override public void onReceiveInvite(String confId, String password, String extension) {
//                EMLog.i(TAG, String.format("Receive conference invite confId: %s, password: %s, extension: %s", confId, password, extension));
//                if(easeUI.hasForegroundActivies() && easeUI.getTopActivity().getClass().getSimpleName().equals("ConferenceActivity")) {
//                    return;
//                }
//                Intent conferenceIntent = new Intent(appContext, ConferenceActivity.class);
//                conferenceIntent.putExtra(Constant.EXTRA_CONFERENCE_ID, confId);
//                conferenceIntent.putExtra(Constant.EXTRA_CONFERENCE_PASS, password);
//                conferenceIntent.putExtra(Constant.EXTRA_CONFERENCE_IS_CREATOR, false);
//                conferenceIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                appContext.startActivity(conferenceIntent);
//            }
//        });
//        //register incoming call receiver
        mAppContext.registerReceiver(callReceiver, callFilter);
//        //register connection listener
//        EMClient.getInstance().addConnectionListener(connectionListener);
//        //register group and contact event listener
////        registerGroupAndContactListener();
//        //register message event listener
//        registerMessageListener();
//
//    }


//    private EaseUser getUserInfo(String username){
//        // To get instance of EaseUser, here we get it from the user list in memory
//        // You'd better cache it if you get it from your server
//        EaseUser user = null;
//        if(username.equals(EMClient.getInstance().getCurrentUser()))
//            return getUserProfileManager().getCurrentUserInfo();
//        user = getContactList().get(username);
//        if(user == null && getRobotList() != null){
//            user = getRobotList().get(username);
//        }
//
//        // if user is not in your contacts, set inital letter for him/her
//        if(user == null){
//            user = new EaseUser(username);
//            EaseCommonUtils.setUserInitialLetter(user);
//        }
//        return user;
    }
    /**
     * EMEventListener
     */

    /**
     * Global listener
     * If this event already handled by an activity, you don't need handle it again
     * activityList.size() <= 0 means all activities already in background or not in Activity Stack
     */
//    public void registerMessageListener() {
//        messageListener = new EMMessageListener() {
//            private BroadcastReceiver broadCastReceiver = null;
//
//            @Override
//            public void onMessageReceived(List<EMMessage> messages) {
//                for (EMMessage message : messages) {
//                    EMLog.d(TAG, "onMessageReceived id : " + message.getMsgId());
//                    // in background, do not refresh UI, notify it in notification bar
//                    if(!easeUI.hasForegroundActivies()){
//                        getNotifier().onNewMsg(message);
//                    }
//                }
//            }
//
//            @Override
//            public void onCmdMessageReceived(List<EMMessage> messages) {
//                for (EMMessage message : messages) {
//                    EMLog.d(TAG, "receive command message");
//                    //get message body
//                    EMCmdMessageBody cmdMsgBody = (EMCmdMessageBody) message.getBody();
//                    final String action = cmdMsgBody.action();//获取自定义action
//                    //获取扩展属性 此处省略
//                    //maybe you need get extension of your message
//                    //message.getStringAttribute("");
//                    EMLog.d(TAG, String.format("Command：action:%s,message:%s", action,message.toString()));
//                }
//            }
//
//            @Override
//            public void onMessageRead(List<EMMessage> messages) {
//            }
//
//            @Override
//            public void onMessageDelivered(List<EMMessage> message) {
//            }
//
//            @Override
//            public void onMessageRecalled(List<EMMessage> messages) {
//                for (EMMessage msg : messages) {
//                    if(msg.getChatType() == EMMessage.ChatType.GroupChat && EaseAtMessageHelper.get().isAtMeMsg(msg)){
//                        EaseAtMessageHelper.get().removeAtMeGroup(msg.getTo());
//                    }
//                    EMMessage msgNotification = EMMessage.createReceiveMessage(EMMessage.Type.TXT);
//                    EMTextMessageBody txtBody = new EMTextMessageBody(String.format(mAppContext.getString(R.string.msg_recall_by_user), msg.getFrom()));
//                    msgNotification.addBody(txtBody);
//                    msgNotification.setFrom(msg.getFrom());
//                    msgNotification.setTo(msg.getTo());
//                    msgNotification.setUnread(false);
//                    msgNotification.setMsgTime(msg.getMsgTime());
//                    msgNotification.setLocalTime(msg.getMsgTime());
//                    msgNotification.setChatType(msg.getChatType());
//                    msgNotification.setAttribute(Constant.MESSAGE_TYPE_RECALL, true);
//                    msgNotification.setStatus(EMMessage.Status.SUCCESS);
//                    EMClient.getInstance().chatManager().saveMessage(msgNotification);
//                }
//            }
//
//            @Override
//            public void onMessageChanged(EMMessage message, Object change) {
//                EMLog.d(TAG, "change:");
//                EMLog.d(TAG, "change:" + change);
//            }
//        };
//
//        EMClient.getInstance().chatManager().addMessageListener(messageListener);
//    }

    /**
     * if ever logged in
     *
     * @return
     */
    public boolean isLoggedIn() {
        return EMClient.getInstance().isLoggedInBefore();
    }

    private boolean isGroupAndContactListenerRegisted;
    /**
     * register group and contact listener, you need register when login
     */
//    public void registerGroupAndContactListener(){
//        if(!isGroupAndContactListenerRegisted){
//            EMClient.getInstance().groupManager().addGroupChangeListener(new MyGroupChangeListener());
//            EMClient.getInstance().contactManager().setContactListener(new MyContactListener());
//            EMClient.getInstance().addMultiDeviceListener(new MyMultiDeviceListener());
//            isGroupAndContactListenerRegisted = true;
//        }
//    }

    /**
     * group change listener
     *//*
    class MyGroupChangeListener implements EMGroupChangeListener {

        @Override
        public void onInvitationReceived(String groupId, String groupName, String inviter, String reason) {

            new InviteMessgeDao(mAppContext).deleteMessage(groupId);

            // user invite you to join group
            InviteMessage msg = new InviteMessage();
            msg.setFrom(groupId);
            msg.setTime(System.currentTimeMillis());
            msg.setGroupId(groupId);
            msg.setGroupName(groupName);
            msg.setReason(reason);
            msg.setGroupInviter(inviter);
            showToast("receive invitation to join the group：" + groupName);
            msg.setStatus(InviteMessageStatus.GROUPINVITATION);
            notifyNewInviteMessage(msg);
            broadcastManager.sendBroadcast(new Intent(Constant.ACTION_GROUP_CHANAGED));
        }

        @Override
        public void onInvitationAccepted(String groupId, String invitee, String reason) {

            new InviteMessgeDao(mAppContext).deleteMessage(groupId);

            //user accept your invitation
            boolean hasGroup = false;
            EMGroup _group = null;
            for (EMGroup group : EMClient.getInstance().groupManager().getAllGroups()) {
                if (group.getGroupId().equals(groupId)) {
                    hasGroup = true;
                    _group = group;
                    break;
                }
            }
            if (!hasGroup)
                return;

            InviteMessage msg = new InviteMessage();
            msg.setFrom(groupId);
            msg.setTime(System.currentTimeMillis());
            msg.setGroupId(groupId);
            msg.setGroupName(_group == null ? groupId : _group.getGroupName());
            msg.setReason(reason);
            msg.setGroupInviter(invitee);
            showToast(invitee + "Accept to join the group：" + _group == null ? groupId : _group.getGroupName());
            msg.setStatus(InviteMessageStatus.GROUPINVITATION_ACCEPTED);
            notifyNewInviteMessage(msg);
            broadcastManager.sendBroadcast(new Intent(Constant.ACTION_GROUP_CHANAGED));
        }

        @Override
        public void onInvitationDeclined(String groupId, String invitee, String reason) {

            new InviteMessgeDao(mAppContext).deleteMessage(groupId);

            //user declined your invitation
            EMGroup group = null;
            for (EMGroup _group : EMClient.getInstance().groupManager().getAllGroups()) {
                if (_group.getGroupId().equals(groupId)) {
                    group = _group;
                    break;
                }
            }
            if (group == null)
                return;

            InviteMessage msg = new InviteMessage();
            msg.setFrom(groupId);
            msg.setTime(System.currentTimeMillis());
            msg.setGroupId(groupId);
            msg.setGroupName(group.getGroupName());
            msg.setReason(reason);
            msg.setGroupInviter(invitee);
            showToast(invitee + "Declined to join the group：" + group.getGroupName());
            msg.setStatus(InviteMessageStatus.GROUPINVITATION_DECLINED);
            notifyNewInviteMessage(msg);
            broadcastManager.sendBroadcast(new Intent(Constant.ACTION_GROUP_CHANAGED));
        }

        @Override
        public void onUserRemoved(String groupId, String groupName) {
            //user is removed from group
            broadcastManager.sendBroadcast(new Intent(Constant.ACTION_GROUP_CHANAGED));
            showToast("current user removed, groupId:" + groupId);
        }

        @Override
        public void onGroupDestroyed(String groupId, String groupName) {
            // group is dismissed,
            broadcastManager.sendBroadcast(new Intent(Constant.ACTION_GROUP_CHANAGED));
            showToast("group destroyed, groupId:" + groupId);
        }

        @Override
        public void onRequestToJoinReceived(String groupId, String groupName, String applyer, String reason) {

            // user apply to join group
            InviteMessage msg = new InviteMessage();
            msg.setFrom(applyer);
            msg.setTime(System.currentTimeMillis());
            msg.setGroupId(groupId);
            msg.setGroupName(groupName);
            msg.setReason(reason);
            showToast(applyer + " Apply to join group：" + groupId);
            msg.setStatus(InviteMessageStatus.BEAPPLYED);
            notifyNewInviteMessage(msg);
            broadcastManager.sendBroadcast(new Intent(Constant.ACTION_GROUP_CHANAGED));
        }

        @Override
        public void onRequestToJoinAccepted(String groupId, String groupName, String accepter) {

            String st4 = mAppContext.getString(R.string.Agreed_to_your_group_chat_application);
            // your application was accepted
            EMMessage msg = EMMessage.createReceiveMessage(Type.TXT);
            msg.setChatType(ChatType.GroupChat);
            msg.setFrom(accepter);
            msg.setTo(groupId);
            msg.setMsgId(UUID.randomUUID().toString());
            msg.addBody(new EMTextMessageBody(accepter + " " +st4));
            msg.setStatus(Status.SUCCESS);
            // save accept message
            EMClient.getInstance().chatManager().saveMessage(msg);
            // notify the accept message
            getNotifier().vibrateAndPlayTone(msg);

            showToast("request to join accepted, groupId:" + groupId);
            broadcastManager.sendBroadcast(new Intent(Constant.ACTION_GROUP_CHANAGED));
        }

        @Override
        public void onRequestToJoinDeclined(String groupId, String groupName, String decliner, String reason) {
            // your application was declined, we do nothing here in demo
            showToast("request to join declined, groupId:" + groupId);
        }

        @Override
        public void onAutoAcceptInvitationFromGroup(String groupId, String inviter, String inviteMessage) {
            // got an invitation
            String st3 = mAppContext.getString(R.string.Invite_you_to_join_a_group_chat);
            EMMessage msg = EMMessage.createReceiveMessage(Type.TXT);
            msg.setChatType(ChatType.GroupChat);
            msg.setFrom(inviter);
            msg.setTo(groupId);
            msg.setMsgId(UUID.randomUUID().toString());
            msg.addBody(new EMTextMessageBody(inviter + " " +st3));
            msg.setStatus(EMMessage.Status.SUCCESS);
            // save invitation as messages
            EMClient.getInstance().chatManager().saveMessage(msg);
            // notify invitation message
            getNotifier().vibrateAndPlayTone(msg);
            showToast("auto accept invitation from groupId:" + groupId);
            broadcastManager.sendBroadcast(new Intent(Constant.ACTION_GROUP_CHANAGED));
        }

        // ============================= group_reform new add api begin
        @Override
        public void onMuteListAdded(String groupId, final List<String> mutes, final long muteExpire) {
            StringBuilder sb = new StringBuilder();
            for (String member : mutes) {
                sb.append(member).append(",");
            }
            showToast("onMuterListAdded: " + sb.toString());
        }


        @Override
        public void onMuteListRemoved(String groupId, final List<String> mutes) {
            StringBuilder sb = new StringBuilder();
            for (String member : mutes) {
                sb.append(member).append(",");
            }
            showToast("onMuterListRemoved: " + sb.toString());
        }


        @Override
        public void onAdminAdded(String groupId, String administrator) {
            showToast("onAdminAdded: " + administrator);
        }

        @Override
        public void onAdminRemoved(String groupId, String administrator) {
            showToast("onAdminRemoved: " + administrator);
        }

        @Override
        public void onOwnerChanged(String groupId, String newOwner, String oldOwner) {
            showToast("onOwnerChanged new:" + newOwner + " old:" + oldOwner);
        }

        @Override
        public void onMemberJoined(String groupId, String member) {
            showToast("onMemberJoined: " + member);
        }

        @Override
        public void onMemberExited(String groupId, String member) {
            showToast("onMemberExited: " + member);
        }

        @Override
        public void onAnnouncementChanged(String groupId, String announcement) {
            showToast("onAnnouncementChanged, groupId" + groupId);
        }

        @Override
        public void onSharedFileAdded(String groupId, EMMucSharedFile sharedFile) {
            showToast("onSharedFileAdded, groupId" + groupId);
        }

        @Override
        public void onSharedFileDeleted(String groupId, String fileId) {
            showToast("onSharedFileDeleted, groupId" + groupId);
        }
        // ============================= group_reform new add api end
    }
*/
    protected EMOptions initChatOptions() {
        Log.d(TAG, "init HuanXin Options");

        EMOptions options = new EMOptions();
        // change to need confirm contact invitation
        options.setAcceptInvitationAlways(false);
        // set if need read ack
        options.setRequireAck(true);
        // set if need delivery ack
        if (!mIsAutoLogin) {
            options.setRequireDeliveryAck(false);
        }

        options.setAutoLogin(false);
        return options;
    }

    /**
     * new message options provider
     */
    public interface EaseSettingsProvider {
        boolean isMsgNotifyAllowed(EMMessage message);

        boolean isMsgSoundAllowed(EMMessage message);

        boolean isMsgVibrateAllowed(EMMessage message);

        boolean isSpeakerOpened();
    }

}
