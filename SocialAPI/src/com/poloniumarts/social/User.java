package com.poloniumarts.social;

import java.io.File;

import android.preference.PreferenceManager.OnActivityResultListener;

public interface User extends OnActivityResultListener {
	static public final int SEX_FEMALE = 1;
	static public final int SEX_MALE = 2;
	static public final int SEX_UNKNOWN = 0;

	public boolean isLoggedIn();

	public void login(OnUserLoggedInListener callback);

	public void logout(OnUserLoggedOutListener callback);

	public void wallPost(final String message, OnWallPostedListener callback);

	public void wallPost(final String message, final File picture,
			OnWallPostedListener callback);

	public String getFirstName();

	public String getLastName();

	/**
	 * 
	 * @return user e-mail or null (for VK the value is always null, because VK
	 *         API doesn't support this feature)
	 */
	public String getEmail();

	/**
	 * 
	 * @return birthday. The format is depended on type of social network and of
	 *         user preferences. May be null.
	 */
	public String getBirthday();

	/**
	 * 
	 * @return Sex of the user {@link #SEX_FEMALE}, {@link #SEX_MALE} or
	 *         {@link #SEX_UNKNOWN}
	 */
	public int getSex();

	/**
	 * 
	 * @return address of user's avatar (100x100)
	 */
	public String getPicture();
	
}
