package com.eb.rpg;

import com.jinoh.ruby.marshal.Marshallable;

public class EventCommand implements Marshallable {

	int code, indent;
	Object[] parameters;
	
	public EventCommand () { this(0, 0, null); }
	
	public EventCommand (int code, int indent, Object[] parameters) {
		this.code = code; this.indent = indent; this.parameters = parameters;
	}

}
