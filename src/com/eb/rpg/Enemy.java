package com.eb.rpg;

import com.eb.rpg.builtin.Table;
import com.jinoh.ruby.marshal.Marshallable;

public class Enemy extends Named {

	public static class Action implements Marshallable {
		public int kind = 0, basic = 0, skill_id = 1,
				condition_turn_a = 0, condition_turn_b = 1,
				condition_hp = 100, condition_level = 1,
				condition_switch_id = 0, rating = 5;
	}

	public String battler_name = "";
	public int battler_hue = 0, maxhp = 500, maxsp = 500,
			str = 50, dex = 50, agi = 50, int_ = 50, atk = 100, pdef = 100, mdef = 100,
			eva = 0, animation1_id = 0, animation2_id = 0;
	public Table element_ranks = new Table(1, 1, 1), state_ranks = new Table(1, 1, 1);
	public Action[] actions = new Action[]{new Action()};
	public int exp = 0, gold = 0, item_id = 0, weapon_id = 0, armor_id = 0, treasure_prob = 100;

}
