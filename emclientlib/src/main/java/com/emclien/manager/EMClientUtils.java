package com.emclien.manager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.text.TextUtils;
import android.util.Log;

import com.hyphenate.EMCallBack;
import com.hyphenate.EMConnectionListener;
import com.hyphenate.EMContactListener;
import com.hyphenate.EMMessageListener;
import com.hyphenate.EMValueCallBack;
import com.hyphenate.chat.EMCallStateChangeListener;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMCmdMessageBody;
import com.hyphenate.chat.EMConversation;
import com.hyphenate.chat.EMCursorResult;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.chat.EMOptions;
import com.hyphenate.chat.EMVideoCallHelper;
import com.hyphenate.exceptions.EMNoActiveCallException;
import com.hyphenate.exceptions.EMServiceNotReadyException;
import com.hyphenate.exceptions.HyphenateException;
import com.hyphenate.media.EMCallSurfaceView;
import com.hyphenate.util.EMLog;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * +----------------------------------------------------------------------
 * |  说明    ：聊天工具类
 *    创建一条文本消息，content为消息文字内容，toChatUsername为对方用户或者群聊的id，
 *    目前接入了登录注册模块，消息模块，好友管理模块，1v1实时通话
 * +----------------------------------------------------------------------
 * | 创建者   :  zzh
 * +----------------------------------------------------------------------
 * 时　　间 ：2018/5/16 13:54
 * +----------------------------------------------------------------------
 * | 版权所有: 北京市车位管家科技有限公司
 * +----------------------------------------------------------------------
 */

public class EMClientUtils {
    private static final String TAG = "EMClientUtils";

    //初始化
    public static void init(Context context, EMOptions options) {
        //options.setAutoLogin(false);根据项目要求是否设置自动登录，默认是true自动登录
        EMClientManager.getInstance().init(context, options);
    }

    //初始化
    public static void init(Context context, EMOptions options,boolean mIsAutoLogin) {
        //options.setAutoLogin(false);根据项目要求是否设置自动登录，默认是true自动登录
        if (mIsAutoLogin){
            EMClientManager.getInstance().init(context, options);
        }else {
            EMClientManager.getInstance().init(context, options,false);
        }
    }

    //登录
    public static void login(String username, String password, EMCallBack callBack) {
        EMClient.getInstance().login(username, password, callBack);
    }

    //注册，该方法同步,仅供测试用，官方推荐通过 开发者通过后台调用 REST 接口去注册环信 ID ，
    // 此方法必须在子线程中运行，否则无法注册成功
    @Deprecated
    public static void register(String username, String password) throws HyphenateException {
            EMClient.getInstance().createAccount(username, password);

    }

    //退出登录，该方法同步
    public static void logout() {
        EMClient.getInstance().logout(true);
    }

    //退出登录，该方法异步
    public static void logoutSyn(EMCallBack callBack) {
        EMClient.getInstance().logout(true, callBack);
    }

    /***************************************************
     * 方法描述 ：注册一个监听连接状态的listener
     * 方法名  : addConnectionListener
     * EMError.USER_REMOVED 显示帐号已经被移除
     *  EMError.USER_LOGIN_ANOTHER_DEVICE 显示帐号在其他设备登录  }
     **************************************************/
    public static void addConnectionListener(EMConnectionListener listener) {
        EMClient.getInstance().addConnectionListener(listener);
    }

    //-------------------------------消息-------------------------------------------------

    /***************************************************
     * 方法描述 ：发送文案
     * 方法名  : sendMessageTxt 创建一条文本消息，content为消息文字内容，toChatUsername为对方用户或者群聊的id，后文皆是如此
     **************************************************/
    public static void sendMessageTxt(String content, String toChatUsername, EMCallBack emCallBack) {

        EMMessage message = EMMessage.createTxtSendMessage(content, toChatUsername);
        //发送消息
        sendMessage(message, emCallBack);
    }

    /***************************************************
     * 方法描述 ：发送语音
     * 方法名  : sendMessageVoice
     **************************************************/
    public static void sendMessageVoice(String filePath, int length, String toChatUsername, EMCallBack emCallBack) {
        //filePath为语音文件路径，length为录音时间(秒)
        EMMessage message = EMMessage.createVoiceSendMessage(filePath, length, toChatUsername);
        sendMessage(message, emCallBack);
    }

