package com.project.btvoting;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ViewPollActivity extends Activity {

	static String TAG = "viewPoll";

	int numPolls;
	protected LinearLayout pollOptions;
	protected List<String> options;
	protected List<Integer> counts;
	protected List<String> pollNames;
	protected String pollName;
	TextView topName;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		options = new ArrayList<String>();
		counts = new ArrayList<Integer>();
		pollNames = new ArrayList<String>();
		pollName = "";
		initLayout();
	}

	private void initLayout() {
		setContentView(R.layout.activity_view);
		pollOptions = (LinearLayout) findViewById(R.id.pollList);

		// load the list of poll names to view
		CreatePollActivity.loadArray(MainActivity.POLL_NAMES, pollNames, getBaseContext());

		Boolean pale = true;

		for (String name : pollNames) {
			LinearLayout box = new LinearLayout(getBaseContext());
			box.setOrientation(LinearLayout.VERTICAL);
			pollOptions.addView(box, LinearLayout.LayoutParams.WRAP_CONTENT);
			// load the poll options
			CreatePollActivity.loadArray(name, options, getBaseContext());

			// load the poll counts
			CreatePollActivity.loadIntArray((name + MainActivity.POLL_COUNTS), counts,
					getBaseContext());

			TextView tv = new TextView(getBaseContext());
			tv.setTextColor(Color.BLACK);
			tv.setText(name);
			if (pale) {
				box.setBackgroundColor(Color.TRANSPARENT);
			} else {
				box.setBackgroundColor(Color.LTGRAY);
			}

			// add poll name
			box.addView(tv, LinearLayout.LayoutParams.WRAP_CONTENT);

			// add each option
			for (int i = 0; i < options.size(); i++) {
				tv = new TextView(getBaseContext());
				tv.setTextColor(Color.BLACK);
				tv.setText("\t" + (i + 1) + ") " + options.get(i) + "\t\t (" + counts.get(i) + ")");
				box.addView(tv, LinearLayout.LayoutParams.WRAP_CONTENT);
			}
			pale = !pale;

			box.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					TextView theNameTV = (TextView) ((LinearLayout) v).getChildAt(0);
					final String name = theNameTV.getText().toString();

					String theUrl = getGraphUrl(name);

					ImageView iv = new ImageView(getBaseContext());

					Log.d(TAG, "THE URL IS " + theUrl);

					new DownloadImageTask(iv).execute(theUrl);

					((LinearLayout) v).addView(iv, LinearLayout.LayoutParams.WRAP_CONTENT);

					//					URL url = new URL(
					//							"http://image10.bizrate-images.com/resize?sq=60&uid=2216744464");
					//					Bitmap bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream());
					//					imageView.setImageBitmap(bmp);
					// https://chart.googleapis.com/chart?cht=p3&chd=t:60,40&chs=250x100&chl=Hello|World
				}
			});

			box.setOnLongClickListener(new OnLongClickListener() {

				public boolean onLongClick(final View v) {
					TextView theNameTV = (TextView) ((LinearLayout) v).getChildAt(0);
					final String name = theNameTV.getText().toString();
					AlertDialog.Builder ad = new AlertDialog.Builder(ViewPollActivity.this);
					ad.setTitle("Delete poll");
					ad.setMessage("Are you sure you want to delete the poll \"" + name + "\"?");
					ad.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							// delete the poll
							// load the list of poll names
							CreatePollActivity.loadArray(MainActivity.POLL_NAMES, pollNames,
									getBaseContext());
							pollNames.remove(name);
							// save it back modified
							CreatePollActivity.saveArray(MainActivity.POLL_NAMES, pollNames,
									getBaseContext());
							// remove poll views
							pollOptions.removeView(v);
							// recolor the background
							Boolean light = true;
							for (int i = 0; i < pollOptions.getChildCount(); i++) {
								int color = Color.TRANSPARENT;
								if (!light) {
									color = Color.LTGRAY;
								}
								pollOptions.getChildAt(i).setBackgroundColor(color);
								light = !light;
							}
						}
					});
					ad.setNegativeButton("No", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							// do nothing
						}
					}).show();
					return false;
				}
			});
		}

	}

	private String getGraphUrl(String name) {
		// pop up a pie graph that shows results 
		String urlString = "http://chart.googleapis.com/chart?cht=p3&chd=t:";
		ArrayList<String> opts = new ArrayList<String>();
		ArrayList<Integer> nums = new ArrayList<Integer>();

		// load the poll counts
		CreatePollActivity.loadIntArray((name + MainActivity.POLL_COUNTS), nums, getBaseContext());
		for (Integer num : nums) {
			urlString = urlString.concat(num + ",");
		}

		// if you added any nums remove the last ,
		if (nums.size() > 0) {
			urlString = urlString.substring(0, urlString.length() - 1);
		}

		urlString += "&chs=250x100&chl=";

		//load the options
		CreatePollActivity.loadArray(name, opts, getBaseContext());
		for (String string : opts) {
			urlString = urlString.concat(string + "|");
		}

		// if you added any options remove the last |
		if (opts.size() > 0) {
			urlString = urlString.substring(0, urlString.length() - 1);
		}
		return urlString;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_other, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.returnToMain:
			Intent myIntent = new Intent(ViewPollActivity.this, MainActivity.class);
			ViewPollActivity.this.startActivity(myIntent);
			return true;
		}
		return false;
	}

	public boolean saveArray(String key, List<String> options) {
		SharedPreferences prefs = getApplicationContext().getSharedPreferences(
				MainActivity.POLL_PREFS, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = prefs.edit();
		// I don't think you need to remove first but oh well
		editor.remove(key + "Size"); // remove what might be there
		editor.putInt(key + "Size", options.size());

		for (int i = 0; i < options.size(); i++) {
			editor.remove(key + i); // remove what might be there
			editor.putString(key + i, options.get(i)); // put the right thing
		}

		return editor.commit();
	}

	private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
		ImageView bmImage;

		public DownloadImageTask(ImageView bmImage) {
			this.bmImage = bmImage;
		}

		protected Bitmap doInBackground(String... urls) {
			String urldisplay = urls[0];
			Bitmap mIcon11 = null;
			try {
				InputStream in = new java.net.URL(urldisplay).openStream();
				mIcon11 = BitmapFactory.decodeStream(in);
			} catch (Exception e) {
				Log.e("Error", e.getMessage());
				e.printStackTrace();
			}
			return mIcon11;
		}

		protected void onPostExecute(Bitmap result) {
			bmImage.setImageBitmap(result);
		}
	}

}
