package com.eb.rpg;

import com.jinoh.ruby.marshal.Marshallable;

public class MoveCommand implements Marshallable {

	int code;
	Object[] parameters;
	
	public MoveCommand () { this (0, null); }
	
	public MoveCommand (int code, Object[] parameters) {
		this.code = code; this.parameters = parameters;
	}

}