    /***************************************************
     * 方法描述 ：发送语音
     * 方法名  : sendMessageVideo
     * params : videoPath为视频本地路径，thumbPath为视频预览图路径，videoLength为视频时间长度
     **************************************************/
    public static void sendMessageVideo(String videoPath, String thumbPath, int videoLength, String toChatUsername, EMCallBack emCallBack) {
        //videoPath为视频本地路径，thumbPath为视频预览图路径，videoLength为视频时间长度
        EMMessage message = EMMessage.createVideoSendMessage(videoPath, thumbPath, videoLength, toChatUsername);
        sendMessage(message, emCallBack);
    }

    /***************************************************
     * 方法描述 ：发送图片
     * 方法名  : sendMessageImage
     * params : imagePath为图片本地路径，false为不发送原图（默认超过100k的图片会压缩后发给对方），需要发送原图传true
     **************************************************/
    public static void sendMessageImage(String imagePath, String toChatUsername, EMCallBack emCallBack) {
        EMMessage message = EMMessage.createImageSendMessage(imagePath, false, toChatUsername);
        sendMessage(message, emCallBack);
    }

    /***************************************************
     * 方法描述 ：发送地理位置
     * 方法名  : sendMessageLocation
     * parems : latitude为纬度，longitude为经度，locationAddress为具体位置内容
     **************************************************/
    public static void sendMessageLocation(double latitude, double longitude, String locationAddress, String toChatUsername, EMCallBack emCallBack) {
        EMMessage message = EMMessage.createLocationSendMessage(latitude, longitude, locationAddress, toChatUsername);
        sendMessage(message, emCallBack);
    }

    /***************************************************
     * 方法描述 ：发送地理位置
     * 方法名  : sendMessageFile
     * params : filePath 文件路径
     **************************************************/
    public static void sendMessageFile(String filePath, String toChatUsername, EMCallBack emCallBack) {
        EMMessage message = EMMessage.createFileSendMessage(filePath, toChatUsername);
        sendMessage(message, emCallBack);
    }

    /***************************************************
     * 方法描述 ：发送传透信息
     * 方法名  : sendMessageAction
     *  说明  ： 透传消息能做什么：头像、昵称的更新等。可以把透传消息理解为一条指令，
     *  通过发送这条指令给对方，告诉对方要做的 action，收到消息可以自定义处理的一种消息。（透传消息不会存入本地数据库中，所以在 UI 上是不会显示的）
     **************************************************/
    public static void sendMessageAction(String action, String toChatUsername, EMCallBack emCallBack) {
        EMMessage cmdMsg = EMMessage.createSendMessage(EMMessage.Type.CMD);
        EMCmdMessageBody cmdBody = new EMCmdMessageBody(action);
        cmdMsg.setTo(toChatUsername);
        cmdMsg.addBody(cmdBody);
        sendMessage(cmdMsg, emCallBack);
    }

    /***************************************************
     * 方法描述 ：发送自定义信息
     * 方法名  : sendMessageCustom
     * params ： params 自定义属性集合
     * 说明  ：  接受信息 ：接收消息的时候获取到扩展属性，获取自定义的属性，第2个参数为没有此定义的属性时返回的默认值
     *          message.getStringAttribute("attribute1",null);
     *          message.getBooleanAttribute("attribute2", false);
     ***************************************************/
    public static void sendMessageCustom(String toChatUsername, String content, Map<String, Object> params, EMCallBack emCallBack) {
        EMMessage message = EMMessage.createTxtSendMessage(content, toChatUsername);
        Iterator iterator = params.keySet().iterator();
        EMLog.d(TAG, "params=" + params.toString());
        while (iterator.hasNext()) {
            Map.Entry<String, Object> enter = (Map.Entry<String, Object>) iterator.next();
            message.setAttribute(enter.getKey(), String.valueOf(enter.getValue()));
        }
        sendMessage(message, emCallBack);
    }


    /***************************************************
     * 方法描述 ：群聊-发送文案
     * 方法名  : sendGroupMessageTxt
     **************************************************/
    public static void sendGroupMessageTxt(String content, String toChatUsername, EMCallBack emCallBack) {
        EMMessage message = EMMessage.createTxtSendMessage(content, toChatUsername);
        sendGroupMessage(message, emCallBack);
    }

    /***************************************************
     * 方法描述 ：群聊发送图片
     * 方法名  : sendGroupMessageImage
     **************************************************/
    public static void sendGroupMessageImage(String imagePath, String toChatUsername, EMCallBack emCallBack) {
        //imagePath为图片本地路径，false为不发送原图（默认超过100k的图片会压缩后发给对方），需要发送原图传true
        EMMessage message = EMMessage.createImageSendMessage(imagePath, false, toChatUsername);
        sendGroupMessage(message, emCallBack);
    }

