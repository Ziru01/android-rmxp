package com.eb.rpg;

import com.jinoh.ruby.marshal.Marshallable;

public class Event extends Named {

	public static class Page implements Marshallable {
		
		public static class Condition implements Marshallable {
			public boolean switch1_valid = false, switch2_valid = false, variable_valid = false, self_switch_valid = false;
			public int switch1_id, switch2_id, variable_id, variable_value;
			public String self_switch_ch = "A";
		}
		
		public static class Graphic implements Marshallable {
			public int tile_id;
			public String character_name = "";
			public int character_hue = 0, direction = 2, pattern = 0, opacity = 255, blend_type = 0;
			
			public static final int DIRECTION_DOWN = 2,
										DIRECTION_LEFT = 4,
										DIRECTION_RIGHT = 6,
										DIRECTION_UP = 8;
		}
		
		public Condition condition = new Condition();
		public Graphic graphic = new Graphic();
		public int move_type = 0, move_speed = 3, move_frequency = 3;
		public MoveRoute move_route = new MoveRoute();
		public boolean walk_anime = true, step_anime = false,
				direction_fix = false, through = false, always_on_top = false;
		public int trigger = 0;
		public EventCommand[] list;
		
	}
	
	public int id = 0;
	public String name = "";
	public int x = 0, y = 0;
	public Page[] pages = new Page[]{new Page()};

}
