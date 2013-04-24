package com.example.minidemo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.poloniumarts.social.OnUserLoggedInListener;
import com.poloniumarts.social.OnWallPostedListener;
import com.poloniumarts.social.SocialNetwork;
import com.poloniumarts.social.User;

public class MainActivity extends Activity implements OnUserLoggedInListener, OnWallPostedListener {

	private static final int onActivityResultRequestCode = 1;
	User user;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		user = SocialNetwork.getUser(this, SocialNetwork.VK, onActivityResultRequestCode);
		if (!user.isLoggedIn()){
			user.login(this);
		} else {
			wallPost();
		}
	}
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		user.onActivityResult(requestCode, resultCode, data);
	}
	
	private void wallPost() {
		user.wallPost("Hello world", this);
	}

	public void onLoggedIn(boolean isFail, Throwable exception) {
		if (isFail){
			System.exit(-1);
		}
		Toast.makeText(this, user.getFirstName() + " has been successfully logged in", Toast.LENGTH_LONG).show();
		wallPost();
	}

	public void onWallPosted(boolean isFail, Throwable exception) {
		Toast.makeText(this, "Message was wall posted", Toast.LENGTH_LONG).show();
	}
}
