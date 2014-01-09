package com.eb.rpg;

import java.util.Locale;

import com.jinoh.ruby.marshal.Marshallable;

public class Named implements Marshallable {

	public int id = 0;
	public String name = "None";
	
	@Override
	public String toString () {
		return String.format(Locale.US, "%03d: %s", id, name);
	}
	
}
