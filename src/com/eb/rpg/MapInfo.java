package com.eb.rpg;

import com.jinoh.ruby.marshal.Marshallable;

public class MapInfo implements Marshallable {

	public String name = "";
	public int parent_id = 0, order = 0;
	public boolean expanded = false;
	public int scroll_x = 0, scroll_y = 0;

}