    /***************************************************
     * 方法描述 ：群聊-发送语音
     * 方法名  : sendGroupMessageVoice
     **************************************************/
    public static void sendGroupMessageVoice(String filePath, int length, String toChatUsername, EMCallBack emCallBack) {
        //filePath为语音文件路径，length为录音时间(秒)
        EMMessage message = EMMessage.createVoiceSendMessage(filePath, length, toChatUsername);
        sendGroupMessage(message, emCallBack);
    }


    /***************************************************
     * 方法描述 ：群聊-发送语音
     * 方法名  : sendGroupMessageVideo
     **************************************************/
    public static void sendGroupMessageVideo(String videoPath, String thumbPath, int videoLength, String toChatUsername, EMCallBack emCallBack) {
        //videoPath为视频本地路径，thumbPath为视频预览图路径，videoLength为视频时间长度
        EMMessage message = EMMessage.createVideoSendMessage(videoPath, thumbPath, videoLength, toChatUsername);
        sendGroupMessage(message, emCallBack);
    }

    /***************************************************
     * 方法描述 ：群聊-发送地理位置
     * 方法名  : sendGroupMessageLocation
     **************************************************/
    public static void sendGroupMessageLocation(double latitude, double longitude, String locationAddress, String toChatUsername, EMCallBack emCallBack) {
        EMMessage message = EMMessage.createLocationSendMessage(latitude, longitude, locationAddress, toChatUsername);
        sendGroupMessage(message, emCallBack);
    }

    /***************************************************
     * 方法描述 ：群聊-发送地理位置
     * 方法名  : sendGroupMessageFile
     **************************************************/
    public static void sendGroupMessageFile(String filePath, String toChatUsername, EMCallBack emCallBack) {
        EMMessage message = EMMessage.createFileSendMessage(filePath, toChatUsername);
        sendGroupMessage(message, emCallBack);
    }

    /***************************************************
     * 方法描述 ：群发-发送传透信息
     * 方法名  : sendGroupMessageAction
     *  说明  ： 透传消息能做什么：头像、昵称的更新等。可以把透传消息理解为一条指令，
     *  通过发送这条指令给对方，告诉对方要做的 action，收到消息可以自定义处理的一种消息。（透传消息不会存入本地数据库中，所以在 UI 上是不会显示的）
     **************************************************/
    public static void sendGroupMessageAction(String action, String toChatUsername, EMCallBack emCallBack) {
        EMMessage cmdMsg = EMMessage.createSendMessage(EMMessage.Type.CMD);
        EMCmdMessageBody cmdBody = new EMCmdMessageBody(action);
        cmdMsg.setTo(toChatUsername);
        cmdMsg.addBody(cmdBody);
        sendGroupMessage(cmdMsg, emCallBack);
    }

    /***************************************************
     * 方法描述 ：群聊-发送自定义信息
     * 方法名  : sendMessageCustom
     * params ： params 自定义属性集合
     * 说明  ：  接受信息 ：接收消息的时候获取到扩展属性，获取自定义的属性，第2个参数为没有此定义的属性时返回的默认值
     *          message.getStringAttribute("attribute1",null);
     *          message.getBooleanAttribute("attribute2", false);
     ***************************************************/
    public static void sendGroupMessageCustom(String toChatUsername, String content, Map<String, Object> params, EMCallBack emCallBack) {
        EMMessage message = EMMessage.createTxtSendMessage(content, toChatUsername);
        Iterator iterator = params.keySet().iterator();
        EMLog.d(TAG, "params=" + params.toString());
        while (iterator.hasNext()) {
            Map.Entry<String, Object> enter = (Map.Entry<String, Object>) iterator.next();
            message.setAttribute(enter.getKey(), String.valueOf(enter.getValue()));
        }
        sendGroupMessage(message, emCallBack);
    }

    /***************************************************
     * 方法描述 ：发生单条消息
     * 方法名  :  sendMessage
     * params ：  EMMessage message 消息对象 ,EMCallBack 监听发送状态
     **************************************************/
    private static void sendMessage(EMMessage message, EMCallBack emCallBack) {
        message.setMessageStatusCallback(emCallBack);
        EMClient.getInstance().chatManager().sendMessage(message);
    }

