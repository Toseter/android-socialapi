package com.poloniumarts.social;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.facebook.FacebookRequestError;
import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.Request.Callback;
import com.facebook.Request.GraphUserCallback;
import com.facebook.RequestAsyncTask;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.Session.StatusCallback;
import com.facebook.SessionState;
import com.facebook.model.GraphUser;

class UserFacebook implements User {

	int onActivityResultRequestCode;
	final Activity context;
	Session session;
	Account account = new Account("FB");
	private volatile boolean lockThread;
	
	public UserFacebook(final Activity context, int onActivityResultRequestCode) {
		this.onActivityResultRequestCode = onActivityResultRequestCode;
		this.context = context;

		session = Session.openActiveSession(context, false, new StatusCallback() {
			@Override
			public void call(Session session, SessionState state, Exception exception) {
				lockThread = true;
			}
		});
		while (lockThread)
			;
		if (session != null)
			account.restore(context);
		session = Session.getActiveSession();
	}

	private boolean hasPublishPermission() {
		Session session = Session.getActiveSession();
		return session != null && session.getPermissions().contains("publish_actions");
	}

	@Override
	public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
		session.onActivityResult(context, requestCode, resultCode, data);
		return false;
	}

	@Override
	public boolean isLoggedIn() {

		if (session == null)
			return false;
		return session.isOpened();
	}

	@Override
	public void login(final OnUserLoggedInListener callback) {
		session = Session.openActiveSession(context, true, new StatusCallback() {

			@Override
			public void call(final Session session, final SessionState state,
					final Exception exception) {
				switch (state) {
				case OPENING:
					return;
				case CLOSED_LOGIN_FAILED:
					callback.onLoggedIn(true, exception);
					return;
				case OPENED_TOKEN_UPDATED:
					Toast.makeText(context, "Token is updated", Toast.LENGTH_SHORT)
							.show();
					return;
				default:
					Request.executeMeRequestAsync(session, new GraphUserCallback() {

						@Override
						public void onCompleted(GraphUser user, Response response) {
							account.accessToken = session.getAccessToken();
							if (user != null) {
								account.userId = user.getId();
								account.firstName = user.getFirstName();
								account.lastName = user.getLastName();
								account.birthday = user.getBirthday();
								account.email = (String) user.getProperty("email");
								account.photoAddress = "http://graph.facebook.com/"
										+ user.getId() + "/picture?width=100&height=100";

								String sex = (String) user.getProperty("gender");
								if (sex.equals("male")) {
									account.sex = User.SEX_MALE;
								} else if (sex.equals("female")) {
									account.sex = User.SEX_FEMALE;
								} else {
									account.sex = User.SEX_UNKNOWN;
								}
								account.save(context);
							}

							if (!hasPublishPermission()) {
								requestPermissions();
							}

							callback.onLoggedIn(exception != null, exception);
						}
					});
					break;
				}
			}
		});
		
	
	}

	@Override
	public void logout(OnUserLoggedOutListener callback) {
		if (isLoggedIn()) {
			session.closeAndClearTokenInformation();
		}
	}

	@Override
	public void wallPost(final String message, final OnWallPostedListener callback) {
		Request.Callback requestCallback = new Request.Callback() {

			@Override
			public void onCompleted(Response response) {
				FacebookRequestError error = response.getError();
				callback.onWallPosted(error != null,
						error == null ? null : error.getException());
			}
		};
		Bundle postParams = new Bundle();
		postParams.putString("message", message);

		Request request = new Request(session, "me/feed", postParams, HttpMethod.POST,
				requestCallback);

		RequestAsyncTask task = new RequestAsyncTask(request);
		task.execute();
	}

	@Override
	public void wallPost(final String message, final File picture,
			final OnWallPostedListener callback) {
		if (hasPublishPermission()) {
			Request request;
			try {
				request = Request.newUploadPhotoRequest(Session.getActiveSession(),
						picture, new Request.Callback() {
							@Override
							public void onCompleted(Response response) {
								FacebookRequestError error = response.getError();
								if (error != null){
									callback.onWallPosted(true, error.getException() );
									return;
								}
								String id = response.getGraphObject().getProperty("id").toString();
								
								Request requestFeed = new Request(session, "me/feed", null, HttpMethod.POST, new Callback() {
									
									@Override
									public void onCompleted(Response response) {

										FacebookRequestError errorFeed = response.getError();
										callback.onWallPosted(errorFeed != null,
												errorFeed != null ? errorFeed.getException() : null);

										
									}
								});
								Bundle parameters = new Bundle();
								parameters.putString("message", message);
								parameters.putString("include_hidden", "true");
								parameters.putString("object_attachment", id);
								parameters.putString("object_id", id);
								requestFeed.setParameters(parameters );
								requestFeed.executeAsync();
								
							}
						});
				if (message != null){
					Bundle params = request.getParameters();
					
					params.putString("message", message);
					request.setParameters(params);
				}
				request.executeAsync();
                
			} catch (FileNotFoundException e) {
				callback.onWallPosted(true, e);
			}

		} else {

			requestPermissions();
			if (session.getPermissions().contains("publish_actions")) {
				wallPost(message, picture, callback);
			} else {
				callback.onWallPosted(true, null);
			}
		}
	}

	private void requestPermissions() {
//		session.requestNewReadPermissions(new Session.NewPermissionsRequest(context, Arrays.asList("email", "user_birthday")));
		session.requestNewPublishPermissions(new Session.NewPermissionsRequest(context, Arrays.asList("publish_actions")));
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
		return account.email;
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
	public String getPicture() {
		return account.photoAddress;
	}
}
