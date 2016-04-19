package com.handler;

import java.io.InputStream;
import java.lang.ref.WeakReference;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.widget.ImageView;

/**
 * 使用Handler造成内存泄露的分析和解决
 * 可参考blog文章：http://blog.csdn.net/u012440207/article/details/51195064
 * @author zhongyao
 * 
 */
public class MainActivity extends ActionBarActivity {

	private ImageView iv;
	private final static int SUCCESS = 1;
	private String imgUrl = "http://m3.biz.itc.cn/pic/new/n/54/56/Img5435654_n.jpg";

	static class MyHandler extends Handler {
		private final WeakReference<Activity> mActivityReference;

		MyHandler(Activity activity) {
			mActivityReference = new WeakReference<Activity>(activity);
		}

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			final MainActivity activity = (MainActivity) mActivityReference.get();
			if (activity != null) {
				switch (msg.what) {
				case SUCCESS:
					Bitmap bitmap = (Bitmap) msg.obj;
					activity.iv.setImageBitmap(bitmap);
					break;

				default:
					break;
				}
			}
		}
	}

	private final MyHandler handler = new MyHandler(this);

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		iv = (ImageView) findViewById(R.id.iv);

		new Thread(new Runnable() {

			@Override
			public void run() {
				doTask();
			}
		}).start();

	}

	protected void doTask() {
		try {
			HttpGet get = new HttpGet(imgUrl);
			HttpClient client = new DefaultHttpClient();
			HttpResponse response = client.execute(get);
			if (response.getStatusLine().getStatusCode() == 200) {
				InputStream inputStream = response.getEntity().getContent();
				if (inputStream != null) {
					final Bitmap bitmap = BitmapFactory
							.decodeStream(inputStream);

					Message msg = new Message();
					msg.obj = bitmap;
					msg.what = SUCCESS;
					handler.sendMessage(msg);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