    /***************************************************
     * 方法描述 ：发生群聊消息
     * 方法名  :  sendGroupMessage
     * params ：  EMMessage message 消息对象
     **************************************************/
    private static void sendGroupMessage(EMMessage message, EMCallBack emCallBack) {
        message.setChatType(EMMessage.ChatType.GroupChat);
        message.setMessageStatusCallback(emCallBack);
        EMClient.getInstance().chatManager().sendMessage(message);
    }

    /***************************************************
     * 方法描述 ：通过注册消息监听来接收消息
     * 方法名  :  addMessageListener
     * params ：listener 监听
     * 说明    ： 记得在不需要的时候移除listener，如在activity的onDestroy()时
     EMClient.getInstance().chatManager().removeMessageListener(msgListener);
     **************************************************/
    public static void addMessageListener(EMMessageListener listener) {
        EMClient.getInstance().chatManager().addMessageListener(listener);
    }

    /***************************************************
     * 方法描述 ：从云端获取前20条获取聊天记录
     * 方法名  :  getConversations
     * params ：String username 用户名
     **************************************************/
    public static List<EMMessage> getConversationsFromServer(String username) {
        EMConversation conversation = EMClient.getInstance().chatManager().getConversation(username);
        //获取此会话的所有消息
        return conversation.getAllMessages();
        //SDK初始化加载的聊天记录为20条，到顶时需要去DB里获取更多
        //获取startMsgId之前的pagesize条消息，此方法获取的messages SDK会自动存入到此会话中，APP中无需再次把获取到的messages添加到会话中
        //List<EMMessage> messages = conversation.loadMoreMsgFromDB(startMsgId, pagesize);
    }

    /***************************************************
     * 方法描述 ：从本地获取聊天记录
     * 方法名  :  getConversations
     * params ：String username 用户名
     **************************************************/
    public static List<EMMessage> getConversationsFromLocal(String username, String startMsgId, int pagesize) {
        EMConversation conversation = EMClient.getInstance().chatManager().getConversation(username);
        return conversation.loadMoreMsgFromDB(startMsgId, pagesize);
    }

    /***************************************************
     * 方法描述 ：获取未读消息数量
     * 方法名  :  getConversations
     * params ：String username 用户名
     **************************************************/
    public static int getUnreadMsgCount(String username) {
        EMConversation conversation = EMClient.getInstance().chatManager().getConversation(username);
        return conversation.getUnreadMsgCount();
    }

    /***************************************************
     * 方法描述 ：指定会话未读消息设为已读
     * 方法名  :  markAllMessagesAsRead
     * params ：String username 用户名
     **************************************************/
    public static void markAllMessagesAsRead(String username) {
        EMConversation conversation = EMClient.getInstance().chatManager().getConversation(username);
        //指定会话消息未读数清零
        conversation.markAllMessagesAsRead();
    }

    /***************************************************
     * 方法描述 ：一条未读消息设为已读
     * 方法名  :  markMessageAsReadById
     * params ：String username 用户名 ,messageId 消息id
     **************************************************/
    public static void markMessageAsReadById(String username, String messageId) {
        EMConversation conversation = EMClient.getInstance().chatManager().getConversation(username);
        //把一条消息置为已读
        conversation.markMessageAsRead(messageId);
    }

    /***************************************************
     * 方法描述 ：所有未读消息数清零
     * 方法名  :  getConversations
     * params ：String username 用户名
     **************************************************/
    public static void markAllConversationsAsRead() {
        EMClient.getInstance().chatManager().markAllConversationsAsRead();//似乎是整个数据库未读消息设为已读
    }

    /***************************************************
     * 方法描述 ：获取本地消息总数
     * 方法名  :  getAllMsgCountInLocal
     * params ：String username 用户名
     **************************************************/
    public static void getAllMsgCountInLocal(String username) {
        EMConversation conversation = EMClient.getInstance().chatManager().getConversation(username);
        conversation.getAllMsgCount();
    }

    /***************************************************
     * 方法描述 ：获取内存中消息总数
     * 方法名  :  getAllMsgCountInCache
     * params ：String username 用户名
     **************************************************/
    public static void getAllMsgCountInCache(String username) {
        EMConversation conversation = EMClient.getInstance().chatManager().getConversation(username);
        //如果只是获取当前在内存的消息数量，调用
        conversation.getAllMessages().size();
    }

