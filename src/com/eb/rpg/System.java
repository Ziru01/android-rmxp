package com.eb.rpg;

import com.jinoh.ruby.marshal.Marshallable;

public class System implements Marshallable {
	
	public static class Words implements Marshallable {
		public String gold = "G", hp = "HP", sp = "SP",
				str = "STR", dex = "DEX", agi = "AGI",
				int_ = "INT", atk = "ATK", pdef = "PDEF",
				mdef = "MDEF", weapon = "Weapon", armor1 = "Shield",
				armor2 = "Helmet", armor3 = "Body armor", armor4 = "Accessory",
				attack = "Attack", skill = "Skill", guard = "Defense",
				item = "Item", equip = "Equip";
	}
	
	public static class TestBattler implements Marshallable {
		public int actor_id = 1, level = 1, weapon_id = 0,
				armor1_id = 0, armor2_id = 0, armor3_id = 0, armor4_id = 0;
	}

	public int magic_number = 0, party_members[] = new int[]{1};
	public String elements[] = new String[] {null, ""},
			switches[] = new String[] {null, ""},
			variables[] = new String[] {null, ""},
			windowskin_name = "", title_name = "", gameover_name = "",
			battle_transition = "";
	public AudioFile title_bgm = new AudioFile(),
			battle_bgm = new AudioFile(),
			battle_end_me = new AudioFile(),
			gameover_me = new AudioFile(),
			cursor_se = new AudioFile("", 80),
			decision_se = new AudioFile("", 80),
			cancel_se = new AudioFile("", 80),
			buzzer_se = new AudioFile("", 80),
			equip_se = new AudioFile("", 80),
			shop_se = new AudioFile("", 80),
			save_se = new AudioFile("", 80),
			load_se = new AudioFile("", 80),
			batle_start_se = new AudioFile("", 80),
			escape_se = new AudioFile("", 80),
			actor_collapse_se = new AudioFile("", 80),
			enemy_collapse_se = new AudioFile("", 80);
	public Words words = new Words();
	public TestBattler[] test_battlers = new TestBattler[0];
	public int test_troop_id = 1,
			start_map_id = 1, start_x = 0, start_y = 0;
	public String battleback_name = "", battler_name = "";
	public int battler_hue = 0, edit_map_id = 1;

}
