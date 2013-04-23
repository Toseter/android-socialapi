package com.poloniumarts.social;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.FormBodyPart;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;

import com.perm.kate.api.Api;
import com.perm.kate.api.Auth;
import com.perm.kate.api.KException;
import com.perm.kate.api.Photo;

class UserVk implements User {

	private final class AsyncTaskWallPost extends AsyncTask<Object, Integer, Throwable> {

		private OnWallPostedListener callback;

		private List<String> attachPicture(File picture) throws MalformedURLException,
				IOException, JSONException, KException {
			if (picture == null)
				return null;
			String wallServer = api.photosGetWallUploadServer(null, null);

			DefaultHttpClient client = new DefaultHttpClient();
			MultipartEntity multipartEntity = new MultipartEntity();
			multipartEntity.addPart(new FormBodyPart("file1", new FileBody(picture)));
			HttpPost post = new HttpPost(wallServer);
			post.setEntity(multipartEntity);
			HttpResponse response = client.execute(post);

			String answer = IOUtils.toString(response.getEntity().getContent());
			JSONObject json = new JSONObject(answer);

			String server = json.optString("server");
			String photo = json.optString("photo");
			String hash = json.optString("hash");
			ArrayList<Photo> photos = api.saveWallPhoto(server, photo, hash, null, null);
			return Arrays.asList(new String[] { "photo" + account.userId + "_"
					+ photos.get(0).pid });
		}

		@Override
		protected Throwable doInBackground(Object... params) {
			String message = (String) params[0];
			File picture = (File) params[1];
			callback = (OnWallPostedListener) params[2];
			try {
				List<String> attachements = attachPicture(picture);
				api.createWallPost(Integer.valueOf(account.userId), message,
						attachements, null, false, false, false, null, null, null, null);
			} catch (Exception e) {
				e.printStackTrace();
				return e;
			}
			return null;
		}

		@Override
		protected void onPostExecute(Throwable result) {
			super.onPostExecute(result);
			if (callback != null) {
				callback.onWallPosted(result != null, result);
			}
		}
	}

	private final int onActivityResultRequestCode;

	private Activity context;
	private Account account = new Account("VK");
	private Api api;

	public Activity getContext() {
		return context;
	}

	public void setContext(Activity context) {
		this.context = context;
	}

	public UserVk(Activity context, int onActivityResultRequestCode) {
		this.onActivityResultRequestCode = onActivityResultRequestCode;
		setContext(context);
		restore();
	}

	private boolean isAlreadyRestored;

	private void loadProfileAsync(final OnProfileLoadedListener callback) {
		new AsyncTask<Void, Void, Exception>() {
			private ProgressDialog dialog;

			@Override
			protected void onPreExecute() {
				super.onPreExecute();
				String title = context.getString(Constants.PROFILE_HEADER);
				String message = context.getString(Constants.PROFILE_LOADING);
				dialog = ProgressDialog.show(context, title,
						message, true);
			}

			@Override
			protected Exception doInBackground(Void... params) {
				try {

					ArrayList<com.perm.kate.api.User> profiles = api.getProfiles(
							Arrays.asList(new Long[] { Long.valueOf(account.userId) }),
							null, "first_name, last_name, sex, photo_medium", null, null, null);
					com.perm.kate.api.User user = profiles.get(0);
					account.sex = user.sex;
					account.birthday = user.birthdate;
					account.firstName = user.first_name;
					account.lastName = user.last_name;
					account.photoAddress = user.photo_medium;
					account.save(context);
				} catch (Exception e) {
					return e;
				}
				return null;
			}

			@Override
			protected void onPostExecute(Exception result) {
				super.onPostExecute(result);
				dialog.dismiss();
				callback.onProfileLoaded(result != null, result);
			}
		}.execute();
	}

	private void restore() {
		if (isAlreadyRestored != true) {
			isAlreadyRestored = true;
			account.restore(context);

			// Если сессия есть создаём API для обращения к серверу
			if (account.accessToken != null)
				api = new Api(account.accessToken, Constants.VK_APP_ID);
		}
	}

	@Override
	public boolean isLoggedIn() {
		restore();
//		if (account.access_token != null) {
//			api = new Api(account.access_token, Constants.API_ID);
//		}
		return account.accessToken != null && api != null;
	}

	private OnUserLoggedInListener onUserLoggedInListener;

	@Override
	public void login(OnUserLoggedInListener callback) {
		restore();
		this.onUserLoggedInListener = callback;
		Intent intent = new Intent(context, LoginActivity.class);
		intent.putExtra(LoginActivity.URL, Auth.getUrl(Constants.VK_APP_ID, Auth.getSettings()));
		intent.putExtra(LoginActivity.REDIRECT_URL, Auth.redirect_url);
		context.startActivityForResult(intent, onActivityResultRequestCode);
	}

	@Override
	public void logout(OnUserLoggedOutListener callback) {
		api = null;
		account.accessToken = null;
		account.userId = null;
		account.save(context);
		callback.onLoggedIn(false, null);
	}

	@Override
	public void wallPost(String message, OnWallPostedListener callback) {
		wallPost(message, null, callback);
	}

	@Override
	public void wallPost(String message, File picture, OnWallPostedListener callback) {
		AsyncTaskWallPost task = new AsyncTaskWallPost();
		task.execute(message, picture, callback);
	}

	@Override
	public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == onActivityResultRequestCode) {
			if (resultCode == Activity.RESULT_OK) {
				
				String url = data.getStringExtra(LoginActivity.URL);
				if (url.contains("error=")){
					onUserLoggedInListener.onLoggedIn(true, null);
				};
				String[] auth;
				try {
					auth = Auth.parseRedirectUrl(url);
				} catch (Exception e) {
					e.printStackTrace();
					onUserLoggedInListener.onLoggedIn(true, e);
					return false;
				}
				account.accessToken = auth[0];
				account.userId = auth[1];
				account.save(context);
				api = new Api(account.accessToken, Constants.VK_APP_ID);
				loadProfileAsync(new OnProfileLoadedListener() {

					@Override
					public void onProfileLoaded(boolean isFail, Throwable exception) {
						if (onUserLoggedInListener != null) {
							onUserLoggedInListener.onLoggedIn(isFail, exception);
						}
					}
				});

			} else {
				if (onUserLoggedInListener != null) {
					onUserLoggedInListener.onLoggedIn(true, null);
				}
			}
		}
		return false;
	}

	@Override
	public String getFirstName() {
		return account.firstName;
	}

	@Override
	public String getLastName() {
		return account.lastName;
	}

	@Override
	public String getEmail() {
		return null;
	}

	@Override
	public String getBirthday() {
		return account.birthday;
	}

	@Override
	public int getSex() {
		return account.sex;
	}
	
	@Override
	public String getPicture(){
		return account.photoAddress;
	}
}