    /**
     * 从服务器获取历史消息
     *
     * @param conversationId 会话名称
     * @param type           会话类型
     * @param pageSize       获取的页面大小
     * @param startMsgId     漫游消息的开始消息id，如果为空，从最新的消息向前开始获取
     * @return 返回消息列表和用于继续获取历史消息的Cursor
     */
    public EMCursorResult<EMMessage> fetchHistoryMessages(String conversationId, EMConversation.EMConversationType type, int pageSize, String startMsgId) {
        try {
            return EMClient.getInstance().chatManager().fetchHistoryMessages(conversationId, type, pageSize, startMsgId);
        } catch (HyphenateException e) {
            return null;
        }
    }

    /**
     * 从服务器获取历史消息
     *
     * @param conversationId 会话名称
     * @param type           会话类型
     * @param pageSize       获取的页面大小
     * @param startMsgId     漫游消息的开始消息id，如果为空，从最新的消息向前开始获取
     * @param callBack       返回消息列表和用于继续获取历史消息的Cursor
     *                       环信sdk 在3.3.4版本增加了一个消息漫游接口，即可以从服务器拉取历史消息到本地，方便用户切换设备同步消息（此功能属于增值服务，需要联系商务同事开通）
     */
    public void asyncFetchHistoryMessage(String conversationId, EMConversation.EMConversationType type, int pageSize, String startMsgId, EMValueCallBack<EMCursorResult<EMMessage>> callBack) {
        EMClient.getInstance().chatManager().asyncFetchHistoryMessage(conversationId, type, pageSize, startMsgId, callBack);
    }


    /***************************************************
     * 方法描述 ：撤回信息
     * 方法名  :  recallMessage
     * params ： EMMessage 消息 消息撤回功能可以撤回一定时间内发送出去的消息，目前只能是两分钟，不能修改（此功能属于增值服务，需要联系商务同事开通）
     **************************************************/
    public static void recallMessage(EMMessage message) {
        if (message == null) {
            EMLog.d(TAG, "message 为空");
        } else {
            try {
                EMClient.getInstance().chatManager().recallMessage(message);
            } catch (HyphenateException e) {
                EMLog.d(TAG, "recallMessage,e=" + e.getMessage());
            }
        }
    }

    /***************************************************
     * 方法描述 ：获取所有的会话
     * 方法名  :  recallMessage
     **************************************************/
    public static Map<String, EMConversation> getAllConversations() {
        return EMClient.getInstance().chatManager().getAllConversations();
    }

    /***************************************************
     * 方法描述 ：删除会话及聊天记录
     * 方法名  :  removeConversation
     **************************************************/
    public static void removeConversation(String username) {
        //删除和某个user会话，如果需要保留聊天记录，传false
        EMClient.getInstance().chatManager().deleteConversation(username, true);
    }

    /***************************************************
     * 方法描述 ：删除当前会话的某条聊天记录
     * 方法名  :  removeConversation
     **************************************************/
    public static void removeMessage(String username, String msgId) {
        EMConversation conversation = EMClient.getInstance().chatManager().getConversation(username);
        conversation.removeMessage(msgId);
    }

    /***************************************************
     * 方法描述 ：导入消息到数据库
     * 方法名  :  importMessages2Local
     **************************************************/
    public static void importMessages2Local(List<EMMessage> msgs) {
        EMClient.getInstance().chatManager().importMessages(msgs);
    }

//---------------------------好友管理----------------------------------------------------

    /***************************************************
     * 方法描述 ：获取好友列表
     * 方法名  :  getAllContactsFromServer 获取好友的 username list，开发者需要根据 username 去自己服务器获取好友的详情。
     **************************************************/
    public static void getAllContactsFromServer() {
        try {
            List<String> usernames = EMClient.getInstance().contactManager().getAllContactsFromServer();
        } catch (HyphenateException e) {
            Log.d(TAG, "importMessages2Local: ");
        }
    }

    /***************************************************
     * 方法描述 ：添加好友
     * 方法名  :  addContact
     **************************************************/
    public static void addContact(String toAddUsername, String reason) {
        try {
            EMClient.getInstance().contactManager().addContact(toAddUsername, reason);
        } catch (HyphenateException e) {
            Log.d(TAG, "getAllContactsFromServer: e=" + e.getMessage());

        }
    }

    /***************************************************
     * 方法描述 ：删除好友
     * 方法名  :  deleteContact
     **************************************************/
    public static void deleteContact(String username) {
        try {
            EMClient.getInstance().contactManager().deleteContact(username);
        } catch (HyphenateException e) {
            Log.d(TAG, "deleteContact: e=" + e.getMessage());

        }
    }

