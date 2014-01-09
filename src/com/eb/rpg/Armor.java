package com.eb.rpg;
import app.create.rpg.R;

public class Armor extends Component {

	public static final int TYPE_SHIELD = 0, TYPE_HELMET = 1, TYPE_BODY_ARMOR = 2, TYPE_ACCESSORY = 3;
	public static final int[] KIND_TO_ID = new int[]{R.id.spinShield, R.id.spinHelmet, R.id.spinBodyArmor, R.id.spinAccessory};
	public int kind = 0, auto_state_id = 0,
			price = 0, pdef = 0, mdef = 0, eva = 0,
			str_plus = 0, dex_plus = 0, agi_plus = 0, int_plus = 0,
			guard_element_set[] = new int[0], guard_state_set[] = new int[0];
	
}
