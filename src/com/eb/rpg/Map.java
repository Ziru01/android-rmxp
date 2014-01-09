package com.eb.rpg;

import java.util.HashMap;

import android.annotation.SuppressLint;

import com.eb.rpg.builtin.Table;
import com.jinoh.ruby.marshal.Marshallable;

public class Map implements Marshallable {

	public int tileset_id = 1, width, height;
	public boolean autoplay_bgm = false;
	public AudioFile bgm = new AudioFile();
	public boolean autoplay_bgs = false;
	public AudioFile bgs = new AudioFile("", 80);
	public int encounter_list[] = new int[0], encounter_step = 30;
	public Table data;
	
	@SuppressLint("UseSparseArrays")
	public java.util.Map<Integer, Event> events = new HashMap<Integer, Event>();
	
	public Map () { }
	
	public Map (int width, int height) {
		data = new Table (width, height, 3);
	}

}
