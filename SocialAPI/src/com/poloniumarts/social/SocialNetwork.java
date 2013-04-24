package com.poloniumarts.social;

import java.lang.reflect.Method;
import java.security.InvalidParameterException;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;

public class SocialNetwork {
	static public final int VK = 1;
	static public final int FACEBOOK = 2;
	static public final int TWITTER = 3;

	/**
	 * 
	 * @param context - something that has startActivityForResult method (Activity, Fragment, etc)
	 *  It is used for launch web-browser for logging in.
	 * @param socialNetworkType - {@value #VK}, {@value #FACEBOOK}
	 * @return interface {@link User} for an interaction with social network
	 */
	static public User getUser(Activity context, int socialNetworkType, int onActivityResultRequestCode) {
		checkOnActivityResultOverloading(context);
		fetchMetadata( context );
	    
		switch (socialNetworkType) {
		case VK:
			fetchVkParameters();
			return new UserVk(context, onActivityResultRequestCode);
		case FACEBOOK:
			fetchFacebookParameters();
			return new UserFacebook(context, onActivityResultRequestCode);
		case TWITTER:
			fetchTwitterParameters();
			return new UserTwitter(context, onActivityResultRequestCode);
		default:
			throw new InvalidParameterException("Unknown type of social network");
		}
	}

	public 	static void skipOnActivityResultCheck(){
		skipOnActivityResultCheck = true;
	}
	private static boolean 	skipOnActivityResultCheck;
	private static Bundle	metadata = null;
	private static void fetchMetadata(Activity context) {
		if (metadata != null){
			return;
		}
		ApplicationInfo ai = null;
		try {
			ai = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
		} catch (NameNotFoundException e){
		};
		
	    metadata = ai.metaData;
	}
	
	/**
	 * Doesn't fetch anything, just check errors */
	private static void fetchFacebookParameters(){
		String facebookId = metadata.getString("com.facebook.sdk.ApplicationId");
	    assert facebookId != null && facebookId.length() > 0: "You haven't set up com.facebook.ApplicationId constant in AndroidManifest.xml";
	}
	
	private static void fetchVkParameters(){
		if (Constants.VK_APP_ID != null){
			return;
		}
		
	    Constants.VK_APP_ID = metadata.getString("com.poloniumarts.vk_app_id");
	    assert Constants.VK_APP_ID != null && Constants.VK_APP_ID.length() > 0 : "You haven't set up com.perm.ApplicationId constant in AndroidManifest.xml";
	}
	
	private static void fetchTwitterParameters(){
		if (Constants.TWITTER_CONSUMER_KEY != null && Constants.TWITTER_CONSUMER_SECRET != null){
			return;
		}
	    Constants.TWITTER_CONSUMER_KEY 		= metadata.getString("com.poloniumarts.twitter_consumer_key");
	    Constants.TWITTER_CONSUMER_SECRET 	= metadata.getString("com.poloniumarts.twitter_consumer_secret");
	    assert Constants.TWITTER_CONSUMER_KEY != null && Constants.TWITTER_CONSUMER_KEY.length() > 0 : "You haven't set up com.poloniumarts.twitter_consumer_key";
	    assert Constants.TWITTER_CONSUMER_SECRET != null && Constants.TWITTER_CONSUMER_SECRET.length() > 0: "You haven't set up com.poloniumarts.twitter_consumer_secret";
	}
	
	private static void checkOnActivityResultOverloading(Activity context){
		if (skipOnActivityResultCheck){
			return;
		}
		Class<? extends Activity> activityClass = context.getClass();
		try {
			Method onActivityResult = activityClass.getDeclaredMethod("onActivityResult", new Class[]{Integer.TYPE, Integer.TYPE, Intent.class});
		} catch (NoSuchMethodException e) {
			assert false : "You haven't overrode onActivityResult in your activity or haven't invoked SocialNetwork.skipOnActivityResultCheck()";
		}
		
	}
}
