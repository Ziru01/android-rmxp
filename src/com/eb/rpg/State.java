package com.eb.rpg;

import com.jinoh.ruby.marshal.Marshallable;

public class State implements Marshallable {

	public int animation_id = 0, restriction = 0;
	public boolean nonresistance = false, zero_hp = false,
			cant_get_exp = false, cant_evade = false,
			slip_damage = false;
	public int rating = 5, hit_rate = 100, maxhp_rate = 100, maxsp_rate = 100,
			str_rate = 100, dex_rate = 100, agi_rate = 100, int_rate = 100,
			atk_rate = 100, pdef_rate = 100, mdef_rate = 100, eva = 0;
	public boolean battle_only = true;
	public int hold_turn = 0, auto_release_prob = 0, shock_release_prob = 0,
			guard_element_set[] = new int[0], plus_state_set[] = new int[0],
			minus_state_set[] = new int[0];

}