    /***************************************************
     * 方法描述 ：删除好友
     * 方法名  :  acceptInvitation
     **************************************************/
    public static void acceptInvitation(String username) {
        try {
            EMClient.getInstance().contactManager().acceptInvitation(username);
        } catch (HyphenateException e) {
            Log.d(TAG, "acceptInvitation:e=" + e.getMessage());
        }
    }

    /***************************************************
     * 方法描述 ：拒绝好友请求
     * 方法名  :  declineInvitation
     **************************************************/
    public static void declineInvitation(String username) {
        try {
            EMClient.getInstance().contactManager().declineInvitation(username);
        } catch (HyphenateException e) {
            Log.d(TAG, "acceptInvitation:e=" + e.getMessage());
        }
    }

    /***************************************************
     * 方法描述 ：监听好友状态事件
     * 方法名  :  declineInvitation   //onContactAgreed 好友请求被同意 ,onContactRefused 收到好友邀请,onContactAdded增加了联系人时回调此方法,onContactDeleted被删除时回调此方法
     **************************************************/
    public static void declineInvitation(EMContactListener listener) {
        EMClient.getInstance().contactManager().setContactListener(listener);
    }
    //-----------------------------------黑名单------------------------------

    /***************************************************
     * 方法描述 ：从服务器获取黑名单列表
     * 方法名  :  getBlackListFromServer
     **************************************************/
    public static void getBlackListFromServer() {
        try {
            EMClient.getInstance().contactManager().getBlackListFromServer();
        } catch (HyphenateException e) {
            e.printStackTrace();
            Log.d(TAG, "getBlackListFromServer: e=" + e.getMessage());
        }
    }

    /***************************************************
     * 方法描述 ：从本地db获取黑名单列表
     * 方法名  :  getBlackListUsernames
     **************************************************/
    public static void getBlackListUsernames() {
        EMClient.getInstance().contactManager().getBlackListUsernames();
    }

    /***************************************************
     * 方法描述 ：把用户加入到黑名单
     * 方法名  :  addUserToBlackList 第二个参数如果为true，则把用户加入到黑名单后双方发消息时对方都收不到；false，则我能给黑名单的中用户发消息，但是对方发给我时我是收不到的
     **************************************************/
    public static void addUserToBlackList(String username) {
        try {
            EMClient.getInstance().contactManager().addUserToBlackList(username, true);
        } catch (HyphenateException e) {
            Log.d(TAG, "addUserToBlackList: e=" + e.getMessage());
        }
    }

    /***************************************************
     * 方法描述 ：获取同一账号在其他端登录的id
     * 方法名  :  getSelfIdsOnOtherPlatform
     **************************************************/
    public static void getSelfIdsOnOtherPlatform() {
        try {
            EMClient.getInstance().contactManager().getSelfIdsOnOtherPlatform();
        } catch (HyphenateException e) {
            Log.d(TAG, "getSelfIdsOnOtherPlatform: e=" + e.getMessage());
        }
    }

    //------------------------------实时通话----------------------------------------------
    //实时通话分为视频通话和音频通话，与普通电话不同，它是基于网络的。实时通话的数据流量
    //实时语音和实时视频的数据流量如下：实时语音：双向 170k bytes/minute,实时视频：双向 2.5M～3M bytes/minute

    /***************************************************
     * 方法描述 ：监听呼入通话
     * 方法名  :
     * params ：Context context, BroadcastReceiver receiver 接收者
     * 说明：receiver中，intent.getStringExtra("from")拨打方username，intent.getStringExtra("type").eques("video")跳转到视频通话页面，否则语音通话界面
     **************************************************/
    public static void registerCallReveriver(Context context, BroadcastReceiver receiver) {
        IntentFilter callFilter = new IntentFilter(EMClient.getInstance().callManager().getIncomingCallBroadcastAction());
        context.registerReceiver(receiver, callFilter);
    }

    /***************************************************
     * 方法描述 ：监听通话状态
     * 方法名  :
     * params ：
     * return ： CONNECTING  正在连接对方 ，CONNECTED 双方已经建立连接， ACCEPTED: 电话接通成功，DISCONNECTED: 电话断了，NETWORK_UNSTABLE: 网络不稳定，默认处理：无通话数据，NETWORK_NORMAL: 网络恢复正常
     **************************************************/
    public static void addCallStateChangeListener(EMCallStateChangeListener listener) {
        EMClient.getInstance().callManager().addCallStateChangeListener(listener);
    }

