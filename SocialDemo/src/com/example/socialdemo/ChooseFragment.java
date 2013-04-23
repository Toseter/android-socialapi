package com.example.socialdemo;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

/**
 * A simple {@link android.support.v4.app.Fragment} subclass. Activities that
 * contain this fragment must implement the
 * {@link ChooseFragment.OnFragmentInteractionListener} interface to handle
 * interaction events. Use the {@link ChooseFragment#newInstance} factory method
 * to create an instance of this fragment.
 * 
 */
public class ChooseFragment extends Fragment implements OnClickListener {
	public static final String URI = "fragment://choose";
	private OnFragmentInteractionListener listener;
	private Button buttonFacebook;
	private Button buttonVk;
	private Button buttonTwitter;
	
	public ChooseFragment() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_choose, container, false);
		buttonFacebook = (Button) view.findViewById(R.id.buttonFacebook);
		buttonVk = (Button)view.findViewById(R.id.buttonVk);
		buttonTwitter = (Button )view.findViewById(R.id.buttonTwitter);
		
		buttonFacebook.setOnClickListener(this);
		buttonVk.setOnClickListener(this);
		buttonTwitter.setOnClickListener(this);
		return view;
	}

	// TODO: Rename method, update argument and hook method into UI event
	public void onButtonPressed(Uri uri) {
		if (listener != null) {
			listener.onFragmentInteraction(uri);
		}
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
	public void onClick(View view) {
		PublishFragment fragment;
		
		switch(view.getId()){
			case R.id.buttonFacebook:
				listener.onFragmentInteraction(Uri.parse( PublishFragment.FACEBOOK ));
				break;
			case R.id.buttonTwitter:
				listener.onFragmentInteraction(Uri.parse( PublishFragment.TWITTER ));
				break;
			case R.id.buttonVk:
				listener.onFragmentInteraction(Uri.parse( PublishFragment.VK));
				break;
			default:
				assert false: "Invalid event";
		}
		
		
	}

}
