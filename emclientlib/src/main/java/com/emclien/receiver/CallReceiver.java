/**
 * Copyright (C) 2016 Hyphenate Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.emclien.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.emclien.bean.CallEvent;
import com.emclien.config.Constant;
import com.hyphenate.chat.EMClient;
import com.hyphenate.util.EMLog;

import rxbus.ecaray.com.rxbuslib.rxbus.RxBus;

public class CallReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
//		if(!DemoHelper.getInstance().isLoggedIn())
//		    return;
		//username
		String from = intent.getStringExtra("from");
		//call type
		String type = intent.getStringExtra("type");
		String ext = EMClient.getInstance().callManager().getCurrentCallSession().getExt();
		if("video".equals(type)){ //video call
			RxBus.getDefault().post(new CallEvent(from,true,ext), Constant.RXBUS_TAG_VIDIO_CALL);
		}else{ //voice call
            RxBus.getDefault().post(new CallEvent(from,true,ext), Constant.RXBUS_TAG_VIOCE_CALL);
        }
		EMLog.d("CallReceiver", "app received a incoming call");
	}

}
