package com.ecs.android.sample.twitter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import oauth.signpost.OAuth;
import twitter4j.Status;
import android.app.ListActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class AndroidTwitterSample extends ListActivity {

	private SharedPreferences prefs;
	private final Handler mTwitterHandler = new Handler();
	private TextView loginStatus;

	final Runnable mUpdateTwitterNotification = new Runnable() {
		public void run() {
			Toast.makeText(getBaseContext(), "Tweet sent !", Toast.LENGTH_LONG)
					.show();
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		this.prefs = PreferenceManager.getDefaultSharedPreferences(this);

		loginStatus = (TextView) findViewById(R.id.login_status);
		Button tweet = (Button) findViewById(R.id.btn_tweet);
		Button login = (Button) findViewById(R.id.btn_login);
		Button clearCredentials = (Button) findViewById(R.id.btn_clear_credentials);

		login.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent i = new Intent(getApplicationContext(),
						PrepareRequestTokenActivity.class);
				i.putExtra("tweet_msg", getTweetMsg());
				startActivity(i);
			}
		});

		tweet.setOnClickListener(new View.OnClickListener() {
			/**
			 * Send a tweet. If the user hasn't authenticated to Tweeter yet,
			 * he'll be redirected via a browser to the twitter login page. Once
			 * the user authenticated, he'll authorize the Android application
			 * to send tweets on the users behalf.
			 */
			public void onClick(View v) {
				if (TwitterUtils.isAuthenticated(prefs)) {
					sendTweet();
				} else {
					Intent i = new Intent(getApplicationContext(),
							PrepareRequestTokenActivity.class);
					i.putExtra("tweet_msg", getTweetMsg());
					startActivity(i);
				}
			}
		});

		clearCredentials.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				clearCredentials();
				updateLoginStatus();
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();
		updateLoginStatus();
	}

	public void updateLoginStatus() {
		loginStatus.setText("Logged into Twitter : "
				+ TwitterUtils.isAuthenticated(prefs));
		fillData();
	}

	private void fillData() {
		// TODO Auto-generated method stub
		try {
			List<Status> s = TwitterUtils.getTweets(prefs);
			List<String> all = new ArrayList<String>();
			for (Status t : s) {
				all.add(t.getUser().getName() + ": " + t.getText());
			}
			ArrayAdapter<String> aa = new ArrayAdapter<String>(this,
					R.layout.tweet_row, all);
			setListAdapter(aa);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private String getTweetMsg() {
		return "Tweeting from Android App at " + new Date().toLocaleString();
	}

	public void sendTweet() {
		Thread t = new Thread() {
			public void run() {

				try {
					TwitterUtils.sendTweet(prefs, getTweetMsg());
					mTwitterHandler.post(mUpdateTwitterNotification);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}

		};
		t.start();
	}

	private void clearCredentials() {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);
		final Editor edit = prefs.edit();
		edit.remove(OAuth.OAUTH_TOKEN);
		edit.remove(OAuth.OAUTH_TOKEN_SECRET);
		edit.commit();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.tweethome_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.menu_refresh:
			fillData();
			return true;
		case R.id.menu_login:
			Intent i = new Intent(getApplicationContext(),
					PrepareRequestTokenActivity.class);
			i.putExtra("tweet_msg", getTweetMsg());
			startActivity(i);
			return true;
		case R.id.menu_clear_login_info:
			clearCredentials();
			updateLoginStatus();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
}