    /***************************************************
     * 方法描述 ：拨打语音通话
     * 方法名  : makeVoiceCall
     * params ：username 拨打的对象，ext 扩展信息，
     * return ：获取扩展内容 String callExt = EMClient.getInstance().callManager().getCurrentCallSession().getExt();
     **************************************************/
    public static void makeVoiceCall(String username, String ext) {
        try {
            if (TextUtils.isEmpty(ext)) {
                EMClient.getInstance().callManager().makeVoiceCall(username);
            } else {
                //多参数
                EMClient.getInstance().callManager().makeVoiceCall(username, ext);
            }
        } catch (EMServiceNotReadyException e) {
            Log.d(TAG, "makeVoiceCall: e=" + e.getMessage());
        }
    }

    /***************************************************
     * 方法描述 ：拨打视频通话
     * 方法名  : makeVoiceCall
     * params ：username 拨打的对象，ext 扩展信息，
     * return ：获取扩展内容 String callExt = EMClient.getInstance().callManager().getCurrentCallSession().getExt();
     **************************************************/
    public static void makeVideoCall(String username, String ext) throws EMServiceNotReadyException {
        try {
            if (TextUtils.isEmpty(ext)) {
                EMClient.getInstance().callManager().makeVideoCall(username);
            } else {
                //多参数
                EMClient.getInstance().callManager().makeVideoCall(username, ext);
            }
        } catch (EMServiceNotReadyException e) {
            Log.d(TAG, "makeVoiceCall: e=" + e.getMessage());
        }
    }

    /***************************************************
     * 方法描述 ：接听通话
     * 方法名  : answerCall
     **************************************************/
    public static void answerCall() {
        try {
            EMClient.getInstance().callManager().answerCall();
        } catch (EMNoActiveCallException e) {
            Log.d(TAG, "answerCall: e=" + e.getMessage());
        }
    }

    /***************************************************
     * 方法描述 ：拒绝接听
     * 方法名  : rejectCall
     **************************************************/
    public static void rejectCall() {
        try {
            EMClient.getInstance().callManager().rejectCall();
        } catch (EMNoActiveCallException e) {
            // TODO Auto-generated catch block
            Log.d(TAG, "answerCall: e=" + e.getMessage());
        }
    }

    /***************************************************
     * 方法描述 ：挂断通话
     * 方法名  : rejectCall
     **************************************************/
    public static void endCall() {
        try {
            EMClient.getInstance().callManager().endCall();
        } catch (EMNoActiveCallException e) {
            Log.d(TAG, "answerCall: e=" + e.getMessage());
        }
    }
    //--------------暂停和恢复语音或视频数据传输-----------------------

    /***************************************************
     * 方法描述 ：语音操作
     * 方法名  : voiceTransfer
     * 调用后对方会收到相应VOICE_PAUSE、VOICE_RESUME callstate的变动通知。
     **************************************************/
    public static void voiceTransfer(boolean isResume) {
        try {
            if (isResume) {
                //恢复语音数据传输：
                EMClient.getInstance().callManager().resumeVoiceTransfer();
            } else {
                //暂停语音数据传输：
                EMClient.getInstance().callManager().pauseVoiceTransfer();
            }
        } catch (HyphenateException e) {
            Log.d(TAG, "voiceTransfer: e=" + e.getMessage());
        }
    }

    /***************************************************
     * 方法描述 ：视频操作
     * 方法名  : videoTransfer
     * 调用后对方会收到相应VVIDEO_PAUSE、VIDEO_RESUME的callstate的变动通知。
     **************************************************/
    public static void videoTransfer(boolean isResume) {
        try {
            if (isResume) {
                //恢复视频数据传输：
                EMClient.getInstance().callManager().resumeVideoTransfer();
            } else {
                //暂停视频数据传输：
                EMClient.getInstance().callManager().pauseVideoTransfer();
            }
        } catch (HyphenateException e) {
            Log.d(TAG, "voiceTransfer: e=" + e.getMessage());
        }
    }

    /***************************************************
     * 方法描述 ：视频操作
     * 方法名  : setSurfaceView
     * 视频通话需要预先设置 localSurfaceView、oppositeSurfaceView，
     * 而且必须在Activity.onCreate(Context context)方法中设置，之后才能正确捕捉 Surface 的变化。
     **************************************************/
    public static void setSurfaceView(EMCallSurfaceView localSurface, EMCallSurfaceView oppositeSurface) {
        EMClient.getInstance().callManager().setSurfaceView(localSurface, oppositeSurface);
    }

