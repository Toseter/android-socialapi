package com.poloniumarts.social;

import java.io.File;

import org.apache.commons.lang3.StringUtils;

import twitter4j.StatusUpdate;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;


class UserTwitter implements User {

	private static Twitter twitter;
	private static RequestToken requestToken;

	// private boolean running = false;

	private Activity context;
	private Account account = new Account("TWITTER");
	private int onActivityResultRequestCode;

	public UserTwitter(Activity context, int onActivityResultRequestCode) {
		this.context = context;
		this.onActivityResultRequestCode = onActivityResultRequestCode;
		restore();
	}

	public void restore() {
		if (isLoggedIn()) {
			account.restore(context);
			String token = account.accessToken;
			String secret = account.accessTokenSecret;
			twitter = UserTwitter.createTwitterFactory().getInstance(
					new AccessToken(token, secret));
		}
	}

	@Override
	public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
		/*
		 * Handle OAuth Callback
		 */
		// Uri uri = context.getIntent().getData();
		if (resultCode == Activity.RESULT_CANCELED){
			return false;
		}
		if (requestCode != onActivityResultRequestCode){
			return false;
		}
		Uri uri = Uri.parse(data.getStringExtra(LoginActivity.URL));
		if (uri == null || !(uri.toString().startsWith(UserTwitter.TWITTER_CALLBACK_URL))){
			return false;
		}
		restore();

		final String verifier = uri.getQueryParameter(UserTwitter.TWITTER_IEXTRA_OAUTH_VERIFIER);
		
		if (verifier == null || verifier.length() == 0){
			return false;
		}
		new AsyncTask<Void, Void, AccessToken>() {
			TwitterException e;

			@Override
			protected AccessToken doInBackground(Void... params) {
				try {
					AccessToken token = twitter.getOAuthAccessToken(requestToken,
							verifier);
					account.userId = String.valueOf(token.getUserId());
					account.accessToken = token.getToken();
					account.accessTokenSecret = token.getTokenSecret();
					account.photoAddress = twitter.showUser(token.getScreenName())
							.getMiniProfileImageURL();

					account.save(context);
					return token;
				} catch (TwitterException e) {
					this.e = e;
					e.printStackTrace();
				}
				return null;
			}

			@Override
			protected void onPostExecute(final AccessToken result) {
				super.onPostExecute(result);
				if (e != null || result == null) {
					if (onUserLoggedInListener != null) {
						onUserLoggedInListener.onLoggedIn(true, e);
					}
					if (e != null) {
						e.printStackTrace();
					}
					return;
				}

				if (onUserLoggedInListener != null) {
					onUserLoggedInListener.onLoggedIn(false, null);
				}
			}
		}.execute();
		return false;
	}

	@Override
	public boolean isLoggedIn() {
		account.restore(context);
		return account.accessToken != null;
	}

	private OnUserLoggedInListener onUserLoggedInListener;
	
	/**
	 * OAuth authentication
	 */
	@Override
	public void login(final OnUserLoggedInListener callback) {
		twitter = UserTwitter.createTwitterFactory().getInstance();

		new AsyncTask<Void, Void, TwitterException>() {
			@Override
			protected TwitterException doInBackground(Void... params) {
				try {
					requestToken = twitter.getOAuthRequestToken(UserTwitter.TWITTER_CALLBACK_URL);
				} catch (TwitterException e) {
					return e;
				}
				return null;
			}

			@Override
			protected void onPostExecute(TwitterException result) {
				super.onPostExecute(result);
				if (callback != null && result != null) {
					callback.onLoggedIn(true, result);
					result.printStackTrace();
					return;
				}
				onUserLoggedInListener = callback;
				Intent loginIntent = new Intent(context, LoginActivity.class);
				loginIntent.putExtra(LoginActivity.URL,
						requestToken.getAuthenticationURL());
				loginIntent.putExtra(LoginActivity.REDIRECT_URL, UserTwitter.TWITTER_CALLBACK_URL);
				context.startActivityForResult(loginIntent, onActivityResultRequestCode);
			}
		}.execute();
	}

	@Override
	public void logout(OnUserLoggedOutListener callback) {
		account.accessToken = null;
		account.accessTokenSecret = null;
		account.save(context);
	}

	@Override
	public void wallPost(String message, OnWallPostedListener callback) {
		wallPost(message, null, callback);
	}

	
	private String cutMessage(final String message, final File picture){
		final int length = (picture == null)? 140: 117;
		return StringUtils.abbreviate(message, length);
	}
	
	@Override
	public void wallPost(final String message, final File picture,
			final OnWallPostedListener callback) {
		if (twitter == null)
			throw new RuntimeException("UserTwitter.wallPost. twitter == null");

		new AsyncTask<Void, Void, TwitterException>() {
			@Override
			protected TwitterException doInBackground(Void... params) {
				try {
					StatusUpdate statusUpdate = new StatusUpdate(cutMessage(message, picture));
					if (picture != null) {
						statusUpdate.setMedia(picture);
					}
					twitter.updateStatus(statusUpdate);
				} catch (TwitterException e) {
					e.printStackTrace();
					return e;
				}
				return null;
			}

			@Override
			protected void onPostExecute(TwitterException result) {
				super.onPostExecute(result);
				if (callback != null) {
					callback.onWallPosted(result != null, result);
				}
			}
		}.execute();

	}

	@Override
	public String getFirstName() {
		// twitter.getScreenName();
		return null;
	}

	@Override
	public String getLastName() {
		return null;
	}

	@Override
	public String getEmail() {
		return null;
	}

	@Override
	public String getBirthday() {
		return null;
	}

	@Override
	public int getSex() {
		return 0;
	}

	@Override
	public String getPicture() {
		return account.photoAddress;
	}

	public static final String  TWITTER_CALLBACK_SCHEME = "x-oauthflow-twitter";
	public static final String  OAUTH_CALLBACK_HOST = "callback";
	public static final String  TWITTER_CALLBACK_URL = TWITTER_CALLBACK_SCHEME + "://" + OAUTH_CALLBACK_HOST;
	
	static final String TWITTER_IEXTRA_OAUTH_TOKEN = "oauth_token";
	static final String TWITTER_IEXTRA_OAUTH_VERIFIER = "oauth_verifier";
	static final String TWITTER_IEXTRA_AUTH_URL = "auth_url";
	static final String TWITTER_PREFERENCE_NAME = "twitter_oauth";
	
	static public TwitterFactory createTwitterFactory(){
		ConfigurationBuilder configurationBuilder = new ConfigurationBuilder();
		configurationBuilder.setOAuthConsumerKey(com.poloniumarts.social.Constants.TWITTER_CONSUMER_KEY);
		configurationBuilder.setOAuthConsumerSecret(com.poloniumarts.social.Constants.TWITTER_CONSUMER_SECRET);
		Configuration configuration = configurationBuilder.build();
		return new TwitterFactory(configuration);
	}

}
