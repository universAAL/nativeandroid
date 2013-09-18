/*
	Copyright 2011-2012 TSB, http://www.tsbtecnologias.es
	TSB - Tecnologías para la Salud y el Bienestar
	
	See the NOTICE file distributed with this work for additional 
	information regarding copyright ownership
	
	Licensed under the Apache License, Version 2.0 (the "License");
	you may not use this file except in compliance with the License.
	You may obtain a copy of the License at
	
	  http://www.apache.org/licenses/LICENSE-2.0
	
	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	See the License for the specific language governing permissions and
	limitations under the License.
 */
package org.universaal.nativeandroid.aalfficiencyclient;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;

import org.universaal.nativeandroid.aalfficiencyclient.utils.Constants;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.android.AsyncFacebookRunner;
import com.facebook.android.BaseRequestListener;
import com.facebook.android.DialogError;
import com.facebook.android.Facebook;
import com.facebook.android.Facebook.DialogListener;
import com.facebook.android.FacebookError;
import com.facebook.android.SessionStore;

public class ActivityScoreActivity extends Activity{
	
	Facebook facebook = new Facebook(Constants.APP_ID);
	private ProgressDialog mProgress;
	private Handler mRunOnUi = new Handler();	
	private SharedPreferences mPrefs;
	private TextView points, todayScore;

	private final static String TAG 	= AALfficiencyClientActivity.class.getCanonicalName();
	private final static String prefix 	= AALfficiencyClientActivity.class.getPackage().getName();
	
	private BroadcastReceiver registeredReceiver = null;
	private BroadcastReceiver registeredReceiver2 = null;
	private ProgressDialog dialog;
	private String description;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		dialog = ProgressDialog.show(getParent(),getString(R.string.loading),getString( R.string.wait));   
        setContentView(R.layout.activityscore);
        todayScore = (TextView)findViewById(R.id.totalTodayA);
        points = (TextView)findViewById(R.id.totalScoreA);
        
        getScore();
        
        ImageButton publish = (ImageButton)findViewById(R.id.publishActivityFB);
        
