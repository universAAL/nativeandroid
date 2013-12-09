package org.universAAL.android.activities;

import org.universAAL.android.R;
import org.universAAL.android.services.MiddlewareService;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;

public class TestActivity extends Activity implements OnTouchListener {
	private static String TAG = "TestActivity";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.v(TAG, "Create");
		setContentView(R.layout.main);
		addTouchListeners();
		Log.v(TAG, "Created");
	};

	private void addTouchListeners() {
		Button button = (Button) findViewById(R.id.startMiddleware);
		button.setOnTouchListener(this);
		Button button2 = (Button) findViewById(R.id.stopMiddleware);
		button2.setOnTouchListener(this);
	}

	public boolean onTouch(View v, MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_UP) {
			switch (v.getId()) {
			case R.id.startMiddleware:
				Intent startServiceIntent = new Intent(this, MiddlewareService.class);
				this.startService(startServiceIntent);
				break;
			case R.id.stopMiddleware:
				Intent stopServiceIntent = new Intent(this, MiddlewareService.class);
				this.stopService(stopServiceIntent);
				break;
			default:
				break;
			}
		}
		return false;
	}

}
