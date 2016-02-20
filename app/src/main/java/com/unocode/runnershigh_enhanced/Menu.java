package com.unocode.runnershigh_enhanced;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.games.Games;
import com.google.android.gms.plus.Plus;
import com.google.example.games.basegameutils.BaseGameUtils;

public class Menu extends Activity implements
		/* google play games added implements */
		View.OnClickListener,
		GoogleApiClient.ConnectionCallbacks,
		GoogleApiClient.OnConnectionFailedListener {

	private Toast loadMessage;
	private Runnable gameLauncher;
	private Intent gameIntent;
	private Handler mHandler;
	private android.widget.Button mPlayButton;

	/* google play games added */
	final static String TAG = "RunnersHighEnhanced";

	private static int RC_SIGN_IN = 9001;

	private boolean mResolvingConnectionFailure = false;
	private boolean mAutoStartSignInFlow = true;
	private boolean mSignInClicked = false;
	boolean mExplicitSignOut = false;
	boolean mInSignInFlow = false; // set to true when you're in the middle of the
	// sign in flow, to know you should not attempt
	// to connect in onStart()
	private GoogleApiClient mGoogleApiClient;  // initialized in onCreate
	
    @Override
    public void onCreate(Bundle savedInstanceState) {

    	requestWindowFeature(Window.FEATURE_NO_TITLE);  
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		
    	super.onCreate(savedInstanceState);
        setContentView(R.layout.menu); 

		loadMessage = Toast.makeText(getApplicationContext(), "loading game...", Toast.LENGTH_SHORT );
		//warning above is dumb because if you check further down, show() is being called. for now IGNORE

		loadMessage.setGravity(Gravity.CENTER, 0, 0);
        
		gameIntent = new Intent (this, main.class);
		mPlayButton = (android.widget.Button)findViewById(R.id.startButton);
		mPlayButton.setClickable(true);
		mPlayButton.setEnabled(true);
		gameLauncher = new Runnable() {
			
			public void run() {
				mPlayButton.setClickable(false);
		    	mPlayButton.getBackground().setColorFilter(Color.GRAY, PorterDuff.Mode.MULTIPLY);
				startActivityForResult(gameIntent, 0);
			}
		};
		
		mHandler = new Handler();

		/* google play games added code */
		findViewById(R.id.sign_in_button).setOnClickListener(this);
		findViewById(R.id.sign_out_button).setOnClickListener(this);

		// Create the Google Api Client with access to Plus and Games
		mGoogleApiClient = new GoogleApiClient.Builder(this)
				.addConnectionCallbacks(this)
				.addOnConnectionFailedListener(this)
				.addApi(Plus.API).addScope(Plus.SCOPE_PLUS_LOGIN)//login with google
				.addApi(Games.API).addScope(Games.SCOPE_GAMES)//have google play games installed
				.addApi(Drive.API).addScope(Drive.SCOPE_APPFOLDER)//have drive installed
				.build();
    }
    
    public void playGame(View view) {

		// Loading Toast
		loadMessage.show();
    	Settings.SHOW_FPS = false;
    	mHandler.post(gameLauncher);
    }
    
    public void playGameWithFPS(View view) {

		// Loading Toast
		loadMessage.show();
    	Settings.SHOW_FPS = true;
		mHandler.post(gameLauncher);
    }
    
    public void showScore(View view) {
    	Intent myIntent = new Intent (this, HighScoreActivity.class);
    	startActivity(myIntent);
    }
    
    public void showInfo(View view) {
    	Intent myIntent = new Intent (this, Info.class);
    	startActivity (myIntent);
    }
    
    protected void onActivityResult (int requestCode, int resultCode, Intent data) {
    	if (resultCode == 1) {
    		showDialog(1);
    		mHandler.postDelayed(new Runnable() {
				
				public void run() {
					mPlayButton.setClickable(true);
					mPlayButton.getBackground().clearColorFilter();
				}
			}, 10000);
    	} else {
    		mPlayButton.setClickable(true);
    		mPlayButton.getBackground().clearColorFilter();
    	}
    	
    }
    
    public void donate(View view) {
    	Intent browserIntent = new Intent("android.intent.action.VIEW", Uri.parse(Settings.URL_DONATE));
    	startActivity(browserIntent);
    }
    
    protected Dialog onCreateDialog(int id) {
    	return new AlertDialog.Builder(this)
		  .setTitle("Error while changing view")
		  .setMessage("System needs some time to free memory. Please try again in 10 seconds.")
		  .setCancelable(true)
		  .create();
    }

	/* below is all google play games added code */
	@Override
	public void onClick(View view) {
		if (view.getId() == R.id.sign_in_button) {
			// start the asynchronous sign in flow
			mSignInClicked = true;
			mGoogleApiClient.connect();
		}
		else if (view.getId() == R.id.sign_out_button) {
			// sign out.
			/*
			mSignInClicked = false;
			Games.signOut(mGoogleApiClient);
			mGoogleApiClient.disconnect();
			*/

			//try this
			// user explicitly signed out, so turn off auto sign in
			mExplicitSignOut = true;
			if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
				Games.signOut(mGoogleApiClient);
				mGoogleApiClient.disconnect();
			}

			// show sign-in button, hide the sign-out button
			findViewById(R.id.sign_in_button).setVisibility(View.VISIBLE);
			findViewById(R.id.sign_out_button).setVisibility(View.GONE);
		}
	}

	@Override
	public void onConnected(Bundle connectionHint) {
		// show sign-out button, hide the sign-in button

		findViewById(R.id.sign_in_button).setVisibility(View.GONE);
		findViewById(R.id.sign_out_button).setVisibility(View.VISIBLE);

		// (your code here: update UI, enable functionality that depends on sign in, etc)
		Log.d(TAG, "onConnected");
	}

	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {
		if (mResolvingConnectionFailure) {
			// Already resolving
			return;
		}

		// If the sign in button was clicked or if auto sign-in is enabled,
		// launch the sign-in flow
		if (mSignInClicked || mAutoStartSignInFlow) {
			mAutoStartSignInFlow = false;
			mSignInClicked = false;
			mResolvingConnectionFailure = true;

			// Attempt to resolve the connection failure using BaseGameUtils.
			// The R.string.signin_other_error value should reference a generic
			// error string in your strings.xml file, such as "There was
			// an issue with sign in, please try again later."
			if (!BaseGameUtils.resolveConnectionFailure(this,
					mGoogleApiClient, connectionResult,
					RC_SIGN_IN, getResources().getString(R.string.signin_other_error))) {
				mResolvingConnectionFailure = false;
			}
		}

		// Put code here to display the sign-in button
	}

	@Override
	public void onConnectionSuspended(int i) {
		Log.d(TAG, "onConnectionSuspended(): attempting to connect");
		mGoogleApiClient.connect();

	}

	@Override
	protected void onStart() {
		super.onStart();
		if (!mInSignInFlow && !mExplicitSignOut) {
			// auto sign in
			mGoogleApiClient.connect();
		}
	}

}
