package com.example.socialdemo;

import com.poloniumarts.social.User;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;

public class MainActivity extends FragmentActivity implements OnFragmentInteractionListener {
	Fragment currentFragment;
	static Uri currentUri = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		if(currentUri == null){
			currentUri = Uri.parse(ChooseFragment.URI);
			getSupportFragmentManager()
				.beginTransaction()
				.replace(R.id.fragmentFrame, new ChooseFragment())
				.commit();
		}
	}
	
	@Override
	public void onFragmentInteraction(Uri uri) {
		if (uri == null){
			return;
		}
		currentUri = uri;
		if ( uri.toString().equals(PublishFragment.VK) 
				|| uri.toString().equals(PublishFragment.FACEBOOK) 
				|| uri.toString().equals(PublishFragment.TWITTER)){
			currentFragment = PublishFragment.newInstance(uri.toString());
		}else if (uri.toString().equals(ChooseFragment.URI)){
			currentFragment = new ChooseFragment();
		}
		
		getSupportFragmentManager()
			.beginTransaction()
			.addToBackStack(null)
			.replace(R.id.fragmentFrame, currentFragment)
			.commit();
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (currentFragment != null){
			currentFragment.onActivityResult(requestCode, resultCode, data);
		}
	}
}
