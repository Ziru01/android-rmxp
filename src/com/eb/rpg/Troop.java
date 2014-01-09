package com.eb.rpg;

import com.jinoh.ruby.marshal.Marshallable;

public class Troop extends Named {

	public static class Member implements Marshallable {
		public int enemy_id = 1, x = 0, y = 0;
		public boolean hidden = false, immortal = false;
	}
	
	public static class Page implements Marshallable {
		
		public static class Condition implements Marshallable {
			public boolean turn_valid = false, enemy_valid = false,
					actor_valid = false, switch_valid = false;
			public int turn_a = 0, turn_b = 0, enemy_index = 0,
					enemy_hp = 50, actor_id = 1, actor_hp = 50,
					switch_id = 1;
		}
		
		public Condition condition = new Condition();
		public int span = 0;
		public EventCommand[] list = new EventCommand[] {new EventCommand()};
		
	}
	
	public Member[] members;
	public Page[] pages;
	
}
