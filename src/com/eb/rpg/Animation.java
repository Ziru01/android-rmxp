package com.eb.rpg;

import com.eb.rpg.builtin.Color;
import com.eb.rpg.builtin.Table;
import com.jinoh.ruby.marshal.Marshallable;

public class Animation extends Named {

	public static class Frame implements Marshallable {
		public int cell_max = 0;
		public Table cell_data = new Table(0, 0, 0);
	}

	public static class Timing implements Marshallable {
		public int frame = 0;
		public AudioFile se = new AudioFile("", 80);
		public int flash_scope = 0;
		public Color flash_color = new Color(255,255,255,255);
		public int flash_duration = 5, condition = 0;
	}
	
	public String animation_name = "";
	public int animation_hue = 0, position = 1, frame_max = 1;
	public Frame[] frames = new Frame[]{new Frame()};
	public Timing[] timings = new Timing[0];

}
