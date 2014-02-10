package org.universAAL.android.activities;

import org.universAAL.android.R;
import org.universAAL.android.container.AndroidContainer;
import org.universAAL.android.container.AndroidContext;
import org.universAAL.android.handler.AndroidHandler;
import org.universAAL.android.services.MiddlewareService;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;

public class HandlerActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.handler);
	}

	@Override
	protected void onResume() {
		super.onResume();
		AndroidHandler handler = (AndroidHandler) AndroidContainer.THE_CONTAINER
				.fetchSharedObject(AndroidContext.THE_CONTEXT,
						new Object[] { AndroidHandler.class.getName() });
		if (handler != null) {
			handler.setViewAndActivity(this);
			handler.render();
		}
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		AndroidHandler handler = (AndroidHandler) AndroidContainer.THE_CONTAINER
				.fetchSharedObject(AndroidContext.THE_CONTEXT,
						new Object[] { AndroidHandler.class.getName() });
		if (handler != null) {
			handler.setViewAndActivity(null);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.handler, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_start:
			Intent startServiceIntent = new Intent(this, MiddlewareService.class);
			this.startService(startServiceIntent);
			return true;
		case R.id.action_stop:
			Intent stopServiceIntent = new Intent(this, MiddlewareService.class);
			this.stopService(stopServiceIntent);
			return true;
		case R.id.action_settings:
			// TODO Show settings
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

}