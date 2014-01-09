package com.eb.rpg;

import com.jinoh.ruby.marshal.Marshallable;

public class MoveRoute implements Marshallable {

	public boolean repeat = true, skippable = false;
	public MoveCommand[] list = new MoveCommand[]{new MoveCommand()};

}
