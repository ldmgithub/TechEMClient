package com.emclien.emclient;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.emclien.manager.EMClientUtils;
import com.emclien.utils.EaseCommonUtils;
import com.hyphenate.EMCallBack;
import com.hyphenate.EMError;
import com.hyphenate.chat.EMClient;
import com.hyphenate.exceptions.EMServiceNotReadyException;
import com.hyphenate.exceptions.HyphenateException;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {
    
    EditText tvName;
    EditText tvPwd;
    private static final String TAG = "LoginActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        tvName = findViewById(R.id.tv_lname);
        tvPwd = findViewById(R.id.tv_pwd);
        findViewById(R.id.btn_login).setOnClickListener(this);
        findViewById(R.id.btn_regist).setOnClickListener(this);
        findViewById(R.id.btn_voice_call).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.btn_regist:
                register();
                break;
            case R.id.btn_login:
                login();
                break;
            case R.id.btn_voice_call:
                voiceCall();
                break;
            default:

                break;
        }
    }

    private void voiceCall() {
        try {
            EMClient.getInstance().callManager().makeVoiceCall("18098926897", "");
        } catch (EMServiceNotReadyException e) {
            Log.d(TAG, "makeVoiceCall: e=" + e.getMessage());
        }
    }

    private void login() {
        final String name = tvName.getText().toString();
        String pwd = tvPwd.getText().toString();
        if (!checkData(name, pwd)) return;
        EMClientUtils.login(name, pwd, new EMCallBack() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "login: onSuccess");
                // ** manually load all local groups and conversation
                EMClient.getInstance().groupManager().loadAllGroups();
                EMClient.getInstance().chatManager().loadAllConversations();
                EMClient.getInstance().updateCurrentUserNick(name);
//                // 获取华为 HMS 推送 token
//                HMSPushHelper.getInstance().getHMSPushToken();
//
//                // get user's info (this should be get from App's server or 3rd party service)
//                DemoHelper.getInstance().getUserProfileManager().asyncGetCurrentUserInfo();

//                Intent intent = new Intent(LoginActivity.this,
//                        MainActivity.class);
//                startActivity(intent);
//                finish();
            }

            @Override
            public void onError(int code, final String message) {
                Log.d(TAG, "onError: message=" + message);

                runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(getApplicationContext(), message,
                                Toast.LENGTH_SHORT).show();
                        Toast.makeText(LoginActivity.this, "登录失败", Toast.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void onProgress(int progress, String status) {
                Log.d(TAG, "login: onProgress,progress=" + progress + ",statues=" + status);
            }
        });
    }

    private boolean checkData(String name, String pwd) {
        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(pwd)) {
            Toast.makeText(this, "用户名或者密码格式不正确", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!EaseCommonUtils.isNetWorkConnected(this)) {
            Toast.makeText(this, R.string.network_isnot_available, Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
    
    private void register() {
        final String name = tvName.getText().toString();
        final String pwd = tvPwd.getText().toString();
        if (!checkData(name, pwd)) return;
        new Thread(new Runnable() {
            public void run() {
                try {
                    // call method in SDK
                    EMClientUtils.register(name, pwd);
                    runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(getApplicationContext(), getResources().getString(R.string.Registered_successfully), Toast.LENGTH_SHORT).show();
                        }
                    });
                } catch (final HyphenateException e) {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            int errorCode = e.getErrorCode();
                            if (errorCode == EMError.NETWORK_ERROR) {
                                Toast.makeText(getApplicationContext(), getResources().getString(R.string.network_anomalies), Toast.LENGTH_SHORT).show();
                            } else if (errorCode == EMError.USER_ALREADY_EXIST) {
                                Toast.makeText(getApplicationContext(), getResources().getString(R.string.User_already_exists), Toast.LENGTH_SHORT).show();
                            } else if (errorCode == EMError.USER_AUTHENTICATION_FAILED) {
                                Toast.makeText(getApplicationContext(), getResources().getString(R.string.registration_failed_without_permission), Toast.LENGTH_SHORT).show();
                            } else if (errorCode == EMError.USER_ILLEGAL_ARGUMENT) {
                                Toast.makeText(getApplicationContext(), getResources().getString(R.string.illegal_user_name), Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getApplicationContext(), getResources().getString(R.string.Registration_failed), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        }).start();

    }
}
