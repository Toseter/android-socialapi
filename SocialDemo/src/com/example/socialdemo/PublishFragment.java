package com.example.socialdemo;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.poloniumarts.social.OnUserLoggedInListener;
import com.poloniumarts.social.OnWallPostedListener;
import com.poloniumarts.social.SocialNetwork;
import com.poloniumarts.social.User;
import com.poloniumarts.utils.ImageDownloader;

/**
 * A simple {@link android.support.v4.app.Fragment} subclass. Activities that
 * contain this fragment must implement the
 * {@link PublishFragment.OnFragmentInteractionListener} interface to handle
 * interaction events. Use the {@link PublishFragment#newInstance} factory
 * method to create an instance of this fragment.
 * 
 */
public class PublishFragment extends Fragment implements OnClickListener, OnWallPostedListener, OnUserLoggedInListener {
	public static final String VK = "fragment://VK";
	public static final String FACEBOOK = "fragment://FACEBOOK";
	public static final String TWITTER = "fragment://TWITTER";
	
	private static final String NETWORK_TYPE = "NETWORK_TYPE";
	private static final int REQUEST_SOCIAL_NETWORK = 1;
	private static final int REQUEST_IMAGE = 2;

	private String networkType;

	private OnFragmentInteractionListener listener;
	private Button		buttonPublishText;
	private Button		buttonPublishImage;
	private EditText	editTextMessage;
	private TextView 	textViewFirstName;
	private TextView 	textViewLastName;
	private TextView 	textViewEmail;
	private TextView 	textViewBirthdate;
	private TextView 	textViewSex;
	private ImageView	imageViewPhoto;
	
	User		socialUser;
	
	/**
	 * Use this factory method to create a new instance of this fragment using
	 * the provided parameters.
	 * 
	 * @param networkType
	 *            Parameter 1.
	 * @return A new instance of fragment PublishFragment.
	 */
	public static PublishFragment newInstance(String networkType) {
		PublishFragment fragment = new PublishFragment();
		Bundle args = new Bundle();
		args.putString(NETWORK_TYPE, networkType);
		fragment.setArguments(args);
		return fragment;
	}
	public PublishFragment() {
		// Required empty public constructor
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getArguments() != null) {
			networkType = getArguments().getString(NETWORK_TYPE);
			if( networkType.equals(VK) ){
				socialUser = SocialNetwork.getUser(getActivity(), SocialNetwork.VK, REQUEST_SOCIAL_NETWORK);
			}else if (networkType.equals(FACEBOOK)){
				socialUser = SocialNetwork.getUser(getActivity(), SocialNetwork.FACEBOOK, REQUEST_SOCIAL_NETWORK);
			}else if(networkType.equals(TWITTER)){
				socialUser = SocialNetwork.getUser(getActivity(), SocialNetwork.TWITTER, REQUEST_SOCIAL_NETWORK);
			}
		}
		assert socialUser != null;
		if (!socialUser.isLoggedIn()){
			socialUser.login(this);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_publish, container, false);
		buttonPublishText	= (Button) view.findViewById(R.id.buttonPublishText);
		buttonPublishImage	= (Button) view.findViewById(R.id.buttonPublishImage);
		editTextMessage		= (EditText) view.findViewById(R.id.editTextMessage);
		textViewFirstName	= (TextView) view.findViewById(R.id.textViewFirstName);
		textViewLastName	= (TextView) view.findViewById(R.id.textViewLastName);
		textViewEmail	= (TextView) view.findViewById(R.id.textViewEmail);
		textViewBirthdate	= (TextView) view.findViewById(R.id.textViewBirthdate);
		textViewSex	= (TextView) view.findViewById(R.id.textViewSex);
		imageViewPhoto = (ImageView)view.findViewById(R.id.imageViewPhoto);
		buttonPublishText.setOnClickListener(this);
		buttonPublishImage.setOnClickListener(this);
		displayProfile();
		return view;
	}


	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			listener = (OnFragmentInteractionListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement OnFragmentInteractionListener");
		}
	}

	@Override
	public void onDetach() {
		super.onDetach();
		listener = null;
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		/* you can't use:
		 * if (requestCode == REQUEST_SOCIAL_NETWORK){
		 * 		socialUser.onActivityResult(requestCode, resultCode, data);
		 * }
		 * because Facebook API generates random request code and you can't change it
		 *  */
		if (socialUser != null){
			socialUser.onActivityResult(requestCode, resultCode, data);
		}
		
		if (requestCode == REQUEST_IMAGE && resultCode == Activity.RESULT_OK && data != null){			
			String path = getImagePath(data);
			socialUser.wallPost(editTextMessage.getText().toString(), new File(path), this);
		}
	}
	private String getImagePath(Intent data) {
		String[] column = {MediaStore.Images.Media.DATA};
		Cursor cursor = getActivity().getContentResolver().query(
				data.getData(), column, null, null, null);
		cursor.moveToFirst();
		int columnIndex = cursor.getColumnIndex( column[0] );
		String picturePath = cursor.getString(columnIndex);
		cursor.close();
		return picturePath;
	}
	
	@Override
	public void onClick(View view) {
		switch(view.getId()){
			case R.id.buttonPublishText:
				socialUser.wallPost(editTextMessage.getText().toString(), this);
				break;
			case R.id.buttonPublishImage:
				Intent intentGetContent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
				Intent chooser = Intent.createChooser(intentGetContent, getString(R.string.choose_image));
				startActivityForResult(intentGetContent, REQUEST_IMAGE);
				break;
		}
	}
	@Override
	public void onWallPosted(boolean isFail, Throwable exception) {
		if (isFail && exception != null && exception.getLocalizedMessage() != null){
			Toast.makeText(getActivity(), exception.getLocalizedMessage(), Toast.LENGTH_LONG).show();
		}else if (isFail){
			Toast.makeText(getActivity(), R.string.wall_post_fail, Toast.LENGTH_LONG).show();
		}else{
			Toast.makeText(getActivity(), R.string.wall_post_success, Toast.LENGTH_LONG).show();
		}
	}
	@Override
	public void onLoggedIn(boolean isFail, Throwable exception) {
		if (!isFail){
			Toast.makeText(getActivity(), R.string.login_success, Toast.LENGTH_LONG).show();
			displayProfile();
			return;
		}
		String message;
		if (exception != null && exception.getLocalizedMessage() != null){
			message = exception.getLocalizedMessage();
		}else{
			message = getString( R.string.login_fail );
		};
		
		new AlertDialog.Builder(getActivity())
		.setTitle(R.string.error)
		.setMessage(message)
		.setPositiveButton(R.string.try_again, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				socialUser.login(PublishFragment.this);
			}
		})
		.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				listener.onFragmentInteraction(Uri.parse(ChooseFragment.URI));
			}
		})
		.show();
	}
	
	private void displayProfile() {
		textViewFirstName	.setText ( socialUser.getFirstName() );
		textViewLastName	.setText ( socialUser.getLastName() );
		textViewEmail		.setText ( socialUser.getEmail() );
		textViewBirthdate	.setText ( socialUser.getBirthday() );
		
		if (socialUser.getPicture() != null){
			ImageDownloader.getInstance().download(socialUser.getPicture(), imageViewPhoto);
		}
		
		switch( socialUser.getSex() ){
			case User.SEX_MALE:
				textViewSex.setText(R.string.male);
				break;
			case User.SEX_FEMALE:
				textViewSex.setText(R.string.female);
				break;
			case User.SEX_UNKNOWN:
				textViewSex.setText(R.string.unknown);
				break;
			default:
				assert false;
		}
	}

}
