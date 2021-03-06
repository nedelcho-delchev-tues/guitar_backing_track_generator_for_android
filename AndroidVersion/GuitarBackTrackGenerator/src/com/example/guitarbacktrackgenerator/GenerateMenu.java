package com.example.guitarbacktrackgenerator;

import java.io.IOException;
import java.util.ArrayList;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

/**
 * A menu where the user can choose key, mode and style of the desired backing track
 * @author Georgi Ivanov and Nedelcho Delchev
 */
public class GenerateMenu extends Activity {

	Button buttonPrev,buttonNext, buttonMaj, buttonMin, buttonCalm, buttonHeavy, buttonRadomize, buttonClear, buttonPlay, buttonExit;
	TextView title,textKey,textMode,textStyle, textKeyChosen; 

	String key = "A", mode, style;
	String[] userChoice = new String[3];

	int keyCounter = 0;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_generate_menu);
		buttonPrev = (Button) findViewById(R.id.buttonPrev);
		buttonNext = (Button) findViewById(R.id.buttonNext);
		buttonMaj = (Button) findViewById(R.id.buttonMaj);
		buttonMin = (Button) findViewById(R.id.buttonMin);
		buttonCalm = (Button) findViewById(R.id.buttonCalm);
		buttonHeavy = (Button) findViewById(R.id.buttonHeavy);
		buttonRadomize = (Button) findViewById(R.id.buttonRandomize);
		buttonClear = (Button) findViewById(R.id.buttonClear);
		buttonPlay = (Button) findViewById(R.id.buttonPlay);
		buttonExit = (Button) findViewById(R.id.buttonExit);

		title = (TextView) findViewById(R.id.Title);
		textKey = (TextView) findViewById(R.id.textKey);
		textMode = (TextView) findViewById(R.id.textMode);
		textStyle = (TextView) findViewById(R.id.textStyle);
		textKeyChosen = (TextView) findViewById(R.id.Key);

		changeTextViewColors();

		buttonPrev.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(keyCounter == 0)
					keyCounter = 11;
				else
					keyCounter--;

				changeKey(keyCounter);
			}
		});

		buttonNext.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(keyCounter == 11)
					keyCounter = 0;
				else
					keyCounter++;

				changeKey(keyCounter);
			}
		});

		buttonMaj.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mode = "maj";
				buttonMin.setBackgroundResource(R.drawable.button);
				buttonMaj.setBackgroundResource(R.drawable.button_clicked);
			}
		});

		buttonMin.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mode = "min";
				buttonMin.setBackgroundResource(R.drawable.button_clicked);
				buttonMaj.setBackgroundResource(R.drawable.button);
			}
		});

		buttonCalm.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				style = "calm";
				buttonCalm.setBackgroundResource(R.drawable.button_clicked);
				buttonHeavy.setBackgroundResource(R.drawable.button);
			}
		});

		buttonHeavy.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				style = "heavy";
				buttonCalm.setBackgroundResource(R.drawable.button);
				buttonHeavy.setBackgroundResource(R.drawable.button_clicked);
			}
		});

		buttonRadomize.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				changeKey(getRandomNumber(12));
				if(getRandomNumber(2) == 0){
					mode = "min";
				}else{
					mode = "maj";
				}

				if(getRandomNumber(2) == 0){
					style = "calm";
				}else{
					style = "heavy";
				}
				
				showSelections();
			}
		});

		buttonClear.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				key = "A";
				keyCounter = 0;
				textKeyChosen.setText("A");
				mode = null;
				style = null;
				
				clearSelection();
			}
		});

		buttonPlay.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				userChoice[0] = key;
				userChoice[1] = mode;
				userChoice[2] = style;

				if(mode == null || style == null){
					String text = "Please choose key, mode and style before clicking play!";
					Toast toast = Toast.makeText(GenerateMenu.this, text, Toast.LENGTH_LONG);
					toast.show();
				}else{
					Thread playThread = new Thread(new Runnable() {
						public void run() {
							ArrayList<String[]> tracksThatMatchUserChoice = null;
							try {
								tracksThatMatchUserChoice = FindBackingTracks(userChoice);
							} catch (IOException e) {
								e.printStackTrace();
							}

							if(tracksThatMatchUserChoice.size() == 0){
								String text = "No tracks matching your input... Sorry :(";
								Toast toast = Toast.makeText(GenerateMenu.this, text, Toast.LENGTH_LONG);
								toast.show();
							}else{
								String[] track = getRandomTrack(tracksThatMatchUserChoice);
								Bundle newBundle=new Bundle();
								newBundle.putStringArray(null, track);
								Intent VideoPlayer = new Intent(GenerateMenu.this, VideoPlayer.class);
								VideoPlayer.putExtras(newBundle);
								GenerateMenu.this.startActivity(VideoPlayer); 
							}

						}
					});
					
					playThread.start();
					try {
						playThread.join();
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
				}
			}
		});

		buttonExit.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
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

	/**
	 * Changes the key acording to the keyCounter
	 * @param keyCounter An integer that counts how many times the buttons have been clicked
	 */
	void changeKey(int keyCounter){
		String[] currentKey = {"A", "A#", "B", "C", "C#", "D", "D#", "E", "F", "F#", "G", "G#"};
		key = currentKey[keyCounter];
		textKeyChosen.setText(key);
	}

	/**
	 * Finds all the backing tracks that match the user's choice of key, mode and style
	 * @param userChoice The user's choice of key, mode and style
	 * @return All the tracks that match the user's choice
	 * @throws IOException
	 */
	ArrayList<String[]> FindBackingTracks(final String[] userChoice) throws IOException{
		final CsvReader newCsvReader = new CsvReader();
		return newCsvReader.parseCsv(userChoice,getAssets().open(userChoice[0]+"_Backing_Tracks.csv"));
	}

	/**
	 * Returns a random track from all the tracks that match the user's choice
	 * @param tracksThatMatchUserChoice The tracks that match the user's choice
	 * @return Returns a random track from all the tracks that match the user's choice
	 */
	String[] getRandomTrack(ArrayList<String[]> tracksThatMatchUserChoice){
		int randomNum = (int)(Math.random()*tracksThatMatchUserChoice.size());	
		String[] randomTrack = tracksThatMatchUserChoice.get(randomNum);
		return randomTrack;		
	}

	/**
	 * Generates a random number from 0 to max
	 * @param max The max number the random number can be
	 * @return The random number
	 */
	int getRandomNumber(int max){
		return (int)(Math.random()*max);
	}
	
	void clearSelection(){
		buttonMaj.setBackgroundResource(R.drawable.button);
		buttonMin.setBackgroundResource(R.drawable.button);
		buttonCalm.setBackgroundResource(R.drawable.button);
		buttonHeavy.setBackgroundResource(R.drawable.button);
	}
	
	void showSelections(){
		clearSelection();
		
		if(mode.equals("maj"))
			buttonMaj.setBackgroundResource(R.drawable.button_clicked);
		else if(mode.equals("min"))
			buttonMin.setBackgroundResource(R.drawable.button_clicked);
		
		if(style.equals("heavy"))
			buttonHeavy.setBackgroundResource(R.drawable.button_clicked);
		else if(style.equals("calm"))
			buttonCalm.setBackgroundResource(R.drawable.button_clicked);
	}

	/**
	 * Changes the color of the textViews in the menu
	 */
	void changeTextViewColors(){
		textKey.setTextColor(Color.parseColor("#FFFFFF"));
		textMode.setTextColor(Color.parseColor("#FFFFFF"));
		textStyle.setTextColor(Color.parseColor("#FFFFFF"));
		textKeyChosen.setTextColor(Color.parseColor("#FFFFFF"));
	}
}
