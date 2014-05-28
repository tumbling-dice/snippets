package com.example.servicetest;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.view.View;
import android.widget.Toast;

import com.example.servicetest.data.TestData;
import com.example.servicetest.service.TestService;

public class MainActivity extends Activity implements ServiceConnection{

	private Messenger _messenger;
	private Messenger _replyMessanger;

	public static final int M_OK = 2;

	static class ReplyHandler extends Handler {

		private Context _cont;

		public ReplyHandler(Context cont) {
			_cont = cont;
		}

		@Override
		public void handleMessage(Message msg) {
			switch(msg.what) {
				case M_OK:
					Toast.makeText(_cont, (String)msg.obj, Toast.LENGTH_SHORT).show();
					break;
			}
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	}

	@Override
	public void onServiceConnected(ComponentName name, IBinder service) {
		Toast.makeText(getApplicationContext(), "サービスに接続しました", Toast.LENGTH_SHORT).show();
		_messenger = new Messenger(service);
		_replyMessanger = new Messenger(new ReplyHandler(getApplicationContext()));
	}

	@Override
	public void onServiceDisconnected(ComponentName name) {
		Toast.makeText(getApplicationContext(), "サービスから切断しました", Toast.LENGTH_SHORT).show();
		_messenger = null;
		_replyMessanger = null;
	}

	/**
	 * サービスへの接続
	 * @param v
	 */
	public void connect(View v) {
		bindService(new Intent("com.example.servicetest.service.TestService")
			, this, Context.BIND_AUTO_CREATE);
		findViewById(R.id.btnSend).setEnabled(true);
		findViewById(R.id.btnDisconnect).setEnabled(true);
		v.setEnabled(false);
	}

	/**
	 * メッセージの送信
	 * @param v
	 */
	public void send(View v) {
		try {

			// メッセージの送信
			//_messenger.send(Message.obtain());

			// メッセージとデータの送信
			//_messenger.send(Message.obtain(null, TestService.M_SEND_STRING, "hoge"));

			// メッセージにBundleを付与して送信
			TestData data = new TestData();
			data.setName("piyo");
			data.setValue("huga");
			Bundle arg = new Bundle();
			arg.putSerializable("testData", data);
			Message msg = Message.obtain(null, TestService.M_SEND_BUNDLE);
			msg.setData(arg);

			// コールバックを設定
			//msg.replyTo = _replyMessanger;

			_messenger.send(msg);

		} catch (RemoteException e) {
			e.printStackTrace();
			Toast.makeText(getApplicationContext(), "メッセージの送信に失敗しました。", Toast.LENGTH_SHORT).show();
		}
	}

	/**
	 * サービスからの切断
	 * @param v
	 */
	public void disconnect(View v) {
		unbindService(this);
		_messenger = null;
		_replyMessanger = null;
		findViewById(R.id.btnConnect).setEnabled(true);
		findViewById(R.id.btnSend).setEnabled(false);
		v.setEnabled(false);
	}

}
