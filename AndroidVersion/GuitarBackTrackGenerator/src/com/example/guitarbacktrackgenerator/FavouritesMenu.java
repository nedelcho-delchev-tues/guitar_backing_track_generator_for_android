package com.example.guitarbacktrackgenerator;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import android.os.Bundle;
import android.os.Environment;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

/**
 * A menu where the user can manage his favourite tracks as well as his recordings
 * @author Georgi Ivanov
 */

public class FavouritesMenu extends Activity{

	Button buttonPlay, buttonSwitchToOtherView, buttonSortBy, buttonRemoveTrack, buttonRemoveAllTracks, buttonExit;
	TextView title, textViewFavouritesMenu;
	TextView[] textView;
	ArrayList<String[]> favouriteTracks = null, recordings = null;
	LinearLayout linearLayout;
	Spinner spinner;
	String trackSelectedName = "", currentView = "favourites", favouritesSortedBy = "none", recordingsSortedBy = "none";
	int viewCounter = 0, sortByCounter = -1, otherViewSortByCounter = -1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_favourites_menu);
		buttonPlay = (Button) findViewById(R.id.buttonPlay);
		buttonRemoveTrack = (Button) findViewById(R.id.buttonRemoveTrack);
		buttonRemoveAllTracks = (Button) findViewById(R.id.buttonRemoveAllTracks);
		buttonSwitchToOtherView = (Button) findViewById(R.id.buttonSwitchToOtherView);
		buttonSwitchToOtherView.setText("Switch To Recordings");
		buttonExit = (Button) findViewById(R.id.buttonExit);
		buttonSortBy = (Button) findViewById(R.id.buttonSortBy);
		title = (TextView) findViewById(R.id.Title);
		textViewFavouritesMenu = (TextView) findViewById(R.id.textViewFavouritesMenu);
		linearLayout = (LinearLayout) findViewById(R.id.LinearLayout);



		try {
			takeTracksFromCsv();
			takeSortingInfo();
			selectingTracks(printTracks(viewCounter));
			buttonSortBy.setText("Sort By: " + favouritesSortedBy);
			sortByCounter = getSortByToInt(favouritesSortedBy);
		} catch (IOException e) {
			e.printStackTrace();
		}


		buttonPlay.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Bundle newBundle = new Bundle();
				if(("").equals(trackSelectedName)){
					String text = "Please select a track first!";
					Toast toast = Toast.makeText(FavouritesMenu.this, text, Toast.LENGTH_LONG);
					toast.show();
				}else{
					CsvWriter newCsvWriter = new CsvWriter();
					try {
						newCsvWriter.changeNumberOfTimesPlayed(trackSelectedName, currentView+".csv", FavouritesMenu.this);
						takeTracksFromCsv();
					} catch (IOException e1) {
						e1.printStackTrace();
					}
					CsvReader newCsvReader = new CsvReader();
					try {
						newBundle.putStringArray(null, newCsvReader.findTrackByName(currentView+".csv", trackSelectedName, FavouritesMenu.this));
					} catch (IOException e) {
						e.printStackTrace();
					}

					if(currentView.equals("favourites")){
						Intent VideoPlayer = new Intent(FavouritesMenu.this, VideoPlayer.class);
						VideoPlayer.putExtras(newBundle);
						FavouritesMenu.this.startActivity(VideoPlayer);
					}else if(currentView.equals("recordings")){
						Intent MusicPlayer = new Intent(FavouritesMenu.this, MusicPlayer.class);
						MusicPlayer.putExtras(newBundle);
						FavouritesMenu.this.startActivity(MusicPlayer);
					}
				}
			}
		});

		buttonSwitchToOtherView.setOnClickListener(new View.OnClickListener() {
			@SuppressLint("DefaultLocale")
			@Override
			public void onClick(View v) {
				if(viewCounter == 0)
					viewCounter++;
				else if(viewCounter == 1)
					viewCounter = 0;

				String otherView = currentView;

				int temp = sortByCounter;
				sortByCounter = otherViewSortByCounter;
				otherViewSortByCounter = temp;

				currentView = changeViewCounter(viewCounter);
				linearLayout.removeAllViews();
				trackSelectedName = "";
				if(currentView.equals("favourites")){
					buttonSortBy.setText("Sort By: " + favouritesSortedBy);
					sortByCounter = getSortByToInt(favouritesSortedBy);
				}else{
					buttonSortBy.setText("Sort By: " + recordingsSortedBy);
					sortByCounter = getSortByToInt(recordingsSortedBy);
				}
				buttonSortBy.setText("Sort By: " + getSortByCounterToAsString(sortByCounter));
				textViewFavouritesMenu.setText(currentView.substring(0, 1).toUpperCase() + currentView.substring(1));
				buttonSwitchToOtherView.setText("Switch to " + otherView.substring(0, 1).toUpperCase() + otherView.substring(1));
				try {
					takeTracksFromCsv();
					selectingTracks(printTracks(viewCounter));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});

		buttonSortBy.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(sortByCounter == 2)
					sortByCounter = 0;
				else
					sortByCounter++;

				try {
					buttonSortBy.setText("Sort By: " + getSortByCounterToAsString(sortByCounter));
					sort(sortByCounter, viewCounter);
				} catch (IOException e) {
					e.printStackTrace();
				}

				trackSelectedName = "";
			}
		});

		buttonRemoveTrack.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(("").equals(trackSelectedName)){
					String text = "Please select a track first!";
					Toast toast = Toast.makeText(FavouritesMenu.this, text, Toast.LENGTH_LONG);
					toast.show();
				}else{
					CsvWriter newCsvWriter = new CsvWriter();
					try {
						newCsvWriter.removeTrack(trackSelectedName, currentView+".csv", FavouritesMenu.this);
					} catch (IOException e) {
						e.printStackTrace();
					}
					linearLayout.removeAllViews();
					if(currentView.equals("recordings")){
						String trackName = null;
						File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath()
								+ "/GuitarRecordings/");
						for(int i = 0; i < recordings.size(); i++){
							if(recordings.get(i)[3].equals(trackSelectedName)){
								trackName = recordings.get(i)[4];
								break;
							}
						}

						trackName += ".3gp";
						if (dir.isDirectory()) 
							new File(dir, trackName).delete();
					}

					String text = "Track successfully removed";
					Toast toast = Toast.makeText(FavouritesMenu.this, text, Toast.LENGTH_LONG);
					toast.show();
					try {
						takeTracksFromCsv();
						selectingTracks(printTracks(viewCounter));
					} catch (IOException e) {
						e.printStackTrace();
					}
					trackSelectedName = "";
				}
			}
		});

		buttonRemoveAllTracks.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				deleteFile(currentView+".csv");
				if(currentView.equals("favourites")){
					favouriteTracks.clear();
				}else if(currentView.equals("recordings")){
					recordings.clear();
				}
				linearLayout.removeAllViews(); // Not sure if OK. Find a better way maybe.
				String text = "All tracks successfully removed";
				Toast toast = Toast.makeText(FavouritesMenu.this, text, Toast.LENGTH_LONG);
				toast.show();

				File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath()
						+ "/GuitarRecordings");

				if (dir.isDirectory()) {
					String[] children = dir.list();
					for (int i = 0; i < children.length; i++) {
						new File(dir, children[i]).delete();
					}
				}

				if(currentView.equals("favourites"))
					favouritesSortedBy = "";
				else 
					recordingsSortedBy = "";

				buttonSortBy.setText("Sort By:");
				trackSelectedName = "";
			}
		});

		buttonExit.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				CsvWriter newCsvWriter = new CsvWriter();
				try {
					updateCsvFiles();
					newCsvWriter.writeInCsv(favouritesSortedBy,recordingsSortedBy,FavouritesMenu.this);
				} catch (IOException e) {
					e.printStackTrace();
				}
				finish();
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public void onBackPressed() {
		CsvWriter newCsvWriter = new CsvWriter();
		try {
			updateCsvFiles();
			newCsvWriter.writeInCsv(favouritesSortedBy,recordingsSortedBy,FavouritesMenu.this);
		} catch (IOException e) {
			e.printStackTrace();
		}
		finish();
	}

	/**
	 * Returns a string according to the viewCounter
	 * @param viewCounter An integer showing the current view
	 * @return "favourites" or "recording" depending on the current view
	 */
	public String changeViewCounter(int viewCounter){
		String[] view = {"favourites", "recordings"};
		return view[viewCounter];
	}

	/**
	 * Returns a string depending ot the sortByCounter
	 * @param sortByCounter An integer showing the current sorting
	 * @return "Name", "Key" or "Most Played" depending on the current sorting
	 */
	public String getSortByCounterToAsString(int sortByCounter){
		String[] sortBy = {"Name", "Key", "Most Played"};
		if(sortByCounter == -1){;
		return "";	
		}else{
			return sortBy[sortByCounter];
		}
	}

	/**
	 * Returns a number depending ot the current sorting
	 * @param str A string holding the current sorting
	 * @return Returns a number depending ot the current sorting
	 */
	public int getSortByToInt(String str){
		int num = -1;
		if(str.equals("Name"))
			num = 0;
		else if(str.equals("Key"))
			num = 1;
		else if(str.equals("Most Played"))
			num = 2;

		return num;
	}	

	/**
	 * Sorting the tracks in the current view
	 * @param sortByCounter Shows by what the tracks should be sorted
	 * @param viewCounter Shows the current view
	 * @throws IOException
	 */
	public void sort(int sortByCounter, int viewCounter) throws IOException{
		ArrayList<String[]> tracks = null;
		if(viewCounter == 0){
			tracks = favouriteTracks;
			favouritesSortedBy = getSortByCounterToAsString(sortByCounter);
		}else if(viewCounter == 1){
			tracks = recordings;
			recordingsSortedBy = getSortByCounterToAsString(sortByCounter);
		}

		Sort newSorter = new Sort(tracks, sortByCounter);
		newSorter.sort();
		tracks = newSorter.getTracks();
		selectingTracks(printTracks(viewCounter));
	}

	/**
	 * Takes the user's favourite tracks and his recordings from the csv files
	 * @throws IOException
	 */
	public void takeTracksFromCsv() throws IOException{
		CsvReader newCsvReader = new CsvReader();
		try {
			favouriteTracks = newCsvReader.readFromInternalStorageCsv("favourites.csv", FavouritesMenu.this);
			recordings = newCsvReader.readFromInternalStorageCsv("recordings.csv", FavouritesMenu.this);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Parses the sorting_info.csv in order to see by what each view is sorted
	 * @throws IOException
	 */
	public void takeSortingInfo() throws IOException{
		CsvReader newCsvReader = new CsvReader();

		File file = new File("data/data/com.example.guitarbacktrackgenerator/files/sorting_info.csv");
		if(file.exists()){
			ArrayList<String []> asd = newCsvReader.readFromInternalStorageCsv("sorting_info.csv", FavouritesMenu.this);
			if(asd.size() == 2){
				favouritesSortedBy = asd.get(0)[0];
				recordingsSortedBy = asd.get(1)[0]; // fuck it..
			}else if(asd.size() == 1)
				favouritesSortedBy = asd.get(0)[0];
		}
	}

	/**
	 * Prints the tracks on current view
	 * @param viewCounter Shows the current view
	 * @return The number of tracks
	 * @throws IOException
	 */
	public int printTracks(int viewCounter) throws IOException{
		ArrayList<String []> tracks = null;
		if(viewCounter == 0)
			tracks = favouriteTracks;
		else if(viewCounter == 1)
			tracks = recordings;

		textView = new TextView[tracks.size()];
		int i;
		for(i = 0; i < tracks.size(); i++) {
			textView[i] = new TextView(this);
		}

		linearLayout.removeAllViews();
		for(i = 0; i < tracks.size(); i++){
			String trackName = tracks.get(i)[3];
			textView[i].setText(trackName);
			textView[i].setTextColor(Color.parseColor("#FFFFFF"));
			textView[i].setTextSize(18);
			linearLayout.setBackgroundColor(Color.TRANSPARENT);
			linearLayout.addView(textView[i]);
		}

		return tracks.size();
	}

	/**
	 * Enables the user to select tracks from the current view
	 * @param numOfTracks The number of tracks on the current view
	 */
	public void selectingTracks(int numOfTracks){
		for(int i = 0; i < numOfTracks; i++) {
			final int num = numOfTracks; // Povrashtam na Javata..
			final TextView trackSelected = textView[i];
			trackSelected.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					for(int i = 0; i < num; i++) {
						textView[i].setTextColor(Color.parseColor("#FFFFFF"));
						textView[i].setBackgroundColor(Color.TRANSPARENT);
					}
					trackSelectedName = (String) trackSelected.getText();
					trackSelected.setTextColor(Color.parseColor("#000000"));
					trackSelected.setBackgroundColor(Color.parseColor("#FFFF00"));
				}
			});
		}
	}

	/**
	 * Updates the csv files when the activity is closed
	 */
	public void updateCsvFiles(){
		CsvWriter newCsvWriter = new CsvWriter();
		deleteFile("favourites.csv");
		for(int i = 0; i < favouriteTracks.size(); i ++){
			try {
				newCsvWriter.writeInInternalStorageCsv(favouriteTracks.get(i), "favourites.csv", FavouritesMenu.this, false);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		deleteFile("recordings.csv");
		for(int i = 0; i < recordings.size(); i ++){
			try {
				newCsvWriter.writeInInternalStorageCsv(recordings.get(i), "recordings.csv", FavouritesMenu.this,false);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}

//Code + 10 => Bugs * 10