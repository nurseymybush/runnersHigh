/*
 * HighscoreActivity
 * runnersHigh 1.0
 * 
 * _DESCRIPTION:
 * 	Highscore form Activity - shows input field to save score and name 
 */

package com.unocode.runnershigh_enhanced;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import android.os.AsyncTask;
import android.view.View.OnKeyListener;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import com.unocode.highscore.HighscoreAdapter;

import javax.net.ssl.HttpsURLConnection;

public class HighScoreForm extends Activity {
	
	private HighscoreAdapter highScoreAdapter = null;
	private EditText nameField;
	private TextView scoreField;
	private Integer score;
	private CheckBox checkboxPushOnline;
	// ---------------------------------------------------
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	requestWindowFeature(Window.FEATURE_NO_TITLE);  
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
		
        super.onCreate(savedInstanceState);
        setContentView(R.layout.highscoreform);
        
        highScoreAdapter = new HighscoreAdapter(this);
        highScoreAdapter.open();
        
        // Find form elements
        nameField = (EditText) findViewById(R.id.title);
        nameField.setSingleLine(true);
        nameField.setOnKeyListener(new OnKeyListener() {
			
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (keyCode == KeyEvent.KEYCODE_ENTER) {
					// hide keyboard when ENTER is pressed
					InputMethodManager imm = (InputMethodManager)getApplicationContext().getSystemService(
							Context.INPUT_METHOD_SERVICE);
					imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
				}
				return false;
			}
		});
        
        scoreField = (TextView) findViewById(R.id.score);
        checkboxPushOnline = (CheckBox) findViewById(R.id.postOnline);
        Button confirmButton = (Button) findViewById(R.id.confirm);
     
        // Performe save
        confirmButton.setOnClickListener(new View.OnClickListener() {
        	public void onClick(View view) {
        		saveState();
        	}
        });        
        
        // Set Checkbox true if device is connected to internet
        if(isOnline())
        	checkboxPushOnline.setChecked(true);
        
        // Get Score
        score = (savedInstanceState == null) ? null : (Integer) savedInstanceState.getSerializable("score");
		if (score == null) {
			Bundle extras = getIntent().getExtras();
			score = extras != null ? extras.getInt("score") : null;
		}
		//scoreField.setText(score.toString());
		scoreField.setText(String.format("%03d",score));
		
		// Get Last Saved Name
		Cursor cursor = highScoreAdapter.fetchLastEntry();
		startManagingCursor(cursor);
		if(cursor.getCount() > 0) {
			nameField.setText(cursor.getString(cursor.getColumnIndexOrThrow(HighscoreAdapter.KEY_NAME)));
		}
		cursor.close();		
    }
    
    // ---------------------------------------------------
    // Save Entry
    private void saveState() {
    	String name 	=  nameField.getText().toString();
        String score 	=  scoreField.getText().toString();

        int isonline = 0;
        
        if(name.length() > 0) { 
                    
        	// Save score online
        	if(checkboxPushOnline.isChecked()) {        	      		
        		
        		if(!isOnline()) {
        			highScoreAdapter.toastMessage(R.string.hs_error_no_internet);
        		} else {

					/* CHANCE CODE */
					HashMap<String, String> nameValuePairs = new HashMap<String, String>(2);
					nameValuePairs.put("name", name);
					nameValuePairs.put("score", score);
					//performPostCall(Settings.HIGHSCORE_POST_URL,nameValuePairs);


					PerformPostCallClass myTask = new PerformPostCallClass(nameValuePairs);
					myTask.execute();

					isonline = 1;
        		}
        	}
        	
        	// Save HS locally
        	try {
                highScoreAdapter.createHighscore(score, name, isonline);
            } catch (Exception e) {
                Log.w(Settings.LOG_TAG, "create highscore threw an exception");
                Log.w(Settings.LOG_TAG, "Maybe a double attempt? HTC Sensation does that for example");
                return;
            }       	
        	
        	highScoreAdapter.close();
        	
        	setResult(RESULT_OK);
        	finish();
        } else {
        	highScoreAdapter.toastMessage(R.string.hs_error_name_empty);
        }
    }

    // ---------------------------------------------------------
	// Check if user is connected to the internet
	public boolean isOnline() {		
	    ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo ni = cm.getActiveNetworkInfo();
	    /*if (ni != null && ni.isAvailable() && ni.isConnected()) {
	        return true;
	    } else {
	        return false; 
	    }*/
		return (ni != null && ni.isAvailable() && ni.isConnected());
	}
    
    // ---------------------------------------------------------
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        saveState();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override    
    protected void onDestroy() {        
        super.onDestroy();
         
        if (highScoreAdapter != null) {
        	highScoreAdapter.close();
        }
    }

	/* CHANCE ADDED CODE TO REPLACE DEPRECATED STUFF */
	//http://stackoverflow.com/questions/9767952/how-to-add-parameters-to-httpurlconnection-using-post
	/*
	public String performPostCall(String requestURL, HashMap<String, String> postDataParams) {

		URL url;
		String response = "";
		try {
			url = new URL(requestURL);

			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setReadTimeout(15000);
			conn.setConnectTimeout(15000);
			conn.setRequestMethod("POST");
			conn.setDoInput(true);
			conn.setDoOutput(true);

			OutputStream os = conn.getOutputStream();
			BufferedWriter writer = new BufferedWriter(
					new OutputStreamWriter(os, "UTF-8"));
			writer.write(getPostDataString(postDataParams));

			writer.flush();
			writer.close();
			os.close();
			int responseCode=conn.getResponseCode();

			if (responseCode == HttpsURLConnection.HTTP_OK) {
				String line;
				BufferedReader br=new BufferedReader(new InputStreamReader(conn.getInputStream()));
				while ((line=br.readLine()) != null) {
					response+=line;
				}
			}
			else {
				response="";

			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return response;
	}*/


	/*Chance CODE */
	//did this change to get rid of the "network on main thread error"
	//http://stackoverflow.com/questions/21519369/passing-hashmap-to-asynctask-and-convert-it-to-jsonobject
	class PerformPostCallClass extends AsyncTask<Void, Void, String> {

		private HashMap<String, String> postDataParams;

		public PerformPostCallClass (HashMap<String,String> m ){
			this.postDataParams = m ;
		}
    	private Exception exception;

    	protected String doInBackground(Void ...params) {

    		String response = "";

        	try {
            	URL url = new URL(Settings.HIGHSCORE_POST_URL);

            	HttpURLConnection conn = (HttpURLConnection) url.openConnection();
				conn.setReadTimeout(15000);
				conn.setConnectTimeout(15000);
				conn.setRequestMethod("POST");
				conn.setDoInput(true);
				conn.setDoOutput(true);

				OutputStream os = conn.getOutputStream();
				BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
				writer.write(getPostDataString(postDataParams));

				writer.flush();
				writer.close();
				os.close();
				int responseCode = conn.getResponseCode();

				if (responseCode == HttpsURLConnection.HTTP_OK) {
					String line;
					BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
					while ((line = br.readLine()) != null) {
						response += line;
					}

				} else {
					response = "";
				}

        	} catch (Exception e) {
            	this.exception = e;
            	return null;
        	}

        	return response;
    	}

    //protected void onPostExecute(String result) {
        // TODO: check this.exception
        // TODO: do something with the feed
    //}
	}


	private String getPostDataString(HashMap<String, String> params) throws UnsupportedEncodingException {
		StringBuilder result = new StringBuilder();
		boolean first = true;
		for(Map.Entry<String, String> entry : params.entrySet()){
			if (first)
				first = false;
			else
				result.append("&");

			result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
			result.append("=");
			result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
		}

		return result.toString();
	}
}
