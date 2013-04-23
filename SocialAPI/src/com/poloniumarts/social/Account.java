package com.poloniumarts.social;


import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

class Account {
	public String accessToken;
	public String accessTokenSecret;
	public String userId;
	public String firstName;
	public String lastName;
	public String birthday;
	public String email;
	public String photoAddress;
	public int sex;
	
	private String prefix;

	public Account(String prefix) {
		this.prefix = prefix + "_";
	}
	public void save(Context context) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		Editor editor = prefs.edit();
		editor.putString(prefix + "access_token", accessToken);
		editor.putString(prefix + "access_token_secret", accessTokenSecret);
		editor.putString(prefix + "user_id", userId);
		editor.putString(prefix + "firstName", firstName);
		editor.putString(prefix + "lastName", lastName);
		editor.putString(prefix + "birthday", birthday);
		editor.putString(prefix + "email", email);
		editor.putString(prefix + "photo_address", photoAddress);
		editor.putInt(prefix + "sex", sex);
		editor.commit();
	}

	public void restore(Context context) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		accessToken = prefs.getString(prefix + "access_token", null);
		accessTokenSecret = prefs.getString(prefix + "access_token_secret", null);
		userId = prefs.getString(prefix + "user_id", null);
		firstName = prefs.getString(prefix + "firstName", null);
		lastName = prefs.getString(prefix + "lastName", null);
		birthday = prefs.getString(prefix + "birthday", null);
		email = prefs.getString(prefix + "email", null);
		photoAddress = prefs.getString(prefix + "photo_address", null);
		sex = prefs.getInt(prefix + "sex", User.SEX_UNKNOWN);
	}
}
