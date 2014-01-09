package com.eb.rpg;

public class Skill extends Component {

	public int scope = 0, occasion = 1, animation1_id = 0, animation2_id = 0;
	public AudioFile menu_se = new AudioFile("", 80);
	public int common_event_id = 0, sp_cost = 0, power = 0,
			atk_f = 0, eva_f = 0, str_f = 0, dex_f = 0, agi_f = 0, int_f = 100,
			hit = 100, pdef_f = 0, mdef_f = 100, variance = 15,
			element_set[] = new int[0], plus_state_set[] = new int[0], minus_state_set[] = new int[0];

}
