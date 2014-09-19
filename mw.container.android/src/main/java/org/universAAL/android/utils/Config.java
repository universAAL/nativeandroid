package org.universAAL.android.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Dictionary;
import java.util.Properties;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * This class holds a centralized way to access settings from everywhere in the
 * app. It is done this way for a couple of reasons: Settings are stored in
 * Preferences, which cannot be accessed from those parts of the app that do not
 * have access to an Android Context. Also, certain configurations have to be
 * accessed from multiple points in the app, so it is better to have them
 * centralized here rather than have them in one class and let others access it
 * (like before when most were in MiddlewareService). Finally, it may improve
 * slightly access to Preferences, since getDefaultSharedPreferences accesses to
 * a file, and the SharedPreferences instance is unique. <br>
 * Of course this also means that, working as a singleton and dealing with
 * settings that can change at any time, there has to be some kind of
 * synchronization - a moment when the in-memory settings of this class are
 * updated to those in the Preferences file. The method load(Context ctxt) is
 * used for this, which is more manageable than using listeners.
 * 
 * @author alfiva
 * 
 */
public class Config {
	private static String mServerURL = "http://158.42.167.41:8181/universaal";
	private static String mServerUSR = "yo";
	private static String mServerPWD = "ual";
	private static String mConfigFolder = "/data/felix/configurations/etc/";
	private static String mUAALUser = "saied";
	private static int mSettingRemoteType = IntentConstants.REMOTE_TYPE_GW;
	private static int mSettingRemoteMode = IntentConstants.REMOTE_MODE_WIFIOFF;
	private static boolean mSettingWifiEnabled = true;
	private static boolean mServiceCoord = true;

	/**
	 * Synchronize the Config utility class with the actual saved preferences.
	 * This must be called when initializing or after a change has happened in
	 * the Preferences. Since changes in Preferences are most relevant during MW
	 * initialization, calling this method should match restarts of the MW
	 * service too.
	 * 
	 * @param ctxt
	 *            Android Context to have access to the Preferences
	 */
	public static void load(Context ctxt) {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(ctxt);
		mServerURL = prefs.getString("setting_connurl_key",
				"http://158.42.167.41:8181/universaal");
		mServerUSR = prefs.getString("setting_connusr_key", "yo");
		mServerPWD = prefs.getString("setting_connpwd_key", "ual");
		mServiceCoord = prefs.getBoolean("setting_iscoord_key", true);
		mSettingWifiEnabled = prefs.getBoolean("setting_connwifi_key", true);
		mSettingRemoteMode = Integer.parseInt(prefs.getString(
				"setting_connmode_key", "1"));
		mSettingRemoteType = Integer.parseInt(prefs.getString(
				"setting_conntype_key", "0"));
		mConfigFolder = prefs.getString("setting_cfolder_key",
				"/data/felix/configurations/etc/");
		mUAALUser = prefs.getString("setting_user_key", "saied");
	}

	/**
	 * Get the URL/IP of the server to establish the remote connection
	 * 
	 * @return URL of the server
	 */
	public static String getServerURL() {
		return mServerURL;
	}

	/**
	 * Get the identification of this local node for the authentication of
	 * remote connection when using Remote API
	 * 
	 * @return the id to use in the user authentication
	 */
	public static String getServerUSR() {
		return mServerUSR;
	}

	/**
	 * Get the password for the authentication of remote connection when using
	 * Remote API
	 * 
	 * @return the password
	 */
	public static String getServerPWD() {
		return mServerPWD;
	}

	/**
	 * Check if this instance of the MW should run as Bus Service Coordinator
	 * 
	 * @return true if it is set as coordinator
	 */
	public static boolean isServiceCoord() {
		return mServiceCoord;
	}

	/**
	 * Get the remote connection mode
	 * 
	 * @return One of the REMOTE_MODE_* constants
	 */
	public static int getRemoteMode() {
		return mSettingRemoteMode;
	}

	/**
	 * Get the remote connection type
	 * 
	 * @return One of the REMOTE_TYPE_* constants
	 */
	public static int getRemoteType() {
		return mSettingRemoteType;
	}

	/**
	 * Check if WiFi discovery has been enabled
	 * 
	 * @return true if it is enabled
	 */
	public static boolean isWifiAllowed() {
		return mSettingWifiEnabled;
	}

	/**
	 * Get the folder where the configuration files for uAAL modules are placed.
	 * 
	 * @return path to the configuration folder
	 */
	public static String getConfigDir() {
		return mConfigFolder;
	}

	/**
	 * Get the suffix to be used to create the User URI to be used in UI
	 * framework, which represents the User resource handling uAAL.
	 * 
	 * @return the user id suffix of the User URI
	 */
	public static String getUAALUser() {
		return mUAALUser;
	}

	/**
	 * Gets the properties of a property file located in the config folder.
	 * 
	 * @param file
	 *            The name of the file (without path nor extension).
	 * @return The Properties
	 */
	public static Dictionary getProperties(String file) {
		Properties prop = new Properties();
		try {
			File conf = new File(Environment.getExternalStorageDirectory()
					.getPath(), getConfigDir() + file + ".properties");
			InputStream in = new FileInputStream(conf);
			prop.load(in);
			in.close();
		} catch (java.io.FileNotFoundException e) {
			Log.w("startBrokerClient", "Properties file does not exist: "
					+ file);
		} catch (IOException e) {
			Log.w("startBrokerClient", "Error reading props file: " + file);
		}
		return prop;
	}

}
