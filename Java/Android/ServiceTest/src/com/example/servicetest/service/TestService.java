package com.example.servicetest.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.widget.Toast;

import com.example.servicetest.data.TestData;

public class TestService extends Service {

	public static final int M_SEND_STRING = 0;
	public static final int M_SEND_BUNDLE = 1;
	public static final int M_OK = 2;

	private Messenger _messenger;

	static class TestHandler extends Handler {

		private Context _cont;

		public TestHandler(Context cont) {
			_cont = cont;
		}

		@Override
		public void handleMessage(Message msg) {
			switch(msg.what) {
				case M_SEND_STRING:
					Toast.makeText(_cont, (String)msg.obj, Toast.LENGTH_SHORT).show();
					break;
				case M_SEND_BUNDLE:
					Bundle arg = msg.getData();
					TestData data = (TestData) arg.getSerializable("testData");
					Toast.makeText(_cont, data.toString(), Toast.LENGTH_SHORT).show();
					break;
				default:
					Toast.makeText(_cont, "Messageを受信しました", Toast.LENGTH_SHORT).show();
					super.handleMessage(msg);
			}

			Messenger reply = msg.replyTo;
			if(reply != null) {
				try {
					reply.send(Message.obtain(null, M_OK, "受信が終わりました"));
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			}
		}

	}

	@Override
	public void onCreate() {
		super.onCreate();
		_messenger = new Messenger(new TestHandler(getApplicationContext()));
	}

	@Override
	public IBinder onBind(Intent i) {
		Toast.makeText(getApplicationContext(), "Bindしました", Toast.LENGTH_SHORT).show();
		return _messenger.getBinder();
	}





}
