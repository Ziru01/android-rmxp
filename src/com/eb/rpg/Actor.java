package com.eb.rpg;
import com.eb.rpg.builtin.Table;

public class Actor extends Named {
	public int class_id = 1;
	public int initial_level = 1, final_level = 99, exp_basis = 30, exp_inflation = 30;
	public String character_name = "";
	public int character_hue = 0;
	public String battler_name = "";
	public int battler_hue = 0;
	public Table parameters = new Table(6, 100, 1);
	public int weapon_id = 0, armor1_id = 0, armor2_id = 0, armor3_id = 0, armor4_id = 0;
	public boolean weapon_fix = false, armor1_fix = false, armor2_fix = false, armor3_fix = false, armor4_fix = false;
	
	public void setDefaults () {
		for (int i = 1; i < 100; i++) {
			parameters.setValueAt(0, i, 0, 500+i*50);
			parameters.setValueAt(1, i, 0, 500+i*50);
			parameters.setValueAt(2, i, 0, 50+i*5);
			parameters.setValueAt(3, i, 0, 50+i*5);
			parameters.setValueAt(4, i, 0, 50+i*5);
			parameters.setValueAt(5, i, 0, 50+i*5);
		}
	}
	
	public int[] generateExpList (boolean bToNextLvl) {
		float pow_i = 2.4F + exp_inflation / 100.0F;
		int i = bToNextLvl ? 0 : 1;
		int c = i + 99;
		int[] arr = new int[c];
		arr[0] = 0;
		for (; i < c; ++i)
			arr[i] = (bToNextLvl ? 0 : arr[i-1]) + (int) (exp_basis * Math.pow(i + 3, pow_i) / Math.pow(5,  pow_i));
		return arr;
	}
	
}
