package com.eb.rpg;

import com.eb.rpg.builtin.Table;
import com.jinoh.ruby.marshal.Marshallable;

public class ClassActor extends Named {
	
	public static class Learning implements Marshallable {
		public int level = 1, skill_id = 1;
	}

	public int position = 0;
	public int[] weapon_set;
	public int[] armor_set;
	public Table element_ranks = new Table(1, 1, 1);
	public Table state_ranks = new Table(1, 1, 1);
	public Learning[] learnings = new Learning[0];
	
}