       publish.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				postToFacebook();
				
			}
       });
       
       Button c = (Button)findViewById(R.id.challengeA);
       
       c.setOnClickListener(new OnClickListener() {
       	
			public void onClick(View v) {
				
				Intent challengeIntent = new Intent(ActivityScoreActivity.this, Challenge.class);
		     	challengeIntent.putExtra("challenge", description);
		     	ActivityScoreActivity.this.startActivity(challengeIntent);
				
			}
       });
       
	}
	
	
	private void postToFacebook() {
        mProgress  = new ProgressDialog(this);
        SessionStore.restore(facebook, this);
      
        mPrefs = getApplicationContext().getSharedPreferences(Constants.Prefs,MODE_PRIVATE);
        String access_token = mPrefs.getString(Constants.token, null);
        long expires = mPrefs.getLong(Constants.expiration, 0);
        facebook.setAccessToken(access_token);
        facebook.setAccessExpires(expires);

        if (facebook.isSessionValid()){
       	 mProgress.setMessage(getString(R.string.posting));
       	 mProgress.show();
 
       	 AsyncFacebookRunner mAsyncFbRunner = new AsyncFacebookRunner(facebook);
 
       	 Bundle params = new Bundle();
 
       	 String message = getString(R.string.activityPublish)+" 1003 "+ getString(R.string.steps)+" "+
       			 getString(R.string.activityPublish2)+" 300 "+ getString(R.string.calories)+". "
       			 + getString(R.string.activityPublishWon)+" 12 " + getString(R.string.points)+
       			 ". "+getString(R.string.activityTotalPublish)+" "+points.getText()
       			 +" "+getString(R.string.points);
        
       	 params.putString("message", message);
       	 params.putString("name", "AALfficiency");
       	 params.putString("caption", "universaal.org"); 
       	 params.putString("link", "http://universaal.org"); 
       	 params.putString("description", getString(R.string.desc));
 
       	 mAsyncFbRunner.request("me/feed", params, "POST", new WallPostListener(),null);
        }
        else{
       	 facebook.authorize(this, new String[] {"email", "publish_checkins", "publish_actions"}, new DialogListener() {
       		 	public void onComplete(Bundle values) {
                    SharedPreferences.Editor editor = mPrefs.edit();
                    editor.putString(Constants.token, facebook.getAccessToken());
                    editor.putLong(Constants.expiration, facebook.getAccessExpires());
                    editor.commit();
                } 
                public void onFacebookError(FacebookError error) {}
                public void onError(DialogError e) {}
                public void onCancel() {}
		
            });
       	 
       	 	mProgress.setMessage(getString(R.string.posting));
            mProgress.show();
     
            AsyncFacebookRunner mAsyncFbRunner = new AsyncFacebookRunner(facebook);
     
            Bundle params = new Bundle();
     
            String message = getString(R.string.publishFB)+" 320"+ getString(R.string.points);
            
            params.putString("message", message);
            params.putString("name", "AALfficiency");
            params.putString("caption", "universaal.org");
            params.putString("link", "http://universaal.org");
            params.putString("description", getString(R.string.desc));
     
            mAsyncFbRunner.request("me/feed", params, "POST", new WallPostListener(),null);
            Log.d("DESPUES", "lololo");
       	 
        }
    }
 
    private final class WallPostListener extends BaseRequestListener {
		public void onComplete(final String response, Object state) {
			mRunOnUi.post(new Runnable() {
                public void run() {
                    mProgress.cancel();
                    Log.d("RESPONSE", response);
                    Toast.makeText(ActivityScoreActivity.this, getString(R.string.postingSuccess), Toast.LENGTH_SHORT).show();
                }
            });
			
		}
		public void onIOException(IOException e, Object state) {	
		}

		public void onFileNotFoundException(FileNotFoundException e,
				Object state) {
			// TODO Auto-generated method stub
			
		}

		public void onMalformedURLException(MalformedURLException e,
				Object state) {
			// TODO Auto-generated method stub
			
		}

		public void onFacebookError(FacebookError e, Object state) {
			mRunOnUi.post(new Runnable() {
               public void run() {
                   mProgress.cancel();

                   Toast.makeText(ActivityScoreActivity.this, "An error ocurred trying to post in Facebook.", Toast.LENGTH_SHORT).show();
               }
           });
			
			
		}

	
    }
    

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
    	super.onActivityResult(requestCode, resultCode, data);
    	facebook.authorizeCallback(requestCode, resultCode, data);
    }
	
    private void getScore(){
    	
// Unregister the previous registration (if exists)
		if (null != registeredReceiver) {
			unregisterReceiver(registeredReceiver);
			registeredReceiver = null;
		}
		
		// Register for receiver that will wait for the response
		BroadcastReceiver receiver = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, final Intent intent) {
				Log.d(TAG, "Got response on Scores request");
				//unregisterReceiver(this);
				
				runOnUiThread(
						new Thread() {
							
							public void run() {
								analyzeGetScoreResponse(intent);
							}
						}
				);
			}

		};
		
		// Action name for the reply
		String actionNameForReply = receiver.getClass().getName();
		
		// Category for the reply
		String category = Intent.CATEGORY_DEFAULT;
		
		// Add a filter to the receiver
		IntentFilter filter = new IntentFilter("org.universaal.nativeandroid.aalfficiencyclient.ActivityScoreActivity.getScore()");
		filter.addCategory(Intent.CATEGORY_DEFAULT);
		registerReceiver(receiver, filter);
		
		// Create the intent that will be sent as a broadcast message
		Intent intent = new Intent(prefix + ".GET_ACTIVITY_SCORE");
		intent.putExtra(Constants.replyToActionArg, "org.universaal.nativeandroid.aalfficiencyclient.ActivityScoreActivity.getScore()");
		intent.putExtra(Constants.replyToCategoryArg, Intent.CATEGORY_DEFAULT);
		
		invokeIntent(intent);    
	}

   private void analyzeGetScoreResponse(Intent intent) {
    		
	   	Bundle scoresBundle = intent.getBundleExtra("activityscores");
	   	String score = scoresBundle.getString("totalActivityScore");
	   	String today = scoresBundle.getString("todayActivityScore");
	   	String steps = scoresBundle.getString("steps");
	   	String kcal = scoresBundle.getString("kcal");

	   	String message = getString(R.string.activityToday)+" "+steps+" "+ getString(R.string.steps)+" "+
     			 getString(R.string.activityCalories)+" "+kcal+" "+ getString(R.string.calories)+". "
     			 + getString(R.string.activityWon)+" "+today+" " + getString(R.string.points);
       todayScore.setText(message);
       
       points.setText(score);     
	   getChallengeInfo();
   }

   
   private void getChallengeInfo(){
   	// Unregister the previous registration (if exists)
		if (null != registeredReceiver2) {
			unregisterReceiver(registeredReceiver2);
			registeredReceiver2 = null;
		}
		
		// Register for receiver that will wait for the response
		BroadcastReceiver receiver = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, final Intent intent) {
				Log.d(TAG, "Got response on Challenge request");
				//unregisterReceiver(this);
				
				runOnUiThread(
						new Thread() {
							
							public void run() {
								analyzeGetChallengeInfoResponse(intent);
							}
						}
				);
			}

		};
		
		// Action name for the reply
		String actionNameForReply = receiver.getClass().getName();
		
		// Category for the reply
		String category = Intent.CATEGORY_DEFAULT;
		
		// Add a filter to the receiver
		IntentFilter filter = new IntentFilter("org.universaal.nativeandroid.aalfficiencyclient.ActivityScoreActivity.getChallenge()");
		filter.addCategory(Intent.CATEGORY_DEFAULT);
		registerReceiver2(receiver, filter);
		
		// Create the intent that will be sent as a broadcast message
		Intent intent = new Intent(prefix + ".GET_ACTIVITY_CHALLENGE");
		intent.putExtra(Constants.replyToActionArg, "org.universaal.nativeandroid.aalfficiencyclient.ActivityScoreActivity.getChallenge()");
		intent.putExtra(Constants.replyToCategoryArg, Intent.CATEGORY_DEFAULT);

		invokeIntent(intent);  
   }
   
   private void analyzeGetChallengeInfoResponse(Intent intent) {
  		
	   	Bundle challengeBundle = intent.getBundleExtra("activitychallenge");
	   	this.description = challengeBundle.getString("Description");
	   	dialog.dismiss();
   }
   
   @SuppressLint("ParserError")
	private void invokeIntent(Intent intent) {
    		sendBroadcast(intent);
    	}
    	
   public Intent registerReceiver(BroadcastReceiver receiver, IntentFilter filter) {
		Intent registeredIntent = super.registerReceiver(receiver, filter);
		
		// Keep the register
		registeredReceiver = receiver;
		
		return registeredIntent;
	}
   public Intent registerReceiver2(BroadcastReceiver receiver, IntentFilter filter) {
		Intent registeredIntent = super.registerReceiver(receiver, filter);
		
		// Keep the register
		registeredReceiver2 = receiver;
		
		return registeredIntent;
	}
   
    
}
