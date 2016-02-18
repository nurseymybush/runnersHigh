/*
 * HighscoreActivity
 * runnersHigh 1.0
 * 
 * _DESCRIPTION:
 * 	Highscore Activity itself - shows highscores of user 
 */

package com.unocode.runnershigh_enhanced;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.TableRow.LayoutParams;

import com.unocode.highscore.HighscoreAdapter;

import javax.net.ssl.HttpsURLConnection;

public class HighScoreActivity extends Activity {
	
	private HighscoreAdapter highScoreAdapter = null;
	
	private static final String POST_HIGHSCORE_URL = Settings.HIGHSCORE_POST_URL; // "http://rh.fidrelity.at/post/post_highscore.php";
	private static final String GET_HIGHSCORE_URL = Settings.HIGHSCORE_GET_URL; // "http://rh.fidrelity.at/best.php";
	
	private TableLayout highscoreTable;
	private Context context2;
	
	// ---------------------------------------------------


    @Override
    public void onCreate(Bundle savedInstanceState) {
		
        super.onCreate(savedInstanceState);
        setContentView(R.layout.highscore);

        highScoreAdapter = new HighscoreAdapter(this);
        highScoreAdapter.open();
        
        highscoreTable = (TableLayout) findViewById(R.id.highscoreTable);
        
        final Context context = this;
		context2 = context;
        
        final Handler handler = new Handler();

        findViewById(R.id.buttonLocalHighscore).setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {

				Toast.makeText(context, R.string.hs_loading_local, Toast.LENGTH_SHORT).show();
				
				handler.postDelayed(new Runnable() {
					
					public void run() {
						showLocalScore();
					}
				}, 500);
			}
		});
        

        findViewById(R.id.buttonOnlineHighscore).setOnClickListener(new OnClickListener() {

			public void onClick(View v) {

				Toast.makeText(context, R.string.hs_loading_online, Toast.LENGTH_SHORT).show();

				handler.postDelayed(new Runnable() {

					public void run() {
						//showOnlineScore(); to try to gix network issue
						executeNetworkThread();
					}
				}, 500);
			}
		});
        
        
        Toast.makeText(context, R.string.hs_loading_local, Toast.LENGTH_SHORT).show();
		
		handler.postDelayed(new Runnable() {
			
			public void run() {
				showLocalScore();
			}
		}, 500);
    }
    
    private void showLocalScore() {
    	
    	highscoreTable.removeAllViews();
    	
    	Cursor c = highScoreAdapter.fetchScores("0");
    	
    	if (c.isAfterLast()) {
            Toast.makeText(this, R.string.hs_no_data, Toast.LENGTH_SHORT).show();
    		return;
    	}
    	
    	int currentPlace = 1;
    	
    	do {

    		final String placeString = ""+(currentPlace++)+".";
    		final String scoreString = c.getString(2);
    		final String nameString = c.getString(1);
    		
    		View additional;
    		
    		if (c.getString(3).equalsIgnoreCase("0")) {
    			additional = new Button(this);
    			
    			final Context context = this;
    			final int id = c.getInt(0);
		       
    			additional.setOnClickListener(new OnClickListener() {
					
					public void onClick(View v) {
						AlertDialog.Builder alert = new AlertDialog.Builder(context);
				
				        alert.setTitle("Push this score online ?");
				        alert.setMessage("Name: " + nameString + "\nScore: " + scoreString);
				
				        // OK
				        alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
				        public void onClick(DialogInterface dialog, int whichButton) {          
				        	// Push score online
				        	if(!isOnline()) {
				        		highScoreAdapter.toastMessage(R.string.hs_error_no_internet);
				        	} else {

								/* CHANCE CODE */
								HashMap<String, String> nameValuePairs = new HashMap<String, String>(2);
								nameValuePairs.put("name", nameString);
								nameValuePairs.put("score", scoreString);
								performPostCall(POST_HIGHSCORE_URL,nameValuePairs);

								highScoreAdapter.updateScore(id, 1);
								highScoreAdapter.toastMessage(R.string.hs_pushed_online);

								runOnUiThread(new Runnable() {

									public void run() {
										showLocalScore();

									}
								});
								/* END OF CHANCE CODE */

				        	}        	
				          }
				        });
				        
				        // CANCEL
				        alert.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
				          public void onClick(DialogInterface dialog, int whichButton) {
				            // Canceled.
				          }
				        });
				        alert.show();  
					}
				});
    			additional.setBackgroundResource(R.drawable.highscore_submit);
    			
    			LayoutParams paramsOfSubmitButton = new LayoutParams(0, LayoutParams.MATCH_PARENT, 3.0f);
            	additional.setLayoutParams(paramsOfSubmitButton);
    		} else {
    			additional = new TextView(this, null, android.R.attr.textAppearanceSmallInverse);
    			((TextView)additional).setText("is online");

        		LayoutParams paramsOfAdditional = new LayoutParams(0, LayoutParams.WRAP_CONTENT, 3.0f);
        		paramsOfAdditional.gravity = Gravity.CENTER_VERTICAL | Gravity.END;
        		additional.setLayoutParams(paramsOfAdditional);
    		}
    		
    		generateLine(placeString, scoreString, nameString, additional);
    		
    	} while(c.moveToNext());
    }
    
    private void showOnlineScore(){
    	if(!isOnline()) {
    		//Toast.makeText(this, R.string.hs_error_no_internet, Toast.LENGTH_SHORT).show();
			Log.e("showOnlineScore", "no internet dummy");
    	} else {
        	try {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						highscoreTable.removeAllViews();
					}
				});
			} catch (Exception e) { e.printStackTrace(); }
	    	try {

				String getURL = GET_HIGHSCORE_URL + "?size=" + Integer.toString(Settings.onlineHighscoreLimit);
				StringBuilder result = new StringBuilder();

				/* CHANCE CODE */
	    		URL url = new URL(getURL);
				HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
				try {
					InputStream in = new BufferedInputStream(urlConnection.getInputStream());

					BufferedReader reader = new BufferedReader(new InputStreamReader(in));
					String line;
					while ((line = reader.readLine()) != null) {
						result.append(line);
					}
					JSONArray jArray = new JSONArray(result.toString());

					String nameString;
					String scoreString;
					String timeStamp;

					for(int i = 0; i < jArray.length(); i++) {
						nameString = jArray.getJSONObject(i).getString("name");
						scoreString = jArray.getJSONObject(i).getString("score");
						timeStamp = jArray.getJSONObject(i).getString("created_at");

						/* to use inside runnable thread below */
						final String nameString2 = nameString;
						final String scoreString2 = scoreString;
						final String timeStamp2 = timeStamp;
						final int j = i;

						try {
							runOnUiThread(new Runnable() {
								@Override
								public void run() {
									View additional = new TextView(context2, null, android.R.attr.textAppearanceSmallInverse);
									((TextView)additional).setText(timeStamp2);

									LayoutParams paramsOfAdditional = new LayoutParams(0, LayoutParams.WRAP_CONTENT, 3.0f);
									paramsOfAdditional.gravity = Gravity.CENTER_VERTICAL | Gravity.END;
									additional.setLayoutParams(paramsOfAdditional);

									generateLine("" + (j + 1), scoreString2, nameString2, additional);
								}
							});
						} catch (Exception e) {
							e.printStackTrace();
						}
						/*View additional = new TextView(this, null, android.R.attr.textAppearanceSmallInverse);
						((TextView)additional).setText(timeStamp);

						LayoutParams paramsOfAdditional = new LayoutParams(0, LayoutParams.WRAP_CONTENT, 3.0f);
						paramsOfAdditional.gravity = Gravity.CENTER_VERTICAL | Gravity.END;
						additional.setLayoutParams(paramsOfAdditional);

						generateLine("" + (i + 1), scoreString, nameString, additional);*/
					}
				} catch (IOException e) { Log.e("Show online score", "IOException"); }
				/* END OF CHANCE CODE */
	    	} catch (Exception e) { e.printStackTrace(); }
    	}
    }
    
    
    private void generateLine(String placeString, String scoreString, String nameString, View additional) {
    	
    	TextView place = new TextView(this, null, android.R.attr.textAppearanceLargeInverse);
		place.setText(placeString);
		LayoutParams paramsOfPlace = new LayoutParams(0, LayoutParams.WRAP_CONTENT, 2.0f);
		paramsOfPlace.gravity = Gravity.CENTER_VERTICAL | Gravity.RIGHT;
		place.setLayoutParams(paramsOfPlace);

		TextView score = new TextView(this, null, android.R.attr.textAppearanceMediumInverse);
		score.setText(scoreString);
		LayoutParams paramsOfScore = new LayoutParams(0, LayoutParams.WRAP_CONTENT, 3.0f);
		paramsOfScore.gravity = Gravity.CENTER;
		score.setLayoutParams(paramsOfScore);
		
		TextView name = new TextView(this, null, android.R.attr.textAppearanceMediumInverse);
		name.setText(nameString);
		LayoutParams paramsOfName = new LayoutParams(0, LayoutParams.WRAP_CONTENT, 10.0f);
		paramsOfName.gravity = Gravity.CENTER_VERTICAL | Gravity.LEFT;
		name.setLayoutParams(paramsOfName);
		

		addLine(place, score, name, additional);
    }
    
    private void addLine(View place, View score, View name, View additional) {
    	TableRow tr = new TableRow(this);

    	tr.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT));

    	tr.addView(place);
    	tr.addView(score);
    	tr.addView(name);
    	tr.addView(additional);
    	
    	highscoreTable.addView(tr);
    	ImageView line = new ImageView(this);
    	line.setBackgroundResource(R.drawable.highscore_line);
    	highscoreTable.addView(line);
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
    protected void onDestroy() {        
        super.onDestroy();
         
        if (highScoreAdapter != null) {
        	highScoreAdapter.close();
        }
    }

	/* CHANCE ADDED CODE TO REPLACE DEPRECATED STUFF */
	//http://stackoverflow.com/questions/9767952/how-to-add-parameters-to-httpurlconnection-using-post
	// website is http://rh.fidrelity.at/
	public String performPostCall(String requestURL,
								   HashMap<String, String> postDataParams) {

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

	class Task implements Runnable {
		@Override
		public void run() {
			android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
			showOnlineScore();
		}
	}

	private void executeNetworkThread() {
		new Thread(new Task()).start();
	}
}
