package org.universAAL.android.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Dictionary;
import java.util.Properties;

import org.universAAL.android.R;
import org.universAAL.android.services.MiddlewareService;
import org.universAAL.middleware.util.Constants;
import org.universAAL.ontology.profile.AssistedPerson;
import org.universAAL.ontology.profile.Caregiver;
import org.universAAL.ontology.profile.User;

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
	private static final String TAG = "Config";
	private static String mServerURL = AppConstants.Defaults.CONNURL;
	private static String mServerUSR = AppConstants.Defaults.CONNUSR;
	private static String mServerPWD = AppConstants.Defaults.CONNPWD;
	private static String mConfigFolder = AppConstants.Defaults.CFOLDER;
	private static String mUAALUser = AppConstants.Defaults.USER;
	private static int mSettingRemoteType = AppConstants.Defaults.CONNTYPE;
	private static int mSettingRemoteMode = AppConstants.Defaults.CONNMODE;
	private static boolean mSettingWifiEnabled = AppConstants.Defaults.CONNWIFI;
	private static boolean mServiceCoord = AppConstants.Defaults.ISCOORD;
	private static boolean mUIHandler = AppConstants.Defaults.UIHANDLER;

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
		mServerURL = prefs.getString(AppConstants.Keys.CONNURL,	AppConstants.Defaults.CONNURL);
		mServerUSR = prefs.getString(AppConstants.Keys.CONNUSR,	AppConstants.Defaults.CONNUSR);
		mServerPWD = prefs.getString(AppConstants.Keys.CONNPWD, AppConstants.Defaults.CONNPWD);
		mServiceCoord = prefs.getBoolean(AppConstants.Keys.ISCOORD, AppConstants.Defaults.ISCOORD);
		mUIHandler = prefs.getBoolean(AppConstants.Keys.UIHANDLER, AppConstants.Defaults.UIHANDLER);
		mSettingWifiEnabled = prefs.getBoolean(AppConstants.Keys.CONNWIFI, AppConstants.Defaults.CONNWIFI);
		mSettingRemoteMode = Integer.parseInt(prefs.getString(AppConstants.Keys.CONNMODE,
				Integer.toString(AppConstants.Defaults.CONNMODE)));
		mSettingRemoteType = Integer.parseInt(prefs.getString(AppConstants.Keys.CONNTYPE,
				Integer.toString(AppConstants.Defaults.CONNTYPE)));
		mConfigFolder = prefs.getString(AppConstants.Keys.CFOLDER, AppConstants.Defaults.CFOLDER);
		mUAALUser = prefs.getString(AppConstants.Keys.USER, AppConstants.Defaults.USER);
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
	 * Check if this application should work as a UI Handler
	 * 
	 * @return true if it is set as UI Handler
	 */
	public static boolean isUIHandler() {
		return mUIHandler;
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
			Log.w(TAG, "Properties file does not exist: "
					+ file);
		} catch (IOException e) {
			Log.w(TAG, "Error reading props file: " + file);
		}
		return prop;
	}
	
	/**
	 * Creates all the configuration files needed for running uAAL if they dont
	 * exist already.
	 * 
	 * @param ctxt
	 *            Application context
	 */
	public static void createFiles(Context ctxt) {
		String basepath = Environment.getExternalStorageDirectory().getPath() + Config.getConfigDir();
//		String ontpath = Environment.getExternalStorageDirectory().getPath()
//				+ PreferenceManager.getDefaultSharedPreferences(ctxt)
//						.getString(AppConstants.Keys.OFOLDER,
//								AppConstants.Defaults.OFOLDER);
		Log.d(TAG, "Creating default configuration files");
		try {
			createFile(ctxt, R.raw.jgroups, basepath,"mw.connectors.communication.jgroups.core.properties");
			createFile(ctxt, R.raw.slp, basepath,"mw.connectors.discovery.slp.core.properties");
			createFile(ctxt, R.raw.managersaalspace, basepath,"mw.managers.aalspace.core.properties");
			createFile(ctxt, R.raw.modulesaalspace, basepath,"mw.modules.aalspace.core.properties");
			createFile(ctxt, R.raw.client, basepath+"ri.gateway.multitenant/","client.properties");
			createFile(ctxt, R.raw.udp, basepath,"udp.xml");
			createFile(ctxt, R.raw.aalspace, basepath,"aalspace.xsd");
			createFile(ctxt, R.raw.home, basepath,"Home.space");
//			createFile(ctxt, R.raw.onthwo321s, ontpath, "ont.hwo-3.2.1-SNAPSHOT.jar");
//			createFile(ctxt, R.raw.ontagendamsg321, ontpath, "ont.agenda.messaging-3.2.1-SNAPSHOT.jar");
			File folder = new File(basepath+"mw.managers.aalspace.osgi/");
			folder.mkdirs(); //This is so that AALSpace manager can place peers.id
		} catch (IOException e) {
			Log.e(TAG, "Could not create one or more default configuarion files."
							+ "You will have to place them manually: " + e);
		}
	}

	/**
	 * Writes a new file into the sdcard if it doesnt exist already
	 * 
	 * @param ctxt
	 *            Application context
	 * @param fileID
	 *            ID of the file in R.raw
	 * @param path
	 *            Path in the sdcard without the file name
	 * @param filename
	 *            Name of the file
	 * @throws IOException
	 *             If an error occurs during writing
	 */
	private static void createFile(Context ctxt, int fileID, String path,
			String filename) throws IOException {
		File file = new File(path, filename);
		if (file.exists()) {
			return; // Do not overwrite existing files
		}
		File folder = new File(path);
		folder.mkdirs(); // Create folder if it did not exist, the file is created in FileOutputStream
		InputStream in = ctxt.getResources().openRawResource(fileID);
		FileOutputStream out = new FileOutputStream(file);
		byte[] buff = new byte[1024];
		int read = 0;
		try {
			while ((read = in.read(buff)) > 0) {
				out.write(buff, 0, read);
			}
		} finally {
			in.close();
			out.close();
		}
	}

	public static User makeUser(){
		switch (MiddlewareService.mUserType) {
			case AppConstants.USER_TYPE_AP:
				return new AssistedPerson(Constants.uAAL_MIDDLEWARE_LOCAL_ID_PREFIX + getUAALUser());
			case AppConstants.USER_TYPE_CARE:
				return new Caregiver(Constants.uAAL_MIDDLEWARE_LOCAL_ID_PREFIX + getUAALUser());
			default:
				return new User(Constants.uAAL_MIDDLEWARE_LOCAL_ID_PREFIX + getUAALUser());
		}
	}
}