    /***************************************************
     * 方法描述 ：视频操作
     * 方法名  : setSurfaceView
     * 视频通话需要预先设置 localSurfaceView、oppositeSurfaceView，
     * 而且必须在Activity.onCreate(Context context)方法中设置，之后才能正确捕捉 Surface 的变化。
     * @param data
     * @param width
     * @param height
     * @param rotate */
    public static void showLocalVedioData(byte[] data, int width, int height, int rotate) {
        // 首先在初始化音视频部分设置外部输入视频数据
        // 设置是否启用外部输入视频数据，默认 false，如果设置为 true，需要自己调用
        // {@link EMCallManager#inputExternalVideoData(byte[], int, int, int)}输入视频数据
        EMClient.getInstance().callManager().getCallOptions().setEnableExternalVideoData(true);
        //然后就是自己获取视频数据，进行美颜等处理，循环调用以下方法输入数据就行了（这个调用频率就相当于你的帧率，调用间隔可以自己进行控制，一般最大30帧/秒）
        // 视频数据的格式是摄像头采集的格式即：NV21 420sp 自己手动传入时需要将自己处理的数据转为 yuv 格式输入

        EMClient.getInstance().callManager().inputExternalVideoData(data, width, height, rotate);

    }

    /***************************************************
     * 方法描述 ：切换摄像头
     * 方法名  : switchCamera
     * *************************************************/
    public static void switchCamera() {
        EMClient.getInstance().callManager().switchCamera();

    }

    /***************************************************
     * 方法描述 ：初始化相机参数，可以不设，使用默认参数
     * 方法名  : initVedio
     * @param frameRate
     * @param width
     * @param height
     * @param maxVideoKbps
     * @param minVideoKbps   *************************************************/
    public static void initVedio(int frameRate, int width, int height, long maxVideoKbps, int minVideoKbps) {

        //设置通话最大帧率，SDK 最大支持(30)，默认(20)
        EMClient.getInstance().callManager().getCallOptions().setMaxVideoFrameRate(frameRate);
        //设置视频通话分辨率 默认是(640, 480)
        EMClient.getInstance().callManager().getCallOptions().setVideoResolution(width, height);
        //设置视频通话最大和最小比特率（可以不设置，SDK会根据手机分辨率和网络情况自动适配），最大值默认800， 最小值默认80
        EMClient.getInstance().callManager().getCallOptions().setMaxVideoKbps(maxVideoKbps < minVideoKbps ? minVideoKbps : maxVideoKbps > 800 ? 800 : maxVideoKbps);
        EMClient.getInstance().callManager().getCallOptions().setMinVideoKbps(minVideoKbps < 80 ? 80 : minVideoKbps);

    }

    /***************************************************
     * 方法描述 ：音视频采样率
     * 方法名  : setAudioSampleRate
     * *************************************************/
    public static void setAudioSampleRate(int sampleRate) {
        //设置音视频采样率，一般不需要设置，除非采集声音有问题才需要手动设置(默认值48000)，根据自己硬件设备确定
        EMClient.getInstance().callManager().getCallOptions().setAudioSampleRate(sampleRate == 0 ? 48000 : sampleRate);

    }

    /***************************************************
     * 方法描述 ：视频截图
     * 方法名  : startVideoRecord
     * *************************************************/
    public static void takePicture(String filepath) {
        EMVideoCallHelper callHelper = EMClient.getInstance().callManager().getVideoCallHelper();
//        callHelper.takePicture(filepath);

    }

    /***************************************************
     * 方法描述 ：录制视频
     * 方法名  : startVideoRecord
     * *************************************************/
    public static void startVideoRecord(String dirPath) {
        EMVideoCallHelper callHelper = EMClient.getInstance().callManager().getVideoCallHelper();
//        callHelper.startVideoRecord(dirPath);

    }

    /***************************************************
     * 方法描述 ：停止录制视频
     * 方法名  : stopVideoRecord
     * *************************************************/
    public static void stopVideoRecord() {
        EMVideoCallHelper callHelper = EMClient.getInstance().callManager().getVideoCallHelper();
//        callHelper.stopVideoRecord();

    }  /***************************************************
     * 方法描述 ：设置用户名
     * 方法名  : updateCurrentUserNick
     * *************************************************/
    public static void updateCurrentUserNick(String nickname) {
        //此方法传入一个字符串String类型的参数，返回成功或失败的一个Boolean类型的返回值
        EMClient.getInstance().updateCurrentUserNick(nickname);

    }


}
