package com.eb.rpg.builtin;

import com.jinoh.ruby.marshal.Marshallable;

public class Color implements Marshallable {
	public int red, green, blue, alpha;
	
	public Color () { this (0, 0, 0, 255); }
	public Color (int red, int green, int blue) { this (red, green, blue, 255); }
	public Color (int red, int green, int blue, int alpha) {
		this.red = red; this.green = green; this.blue = blue; this.alpha = alpha;
	}
}
