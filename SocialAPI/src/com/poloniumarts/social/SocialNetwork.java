package com.poloniumarts.social;

import java.security.InvalidParameterException;

import android.app.Activity;
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
		
		fetchRequisitions( context );
	    
		switch (socialNetworkType) {
		case VK:
			return new UserVk(context, onActivityResultRequestCode);
		case FACEBOOK:
			return new UserFacebook(context, onActivityResultRequestCode);
		case TWITTER:
			return new UserTwitter(context, onActivityResultRequestCode);
		default:
			throw new InvalidParameterException("Unknown type of social network");
		}
	}

	private static void fetchRequisitions(Activity context) {
		ApplicationInfo ai = null;
		try {
			ai = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
		} catch (NameNotFoundException e){
		};
		
	    Bundle bundle = ai.metaData;
	    String facebookId = bundle.getString("com.facebook.sdk.ApplicationId");
	    if (facebookId == null){
	    	throw new IllegalStateException("You haven't set up com.facebook.ApplicationId constant in AndroidManifest.xml");
	    }
		
	    String vkId = bundle.getString("com.poloniumarts.vk_app_id");
	    if (vkId == null){
	    	throw new IllegalStateException("You haven't set up com.perm.ApplicationId constant in AndroidManifest.xml");
	    }	    
	    
	    Constants.TWITTER_CONSUMER_KEY = bundle.getString("com.poloniumarts.twitter_consumer_key");
	    Constants.TWITTER_CONSUMER_SECRET = bundle.getString("com.poloniumarts.twitter_consumer_secret");
	    
	    Constants.VK_APP_ID = vkId;
	    
	    		
	}
}
