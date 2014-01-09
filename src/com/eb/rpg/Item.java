package com.eb.rpg;

public class Item extends Component {

	public int scope, occasion, animation1_id, animation2_id;
	public AudioFile menu_se = new AudioFile("", 80);
	public int common_event_id = 0, price = 0;
	public boolean consumable = true;
	public int parameter_type = 0, parameter_points = 0,
			recover_hp_rate = 0, recover_hp = 0,
			recover_sp_rate = 0, recover_sp = 0,
			hit = 100, pdef_f = 0, mdef_f = 0, variance = 0,
			element_set[] = new int[0], plus_state_set[] = new int[0], minus_state_set[] = new int[0];

}
